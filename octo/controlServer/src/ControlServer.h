#ifndef __CONTROL_SERVER__H
#define __CONTROL_SERVER__H

#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/base/Logging.h>
#include <muduo/base/Mutex.h>

#include <boost/make_shared.hpp>
#include <boost/bind.hpp>

#include "ControllerService.h"
#include "condition_latch.h"
#include "common.h"
#include "storage.h"
#include "task_queue.h"
#include "thread_pool.h"

namespace Controller{

class ControllerServer: virtual public ControllerServiceIf {
public:
    ControllerServer();

    /*ControllerServer(const std::string& db_name, const std::string& db_server,
                     const std::string& db_user, const std::string& db_pass, int db_port = 3306);*/

    //thrift interface
    int32_t installPlugin(const Plugin& p, const std::vector<std::string> & ip_addr_list);

    int32_t startPlugin(const Plugin& p, const std::vector<std::string> & ip_addr_list);

    int32_t stopPlugin(const Plugin& p, const std::vector<std::string> & ip_addr_list);

    int32_t restartPlugin(const Plugin& p, const std::vector<std::string> & ip_addr_list);

    int32_t handlePlugin(const Operation::type op, const Plugin& p, const std::string& ip_addr);

    int32_t handlePluginList(const Operation::type op, const Plugin& p, const std::vector<std::string> & ip_addr_list);

    int32_t handlePluginUnion(const Operation::type op, const Plugin& p, const Department& d,
                              const Location& l, const std::string& env);

    void checkPerformance(std::vector<Performance> & _return, const Plugin& p, const std::vector<std::string> & ip_addr_list){
    }

    void getVersionList(std::vector<PluginVersion> & _return, const Plugin& p, const std::vector<std::string> & ip_addr_list){
    }

    void getVersionUnion(std::vector<PluginVersion> & _return, const Plugin& p, const Department& d, const Location& l){
    }

    int32_t reportVersion(const std::string& ip_addr, const int32_t plugin_id, const int32_t task_id, const std::string& err_message);
    int32_t reportCpluginInfo(const std::string& ip_addr, const std::string& hostname, const Location& l, const CPluginInfo& info);
    int32_t regularCheckPlugin(const std::string& ip_addr, const std::vector<Plugin> & plugin_list);
    int32_t reportHealth(const std::string& ip_addr, const std::vector<PluginHealth>& health_list);
    void reportMoniterInfo(MoniterResponse& _return, const MoniterRequest& request);

private:
    static const int DB_RETRY;
    //慢队列
    muduo::net::EventLoop *p_event_loop_;
    boost::shared_ptr<muduo::net::EventLoopThread> sp_thread_;
    //规则匹配任务队列
    task_queue queue_;
    //快队列
    thread_pool pool_;

    std::string db_name_;
    std::string db_server_;
    std::string db_user_;
    std::string db_pass_;
    int         db_port_;
    int         num_fast_worket_;
    int         num_bq_worker_;

    typedef boost::unordered_map<std::string, int> Map;
    typedef boost::shared_ptr<Map> MapPtr;
    MapPtr alive_cache_;
    mutable muduo::MutexLock mutex_;

    typedef boost::unordered_map<int, std::vector<std::string> > PluginMap;
    typedef boost::shared_ptr<PluginMap> PluginMapPtr;
    PluginMapPtr plugin_cache_;
    mutable muduo::MutexLock plugin_mutex_;
private:
    MapPtr getCache() const {
        muduo::MutexLockGuard lock(mutex_);
        return alive_cache_;
    }

    PluginMapPtr getPluginCache() const {
        muduo::MutexLockGuard lock(plugin_mutex_);
        return plugin_cache_;
    }
    void init();

    void regularFetchOps();

    void initRegularJob();

    void doPluginCache();

    void doRegularFetch();

    void initStorage(muduo::net::EventLoop *p_event_loop);

    void initThreadPool(int thread_num);


    void processor(const Plugin &p, const std::vector<std::string> &ip_addr_list,
                   const Operation::type op);

    void processor(const Plugin &p, const Department &d,
                   const Location &l, const std::string& env,
                   const Operation::type op);

    void reportProcessor(const std::string& ip_addr, const int32_t plugin_id,
                         const int32_t task_id, const std::string& err_message);

    void agentCheckProcessor(const std::string& ip_addr,
                             const std::vector<Plugin> &plugin_list);

    void healthProcessor(const std::string& ip,
                         const std::string& plugin_name,
                         int16_t status);

    int insertPlugin(const Plugin &p, boost::shared_ptr<Storage> &sp_storage);

    int recordHistory(int plugin_id, const Operation::type op,
                      const std::string& content,
                      boost::shared_ptr<Storage> &sp_storage);
};

};

#endif
