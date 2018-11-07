# coding=utf-8
import sys
import json
import time
import os,sys,re
import socket
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
        #self.env_path = {"stage":"/stage"}
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
              ipport = ele.split(":")
	      self.check_telnet(ipport[0], int(ipport[1]))

    #检查ip能否ping通
    #0:正常，1：ping不通
    def check_telnet(self, ip, port):
      s=socket.socket()
      try:
        s.connect((ip, port))
        #print ip,'----connect success----'
      except socket.error,e:
        print ip,'----connect failed----'

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
