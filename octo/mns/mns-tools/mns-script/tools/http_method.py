#!/usr/bin/env python
# -*- coding:utf-8 -*-
# File: http_delete.py

import urllib2
import json

def http_delete(url, values):
  jdata = json.dumps(values)
  request = urllib2.Request(url, jdata)
  request.add_header('Content-Type', 'your/conntenttype')
  request.get_method = lambda:'DELETE'        # 设置HTTP的访问方式
  request = urllib2.urlopen(request)
  return request.read()

if __name__ == '__main__':
  url = "http://mns.test.sankuai.info/api/providers"
  values = "[{\"appkey\":\"com.sankuai.octo.tmy\", \"protocol\":\"thrift\", \"ip\":\"10.4.243.121\", \"port\":\"9035\", \"envir\":3}]"
  values = " [{\"appkey\":\"com.sankuai.octo.tmy\",\"version\":\"mnsc\",\"ip\":\"10.4.243.121\",\"port\":9035,\"weight\":10,\"status\":2,\"role\":0,\"envir\":3,\"lastUpdateTime\":1235235135,\"extend\":\"\",\"fweight\":10.0,\"serverType\":0,\"protocol\":\"thrift\"}]"

  resp = http_delete(url, values)
  print resp
