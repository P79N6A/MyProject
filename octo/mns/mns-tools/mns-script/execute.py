#!/usr/bin/python
# coding=utf-8
import sys

if __name__ == '__main__':
    args = sys.argv
    del args[0]
    sys.argv = args
    execfile(args[0])