//
// Created by Xiang Zhang on 2017/8/30.
//
#include <iostream>
#include <boost/lexical_cast.hpp>

#include <mysqld_error.h>

#include <muduo/base/Logging.h>

#include "storage.h"

namespace Controller {
    using namespace std;

    Storage::Storage(const string &db_name, const string &db_server,
                       const string &db_user, const string &db_pass,
		     int db_port)
            :db_name_(db_name), db_server_(db_server),
             db_user_(db_user), db_pass_(db_pass), db_port_(db_port) {
    }

    int Storage::init() {
        //establish db connection
        try {
            db_conn_ = mysqlpp::Connection(
                    db_name_.c_str(), db_server_.c_str(),
                    db_user_.c_str(), db_pass_.c_str(), db_port_);
            //set auto-reconnection, keepalive
            db_conn_.set_option(new mysqlpp::ReconnectOption(true));
        } catch (mysqlpp::Exception tx) {
            LOG_ERROR << "Init mysql connect exception: " << "tx_info: " << tx.what();
            return -1;
        }
        LOG_DEBUG << "Init mysql connect success";
        return 0;
    }

    int64_t Storage::recordHistory(int plugin_id, int op_type,
                                   op_status flag, const string& content) {
        try {
            mysqlpp::Query query = db_conn_.query();
            {
                mysqlpp::Transaction trans(db_conn_,
                                           mysqlpp::Transaction::serializable,
                                           mysqlpp::Transaction::session);
                query << "INSERT into `history` (plugin_id, op_type, flag, content, timestamp) "
                      << "VALUES(%0, %1, %2, %3q, %4)";
                query.parse();
                query.execute(plugin_id, op_type, flag, content, mysqlpp::DateTime::now());

                query.reset();
                query << "SELECT LAST_INSERT_ID()";
                mysqlpp::StoreQueryResult res = query.store();
                trans.commit();
                if (!res.empty()) {
                    return res[0][0];
                }
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                        "\tretrieved data size: " << tx.retrieved <<
                        ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int Storage::getAllIdc(vector<string> &idc_list) {
        mysqlpp::Query query = db_conn_.query(
                "SELECT idc FROM `region`");
        return queryList(query, idc_list);
    }

    int Storage::getIdc(const string &region,
                        const string &center,
                        vector<string> &idc_list) {
        mysqlpp::Query query = db_conn_.query();
        query << "SELECT idc FROM `region`";
        if (!region.empty()) {
            query << " WHERE region = " << mysqlpp::quote_only << region;
        }
        if (!center.empty()) {
            query << " AND center = " << mysqlpp::quote_only << center;
        }
        return queryList(query, idc_list);
    }

    int Storage::setPdlList(const vector<string> &pdls) {
        if (pdls.empty()) {
            LOG_DEBUG << "no need to update pdl info";
            return 0;
        }
        string str_value;
        str_value.reserve(pdls.size() * 22);
        for (vector<string>::const_iterator itr = pdls.begin();
             pdls.end() != itr; ++itr) {
            string::size_type pos = itr->find_last_of('.');
            if (string::npos == pos) {
                LOG_WARN << *itr << " is not a valid pdl format";
                continue;
            }
            str_value += "('" + *itr + "','" + itr->substr(0, itr->find_last_of('.')) + "'),";
        }
        str_value[str_value.size()-1] = ' ';

        try {
            mysqlpp::Query query = db_conn_.query();
            query << "INSERT into `departure`(pdl, owt) VALUES " << str_value
                  << "ON DUPLICATE KEY UPDATE timestamp=%0";
            query.parse();
            query.execute(mysqlpp::DateTime::now());
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return 0;
    }

    int Storage::setPdl(const string &pdl, const string &owt) {
        //promise pdl is unique
        try {
            mysqlpp::Query query = db_conn_.query("INSERT into `departure`(pdl, owt, timestamp) "
                                  "VALUES(%0q, %1q, %2) ON DUPLICATE KEY UPDATE timestamp=%2");
            query.parse();
            query.execute(pdl, owt, mysqlpp::DateTime::now());
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return 0;
    }

    int Storage::getPdl(const string &owt, vector<string> &pdl_list) {
        mysqlpp::Query query = db_conn_.query();
        query << "SELECT pdl FROM `departure`";
        if (!owt.empty()) {
            query << " WHERE owt = " << mysqlpp::quote_only << owt;
        }
        return queryList(query, pdl_list);
    }

    int Storage::getIPList(int tuple_id, srv_rank r, vector<string>& ip_list) {
        mysqlpp::Query query = db_conn_.query();
        query << "SELECT ip FROM `ip_affiliation` WHERE "
              << "tuple_id=" << tuple_id
              << " AND rank=" << r;
        return queryList(query, ip_list);
    }

    int64_t Storage::getTupleId(const string &env, const string &pdl, const string &idc) {
        int64_t ID = -1;
        try {
            mysqlpp::Query query = db_conn_.query(
                    "SELECT id FROM `tuple` WHERE env=%0q AND pdl=%1q AND idc=%2q limit 1");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(env, pdl, idc);
            if (!res.empty()) {
                ID = res[0][0];
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return ID;
    }

    int64_t Storage::setTuple(const string &env, const string &pdl, const string &idc) {
        int64_t par_id = getTupleId(env, pdl, idc);
        if (-1 != par_id) {
            LOG_DEBUG << "tuple record has already exist, need not insert";
            return par_id;
        }
        try {
            mysqlpp::Query query = db_conn_.query();
            {
                mysqlpp::Transaction trans(db_conn_,
                                           mysqlpp::Transaction::serializable,
                                           mysqlpp::Transaction::session);
                query << "INSERT into `tuple`(pdl, env, idc) VALUES(%0q, %1q, %2q)";
                query.parse();
                query.execute(pdl, env, idc);

                query.reset();
                query << "SELECT LAST_INSERT_ID()";
                mysqlpp::StoreQueryResult res = query.store();
                trans.commit();
                if (!res.empty()) {
                    par_id = res[0][0];
                }
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return par_id;
    }

    int Storage::getIPAffiliation(const string &IP) {
        int id = -1;
        try {
            mysqlpp::Query query = db_conn_.query(
                    "SELECT tuple_id FROM `ip_affiliation` WHERE ip=%0q limit 1");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(IP);
            if (!res.empty()) {
                id = res[0]["tuple_id"];
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return id;
    }

    void Storage::deleteExpiredIP(int hour) {
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "DELETE FROM `ip_affiliation` WHERE timestamp < %0 - INTERVAL " << hour << " HOUR";
            query.parse();
            query.execute(mysqlpp::DateTime::now());
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
    }

    int Storage::setIPAffiliation(const vector<ip_host> &ip_hostname, int tuple_id) {
        //batch update ip hostname.
        if (ip_hostname.empty()) {
            LOG_DEBUG << "no need to update ip info";
            return 0;
        }
        string str_values;
        str_values.reserve(36*ip_hostname.size());
        mysqlpp::DateTime time_now(time(NULL));
        for (std::vector<ip_host>::const_iterator itr = ip_hostname.begin();
             ip_hostname.end() != itr; ++itr) {
            str_values += "('" + itr->ip_ + "','" + itr->host_ + "','" + (char)('0'+itr->rank_) + "',"
                          + boost::lexical_cast<string>(tuple_id)+",'" + time_now.str() + "'),";
        }
        //instead last ',' in str_values.
        str_values[str_values.size()-1] = ' ';

        try {
            mysqlpp::Query query = db_conn_.query();
            //mysql batch update.
            query << "INSERT into `ip_affiliation` VALUES " << str_values
                  << "ON DUPLICATE KEY UPDATE hostname=VALUES(hostname), rank=VALUES(rank),"
                  << " tuple_id=VALUES(tuple_id), timestamp=VALUES(timestamp)";
            query.parse();
            query.execute();
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return 0;
    }

    int64_t Storage::getPluginId(const string& md5) {
        try{
            mysqlpp::Query query = db_conn_.query(
                    "SELECT id FROM `cplugin` WHERE md5=%0q limit 1");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(md5);
            if (!res.empty()) {
                return res[0]["id"];
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int64_t Storage::setPlugin(const string& name, const string& md5,
                               const string& version, const string& lib_content) {
        try {
            mysqlpp::Query query = db_conn_.query();
            {
                mysqlpp::Transaction trans(db_conn_,
                                           mysqlpp::Transaction::serializable,
                                           mysqlpp::Transaction::session);
                query << "INSERT into `cplugin`(plugin_name, md5, plugin_version, lib_content, timestamp) "
                      << "VALUES(%0q, %1q, %2q, %3q, %4) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), timestamp=%4";
                query.parse();
                query.execute(name, md5, version, lib_content, mysqlpp::DateTime::now());

                query.reset();
                query << "SELECT LAST_INSERT_ID()";
                mysqlpp::StoreQueryResult res = query.store();
                trans.commit();
                if (!res.empty()) {
                    return res[0][0];
                }
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int Storage::getUpdateInfo(int rule_id, int *plugin_id, int *tuple_id,
                               int *op_type, string *plugin_name, string *md5) {
        if (NULL == op_type || NULL == md5 || NULL == plugin_name) {
            LOG_ERROR << "input point is NULL, please check input param.";
            return -1;
        }
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "SELECT rule.plugin_id, rule.tuple_id, rule.op_type, cplugin.plugin_name, cplugin.md5 "
                  << "FROM `rule` JOIN `cplugin` ON rule.id = " << rule_id
                  << " AND rule.plugin_id = cplugin.id LIMIT 1";
            mysqlpp::StoreQueryResult res = query.store();
            if (!res.empty()) {
                *plugin_id = res[0][0];
                *tuple_id = res[0][1];
                *op_type = res[0][2];
                plugin_name->assign(res[0][3].data(), res[0][3].length());
                md5->assign(res[0][4].data(), res[0][4].length());
            }
            return 0;
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int64_t Storage::setRule(int plugin_id, int tuple_id, int task_id,
                             const string &plugin_name, int op_type) {
        try {
            mysqlpp::Query query = db_conn_.query();
            {
                mysqlpp::Transaction trans(db_conn_,
                                           mysqlpp::Transaction::serializable,
                                           mysqlpp::Transaction::session);
                query << "INSERT into `rule`(plugin_id, tuple_id, task_id, plugin_name, op_type, timestamp) VALUES(%0, %1, %2, %3q, %4, %5) "
                      << "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), task_id=%2, op_type=%4, timestamp=%5";
                query.parse();
                query.execute(plugin_id, tuple_id, task_id, plugin_name, op_type, mysqlpp::DateTime::now());

                query.reset();
                query << "SELECT LAST_INSERT_ID()";
                query.parse();
                mysqlpp::StoreQueryResult res = query.store(plugin_id, tuple_id);
                trans.commit();
                if (!res.empty()) {
                    return res[0][0];
                }
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int Storage::SetIPUpgrade(const std::string &ip,
                              const std::string &plugin_name,
                              int task_id,
                              int plugin_id,
                              int op_type) {
        int res = -1;
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "INSERT into `ip_upgrade`(ip, plugin_id, task_id, plugin_name, op_type, timestamp)"
                  <<" VALUES(%0q, %1, %2, %3q, %4, %5) "
                  << "ON DUPLICATE KEY UPDATE plugin_id=%1, task_id=%2, op_type=%4, timestamp=%5";
            query.parse();
            query.execute(ip, plugin_id, task_id,  plugin_name, op_type, mysqlpp::DateTime::now());
            res = 0;
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return res;
    }

    int Storage::getIPtime(const std::string &ip,
                           const std::string &plugin_name,
                           time_t *ip_time) {
        int ret = -1;
        try {
            mysqlpp::Query query = db_conn_.query("SELECT timestamp FROM `ip_upgrade` WHERE ip=%0q AND plugin_name=%1q LIMIT 1");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(ip, plugin_name);
            if (!res.empty()) {
                *ip_time = res[0][0];
                ret = 0;
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return ret;
    }

    int Storage::setReport(int task_id, int plugin_id, const string& ip,
                           plugin_status flag, const string& err) {
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "INSERT into `report`(history_id, plugin_id, ip, flag, timestamp, err_message) "
                  << "VALUES(%0, %1, %2q, %3, %4, %5q) ON DUPLICATE KEY UPDATE flag=%3, timestamp=%4, err_message=%5q";
            query.parse();
            query.execute(task_id, plugin_id, ip, flag, mysqlpp::DateTime::now(), err);
            return 0;
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int Storage::getReport(int task_id, const std::string& ip) {
        try {
            mysqlpp::Query query = db_conn_.query("SELECT flag FROM `report`"
                                                  " WHERE history_id=%0 AND ip=%1q LIMIT 1");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(task_id, ip);
            if (!res.empty()) {
                return res[0][0];
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return UPDATING;
    }

    bool Storage::isTaskCompleted(int task_id) {
        bool ret = false;
        try {
            mysqlpp::Query query = db_conn_.query("SELECT EXISTS(SELECT 1 FROM `report` WHERE"
                                                  " history_id=%0 AND flag<>%1 LIMIT 1)");
            query.parse();
            mysqlpp::StoreQueryResult res = query.store(task_id, SUCCESS);
            ret = !res[0][0];
            if (ret) {
                query.reset();
                query << "UPDATE `history` SET flag=%0 WHERE id=%1";
                query.parse();
                query.execute(PROCESSED, task_id);
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return ret;
    }

    int Storage::findLeftPlugin(const string &plugin_name,
                                int tuple_id,
                                int *p_plugin_id,
                                int *p_task_id,
                                string *p_md5){
        int ret_code = -1;
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "SELECT plugin_id, task_id FROM `rule` WHERE tuple_id = %0 AND plugin_name = %1q ORDER BY timestamp desc limit 1";
            query.parse();
            mysqlpp::StoreQueryResult res1 = query.store(tuple_id, plugin_name);
            if (!res1.empty()) {
                *p_plugin_id = res1[0][0];
                *p_task_id = res1[0][1];
                query.reset();
                query << "SELECT md5 FROM `cplugin` where id = " << *p_plugin_id;
                mysqlpp::StoreQueryResult res2 = query.store();
                if (!res2.empty()) {
                    p_md5->assign(res2[0][0].data(), res2[0][0].length());
                    ret_code = 0;
                }
            }

        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return ret_code;
    }

    int Storage::checkPlugin(const string &ip,
                             const string &plugin_name,
                             int *p_plugin_id,
                             int *p_task_id,
                             int *p_type,
                             string *p_md5){
        int ret_code = -1;
        try {
            mysqlpp::Query query = db_conn_.query();
            mysqlpp::StoreQueryResult res1, res2;
            mysqlpp::DateTime rule_time(1, 0, 0, 0, 0, 0), ip_time(1, 0, 0, 0, 0, 0);
            {
                mysqlpp::Transaction trans(db_conn_,
                                           mysqlpp::Transaction::serializable,
                                           mysqlpp::Transaction::session);
                query << "SELECT rule.plugin_id, rule.task_id, rule.op_type, cplugin.md5, rule.timestamp FROM `rule` INNER JOIN `ip_affiliation` ";
                query << "ON ip_affiliation.ip = %0q AND rule.plugin_name = %1q AND ip_affiliation.tuple_id = rule.tuple_id ";
                query << "INNER JOIN cplugin ON cplugin.id = rule.plugin_id ORDER BY rule.timestamp desc limit 1";
                query.parse();
                res1 = query.store(ip, plugin_name);
                if (!res1.empty()) {
                    rule_time = res1[0][4];
                }

                query.reset();
                query << "SELECT ip_upgrade.plugin_id, ip_upgrade.task_id, ip_upgrade.op_type, cplugin.md5, ip_upgrade.timestamp FROM `ip_upgrade` INNER JOIN";
                query << " `cplugin` ON ip_upgrade.ip = %0q AND ip_upgrade.plugin_name = %1q AND cplugin.id = ip_upgrade.plugin_id limit 1";
                query.parse();
                res2 = query.store(ip, plugin_name);
                trans.commit();
                if (!res2.empty()) {
                    ip_time = res2[0][4];
                }
		    ret_code = 0;
                if (rule_time.compare(ip_time) > 0) {
                    *p_plugin_id = res1[0][0];
                    *p_task_id = res1[0][1];
                    *p_type = res1[0][2];
                    p_md5->assign(res1[0][3].data(), res1[0][3].length());
                } else if (rule_time.compare(ip_time) < 0) {
                    *p_plugin_id = res2[0][0];
                    *p_task_id = res2[0][1];
                    *p_type = res2[0][2];
                    p_md5->assign(res2[0][3].data(), res2[0][3].length());
                } else {
		            p_md5->assign("");
                    ret_code = -1;
		        }
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return ret_code;
    }

    bool Storage::isFetchOpsNow() {
        try{
            mysqlpp::Query query = db_conn_.query();
            query << "SELECT (SELECT COUNT(*) FROM `departure`) AS dCnt,";
            query << " (SELECT COUNT(*) FROM `ip_affiliation`) AS hCnt,";
            query << " (SELECT COUNT(*) FROM `tuple`) AS pCnt FROM dual";
            mysqlpp::StoreQueryResult res = query.store();
            if (res.empty() || 0 == int(res[0][0]) ||
                0 == int(res[0][1]) || 0 == int(res[0][2])) {
                return true;
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return false;
    }

    int Storage::updatePluginHealth(const string& ip,
                                    const string& p_name,
                                    int16_t status) {
        try{
            mysqlpp::Query query = db_conn_.query();
            query << "INSERT into `plugin_alive`(ip, plugin_name, status, last_update) "
                  << "VALUES(%0q, %1q, %2, %3) ON DUPLICATE KEY UPDATE status=%2, last_update=%3";
            query.parse();
            query.execute(ip, p_name, status, mysqlpp::DateTime::now());
            return 0;
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return -1;
    }

    int Storage::getAllTupleId(vector<int>& tuple_list) {
        int ret = -1;
        try {
            mysqlpp::Query query = db_conn_.query();
            query << "SELECT distinct tuple_id FROM rule";
            mysqlpp::StoreQueryResult res = query.store();
            if (!res.empty()) {
                for (size_t i = 0; i != res.num_rows(); i++) {
                    tuple_list.push_back(res[i][0]);
                }
                ret = 0;
            }
        }catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
        }
        return ret;
    }

    int Storage::getPluginsByTuple(int tuple_id, int filter_type,
                                   std::vector<std::string>& p_names){
        mysqlpp::Query query = db_conn_.query();
        query << "SELECT distinct plugin_name FROM rule WHERE tuple_id = "
              << tuple_id << " AND op_type <> " << filter_type;
        return queryList(query, p_names);
    }

    int Storage::queryList(mysqlpp::Query &query, vector<string> &res) {
        try {
            mysqlpp::StoreQueryResult sqr = query.store();
            res.reserve(sqr.num_rows());
            for (size_t i = 0; i != sqr.num_rows(); i++) {
                res.push_back(string(sqr[i][0].data(), sqr[i][0].length()));
            }
        } catch (const mysqlpp::BadQuery& tx) {
            LOG_ERROR << "Query error: " << tx.what();
            return -1;
        } catch (const mysqlpp::BadConversion& tx) {
            LOG_ERROR << "Conversion error: " << tx.what() <<
                      "\tretrieved data size: " << tx.retrieved <<
                      ", actual size: " << tx.actual_size;
            return -1;

        } catch (const mysqlpp::Exception& tx) {
            LOG_ERROR << "Error: " << tx.what();
            return -1;
        }
        return 0;
    }
}
