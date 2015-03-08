package edu.ucsd.cse124;

import java.util.List;

public class TwitterHandler implements Twitter.Iface {

    @Override
    public void ping() {
    }

    @Override
    public void createUser(String handle) throws AlreadyExistsException
    {
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
