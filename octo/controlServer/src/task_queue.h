//
// Created by Xiang Zhang on 2017/9/29.
//

#ifndef CONTROLSERVER_TASK_QUEUE_H
#define CONTROLSERVER_TASK_QUEUE_H

#include <boost/ptr_container/ptr_vector.hpp>
#include <boost/unordered_map.hpp>

#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/base/Logging.h>
#include <muduo/base/BlockingQueue.h>

#include "storage.h"

namespace Controller {

struct task{
    int rule_id_;
    int his_id_;
    srv_rank rank;
    task(int rule, int his)
            :rule_id_(rule), his_id_(his), rank(NON_CORE_SRV){
    }

    void setRank(srv_rank r) {
        rank = r;
    }
};

class task_queue {
public:
    task_queue() {

    }

    void init(const std::string& db_name,
              const std::string& db_server,
              const std::string& db_user,
              const std::string& db_pass,
              int db_port, int numThreads);

    void produce(int his_id, int rule_id) {
        queue_.put(task(his_id, rule_id));
    }

    void joinAll();

    void initTaskStage(int task_id);

private:
    const static int8_t NON_CORE_STAGE_;
    const static int STAGE_FLAG_;
    const static int8_t CORE_STAGE_;
    void threadFunc(const std::string& db_name,
                    const std::string& db_server,
                    const std::string& db_user,
                    const std::string& db_pass,
                    int db_port);

    muduo::BlockingQueue<task> queue_;
    int numThreads_;
    boost::ptr_vector<muduo::Thread> threads_;
    boost::unordered_map<int, int8_t > task_stage_;
};

}


#endif //CONTROLSERVER_TASK_QUEUE_H
