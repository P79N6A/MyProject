#include <vector>

#include <boost/any.hpp>
#include <boost/algorithm/string.hpp>

#include "ops_fetcher.h"
#include "ControlServer.h"
#include "controller_types.h"

namespace Controller {
    using namespace std;

    const int ControllerServer::DB_RETRY = 3;

    ControllerServer::ControllerServer()
            : queue_(),
              pool_(p_event_loop_),
              alive_cache_(new Map),
              plugin_cache_(new PluginMap){
        readCfg(&db_name_, &db_user_, &db_server_, &db_pass_, &db_port_,
                &num_fast_worket_, &num_bq_worker_);
        LOG_INFO << "DB_NAME: " << db_name_
                 << "DB_USER: " << db_user_
                 << "DB_SRV_IP: "<< db_server_
                 << "DB_Pass: " << db_pass_
                 << "DB_Port: "<<db_port_
                 << "num_fast_work: "<< num_fast_worket_
                 << "num_blocking_queue: "<< num_bq_worker_;
        init();
    }

    void ControllerServer::init() {
        sp_thread_ = boost::make_shared<muduo::net::EventLoopThread>();
        p_event_loop_ = sp_thread_->startLoop();
        initStorage(p_event_loop_);

        //start fast thread pool
        initThreadPool(num_fast_worket_);
        //init blocking queue conn
        queue_.init(db_name_, db_server_, db_user_, db_pass_, db_port_, num_bq_worker_);
        initRegularJob();
        LOG_INFO << "Init work thread success.";
    }

    void ControllerServer::initRegularJob() {
        if (checkMaster("com.sankuai.inf.octo.cpluginserver", "ops_fetch_node")) {
            LOG_INFO << "this service need regular check data with ops.";
            p_event_loop_->runInLoop(boost::bind(&ControllerServer::doRegularFetch, this));
        }
        LOG_INFO << "Init plugin cache.";
        p_event_loop_->runInLoop(boost::bind(&ControllerServer::doPluginCache, this));
    }

