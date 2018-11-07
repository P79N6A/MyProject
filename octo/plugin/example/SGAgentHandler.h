#include <string>
#include "SGAgent.h"
#include "sgagent_service_types.h"
#include "config_common_types.h"

#ifndef __sgagenthandler_H__
#define __sgagenthandler_H__

#define HANDLERNAME "sg_agent"

using namespace __gnu_cxx;
namespace sg_agent
{

const size_t NMATCH = 10;

class SGAgentHandler : virtual public SGAgentIf {
    public:
        SGAgentHandler();

        int Init();

        void getServiceListByProtocol(ProtocolResponse& _return, const ProtocolRequest& req);

        void getServiceList(std::vector<SGService> & _return,
                    const std::string& localAppkey,
                    const std::string& remoteAppkey);

        void getHttpServiceList(std::vector<SGService> & _return,
                    const std::string& localAppkey,
                    const std::string& remoteAppkey);

        void getAppKeyListByBusinessLine(std::vector<std::string> & _return, const int32_t businessLineCode);

        int32_t registService(const SGService& oService);

        int32_t registServicewithCmd(const int32_t uptCmd, const SGService& oService);

        int32_t unRegistService(const SGService& oService);

        void getLocalConfig(std::string& _return, const std::string& localAppkey,
                    const std::string& ip);

        int32_t uploadLog(const SGLog& oLog);

        int32_t uploadCommonLog(const CommonLog& oLog);

        int32_t uploadModuleInvoke(const SGModuleInvokeInfo& oInfo);

        void getZabbixInfo(ZabbixSelfCheck& _return);

        void getDegradeActions(std::vector< ::DegradeAction> & _return, const std::string& localAppkey, const std::string& remoteAppkey);

        void getService(SGService& _return, const std::string& localAppkey,
                    const std::string& remoteAppkey, const int32_t strategy);

        /**
         * Config 更新通知
         */
        int updateConfig(const ::ConfigUpdateRequest& request);

        void getFileConfig( ::file_param_t& _return,  const  ::file_param_t& file);

        void getAuthorizedConsumers(std::string& _return, const std::string& targetAppkey);

        void getAuthorizedProviders(std::string& _return, const std::string& targetAppkey);

        void getHttpPropertiesByBusinessLine(std::map<std::string, HttpProperties> & _return, const int32_t bizCode);

        void getHttpPropertiesByAppkey(std::map<std::string, ::HttpProperties> & _return, const std::string& appkey);

        bool switchEnv(const std::string& env, const std::string& verifyCode);

        int32_t notifyFileConfigIssued(const  ::file_param_t& files);

        int32_t notifyFileConfigWork(const  ::file_param_t& files);

        void getConfig(std::string& _return, const ::proc_conf_param_t& node);

        int32_t setConfig(const proc_conf_param_t& conf);

        void getName(std::string& _return);

        void getVersion(std::string& _return);

	    int32_t getEnv();

        fb_status::type getStatus();

        void getStatusDetails(std::string& _return);

        void getCounters(std::map<std::string, int64_t> & _return);

        int64_t getCounter(const std::string& key);

        void setOption(const std::string& key, const std::string& value);

        void getOption(std::string& _return, const std::string& key);

        void getOptions(std::map<std::string, std::string> & _return);

        void getCpuProfile(std::string& _return, const int32_t profileDurationInSec);

        int64_t aliveSince();

        void reinitialize();

        void setRemoteSwitch(SwitchResponse& _return, const SwitchRequest& req);

        bool shutdown(const std::string& verifyCode);

    private:
        std::string sg_agent_appkey;
};

} // namespace

#endif
