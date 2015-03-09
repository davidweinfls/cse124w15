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
import java.util.concurrent.TimeUnit;

public class TwitterHandler implements Twitter.Iface {
    protected Map<String, List <String>> user_subs;
    protected Map<String, List <Tweet>> user_tweets;
    protected int id = 0;

    public TwitterHandler(Map<String, List<String>> map) {
        this.user_subs = map;
        this.user_tweets = new HashMap<String, List<Tweet>> ();
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
            user_tweets.put(handle, new ArrayList<Tweet>());
            System.out.println("created user: " + handle);
        }
    }

    @Override
    public void subscribe(String handle, String theirhandle)
        throws NoSuchUserException
    {
        if (user_subs.containsKey(handle)) {
            if (user_subs.containsKey(theirhandle)) {
                if (user_subs.get(handle).contains(theirhandle)) {
                    System.err.println(handle + " already subscribed " + theirhandle);
                } else {
                    user_subs.get(handle).add(theirhandle);
                }
            } else {
                System.err.println("error - trying to subscribe a non-existent user: " + theirhandle);
                throw new NoSuchUserException();
            }
        } else {
            System.err.println("user: " + handle + " does not exist");
            throw new NoSuchUserException();
        }
    }

    @Override
    public void unsubscribe(String handle, String theirhandle)
        throws NoSuchUserException
    {
        if (user_subs.containsKey(handle)) {
            if (user_subs.containsKey(theirhandle)) {
                if (!user_subs.get(handle).contains(theirhandle)) {
                    System.err.println(handle + " already unsubscribed " + theirhandle);
                } else {
                    user_subs.get(handle).remove(theirhandle);
                }
            } else {
                System.err.println("error - trying to unsubscribe a non-existent user: " + theirhandle);
                throw new NoSuchUserException();
            }
        } else {
            System.err.println("user: " + handle + " does not exist");
            throw new NoSuchUserException();
        }
    }

    public long getTimeInSeconds() {
        long ms = System.currentTimeMillis();
        long s = TimeUnit.MILLISECONDS.toSeconds(ms);
        return s;
    }

    @Override
    public void post(String handle, String tweetString)
        throws NoSuchUserException, TweetTooLongException
    {
        if (user_subs.containsKey(handle)) {
            if (tweetString.length() > 140) {
                System.err.println("tweet exceed maximum length");
                throw new TweetTooLongException();
            }
            // create a new tweet
            id++;
            Tweet t = new Tweet(id, handle, getTimeInSeconds(), 0, tweetString);
            System.out.println("here");
            user_tweets.get(handle).add(t);
            System.out.println("user: " + handle + " posted a tweet, id: " + id);
        } else {
            System.err.println("user: " + handle + " does not exist");
            throw new NoSuchUserException();
        }
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
