Starter files for CSE 124's Project 3
Winter 2015
George Porter <gmporter@cs.ucsd.edu>

To build and run the server:

$ cd src/server
$ thrift --gen java ../thrift/Twitter.thrift
$ ant
$ ./run-server.sh

To build and run the client:

$ cd src/client
$ thrift --gen py ../thrift/Twitter.thrift
$ ./cl.py
