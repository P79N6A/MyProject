// =====================================================================================
//
//       Filename:  config_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-04-16
//       Revision:  none
//
// =====================================================================================
#include <string.h>
#include <vector>
#include <string>
#include <dlfcn.h>
#include <boost/algorithm/string/trim.hpp>
#include <boost/algorithm/string.hpp>
#include "config_client.h"
#include "util/sgagent_stat.h"
#include "comm/inc_comm.h"
#include "comm/log4cplus.h"
#include "util/sg_agent_def.h"
#include "util/SGAgentErr.h"
#include "comm/tinyxml2.h"
#include "mtconfig_server_client.h"
#include "util/falcon_mgr.h"
#include "sgcommon_invoker.h"

#include "util/global_def.h"
using namespace tinyxml2;
using namespace muduo::net;

extern GlobalVar *g_global_var;

namespace sg_agent {
static const size_t kMaxPendingTasks = 100;

BufferMgr<proc_conf_param_t> *ConfigClient::mConfigBufferMgr = NULL;
ConfigClient *ConfigClient::mConfigClient = NULL;
muduo::MutexLock ConfigClient::s_cmutex;

ConfigClient::ConfigClient()
    : loop_(NULL),
      config_collector(NULL),
      loop_thread_(NULL),
      m_timeout(DEFAULT_CONFIG_TIMEOUT),
      m_retry(DEFAULT_CONFIG_RETRY){
}

ConfigClient::~ConfigClient() {
  EventLoopThreadProxyDestroyer(loop_thread_);
  SAFE_DELETE(mConfigBufferMgr);
  SAFE_DELETE(config_collector);
  SAFE_DELETE(mConfigClient);
}

ConfigClient *ConfigClient::getInstance() {
  if (NULL == mConfigClient) {
    muduo::MutexLockGuard lock(s_cmutex);
    if (NULL == mConfigClient) {
      LOG_INFO("first time to get ConfigClient instance, now new it.");
      mConfigClient = new ConfigClient();
    }
  }
  return mConfigClient;
}

void ConfigClient::Destroy() {
  SAFE_DELETE(mConfigClient);
}

int ConfigClient::Init(int timeout, int retry) {
  m_timeout = timeout;
  m_retry = retry;

  XMLDocument conf;
  XMLError confRet = conf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (unlikely(tinyxml2::XML_SUCCESS != confRet)) {
    LOG_FATAL("failed to load config file: " << SG_AGENT_MUTABLE_CONF);
    return ERR_CONFIG_LOAD_CONF_FAIL;
  }
  const char *configPath = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("KVConfigPath")->GetText();
  mConfigPath = configPath;
  if (unlikely(mConfigPath.empty())) {
    LOG_FATAL("kv config not get path in " << SG_AGENT_MUTABLE_CONF);
    return ERR_CONFIG_PATH_EMPTY;
  }

  const char *configFile = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("KVConfigFile")->GetText();
  mConfigFile = configFile;
  if (unlikely(mConfigFile.empty())) {
    LOG_FATAL("kv config not get file in " << SG_AGENT_MUTABLE_CONF);
    return ERR_CONFIG_FILE_EMPTY;
  }

  if (NULL == mConfigBufferMgr) {
    mConfigBufferMgr = new BufferMgr<proc_conf_param_t>();
  }

  //初始化mtconfig-server
  if (NULL == config_collector) {
    config_collector = new MtConfigCollector();
  }
  int ret = config_collector->init();
  if (ret != 0) {
    //mtconfig-server初始化失败，直接报警，不退出sg_agent_worker
    LOG_FATAL("Init MtConfigCollector failed! ret = " << ret);
    return ret;
  }

  keyMap_ = boost::shared_ptr < std::map < std::string, timeval > > (new std::map<std::string, timeval>());

  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return FAILURE;
  }
  // 启动backend处理线程
  loop_ = loop_thread_->startLoop();

