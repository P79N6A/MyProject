// =====================================================================================
// 
//       Filename:  main.cpp
// 
//    Description:  learn msg
// 
//        Version:  1.0
//        Created:  2015-04-17 11时41分55秒
//       Revision:  none
// 
// =====================================================================================
#include <iostream>
#include <stdio.h>
#include "comm/inc_comm.h"
#include <gtest/gtest.h>
#include "sg_agent/count_request.h"
#include "muduo/base/CountDownLatch.h"
#include "muduo/net/EventLoopThreadPool.h"
#include "muduo/net/EventLoopThread.h"
#include "muduo/net/EventLoop.h"
#define private public
#define protected public
#include "sg_agent/file_config_client.h"
#include "sg_agent/thrift_client_handler.h"
#undef private
#undef protected


#include "sg_agent/file_config_client.h"

using namespace sg_agent;
class FileConfig : public testing::Test {
 public:
  virtual void SetUp() {
    filename = "setting.xml";
    filepath = "/opt/meituan/apps/mcc";
    content = "this is a test file write error";
    file_client = FileConfigClient::getInstance();
  	config_collector_test= new MtConfigCollector();
		config_collector_test->init();
		m_loop = m_thread_pool.startLoop();
	}	

  virtual void TearDown() {
  }
	void monitor() {	
		boost::unordered_map<std::string, int> count;
		CountRequest::GetInstance()->GetReqData(count);
	}

	muduo::net::EventLoop* m_loop;
	muduo::net::EventLoopThread m_thread_pool;
 protected:
  
	std::string filename;
  std::string filepath;
  std::string content;
	FileConfigClient* file_client;
	MtConfigCollector* config_collector_test;
};

TEST_F(FileConfig, write) {
	std::cout<<"this is test to write the full disk"<<std::cout<<std::endl;
  EXPECT_EQ(0, writeToTmp(content, filename, filepath));
}

TEST_F(FileConfig, move) {
  EXPECT_EQ(0, moveForWork(filename, filepath));
}

TEST_F(FileConfig, load) {
  std::string loadcontent;
  EXPECT_EQ(0, loadFile(loadcontent, filename, filepath));
  EXPECT_STREQ(content.c_str(), loadcontent.c_str());
  std::cout << loadcontent << std::endl;
}

TEST_F(FileConfig,getFileConfigFromMtconfig){

  //根据消息类型，进行业务逻辑处理
  file_param_t res_msg;
  std::vector<ConfigFile>  configFiles;
  ConfigFile config_file;
  config_file.filename = "ssettings.xml";
  config_file.filepath = "/opt/meituan/apps/mcc";
  configFiles.push_back(config_file);
  //解析msg
  res_msg.__set_appkey("com.sankuai.octo.tmy");
  res_msg.__set_path("/opt/meituan/apps/mcc");
  res_msg.__set_cmd(4);
  res_msg.__set_configFiles(configFiles);
  res_msg.__set_ip("10.5.245.3");
//	m_loop->runEvery(2,boost::bind(&FileConfig::monitor,this));
	int i = 0;
	while (++i<10000) {
  	FileConfigClient::getInstance()->getFileConfigFromWorker(res_msg);
	}
	
}

TEST_F(FileConfig, SyncFileConfig){

}

TEST_F(FileConfig, GetFileConfig) {
  file_param_t res_msg;
  std::vector<ConfigFile>  configFiles;
  ConfigFile config_file;
  config_file.filename = "settings.xml";
  config_file.filepath = "/opt/meituan/apps/mcc";
  configFiles.push_back(config_file);
  //解析msg
  res_msg.__set_appkey("com.sankuai.octo.tmy");
  res_msg.__set_path("/opt/meituan/apps/mcc");
  res_msg.__set_cmd(4);
  res_msg.__set_configFiles(configFiles);
  res_msg.__set_ip("10.5.245.3");
	boost::unordered_map<std::string, int> count;
  CountRequest::GetInstance()->GetReqData(count);
  for ( int i = 0; i < 10 ; ++i) {
    EXPECT_EQ(501,config_collector_test->getFileConfigData(res_msg));
  }
  CountRequest::GetInstance()->GetReqData(count);
  //EXPECT_EQ(10, count.at("fconfig"));
  //EXPECT_EQ(10, count.at("allfconfig"));

  //todo 失败的场景构造
}
