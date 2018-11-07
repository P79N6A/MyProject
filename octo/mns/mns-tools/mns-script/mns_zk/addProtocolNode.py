# coding=utf-8
import sys
import json
import time
from kazoo.client import KazooClient

from zk_conn import ZkConn
from mns_zk.comm import MnsComm


class Protocol:
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
        self.zk.execute(lambda zk: self.buildProtocolNode(zk))

    def buildProtocolNode(self, zk):
        assert isinstance(zk, KazooClient)
        print("start to get Appkeys")
        appkeys = ["com.sankuai.cellar.config.test","com.sankuai.cellar.configchenxintest"]
        protocol = "cellar"
        parent = ["/routes", "/providers"]
        for parent_node in parent:
            for (env, epath) in self.env_path.items():
                envNum = self.__getEnvNum(env)
                for appkey in appkeys:
                    route_path = "%s%s%s%s%s" %(self.mns_path, epath, "/", appkey, parent_node)
                    print route_path
                    zk.ensure_path(route_path)
                    #zk.set(route_path, json.dumps(""))
                    node_json = {"appkey":appkey, "lastUpdateTime":int(time.time())}
                    node_path = "%s%s%s" % (route_path, "/", protocol)
                    if not zk.exists(node_path):
                        time.sleep(0.12)
                        print "create node:" + node_path
                        zk.create(node_path, json.dumps(node_json))

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
    Protocol(zk_conn)
