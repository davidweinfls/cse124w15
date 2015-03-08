package edu.ucsd.cse124;

import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TThreadPoolServer;

public class TwitterServer {

 public static void StartsimpleServer(Twitter.Processor<TwitterHandler> processor) {
  try {
   TServerTransport serverTransport = new TServerSocket(9090);

   //TServer server = new TSimpleServer(
   //  new Args(serverTransport).processor(processor));

   TServer server = new TThreadPoolServer(new
   TThreadPoolServer.Args(serverTransport).processor(processor));

   System.out.println("Starting the multithreaded server...");
   server.serve();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 
 public static void main(String[] args) {
    TwitterHandler handler = new TwitterHandler();
    StartsimpleServer(new Twitter.Processor<TwitterHandler>(handler));
 }

}
