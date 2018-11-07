//
// Created by Xiang Zhang on 2017/9/12.
//

#ifndef CONTROLSERVER_COMMOM_H
#define CONTROLSERVER_COMMOM_H

#include <string>
#include <arpa/inet.h>
#include <time.h>

#include "plugin/plugin_types.h"

namespace Controller {
    enum srv_rank {
        NON_CORE_SRV = 0,
        CORE_SRV
    };

    enum op_status {
        PROCESSING = 0,
        PROCESSED
    };

    enum plugin_status {
        UPDATING = 0,
        SUCCESS,
        OPERATION_FAIL,
        SEND_RPC_FAIL
    };

    struct ip_host {
        std::string ip_;
        std::string host_;
        int rank_;
        ip_host(const std::string& ip,
                const std::string& host,
                srv_rank tmp_rank = NON_CORE_SRV)
                : ip_(ip), host_(host),rank_(tmp_rank) {

        }
    };

    extern const char* enum_string[];
    inline const char * getTextForEnum( int enumVal ){
        if (enumVal >= 10000)
            enumVal = -1;
        return enum_string[enumVal+1];
    }

    size_t write_data(void *ptr, size_t size, size_t nmemb, std::string* p_str);

    time_t get_zero_clock();
    time_t get_one_clock();

    inline bool is_ipv4_addr(const std::string& str) {
        struct sockaddr_in sa;
        return 0 != inet_pton(AF_INET, str.c_str(), &(sa.sin_addr));
    }

    void sendHeartbeat(const std::string &ip_addr);
    int sendCommand(const std::string &ip_addr, const std::vector<cplugin::PluginAction>& plugins);

    int sendCommand(const std::string &name,
                    const std::string &md5,
                    const std::string &ip_addr,
                    int op, int p_id,
                    int task_id, int r_num);

    int8_t readCfg(std::string *db_name,
                   std::string *db_user,
                   std::string *db_ip,
                   std::string *passwd,
                   int *port,
                   int *fast_worker,
                   int *bq_worker);
    bool checkMaster(const std::string& appkey,
                     const std::string& key);
}


#endif //CONTROLSERVER_COMMOM_H
