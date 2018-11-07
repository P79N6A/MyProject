#include <string.h>
#include <iostream>
#include <zookeeper/zookeeper.h>
using namespace std;

int main(int argc, char** argv)
{
    //init handler
    string server_ = "192.168.12.164:2181";
    zhandle_t *zk_handler_ = zookeeper_init(server_.c_str(), NULL, 30000, 0, NULL, 0);
    if(zk_handler_ == NULL){
        cout << "zookeeper_init return null with ip: " << server_;
        return -1;
    }

    //create appkey
    std::string base_zkpath_ = "/mns/sankuai/stage/";
    std::string appkey_ = "com.sankuai.inf.chenxin4";
    string newpath = base_zkpath_ + appkey_;
    string strJson = "test";
    int ret = zoo_create(zk_handler_, newpath.c_str(), strJson.c_str(), strJson.size(), &ZOO_OPEN_ACL_UNSAFE, 0,  NULL, 0);
    if (ret) {
        cout << "Error for create appkey fail" << endl;
        //return -1;
    }

    //sleep(1);
    
    //create provider
    newpath += "/provider";
    ret = zoo_create(zk_handler_, newpath.c_str(), strJson.c_str(), strJson.size(), &ZOO_OPEN_ACL_UNSAFE, 0,  NULL, 0);
    if (ret) {
        cout << "Error for create provider" << endl;
    }


    //close
    zookeeper_close(zk_handler_);
}
