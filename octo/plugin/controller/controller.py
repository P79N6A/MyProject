import os
import sys
import getopt 
import time
import multiprocessing
sys.path.append('gen-py')

from core import Core 
  
from thrift import Thrift 
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.Thrift import TType, TMessageType, TException, TApplicationException

def Download(ip, port, config_version):
  try:
    #create socket
    socket = TSocket.TSocket(ip, port)
    socket.setTimeout(60 * 1000)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    client = Core.Client(protocol)
    transport.open()

    ret = client.DownloadConfig(config_version)
                          
    transport.close()
    print ip + " result: " + str(ret)
    err_message = ""
    if ret < 0:
      err_message = ip + " result: " + str(ret) 
    return err_message

  except Thrift.TException, ex:
    err_message = ip + " exception: " + str(ex) 
    print "%s" % (ex.message)
    return err_message
  except Exception, ex:
    err_message = ip + " exception: " + str(ex)
    print "%s" % str(ex)
    return err_message

def ReStartProcess(ip, port):
  try:
    #create socket
    socket = TSocket.TSocket(ip, port)
    socket.setTimeout(10 * 1000)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    client = Core.Client(protocol)
    transport.open()

    ret = client.Stop()
    ret = client.StartAfterStoppingSgagent()

    transport.close()
    print ip + " result: " + str(ret)
    err_message = ""
    if ret < 0:
      err_message = ip + " result: " + str(ret) 
    return err_message

  except Thrift.TException, ex:
    err_message = ip + " exception: " + str(ex) 
    print "%s" % (ex.message)
    return err_message
  except Exception, ex:
    err_message = ip + " exception: " + str(ex)
    print "%s" % str(ex)
    return err_message

