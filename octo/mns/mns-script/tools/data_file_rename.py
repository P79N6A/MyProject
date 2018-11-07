# coding=utf-8
import os
import sys
from kazoo.client import KazooClient


class DataFileRename:
    zk_addr = None
    import_dir = None
    zk = None

    def __init__(self, args):
        if 3 != len(args):
            print("参数说明:")
            print("\t1:数据文件dir")
            print("\t2:要替换的前缀")
            print("\t2:要替换的后缀")
            sys.exit()
        data_dir = args[0]
        ori_prefix = args[1]
        target_prefix = args[2]
        for file_name in os.listdir(data_dir):
            if file_name.startswith(ori_prefix):
                os.rename("%s/%s" % (data_dir, file_name), "%s/%s" % (data_dir, file_name.replace(ori_prefix, target_prefix, 1)))

if __name__ == '__main__':
    DataFileRename(sys.argv[1:])