  // start update config timer
  boost::shared_ptr<UpdateConfigHandler> update_task(new UpdateConfigHandler(this));
  loop_->runEvery(DEFAULT_SCANTIME, update_task);
  // start save config to disk timer
  boost::shared_ptr<SaveConfigToDiskHandler> save_disk_task(new SaveConfigToDiskHandler(this));
  loop_->runEvery(DEFAULT_SAVECONFIG_TIME, save_disk_task);
  // start Clear config from buffer timer
  boost::shared_ptr<ClearConfigHandler> clear_task(new ClearConfigHandler(this));
  loop_->runEvery(DEFAULT_CLEARCONFIG_TIME, clear_task);
  // start Sync Config info to server
  boost::shared_ptr<SyncConfigHandler> sync_task(new SyncConfigHandler(this));
  loop_->runEvery(DEFAULT_SYNCCONFIG_TIME, sync_task);

  return 0;
}

int ConfigClient::GetConfig(std::string &_return,
                            const proc_conf_param_t &node) {


  ConfigParamPtr req_param(new proc_conf_param_t());
  req_param->conf = "";
  req_param->appkey = "";

  std::string key = GenCacheKey(node);


  // 从mConfigBufferMgr中获取config
  int ret = mConfigBufferMgr->get(key, *req_param);
  if (SUCCESS == ret) {
    int count = 0;
    static InvokeStat hitBuffStat;
    if (InvokeStat::GetInvokeStatInfo(hitBuffStat, count)) {
      LOG_STAT("succeed to get config from mConfigBufferMgr, count = "
                   << count);
    }

    GenRetJson(_return, *req_param, 0);

    _updateAccessTime(key);
    return 0;
  }

  LOG_INFO("the dynamic cfg buffer does not have key = " << key << ", now get it from the mcc server.");
  ret = GetConfigFromWorker(_return, node);
  if (SUCCESS != ret) {
    LOG_WARN("getConfigFromWorker failed! ret = " << ret);
    proc_conf_param_t conf_param;
    {
      muduo::MutexLockGuard lock(rdDiskMutexLock_);
      ret = _LoadFromDisk(conf_param, node);
    }
    if (SUCCESS == ret) {
      // 更新buffer
      std::string key = GenCacheKey(conf_param);
      mConfigBufferMgr->insert(key, conf_param);
      LOG_DEBUG("Succeed to get config from file,"
                    << " key = " << key
                    << "; config = " << conf_param.conf);
      _return = conf_param.conf;
    }
  }
  return ret;
}


int ConfigClient::GetConfigFromWorker(std::string &_return,
                                      const proc_conf_param_t &node) {
  int ret = SUCCESS;
  _return = "";

  /**
   * snd msg to worker
   */
  proc_conf_param_t req_param(node);
  req_param.__set_cmd(MQ_GET_CONF);

  ConfigContextPtr context(
      new TaskContext<proc_conf_param_t, proc_conf_param_t>(req_param));

  ret = SendMCCReq(context);
  if (0 != ret) {
    return ret;
  }

  //wait for return
  context->WaitResult(m_timeout);
  if (NULL == context->get_response()) {
    LOG_INFO("don't get response in time, key = "
                 << node.key);
    ret = ERR_GETCONFIG_TIMEOUT;
    GenRetJson(_return, ret);
  } else {
    GenRetJson(_return, *context->get_response(), context->get_response()->err);
  }
  return ret;
}

int ConfigClient::SendMCCReq(ConfigContextPtr context) {
  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetKvConfigQueueSize(pending_tasks_size);

  boost::shared_ptr<MCCHandler> task(new MCCHandler(this, context));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_ERROR("the dynamic cfg queue is full, now drop the task. size: " << pending_tasks_size);
    return ERR_CONFIG_TASKSIZE_OVERLOAD;
  }
  return 0;
}
int ConfigClient::GetConfigBeforeSet(const proc_conf_param_t &node){

  std::string _return = "";
  int ret = GetConfigFromWorker(_return, node);
  LOG_INFO("node is key = " << node.key << "appkey = " << node.appkey);
	if (SUCCESS != ret) {
    LOG_WARN("getConfigFromWorker failed! ret = " << ret);
    proc_conf_param_t conf_param;
    do{
      muduo::MutexLockGuard lock(rdDiskMutexLock_);
      ret = _LoadFromDisk(conf_param, node);
    }while(0);
    if (SUCCESS == ret) {
      // 更新buffer
      std::string key = GenCacheKey(conf_param);
      mConfigBufferMgr->insert(key, conf_param);
      LOG_DEBUG("Succeed to get config from file,"
                    << " key = " << key
                    << "; config = " << conf_param.conf);
    }
  }
  return ret;
}

