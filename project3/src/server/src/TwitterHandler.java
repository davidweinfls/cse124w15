package edu.ucsd.cse124;

import org.apache.thrift.TException;

import edu.ucsd.cse124.AlreadyExistsException;
import edu.ucsd.cse124.NoSuchUserException;
import edu.ucsd.cse124.TweetTooLongException;
import edu.ucsd.cse124.NoSuchTweetException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TwitterHandler implements Twitter.Iface {
    protected Map<String, List <String>> user_subs;
    protected Map<String, List <Long>> user_tweets;

    public TwitterHandler(Map<String, List<String>> map) {
        this.user_subs = map;
        this.user_tweets = new HashMap<String, List<Long>> ();
    }

    @Override
    public void ping() {
        System.out.println("Server receives ping from client.");
    }

    @Override
    public void createUser(String handle) throws AlreadyExistsException
    {
        // check if user already exists
        if (user_subs.containsKey(handle) || user_tweets.containsKey(handle)) {
            System.err.println("create user error");
            throw new AlreadyExistsException();
        } else {
            user_subs.put(handle, new ArrayList<String>());
            user_tweets.put(handle, new ArrayList<Long>());
            System.out.println("created user: " + handle);
        }
    }

    @Override
    public void subscribe(String handle, String theirhandle)
        throws NoSuchUserException
    {
    }

    @Override
    public void unsubscribe(String handle, String theirhandle)
        throws NoSuchUserException
    {
    }

    @Override
    public void post(String handle, String tweetString)
        throws NoSuchUserException, TweetTooLongException
    {
    }

    @Override
    public List<Tweet> readTweetsByUser(String handle, int howmany)
        throws NoSuchUserException
    {
        return null;
    }

    @Override
    public List<Tweet> readTweetsBySubscription(String handle, int howmany)
        throws NoSuchUserException
    {
        return null;
    }

    @Override
    public void star(String handle, long tweetId) throws
        NoSuchUserException, NoSuchTweetException
    {
    }
}
