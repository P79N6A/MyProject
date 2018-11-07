#!/usr/bin/python

import sys
import re
import socket
import os
import urllib
import urllib2
import time
import json
from warnings import filterwarnings
from socket import gethostname

os.chdir(os.path.abspath(os.path.dirname(sys.argv[0])))
sys.path.append('./gen-py')
from sgagent import SGAgent
from sgagent.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

host = gethostname().split('.')[0]
payload = []
ts = int(time.time())

def add_falcon_data(key, value):
	global host
	global payload
	global ts
	data = {
		"endpoint": host,
		"metric": key,
		"timestamp": ts,
		"step": 60,
		"value": value,
		"counterType": "GAUGE",
		"tags": "location=beijing,service=sg_agent",
	}
	payload.append(data)

# Make socket
transport = TSocket.TSocket(host, 5266)
# Buffering is critical. Raw sockets are very slow
transport = TTransport.TFramedTransport(transport)
# Wrap in a protocol
protocol = TBinaryProtocol.TBinaryProtocol(transport)
# Create a client to use the protocol encoder
client = SGAgent.Client(protocol)

try:
	# Connect!
	transport.open()
except:
	print 2
	exit(1)

# Call Server services  
zabbix = client.getZabbixInfo()
transport.close()

nameDict = {
	10101 : 'sg_agent.mq.service_key',
	10102 : 'sg_agent.mq.route_key',
	10103 : 'sg_agent.mq.config_key',
	10104 : 'sg_agent.mq.register_key',
	10105 : 'sg_agent.mq.send_log_key',
	10106 : 'sg_agent.mq.send_module_key',
	10107 : 'sg_agent.mq.send_common_log_key'
}

msgDict = zabbix.msgQueueBytes

add_falcon_data('sg_agent.vmRss', zabbix.agent_vmRss * 1024)
add_falcon_data('sg_agent_worker.vmRss', zabbix.worker_vmRss * 1024)
add_falcon_data('sg_agent.cpu', zabbix.agent_cpu)
add_falcon_data('sg_agent_worker.cpu', zabbix.worker_cpu)
add_falcon_data('sg_agent.bufferKeyNum', zabbix.bufferKeyNum)
add_falcon_data('sg_agent.missBuffNum', zabbix.missBuffNum)
add_falcon_data('sg_agent_worker.zkConnections', zabbix.zkConnections)
add_falcon_data('sg_agent_worker.mtConfigConnections', zabbix.mtConfigConnections)
add_falcon_data('sg_agent_worker.logCollectorConnections', zabbix.logCollectorConnections)
for key in msgDict:
	add_falcon_data(nameDict[key], msgDict[key])

print json.dumps(payload)
