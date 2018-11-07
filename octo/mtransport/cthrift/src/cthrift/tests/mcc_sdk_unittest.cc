//
// Created by hawk on 2017/9/7.
//
#include <iostream>

#include <gtest/gtest.h>

#include <cthrift/mcc_sdk/mcc_sdk.h>

using namespace std;
using namespace mcc_sdk;

using testing::Types;

void Job(const string
          &str_appkey,
          const string
          &str_file_name,
          const string
          &str_file_content) {
    cout << "Invoke job" << endl;
    if ("com.sankuai.inf.newct" == str_appkey) {
	string str_zk_value = "", str_err_info = "";
	EXPECT_EQ(0, GetCfg("com.sankuai.inf.octo.cpluginserver", "ops_fetch_node", &str_zk_value, &str_err_info));
	EXPECT_EQ("10.21.130.250", str_zk_value);
    }
    
}

void Job1(const string
         &str_appkey,
         const string
         &str_key,
         const string
         &str_new_value,
          const string
          &str_old_value) {
    cout << "Invoke job new value" << str_new_value << " old value " << str_old_value << endl;
}

void Job2(const string
          &str_appkey,
          int64_t
          version,
          const std::map<std::string, std::string>
          &kv_map) {
    cout << "Invoke job2 version " << version << " kv size: " << kv_map.size() << endl;
}

boost::function < void(
       const string
       &str_appkey,
       const string
       &str_file_name,
       const string
       &str_file_content)> job(boost::bind(&Job, _1, _2, _3));


boost::function < void(
       const string
       &str_appkey,
       const string
       &str_key,
       const string
       &str_new_value,
       const string
       &str_old_value)> zk_key_job(boost::bind(&Job1, _1, _2, _3, _4));

boost::function < void (const string
          &str_appkey,
          int64_t
          version,
          const std::map<std::string, std::string>
          &kv_map)> zk_global_job(boost::bind(&Job2, _1, _2, _3));

TEST(GetConfTest, HandleZeroReturn) {
    string str_err_info, str_file_content;
    string str_appkey("com.sankuai.inf.newct"), str_file_name("test.txt");
    EXPECT_EQ(0, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
    EXPECT_EQ(0, SetFileConfigCallbackFunc(str_appkey, str_file_name, job, &str_err_info));
    cout << "file content " << str_file_content << endl;

    string str_key("hawktest"), str_zk_value;
    EXPECT_EQ(0, GetCfg(str_appkey, str_key, &str_zk_value, &str_err_info));
    EXPECT_EQ(0, AddConfigCallbackFunc(str_appkey, str_key, zk_key_job, &str_err_info));
    cout << "zk value " << str_zk_value << endl;

    map<string, string> ret_map;
    EXPECT_EQ(0, GetGlobalCfg(str_appkey, &ret_map, &str_err_info));
    EXPECT_EQ(0, SetGlobalConfigCallbackFunc(str_appkey, zk_global_job, &str_err_info));
    cout << "zk value " << str_zk_value << endl;


    //write case
    str_appkey = "com.sankuai.octo.tmy";
    str_err_info = "";
    ret_map.clear();
    if (GetGlobalCfg(str_appkey,
            &ret_map, &str_err_info)) {
    cerr << str_err_info << endl;
    } else{
    cout << " map_size: " << ret_map.size() << endl;
    }
    SetCfg(str_appkey, "hawktest", "#1#2", "12F871EDB38C497D624D5D5C6105501FDD073DDD", &str_err_info);
    sleep(20);
}

TEST(GetConfTest, HandleNotZeroReturn) {
string str_err_info, str_file_content;
string str_appkey("com.sankuai.inf.a"), str_file_name("test.txt");
EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
cout << str_err_info << endl;
str_appkey="", str_file_name = "test.txt";
EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
cout << str_err_info << endl;
str_appkey="com.sankuai.inf.msgp", str_file_name = "test.t";
EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
cout << str_err_info << endl;
str_appkey="com.sankuai.inf.msgp", str_file_name = "";
EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
cout << str_err_info << endl;
str_appkey="", str_file_name = "";
EXPECT_EQ(-1, GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info));
cout << str_err_info << endl;

str_appkey="com.sankuai.inf.ms";
string str_key("hawktest"), str_zk_value;
EXPECT_EQ(-1, GetCfg(str_appkey, str_key, &str_zk_value, &str_err_info));
cout << str_err_info << endl;

str_appkey="com.sankuai.inf.msgp";
str_key = "123";
EXPECT_EQ(-1, GetCfg(str_appkey, str_key, &str_zk_value, &str_err_info));
cout << str_err_info << endl;

str_appkey="com.sankuai.inf.msgp", str_key = "hawktest";
string *p_str_zk_value = 0;
EXPECT_EQ(-1, GetCfg(str_appkey, str_key, p_str_zk_value, &str_err_info));
cout << str_err_info << endl;


map<string, string> *p_ret_map = 0;
EXPECT_EQ(-1, GetGlobalCfg(str_appkey, p_ret_map, &str_err_info));
cout << "zk value " << str_zk_value << endl;
}

