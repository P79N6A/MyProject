# coding=utf-8
from kazoo.client import KazooClient


class ZkConn(KazooClient):
    zk = None

    def __init__(self, zk_conn):
        KazooClient.__init__(self, hosts=zk_conn)
        self.zk = self
        print("zk地址:{zk_path}".format(zk_path=zk_conn))

    def execute(self, invoker):
        self.zk.start()
        result = invoker(self.zk)
        self.zk.stop()
        self.zk.close()
        return result

    def create_if_not_exist(self, path, data=b"", makepath=False):
        if not self.zk.exists(path):
            self.zk.create(path, data, makepath = makepath)
        else:
            print("[%s]节点已经存在" % path)