
namespace java edu.ucsd.cse124
namespace py cse124

struct Tweet {
    1:i64 tweetId,
    2:string handle,
    3:i64 posted,
    4:i32 numStars,
    5:string tweetString
}

exception AlreadyExistsException {
    1:string user; // the handle that already exists
}

exception NoSuchUserException {
    1:string user;  // the user that doesn't exist
}

exception TweetTooLongException {
    // can't be longer than 140 characters
}

exception NoSuchTweetException {
}

service Twitter {
    void ping(),

    void createUser(1:string handle) throws
        (1:AlreadyExistsException existsx),

    void subscribe(1:string handle, 2:string theirhandle) throws
        (1:NoSuchUserException userx),

    void unsubscribe(1:string handle, 2:string theirhandle) throws
        (1:NoSuchUserException userx),

    void post(1:string handle, 2:string tweetString) throws
        (1:NoSuchUserException userx, 2:TweetTooLongException longx),

    list<Tweet> readTweetsByUser(1:string handle, 2:i32 howmany)
        throws (1:NoSuchUserException userx),

    list<Tweet> readTweetsBySubscription(1:string handle, 2:i32 howmany)
        throws (1:NoSuchUserException userx),

    void star(1:string handle, 2:i64 tweetId)
        throws (1:NoSuchUserException userx, 2:NoSuchTweetException tweetx)  
}