int ConfigClient::SetConfig(const proc_conf_param_t &node) {

  proc_conf_param_t req_param;
  req_param.conf = "";
	LOG_INFO("node is key = " << node.key << " node appkey = " << node.appkey << " node token = " << node.token);
  std::string key = GenCacheKey(node);

  // 从mConfigBufferMgr中获取config
  int ret = mConfigBufferMgr->get(key, req_param);
  if (SUCCESS != ret) {
    LOG_ERROR("config buffer doesn't have key: " << key
                                                 << ", do getconfig from mcc server");
    if(SUCCESS != GetConfigBeforeSet(node)){
      LOG_ERROR("get config from mcc server, file and cache failed: " << key);
      return ERR_CANNOTSET;
    }else{
      LOG_INFO("get config from mcc server success: " << key);
      ret = mConfigBufferMgr->get(key, req_param);
    }
  }
  long version = req_param.version;

  req_param = node;
  req_param.__set_cmd(MQ_SET_CONF);
  req_param.__set_version(version);

  ConfigContextPtr context(
      new TaskContext<proc_conf_param_t, proc_conf_param_t>(req_param));
  ret = SendMCCReq(context);
  if (0 != ret) {
    return ret;
  }

  //wait for return
  context->WaitResult(m_timeout);
  if (NULL == context->get_response()) {
    LOG_INFO("don't get response in time, key = "
                 << node.key);
    ret = ERR_GETCONFIG_TIMEOUT;
  } else {
    if (200 == context->get_response()->err
        || 0 == context->get_response()->err) {
      ret = SUCCESS;
    } else if (0 < context->get_response()->err) {
      ret = ERRCODE_DISC - context->get_response()->err; // errno转换
      // 如果鉴权失败，mcc-server错误码是401，计算后错误码变成-201401
      // 鉴权失败，MCC Server正常，不作为可用性异常统计
    } else {
      ret = context->get_response()->err;
    }
  }
  return ret;
}

int ConfigClient::UpdateConfig(
    const ConfigUpdateRequest &request) {

  int ret = 0;

  proc_conf_param_t req_param;
  req_param.conf = "";
  for (int i = 0; i < request.nodes.size(); ++i) {
    ret = _convertNode(request.nodes[i], req_param);
    if (0 != ret) {
      LOG_ERROR("The node's param in ConfigUpdateRequest is not complete"
                    << ", ret = " << ret);
      return ret;
    }

    req_param.__set_cmd(MQ_UPDATE_CONF);

    ConfigContextPtr context(
        new TaskContext<proc_conf_param_t, proc_conf_param_t>(req_param));
    ret = SendMCCReq(context);
    if (0 != ret) {
      return ret;
    }

    //wait for return
    context->WaitResult(m_timeout);
    if (NULL == context->get_response()) {
      LOG_ERROR("don't get response in time, appkey = "
                    << req_param.appkey);
      ret = ERR_GETCONFIG_TIMEOUT;
    } else if (200 == context->get_response()->err
        || 0 == context->get_response()->err) {
      LOG_INFO("succeed get response in time when update config, key = "
                   << req_param.appkey);
      ret = SUCCESS;
    } else if (0 < context->get_response()->err) {
      LOG_INFO("get response in time when update config, key = "
                   << req_param.appkey
                   << ", errcode = " << context->get_response()->err);
      ret = ERRCODE_DISC - context->get_response()->err; // errno转换
    } else {
      LOG_INFO("get response in time when update config, key = "
                   << req_param.appkey
                   << ", errcode = " << context->get_response()->err);
      ret = context->get_response()->err;
    }
  }

  return ret;
}

/**
 * path+appkey+env or path+appkey+env+swimlane while the siwmlane is not empty.
 * @param node
 * @return
 */
