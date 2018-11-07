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
domain = "data.octo.vip.sankuai.com"

zk = KazooClient( hosts='10.4.254.98:2181')
zk.start()

appkey_list = []
envPath = "/mns/sankuai/prod"
if zk.exists(envPath) :
    appkey_list = zk.get_children(envPath)

interface_map = {}
a = "2015-12-09 00:00:00"
i_time = int(time.mktime( time.strptime(a, "%Y-%m-%d %H:%M:%S")))
end_time = i_time + 24*60*60
for appkey in appkey_list :
    interface_map[appkey] = []
    for env in Env:
        url = "http://%s/api/tags?appkey=%s&start=%d&end=%d&env=%s&source=server" % (domain, appkey, i_time, end_time, env)
        req = urllib2.Request( url)
        try :
            handle = urllib2.urlopen(req)
        except IOError, e:
            print "[ERROR] for "+appkey + str(e)
            continue

        print "=========== %s %s =============="%( appkey, env)
        strRet = handle.read()
        date = json.loads(strRet)
        interface_map[appkey] += date["spannames"]

print str(interface_map)
file_object = open('interfaceFile','w')
for (k,v) in interface_map.items() :
    file_object.write( k)
    file_object.write( '\t')
    file_object.write( str(v) + '\n')

zk.stop()
