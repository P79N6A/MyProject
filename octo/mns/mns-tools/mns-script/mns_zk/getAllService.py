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
envPath = "/mns/sankuai/prod"
if zk.exists(envPath) :
    appkey_list = zk.get_children(envPath)

provider_http_map = {}
provider_thrift_map = {}
for appkey in appkey_list :
    provider_http_map[appkey] = {}
    provider_thrift_map[appkey] = {}
    for env in Env:
        provider_http_map[appkey][env] = []
        provider_thrift_map[appkey][env] = []
        provider_http_path = "/mns/sankuai/"+env +"/"+ appkey +"/provider-http"
        provider_thrift_path = "/mns/sankuai/"+env +"/"+ appkey +"/provider"
        http_service_list = zk.get_children(provider_http_path)
        provider_http_map[appkey][env] += http_service_list
        thrift_service_list = zk.get_children(provider_thrift_path)
        provider_thrift_map[appkey][env] += thrift_service_list

print str(provider_http_map)
print str(provider_thrift_map)
file_object = open('result-files/allService','w')

file_object.write( "[thrift服务列表]\n")
for (appkey, all_list) in provider_thrift_map.items() :
    file_object.write( appkey)
    file_object.write( '\n')
    for (env,ip_list) in all_list.items():
        file_object.write( env)
        file_object.write( " : ")
        for ip in ip_list:
            file_object.write( str(ip) + '\t')
        file_object.write( '\n')
    file_object.write( '\n')

file_object.write( "[HTTP服务列表]\n")
for (appkey, all_list) in provider_http_map.items() :
    file_object.write( appkey)
    file_object.write( '\n')
    for (env,ip_list) in all_list.items():
        file_object.write( env)
        file_object.write( " : ")
        for ip in ip_list:
            file_object.write( str(ip) + '\t')
    file_object.write( '\n')

zk.stop()