std::string ConfigClient::GenCacheKey(const proc_conf_param_t &node) {
  std::string env = node.env.empty() ? g_global_var->gEnvStr : node.env;
  std::string key = node.path + "+" + node.appkey + "+" + env;

  const std::string prefix_cell = "cell";
  const std::string prefix_swimlane = "swimlane";

  if (!node.cell.empty()&&node.swimlane.empty()) {
    std::string cell = node.cell;
    boost::trim(cell);
    key = prefix_cell+ key + "+" + node.cell;
  }else if (!node.cell.empty()&&!node.swimlane.empty()) {
    std::string cell = node.cell;
    boost::trim(cell);
    std::string swimlane = node.swimlane;
    boost::trim(swimlane);
    key = prefix_cell + "+" + prefix_swimlane + "+" + key + "+" + node.cell + node.swimlane;
  }else if (node.cell.empty()&&!node.swimlane.empty()) {
    std::string swimlane = node.swimlane;
    boost::trim(swimlane);
    key = prefix_swimlane + "+" + key + "+" + node.swimlane;
  }else{
    LOG_INFO("using default,no swimlane and cell flags,node appkey: " <<node.appkey);
  }
  return key;
}

/**
 * appkey+env or appkey+env+swimlane while the siwmlane is not empty.
 * add cell or swimlane prefix to distinguish
 * @param node
 * @return
 */
std::string ConfigClient::GenFileName(const proc_conf_param_t &node) {

  std::string key = node.env.empty() ? g_global_var->gEnvStr : node.env;
  std::string prefix_cell = "cell", prefix_swimlane = "swimlane";

  // in order to compatible the original file name.
  if (!node.cell.empty() && node.swimlane.empty()) {
    std::string cell = node.cell;
    boost::trim(cell);
    key = prefix_cell + "_" + key + "_" + cell;
  } else if (!node.cell.empty() && !node.swimlane.empty()) {
    std::string cell = node.cell;
    boost::trim(cell);
    std::string swimlane = node.swimlane;
    boost::trim(swimlane);
    key = prefix_cell + "_" + prefix_swimlane + "_" + key + "_" + cell + "_" + swimlane;
  } else if (node.cell.empty() && !node.swimlane.empty()) {
    std::string swimlane = node.swimlane;
    boost::trim(swimlane);
    key = prefix_swimlane + "_" + key + "_" + swimlane;
  } else {
    LOG_INFO("using default,no swimlane and cell flags,node appkey: " <<node.appkey);
  }
  return key;
}

std::string ConfigClient::GenDiskPath(const proc_conf_param_t &node) {

  std::string path = node.path;
  boost::replace_all(path, ".", "/");
  return mConfigPath + "/" + node.appkey + "/" + path;
}

int ConfigClient::_convertNode(const ConfigNode &node, proc_conf_param_t &param) {

  if (node.appkey.empty() || node.env.empty() || node.path.empty()) {
    LOG_ERROR("param is not complete"
                  << ", appkey = " << node.appkey
                  << ", env = " << node.env
                  << ", path = " << node.path);

    return ERR_PARAMNOTCOMPLETE;
  }

  param.__set_appkey(node.appkey);
  param.__set_env(node.env);
  param.__set_path(node.path);
  if (node.__isset.cell) {
    param.__set_cell(node.cell);
  }
  if (node.__isset.swimlane) {
    param.__set_swimlane(node.swimlane);
  }
  return 0;
}

int ConfigClient::_convertToNode(const proc_conf_param_t &param, ConfigNode &node) {
  if (param.appkey.empty() || param.env.empty() || param.path.empty()) {
    LOG_ERROR("param is not complete"
                  << ", appkey = " << param.appkey
                  << ", env = " << param.env
                  << ", path = " << param.path);
    return ERR_PARAMNOTCOMPLETE;
  }

  node.__set_appkey(param.appkey);
  node.__set_env(param.env);
  node.__set_path(param.path);
  return 0;
}

void ConfigClient::UpdateConfigTimer() {

  boost::shared_ptr<std::map<std::string, timeval> > keyMap;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    keyMap = keyMap_;
  }
  std::map<std::string, timeval>::iterator iter;
  for (iter = keyMap->begin(); keyMap->end() != iter; ++iter) {
    //先从buf中获取老版本信息, 如果没有则直接传空给sg_agent_worker
    proc_conf_param_t configParamTrigger;
    std::string key = iter->first;
    int ret = mConfigBufferMgr->get(key, configParamTrigger);
    configParamTrigger.__set_cmd(MQ_SCAN_CONF);
    if (ret != 0) {
      LOG_ERROR("failed to get key: " << key
                                      << " from buffer. ret = " << ret);
      continue;
    }

    ConfigContextPtr context(
        new TaskContext<proc_conf_param_t, proc_conf_param_t>(configParamTrigger));
    ret = SendMCCReq(context);
    if (0 != ret) {
      LOG_ERROR("failed to sendConfigMsg, appkey = " << configParamTrigger.appkey
                                                     << "; env = " << configParamTrigger.env
                                                     << "; path = " << configParamTrigger.path
                                                     << "; errno = " << ret);
    }
  }
}

