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

Env = ["prod", "stage", "test"]
zk = KazooClient( hosts='10.4.254.98:2181')
zk.start()

appkey_list = []
env = Env[0]
envPath = "/mns/sankuai/prod"
if zk.exists(envPath) :
    appkey_list = zk.get_children(envPath)

provider_count_map = {}
for appkey in appkey_list :
    provider_count_map[appkey] = 0
    for env in Env:
        provider_path = "/mns/sankuai/"+env +"/"+ appkey +"/provider"
        data1, stat1 = zk.get(provider_path)
        print "============== %s %s   %d"%( appkey, env, stat1.numChildren)
        provider_count_map[appkey] += stat1.numChildren

print str(provider_count_map)
file_object = open('providerCountFile','w')
for (k,v) in provider_count_map.items() :
    file_object.write( k)
    file_object.write( '\t')
    file_object.write( str(v) + '\n')

zk.stop()
