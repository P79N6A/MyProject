#include <string>
#include <vector>
#include <map>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Timestamp.h>
//#include <octoidl/SGAgent.h>
#include "controlServerManager.h"
#include <glog/logging.h>
#include "util/sg_agent.h"


namespace cplugin {

    bool CMCP(const cplugin_sgagent::SGService &a,const cplugin_sgagent::SGService &b)
    {
        return a.fweight >b.fweight;
    }

    void ControlManager::updateForFail(){

        vector<cplugin_sgagent::SGService>::iterator it = vec_control_server_sgservice_old_.begin();
        for(; it != vec_control_server_sgservice_old_.end(); it++){
            if((*it).ip == current_ip_ && (*it).port == current_port_){
                vec_control_server_sgservice_old_.erase(it);
                current_ip_ = "";
                current_port_ = 0;
                break;
            }
        }

        int32_t  now_size = vec_control_server_sgservice_old_.size();

        if(now_size > 0){
            srand((int)time(0));
            int32_t  index = rand()%now_size;


            current_ip_ = vec_control_server_sgservice_old_[index].ip;
            current_port_ = vec_control_server_sgservice_old_[index].port;

            manager_->InitControlServerLongConn(current_ip_, current_port_);
        }
    }

        void ControlManager::update(const vector<cplugin_sgagent::SGService>& vec_sgservice){

            //同机房，是否存活过滤
            filterIDC(vec_sgservice);

            //当列表变动时做新连接的建立//
            if(!manager_->is_shortConn()){

                if(current_ip_.empty() && current_port_ == 0){

                    if(vec_control_server_sgservice_.empty() ) {
                        LOG(ERROR) << "empty control Server list: " ;
                        return ;
                    }

                    int32_t  size = vec_control_server_sgservice_.size();
                    srand((int)time(0));
                    int32_t  index = rand()%size;

                    current_ip_ = vec_control_server_sgservice_[index].ip;
                    current_port_ = vec_control_server_sgservice_[index].port;

                    vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                    manager_->InitControlServerLongConn(current_ip_, current_port_);
                    return;
                }

                vector<cplugin_sgagent::SGService> add;
                vector<cplugin_sgagent::SGService> remove;
                UpdateType type = getType(vec_control_server_sgservice_ , vec_control_server_sgservice_old_, add, remove);

                switch (type){
                    case  LIST_EQUAL: {
                        //一样，则不动
                        LOG(INFO) << "LIST_EQUAL";
                        break;
                    }
                    case  LIST_INCREASE: {
                        //有增的，则挑出一部分随机迁到新增机器上面
                        int32_t  now_size = vec_control_server_sgservice_.size();
                        int32_t  add_size = add.size();

                        srand((int)time(0));
                        int32_t  pre_index = rand()%now_size;

                        vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                        if(pre_index >= now_size - add_size){
                            int32_t  now_index = rand()%add_size;
                            current_ip_ = add[now_index].ip;
                            current_port_ = add[now_index].port;

                            manager_->InitControlServerLongConn(current_ip_, current_port_);
                        }


                        LOG(INFO) << "LIST_INCREASE";
                        break;
                    }
                    case  LIST_DECREASE: {
                        //有减的，自己没有命中，则不动
                        vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                        LOG(INFO) << "LIST_DECREASE";
                        break;
                    }
                    case  LIST_DECREASE_ON: {
                        //有减的，自己在被减的机器上面，把自己迁移到其他机器上面
                        int32_t  now_size = vec_control_server_sgservice_.size();
                        vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                        if(now_size > 0){
                            srand((int)time(0));
                            int32_t  index = rand()%now_size;

                            current_ip_ = vec_control_server_sgservice_[index].ip;
                            current_port_ = vec_control_server_sgservice_[index].port;

                            manager_->InitControlServerLongConn(current_ip_, current_port_);
                        }else{
                            current_ip_ = "";
                            current_port_ = 0;
                            manager_->InitControlServerLongConn(current_ip_, current_port_);
                            LOG(ERROR) << "empty for vec_control_server_sgservice_";

                        }
                        LOG(INFO) << "LIST_DECREASE_ON";
                        break;
                    }
                    case  LIST_BOTH: {
                        //既有增的又有减的，自己没有在被减的机器上面，随机把一部分机器迁移到新的机器上面
                        int32_t  now_size = vec_control_server_sgservice_.size();
                        int32_t  add_size = add.size();

                        srand((int)time(0));
                        int32_t  pre_index = rand()%now_size;

                        vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                        if(pre_index >= now_size - add_size){
                            int32_t  now_index = rand()%add_size;
                            current_ip_ = add[now_index].ip;
                            current_port_ = add[now_index].port;

                            manager_->InitControlServerLongConn(current_ip_, current_port_);
                        }

                        LOG(INFO) << "LIST_BOTH";
                        break;
                    }
                    case  LIST_BOTH_ON: {
                        //既有增的又有减的，并且自己的长连接在减的机器上面，则把自己迁到新的机器上面
                        int32_t  add_size = add.size();
                        srand((int)time(0));

                        int32_t  now_index = rand()%add_size;
                        current_ip_ = add[now_index].ip;
                        current_port_ = add[now_index].port;

                        vec_control_server_sgservice_old_ = vec_control_server_sgservice_;
                        manager_->InitControlServerLongConn(current_ip_, current_port_);

                        LOG(INFO) << "LIST_BOTH_ON";
                        break;
                    }
                    default:{
                        LOG(ERROR) << "default";
                        break;
                    }
                }

            }else{
                manager_->updateControlServerList(vec_control_server_sgservice_);
            }

        }