void ConfigClient::SaveConfigToDiskTimer() {
  int ret = 0;
  _PrintConfigListToLog();
  LOG_STAT("ConfigClient print ConfigList");

  // 持久化到磁盘
  {
    muduo::MutexLockGuard lock(rdDiskMutexLock_);
    ret = _SaveToDisk();
  }
  if (0 == ret) {
    LOG_STAT("Succeed to save config to disk");
  } else {
    LOG_STAT("failed to save config to disk");
  }
}

/*
 * 将缓存配置内容打印到日志中
 * */
int ConfigClient::_PrintConfigListToLog() {
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    if (!keyMap_.unique()) {
      keyMap_.reset(new std::map<std::string, timeval>(*keyMap_));
    }
    if (0 == keyMap_->size()) {
      LOG_INFO("dynamic cfg cache is empty");
      return 0;
    }
  }

  static proc_conf_param_t configParamPrint;

  boost::shared_ptr<std::map<std::string, timeval> > keyMap;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    keyMap = keyMap_;
  }
  std::map<std::string, timeval>::iterator iter;
  std::string key;
  for (iter = keyMap->begin();
       iter != keyMap->end(); ++iter) {
    key = iter->first;
    int ret = mConfigBufferMgr->get(key, configParamPrint);
    if (ret != 0) {
      LOG_ERROR("failed to get key: " << key << " from configBufferMgr");
      continue;
    }

    LOG_DEBUG("appkey = " << configParamPrint.appkey
                          << "; env = " << configParamPrint.env
                          << "; path = " << configParamPrint.path
                          << "; version = " << configParamPrint.version
                          << "; conf = " << configParamPrint.conf);
  }

  return 0;
}

/*
 * 将缓存配置内容保存到磁盘
 * */
int ConfigClient::_SaveToDisk() {
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    if (!keyMap_.unique()) {
      keyMap_.reset(new std::map<std::string, timeval>(*keyMap_));
    }
    if (keyMap_->empty()) {
      LOG_INFO("mConfigBuffer is empty, needn't to save to disk");
      return 0;
    }
  }

  int ret = mkCommonDirs(mConfigPath.c_str());
  if (0 != ret) {
    LOG_ERROR("failed to mkdir: " << mConfigPath);
    return ERR_CONFIG_MKDIR;
  }

  static proc_conf_param_t configParamPrint;

  boost::shared_ptr<std::map<std::string, timeval> > keyMap;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    keyMap = keyMap_;
  }
  std::map<std::string, timeval>::iterator iter;
  std::string key;

  for (iter = keyMap->begin();
       iter != keyMap->end(); ++iter) {
    configParamPrint.appkey.clear();
    configParamPrint.conf.clear();
    configParamPrint.path.clear();
    key = iter->first;
    ret = mConfigBufferMgr->get(key, configParamPrint);
    if (SUCCESS != ret) {
      LOG_ERROR("fail to get key: " << key << " from configBufferMgr");
      continue;
    }

    std::string dir_path = GenDiskPath(configParamPrint);
    ret = mkCommonDirs(dir_path.c_str());
    if (SUCCESS != ret) {
      LOG_ERROR("fail to mkdir: " << dir_path);
      continue;
    }
    const std::string file = dir_path + "/" + GenFileName(configParamPrint);
    std::ofstream fout(file.c_str());
    if (!fout.is_open()) {
      LOG_ERROR("faild to open file: " << file);
      fout.close();
      continue;
    }

    LOG_DEBUG("appkey = " << configParamPrint.appkey
                          << "; env = " << configParamPrint.env
                          << "; path = " << configParamPrint.path
                          << "; version = " << configParamPrint.version
                          << "; cell = " << configParamPrint.version
                          << "; swimlane = " << configParamPrint.cell
                          << "; conf = " << configParamPrint.conf);
    std::string title = "[" + configParamPrint.appkey + "]";
    fout << title << "\n";
    fout << "appkey=" << configParamPrint.appkey << "\n";
    fout << "env=" << configParamPrint.env << "\n";
    fout << "path=" << configParamPrint.path << "\n";
    fout << "version=" << configParamPrint.version << "\n";
    fout << "cell=" << configParamPrint.cell << "\n";
    fout << "swimlane=" << configParamPrint.swimlane << "\n";
    fout << "conf=" << configParamPrint.conf << "\n";

    fout.close();
    LOG_INFO("succeed to save file: " << file);
  }

  return 0;
}

