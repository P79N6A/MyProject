//
// Created by Xiang Zhang on 2017/9/12.
//

#include <iostream>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>

#include <boost/algorithm/string.hpp>
#include <muduo/base/Logging.h>

#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

#include <rapidjson/document.h>
#include <rapidjson/filereadstream.h>

#include <cthrift/mcc_sdk/mcc_sdk.h>

#include "common.h"
#include "controller_types.h"
#include "plugin/Core.h"

namespace Controller {
    const char* enum_string[] = {"test", "Install", "Start", "Stop", "Restart", "Upgrade", "Rollback"};
    const char* db_path_ = "/opt/meituan/apps/cplugin_server/config.json";

    size_t write_data(void *ptr, size_t size, size_t nmemb, std::string* p_str){

        if(!p_str || !ptr) {
            return 0;
        }

        char* data = static_cast<char*>(ptr);
        size_t count = size * nmemb;
        p_str->append(data, size * nmemb);
        return count;
    }

    time_t get_zero_clock() {
        time_t t = time(0);   // get time now
        struct tm * zeroTm= localtime(&t);
        zeroTm->tm_hour = 23;
        zeroTm->tm_min = 59;
        zeroTm->tm_sec = 60;
        LOG_INFO << "next regular check ops data in: " << asctime(zeroTm);
        return mktime(zeroTm);
    }

    time_t get_one_clock() {
        time_t t = time(0);   // get time now
        struct tm * zeroTm= localtime(&t);
        zeroTm->tm_mday += 1;
        zeroTm->tm_hour = 01;
        zeroTm->tm_min = 00;
        zeroTm->tm_sec = 00;
	time_t next = mktime(zeroTm);
        LOG_INFO << "next regular update cache in: " << ctime(&next);
        return next;
    }

    using namespace ::apache::thrift;
    using namespace ::apache::thrift::protocol;
    using namespace ::apache::thrift::transport;
    const int RPC_PORT = 5288;

    void sendHeartbeat(const std::string &ip_addr) {
        boost::shared_ptr<TSocket> socket(new TSocket(ip_addr, RPC_PORT));
        socket->setConnTimeout(50);
        socket->setSendTimeout(50);
        socket->setRecvTimeout(50);
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        try {
            cplugin::CoreClient client(protocol);
            transport->open();
            if (0 != client.KeepAlive()) {
                LOG_WARN << "Failed to send heartbeat to " << ip_addr;
            }
            transport->close();
            LOG_INFO << "send heartbeat to " << ip_addr;
        } catch (TApplicationException& tx) {
            LOG_ERROR << "Failed to send plugin action list, exception: " << tx.what() << " ip: " << ip_addr;
        } catch (TException& tx) {
            LOG_ERROR << "Failed to send plugin action list, exception: " << tx.what() << " ip: " << ip_addr;
        }
    }

    int sendCommand(const std::string &ip_addr, const std::vector<cplugin::PluginAction>& plugins){
        boost::shared_ptr<TSocket> socket(new TSocket(ip_addr, RPC_PORT));
        socket->setConnTimeout(50);
        socket->setSendTimeout(50);
        socket->setRecvTimeout(50);
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        int ret = -1;
        try {
            cplugin::CoreClient client(protocol);
            transport->open();
            ret = client.notifyPluginAction(plugins);
            if(0 != ret) {
                LOG_WARN << "Failed to send regular check to " << ip_addr;
            }
            transport->close();
            LOG_INFO << "Send plugin list to " << ip_addr << ", size: " << plugins.size();
        } catch (TApplicationException& tx) {
            LOG_ERROR << "Failed to send plugin action list, exception: " << tx.what() << " ip: " << ip_addr;
        } catch (TException& tx) {
            LOG_ERROR << "Failed to send plugin action list, exception: " << tx.what() << " ip: " << ip_addr;
        }
        return  ret;
    }

