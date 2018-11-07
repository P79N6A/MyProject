#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sys
import time
import urllib2
import re
import base64
import json
from urlparse import urlparse
from kazoo.client import KazooClient
from kazoo.client import KazooState
from kazoo.client import KeeperState

aaa = ["a"]
#Env = ["prod", "stage", "test"]
Env = ["prod"]

#10.64.30.199:2181,10.64.23.173:2181,10.64.29.222:2181
#zk = KazooClient( hosts='10.4.245.246:2181')
zk = KazooClient( hosts='10.32.144.204:2181')
zk.start()

appkey = sys.argv[1]
print "====================================  " + appkey + "\n"
for a in aaa:
    for env in Env:
        provider_path = "/mns/sankuai/"+env +"/"+ appkey +"/provider"
        ipList = zk.get_children( provider_path)
        for ip in ipList:
            serviceNode, stat2 = zk.get( provider_path+"/"+ip)
            serviceJson = json.loads(serviceNode)
            for item in serviceJson:
                if (item == "status") :
                    if serviceJson[item] == 0 :
                        print appkey + "        " + ip + "    " + str( serviceJson[item])
                        serviceJson[item] = 2
                        json_str = json.dumps(serviceJson)
                        print json_str
                        zk.set( provider_path+"/"+ip, json_str)

zk.stop()