        void ControlManager::filterIDC(const vector<cplugin_sgagent::SGService>& vec_sgservice){

            vector<cplugin_sgagent::SGService> temp;
            vec_control_server_sgservice_.clear();
            LOG(INFO)  << "service getsvrlist size " << vec_sgservice.size();
            vector <cplugin_sgagent::SGService>::const_iterator it = vec_sgservice.begin();
            for(; it != vec_sgservice.end(); it++){
                if((*it).status == 2){
                    vec_control_server_sgservice_.push_back(*it);
                }
                //LOG(INFO)  << CpluginSgagent::SGService2String(*it);
            }


            if(vec_control_server_sgservice_.size() > 0){

                temp.swap(vec_control_server_sgservice_);
                vec_control_server_sgservice_.clear();

                sort(temp.begin(),temp.end(),CMCP);

                //同机房优先
                vector <cplugin_sgagent::SGService>::iterator it = temp.begin();
                double  max_fweight = it->fweight;
                vec_control_server_sgservice_.push_back(*it);
                for(it++; it != temp.end(); it++){
                    if(max_fweight < it->fweight*100){
                        vec_control_server_sgservice_.push_back(*it);
                    }
                }
            }

        }


        UpdateType ControlManager::getType(const vector<cplugin_sgagent::SGService>& now, const vector<cplugin_sgagent::SGService>& old,
                           vector<cplugin_sgagent::SGService>& add,  vector<cplugin_sgagent::SGService>& remove){

            bool hit = false;
            //loop for add
            do{

                vector<cplugin_sgagent::SGService>::const_iterator it = now.begin();
                for(; it != now.end(); it++){
                    bool flag = false;
                    vector<cplugin_sgagent::SGService>::const_iterator it_old = old.begin();
                    for(; it_old != old.end(); it_old++){
                        if((*it_old).ip == (*it).ip && (*it_old).port == (*it).port){
                            flag = true;
                            break;
                        }
                    }

                    if(flag == false){
                        add.push_back(*it);
                    }

                }

            }while(0);


            //loop for remove
            do{

                vector<cplugin_sgagent::SGService>::const_iterator it_old = old.begin();
                for(; it_old != old.end(); it_old++){
                    bool flag = false;
                    vector<cplugin_sgagent::SGService>::const_iterator it = now.begin();
                    for(; it != now.end(); it++){
                        if((*it_old).ip == (*it).ip && (*it_old).port == (*it).port){
                            flag = true;
                            break;
                        }
                    }

                    if(flag == false){
                        remove.push_back(*it_old);

                        if((*it_old).ip == current_ip_ && (*it_old).port == current_port_){
                            hit = true;
                        }
                    }

                }

            }while(0);

#if 0
            LOG(INFO)  << "-------------------debug--------------------- " ;
            do{
                vector<SGService>::const_iterator it = now.begin();
                LOG(INFO)  << "now---------------------- " << now.size();
                for(; it != now.end(); it++){
                    LOG(INFO)  << " " << CpluginSgagent::SGService2String(*it);
                }
            }while(0);

            do{
                vector<SGService>::const_iterator it = old.begin();
                LOG(INFO)  << "old---------------------- " << old.size();
                for(; it != old.end(); it++){
                    LOG(INFO)  << " " << CpluginSgagent::SGService2String(*it);
                }
            }while(0);

#endif

            do{
                vector<cplugin_sgagent::SGService>::const_iterator it = add.begin();
                //LOG(INFO)  << "add---------------------- " << add.size();
                for(; it != add.end(); it++){
                    LOG(INFO)  << "add " << CpluginSgagent::SGService2String(*it);
                }
            }while(0);


            do{
                vector<cplugin_sgagent::SGService>::const_iterator it = remove.begin();
                //LOG(INFO)  << "remove---------------------- " << remove.size();
                for(; it != remove.end(); it++){
                    LOG(INFO)  << "remove  " << CpluginSgagent::SGService2String(*it);
                }
            }while(0);


            if(add.empty()){
                if(remove.empty()){
                    return LIST_EQUAL;
                }else{
                    if(hit){
                        return LIST_DECREASE_ON;
                    }else{
                        return LIST_DECREASE;
                    }
                }

            }else{
                if(remove.empty()){
                    return LIST_INCREASE;
                }else{
                    if(hit){
                        return LIST_BOTH_ON;
                    }else{
                        return LIST_BOTH;
                    }
                }
            }
        }


} // namespace cplugin


