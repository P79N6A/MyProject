#ifndef RETRY_MANAGER_H_
#define RETRY_MANAGER_H_

#include <string>
#include <vector>
#include <map>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Timestamp.h>
#include <muduo/base/Timestamp.h>
#include <glog/logging.h>

using namespace muduo;


namespace cplugin {

    class RetryNode{
    public:
        RetryNode(const std::string& plugin, const std::string& version)
        :plugin_(plugin),
         version_(version),
         time_(60),
         count_(0),
         last_(Timestamp::now())
        {
        }


        void UpdateTime(int32_t flag = 1){

            if(flag == 0){
                time_ = 60;
            }else{
                if(time_ < 30*60)
                time_ = time_*2;
            }

            count_++;
            last_ = Timestamp::now();
        }


        bool isNeedRetry(){

            Timestamp now = Timestamp::now();
            if(now.microSecondsSinceEpoch() - last_.microSecondsSinceEpoch() > time_*1000*1000){
                return true;
            }
            return false;
        }

        bool isOutOfCount(){
            return (count_>50);
        }

        void setVersion(const std::string& version){
             version_ = version;
        }

        std::string getVersion(){
            return version_;
        }
    private:

        std::string plugin_;
        std::string version_;
        int32_t  time_;
        int32_t  count_;
        Timestamp last_;
        friend  class RetryManager;
    };

    typedef std::map<std::string, boost::shared_ptr< RetryNode > > StrRetryNodeMap;
    typedef std::map<std::string, std::string > StrStrMap;
    class RetryManager {

    public:
        RetryManager(){

        }

        void AddRetryNode(const std::string& plugin,const std::string& version){
            StrRetryNodeMap::iterator it = RetryNodeMap_.find(plugin);
            if(it != RetryNodeMap_.end() ){

                if(version != it->second->getVersion()){
                    it->second->setVersion(version);
                    it->second->UpdateTime(0);
                }
                return;
            }


            boost::shared_ptr<RetryNode> pNode = boost::shared_ptr<cplugin::RetryNode>(new RetryNode(plugin, version));
            RetryNodeMap_.insert(make_pair(plugin, pNode));
        }

        void DeleteRetryNode(const std::string& plugin){
            StrRetryNodeMap::iterator it = RetryNodeMap_.find(plugin);
            if(it == RetryNodeMap_.end() )
                return;


            RetryNodeMap_.erase(it);
        }

        StrRetryNodeMap GetRetryNode(){
            StrRetryNodeMap needRetry;
            std::vector<std::string> needDelete;
            StrRetryNodeMap::iterator it = RetryNodeMap_.begin();
            for(; it != RetryNodeMap_.end(); it++ ){
                if(it->second->isNeedRetry()){
                    if(it->second->isOutOfCount()){
                        needDelete.push_back(it->first);
                        continue;
                    }
                    needRetry.insert(make_pair(it->first, it->second));
                }
            }

            std::vector<std::string>::iterator it_del = needDelete.begin();
            for(; it_del != needDelete.end(); it_del++){
                DeleteRetryNode(*it_del);
            }

           return needRetry;
        }
    private:

        StrRetryNodeMap RetryNodeMap_;
    };

} // namespace cplugin

#endif // HOST_MANAGER_H_
