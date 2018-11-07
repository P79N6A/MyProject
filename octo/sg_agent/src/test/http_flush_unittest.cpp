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

#include "comm/inc_comm.h"
#include <gtest/gtest.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>
#include <string>
#include "muduo/net/EventLoop.h"
#include "muduo/net/EventLoopThread.h"
#include "curl/curl.h"

#define private public
#define protected public
#include "sg_agent/file_config_client.h"
#undef private
#undef protected


#include "sg_agent/file_config_client.h"

using namespace sg_agent;
class HttpFlushNode : public testing::Test {
 public:
  virtual void SetUp() {
	  loop_1 = thread_1.startLoop();
	  loop_2 = thread_2.startLoop();
	  loop_3 = thread_3.startLoop();
	  loop_4 = thread_4.startLoop();
	  std::string url = "http://10.4.245.3:5267/api/mns/provider/";
	  std::string get_method = "get";
	  std::string replace_method = "replace";
	  std::string get_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\"}\'";
	  std::string replace_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\",\"serviceList\":[{\"ip\":\"10.4.227.247.344\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.245\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":1,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.246.345\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"port\":5266,\"protocol\":\"thrift\",\"status\":2,\"lastUpdateTime\":1511167765,\"extend\":\"OCTO|slowStartSeconds:180\",\"appkey\":\"com.sankuai.octo.test\",\"heartbeatSupport\":2,\"env\":3,\"serviceInfo\":{\"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService\":{\"unifiedProto\":1},\"xxx\":{\"unifiedProto\":2}},\"version\":\"mtthrift-v1.8.2\",\"ip\":\"10.4.245.3\",\"serverType\":0,\"role\":0}]}\'";
	  std::string normal_replace_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\",\"serviceList\":[{\"ip\":\"10.4.227.244\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.245\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.246\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"port\":5266,\"protocol\":\"thrift\",\"weight\":40,\"status\":2,\"lastUpdateTime\":1511167765,\"extend\":\"OCTO|slowStartSeconds:180\",\"fweight\":10,\"appkey\":\"com.sankuai.octo.tmy\",\"heartbeatSupport\":2,\"env\":3,\"serviceInfo\":{\"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService\":{\"unifiedProto\":1},\"xxx\":{\"unifiedProto\":2}},\"version\":\"mtthrift-v1.8.2\",\"ip\":\"10.4.245.3\",\"serverType\":0,\"role\":0}]}\'";

  }

  void HandleSystemCmd() {
	  system("./http_flush_node.sh");
  }

  virtual void TearDown() {
  }
 public:
  muduo::net::EventLoopThread thread_1;
  muduo::net::EventLoopThread thread_2;
  muduo::net::EventLoopThread thread_3;
  muduo::net::EventLoopThread thread_4;
  muduo::net::EventLoop* loop_1;
  muduo::net::EventLoop* loop_2;
  muduo::net::EventLoop* loop_3;
  muduo::net::EventLoop* loop_4;
  std::string url;
  std::string get_method;
  std::string replace_method;
  std::string get_data;
  std::string replace_data;
  std::string normal_replace_data;
 protected:
};
/*
TEST_F(HttpFlushNode, ManyThreadFlush){
	loop_1->runEvery(0.01, boost::bind(&HttpFlushNode::HandleSystemCmd,this));
	loop_2->runEvery(0.01, boost::bind(&HttpFlushNode::HandleSystemCmd,this));
	loop_3->runEvery(0.01, boost::bind(&HttpFlushNode::HandleSystemCmd,this));
	loop_4->runEvery(0.01, boost::bind(&HttpFlushNode::HandleSystemCmd,this));
	std::cout<<"run buffer_mgr in multi-thread, please wait 60s." << std::endl;
	sleep(1);
}*/

