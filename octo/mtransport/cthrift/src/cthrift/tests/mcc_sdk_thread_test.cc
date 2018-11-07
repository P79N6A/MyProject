//
// Created by hawk on 2017/9/7.
//
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

boost::function < void (const string &str_appkey,
                        int64_t version,
                        const std::map<std::string, std::string>
                        &kv_map)> zk_global_job(boost::bind(&Job2, _1, _2, _3));


void testThreadFunc(muduo::CountDownLatch *p_countdown) {
    string str_err_info;
    if (-1 == InitMCCClient(&str_err_info, 50, 100)) {
        return;
    }
    string str_file_content;
    string str_appkey("com.sankuai.inf.msgp"), str_file_name("test.txt");
    GetFileCfg(str_appkey, str_file_name, &str_file_content, &str_err_info);
    SetFileConfigCallbackFunc(str_appkey, str_file_name, job, &str_err_info);
    cout << "file content " << str_file_content << endl;

    string str_key("hawktest"), str_zk_value;
    if (0 == GetCfg(str_appkey, str_key, &str_zk_value, &str_err_info)) {
        cout << "zk value " << str_zk_value << endl;
    }
    AddConfigCallbackFunc(str_appkey, str_key, zk_key_job, &str_err_info);

    map<string, string> ret_map;
    GetGlobalCfg(str_appkey, &ret_map, &str_err_info);
    SetGlobalConfigCallbackFunc(str_appkey, zk_global_job, &str_err_info);
    cout << "zk value " << str_zk_value << endl;

    sleep(5);
    DestroyMCCClient();
    p_countdown->countDown();
}

int main(int argc, char** argv) {
    int thread_num = 4;
	CLOG_CLOSE();
    muduo::CountDownLatch countdown_thread_finish(thread_num);
    for (int i = 0; i < thread_num; ++i) {
         muduo::net::EventLoopThread *pt =
              new muduo::net::EventLoopThread;

         pt->startLoop()->runInLoop(boost::bind(testThreadFunc,
                                                &countdown_thread_finish));
    }
    countdown_thread_finish.wait();
	CLOG_CLOSE();
    return 0;
}
