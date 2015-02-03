#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <stdio.h>
#include <iostream>
#include <string.h>
#include <unistd.h>
#include <fstream>
#include <stdio.h>
#include <sstream>
#include <sys/stat.h>
#include <vector>
#include <map>
#include <arpa/inet.h>

using namespace std;

#define CRLF "\r\n"
#define DEFAULT_CONTENT_TYPE "application/octet-stream";
#define SOCKET_TIMEOUT_SEC 20

string doc_root;

map<string, string> contentTypeMap() {
    map<string, string> m;
    m["html"] = "text/html";
    m["png"] = "image/png";
    m["jpg"] = "image/jpeg";
    m["jpeg"] = "image/jpeg";
    m["gif"] = "image/gif";
    m["css"] = "text/css";
    m["js"] = "application/javascript";
    return m;
}

string getContentType(string filename) {
    size_t extIndex = filename.find_last_of(".");
    if (extIndex == string::npos || extIndex == (filename.length() - 1)) {
        return DEFAULT_CONTENT_TYPE;
    }

    string ext = filename.substr(extIndex + 1);
    string contentType = contentTypeMap()[ext];
    if (contentType.length() == 0) {
        return DEFAULT_CONTENT_TYPE;
    }

    return contentType;
}

int parseRequest(const string data, string& method, string& url, string& protocol) {

    cout << "Receiving request: " << data << endl;
    
    size_t prev = 0, cur = 0;
    cur = data.find_first_of(" ", prev);
    // check malform
    method = data.substr(prev, cur - prev);
    prev = cur + 1;  // skip the GET method
    if (method != "GET") return 400;

    // get the url
    cur = data.find_first_of(" ", prev);
    url = data.substr(prev, cur - prev);
    if (url[0] != '/') return 400;
    prev = cur + 1;

    // get the protocol version
    cur = data.find_first_of(CRLF, prev);
    protocol = data.substr(prev, cur - prev);
    if (protocol.substr(0, 5) != "HTTP/") return 400;
    return 200;
}

bool isDirectory(string filename) {
    struct stat st;
    lstat(filename.c_str(), &st);
    if (S_ISDIR(st.st_mode)) {
        return true;
    } else {
        return false;
    }
}

string getFilename(string url) {
    string filename;

    // get rid of leading '/'
    int i = 0;
    while (url[i] == '/') {
        i++;
    }

    filename = url.substr(i);

    // get rid of trailing '/'
    while ((*filename.rbegin()) == '/') {
        filename = filename.substr(0, filename.length() - 1);
    }

    // prepend document root
    filename = doc_root + "/" + filename;

    // if client is accessing a directory, append index.html
    if (isDirectory(filename)) {
        filename = filename + "/index.html";
    }

    return filename;
}

vector<string> &split(const string &s, char delim, vector<string> &elems) {
    stringstream ss(s);
    string item;
    while (getline(ss, item, delim)) {
        elems.push_back(item);
    }
    return elems;
}


vector<string> split(const string &s, char delim) {
    vector<string> elems;
    split(s, delim, elems);
    return elems;
}

bool isFileInDocRoot(string filename) {
    string rel_filename = filename.substr(doc_root.length() + 1);
    int dir_depth = 0;
    vector<string> filename_splitted = split(rel_filename, '/');
    for (vector<string>::const_iterator i = filename_splitted.begin(); i != filename_splitted.end(); ++i) {
        string filename_part = string(*i);

        if (filename_part.compare("..") == 0) {
            --dir_depth;
        } else if (filename_part.length() > 0) {
            ++dir_depth;
        }

        if (dir_depth < 0) {
            return false;
        }
    }

    return true;
}

int findFile(const string filename, string& responseBody, size_t& length) {
    int status = 0;

    if (isFileInDocRoot(filename) == false) {
        status = 400;
        cerr << "client attempted to access file outside of document root" << endl;
        return status;
    }

    // open file
    ifstream ifs(filename.c_str());
    // check if file exists
    if (ifs.good()) {
        struct stat fileStat;
        if (stat(filename.c_str(), &fileStat) < 0) {
            cerr << "cannot get permission status of file" << endl;
        }

        // check read permission of other users
        if (!(fileStat.st_mode & S_IROTH)) {
            ifs.close();
            cerr << "file is not world readable" << endl;
            status = 403;
            return status;
        }

        ifs.seekg(0, ifstream::end);
        length = ifs.tellg();
        ifs.seekg(0, ifstream::beg);

        char* buf = new char[length];
        memset(buf, '\0', length);

        ifs.read(buf, length);

        responseBody.append(buf, length);
        status = 200;
    } else { // 404 Not Found
        ifs.close();
        cerr << "cannot open file or file is protected" << endl;
        status = 404;
    }

    return status;
}

