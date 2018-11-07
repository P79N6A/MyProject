#ifndef __HLB_TENGINE_TRIGER_H__
#define __HLB_TENGINE_TRIGER_H__

#include <boost/shared_ptr.hpp>
#include "./utils/Singleton.h"
#include "./utils/auto_lock.h"
#include "./hlb_gen_cpp/SGAgent.h"
#include "OriginalUpstreamKeyTab.h"

namespace inf {
namespace hlb {
using namespace std;
        
class TengineTriger : public Singleton<TengineTriger> {
public:
    TengineTriger() {}
    bool initialization() {
        return true;
    }
    
    //传入变动的HttpServiceList和HttpProperties，触发dyups更新
    void dyupsDataChanged( map< string, vector< SGService> > diffHttpServiceListMap,
                           map< string, HttpProperties> diffPropertiesMap);
    
    //传入变动的original upstream内容，触发dyups更新
    void OriginalUpstreamChanged( const map<string, UpstreamContent>& diffUpstreamMap);
    
    //触发Tengine reload
    int reloadTengine();

private:
    void GenerateMd5( const char *appkey, char *md5HexBuf, size_t bufLength);
    
    int UpdateOctoUpstream(const string& appkey,
                           const vector<SGService>& httpServiceList,
                           const HttpProperties& properties);
    
    void getPostDataByServiceList(const string& appKeyName,
                                  const vector<SGService>& httpServiceList,
                                  const HttpProperties& properties,
                                  char *postData, int MaxLen);
    
    //-----------------------------------------------------------------------
    int UpdateOriginalUpstream(const string& upstreamName,
                               const UpstreamContent& upstreamContent);
    
    void getPostDataByUpstreamJsonStr(const string& upstreamName,
                                      const string& upstreamJsonStr,
                                      char *postData, int MaxLen);
    void getPostDataByUpstreamContent(const string& upstreamName,
                                      const UpstreamContent& upstreamContent,
                                      char *postData, int MaxLen);
    
private:
    OriginalUpstreamKeyTab _key_tab;
};
    
}
}
#endif  //__HLB_TENGINE_TRIGER_H__
