#include "Config.h"
#include "./utils/tinyxml2.h"
#include "./utils/CommonTool.h"
#include "./utils/log4cplus.h"

using namespace inf::hlb;

tinyxml2::XMLDocument doc;
tinyxml2::XMLElement * HlbXml = NULL;

static bool GetStringValue(const char *name, std::string &value) {
    bool ret = true;
    if(HlbXml->FirstChildElement(name) && HlbXml->FirstChildElement(name)->GetText()) {
        value = HlbXml->FirstChildElement(name)->GetText();
        LOG_INFO( "[HlbConfig] "<<name <<" : "<<value);
    } else {
        LOG_ERROR( "[HlbConfig] GetStringValue ERROR for "<< name);
        ret = false;
    }
    return ret;
}

static bool GetIntValue(const char *name, int &value) {
    bool ret = true;
    if(HlbXml->FirstChildElement(name) && HlbXml->FirstChildElement(name)->GetText()) {
        value = atoi(HlbXml->FirstChildElement(name)->GetText());
        LOG_INFO( "[HlbConfig] "<<name <<" : "<<value);
    } else {
        LOG_ERROR( "[HlbConfig] GetIntValue ERROR for "<< name);
        ret = false;
    }
    return ret;
}

HlbConfig::HlbConfig() {
    m_hlbManagerHttpPort = 0;
    m_nginxDyPort = 0;
    m_sgagentUpdateTime = 20;
    m_mnscUpdateTime = 10;
    m_upstreamIsGrey = 0;
}

//#define AD_GUARD_CONFIG(CONF) do {if(0 != (CONF)){return (CONF);}} while(0)
bool HlbConfig::initialization(const char * path) {
    int xml_ret = doc.LoadFile(path);
    if (xml_ret) {
        LOG_ERROR( "[HlbConfig] XMLDocument LoadFile ERROR"<< xml_ret);
        return false;
    }

    HlbXml = doc.RootElement();
    if(HlbXml) {
        if (!GetStringValue("appkey", m_hlbManagerAppkey)) {return false;}
        
        std::string business;
        if (!GetStringValue("business", business)) {return false;}
        split(business, ',', m_businessVec);
        
        if (!GetIntValue("hlbManagerHttpPort", m_hlbManagerHttpPort)) {return false;}

        if (!GetStringValue("nginxIp", m_nginxIp)) {return false;}
        if (!GetIntValue("nginxDyPort", m_nginxDyPort)) {return false;}
        if (!GetStringValue("nginxSecureKey", m_nginxSecureKey)) {return false;}

        if (!GetStringValue("nginxBinPath", m_nginxBinPath)) {return false;}
        if (!GetStringValue("nginxPidPath", m_nginxPidPath)) {return false;}
        if (!GetStringValue("nginxConfigPrefix", m_nginxConfigPrefix)) {return false;}
        if (!GetStringValue("nginxAppkeyConf", m_nginxAppkeyConf)) {return false;}
        if (!GetStringValue("nginxOriginalUpstreamPath", m_nginxOriginalUpstreamPath)) {return false;}
        
        if (!GetStringValue("sgagentIp", m_sgagentIp)) {return false;}
        if (!GetIntValue("sgagentPort", m_sgagentPort)) {return false;}
        if (!GetIntValue("sgagentUpdateTime", m_sgagentUpdateTime)) {return false;}
        
        if (!GetStringValue("mnscAppkey", m_mnscAppkey)) {return false;}
        if (!GetIntValue("mnscUpdateTime", m_mnscUpdateTime)) {return false;}
        
        if (!GetStringValue("upstreamNginxType", m_upstreamNginxType)) {return false;}
        if (!GetStringValue("upstreamIDCType", m_upstreamIDCType)) {return false;}
        if (!GetIntValue("upstreamIsGrey", m_upstreamIsGrey)) {return false;}
        
        return true;
    } else {
        LOG_ERROR( "[HlbConfig] XMLDocument RootElement ERROR");
    }
    return false;
}