string handleErrorPage(int status, size_t& length) {
    string errorBody, filename;
    ifstream ifs;

    switch(status) {
        case 400: 
            filename = "400.html";
            break;
        case 404: 
            filename = "404.html";
            break;
        case 403:
            filename = "403.html";
            break;
        default:
            break;
    }

    filename = doc_root + "/" + filename;

    ifs.open(filename.c_str(), ifstream::in);
    if (ifs.is_open()) {
        ifs.seekg(0, ifstream::end);
        length = ifs.tellg();
        ifs.seekg(0, ifstream::beg);

        char* buf = new char[length];
        memset(buf, '\0', length);
        if (ifs.good()) {
            ifs.read(buf, length);
        } else {
            cerr << "cannot read error page" << endl;
        }
        errorBody.append(buf, length);
    } else { 
        ifs.close();
        cerr << "cannot open error page" << endl;
    } 
    return errorBody;
}

bool prepareResponse(string& response, const string responseBody, const string type, const size_t length, const string protocol, int status) {
    ostringstream s;
    if (status == 200) {
        s << CRLF << "HTTP/1.1" << " 200 " << "OK\r\n";
        s << "Content-Length: " << length << CRLF;
        s << "Content-Type: " << type << CRLF;
        s << CRLF;
        s << responseBody << CRLF;
        response = s.str();
        return true;
    } else {
        // report 4XX error
        if (status == 404) {
            s << CRLF << "HTTP/1.1" << " 404 " << "Not Found" << CRLF;
        } else if (status == 400) {
            s << CRLF << "HTTP/1.1" << " 400 " << "Bad Request" << CRLF;
        } else if (status == 403) {
            s << CRLF << "HTTP/1.1" << " 403 " << "Forbidden" << CRLF;
        }
        size_t errorLength;
        string errorBody = handleErrorPage(status, errorLength);
        s << "Content-Length: " << errorLength << CRLF;
        s << "Content-Type: " << "text/html" << CRLF;
        s << CRLF;
        s << errorBody << CRLF;
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
        size_t end_of_request = buf.find(CRLF);
        request = buf.substr(0, end_of_request);
    } else {
        request = request + buf;
    }
    return num_of_CRLF;
}

int main (int argc, char* argv[]) {
    if (argc != 3) {
        cerr << "Invalid number of arguments specified." << endl;
        cerr << "Usage: " << argv[0] << " [port] [document root]" << endl;
        exit(1);
    }

    // get the document root; remove trailing slashes
    doc_root = string(argv[2]);
    while ((*doc_root.rbegin()) == '/') {
        doc_root = doc_root.substr(0, doc_root.length() - 1);
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
        cout << "Connected to: " << client_address.sin_addr.s_addr << endl;

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
                string method, url, protocol, responseBody;
                string type = DEFAULT_CONTENT_TYPE;
                int status = 0;
                size_t length;

                // set the socket timeout
                struct timeval tv;
                tv.tv_sec = SOCKET_TIMEOUT_SEC;
                tv.tv_usec = 0;
                setsockopt(csock, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv,sizeof(struct timeval));

                bytes_read = recv(csock, &buf, BUFSIZ - 1, 0);

                cout << "Buf contains: " << buf << endl;
                cout << "bytes read: " << bytes_read << endl;

                if (bytes_read < 0) {
                    cerr << "recv failed" << endl;
                    //if (errno == EWOULDBLOCK) {
                      //  cerr << "recv timeout expired" << endl;
                    //}

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
                    cout << "\n<<<<< request buff: " << request << "\n>>>>>>>\n" << endl;
                    // found a CRLF
                    if (count == 2) {
                        // parse received request
                        status = parseRequest(request, method, url, protocol);
                        count = 0;
                        request = "";
                    } else continue; // keep receiving the 2nd CRLF
                } else {
                    // no CRLF found, keep receiving
                    continue;
                }

                // find html file and load
                if (status != 400) {
                    string filename = getFilename(url);
                    status = findFile(filename, responseBody, length);
                    type = getContentType(getFilename(url));
                } else {
                    cout << "error 400; client error. disconnecting client" << endl;
                    break;
                }
                // generate response buffer
                string response;
                cout << "=======status: " << status << "=======" << endl;
                if (prepareResponse(response, responseBody, type, length,
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

                // disconnect HTTP 1.0 clients
                if (protocol.compare("HTTP/1.0") == 0) {
                    cout << "disconnecting HTTP/1.0 client" << endl;
                    break;
                }
            } while (bytes_read > 0);

            close(csock);
            close(sock);
            exit(0);
        }
        close(csock);
    } // end of while
    close(sock);
}
