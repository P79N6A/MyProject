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
#include "file_config_client.h"
#include "mtconfig_server_client.h"
#include "util/sgagent_stat.h"
#include "comm/inc_comm.h"
#include "comm/md5.h"
#include "comm/log4cplus.h"
#include "util/sg_agent_def.h"
#include "util/SGAgentErr.h"
#include "comm/tinyxml2.h"
#include "util/falcon_mgr.h"
#include <dlfcn.h>
#include "sgcommon_invoker.h"
#include "util/global_def.h"
#include "zk_tools.h"

using namespace tinyxml2;
using namespace muduo::net;

extern GlobalVar *g_global_var;

namespace sg_agent {

static const size_t kMaxPendingTasks = 100;

BufferMgr<ConfigFile> *FileConfigClient::mFileConfigBufferMgr = NULL;
FileConfigClient *FileConfigClient::mFileConfigClient = NULL;
pthread_mutex_t FileConfigClient::m_fMutex = PTHREAD_MUTEX_INITIALIZER;

FileConfigClient::FileConfigClient()
    : loop_(NULL),
      config_collector(NULL),
      loop_thread_(NULL),
      m_timeout(DEFAULT_FILECONFIG_TIMEOUT),
      m_retry(DEFAULT_CONFIG_RETRY) {

  config_collector = new MtConfigCollector();
}

FileConfigClient::~FileConfigClient() {
  EventLoopThreadProxyDestroyer(loop_thread_);
  SAFE_DELETE(mFileConfigBufferMgr);
  SAFE_DELETE(config_collector);
  SAFE_DELETE(mFileConfigClient);
}

FileConfigClient *FileConfigClient::getInstance() {
  if (NULL == mFileConfigClient) {
    pthread_mutex_lock(&m_fMutex);
    if (NULL == mFileConfigClient) {
      mFileConfigClient = new FileConfigClient();
    }
    pthread_mutex_unlock(&m_fMutex);
  }
  return mFileConfigClient;
}

void FileConfigClient::Destroy() {
  SAFE_DELETE(mFileConfigClient);
}

int FileConfigClient::init(int timeout, int retry) {
  m_timeout = timeout;
  m_retry = retry;

  XMLDocument conf;
  tinyxml2::XMLError eRes = conf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (unlikely(tinyxml2::XML_SUCCESS != eRes)) {
    LOG_FATAL("failed to load config file: " << SG_AGENT_MUTABLE_CONF);
    return ERR_CONFIG_LOAD_CONF_FAIL;
  }

  const char *fileConfigPath =
      conf.FirstChildElement("SGAgentMutableConf")
          ->FirstChildElement("FileConfigPath")->GetText();

  m_basepath = fileConfigPath;
  LOG_INFO("file basepath = " << m_basepath);

  mFileConfigBufferMgr = new BufferMgr<ConfigFile>();

  //初始化mtconfig-server
  LOG_INFO("start to init config_collector");
  int ret = config_collector->init();
  if (ret != 0) {
    //mtconfig-server初始化失败，直接报警，不退出sg_agent_worker
    LOG_FATAL("Init MtConfigCollector failed! ret = " << ret);
    return ret;
  }
  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return FAILURE;
  }
  // 启动backend处理线程
  loop_ = loop_thread_->startLoop();
  return 0;
}
//sg notify下发配置文件接口
int FileConfigClient::notifyIssued(const file_param_t &notice) {
  int ret = 0;
  for (int i = 0; i < notice.configFiles.size(); ++i) {
    std::string filepath = notice.configFiles[i].filepath;
    if (filepath.empty()) {
      filepath = m_basepath + notice.appkey;
    }
    //计算md5值并校验
    MD5 md5String(notice.configFiles[i].filecontent);
    if (notice.configFiles[i].md5 != md5String.md5()) {
      ret = ERR_FILECONFIG_MD5_WRONG; //md5值不一致;
      LOG_ERROR("notify issued md5 is not the same. Agent md5 =" << md5String.md5()
                                                                 << "notify md5 = " << notice.configFiles[i].md5);
      break;
    }

    ret = writeToTmp(notice.configFiles[i].filecontent,
                     notice.configFiles[i].filename, filepath);
    if (0 != ret) {
      LOG_ERROR("write tmpfile failed. appkey: " << notice.appkey
                                                 << ", filename: " << notice.configFiles[i].filename
                                                 << ", filepath: " << filepath
                                                 << ", ret = " << ret);
      break;
    }
  }
  return ret;
}

