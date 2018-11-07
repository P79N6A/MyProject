#!/usr/bin/python
""" Check Zookeeper Cluster

Generic monitoring script that could be used with multiple platforms (Ganglia, Nagios, Cacti).

It requires ZooKeeper 3.4.0 or greater. The script needs the 'mntr' 4letter word 
command (patch ZOOKEEPER-744) that was now commited to the trunk.
The script also works with ZooKeeper 3.3.x but in a limited way.
"""

import sys
import socket
import re
from StringIO import StringIO
import json
import time


class ZooKeeperServer(object):
    def __init__(self, host='localhost', port='2181', timeout=3):
        self._address = (host, int(port))
        self._timeout = timeout

    def get_stats(self):
        """ Get ZooKeeper server stats as a map """
        data = self._send_cmd('mntr')
        if data:
            return self._parse(data)
        else:
            data = self._send_cmd('stat')
            return self._parse_stat(data)

    def _create_socket(self):
        return socket.socket()

    def _send_cmd(self, cmd):
        """ Send a 4letter word command to the server """
        s = self._create_socket()
        s.settimeout(self._timeout)

        s.connect(self._address)
        s.send(cmd)

        data = s.recv(2048)
        s.close()

        return data

    def _parse(self, data):
        """ Parse the output from the 'mntr' 4letter word command """
        h = StringIO(data)
        
        result = {}
        for line in h.readlines():
            try:
                key, value = self._parse_line(line)
		if key == 'zk_server_state':
                    if value == "observer":
                        result['zk_server_state'] = "0"
                    elif value == "follower":
                        result['zk_server_state'] = "1"
                    elif value == "leader":
                        result['zk_server_state'] = "2"
                    else:
                        result['zk_server_state'] = "3"
                elif key != 'zk_version':
                    result[key] = value
            except ValueError:
                pass # ignore broken lines

        return result

    def _parse_stat(self, data):
        """ Parse the output from the 'stat' 4letter word command """
        h = StringIO(data)

        result = {}
        
        version = h.readline()
        if version:
            result['zk_version'] = version[version.index(':')+1:].strip()

        # skip all lines until we find the empty one
        while h.readline().strip(): pass

        for line in h.readlines():
            m = re.match('Latency min/avg/max: (\d+)/(\d+)/(\d+)', line)
            if m is not None:
                result['zk_min_latency'] = int(m.group(1))
                result['zk_avg_latency'] = int(m.group(2))
                result['zk_max_latency'] = int(m.group(3))
                continue

            m = re.match('Received: (\d+)', line)
            if m is not None:
                result['zk_packets_received'] = int(m.group(1))
                continue

            m = re.match('Sent: (\d+)', line)
            if m is not None:
                result['zk_packets_sent'] = int(m.group(1))
                continue

            m = re.match('Outstanding: (\d+)', line)
            if m is not None:
                result['zk_outstanding_requests'] = int(m.group(1))
                continue

            m = re.match('Mode: (.*)', line)
            # zk_server_stat: observer->0, follwer->1, leader->2
            if m is not None:
                if m.group(1).equals("observer"):
                    result['zk_server_state'] = "0"
                elif m.group(1).equals("follower"):
                    result['zk_server_state'] = "1"
                elif m.group(1).equals("leader"):
                    result['zk_server_state'] = "2"
                else:
                    result['zk_server_state'] = "3"
                continue

            m = re.match('Node count: (\d+)', line)
            if m is not None:
                result['zk_znode_count'] = int(m.group(1))
                continue

        return result 

    def _parse_line(self, line):
        try:
            key, value = map(str.strip, line.split('\t'))
        except ValueError:
            raise ValueError('Found invalid line: %s' % line)

        if not key:
            raise ValueError('The key is mandatory and should not be empty')

        try:
            value = int(value)
        except (TypeError, ValueError):
            pass

        return key, value

def post_data(url, data):
    import urllib2, urllib
    ret = urllib.urlopen(url, data)
    return ret.read()

def main():
    host = socket.gethostname()
    ports = ["2181", "2182", "2183", "2191", "9331"]
    stats = {}
    dataAll = []
    timestamp = int(time.time())

    # get short hostname
    if re.search('.', host):
        host = host.split('.')[0]

    for port in ports:
        try:
            zk = ZooKeeperServer(host, port)
            stats = zk.get_stats()
            for k in stats:
                dataOne = {
                    "endpoint": host,
                    "metric": 'zookeeper_' + str(port) + '.' + str(k),
                    "timestamp": timestamp,
                    "step": 60,
                    "value": stats[k],
                    "counterType": "GAUGE",
                    "tags": "service=zookeeper",
                }
                dataAll.append(dataOne) 
        except Exception:
            pass

    if dataAll:
        r = post_data("http://127.0.0.1:1988/v1/push", data=json.dumps(dataAll) )
    # print(r)
    print(json.dumps(dataAll))

if __name__ == '__main__':
    main()

