#-*- coding: utf-8 -*-
 
import platform
import sys
import os
import time
import thread

def GetOS():
  os = platform.system()
  if os == "Windows":
    return "n"
  else:
    return "c"

def PingIP(ip_str, ip_list):
  cmd = ["ping", "-{op}".format(op=GetOS()), "1", ip_str]
  output = os.popen(" ".join(cmd)).readlines()
  print " ".join(cmd) 

  flag = False 
  for line in list(output):
    if not line:
      continue
    if str(line).upper().find("TTL") >= 0:
      flag = True
      break

  if flag:
    print "ip: %s is ok ***"%ip_str
    ip_list.append(ip_str)
    

def FindIP(ip_prefix):
  ip_list = []
  for i in range(1, 256):
    ip = '%s.%s'%(ip_prefix, i)
    thread.start_new_thread(PingIP, (ip, ip_list))
    time.sleep(0.3)
  return ip_list

if __name__ == "__main__":
  print "start time %s"%time.ctime()
  commandargs = sys.argv[1:]
  args = "".join(commandargs)

  ip_prefix = '.'.join(args.split('.')[:-1])
  ip_list = FindIP(ip_prefix)
  file = open("iplist.txt", "w+")
  try:
    for line in ip_list:
      file.write(line)
      file.write("\n")
  finally:
    file.close()

  print "end time %s"%time.ctime()
