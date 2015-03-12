#!/usr/bin/env python

import sys
import time

sys.path.append('gen-py')

from cse124 import Twitter
from cse124.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

try:
  transport = TSocket.TSocket('localhost', 9090)

  # Buffering is critical. Raw sockets are very slow
  transport = TTransport.TBufferedTransport(transport)

  # Wrap in a protocol
  protocol = TBinaryProtocol.TBinaryProtocol(transport)

  # Create a client to use the protocol encoder
  client = Twitter.Client(protocol)

  # Connect!
  transport.open()

  client.ping()
  print 'ping()'

  client.createUser("@user1")
  print '@user1 created'

  client.createUser("@user2")
  print '@user2 created'

  client.createUser("@user3")
  print '@user3 created'

  client.createUser("@david")
  print '@david created'

  client.subscribe("@david", "@user1")
  print 'subscribe user1'

  #client.subscribe("@david", "@user1")
  #print 'already subscribed user1'

  #client.subscribe("@david", "@user2")
  #print 'cannot subscribe user2, user2 does not exist'

  client.unsubscribe("@david", "@user1")
  print 'unsubscribe user1'

  client.unsubscribe("@david", "@user2")
  print 'nop - unsubscribe user2'

  client.subscribe("@user1", "@user1")
  print '@user1 subscribe himself'

  client.post("@user1", "first tweet")
  print '@user1 posted first tweet'
  time.sleep(2)

  client.post("@user3", "second tweet")
  print '@user3 posted second tweet'
  time.sleep(2)

  client.post("@user1", "third tweet")
  print '@user1 posted third tweet'
  time.sleep(2)

  client.post("@user3", "fourth tweet")
  print '@user3 posted fourth tweet'
  time.sleep(2)

  client.star("@user2", 1)
  print '@user2 star tweet 1'

  client.star("@user1", 1)
  print '@user1 star tweet 1'

  client.star("@user2", 1)
  print '@user2 cannot star tweet 1 again'

  tweets = client.readTweetsByUser("@user1", -1)
  print "getting -1 of @user1's tweets; should be empty list"
  print tweets

  tweets = client.readTweetsByUser("@user1", 10)
  print "@user1's tweets:"
  print tweets

  client.subscribe("@user2", "@user1")
  print '@user2 subscribed to @user1'

  client.subscribe('@user2', '@user3')
  print '@user2 subscribed to @user3'

  tweets = client.readTweetsBySubscription('@user2', 1)
  print "@user2's subscribed tweets (1)"
  print tweets

  tweets = client.readTweetsBySubscription('@user2', 2)
  print "@user2's subscribed tweets (2)"
  print tweets

  tweets = client.readTweetsBySubscription('@user2', 3)
  print "@user2's subscribed tweets (3)"
  print tweets

  tweets = client.readTweetsBySubscription('@user2', 4)
  print "@user2's subscribed tweets (4)"
  print tweets

  tweets = client.readTweetsBySubscription('@user2', 5)
  print "@user2's subscribed tweets (5)"
  print tweets

  tweets = client.readTweetsBySubscription('@user2', -1)
  print "@user2's subscribed tweets (-1)"
  print tweets

  # Close!
  transport.close()

except Thrift.TException, tx:
  print '%s' % (tx.message)
