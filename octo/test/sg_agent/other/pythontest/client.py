#!/usr/bin/python

import sys
sys.path.append('./gen-py')
from sgagent import SGAgent
from sgagent.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

# Make socket
transport = TSocket.TSocket('192.168.3.163', 5266)
# Buffering is critical. Raw sockets are very slow
transport = TTransport.TFramedTransport(transport)
# Wrap in a protocol
protocol = TBinaryProtocol.TBinaryProtocol(transport)
# Create a client to use the protocol encoder
client = SGAgent.Client(protocol)
# Connect!
transport.open()
# Call Server services  
for i in range(0, 1000):
    sList = client.getServiceList("com.sankuai.inf.test", "com.sankuai.inf.sg_agent")
    #print sList

transport.close()


