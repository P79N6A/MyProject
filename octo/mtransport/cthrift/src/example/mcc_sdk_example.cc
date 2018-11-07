//
// Created by Xiang Zhang on 17/9/6.
//

#include <map>

#include <boost/algorithm/string.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/unordered/unordered_map.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/random.hpp>

#include <cthrift/cthrift_sgagent.h>
#include <cthrift/mcc_sdk/mcc_sdk.h>

using namespace std;
using namespace cthrift;
using namespace mcc_sdk;

void Job(const string
         &str_appkey,
         const string
         &str_file_name,
         const string
         &str_file_content) {
  cout << "Invoke job" << endl;
  cout << "appkey: " << str_appkey << " filename:" << str_file_name
       << " file content: " << str_file_content << endl;
}

void Job1(const string
         &str_appkey,
         const string
         &str_key,
         const string
         &str_new_value,
		const string
		&str_old_value) {
  cout << "Invoke job" << endl;
  cout << "appkey: " << str_appkey << " key:" << str_key
       << " new value: " << str_new_value
       <<" old value: "<< str_old_value << endl;
}

void Job2(const string
         &str_appkey,
         int64_t
         version,
         const std::map<std::string, std::string>
         &kv_map) {
  cout << "Invoke job" << endl;
  cout << "appkey: " << str_appkey << " version:" << version
       << " dynamic config content: " << kv_map.size() << endl;
  /*for (map<string, string>::const_iterator it = kv_map.begin(); it != kv_map.end(); ++it) {
	  cout << it->first << ":" << it->second << ";";
  }*/
}

int main(void) {
  //注意：请使用业务自身的appkey进行cat初始化！！！！！
  catClientInit("com.sankuai.inf.newct");

  string str_err_info;
  //启用统一日志，一个进程仅初始化日志一次
  CLOG_INIT();

  //一个业务线程，仅Init一次。
  if (InitMCCClient(&str_err_info, "com.sankuai.inf.newct.client", 100, 200)) {
    cerr << str_err_info << endl;
    return -1;
  }

  //获取静态配置文件	
  string str_appkey("com.sankuai.inf.newct");
  string str_file_name("test.txt");
  str_err_info = "";
  boost::function < void(
      const string
      &str_appkey,
      const string
      &str_file_name,
      const string
      &str_file_content)> job(boost::bind(&Job, _1, _2, _3));

  if(SetFileConfigCallbackFunc(str_appkey, str_file_name, job, &str_err_info)){
    cerr << str_err_info << endl;
    return -1;
  }

  str_err_info = "";
  string str_file_content;
  if (GetFileCfg(str_appkey,
                 str_file_name,
                 &str_file_content,
                 &str_err_info)) {
    cerr << str_err_info << endl;
  } else{
    cout << "file content " << str_file_content << endl;
  }


  //获取动态配置文件
  boost::function < void(
      const string
      &str_appkey,
      const string
      &str_key,
      const string
      &str_new_value,
	  const string
      &str_old_value)> key_job(boost::bind(&Job1, _1, _2, _3, _4));

  str_err_info = "";
  string str_key = "hawktest", str_key2 = "testdelete";
  if(AddConfigCallbackFunc(str_appkey, str_key, key_job, &str_err_info)){
    cerr << str_err_info << endl;
    return -1;
  }

  str_err_info = "";
  string str_value;
  if (GetCfg(str_appkey,
             str_key,
             &str_value,
             &str_err_info)) {
        cerr << str_err_info << endl;
  } else{
        cout << "value of " << str_key <<  " is " << str_value << endl;
  }

  str_err_info = "";
  if(AddConfigCallbackFunc(str_appkey, str_key2, key_job, &str_err_info)){
    cerr << str_err_info << endl;
    return -1;
  }

  str_err_info = "";
  str_value = "";
  if (GetCfg(str_appkey,
                 str_key2,
                 &str_value,
                 &str_err_info)) {
    cerr << str_err_info << endl;
  } else{
    cout << "value of " << str_key2 <<  " is " << str_value << endl;
  }

  boost::function < void (const string
         &str_appkey,
         int64_t
         version,
         const std::map<std::string, std::string>
         &kv_map)> zk_global_job(boost::bind(&Job2, _1, _2, _3));

  str_err_info = "";
  if(SetGlobalConfigCallbackFunc(str_appkey, zk_global_job, &str_err_info)){
    cerr << str_err_info << endl;
    return -1;
  }

  str_err_info = "";
  map<string, string> ret_map;
  if (GetGlobalCfg(str_appkey,
                 &ret_map,
                 &str_err_info)) {
    cerr << str_err_info << endl;
  } else{
       cout << " map_size: " << ret_map.size() << endl;
    /*for (map<string, string>::iterator it = ret_map.begin(); it != ret_map.end(); ++it) {
		cout << it->first << ":" << it->second << ";";
	}*/
  }

  str_err_info = "";
  SetCfg(str_appkey, "new_key", "new_value", "", &str_err_info);
  sleep(10);
  DestroyMCCClient();
  CLOG_CLOSE();
  return 0;
}