/*
 * 将制定appkey内容从文件load到buffer
 * */
int ConfigClient::_LoadFromDisk(proc_conf_param_t &_return,
                                const proc_conf_param_t &node) {

  const std::string file = GenDiskPath(node) + "/" + GenFileName(node);
  const std::string appkey = node.appkey;
  std::ifstream fin(file.c_str());
  if (!fin.is_open()) {
    LOG_ERROR("dynamic cfg can not open file = " << file);
    return ERR_CONFIG_OPENFILE_FAILED;
  }

  std::string content;
  while (!fin.eof()) {
    fin >> content;
    std::string tmp_appkey;
    if (0 == _isNewApp(tmp_appkey, content)) {
      if (tmp_appkey == appkey) {
        _return.__set_appkey(appkey);
        fin >> content;
        while (!fin.eof() &&
            0 != _isNewApp(tmp_appkey, content)) {
          int pos = content.find_first_of("=", 0);
          std::string key = content.substr(0, pos);
          std::string value = content.substr(pos + 1,
                                             content.length() - 1 - pos);
          _genConfParam(_return, key, value);
          fin >> content;
        }

        LOG_DEBUG("get from file, appkey = " << _return.appkey
                                             << "; env = " << _return.env
                                             << "; path = " << _return.path
                                             << "; version = " << _return.version
                                             << "; cell = " << _return.cell
                                             << "; swimlane = " << _return.swimlane
                                             << "; conf = " << _return.conf);
        fin.close();
        return SUCCESS;
      }
    }
  }
  fin.close();
  return FAILURE;
}

int ConfigClient::_isNewApp(std::string &appkey,
                            const std::string &content) {
  if (content.empty()) {
    return -1;
  }

  int ret = 0;
  int len = content.length();
  if ('[' == content[0] && ']' == content[len - 1]) {
    appkey = content.substr(1, len - 2);
  } else {
    return -1;
  }

  return ret;
}

int ConfigClient::_genConfParam(proc_conf_param_t &_return,
                                std::string key, std::string value) {
  if ("appkey" == key) {
    _return.__set_appkey(value);
  } else if ("env" == key) {
    _return.__set_env(value);
  } else if ("path" == key) {
    _return.__set_path(value);
  } else if ("version" == key) {
    long version = atol(value.c_str());
    _return.__set_version(version);
  } else if ("cell" == key) {
    _return.__set_cell(value);
  } else if ("swimlane" == key) {
    _return.__set_swimlane(value);
  } else if ("conf" == key) {
    _return.__set_conf(value);
  }
  return SUCCESS;
}

// 更新conf最近访问时间
int ConfigClient::_updateAccessTime(std::string key) {
  // update conf access time
  timeval tval;
  gettimeofday(&tval, NULL);

  boost::shared_ptr<std::map<std::string, timeval> > keySet;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    if (!keyMap_.unique()) {
      keyMap_.reset(new std::map<std::string, timeval>(*keyMap_));
    }

    keyMap_->insert(std::pair<std::string, timeval>(key, tval));
  }
  return 0;
}

void ConfigClient::ClearConfigTimer() {
  timeval tvalNow;
  timeval tvalSet;
  gettimeofday(&tvalNow, NULL);
  long deltaTime;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    if (!keyMap_.unique()) {
      keyMap_.reset(new std::map<std::string, timeval>(*keyMap_));
    }
    std::map<std::string, timeval>::iterator iter = keyMap_->begin();
    while (iter != keyMap_->end()) {
      //先从buf中获取老版本信息, 如果没有则直接传空给sg_agent_worker
      std::string key = iter->first;
      tvalSet = iter->second;

      deltaTime = (tvalNow.tv_sec - tvalSet.tv_sec) * 1000000L
          + (tvalNow.tv_usec - tvalSet.tv_usec);
      if (deltaTime > DEFAULT_EXTIME) {
        LOG_INFO("delete key = " << key << " from configBuffer");
        int ret = mConfigBufferMgr->del(key);
        if (unlikely(SUCCESS != ret)) {
          LOG_ERROR("failed to del key: " << key
                                          << " from buffer, ret = " << ret);
        } else {
          keyMap_->erase(iter++); // map erase: iter not move to next
          continue;
        }
      } //if deltaTime的范围
      ++iter;
    }
  }
}