    int sendCommand(const std::string &name,
                    const std::string &md5,
                    const std::string &ip_addr,
                    int op, int p_id,
                    int his_id, int retry_num) {
        boost::shared_ptr<TSocket> socket(new TSocket(ip_addr, RPC_PORT));
        socket->setConnTimeout(50);
        socket->setSendTimeout(50);
        socket->setRecvTimeout(50);
        boost::shared_ptr<TFramedTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        int ret = -1;
        try {
            cplugin::CoreClient client(protocol);
            transport->open();
            switch (op) {
                case Operation::INSTALL:
                    ret = client.StartNew(name, md5, p_id, his_id);
                    break;
                //TODO: 前端保证不带md5操作时，拉取争取版本的plugin
                case Operation::START:
                    ret = client.Start(name, p_id, his_id);
                    break;
                case Operation::RESTART:
                    ret = client.ReStart(name, p_id, his_id);
                    break;
                case Operation::STOP:
                    ret = client.Stop(name, p_id, his_id);
                    break;
                case Operation::UPGRADE:
                    ret = client.Upgrade(name, md5, p_id, his_id);
                    break;
                case Operation::ROLLBACK:
                    ret = client.RollBack(name, md5,p_id, his_id);
                    break;
                default:
                    LOG_WARN << "Illegal operation";
            }
            if (0 != ret)
                LOG_WARN << "Operation " << getTextForEnum(op) <<
                          " failed: host " << ip_addr << " plugin " << name;

            transport->close();
            LOG_INFO << "send RPC over";
        } catch (TApplicationException& tx) {
            LOG_ERROR << "Operation " << getTextForEnum(op) <<  " Cause Exception. host " << ip_addr
                      << " plugin " << name << " tx_info: " << tx.what();
        } catch (TException& tx) {
            //read socket cause EAGAIN, sleep 15ms, try again.
            if (retry_num-- && NULL != strstr(tx.what(), "EAGAIN")) {
                LOG_WARN << ip_addr << ":" << tx.what() << "; sleep 15ms and try again";
                usleep(15 * 1000);
                ret = sendCommand(name, md5, ip_addr, op, p_id, his_id, retry_num);
            } else {
                LOG_ERROR << "Operation " << getTextForEnum(op) << " Cause Exception. host " << ip_addr
                          << " plugin " << name << " tx_info: " << tx.what();
            }
        }
        return ret;
    }

    int getMccConfig(const std::string& appkey,
                     const std::string& key,
                     std::string* p_value) {
        std::string str_err_info;
        if (mcc_sdk::InitMCCClient(&str_err_info, 100, 200)) {
            LOG_ERROR << "Init MCC client failed, error_info:  " << str_err_info;
            return -1;
        }
        str_err_info = "";
        if (mcc_sdk::GetCfg(appkey, key, p_value, &str_err_info)) {
            LOG_ERROR << "Get config value from: " << appkey + ":" + key
                      << " failed; error info: " << str_err_info;
            return -1;
        } else {
            LOG_DEBUG << "config value is: " << *p_value;
        }
        mcc_sdk::DestroyMCCClient();
        return 0;
    }

    bool checkMaster(const std::string& appkey,
                     const std::string& key){
        std::string mcc_cfg_value = "";
        if (getMccConfig(appkey, key, &mcc_cfg_value)) {
            return false;
        }

        boost::trim(mcc_cfg_value);
        struct ifaddrs *ifAddrStruct=NULL;
        getifaddrs(&ifAddrStruct);
        char str_ip[INET_ADDRSTRLEN];
        for (struct ifaddrs *ifa = ifAddrStruct; ifa; ifa = ifa->ifa_next) {
            if (ifa->ifa_addr && ifa->ifa_addr->sa_family == AF_INET) {
                struct sockaddr_in *pAddr = reinterpret_cast<struct sockaddr_in *>(ifa->ifa_addr);
                inet_ntop(AF_INET, &(pAddr->sin_addr), str_ip, INET_ADDRSTRLEN);
                //LOG_DEBUG << "local ip addr: " << str_ip;
                if (strcmp(mcc_cfg_value.c_str(), str_ip) == 0) {
                    freeifaddrs(ifAddrStruct);
                    return true;
                }
            }
        }
        freeifaddrs(ifAddrStruct);
        return false;
    }

    using namespace rapidjson;
    int8_t readCfg(std::string *db_name, std::string *db_user,
                   std::string *db_ip, std::string *db_passwd,
                   int *port, int *fast_worker, int *bq_worker) {
        FILE *fp = fopen(db_path_, "r");
        if (NULL == fp) {
            LOG_ERROR << "Open env config file failed";
            return -1;
        }

        char read_buffer[256];
        FileReadStream is(fp, read_buffer, sizeof(read_buffer));

        Document d;
        d.ParseStream(is);
        //read DB cfg content
        if (!d.HasMember("db_name") || !d.HasMember("db_user")
            || !d.HasMember("db_ip") || !d.HasMember("db_passwd")
            || !d.HasMember("db_port") || !d["db_port"].IsInt()) {
            LOG_ERROR << "Can not parse valid DB connect info, stop server emit";
            fclose(fp);
            return -1;
        }
        *db_name =  d["db_name"].GetString();
        *db_user =  d["db_user"].GetString();
        *db_ip =  d["db_ip"].GetString();
        *db_passwd =  d["db_passwd"].GetString();
        *port = d["db_port"].GetInt();

        //read work thread number
        if (!d.HasMember("fast_worker") || !d["fast_worker"].IsInt()) {
            LOG_INFO << "There is not a config option for slow_worker_thread, will use default setting 3";
            *fast_worker = 3;
        } else {
            *fast_worker = d["fast_worker"].GetInt();
        }

        if (!d.HasMember("bq_worker") || !d["bq_worker"].IsInt()) {
            LOG_INFO << "There is not a config option for bq_worker_thread, will use default setting 2";
            *bq_worker = 2;
        } else {
            *bq_worker = d["bq_worker"].GetInt();
        }
        fclose(fp);
        return 0;
    }
}
