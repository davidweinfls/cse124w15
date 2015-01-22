#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <stdio.h>
#include <iostream>
#include <unistd.h>

using namespace std;

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

        if (fork() == 0) {
            string buf (BUFSIZ, 0);
            ssize_t bytes_read;

            // communicate with client via new socket using send(), recv()
            do {
                cout << "recv called" << endl;
                bytes_read = recv(csock, &buf, BUFSIZ - 1, 0);

                if (bytes_read < 0) {
                    perror("recv failed");
                    exit(1);
                } else if (bytes_read == 0) {
                    cerr << "client disconnected" << endl;
                    exit(1);
                }

                cout << buf << endl;

                ssize_t bytes_sent = send(csock, &buf, bytes_read, 0);

                if (bytes_sent < 0) {
                    perror("sent failed");
                    exit(1);
                } else if (bytes_sent < bytes_read) {
                    perror("couldn't send anything");
                    exit(1);
                }

            } while (bytes_read > 0);

            close(csock);
        }
    } // end of while
}
