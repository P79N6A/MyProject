# coding=utf-8
import os
import sys
from kazoo.client import KazooClient


class ExportAllData:
    zk_addr = None
    export_dir = None
    zk = None

    def __init__(self, args):
        if 3 != len(args):
            print("参数说明:")
            print("\t1:要导出的zk的连接地址")
            print("\t2:导出的文件的存放地点")
            print("\t3:导出的zk基础节点")
            sys.exit()
        self.zk_addr = args[0]
        self.export_dir = os.path.abspath(os.path.expanduser(args[1]))
        self.zk = KazooClient(hosts=self.zk_addr)
        self.zk.start()
        basic_path = args[2]
        self.export_each_node(basic_path)
        self.export_data(basic_path)
        self.zk.stop()
        self.zk.close()
        print("导出成功!")

    def export_each_node(self, root_path):
        childs = self.zk.get_children(root_path)
        if len(childs) > 0:
            for child in childs:
                path_child = root_path + "/" + child
                self.export_data(path_child)
                self.export_each_node(path_child)

    def export_data(self, path):
        if len(path) > 0:
            data, stat = self.zk.get(path)
            export_file = open(self.export_dir + "/" + path.replace("/", "|") + ".txt", 'w')
            export_file.write(data)

if __name__ == '__main__':
    ExportAllData(sys.argv[1:])