    void ControllerServer::doRegularFetch() {
        //get ops data, emission now or 24:00 by checking db tables
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());
        if (sp_storage->isFetchOpsNow()) {
            //如果DB中没有任何数据，工作线程现在开始同步
            regularFetchOps();
        } else {
            //如何DB已经有数据，比如服务被重启拉启；这时不立即进行ops数据拉取；设置24：00定时器进行处理
            p_event_loop_->runAt(muduo::Timestamp::fromUnixTime(get_zero_clock()),
                                 boost::bind(&ControllerServer::regularFetchOps, this));
        }
    }

    void ControllerServer::initThreadPool(int num_threads) {
        pool_.start(num_threads);
        vector<muduo::net::EventLoop*> pool_threads = pool_.getAllLoops();
        for(size_t i = 0; i < pool_threads.size(); ++i) {
            initStorage(pool_threads[i]);
        }
    }

    void ControllerServer::initStorage(muduo::net::EventLoop *p_event_loop) {
        p_event_loop->setContext(
                boost::make_shared<Storage>(db_name_, db_server_, db_user_, db_pass_, db_port_));
        //if init DB connection failed, retry 3 times.
        int retry_num = 0, ret = -1;
        while (DB_RETRY > retry_num++ && 0 != ret) {
            ret = (boost::any_cast<boost::shared_ptr<Storage> >(p_event_loop->getContext()))->init();
        }
        if (0 != ret) {
            LOG_ERROR << "Init DB connection failed, after retry 3 times; return code: " << ret;
            throw 2;
        }
    }

    void ControllerServer::doPluginCache() {
        p_event_loop_->runAt(muduo::Timestamp::fromUnixTime(get_one_clock()),
                             boost::bind(&ControllerServer::doPluginCache, this));
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());

        vector<int> all_tuples;
        if ( 0 == sp_storage->getAllTupleId(all_tuples) && !all_tuples.empty()) {
            //全量更新cache, 这种更新会阻塞read线程，但是一天这个操作目前只在01：00进行
            PluginMapPtr newCache(new PluginMap());

            for (vector<int>::iterator iter = all_tuples.begin(); all_tuples.end() != iter; ++iter) {
                vector<string> tmp_plugins;
                if (0 != sp_storage->getPluginsByTuple(*iter, Operation::REMOVE, tmp_plugins)) {
                    LOG_ERROR << "Can not get plugin list of " << *iter << " tuple, in Cache update.";
                } else {
                    (*newCache)[*iter] = tmp_plugins;
                }
            }

            //swap memo cache, by shared_ptr copy on write
            if (!newCache->empty()) {
                muduo::MutexLockGuard lock(plugin_mutex_);
                plugin_cache_.swap(newCache);
            }
            LOG_INFO << "Complete to update plugin cache, size: " << plugin_cache_->size();
        }
    }

    int32_t ControllerServer::installPlugin(const Plugin& p, const std::vector<std::string> & ip_list){
        if (!p.name.empty() && !ip_list.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::processor, this, p,
                                                 ip_list, Operation::INSTALL));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::startPlugin(const Plugin& p, const std::vector<std::string> & ip_list){
        if (!p.name.empty() && !ip_list.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::processor, this, p,
                                                 ip_list, Operation::START));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::stopPlugin(const Plugin& p, const std::vector<std::string> & ip_list){
        if (!p.name.empty() && !ip_list.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::processor,
                                                 this, p, ip_list, Operation::STOP));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::restartPlugin(const Plugin& p, const std::vector<std::string> & ip_list){
        if (!p.name.empty() && !ip_list.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::processor, this, p,
                                                 ip_list, Operation::RESTART));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::handlePlugin(const Operation::type op, const Plugin& p, const string& ip_addr) {
        if (p.name.empty() || ip_addr.empty()) {
            return -1;
        }
        std::vector<std::string> tmp;
        tmp.push_back(ip_addr);
        return handlePluginList(op, p, tmp);
    }

    int32_t ControllerServer::handlePluginList(const Operation::type op, const Plugin& p, const vector<std::string> & ip_list) {
        if (!ip_list.empty() && !p.name.empty() && !p.md5.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::processor,
                                                 this, p, ip_list, op));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::handlePluginUnion(const Operation::type op,
                                                const Plugin& p,
                                                const Department& d,
                                                const Location& l,
                                                const string& env) {
        if (!p.name.empty() && !p.md5.empty()) {
            p_event_loop_->runInLoop(boost::bind(&ControllerServer::processor,
                                                 this, p, d, l, env, op));
            LOG_INFO << "fast task queue size: " << p_event_loop_->queueSize();
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::reportVersion(const string& ip_addr, const int32_t plugin_id,
                                            const int32_t task_id, const string& err_message) {
        if (!ip_addr.empty() && plugin_id >= 0 && task_id >= 0) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::reportProcessor, this,
                                                 ip_addr, plugin_id, task_id, err_message));
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::reportCpluginInfo(const string& ip_addr, const string& hostname,
                                                const Location& l, const CPluginInfo& info) {
        return -1;
    }

    void ControllerServer::reportMoniterInfo(MoniterResponse& _return, const MoniterRequest& request) {
        LOG_INFO << request.ip_addr << " reports monitor info: ";
        map<string, string>::const_iterator iter = request.agent_info.begin();
        while (request.agent_info.end() != iter) {
            LOG_INFO << iter->first << " = " << iter->second;
	    iter++;
        }
        LOG_INFO << request.ip_addr << " reports monitor complete";
        _return.ret = 0;
    }

    int32_t ControllerServer::reportHealth(const string &ip_addr,
                                           const vector<PluginHealth> &health_list) {
        if (!ip_addr.empty() && !health_list.empty()) {
            for (vector<PluginHealth>::const_iterator itr = health_list.begin();
                 itr != health_list.end(); ++itr) {
                //limiting flow
                string key = ip_addr + itr->name;
                MapPtr data = getCache();
                if (itr->name.empty() || (data->find(key) != data->end() && itr->status == (*data)[key])) {
                    LOG_DEBUG << key  << " status is not change, Need't to update DB.";
                    continue;
                }
                p_event_loop_->runInLoop(boost::bind(&ControllerServer::healthProcessor, this,
                                                    ip_addr, itr->name, itr->status));
                LOG_INFO << "slow task queue size: " << p_event_loop_->queueSize();
            }
            LOG_DEBUG << ip_addr << " report " << health_list.size() << " agents health status.";
            return 0;
        }
        return -1;
    }

    int32_t ControllerServer::regularCheckPlugin(const string& ip_addr,
                                              const vector<Plugin> &plugin_list) {
        if (!ip_addr.empty() && !plugin_list.empty()) {
            muduo::net::EventLoop *p_event_loop = pool_.getNextLoop();
            p_event_loop->runInLoop(boost::bind(&ControllerServer::agentCheckProcessor,
                                                this, ip_addr, plugin_list));
            LOG_INFO << "fast task queue size: " << p_event_loop_->queueSize();
            return 0;
        }
        return -1;
    }

    void ControllerServer::agentCheckProcessor(const string& ip_addr,
                                               const vector<Plugin> &plugin_list) {
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                    (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());
        int plugin_id = -1, task_id = -1;
        cplugin::Action::type op_type = cplugin::Action::TEST;
        string ret_md5 = "";
        cplugin::PluginAction tmp_action;
        vector<cplugin::PluginAction> ret_list;

        set<string> plugin_dic;
        for (vector<Plugin>::const_iterator itr = plugin_list.begin();
             plugin_list.end() != itr; ++itr) {
            //根据agent上报的plugin信息，以及agent ip；获取最新rule对应的plugin md5；如果md5与agent携带的md5不同，则需要更新操作。
            if (0 == sp_storage->checkPlugin(ip_addr, itr->name, &plugin_id, &task_id, reinterpret_cast<int*>(&op_type), &ret_md5)){
                if (ret_md5 != itr->md5) {
                    tmp_action.name = itr->name;
                    tmp_action.md5 = ret_md5;
                    tmp_action.op = op_type;
                    tmp_action.plugin_id = plugin_id;
                    tmp_action.task_id = task_id;
                    ret_list.push_back(tmp_action);
                } else if (SUCCESS != sp_storage->getReport(task_id, ip_addr)){
                    LOG_DEBUG << task_id << ip_addr << "flag is not Success, but agent has completed task, update DB flag";
                    sp_storage->setReport(task_id,plugin_id,ip_addr, SUCCESS, "");
                    sp_storage->isTaskCompleted(task_id);
                }
            }

            plugin_dic.insert(itr->name);
        }
        //检测是否有遗落的plugin
        int tupe_id = sp_storage->getIPAffiliation(ip_addr);
        if (tupe_id >= 0) {
            PluginMapPtr data = getPluginCache();
            if (data->find(tupe_id) != data->end()) {
                vector<string> &plugin_list = (*data)[tupe_id];
                for (vector<string>::const_iterator itr = plugin_list.begin(); itr != plugin_list.end(); itr++) {
                    if (plugin_dic.find(*itr) == plugin_dic.end() &&
                        0 == sp_storage->findLeftPlugin(*itr, tupe_id, &plugin_id, &task_id, &ret_md5)) {
                        tmp_action.name = *itr;
                        tmp_action.md5 = ret_md5;
                        tmp_action.op = cplugin::Action::INSTALL;
                        tmp_action.plugin_id = plugin_id;
                        tmp_action.task_id = task_id;
                        ret_list.push_back(tmp_action);
                    }
                }
            }
        } else {
            LOG_WARN << ip_addr << " isn't affiliated to any tuple.";
        }

	    if (!ret_list.empty()) {
          LOG_INFO << "send check plugin action list to: " << ip_addr;
          sendCommand(ip_addr, ret_list);
	    } else {
          LOG_INFO << "no candidator plugin need to change in " << ip_addr;
        }
    }

    void ControllerServer::reportProcessor(const string& ip_addr, const int32_t plugin_id,
                                           const int32_t task_id, const string& err_message) {
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());
        // 无需冗余检测，只有中控发送的指令，才会导致该接口被调用
        sp_storage->setReport(task_id, plugin_id, ip_addr, err_message.empty() ? SUCCESS : OPERATION_FAIL, err_message);
        sp_storage->isTaskCompleted(task_id);
    }

    void ControllerServer::healthProcessor(const string &ip_addr,
                                           const string &plugin_name,
                                           int16_t status) {
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());
        if (0 != sp_storage->updatePluginHealth(ip_addr, plugin_name, status)) {
            LOG_WARN << "Update plugin: " << plugin_name << " db_alive status failed.";
            return;
        }
        //update memo cache, shared_ptr copy on write
        muduo::MutexLockGuard lock(mutex_);
        if (!alive_cache_.unique()) {
            MapPtr newCache(new Map(*alive_cache_));
            LOG_WARN << "Worst case: There ara thread read cache, need copy backup.";
            alive_cache_.swap(newCache);
        }
        assert(alive_cache_.unique());
        (*alive_cache_)[ip_addr + plugin_name] = status;
    }

    void ControllerServer::processor(const Plugin &p, const vector<string> &ip_addr_list,
                                     const Operation::type op) {
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());

        int plugin_id = insertPlugin(p, sp_storage);
        int his_id = recordHistory(plugin_id, op, "handle ip list", sp_storage);
        if (-1 == his_id) {
            return;
        }

        for (vector<string>::const_iterator it = ip_addr_list.begin();
            it != ip_addr_list.end(); it++){

            //检测IP是否可用
            if (is_ipv4_addr(*it)) {
                sp_storage->SetIPUpgrade(*it, p.name, his_id, plugin_id, op);
                if (0 == sendCommand(p.name, p.md5, *it, op, plugin_id, his_id, 1)) {
	                sp_storage->setReport(his_id, plugin_id, *it, UPDATING, "");
                } else {
                    sp_storage->setReport(his_id, plugin_id, *it, SEND_RPC_FAIL, "");
                }
            }
        }

    }

    void ControllerServer::processor(const Plugin &p, const Department &d, const Location &l,
                                     const string& env, const Operation::type op) {
        //TODO: Department查mysql获得一个dpl_list，Location通过xml获得idc_list.
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());

        string his_content = "";
        vector<string> env_list, idc_list, pdl_list;
        if (env.empty()) {
            his_content += "env: all cluster list; ";
            ops_fetcher fetcher;
            if (!fetcher.initCurl()) {
            	LOG_ERROR << "init fetcher handler failed.";
            	return;
            }
            if (0 != fetcher.getCluster(env_list, "data")) {
            	LOG_ERROR << "can not fetch env_list from config file.";
            	return;
            }
        } else {
            his_content += "env: " + env + "; ";
            env_list.push_back(env);
        }

        if (d.pdl.empty()) {
            his_content += "owt: " + (d.owt.empty() ? "all" : d.owt) + ", pdl: all; ";
            int ret = -1, retry_num = 0;
            while (DB_RETRY > retry_num++ && -1 == ret) {
                ret = sp_storage->getPdl(d.owt, pdl_list);
            }
            if (-1 == ret) {
                LOG_ERROR << "get pdl list failed";
                return;
            }
        } else {
            his_content += "owt: " + d.owt + ", pdl: " + d.pdl + "; ";
            pdl_list.push_back(d.owt + "." + d.pdl);
        }

        if (l.idc.empty()) {
            his_content += "region: " + (l.region.empty() ? "all" : l.region) +
                    ", center: " + (l.center.empty() ? "all" : l.center) + ", idc:all";
            int ret = -1, retry_num = 0;
            while (DB_RETRY > retry_num++ && -1 == ret) {
                ret = sp_storage->getIdc(l.region, l.center, idc_list);
            }
            if (-1 == ret) {
                LOG_ERROR << "get idc list failed";
                return;
            }
        } else {
            his_content += "region: " + l.region + ", center: " + l.center + ", idc: " + l.idc;
            idc_list.push_back(l.idc);
        }

        int plugin_id = insertPlugin(p, sp_storage);
        int his_id = recordHistory(plugin_id, op, his_content, sp_storage);
        if (-1 == his_id) {
            return;
        }

        int tuple_id = -1, rule_id = -1;
        for (vector<string>::const_iterator env_itr = env_list.begin();
             env_list.end() != env_itr; ++env_itr) {
            for (vector<string>::const_iterator idc_itr = idc_list.begin();
                 idc_list.end() != idc_itr; ++idc_itr) {
                for (vector<string>::const_iterator pdl_itr = pdl_list.begin();
                     pdl_list.end() != pdl_itr; ++pdl_itr) {
                    tuple_id = sp_storage->getTupleId(*env_itr, *pdl_itr, *idc_itr);
                    if (-1 != tuple_id) {
                        rule_id = sp_storage->setRule(plugin_id, tuple_id, his_id, p.name, op);
                        LOG_DEBUG << "tuple id: " << tuple_id << "; rule_id: " << rule_id;
                        if (-1 != rule_id) {
                            queue_.produce(rule_id, his_id);
                        }
                    }
                }
            }
        }
        queue_.initTaskStage(his_id);
    }

    int ControllerServer::insertPlugin(const Plugin &p, boost::shared_ptr<Storage> &sp_storage) {
        int plugin_id = -1, retry_num = 0;
        while (DB_RETRY > retry_num++ && -1 == plugin_id) {
            plugin_id = sp_storage->setPlugin(p.name, p.md5, p.version);
        }
        if (DB_RETRY == retry_num) {
            LOG_ERROR << "set plugin info failed, after retry 3 times.";
        }
        return plugin_id;
    }

    int ControllerServer::recordHistory(int plugin_id, const Operation::type op,
                                        const std::string& content,
                                        boost::shared_ptr<Storage> &sp_storage) {
        //write DB, retry 3 times.
        if (-1 == plugin_id) {
            return -1;
        }
        int his_id = -1, retry_num = 0;
        while (DB_RETRY > retry_num++ && -1 == his_id) {
            his_id = sp_storage->recordHistory(plugin_id, op, PROCESSING, content);
        }
        if (-1 == his_id) {
            LOG_ERROR << "write operation history failed, after retry 3 times.";
        }
        return his_id;
    }

    void ControllerServer::regularFetchOps() {
        LOG_INFO << "regular fetch ops data begin.";
        //set next update timer.
        sleep(1);
        p_event_loop_->runAt(muduo::Timestamp::fromUnixTime(get_zero_clock()),
                             boost::bind(&ControllerServer::regularFetchOps, this));
        boost::shared_ptr<Storage> sp_storage =
                boost::any_cast<boost::shared_ptr<Storage> >(
                        (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());

        ops_fetcher fetcher;
        if (!fetcher.initCurl()) {
            LOG_ERROR << "init curl failed, can not emit fetch data from Ops.";
            return;
        }
        //fetch tuple info.
        vector<string> pdl_list, env_list, idc_list;
        if (fetcher.getPdl("pdls", pdl_list) || fetcher.getCluster(env_list, "data")
            || sp_storage->getAllIdc(idc_list)) {
            LOG_ERROR << "get pdl list failed or get cluster list failed or get idc list failed, can not update tuple info.";
            return;
        }
        //insert pdls list into db.
        sp_storage->setPdlList(pdl_list);
        map<string, vector<ip_host> > idc_map_ip_list;
        //cartesian product: (env, pdl, idc).
        for (vector<string>::const_iterator cluster_itr = env_list.begin();
             env_list.end() != cluster_itr; ++cluster_itr) {
            for (size_t i = 0; i != pdl_list.size(); ++i) {
                //fetch ip list from ops.
                idc_map_ip_list.clear();
                vector<string> business;
                //string tmp_str = *pdl_itr;
                boost::trim(pdl_list[i]);
                boost::split(business, pdl_list[i], boost::is_any_of("."), boost::token_compress_on);
                if (3 != business.size()) {
                    LOG_WARN << "pdl size is " << business.size() << ", can not handle it, continue next";
                    continue;
                }

                if (0 != fetcher.getIpList(business[0], business[1], business[2], *cluster_itr, idc_map_ip_list)) {
                    LOG_WARN << "fetch ip list from ops failed, by pdl: " << pdl_list[i] << " And env: " << *cluster_itr;
		    continue;
                }

                int tuple_id = -1;
                //TODO: ops need provide batch interface
                for (map<string, vector<ip_host> >::const_iterator idc_itr = idc_map_ip_list.begin();
                     idc_map_ip_list.end() != idc_itr; ++idc_itr) {
                    tuple_id = sp_storage->setTuple(*cluster_itr, pdl_list[i], idc_itr->first);
                    if (tuple_id != -1) {
                        sp_storage->setIPAffiliation(idc_itr->second, tuple_id);
                    }
                }
            }
        }
        LOG_INFO << "regular fetch ops data over.";
        LOG_INFO << "begin to clean Expired ops data.";
        sp_storage->deleteExpiredIP(10);
        LOG_INFO << "end clean Expired ops data.";
    }
};
