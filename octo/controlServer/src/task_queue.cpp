//
// Created by Xiang Zhang on 2017/9/29.
//
#include <cstdio>

#include <boost/make_shared.hpp>
#include <boost/bind.hpp>

#include "common.h"
#include "task_queue.h"

using namespace std;
using namespace muduo;
using namespace Controller;

const int8_t task_queue::NON_CORE_STAGE_ = 0;
const int8_t task_queue::CORE_STAGE_ = 1;
const int task_queue::STAGE_FLAG_ = -111;

void task_queue::init(const string& db_name,
                      const string& db_server,
                      const string& db_user,
                      const string& db_pass,
                      int db_port, int num = 2) {
    numThreads_ = num;
    for (int i = 0; i < numThreads_; ++i) {
        char name[32];
        snprintf(name, sizeof(name), "blocking queue consumer thread %d", i);
        threads_.push_back(new muduo::Thread(
                boost::bind(&task_queue::threadFunc, this, db_name, db_server, db_user, db_pass, db_port),
                muduo::string(name)));
    }
    for_each(threads_.begin(), threads_.end(), boost::bind(&muduo::Thread::start, _1));
}

void task_queue::joinAll() {
    for (size_t i = 0; i < threads_.size(); ++i) {
        queue_.put(task(-100, -100));
    }
    for_each(threads_.begin(), threads_.end(), boost::bind(&muduo::Thread::join, _1));
}

void task_queue::initTaskStage(int task_id) {
    queue_.put(task(STAGE_FLAG_, task_id));
    task_stage_[task_id] = NON_CORE_STAGE_;
}

void task_queue::threadFunc(const string& db_name,
                            const string& db_server,
                            const string& db_user,
                            const string& db_pass,
                            int db_port) {
    bool running = true;
    //work thread using itself db_conn
    Storage storage(db_name, db_server, db_user, db_pass, db_port);
    int retry_num = 0, ret = -1;
    while (3 > retry_num++ && -1 == ret) {
        ret = storage.init();
    }

    if (-1 == ret) {
        LOG_ERROR << "Init DB connection failed in blocking queue thread, after retry 3 times.";
        return;
    }

    int plugin_id = -1, partition = -1, op_type = -1;
    string name, md5;
    while (running) {
        task t(queue_.take());
        //LOG_DEBUG << "begin to handle task: " << t.rule_id_ << "; " << t.his_id_ << "; " << t.rank;
        running = (t.rule_id_ != -100 || t.his_id_ != -100);

        //遇到哨兵task时，将该任务flag置为开始处理核心服务机器列表
        if (STAGE_FLAG_ == t.rule_id_ && t.his_id_ > 0) {
            task_stage_[t.his_id_] = CORE_STAGE_;
            continue;
        }
        //将该任务还有未处理完的非核心服务时，将核心任务放回队列。
        if (CORE_SRV == t.rank && NON_CORE_STAGE_ == task_stage_[t.his_id_]) {
            //set core task back to blocking queue
            queue_.put(t);
            continue;
        }

        if (-1 == storage.getUpdateInfo(t.rule_id_, &plugin_id, &partition, &op_type, &name, &md5)) {
            LOG_WARN << "handle task failed, rule_id: " << t.rule_id_ << ", his_id: " << t.his_id_;
            continue;
        }
        LOG_DEBUG << "update info: " << plugin_id << "; " << partition << "; " << op_type << "; " << name << "; " << md5;

        vector<string> ip_list;
        if (-1 == storage.getIPList(partition, t.rank, ip_list)) {
            LOG_WARN << "get ip list failed, by partition: " << partition;
            continue;
        }
        //send rpc
        for (vector<string>::const_iterator itr = ip_list.begin();
             ip_list.end() != itr; ++itr) {
            LOG_DEBUG << *itr;
            if (0 == sendCommand(name, md5, *itr, op_type, plugin_id, t.his_id_, 1)) {
                storage.setReport(t.his_id_, plugin_id, *itr, UPDATING, "");
            } else {
                storage.setReport(t.his_id_, plugin_id, *itr, SEND_RPC_FAIL, "");
            }
        }

        //TODO: maybe using two blocking queue is good. handle core server
        if (NON_CORE_SRV == t.rank) {
            t.setRank(CORE_SRV);
            queue_.put(t);
        }
    }
}
