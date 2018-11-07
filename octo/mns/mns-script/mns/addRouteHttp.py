# coding=utf-8
import sys
import json
from kazoo.client import KazooClient

from zk_conn import ZkConn


class RouteHttp:
    userName = None
    zk = None
    mns_path = None
    prod_path = None
    desc_path = None

    def __init__(self, zk_conn, user):
        self.zk = ZkConn(zk_conn)
        self.mns_path = "/mns/sankuai"
        self.prod_path = "/prod"
        self.desc_path = "/desc"
        self.zk.execute(lambda zk: self.__execute(zk, user))

    def __execute(self, zk, user):
        assert isinstance(zk, KazooClient)
        self.__getAppkeys(zk)

    def __getAppkeys(self, zk):
        path = "%s%s" % (self.mns_path, self.prod_path)
        childes = zk.get_children(path)
        if len(childes) > 0:
            for child in childes:
                desc = self.__getDesc(zk, child)
                dictdesc = json.loads(desc)
                owners = dictdesc['owners']
                for owner in owners:
                    login = owner['login']
                    #print login
                    if login == user:
                        print child
                        break

    def __getDesc(self, zk, appkey):
        path = "%s%s%s%s%s" % (self.mns_path, self.prod_path,\
                "/", appkey, self.desc_path)
        data, stat = zk.get(path)
        return data



if __name__ == '__main__':
    if 3 != len(sys.argv):
        print("参数说明:")
        print("\t1:zk连接地址(包括端口,可以多个,用','做分割)")
        print("\t2:要查询的用户名，比如yangjie17")
        sys.exit()

    zk_conn = sys.argv[1]
    user = sys.argv[2]
    User(zk_conn, user)