int FileConfigClient::notifyWork(const file_param_t &command) {
  int ret = 0;
  std::string key = "";
  ConfigFile tmpfile;
  for (int i = 0; i < command.configFiles.size(); ++i) {
    key = command.appkey + "|" + command.configFiles[i].filename;
    tmpfile.__set_filename(command.configFiles[i].filename);

    std::string filepath = command.configFiles[i].filepath;
    if (filepath.empty()) {
      filepath = m_basepath + command.appkey;
    }
    tmpfile.__set_filepath(filepath);

    ret = moveForWork(command.configFiles[i].filename, filepath);
    if (0 != ret) {
      LOG_ERROR("mv file failed. key: " << key
                                        << ", filename: " << command.configFiles[i].filename
                                        << ", filepath: " << filepath
                                        << ", ret = " << ret);
      break;
    }
    //把文件加载进内存,并进行md5校验
    ret = _loadFileToBuffer(key, tmpfile);

    LOG_DEBUG("notify work file. key = " << key
                                         << " filepath = " << tmpfile.filepath
                                         << " md5 = " << tmpfile.md5);
  }
  return ret;
}

int FileConfigClient::getFileConfig(file_param_t &returnFile, const file_param_t &node) {
  if (NULL == mFileConfigBufferMgr) {
    LOG_ERROR("file config BufferManager is NULL");
    returnFile.__set_err(ERR_FILECONFIG_BUFFER_NULL);
    return ERR_FILECONFIG_BUFFER_NULL;
  }

  if (node.configFiles.empty()) {
    LOG_DEBUG("file name cannot be empty.");
    returnFile.__set_err(ERR_INVALID_PARAM);
    return ERR_INVALID_PARAM;
  }
  LOG_DEBUG("getFileConfig filename = " << node.configFiles[0].filename);

  int ret = 0;
  std::string key = "";
  returnFile.appkey = node.appkey;

  ConfigFile tmpfile;
  std::vector<ConfigFile> tmpfiles;
  //暂时在agent这边固定路径管理
  std::string path = m_basepath + node.appkey;
  tmpfile.__set_filepath(path);

  for (int i = 0; i < node.configFiles.size(); ++i) {
    tmpfile.filename = node.configFiles[i].filename;
    key = node.appkey + "|" + node.configFiles[i].filename;

    ret = mFileConfigBufferMgr->get(key, tmpfile);
    if (0 == ret) {
      int count = 0;
      static InvokeStat hitBuffStat;
      if (InvokeStat::GetInvokeStatInfo(hitBuffStat, count)) {
        LOG_STAT("succeed to get config from mFileConfigBufferMgr, count = "
                     << count);
      }
      //md5值比较
      if (tmpfile.md5 == node.configFiles[i].md5) {
        int err_md5 = ERR_FILECONFIG_MD5_SAME;
        tmpfile.__set_err_code(err_md5);
      }
    } else {
      //保证agent重启,先去读取本地配置文件
      ret = _loadFileToBuffer(key, tmpfile);
      if (0 != ret) {
        LOG_ERROR("_loadFileToBuffer failed! configFile. key = " << key
                                                                 << ", filename = " << tmpfile.filename
                                                                 << ", filepath = " << tmpfile.filepath
                                                                 << ", md5 = " << tmpfile.md5
                                                                 << ", ret = " << ret);
      }
    }

    LOG_DEBUG("get config file. key = " << key <<
                                        " filepath = " << tmpfile.filepath <<
                                        " md5 = " << tmpfile.md5);

    tmpfiles.push_back(tmpfile);
    returnFile.__set_configFiles(tmpfiles);
  }
  //只有当新机器重启时，才会向worker已有的配置文件
  if (0 != ret) {
    LOG_WARN("get file from worker(happen rarely). appkey: " << returnFile.appkey);
    ret = getFileConfigFromWorker(returnFile);
    if (0 != ret) {
      LOG_ERROR("getFileConfigFromWorker failed! appkey: " << returnFile.appkey
                                                           << ", ret = " << ret);
    }
  }

  return ret;
}
//从mcc server获取配置文件
int FileConfigClient::getFileConfigFromWorker(file_param_t &node) {
  int ret = 0;

  node.__set_ip(g_global_var->gIp);
  node.__set_key(_genKey(node) + boost::lexical_cast<std::string>(time(0)));
  LOG_INFO("mcc group's IP = " << node.ip << ", key = " << node.key);

  //snd msg to worker
  FileConfigContextPtr context(
      new TaskContext<file_param_t, file_param_t>(node));
  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetFileConfigQueueSize(pending_tasks_size);

  boost::shared_ptr<FileConfigHandler> task(new FileConfigHandler(this, context));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_WARN("file config backend thread overload, task queue size: "
                 << pending_tasks_size);
    // TODO: 设置合理的错误码
    ret = BackendErrorToFrontError(ERR_GETCONFIG_TIMEOUT);
    node.__set_err(ret);
    return ret;
  }

  //wait for return
  context->WaitResult(m_timeout);
  const file_param_t *rsp = context->get_response();

  if (NULL == rsp) {
    LOG_INFO("don't get response in time, key = " << node.key);
    ret = BackendErrorToFrontError(ERR_GETCONFIG_TIMEOUT);
    node.__set_err(ret);
    return ret;
  }

  if (MTCONFIG_OK != rsp->err) {
    LOG_ERROR("get fileconfig not succeed, errcode = " << rsp->err);
    ret = BackendErrorToFrontError(rsp->err);
    node.__set_err(ret);
    return ret;
  }

  ret = notifyIssued(*rsp);
  if (0 == ret) {
    ret = notifyWork(*rsp);
    if (0 != ret) {
      LOG_ERROR("notifyWork failed! key = " << rsp->key
                                            << ", ret = " << ret);
    } else {
      LOG_INFO("notifyWork sucess! key = " << rsp->key
                                           << ", ret = " << ret);
    }
  } else {
    LOG_ERROR("notifyIssued failed! key = " << rsp->key
                                            << ", ret = " << ret);
    ret = BackendErrorToFrontError(ret);
    node.__set_err(ret);
    node.__set_configFiles(rsp->configFiles);
    return ret;

  }
  //操作频率低，打印信息，进行分析处理
  LOG_INFO("write file to local from worker! key = " << rsp->key
                                                     << ", ret = " << ret);

  ret = BackendErrorToFrontError(rsp->err);
  node.__set_err(ret);
  node.__set_configFiles(rsp->configFiles);

  return ret;
}
//配置文件加载成功单配置文件内容为空不更新缓存
int FileConfigClient::_loadFileToBuffer(const std::string &key, ConfigFile &file) {
  if (NULL == mFileConfigBufferMgr) {
    LOG_ERROR("_loadFileToBuffer file config BufferManager is NULL");
    return ERR_FILECONFIG_BUFFER_NULL;
  }
  //把生效文件内容写入file
  std::string filecontent = "";
  int ret = loadFile(filecontent, file.filename, file.filepath);
  if (0 == ret) {
    file.__set_filecontent(filecontent);
    //计算md5值
    MD5 md5String(file.filecontent);
    file.__set_md5(md5String.md5());
    //写入缓存
    ret = mFileConfigBufferMgr->insert(key, file);
  }
  return ret;
}