class Controller():
  def __init__(self, iplist = "iplist.txt", out = "result.txt"):
    self.iplist_file = open(iplist, "r")
    self.result_file = open(out, "w")

  def __del__(self):
    self.iplist_file.close()
    self.result_file.close()
    pass

  def Download(self, numbers, interval, config_version):
    cnt = 0
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__Download(ip, 5288, config_version)
      cnt = cnt + 1
      if (cnt == numbers):
        time.sleep(interval)
        cnt = 0
        print "sleep %s seconds"%interval

  def MultiDownload(self, numbers, interval, config_version):
    cnt = 0
    ip_list = []
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      ip_list.append(ip)
      cnt = cnt + 1
      if (cnt == numbers):
        pool = multiprocessing.Pool(processes=numbers)
        result = []
        for e in ip_list:
          result.append(pool.apply_async(Download, (e, 5288, config_version,)))
        pool.close()
        pool.join()
        time.sleep(interval)
        cnt = 0
        ip_list = []
        
        for res in result:
          self.result_file.write(res.get())
          self.result_file.write("\n")
        print "sleep %s seconds"%interval

  def MultiDownload2(self, numbers, interval, config_version):
    ip_list = []
    pool = multiprocessing.Pool(processes=numbers)
    result = []
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      result.append(pool.apply_async(Download, (ip, 5288, config_version,)))

    pool.close()
    pool.join()
        
    for res in result:
      self.result_file.write(res.get())
      self.result_file.write("\n")
    print "sleep %s seconds"%interval

  def Start(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__Start(ip, 5288)

  def ReStart(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__ReStart(ip, 5288)

  def MultiReStart2(self, numbers, interval):
    ip_list = []
    pool = multiprocessing.Pool(processes=numbers)
    result = []
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      result.append(pool.apply_async(ReStartProcess, (ip, 5288,)))

    pool.close()
    pool.join()
        
    for res in result:
      self.result_file.write(res.get())
      self.result_file.write("\n")
    print "sleep %s seconds"%interval

  def StartAfter(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__StartAfter(ip, 5288)

  def StopCPluginAndStartSgagent(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__StopCPluginAndStartSgagent(ip, 5288)

  def Stop(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__Stop(ip, 5288)

  def Upgrade(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__Upgrade(ip, 5288)

  def HeartBeat(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__HeartBeat(ip, 5288)

  def CheckVersion(self):
    for line in self.iplist_file:
      ip = line
      ip = ip.replace("\r", "").replace("\n", "").replace("\t", "")
      self.__CheckVersion(ip, 5288)

  def PutSo(self, file_name):
    file_name_without_path = file_name.split("/")[-1]
    cmd = "curl --request PUT --data-binary \"@" + file_name + "\" --header \"Content-Type: application/octet-stream\" http://cplugin.test.sankuai.com/res/opt/meituan/apps/sg_agent/" + file_name_without_path
    print cmd
    output = os.popen(cmd)
    print output.read()

  def PutConfig(self, file_name):
    cmd = "curl --request PUT --data-binary \"@" + file_name + "\" --header \"Content-Type: application/octet-stream\" http://cplugin.test.sankuai.com/put/opt/meituan/apps/cplugin/config.xml"
    print cmd
    output = os.popen(cmd)
    print output.read()

  def __Download(self, ip, port, config_version):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(60 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()

      ret = client.DownloadConfig(config_version)
                            
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")

      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s" % (ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __Start(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      ret = client.Start()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s %s" % (ip, ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __ReStart(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      ret = client.Stop()
      ret = client.StartAfterStoppingSgagent()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s %s" % (ip, ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __StartAfter(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      ret = client.StartAfterStoppingSgagent()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s %s" % (ip, ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __StopCPluginAndStartSgagent(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      ret = client.StopCPluginAndStartSgagent()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s %s" % (ip, ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __Stop(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
                            
      ret = client.Stop()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s" % (ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __Upgrade(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(10 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
                            
      ret = client.Upgrade()
      print ip + " result: " + str(ret)
      if ret < 0:
        self.result_file.write(ip + " result: " + str(ret) + "\n")
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s" % (ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __HeartBeat(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(3 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      client.HeartBeat()
                                 
      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s" % (ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

  def __CheckVersion(self, ip, port):
    try:
      #create socket
      socket = TSocket.TSocket(ip, port)
      socket.setTimeout(3 * 1000)
      transport = TTransport.TFramedTransport(socket)
      protocol = TBinaryProtocol.TBinaryProtocol(transport)

      client = Core.Client(protocol)
      transport.open()
      
      version = client.GetVersion()
      self.result_file.write(ip + " version: " + version + "\n") 

      transport.close()
    except Thrift.TException, ex:
      err_message = ex.message
      self.result_file.write(ip + " exception: " + err_message + "\n") 
      print "%s" % (ex.message)
    except Exception, ex:
      self.result_file.write(ip + " exception: " + str(ex) + "\n") 
      print "%s" % str(ex)

class Options:
  def __init__(self):
    self.heartbeat = False
    self.start = False
    self.restart = False
    self.startafter = False
    self.start_old_sgagent = False
    self.stop = False
    self.upgrade = False
    self.download = False
    self.put = False
    self.checkversion = False 
    self.so_filename = ""
    self.config_filename = ""
    self.config_version = ""
    self.threadnum = 1 
    self.interval = 1 
    self.iplist = "iplist.txt"
    self.out = "result.txt"

def ParseOptions(opts, myopts):
  for opt, value in opts:
    if opt in ("--start"):
      myopts.start = True
    elif opt in ("--restart"):
      myopts.restart = True
    elif opt in ("--startafter"):
      myopts.startafter = True
    elif opt in ("--start_old_sgagent"):
      myopts.start_old_sgagent = True
    elif opt in ("--stop"):
      myopts.stop = True
    elif opt in ("--upgrade"):
      myopts.upgrade = True
    elif opt in ("--heartbeat"):
      myopts.heartbeat = True
    elif opt in ("--download"):
      myopts.download = True
    elif opt in ("--put"):
      myopts.put = True
    elif opt in ("--checkversion"):
      myopts.checkversion = True
    elif opt in ("--file"):
      myopts.so_filename = value 
    elif opt in ("--config"):
      myopts.config_filename = value 
    elif opt in ("--config_version"):
      myopts.config_version = value 
    elif opt in ("--interval"):
      myopts.interval = value
    elif opt in ("--threadnum"):
      myopts.threadnum = value
    elif opt in ("--iplist"):
      myopts.iplist = value
    elif opt in ("--out"):
      myopts.out = value

if __name__ == "__main__":
  try:
    opts, args = getopt.getopt(sys.argv[1:], "sdt:n:", ["start", "restart", "startafter", "start_old_sgagent", "stop", "upgrade", "download", "heartbeat", "put", "checkversion", "file=", "config=", "config_version=", "interval=", "threadnum=", "iplist=", "out="])
  except getopt.GetoptError, err:
    print str(err)
    sys.exit(2)

  myopts = Options()
  ParseOptions(opts, myopts)

  if myopts.start == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.Start()
  elif myopts.restart == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.MultiReStart2(int(myopts.threadnum), int(myopts.interval))
  elif myopts.startafter == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.StartAfter()
  elif myopts.start_old_sgagent == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.StopCPluginAndStartSgagent()
  elif myopts.stop == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.Stop()
  elif myopts.upgrade == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.Upgrade()
  elif myopts.download == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.MultiDownload2(int(myopts.threadnum), int(myopts.interval), myopts.config_version)
  elif myopts.heartbeat == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.HeartBeat()
  elif myopts.checkversion == True:
    controller = Controller(myopts.iplist, myopts.out)
    controller.CheckVersion()
  elif myopts.put == True:
    if (myopts.so_filename == ""):
      controller = Controller(myopts.iplist, myopts.out)
      controller.PutConfig(myopts.config_filename)
    else:
      controller = Controller(myopts.iplist, myopts.out)
      controller.PutSo(myopts.so_filename)

