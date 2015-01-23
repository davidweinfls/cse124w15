#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <stdio.h>
#include <iostream>
#include <string.h>
#include <unistd.h>
#include <fstream>
#include <sstream>
#include <map>

using namespace std;

#define CRLF "\r\n"

void parseRequest(const string data, string& url, string& protocol) {
    size_t prev = 0, cur = 0;
    cur = data.find_first_of(" ", prev);
    prev = cur + 1;  // skip the GET method
    // get the url
    cur = data.find_first_of(" ", prev);
    url = data.substr(prev, cur - prev);
    prev = cur + 1;
    // get the protocol version
    cur = data.find_first_of(CRLF, prev);
    protocol = data.substr(prev, cur - prev);
    prev = cur + 1;
}

bool findFile(const string url, string& responseBody, size_t& length) {
    ifstream ifs, errfs;
    string filename;

    if (url[0] == '/' && url.size() == 1) {
        filename = "index.html";
    } else if (url[0] == '/') {
        filename = url.substr(1);
    }

    // open file
    ifs.open(filename.c_str(), ifstream::in);
    // TODO: check if file exist, if permitted
    if (ifs.is_open()) {
        ifs.seekg(0, ifstream::end);
        length = ifs.tellg();
        ifs.seekg(0, ifstream::beg);

        char* buf = new char[length];
        memset(buf, '\0', length);

        if (ifs.good()) {
            ifs.read(buf, length);
        }
        responseBody.append(buf, length);
    } else {
        ifs.close();
        cerr << "cannot open file or file is protected" << endl;
        return false;
    }

}

void prepareResponse(string& response, const string responseBody, const size_t length, const string protocol, bool status) {
    ostringstream s;
    if (status) {
        s << protocol << " 200 " << "OK\r\n";
    } else {
        // report 4XX error
    }
    s << "Content-Length: " << length << CRLF;
    s << "Content-Type: " << "text/html" << CRLF;
    s << CRLF;
    s << responseBody << CRLF;
    response = s.str();
}

int main (int argc, char* argv[]) {
    if (argc != 2) {
        cerr << "No port specified." << endl << "Example: " << argv[0] << " [port]" << endl;
        exit(1);
    }

    int sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    const int so_reuseaddr = 1;
    int rtn = setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (void*)&so_reuseaddr, sizeof(so_reuseaddr));

    struct sockaddr_in address = {0};
    
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = htonl(INADDR_ANY); // handle big or little endian
    address.sin_port = htons(atoi(argv[1]));

    // assign a port number to the socket with bind()
    if (bind(sock, (struct sockaddr*) &address, sizeof(address)) < 0) {
        perror("bind error");
        exit(1);
    }

    // tell sys to allow connection to be made to that port, using listen()
    if (listen(sock, 128) < 0) {
        perror("listen fail");
        exit(1);
    }

    while (1) {
        struct sockaddr_in client_address = {0};
        socklen_t ca_len = 0;

        // call accept() to get a new socket for each client connection
        int csock = accept(sock, (struct sockaddr*) &client_address, &ca_len);


        if (csock < 0) {
            perror("accept failed");
            exit(1);
        }

        //if (fork() == 0) {
            char buf[BUFSIZ];
            ssize_t bytes_read;
            string data;

            // communicate with client via new socket using send(), recv()
            do {
                bytes_read = recv(csock, &buf, BUFSIZ - 1, 0);

                if (bytes_read < 0) {
                    perror("recv failed");
                    exit(1);
                } else if (bytes_read == 0) {
                    cerr << "client disconnected" << endl;
                    exit(1);
                }

                data.append(buf, bytes_read);

                // parse received request
                string url, protocol;
                parseRequest(data, url, protocol);

                // find html file and load
                size_t length;
                string responseBody;
                bool success = findFile(url, responseBody, length);

                // generate response buffer
                string response;
                prepareResponse(response, responseBody, length,
                        protocol, success);

                // prepare response, convert to c_str
                char *buf = new char[response.size()];
                memset(buf, '\0', response.size());
                memcpy(buf, response.c_str(), response.size());

                // send response
                ssize_t bytes_sent = send(csock, buf, response.size(), 0);

                if (bytes_sent < 0) {
                    perror("sent failed");
                    exit(1);
                } else if (bytes_sent < bytes_read) {
                    perror("couldn't send anything");
                    exit(1);
                }

            } while (bytes_read > 0);

            close(csock);
        //}
    } // end of while
}