void FileConfigClient::FileConfigBackendHandler(FileConfigContextPtr context) {
  const file_param_t *request = context->get_request();
  if (NULL == request) {
    LOG_ERROR("don't set request when get file config.");
    return;
  }

  //根据消息类型，进行业务逻辑处理
  file_param_t res_msg;
  //解析msg
  res_msg.__set_appkey(request->appkey);
  res_msg.__set_path(request->path);
  res_msg.__set_cmd(request->cmd);
  res_msg.__set_configFiles(request->configFiles);
  res_msg.__set_ip(request->ip);
  res_msg.__set_key(request->key);

  if ("prod" != request->env && ZkTools::operation_.IsAllEnvFileCfgAppkeys(request->appkey)) {
    // check the appkey is in white list or not.
    res_msg.__set_env("prod");
    LOG_INFO("file cfg env is changed from " << request->env << " to prod");
  } else if (request->env.empty()) {
    res_msg.__set_env(g_global_var->gEnvStr);
  } else {
    res_msg.__set_env(request->env);
  }

  int ret = config_collector->getFileConfig(res_msg);
  //返回操作结果给sg_agent前端
  if (ret != sg_agent::MTCONFIG_OK) {
    LOG_ERROR("operate Config fail , appkey : " << res_msg.appkey
                                                << ", env: " << res_msg.env
                                                << ", path: " << res_msg.path
                                                << ", ret : " << ret);
  }
  //封装返回错误码
  res_msg.__set_err(ret);

  context->set_response(res_msg);
}

