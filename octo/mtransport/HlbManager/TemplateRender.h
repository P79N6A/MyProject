#ifndef __HLB_MANAGER_TEMPLATE_RENDER_H__
#define __HLB_MANAGER_TEMPLATE_RENDER_H__

#include "./hlb_gen_cpp/quota_common_types.h"

namespace inf {
namespace hlb {
using namespace std;

class TemplateRender {
public:
    TemplateRender():
        _normal_lua_tpl_filename("./hlb_tpl/hlb_normal_lua_v1.tpl"),
        _degrade_lua_tpl_filename("./hlb_tpl/hlb_degrade_lua_v1.tpl") {
        ;
    }
    
    //** 传入appkey， 返回正常情况下的lua脚本内容
    string getNormalLua( const string& appkey);

    //** 传入DegradeAction， 返回执行该降级操作的lua脚本内容
    string getDegradeLua( const string& appkey, const DegradeAction& degradeAction);

private:
    //TODO: template有缓存，若修改tpl需要重启服务生效，是否需要Timer检测tpl变更
    bool illegalCheck4Appkey( const string& appkey);
    bool illegalCheck4DegradeAction( const DegradeAction& degradeAction);

    string _normal_lua_tpl_filename;
    string _degrade_lua_tpl_filename;
};

}
}

#endif  //__HLB_MANAGER_TEMPLATE_RENDER__
