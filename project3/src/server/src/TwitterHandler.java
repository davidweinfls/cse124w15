package edu.ucsd.cse124;

import org.apache.thrift.TException;

import edu.ucsd.cse124.AlreadyExistsException;
import edu.ucsd.cse124.NoSuchUserException;
import edu.ucsd.cse124.TweetTooLongException;
import edu.ucsd.cse124.NoSuchTweetException;

import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TwitterHandler implements Twitter.Iface {
    protected Map<String, List<String>> user_subs;
    protected Map<String, List<Long>> user_tweets;
    protected long id = 0;
    protected Map<Long, Tweet> tweets;
    protected Map<String, List<Long>> user_star_tweets;

    public static Comparator<Tweet> TweetComparator = new Comparator<Tweet>() {
        public int compare(Tweet tweet1, Tweet tweet2) {
 
 
          //ascending order
          return new Long(tweet1.posted).compareTo(new Long(tweet2.posted));
 
          //descending order
          //return fruitName2.compareTo(fruitName1);
        }
 
    };

    public TwitterHandler() {
        this.user_subs = new HashMap<String, List<String>> ();
        this.user_tweets = new HashMap<String, List<Long>> ();
        this.user_star_tweets = new HashMap<String, List<Long>> ();
        this.tweets = new HashMap<Long, Tweet> ();
    }

    @Override
    public void ping() {
        System.out.println("Server receives ping from client.");
    }

    @Override
    public void createUser(String handle) throws AlreadyExistsException
    {
        // check if user already exists
        if (user_subs.containsKey(handle)) {
            System.err.println("create user error");
            throw new AlreadyExistsException();
        } else {
            user_subs.put(handle, new ArrayList<String>());
            user_tweets.put(handle, new ArrayList<Long>());
            user_star_tweets.put(handle, new ArrayList<Long>());
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
                    System.out.println(handle + " subscribed " + theirhandle);
                }
            } else {
                System.err.println("error - trying to subscribe a non-existent user: " + theirhandle);
                throw new NoSuchUserException();
            }
        } else {
            System.err.println(handle + " does not exist");
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
                    // if trying to unsubscribe from a valid user, not subscribed
                    // operate like a nop
                    System.out.println("nop - " + handle + " unsubscribe " + theirhandle);
                } else {
                    user_subs.get(handle).remove(theirhandle);
                    System.out.println(handle + " unsubscribed " + theirhandle);
                }
            } else {
                System.err.println("unsubsribe err - cannot unsubscribe a non-exist user");
                throw new NoSuchUserException();
            }
        } else {
            System.err.println(handle + " does not exist");
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
        // check if valid user
        if (user_subs.containsKey(handle)) {
            // check tweet length
            if (tweetString.length() > 140) {
                System.err.println("tweet exceed maximum length");
                throw new TweetTooLongException();
            }
            // create a new tweet
            id++;
            Tweet t = new Tweet(id, handle, getTimeInSeconds(), 0, tweetString);
            tweets.put(id, t);
            user_tweets.get(handle).add(id);
            System.out.println(handle + " posted a tweet, id: " + id);
        } else {
            System.err.println(handle + " does not exist");
            throw new NoSuchUserException();
        }
    }

    @Override
    public List<Tweet> readTweetsByUser(String handle, int howmany)
        throws NoSuchUserException
    {
        // check if valid user
        if (user_subs.containsKey(handle)) {
            List<Long> thisUsersTweetIds = user_tweets.get(handle);
            List<Tweet> thisUsersTweets = new ArrayList<Tweet>();
            for (Long tweetId : thisUsersTweetIds) {
                thisUsersTweets.add(tweets.get(tweetId));
            }

            int tweetsToRead = (thisUsersTweets.size() < howmany) ? thisUsersTweets.size() : howmany;

            int addIndex = thisUsersTweets.size() - 1;
            List<Tweet> returnedTweets = new ArrayList<Tweet>();
            while (tweetsToRead > returnedTweets.size()) {
                returnedTweets.add(thisUsersTweets.get(addIndex));
                --addIndex;
            }

            // TODO: sort tweets by posted time in case they're somehow out of order

            return returnedTweets;
        } else {
            System.err.println(handle + " does not exist");
            throw new NoSuchUserException();
        }
    }

    @Override
    public List<Tweet> readTweetsBySubscription(String handle, int howmany)
        throws NoSuchUserException
    {
        // check if valid user
        if (user_subs.containsKey(handle)) {
            // ineffecient but works
            List<String> subs = user_subs.get(handle);
            List<Tweet> allTweets = new ArrayList<Tweet>();
            for (String sub : subs) {
                allTweets.addAll(readTweetsByUser(sub, howmany));
            }

            Collections.sort(allTweets, TweetComparator);

            //only take the top 'howmany' number of tweets
            int tweetsToRead = (allTweets.size() < howmany) ? allTweets.size() : howmany;
            int addIndex = allTweets.size() - 1;
            List<Tweet> returnedTweets = new ArrayList<Tweet>();
            while (tweetsToRead > returnedTweets.size()) {
                returnedTweets.add(allTweets.get(addIndex));
                --addIndex;
            }

            return returnedTweets;
        } else {
            System.err.println(handle + " does not exist");
            throw new NoSuchUserException();
        }
    }
    @Override
    public void star(String handle, long tweetId) throws
        NoSuchUserException, NoSuchTweetException
    {
        // check if valid user
        if (user_subs.containsKey(handle)) {
            // check if valid tweet
            if (tweets.containsKey(tweetId)) {
                // check if user already stared this tweet
                if (user_star_tweets.get(handle).contains(tweetId)) {
                    // star tweet more than once, simply return
                    System.out.println(handle + " already stared tweet: " + tweetId);
                    return;
                } else {
                    Tweet t = tweets.get(tweetId);
                    int old_star = t.getNumStars();
                    tweets.get(tweetId).setNumStars(old_star + 1);
                    user_star_tweets.get(handle).add(tweetId);

                    System.out.println(handle + " stared tweet " + "id : " + tweetId);
                    System.out.println("\ttweet id: " + tweetId + " numOfStar: "
                            + tweets.get(tweetId).getNumStars());
                }

            } else {
                System.err.println("tweet: " +tweetId + " does not exist");
                throw new NoSuchTweetException();
            }
        } else {
            System.err.println(handle + " does not exist");
            throw new NoSuchUserException();
        }

    }
}