std::string FileConfigClient::_genKey(const file_param_t &node) {
  std::string env = node.env;
  if (env.empty()) {
    env = ENVNAME[g_global_var->gEnv - 1];
  }
  std::string key = node.appkey + "+" + env; // + "+" + node.path;
  return key;
}
int FileConfigClient::ConvertToSyncNode(const ConfigFile &param,
                                        FileConfigSyncRequest &node,
                                        const std::string &appKey) {

  if (param.filepath.empty() || param.filename.empty() || appKey.empty()) {
    LOG_ERROR("param is not complete"
                  << ", filepath = " << param.filepath
                  << ", filename = " << param.filename
                  << ", appKey = " << appKey);
    return ERR_PARAMNOTCOMPLETE;
  }
  node.__set_appkey(appKey);
  node.__set_groupId("");  //目前暂填为空，由server侧进行逻辑处理
  node.__set_path(param.filepath);
  node.__set_env(g_global_var->gEnvStr);
  node.__set_ip(g_global_var->gIp);

  return SUCCESS;
}

int FileConfigClient::GetAppkeyFromKey(std::string &key, std::string &appKey) {

  if (key.empty()) {
    return FAILURE;
  } else {
    size_t pos = key.find('|');
    if (std::string::npos == pos) {
      LOG_ERROR("the valid key position is not exist");
      return FAILURE;
    }
    std::string keyTmp = "";
    keyTmp.assign(key.c_str(), pos);
    appKey = keyTmp;

    LOG_INFO("get appkey from mFileConfigBuffer is" << appKey);

    return SUCCESS;
  }
}

void FileConfigClient::SyncConfigPeriodicTimer() {

  if (NULL == mFileConfigBufferMgr) {
    LOG_ERROR("mFileConfigBufferMgr is null, nothing to sync");
    return;
  }
  ConfigFile srcConfigNode;
  FileConfigSyncRequest syncRequest;
  FileConfigSyncResponse syncResponse;
  std::vector<std::string> keyList;
  std::vector<std::string>::iterator iter;
  std::string key, appKey;
  //由于fileconfig未存key信息，须获取缓存中所有key值，因key包含appkey信息
  mFileConfigBufferMgr->GetKeyList(keyList);
  if (keyList.empty()) {
    LOG_INFO("the key list is null");
    return;
  }

  for (iter = keyList.begin();
       iter != keyList.end(); ++iter) {
    key = *iter;
    int ret = mFileConfigBufferMgr->get(key, srcConfigNode);
    if (unlikely(ret != 0)) {
      LOG_ERROR("failed to get key: " << key);
      continue;
    }
    if (GetAppkeyFromKey(key, appKey) < 0) {
      if (unlikely(ret != 0)) {
        LOG_ERROR("failed to get appkey from key: " << key);
        continue;
      }
    }

    ret = ConvertToSyncNode(srcConfigNode, syncRequest, appKey);   //convert to syncRequest
    if (unlikely(0 != ret)) {
      LOG_ERROR("failed to ConvertToSyncNode, key = " << key);
      continue;
    }
    LOG_INFO("sync node appKey is: " << appKey << ",filepath: " << syncRequest.path);
    config_collector->SyncFileConf(syncResponse, syncRequest);
  }
}

int FileConfigClient::BackendErrorToFrontError(int error) {
  switch (error) {
    case 0:
    case 200: return SUCCESS;
    case 302: return ERR_NO_CHANGE;
    case 500: return ERR_UNKNOE_ERROR;
    case 501: return ERR_PARAM_ERROR;
    case 502: return ERR_NODE_NOT_EXIST;
    case 503: return ERR_NOT_EXIST_VERSION;
    case 504: return ERR_DEPRECATED_VERSION;
    case ERR_GETCONFIG_TIMEOUT: return ERR_GETCONFIG_TIMEOUT;
    default: return ERR_GETCONFIG_TIMEOUT;
  }
}

}
