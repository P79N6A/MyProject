# coding=utf-8
import sys
import json
import time
from kazoo.client import KazooClient

from zk_conn import ZkConn
from mns_zk.comm import MnsComm


class Route:
    userName = None
    zk = None
    mns_path = None
    env_path = None
    mnsComm = None

    def __init__(self, zk_conn):
        self.zk = ZkConn(zk_conn)
        self.mns_path = "/mns/sankuai"
        #self.env_path = {"prod":"/prod", "stage":"/stage", "test":"/test"}
        self.env_path = {"prod":"/prod"}
        self.mnsComm = MnsComm()
        print("start to execute")
        self.zk.execute(lambda zk: self.buildRouteHttp(zk))

    def buildRouteHttp(self, zk):
        route_http_json = """{\"id\" : \"default\",\"name\" : \"auto\",\"category\" : 1,\"appkey\" : \"com.sankuai.inf.chenxin11\",\"env\" : 2,\"priority\" : 0,\"status\" : 0,\"consumer\" : {\"ips\" : [],\"appkeys\" : []},\"provider\" : [],\"createTime\" : 1463150757175,\"updateTime\" : 1463154301619,\"reserved\" : \"route_limit:1\"}"""
        route_http_dict = {"id" : "default","name" : "auto","category" : 1, "priority" : 0,"status" : 0,"consumer" : {"ips" : [],"appkeys" : []},"provider" : [],"createTime" : 1463150757175,"updateTime" : 1463154301619,"reserved" : ""}
        assert isinstance(zk, KazooClient)
        print("start to get Appkeys")
        appkeys = self.mnsComm.getAppkeys(zk)
        route_node = "/route-http"
        for (env, epath) in self.env_path.items():
            envNum = self.__getEnvNum(env)
            for appkey in appkeys:
                #if appkey != "com.sankuai.rc.tatooine.event" and appkey != "com.sankuai.rc.tatooine.event.groupa" and appkey != "com.sankuai.rc.tatooine.event.groupb":
		#    continue
                route_path = "%s%s%s%s%s" %(self.mns_path, epath, "/", appkey, route_node)
                print route_path
                zk.ensure_path(route_path)
                route_json = {"appkey":appkey, "lastUpdateTime":int(time.time())}
                zk.set(route_path, json.dumps(route_json))
                node_path = "%s%s" % (route_path, "/default")
                route_http_dict["appkey"] = appkey
                route_http_dict["createTime"] = int(time.time())
                route_http_dict["updateTime"] = int(time.time())
                route_http_dict["env"] = envNum
                if not zk.exists(node_path):
                    time.sleep(0.12)
                    print "create node:" + node_path
                    zk.create(node_path, json.dumps(route_http_dict))
                #else :
                #    print "set node:" + node_path
                #    zk.set(node_path, json.dumps(route_http_dict))

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
    Route(zk_conn)
