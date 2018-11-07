#include <regex.h>
#include <iostream>

#include "SGAgentHandler.h"
#include "operation_common_types.h"

using namespace std;

namespace sg_agent{

SGAgentHandler::SGAgentHandler() {
}

int SGAgentHandler::Init()
{
    return 0;
}

void SGAgentHandler::getServiceListByProtocol(ProtocolResponse& _return, const ProtocolRequest& req) {
}


void SGAgentHandler::getDegradeActions(std::vector<DegradeAction> & _return,
            const std::string& localAppkey, const std::string& remoteAppkey)
{
}

void SGAgentHandler::getServiceList(
    std::vector<SGService> & _return,
    const std::string& localAppkey,
    const std::string& remoteAppkey) {
}

void SGAgentHandler::getHttpServiceList(std::vector<SGService> & _return,
            const std::string& localAppkey, const std::string& remoteAppkey)
{
}

void SGAgentHandler::getAppKeyListByBusinessLine(std::vector<std::string> & _return, const int32_t businessLineCode)
{
}

int32_t SGAgentHandler::registService(const SGService& oService)
{
  cout << "registe service1" << endl;
  return 0;
}

int32_t SGAgentHandler::registServicewithCmd(
        const int32_t uptCmd,
        const SGService& oService){
return 0;
}

int32_t SGAgentHandler::unRegistService(const SGService& oService) {
    return 0;
}

int SGAgentHandler::updateConfig(const ::ConfigUpdateRequest& request)
{
  return 0;
}

void SGAgentHandler::getConfig(std::string& _return, const ::proc_conf_param_t& node)
{
}

int32_t SGAgentHandler::setConfig(const proc_conf_param_t& conf)
{
    return 0;
}

void SGAgentHandler::getLocalConfig(
    std::string& _return,
    const std::string& localAppkey,
    const std::string& ip) {
}

int32_t SGAgentHandler::uploadLog(const SGLog& oLog) {
    return 0;
}

int32_t SGAgentHandler::uploadCommonLog(const CommonLog& oLog)
{
    return 0;
}

int32_t SGAgentHandler::uploadModuleInvoke(const SGModuleInvokeInfo& oInfo)
{
    return 0;
}


void SGAgentHandler::getZabbixInfo(ZabbixSelfCheck& _return) {
}

void SGAgentHandler::getService(SGService& _return, const std::string& localAppkey,
            const std::string& remoteAppkey, const int32_t strategy) {
}

void SGAgentHandler::getName(std::string& _return) {
}

void SGAgentHandler::getVersion(std::string& _return) {
}

int32_t SGAgentHandler::getEnv() {
  return 0;
}

fb_status::type SGAgentHandler::getStatus() {
    return fb_status::ALIVE;
}

void SGAgentHandler::getStatusDetails(std::string& _return) {
    return;
}

void SGAgentHandler::getFileConfig( ::file_param_t & _return, const  ::file_param_t& file) 
{
}

int32_t SGAgentHandler::notifyFileConfigIssued(const  ::file_param_t& files) 
{
    return 0;
}

int32_t SGAgentHandler::notifyFileConfigWork(const  ::file_param_t& files) {
    return 0;
}

void SGAgentHandler::getAuthorizedConsumers(std::string & _return, const std::string& targetAppkey)
{
}

void SGAgentHandler::getAuthorizedProviders(std::string & _return, const std::string& targetAppkey)
{
}

bool SGAgentHandler::switchEnv(const std::string& env, const std::string& verifyCode)
{
    return true;
}

void SGAgentHandler::getHttpPropertiesByBusinessLine(
        std::map<std::string, HttpProperties> & _return,
        const int32_t bizCode)
{
  return;
}

void SGAgentHandler::getHttpPropertiesByAppkey(std::map<std::string, HttpProperties> & _return, const std::string& appkey)
{
}

void SGAgentHandler::getCounters(std::map<std::string, int64_t> & _return) {
}

int64_t SGAgentHandler::getCounter(const std::string& key) {
    return 0;
}

void SGAgentHandler::setOption(const std::string& key, const std::string& value) {
    return;
}

void SGAgentHandler::getOption(std::string& _return, const std::string& key) {
    return;
}

void SGAgentHandler::getOptions(std::map<std::string, std::string> & _return) {
    return;
}

void SGAgentHandler::getCpuProfile(std::string& _return, const int32_t profileDurationInSec) {
    return;
}

int64_t SGAgentHandler::aliveSince() {
    return 0;
}

void SGAgentHandler::reinitialize() {
    return;
}

bool SGAgentHandler::shutdown(const std::string& verifyCode) {
    return true;
}

void SGAgentHandler::setRemoteSwitch(SwitchResponse& _return, const SwitchRequest& req) {
}

} // namespace
