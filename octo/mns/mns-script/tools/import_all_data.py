# coding=utf-8
import os
import sys
from kazoo.client import KazooClient


class ExportAllData:
    zk_addr = None
    import_dir = None
    zk = None

    def __init__(self, args):
        if 2 != len(args):
            print("参数说明:")
            print("\t1:要导入的zk的连接地址")
            print("\t2:导入的文件的存放目录")
            sys.exit()
        self.zk_addr = args[0]
        self.import_dir = os.path.abspath(os.path.expanduser(args[1]))
        self.zk = KazooClient(hosts=self.zk_addr)
        self.zk.start()
        self.import_data()
        self.zk.stop()
        self.zk.close()

    def import_data(self):
        assert os.path.isdir(self.import_dir), "[%s]不是目录" % self.import_dir
        data_files = sorted(os.listdir(self.import_dir), cmp=lambda x, y: sort_data(x, y))
        for data_file in data_files:
            node_path = "/%s" % "/".join(os.path.splitext(data_file)[0].split("|")[1:])
            if self.zk.exists(node_path):
                self.zk.set(node_path, open("%s/%s" % (self.import_dir, data_file)).read())
            else:
                self.zk.create(node_path, open("%s/%s" % (self.import_dir, data_file)).read(), makepath=True)

def sort_data(x, y):
    x_len = len(str(x).split("|"))
    y_len = len(str(y).split("|"))
    if x_len > y_len:
        return 1
    elif x_len < y_len:
        return -1
    else:
        return cmp(x, y)


if __name__ == '__main__':
    ExportAllData(sys.argv[1:])