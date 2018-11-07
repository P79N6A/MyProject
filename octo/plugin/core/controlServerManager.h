#ifndef CONTROL_MANAGER_H_
#define CONTROL_MANAGER_H_

#include <string>
#include <vector>
#include <map>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Timestamp.h>
//#include <octoidl/SGAgent.h>
#include "host_manager.h"

using namespace muduo;


namespace cplugin {


    typedef enum UpdateType{
        LIST_EQUAL,
        LIST_INCREASE,
        LIST_DECREASE,
        LIST_DECREASE_ON,
        LIST_BOTH,
        LIST_BOTH_ON
    }UpdateType;

    class ControlManager {

    public:
        ControlManager(HostManager* manager)
        :manager_(manager),
         current_port_(0)
        {
        }


      void updateForFail();
      void update(const vector<cplugin_sgagent::SGService>& vec_sgservice);

    private:

      void filterIDC(const vector<cplugin_sgagent::SGService>& vec_sgservice);


        UpdateType getType(const vector<cplugin_sgagent::SGService>& now, const vector<cplugin_sgagent::SGService>& old,
                            vector<cplugin_sgagent::SGService>& add,  vector<cplugin_sgagent::SGService>& remove);

    private:


        HostManager* manager_;
        std::string current_ip_;
        int current_port_;
        vector<cplugin_sgagent::SGService> vec_control_server_sgservice_;
        vector<cplugin_sgagent::SGService> vec_control_server_sgservice_old_;
    };

} // namespace cplugin

#endif // HOST_MANAGER_H_
