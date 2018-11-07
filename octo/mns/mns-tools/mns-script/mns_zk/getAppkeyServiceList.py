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

from optparse import OptionParser

usage = "usage: %prog [options]"
parser = OptionParser(usage=usage)
parser.add_option("-s", "--servers", dest="servers",
                  default="10.4.254.98:2181", help="host:port (default %default)")
parser.add_option("-e", "--environment", dest="environment",
                  default="prod", help="mns environment")
parser.add_option("", "--appkey", dest="appkey",
                  default="com.sankuai.inf.logCollector", help="appkey in octo")
parser.add_option("-o", "--outputfile",
                  dest="outputfile", default="ip_list.txt",
                  help="outputfile to get Ip List")

(options, args) = parser.parse_args()
print options

envPath = "/mns/sankuai/" + options.environment + "/" + options.appkey + "/provider"
print envPath

zk = KazooClient( hosts= options.servers)
zk.start()
if zk.exists(envPath) :
    service_list = zk.get_children(envPath)
filename = "result-files/" + options.appkey + "-" + options.outputfile
file_object = open(filename, 'w')
for service in service_list :
    ip_port = service.split(':')
    file_object.write(ip_port[0] + '\n')
file_object.close()
zk.stop()
