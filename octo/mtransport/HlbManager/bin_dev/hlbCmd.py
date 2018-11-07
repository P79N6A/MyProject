#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import time
import commands
import traceback
import string
import paramiko

def ssh2(ip,username,passwd,cmd):
  try :
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(ip,22,username,passwd,timeout=5)
    stdin,stdout,stderr = ssh.exec_command(cmd)
    print stdout.read()
    print '%s %s\tOK\n'%(ip,cmd)
    ssh.close()
  except :
    print '%s \tError\n'%(ip)

ipList = ["10.4.242.103", "10.4.242.104", "10.4.242.105", "10.4.246.31"]

try :
  s1, o1 = commands.getstatusoutput('nginx -V')
  print "NGINX -V ---- " + str(s1) +"\n"+ str(o1)
  if (string.find("[emerg]", str(o1)) != -1) :
    print "\n======================= NGINX CONFIG INVALID =======================\n"
    sys.exit(1)

  s2, o2 = commands.getstatusoutput('nginx -s reload')
  print "nginx -s reload ---- " + str(s2) +"\n"+ str(o2)
  for ip in ipList:
    ssh2( ip, 'root', 'nginx123', 'nginx -s reload')

except :
  print traceback.print_exc()
