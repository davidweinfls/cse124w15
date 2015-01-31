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

using namespace std;

#define CRLF "\r\n"

void parseRequest(const string data, string& url, string& protocol) {

    cout << "Receiving request: " << data << endl;
    
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

int findFile(const string url, string& responseBody, size_t& length) {
    ifstream ifs, errfs;
    string filename;
    int status = 0;

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
        status = 200;
    } else if (!ifs.is_open()) { // 404 Not Found
        ifs.close();
        cerr << "cannot open file or file is protected" << endl;
        status = 404;
    } else { // 401 - Unauthorized
        status = 401;
    } // 403 Forbidden

    return status;
}

bool prepareResponse(string& response, const string responseBody, const size_t length, const string protocol, int status) {
    ostringstream s;
    if (status == 200) {
        s << CRLF << protocol << " 200 " << "OK\r\n";
        s << "Content-Length: " << length << CRLF;
        s << "Content-Type: " << "text/html" << CRLF;
        s << CRLF;
        s << responseBody << CRLF;
        response = s.str();
        return true;
    } else {
        // report 4XX error
        if (status == 404) {
            s << CRLF << protocol << " 404 " << "Not Found" << CRLF;
        } else if (status == 401) {
            s << CRLF << protocol << " 401 " << "Unauthorized" << CRLF;
        } else if (status == 403) {
            s << CRLF << protocol << " 403 " << "Forbidden" << CRLF;
        }
        s << "Content-length: " << length << CRLF;
        s << "Content-Tpye: " << "text/html" << CRLF;
        s << CRLF;
        response = s.str();
        return false;
    }
}

int checkCRLF(const string buf, string& request, ssize_t length) {
    //cout << "string passed in checkCRLF: " << buf << endl;
    
    int i = 0;
    int num_of_CRLF = 0;
    for (; i < buf.size(); ++i) {
        if (buf[i] == '\n') {
            cout << "found a new line" << endl;
        } else if (buf[i] == '\r' && buf[i+1] == '\n') {
            cout << "CRLF found!!!" << endl;
            ++num_of_CRLF;
            if (num_of_CRLF == 2) {
                break;
            } else { // skip this current CRLF
                ++i;
            }
        }
    }
    if (num_of_CRLF == 2) {
        request = request + buf.substr(0, i+1);
    } else {
        request = request + buf;
    }
    return num_of_CRLF;
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
        } else {
            cout << "Connected with socket " << csock << endl;
        }

        if (fork() == 0) {
            char buf[BUFSIZ];
            ssize_t bytes_read;
            int count = 0;
            string request; 

            // communicate with client via new socket using send(), recv()
            do {
                string url, protocol;
                bytes_read = recv(csock, &buf, BUFSIZ - 1, 0);

                cout << "Buf contains: " << buf << endl;
                cout << "bytes read: " << bytes_read << endl;

                if (bytes_read < 0) {
                    cerr << "recv failed" << endl;
                    exit(1);
                } else if (bytes_read == 0) {
                    cerr << "client disconnected" << endl;
                    exit(1);
                }

                // copy buffer content to temp string, clear buffer
                string temp(buf);
                memset(buf, '\0', BUFSIZ);

                // check CRLF and generate a request string
                if (count += checkCRLF(temp, request, bytes_read)) {
                    cout << "request buff: " << request << endl;
                    // found a CRLF
                    if (count == 2) {
                        // parse received request
                        parseRequest(request, url, protocol);
                        count = 0;
                        // request = temp.substr(temp.find_first_of("\r\n"));
                    } else continue; // keep receiving the 2nd CRLF
                } else {
                    // no CRLF found, keep receiving
                    continue;
                }

                // find html file and load
                size_t length;
                string responseBody;
                int status = findFile(url, responseBody, length);

                // generate response buffer
                string response;
                if (prepareResponse(response, responseBody, length,
                        protocol, status)) {
                    cout << "\nGenerate HTTP response" << endl;
                } else {
                    cerr << "\nCannot generate HTTP response" << endl;
                }

                // prepare response, convert to c_str
                char *buf = new char[response.size()];
                memset(buf, '\0', response.size());
                memcpy(buf, response.c_str(), response.size());

                // send response
                ssize_t bytes_sent = send(csock, buf, response.size(), 0);

                if (bytes_sent < 0) {
                    perror("sent failed");
                    exit(1);
                } //else if (bytes_sent < bytes_read) {
                    //perror("couldn't send anything");
                    // should handle 400, 404 request
                //}

            } while (bytes_read > 0);

            close(csock);
            close(sock);
        }
        close(csock);
    } // end of while
    close(sock);
}
