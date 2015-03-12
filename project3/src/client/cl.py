#!/usr/bin/env python

import sys

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

  client.star("@user2", 1)
  print '@user2 star tweet 1'

  client.star("@user1", 1)
  print '@user1 star tweet 1'

  client.star("@user2", 1)
  print '@user2 cannot star tweet 1 again'

  # Close!
  transport.close()

except Thrift.TException, tx:
  print '%s' % (tx.message)
