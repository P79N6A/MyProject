import sys
import json
from kazoo.client import KazooClient

from zk_conn import ZkConn


class MnsComm:
    userName = None
    mns_path = None
    prod_path = None

    def __init__(self):
        self.mns_path = "/mns/sankuai"
        self.prod_path = "/prod"

    def getAppkeys(self, zk):
        path = "%s%s" % (self.mns_path, self.prod_path)
        children = zk.get_children(path)
        return children
