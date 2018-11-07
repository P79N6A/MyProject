# coding=utf-8
import sys
from kazoo.client import KazooClient
import time


class DeleteData:
    zk_addr = None
    delete_dir = None
    zk = None
    delete_count = None

    def __init__(self, args):
        if 3 != len(args):
            print("msg:")
            print("\t1:zk address")
            print("\t2:delete node")
            sys.exit()
        self.zk_addr = args[1]
        self.delete_dir = args[2]
        self.delete_count = 0
        self.zk = KazooClient(hosts=self.zk_addr)
        self.zk.start()
        self.delete_each_node(self.delete_dir)
        self.zk.stop()
        self.zk.close()

    def delete_each_node(self, root_path):
        print("delete node %s" % root_path)
        childs = self.zk.get_children(root_path)
        print("child len %s" % len(childs))
        if len(childs) > 0:
            for child in childs:
                if child is not None:
                    path_child = root_path + "/" + child
                    print("to delete child %s" % path_child)
                    self.delete_each_node(path_child)
        print("delete:" + root_path)
        self.zk.delete(root_path)
        self.delete_count += 1
        if self.delete_count > 50:
            self.delete_count = 0
            time.sleep(0.1)


if __name__ == '__main__':
    export = DeleteData(sys.argv)
    print("delete success")