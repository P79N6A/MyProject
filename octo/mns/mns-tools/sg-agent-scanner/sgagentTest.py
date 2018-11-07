#!/usr/bin/env python
# -*- coding: utf-8 -*- 
 
import sys
sys.path.append('./thrift/gen-py')
import time
from sgagent_service import *
 
from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

def getservicelist(ip):
    try:
        transport = TSocket.TSocket(ip, 5266)
        transport = TTransport.TFramedTransport(transport)
        protocol = TBinaryProtocol.TBinaryProtocol(transport)
        client = SGAgent.Client(protocol)
        transport.open()
        servicelist = client.getServiceList("localAppkey", "com.sankuai.inf.logCollector");
        ret = len(servicelist)
        transport.close()

        if ret == 23:
            return 0
        else:
	    print "ret = %d, ip = %s" %(ret, ip)
            return 1
       
    except Thrift.TException, ex:
        print "%s" % (ex.message)

# 打开文件
fp = open("dxad", "rw+")
print "文件名为: ", fp.name
count = 0
wrong = 0
for eachline in fp:
        try:
            count = count + int(getservicelist(eachline))
        except TypeError:
            wrong = wrong + 1
# 关闭文件
fp.close()
print "wrong num is %d" %(wrong)
print "count num is %d" %(count)

