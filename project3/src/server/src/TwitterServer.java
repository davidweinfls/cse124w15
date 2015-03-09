package edu.ucsd.cse124;

import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TThreadPoolServer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TwitterServer {

    protected static Map<String, List<String>> sampleUsers() {
        Map<String, List<String>> user_subs = 
            new HashMap<String, List<String>>();

        user_subs.put("@david", new ArrayList<String>());

        return user_subs;
    }

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
    TwitterHandler handler = new TwitterHandler(sampleUsers());
    StartsimpleServer(new Twitter.Processor<TwitterHandler>(handler));
 }

}
