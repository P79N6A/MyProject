# coding=utf-8
import sys
import json
import time
import os,sys,re
import subprocess
from kazoo.client import KazooClient

from zk_conn import ZkConn
from mns_zk.comm import MnsComm


class SGAgent:
    userName = None
    zk = None
    mns_path = None
    env_path = None
    mnsComm = None

    def __init__(self, zk_conn):
        self.zk = ZkConn(zk_conn)
        self.mns_path = "/mns/sankuai"
        self.env_path = {"prod":"/prod", "stage":"/stage", "test":"/test"}
        self.mnsComm = MnsComm()
        print("start to execute")
        self.zk.execute(lambda zk: self.getUnPingSGAgent(zk))

    def getUnPingSGAgent(self, zk):
        assert isinstance(zk, KazooClient)
        print("start to get Appkeys")
        for env, epath in self.env_path.items():
            envNum = self.__getEnvNum(env)
            appkey = "com.sankuai.inf.sg_agent"
            provider = "provider"
            agentPath = "%s%s%s%s%s%s" %(self.mns_path, epath, "/", appkey, "/", provider)
            print agentPath
            agentList = zk.get_children(agentPath)
            for ele in agentList:
              ip = ele.split(":")
	      self.check_ip_ping(ip[0])

    #检查ip能否ping通
    #0:正常，1：ping不通
    def check_ip_ping(self, ip):
        p = subprocess.Popen([r'./mns_zk/ping2.sh',ip], stdout=subprocess.PIPE)
        result = p.stdout.read()
        Status = 0
        if result =='1\n':
            Status = 1
            print ip,'----ping failed----'
        #else:
            #ping_ok.append(record[i])
            #print ip,'----ping success----'

    def __getEnvNum(selft, env):
        if env == "test":
            return 1
        elif env == "stage":
            return 2
        else:
            return 3


if __name__ == '__main__':
    if 2 != len(sys.argv):
        print("参数说明:")
        print("\t1:zk连接地址(包括端口,可以多个,用','做分割)")
        sys.exit()

    zk_conn = sys.argv[1]
    SGAgent(zk_conn)
