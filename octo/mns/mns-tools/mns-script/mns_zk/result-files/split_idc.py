#!/usr/bin/env python
# -*- coding: utf-8 -*- 
dx = open("dx.txt", "rw+")
yf = open("yf.txt", "rw+")
other = open("other.txt", "rw+")

def split_idc(ip):
	if(ip[3] == '3'):	
		dx.write(ip)
	elif(ip[3] == '4'):
		yf.write(ip)
	else:
		other.write(ip)


# 打开文件
fp = open("allSgAgentList", "rw+")
for eachline in fp:
	split_idc(eachline)
# 关闭文件
fp.close()
dx.close()
yf.close()
other.close()

