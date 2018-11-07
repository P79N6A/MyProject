//
// Created by Xiang Zhang on 2017/8/30.
//

#ifndef CONTROLSERVER_DBWORKER_H
#define CONTROLSERVER_DBWORKER_H

#include <string>
#include <iostream>

#include <mysql++/mysql++.h>

#include "common.h"

namespace Controller {

class Storage {
public:
    Storage() {
    }

    Storage(const std::string& db_name, const std::string& db_server,
            const std::string& db_user, const std::string& db_pass, 
	        int db_port);

    int init();

    int64_t recordHistory(int plugin_id, int op_type,
                          op_status flag, const std::string& content);

    int getAllIdc(std::vector<std::string> &idc_list);

    int getIdc(const std::string &region,
               const std::string &center,
               std::vector<std::string> &idc_list);

    int getPdl(const std::string &owt,
               std::vector<std::string> &pdl_list);

    int setPdl(const std::string &pdl,
               const std::string &owt);

    int setPdlList(const std::vector<std::string> &pdls);

    int64_t getTupleId(const std::string &env,
                           const std::string &pdl,
                           const std::string &idc);

    int64_t setTuple(const std::string &env,
                     const std::string &pdl,
                     const std::string &idc);

    int getIPAffiliation(const std::string &IP);

    int setIPAffiliation(const std::vector<ip_host> &ip_hostname, int tuple_id);

    int getIPList(int tuple_id, srv_rank r,
                  std::vector<std::string>& ret);

    int64_t getPluginId(const std::string& md5);

    int64_t setPlugin(const std::string& name, const std::string& md5,
                      const std::string& version="", const std::string& lib_content="");

    int getUpdateInfo(int rule_id, int *plugin_id, int *tuple_id,
                      int *op_type, std::string *name, std::string *md5);

    int64_t setRule(int plugin_id, int tuple_id, int task_id,
                    const std::string &plugin_name, int op_type);

    int SetIPUpgrade(const std::string &ip,
                     const std::string& plugin_name,
                     int task_id,
                     int plugin_id,
                     int op_type);

    int getIPtime(const std::string &ip,
                  const std::string &plugin_name,
                  time_t *ip_time);

    int setReport(int task_id, int plugin_id, const std::string& ip,
                  plugin_status flag, const std::string& err);

    int getReport(int task_id, const std::string& ip);

    int getAllTupleId(std::vector<int>& tuple_list);

    int getPluginsByTuple(int tuple_id, int filter_type,
                          std::vector<std::string>& p_names);

    bool isTaskCompleted(int task_id);

    int checkPlugin(const std::string &ip,
                    const std::string &plugin_name,
                    int *p_plugin_id,
                    int *p_task_id,
                    int *p_type,
                    std::string *p_md5);

    int updatePluginHealth(const std::string& ip,
                           const std::string& p_name,
                           int16_t status);

    int findLeftPlugin(const std::string &plugin_name,
                       int tuple_id,
                       int *p_plugin_id,
                       int *p_task_id,
                       std::string *p_md5);
    bool isFetchOpsNow();

    void deleteExpiredIP(int hour);

    void setDBName(const std::string& db_name) {
        db_name_ = db_name;
    }

    void setDBServer(const std::string& db_server) {
        db_server_ = db_server;
    }

    void setDBUser(const std::string& db_user) {
        db_user_ = db_user;
    }

    void setDBPass(const std::string& db_pass) {
        db_pass_ = db_pass;
    }

    ~Storage() {
        db_conn_.disconnect();
    }
private:
    std::string db_name_;
    std::string db_server_;
    std::string db_user_;
    std::string db_pass_;
    int db_port_;
    mysqlpp::Connection db_conn_;


private:
    int queryList(mysqlpp::Query& query, std::vector<std::string> &region_list);
};

}


#endif //CONTROLSERVER_DBWORKER_H
