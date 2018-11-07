#ifndef __HLB_MANAGER_H__
#define __HLB_MANAGER_H__

#include <string>
#include <vector>
#include "./utils/Singleton.h"
#include "hlb_gen_cpp/SGAgent.h"

namespace inf {
namespace hlb {
        
using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;
using namespace boost;

class HlbManager : public Singleton<HlbManager> {
public:
    HlbManager() {}

    //落地配置文件后，触发nginx reload
    static int ReloadTengine();

    //起线程调用DyupsDataManager::instance().DyupsDataPullTimer();
    static void * DyupsDataUpdater(void * args);
    
    //起线程调用OriginalUpstreamManager::instance().OriginalUpstreamPullTimer()
    static void * OriginalUpstreamUpdater(void * args);
    
private:
    
};
    
}
}
#endif //__HLB_MANAGER_H__