void ConfigClient::SyncConfigTimer() {

  {

    FileConfigClient::getInstance()->SyncConfigPeriodicTimer();

    muduo::MutexLockGuard lock(keyMapMutexLock_);
    if (!keyMap_.unique()) {
      keyMap_.reset(new std::map<std::string, timeval>(*keyMap_));
    }
    if (0 == keyMap_->size()) {
      LOG_INFO("configBuffer is empty, don't need to sync");
      return;
    }
  }

  static proc_conf_param_t configParamSyncTmp;
  static ConfigNode node;
  static proc_conf_param_t configParamSync;
  static std::vector<ConfigNode> nodeList;
  static sg_msgbuf sync_req_buf;

  boost::shared_ptr<std::map<std::string, timeval> > keyMap;
  {
    muduo::MutexLockGuard lock(keyMapMutexLock_);
    keyMap = keyMap_;
  }
  std::map<std::string, timeval>::iterator iter;
  std::string key;
  for (iter = keyMap->begin();
       iter != keyMap->end(); ++iter) {
    key = iter->first;
    int ret = mConfigBufferMgr->get(key, configParamSyncTmp);
    if (unlikely(ret != 0)) {
      LOG_ERROR("failed to get key: " << key << " from configBufferMgr");
      continue;
    }

    ret = _convertToNode(configParamSyncTmp, node);
    if (unlikely(0 != ret)) {
      LOG_ERROR("failed to convertToNode, key = " << key);
      continue;
    }
    nodeList.push_back(node);
  }

  if (0 < nodeList.size()) {
    configParamSync.__set_configNodeList(nodeList);
    configParamSync.__set_cmd(MQ_SYNC_CONF);
    // 下面三个值无用，但是由于是required, 故填入一个任意值
    configParamSync.__set_appkey("sg_agent");
    configParamSync.__set_path("path");
    configParamSync.__set_env("prod");
    ConfigContextPtr context(
        new TaskContext<proc_conf_param_t, proc_conf_param_t>(configParamSync));
    int ret = SendMCCReq(context);
    if (likely(0 == ret)) {
      LOG_INFO("Succeed to send msg to sync config, size = "
                   << nodeList.size());
    } else {
      LOG_ERROR("Failed to send msg to sync config, size = "
                    << nodeList.size());
    }
    nodeList.clear();
  }
  //增加同步配置文件接口

  // hotfix for sg_agent 3.1.6.  disable the file sync function because of coredump
  // FileConfigClient::getInstance()->SyncConfigPeriodicTimer();

}

int ConfigClient::GenRetJson(
    std::string &_return,
    const proc_conf_param_t &res, int err) {
  return GenRetJson(_return, err, res.conf, res.version);
}
int ConfigClient::GenRetJson(
    std::string &_return,
    const int err,
    const std::string &conf,
    const int64_t version) {
  std::string res_ret = "\"ret\":";
  std::string res_msg = "\"msg\":";
  std::string res_data = "\"data\":";
  std::string res_version = "\"version\":";
  std::string res_null = "null";

  const int timeout_errno = ERR_GETCONFIG_TIMEOUT;
  switch (err) {
    case 0:
    case 200: {
      res_ret += "0";
      res_msg += "\"success\"";
      res_version += "\"" + boost::lexical_cast<std::string>(version) + "\"";
      res_data += conf;
      break;
    }
    case 302: {
      res_ret += "-201302";
      res_msg += "\"no change\"";
      res_data += res_null;
      break;
    }
    case 500: {
      res_ret += "-201500";
      res_msg += "\"unknow error\"";
      res_data += res_null;
      break;
    }
    case 501: {
      res_ret += "-201501";
      res_msg += "\"param error\"";
      res_data += res_null;
      break;
    }
    case 502: {
      res_ret += "-201502";
      res_msg += "\"node not exist\"";
      res_data += res_null;
      break;
    }
    case 503: {
      res_ret += "-201503";
      res_msg += "\"not exsit version\"";
      res_data += res_null;
      break;
    }
    case 504: {
      res_ret += "-201504";
      res_msg += "\"depracated version\"";
      res_data += res_null;
      break;
    }
    case timeout_errno: {
      res_ret += "-201010";
      res_msg += "\"timeout\"";
      res_data += res_null;
      break;
    }
    default: {
      //res_ret += "-201009";
      char err_str[32];
      sprintf(err_str, "%d", err);
      res_ret += err_str;
      res_msg += "\"sg_agent unknow error\"";
      res_data += res_null;
    }
  }

  if (0 == err || 200 == err) {
    _return = "{" + res_ret + ","
        + res_msg + "," + res_data + ","
        + res_version + "}";
  } else {
    _return = "{" + res_ret + ","
        + res_msg + "}";
  }

  return 0;
}

