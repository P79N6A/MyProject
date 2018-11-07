#ifndef __HLB_CONFIG_H__
#define __HLB_CONFIG_H__

#include <string>
#include <stdint.h>
#include <vector>
#include "./utils/Singleton.h"

namespace inf {
namespace hlb {

class HlbConfig : public Singleton<HlbConfig> {
public:
    HlbConfig();
    /**
    * @Brief 配置项初始化
    * @param path 配置文件路径
    * @return  true: 正确加载配置文件
    *         false: 配置解析失败
    */
    bool initialization(const char * path);

public:
    std::string m_hlbManagerAppkey;
    std::vector<std::string> m_businessVec;
    int         m_hlbManagerHttpPort;

    std::string m_nginxIp;
    int         m_nginxDyPort;
    std::string m_nginxSecureKey;

    std::string m_nginxBinPath;
    std::string m_nginxPidPath;
    std::string m_nginxConfigPrefix;
    std::string m_nginxAppkeyConf;
    std::string m_nginxOriginalUpstreamPath;
    
    std::string m_sgagentIp;
    int         m_sgagentPort;
    int         m_sgagentUpdateTime;
    
    std::string m_mnscAppkey;
    int         m_mnscUpdateTime;
    
    std::string m_upstreamNginxType;
    std::string m_upstreamIDCType;
    int         m_upstreamIsGrey;
};

}
}
#endif //__HLB_CONFIG_H__