TEST_F(HttpFlushNode, get) {
	std::string real_url = url+get_method;
	CURL* curl = curl_easy_init();
	CURLcode code;
	code = curl_easy_setopt(curl, CURLOPT_URL, real_url);
	curl_easy_setopt(curl, CURLOPT_POST, 1);
	std::string data = get_data;
	code = curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.c_str());


	//curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, CallBackFunc);
	//设置写数据
	std::string response_data;
	curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void*)&response_data);
	// 执行请求
	code = curl_easy_perform(curl);

	if(code == CURLE_OK) {
		long responseCode = 0;
		curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &responseCode);
		if (responseCode < 200 || responseCode >= 300 || response_data.empty()) {
			std::cout <<" the wrong response code"<<std::endl;
		}
		std::cout << response_data << std::endl;
	}
	//下面可以对应答的数据进行处理了
	// strData

	// 清除curl对象
	curl_easy_cleanup(curl);

	/*std::string cmd = "curl -i -X POST -H 'Content-type':'application/json' -d";
	std::string url = "http://10.4.245.3:5267/api/mns/provider/";
	std::string get_method = "get";
	std::string replace_method = "replace";
	std::string get_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\"}\'";
	std::string replace_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\",\"serviceList\":[{\"ip\":\"10.4.227.247.344\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.245\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":1,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.246.345\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"port\":5266,\"protocol\":\"thrift\",\"status\":2,\"lastUpdateTime\":1511167765,\"extend\":\"OCTO|slowStartSeconds:180\",\"appkey\":\"com.sankuai.octo.test\",\"heartbeatSupport\":2,\"env\":3,\"serviceInfo\":{\"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService\":{\"unifiedProto\":1},\"xxx\":{\"unifiedProto\":2}},\"version\":\"mtthrift-v1.8.2\",\"ip\":\"10.4.245.3\",\"serverType\":0,\"role\":0}]}\'";
	std::string normal_replace_data = "\'{\"remoteAppkey\":\"com.sankuai.octo.tmy\",\"protocol\":\"thrift\",\"serviceList\":[{\"ip\":\"10.4.227.244\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.245\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"ip\":\"10.4.227.246\",\"port\":5266,\"protocol\":\"thrift\",\"serverType\":0,\"appkey\":\"com.sankuai.octo.tmy\"},{\"port\":5266,\"protocol\":\"thrift\",\"weight\":40,\"status\":2,\"lastUpdateTime\":1511167765,\"extend\":\"OCTO|slowStartSeconds:180\",\"fweight\":10,\"appkey\":\"com.sankuai.octo.tmy\",\"heartbeatSupport\":2,\"env\":3,\"serviceInfo\":{\"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService\":{\"unifiedProto\":1},\"xxx\":{\"unifiedProto\":2}},\"version\":\"mtthrift-v1.8.2\",\"ip\":\"10.4.245.3\",\"serverType\":0,\"role\":0}]}\'";

	std::string cmd1 = cmd + " " + get_data + " " + url + get_method;//get获取当前缓存大小和serlist
	system(cmd1.c_str());
	sleep(3);

	std::string cmd2 = cmd + " " + normal_replace_data + " " + url + replace_method;//replace 服务列表正常
	system(cmd2.c_str());
	sleep(3);

	std::string cmd3 = cmd + " " + get_data + " " + url + get_method;//get获取当前的缓存大小和serlist
	system(cmd3.c_str());
	sleep(3);

	std::string cmd4 = cmd + " " + replace_data + " " + url + replace_method;//replace 服务列表中有不符合规范的信息	// （IP不符合规范，protocol和serverType不匹配）
	system(cmd4.c_str());
	sleep(3);

	std::string cmd5 = cmd + " " + get_data + " " + url + get_method; //获取观察结果
	system(cmd5.c_str());
	sleep(1);
*/

}

TEST_F(HttpFlushNode, FlushServieNode){
//TODO add test case for http service.
//   std::cout<<"test the flush node"<<std::endl;
//	 system("./http_flush_node.sh");
}