//worker thread handle mtconfig
void ConfigClient::BackendMCCHandler(ConfigContextPtr context) {
  int ret = 0;
  proc_conf_param_t msg_res = *context->get_request();
  if (msg_res.env.empty()) {
    LOG_INFO("the env of the request dynamic cfg is not set up. now use the sg_agent env = " << g_global_var->gEnvStr);
    msg_res.__set_env(g_global_var->gEnvStr);
  }

  //根据消息类型，进行业务逻辑处理 
  switch (msg_res.cmd) {
    case MQ_GET_CONF:
    case MQ_UPDATE_CONF:
    case MQ_SCAN_CONF:
      //getConf, updateConfig
      ret = GetConfigFromServer(&msg_res);
      break;
    case MQ_SYNC_CONF:
      //将消息体中节点信息同步到mtconfig-server
      ret = SyncConfigToServer(&msg_res);
      break;
    case MQ_SET_CONF:
      //setConfig 
      ret = SetConfigToServer(&msg_res);
      break;
    default:ret = ERR_CONFIG_INVILIDCMD;
      LOG_ERROR("process mtconfig request fail, oparam.cmd error, cmd : " << msg_res.cmd);
  }

  //返回操作结果给sg_agent前端
  if (ret != sg_agent::MTCONFIG_OK
      && ret != sg_agent::MTCONFIG_NOT_CHANGE) {
    LOG_WARN("operate Config fail , appkey : " << msg_res.appkey
                                               << ", env : " << msg_res.env
                                               << ", path : " << msg_res.path
                                               << ", version : " << msg_res.version
                                               << " , ret : " << ret
                                               << ", cmd : " << msg_res.cmd);
  }
  //封装返回错误码
  msg_res.__set_err(ret);

  context->set_response(msg_res);
}

int ConfigClient::GetConfigFromServer(proc_conf_param_t *res) {
  //getConf, updateConfig
  int ret = config_collector->getConfigData(*res);
  if (ret != SUCCESS && ret != sg_agent::MTCONFIG_OK) {
    LOG_ERROR("getConfig from config-server fail, appkey : "
                  << res->appkey
                  << ", env : " << res->env
                  << ", path : " << res->path
                  << ", version : " << res->version
                  << ", ret : " << ret
                  << ", conf : " << res->conf);
  } else {
    std::string key = GenCacheKey(*res);
    mConfigBufferMgr->insert(key, *res);
    LOG_DEBUG("Succeed to get config from server,"
                  << " key = " << key
                  << "; config = " << res->conf);
    _updateAccessTime(key);
  }
  return ret;
}

int ConfigClient::SetConfigToServer(proc_conf_param_t *res) {
  int ret = config_collector->setConfigData(*res);
  if ((ret != 0) && (ret != sg_agent::MTCONFIG_OK)) {
    LOG_ERROR("setConfig from config-server, appkey : " << res->appkey
                                                        << ", env : " << res->env
                                                        << ", path : " << res->path
                                                        << ", version : " << res->version
                                                        << ", ret : " << ret
                                                        << ", conf : " << res->conf);
  }
  return ret;
}

int ConfigClient::SyncConfigToServer(proc_conf_param_t *res) {
  int ret = config_collector->syncRelation(*res);
  if ((ret != 0) && (ret != sg_agent::MTCONFIG_OK)) {
    LOG_ERROR("syncRelation failed, ret: " << ret
                                           << ", size: " << res->configNodeList.size());
  }
  return ret;
}

} //namespace
