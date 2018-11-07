//
// Created by Chao Shu on 16/4/21.
//
#include <algorithm>

#include <boost/algorithm/string.hpp>
#include <boost/filesystem.hpp>
#include <boost/tokenizer.hpp>
#define BOOST_SPIRIT_THREADSAFE
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/foreach.hpp>

#include <rapidjson/document.h>
#include <rapidjson/stringbuffer.h>

#include "../cthrift_sgagent.h"
#include "../cthrift_client.h"

#include "config_file.h"
#include "config_dynamic.h"
#include "mcc_sdk.h"

namespace bfs = boost::filesystem;

using namespace cthrift;
using namespace mcc_sdk;
using namespace std;

/*muduo::AsyncLogging g_mcc_sdk_log("mcc_sdk", 500 * 1024 * 1024);
muduo::AsyncLogging *g_mccsdk_asyncLog = 0;

void MCCSDKAsyncOutput(const char *msg, int len) {
  g_mccsdk_asyncLog->append(msg, len);
}*/

namespace mcc_sdk {
const string kStrDiskFileDir = "/opt/meituan/config_snapshot/static";
const string kStrDiskZKDir = "/opt/meituan/config_snapshot/dynamic";

muduo::MutexLock file_mutex, zk_mutex, init_mutex;

muduo::net::EventLoop *mcc_eventloop_p_ = NULL;

int ref_count_ = 0;

boost::shared_ptr<muduo::net::EventLoopThread> mcc_worker_thread_sp_;

typedef muduo::ThreadLocalSingleton <boost::shared_ptr<CthriftClient> >
    ThreadLocalSingletonCthriftClientSharedPtr;

typedef muduo::ThreadLocalSingleton <boost::shared_ptr<SGAgentClient> >
    ThreadLocalSingletonSGAgentClientSharedPtr;

//get conf data from file
muduo::ThreadLocal<boost::unordered_map<string, FileCfgInfo> >
        g_map_appkey_filename_to_filecfginfo; //key="appkey:filename"

void InvokeGetFileCfg(const string &str_appkey, const string
&str_file_name, string *p_str_file_content = 0, string *p_err_info = 0,
                      muduo::CountDownLatch *p_countdown_filecfg = 0);

void DoGetFileCfg(const string *p_str_appkey,
                  const string *p_str_file_name,
                  string *p_str_file_content,
                  string *p_err_info,
                  muduo::CountDownLatch *p_countdown_filecfg);

int8_t GetFileCfg(const string &str_appkey, const string
&str_file_name, string *p_str_file_content, string *p_err_info);

void DoSetFileConfigCallbackFunc(const string &str_appkey, const string
&str_file_name, const FileConfigCallback &file_cfg_cb);

void LoadDiskFileCfg(void);

void RegularCheckFileCfg(void);
//end get conf data from file

//get conf data from zk
muduo::ThreadLocal<boost::unordered_map<string, ZKCfgInfo> >
        g_map_appkey_to_zkcfginfo;

void InvokeGetZKCfg(const string &str_appkey,
                    const string &key = "",
                    string *p_str_value = 0,
                    string *p_err_info = 0,
                    map<string, string> *p_map_conf = 0,
                    muduo::CountDownLatch *p_countdown_zkcfg = 0);

void InvokeSetCfg(const string &str_appkey,
                  const string &str_key,
                  const string &str_value,
                  const string &token,
                  string *p_str_err_info,
                  muduo::CountDownLatch *p_countdown_zkcfg);

void DoGetZKCfg(const string &str_appkey,
                const string &str_key,
                string *p_str_value,
                string *p_err_info,
                map<string, string> *p_map_conf,
                muduo::CountDownLatch *p_countdown_zkcfg);

void DoSetZKGlobalConfigCallbackFunc(const string &str_appkey,
                                     const GlobalConfigCallback& zk_g_cfg_cb);

void DoAddZKConfigCallbackFunc(const string &str_appkey,
                               const string &key,
                               const ConfigCallback& zk_cfg_cb);

void LoadDiskZKCfg(void);
void RegularCheckZKCfg(void);
//end get conf data from zk



void InitCthriftClient(const int32_t &i32_reqest_timeout_ms,
                       const int32_t &i32_loop_interval_ms);


//get conf from file
void InvokeGetFileCfg(const string &str_appkey,
                      const string &str_file_name,
                      string *p_str_file_content,
                      string *p_err_info,
                      muduo::CountDownLatch *p_countdown_filecfg) {
  if (CTHRIFT_UNLIKELY(!(
      (!p_str_file_content && !p_err_info && !p_countdown_filecfg)
          || (p_str_file_content && p_err_info && p_countdown_filecfg)))) {
    CLOG_STR_ERROR("input invalid");
    return;
  }

  string str_err_info;

  /*file_param_t file_cfg_req;
  file_cfg_req.appkey.assign(str_appkey);

  ConfigFile cfg_file;
  cfg_file.filename.assign(str_file_name);
  file_cfg_req.configFiles.push_back(cfg_file);*/

  //应该直接使用sg_agent提供的__set接口填充通讯数据；手动构造对象会缺失占位符，
  file_param_t file_cfg_req;
  file_cfg_req.__set_appkey(str_appkey);
  file_cfg_req.__set_path("/");

  ConfigFile cfg_file;
  cfg_file.filename.assign(str_file_name);

  vector<ConfigFile> cfg_file_list;
  cfg_file_list.push_back(cfg_file);
  file_cfg_req.__set_configFiles(cfg_file_list);

  file_param_t file_cfg_rsp;
  try {
    (ThreadLocalSingletonSGAgentClientSharedPtr::instance())
        ->getFileConfig(file_cfg_rsp, file_cfg_req);
  } catch (TException &tx) {
    str_err_info.assign("getFileConfig error " + string(tx.what()));
    CLOG_STR_ERROR(str_err_info);

    if (p_err_info) {
      p_err_info->assign(str_err_info);   //already assure safe together
      p_countdown_filecfg->countDown();
    }

    return;
  }

  if (CTHRIFT_UNLIKELY(file_cfg_rsp.err)) {
    string str_err_code;
    try {
        str_err_code = boost::lexical_cast<string>(file_cfg_rsp.err);
    } catch (boost::bad_lexical_cast & e) {
      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                     << "Convertion SGAgent response error code failed: " << file_cfg_rsp.err);
      return;
    }
    str_err_info.assign("sgagent return err: " + str_err_code);
    CLOG_STR_ERROR(str_err_info);

    if (p_err_info) {
      p_err_info->assign(str_err_info); //already assure safe together
      p_countdown_filecfg->countDown();
    }

    return;
  }

  vector<ConfigFile>::const_iterator it = file_cfg_rsp.configFiles.begin();
  while (file_cfg_rsp.configFiles.end() != it) {
    if (it->filename == str_file_name) {
      if (CTHRIFT_UNLIKELY(it->err_code)) {
        string str_err_code;
        try {
          str_err_code = boost::lexical_cast<string>(it->err_code);
        } catch (boost::bad_lexical_cast & e) {
          CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                         << "Convertion configFile's err_code failed: " << it->err_code);
          return;
        }
        str_err_info.assign(
            "file_param_t err 0, but in config, err: " + str_err_code);
        CLOG_STR_ERROR(str_err_info);

        if (p_err_info) {
          p_err_info->assign(str_err_info);
          p_countdown_filecfg->countDown();
        }

        return;
      }

      if (p_str_file_content) {
        p_str_file_content->assign(it->filecontent);
        p_countdown_filecfg->countDown();  //CANNOT use input appkey, filename in case mem conflict
      }

      //update cache,disk file, invoke callback
      FileCfgInfo &file_cfg_info =
              (g_map_appkey_filename_to_filecfginfo.value())[file_cfg_rsp.appkey + ":"
              + it->filename];  //if no, create one
      file_cfg_info.UpdateFileCfg(file_cfg_rsp.appkey, *it);

      return;
    }

    ++it;
  }

  str_err_info.assign(
      "No file name: " + str_file_name + " under appkey: " + str_appkey);
  CLOG_STR_ERROR(str_err_info);

  if (p_err_info) {
    p_err_info->assign(str_err_info);
    p_countdown_filecfg->countDown();
  }
}

void DoGetFileCfg(const string *p_str_appkey,
                  const string *p_str_file_name,
                  string *p_str_file_content,
                  string *p_err_info,
                  muduo::CountDownLatch *p_countdown_filecfg) {
   boost::unordered_map<string, FileCfgInfo>::const_iterator it =
           (g_map_appkey_filename_to_filecfginfo.value()).find(
          *p_str_appkey + ":" + *p_str_file_name);
 if (CTHRIFT_LIKELY((g_map_appkey_filename_to_filecfginfo.value()).end() != it&&
                            !(((it->second).cfg_file).filecontent.empty()))) {
    CLOG_STR_DEBUG("find config in cache");
    p_str_file_content->assign(((it->second).cfg_file).filecontent);

    p_countdown_filecfg->countDown();
  } else {
    CLOG_STR_DEBUG("cache miss, invoke sgagent");
    InvokeGetFileCfg(*p_str_appkey,
                     *p_str_file_name,
                     p_str_file_content,
                     p_err_info, p_countdown_filecfg); //will countdown inside
  }
}

int8_t GetFileCfg(const string &str_appkey, const string
&str_file_name, string *p_str_file_content, string *p_err_info) {
  bool b_appkey_empty = str_appkey.empty();
  bool b_filename_empty = str_file_name.empty();
  bool b_file_content_null = (NULL == p_str_file_content);
  bool b_err_info_null = (NULL == p_err_info);

  if (CTHRIFT_UNLIKELY(
      b_appkey_empty || b_filename_empty || b_file_content_null
          || b_err_info_null)) {
    string str_err_info("input appkey empty: " + (b_appkey_empty ?
                                                       string("true;") : string(
            "false;"))
                            + " input filename empty: "
                            + (b_filename_empty ?
                               string("true;") : string("false;"))
                            + " input file content pointer null: "
                            + (b_file_content_null
                               ?
                               string("true;") :
                               string("false;"))
                            + " input err info pointer null: "
                            + (b_err_info_null ?
                               string("true.") : string("false.")));
    if (!b_err_info_null) {
      p_err_info->assign(str_err_info);
    }

    CLOG_STR_ERROR(str_err_info);
    return -1;
  }

  CLOG_STR_DEBUG("str_appkey: " << str_appkey << " str_file_name: "
                << str_file_name);
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    muduo::CountDownLatch countdown_get_filecfg(1);
    mcc_eventloop_p_->runInLoop(boost::bind(&DoGetFileCfg,
                                            &str_appkey,
                                            &str_file_name,
                                            p_str_file_content,
                                            p_err_info,
                                            &countdown_get_filecfg)); //use pointer for performance

    countdown_get_filecfg.wait();
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }
  return p_err_info->empty() ? 0 : -1;
}

void RegularCheckFileCfg(void) {

  string str_appkey;
  string str_file_name;

  boost::unordered_map<string, FileCfgInfo>::const_iterator
      it = (g_map_appkey_filename_to_filecfginfo.value()).begin(); //may change member
  // content, but NOT change map itself
  while ((g_map_appkey_filename_to_filecfginfo.value()).end() != it) {
    str_appkey.clear();
    str_file_name.clear();

    //fetch appkey:filename seperately

    //boost::tokenizer本身未说明异常捕获方式，使用try防止未知异常出现。
      try {
          static const boost::char_separator<char> sep(":");
          boost::tokenizer<boost::char_separator<char> > custom_tokenizer
                  (it->first, sep);

          boost::tokenizer<boost::char_separator<char> >::const_iterator
                  it_token = custom_tokenizer.begin();
          int8_t i8_count = 0;
          while (custom_tokenizer.end() != it_token) {
              //bug: when next timer callback, the static variable(i8_count) will keep last value
              //static int8_t i8_count = 0;
              switch (i8_count) {
                  case 0:
                      str_appkey.assign(*it_token);
                      break;
                  case 1:
                      str_file_name.assign(*it_token);
                      break;
                  default:
                    CLOG_STR_ERROR("token size > 2, member: " << *it_token); //ONLY
                      // appkey:filename, just for safe
                      break;
              }
              ++i8_count;
              ++it_token;
          }
      } catch (...) {
        CLOG_STR_ERROR("boost::tokenizer cause exception, but can not get exception info.");
      }

    if (CTHRIFT_UNLIKELY(str_appkey.empty() || str_file_name.empty())) {
      CLOG_STR_ERROR("appkey " << str_appkey << " filename " << str_file_name);
      //key miss

      ++it;
      continue;
    }

    CLOG_STR_DEBUG("str_appkey " << str_appkey << " str_file_name " <<
              str_file_name);

    InvokeGetFileCfg(str_appkey, str_file_name); //ignore return

    ++it;
  }
}

void DoSetFileConfigCallbackFunc(const string &str_appkey, const string
&str_file_name, const FileConfigCallback &file_cfg_cb){
  boost::unordered_map<string, FileCfgInfo> &g_cache_file =
          g_map_appkey_filename_to_filecfginfo.value();
  string str_key(str_appkey + ":" + str_file_name);
  if (g_cache_file.end() == g_cache_file.find(str_key)) {
    InvokeGetFileCfg(str_appkey, str_file_name);
  }
  g_cache_file[str_key].SetFileCfgCallback(file_cfg_cb);
}

int8_t SetFileConfigCallbackFunc(const string &str_appkey, const string
&str_file_name, const FileConfigCallback &file_cfg_cb, string *p_str_err_info){
  bool b_appkey_empty = str_appkey.empty();
  bool b_file_name_empty = str_file_name.empty();
  bool b_err_info_null = (NULL == p_str_err_info);

  if(CTHRIFT_UNLIKELY(b_appkey_empty || b_file_name_empty || b_err_info_null)){
    string str_err_info("input appkey empty " + (b_appkey_empty ? string("true;") : string("false;")) + " input filename empty " + (b_file_name_empty ? string("true;") : string("false;")) + " input err info pointer null " + (b_err_info_null ? string("true.") : string("false.")));
    CLOG_STR_ERROR(str_err_info);
    if(!b_err_info_null){
      p_str_err_info->assign(str_err_info);
    }

    return -1;
  }
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    mcc_eventloop_p_->runInLoop(boost::bind(&DoSetFileConfigCallbackFunc,
                                            str_appkey,
                                            str_file_name, file_cfg_cb));
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }
  return 0;
}


void LoadDiskFileCfg(void) {
  bfs::path full_path(
      kStrDiskFileDir,
      bfs::native);

  if (CTHRIFT_UNLIKELY(
      !(bfs::exists(full_path)) && !(bfs::create_directories(kStrDiskFileDir
      )))) {
    CLOG_STR_ERROR("disk dir " << kStrDiskFileDir << " NOT exist, and "
            "create failed");
    return;
  }
  bfs::path current_path;
  for (bfs::recursive_directory_iterator rd_it(kStrDiskFileDir), rd_end;
       rd_end != rd_it; ++rd_it) {
    try {
      if (CTHRIFT_LIKELY(!(bfs::is_directory(*rd_it)))) {
        current_path = rd_it->path();
        CLOG_STR_INFO("Load file " << current_path.string());
        //TODO
        ifstream input_stream;
		try {
			input_stream.open((current_path.string()).c_str(), ios::in);
		} catch (const ifstream::failure& e) {
            CLOG_STR_ERROR("Exception opening config file: " << (current_path.string()).c_str());
			continue;
		}
		if (CTHRIFT_UNLIKELY(!input_stream.is_open())) {
			continue;
		}
		if (CTHRIFT_UNLIKELY(input_stream.bad())) {
            CLOG_STR_ERROR("Reading config file failed: " << (current_path.string()).c_str());
			input_stream.close();
			continue;
		}
		string cfg_file_all((istreambuf_iterator<char>(input_stream)),
                                 (istreambuf_iterator<char>()));
        //cfg_file_md5, cfg_file_content;
		//input_stream >> cfg_file_md5 >>  cfg_file_content;
		input_stream.close();
        size_t pos = cfg_file_all.find_first_of(" ");

		string str_appkey = current_path.parent_path().filename();
		string str_filename = current_path.filename();
        CLOG_STR_INFO("appkey: " << str_appkey << " filename " << str_filename);

        ConfigFile load_cfg_file;
        load_cfg_file.__set_md5(cfg_file_all.substr(0, pos));
        load_cfg_file.__set_filecontent(cfg_file_all.substr(pos+1));
        (g_map_appkey_filename_to_filecfginfo.value())[str_appkey + ":"
			+ str_filename].cfg_file = load_cfg_file;
      }
    } catch (const exception &ex) {
      CLOG_STR_ERROR("recursive file failed: " << ex.what());
    }
  }
}


//operation from dynamic configure

void InvokeSetCfg(const string &str_appkey,
                  const string &str_key,
                  const string &str_value,
                  const string &token,
                  string *p_str_err_info,
                  muduo::CountDownLatch *p_countdown_cfg) {
  rapidjson::Document jsonDoc;
  jsonDoc.SetObject();
  rapidjson::Document::AllocatorType& allocator = jsonDoc.GetAllocator();
  jsonDoc.AddMember(rapidjson::Value(str_key.c_str(), allocator).Move(),
                    rapidjson::Value(str_value.c_str(), allocator).Move(),
                    allocator);
  rapidjson::StringBuffer buffer;
  rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
  jsonDoc.Accept(writer);

  const char* bufJson = buffer.GetString();
  string configJson(bufJson);

  proc_conf_param_t zk_cfg_req;
  zk_cfg_req.__set_appkey(str_appkey);
  zk_cfg_req.__set_path("/");
  zk_cfg_req.__set_conf(configJson);
  zk_cfg_req.__set_token(token);

  string str_err_info;
  int retCode = 0;
  try {
    retCode = (ThreadLocalSingletonSGAgentClientSharedPtr::instance())
            ->setConfig(zk_cfg_req);
  } catch (TException &tx) {
    str_err_info.assign("setConfig catch a exception: " + string(tx.what()));
    CLOG_STR_ERROR(str_err_info);
  }

  if (0 != retCode) {
    str_err_info.assign("setConfig error, sg_agent return code: " + boost::lexical_cast<string>(retCode));
    CLOG_STR_ERROR(str_err_info);
  }
  if (p_str_err_info) {
    p_str_err_info->assign(str_err_info);   //already assure safe together
  }
  p_countdown_cfg->countDown();
}

void InvokeGetZKCfg(const string &str_appkey,
                    const string &str_key,
                    string *p_str_value,
                    string *p_err_info,
                    map<string, string> *p_map_conf,
                    muduo::CountDownLatch *p_countdown_cfg) {
if (CTHRIFT_UNLIKELY(!(
        (!p_str_value && !p_err_info && !p_map_conf && !p_countdown_cfg)
        || ((p_str_value || p_map_conf) && p_err_info && p_countdown_cfg)))) {
    CLOG_STR_ERROR("input invalid");
    return;
}

  proc_conf_param_t zk_cfg_req;
  zk_cfg_req.__set_appkey(str_appkey);
  zk_cfg_req.__set_path("/");
  string zk_cfg_rsp, str_err_info;
  try {
    (ThreadLocalSingletonSGAgentClientSharedPtr::instance())
            ->getConfig(zk_cfg_rsp, zk_cfg_req);
  } catch (TException &tx) {
    str_err_info.assign("getZKConfig error: " + string(tx.what()));
    CLOG_STR_ERROR(str_err_info);

    if (p_err_info) {
      p_err_info->assign(str_err_info);   //already assure safe together
      p_countdown_cfg->countDown();
    }

    return;
  }

  if (!zk_cfg_rsp.empty()) {
    ZKCfgInfo &zk_cfg_info =
            (g_map_appkey_to_zkcfginfo.value())[str_appkey];
    bool b_persist = zk_cfg_info.UpdateCfg(str_appkey, zk_cfg_rsp);

    //return value to client, before persist data to disk.
    if (p_str_value) {
        if (zk_cfg_info.map_key_value.end() != zk_cfg_info.map_key_value.find(str_key)) {
      		p_str_value->assign(zk_cfg_info.map_key_value[str_key]);
	} else if (p_err_info){
		p_err_info->assign("appkey: " + str_appkey + " doesn't contain the key: " + str_key);		
	}
    } else if (p_map_conf){
        *p_map_conf = zk_cfg_info.map_key_value;
    }
    if (p_countdown_cfg) {
      p_countdown_cfg->countDown();
    }
    //persist all info, not only conf data.
    if (b_persist) {
      zk_cfg_info.UpdateDiskFile(str_appkey, zk_cfg_rsp);
    }
    return;
  }

  str_err_info.assign("appkey: " + str_appkey + " can not get dynamic config");
  CLOG_STR_ERROR(str_err_info);

  if (p_err_info) {
    p_err_info->assign(str_err_info);
    p_countdown_cfg->countDown();
  }
}

void DoGetZKCfg(const string &str_appkey,
                const string &str_key,
                string *p_str_value,
                string *p_err_info,
                map<string, string> *p_map_conf,
                muduo::CountDownLatch *p_countdown_cfg) {
  boost::unordered_map<string, ZKCfgInfo>::const_iterator it =
          (g_map_appkey_to_zkcfginfo.value()).find(str_appkey);

  if (CTHRIFT_LIKELY(((g_map_appkey_to_zkcfginfo.value()).end()) != it)) {
    if (str_key.empty() && p_map_conf) {
      CLOG_STR_DEBUG("zk cache hit");
      *p_map_conf = it->second.map_key_value;
      CLOG_STR_DEBUG("zk map size: " << (it->second.map_key_value).size());
      p_countdown_cfg->countDown();
      return;
    } else if (it->second.map_key_value.end() != it->second.map_key_value.find(str_key) && p_str_value) {
      CLOG_STR_DEBUG("zk cache hit");
      *p_str_value = (it->second.map_key_value).at(str_key);
      p_countdown_cfg->countDown();
      return;
    }
  }
  CLOG_STR_DEBUG("zk cache miss, invoke sgagent");
  InvokeGetZKCfg(str_appkey,
                 str_key,
                 p_str_value,
                 p_err_info,
                 p_map_conf,
                 p_countdown_cfg);
}

int8_t GetCfg(const string &str_appkey, const string &str_key,
              string *p_str_value, string *p_err_info) {
  bool b_appkey_empty = str_appkey.empty();
  bool b_key_empty = str_key.empty();
  bool b_zk_value_null = (NULL == p_str_value);
  bool b_err_info_null = (NULL == p_err_info);

  if (CTHRIFT_UNLIKELY(
          b_appkey_empty || b_key_empty || b_zk_value_null
          || b_err_info_null)) {
    string str_err_info("input appkey empty: " + (b_appkey_empty ?
                                                       string("true;") : string(
                    "false;"))
                        + " input key empty: "
                        + (b_key_empty ?
                           string("true;") : string("false;"))
                        + " input file content pointer null: "
                        + (b_zk_value_null
                           ?
                           string("true;") :
                           string("false;"))
                        + " input err info pointer null: "
                        + (b_err_info_null ?
                           string("true.") : string("false.")));
    if (!b_err_info_null) {
      p_err_info->assign(str_err_info);
    }
    CLOG_STR_ERROR(str_err_info);
    return -1;
  }
  CLOG_STR_DEBUG("str_appkey: " << str_appkey << " key: "
                                << str_key);
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    muduo::CountDownLatch countdown_get_cfg(1);
    map<string, string> *null_ptr = NULL;
    mcc_eventloop_p_->runInLoop(boost::bind(&DoGetZKCfg,
                                            str_appkey,
                                            str_key,
                                            p_str_value,
                                            p_err_info,
                                            null_ptr,
                                            &countdown_get_cfg)); //use pointer for performance

    countdown_get_cfg.wait();
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }

  return p_err_info->empty() ? 0 : -1;
}

int8_t GetGlobalCfg(const string &str_appkey,
                map<string, string> *p_map_conf,
                string *p_err_info) {
  bool b_appkey_empty = str_appkey.empty();
  bool b_zk_value_null = (NULL == p_map_conf);
  bool b_err_info_null = (NULL == p_err_info);

  if (CTHRIFT_UNLIKELY(
          b_appkey_empty || b_zk_value_null
          || b_err_info_null)) {
    string str_err_info("input appkey empty: " + (b_appkey_empty ?
                                                       string("true;") : string(
                    "false;"))
                        + " input map pointer null: "
                        + (b_zk_value_null
                           ?
                           string("true;") :
                           string("false;"))
                        + " input err info pointer null: "
                        + (b_err_info_null ?
                           string("true.") : string("false.")));
    if (!b_err_info_null) {
      p_err_info->assign(str_err_info);
    }

    CLOG_STR_ERROR(str_err_info);
    return -1;
  }
  CLOG_STR_DEBUG("get all zk conf data, str_appkey: " << str_appkey);
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    muduo::CountDownLatch countdown_get_cfg(1);
    string *null_ptr = NULL;
    mcc_eventloop_p_->runInLoop(boost::bind(&DoGetZKCfg,
                                            str_appkey,
                                            "",
                                            null_ptr,
                                            p_err_info,
                                            p_map_conf,
                                            &countdown_get_cfg)); //use pointer for performance

    countdown_get_cfg.wait();
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }

  return p_err_info->empty() ? 0 : -1;
}

void RegularCheckZKCfg(void) {

  boost::unordered_map<string, ZKCfgInfo>::iterator
          it = (g_map_appkey_to_zkcfginfo.value()).begin();
  //may change member content, but NOT change map itself
  for (;(g_map_appkey_to_zkcfginfo.value()).end() != it; ++it) {
    if (CTHRIFT_UNLIKELY(it->first.empty())) {
      CLOG_STR_ERROR("appkey " << it->first << " empty");
      continue;
    }

    CLOG_STR_DEBUG("str_appkey " << it->first << " begin to check update.");
    InvokeGetZKCfg(it->first);
  }
}

void DoSetZKGlobalConfigCallbackFunc(const string &str_appkey,
                                     const GlobalConfigCallback& zk_g_cfg_cb) {
   boost::unordered_map<string, ZKCfgInfo> &g_cache = g_map_appkey_to_zkcfginfo.value();
   if (g_cache.end() == g_cache.find(str_appkey)) {
     InvokeGetZKCfg(str_appkey);
   }
   g_cache[str_appkey].SetGolbalCfgCallback(zk_g_cfg_cb);
}

void DoAddZKConfigCallbackFunc(const string &str_appkey,
                               const string &key,
                               const ConfigCallback& zk_cfg_cb) {
  boost::unordered_map<string, ZKCfgInfo> &g_cache = g_map_appkey_to_zkcfginfo.value();
  if (g_cache.end() == g_cache.find(str_appkey) ||
          g_cache[str_appkey].map_key_value.end() == g_cache[str_appkey].map_key_value.find(key)) {
    InvokeGetZKCfg(str_appkey);
  }
  g_cache[str_appkey].AddCfgCallback(key, zk_cfg_cb);
}

int8_t SetCfg(const string &str_appkey,
              const string &str_key,
              const string &str_value,
              const string &token,
              std::string *p_str_err_info) {
  if (CTHRIFT_UNLIKELY(str_appkey.empty() ||
        str_key.empty() || NULL == p_str_err_info)) {
    string str_err_info("input appkey empty: " +
                        (str_appkey.empty() ? string("true;") : string("false;")) +
                        " input key empty: " + (str_key.empty() ? string("true;") : string("false;")) +
                        " input err info pointer null " + (NULL == p_str_err_info ? string("true.") : string("false.")));
    CLOG_STR_ERROR(str_err_info);

    if(p_str_err_info){
      p_str_err_info->assign(str_err_info);
    }

    return -1;
  }
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    muduo::CountDownLatch countdown_set_cfg(1);
    mcc_eventloop_p_->runInLoop(boost::bind(&InvokeSetCfg,
                                            str_appkey,
                                            str_key,
                                            str_value,
                                            token,
                                            p_str_err_info,
                                            &countdown_set_cfg));
    countdown_set_cfg.wait();
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }
  return p_str_err_info->empty() ? 0 : -1;
}

int8_t SetGlobalConfigCallbackFunc(const string &str_appkey,
                                     const GlobalConfigCallback &zk_cfg_cb,
                                     string *p_str_err_info) {
  if (str_appkey.empty() || NULL == p_str_err_info) {
    string str_err_info("input appkey empty or error point is null");
    CLOG_STR_ERROR(str_err_info);

    if(p_str_err_info){
      p_str_err_info->assign(str_err_info);
    }

    return -1;
  }
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    mcc_eventloop_p_->runInLoop(boost::bind(&DoSetZKGlobalConfigCallbackFunc,
                                            str_appkey,
                                            zk_cfg_cb));
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }
  return 0;
}


int8_t AddConfigCallbackFunc(const string &str_appkey, const string &str_key,
                               const ConfigCallback &zk_cfg_cb, string *p_str_err_info) {
  bool b_appkey_empty = str_appkey.empty();
  bool b_key_empty = str_key.empty();
  bool b_err_info_null = (NULL == p_str_err_info);

  if(CTHRIFT_UNLIKELY(b_appkey_empty || b_key_empty || b_err_info_null)){
    string str_err_info("input appkey empty " +
                        (b_appkey_empty ? string("true;") : string("false;")) +
                        " input key empty " + (b_key_empty ? string("true;") : string("false;")) +
                        " input err info pointer null " + (b_err_info_null ? string("true.") : string("false.")));
    CLOG_STR_ERROR(str_err_info);

    if(!b_err_info_null){
      p_str_err_info->assign(str_err_info);
    }

    return -1;
  }
  if (CTHRIFT_LIKELY(mcc_eventloop_p_)) {
    mcc_eventloop_p_->runInLoop(boost::bind(&DoAddZKConfigCallbackFunc,
                                            str_appkey,
                                            str_key, zk_cfg_cb));
  } else {
    CLOG_STR_ERROR("Please init mcc_sdk first");
    return -1;
  }
  return 0;
}


void LoadDiskZKCfg(void) {
  bfs::path full_path(kStrDiskZKDir, bfs::native);

  if (CTHRIFT_UNLIKELY(!(bfs::exists(full_path)) &&
                       !(bfs::create_directories(kStrDiskZKDir)))) {
    CLOG_STR_ERROR("disk dir " << kStrDiskZKDir << " NOT exist, and "
            "create failed");
    return;
  }

  ZKCfgInfo load_cfg_zk;
  bfs::path current_path;
  for (bfs::recursive_directory_iterator rd_it(kStrDiskZKDir), rd_end;
       rd_end != rd_it; ++rd_it) {
    try {
      if (CTHRIFT_LIKELY(!(bfs::is_directory(*rd_it)))) {
        bfs::path current_path = rd_it->path();
        CLOG_STR_INFO("Load file " << current_path.string());
        //TODO
        boost::property_tree::ptree pt;
        try {
          boost::property_tree::read_json(current_path.string(), pt);
        } catch (boost::property_tree::ptree_error &e) {
          CLOG_STR_ERROR("parser dynamic configfile content as ini failed, error: "
                    << e.what());
          return;
        }

        load_cfg_zk.version = pt.get<int64_t>("version", 0);
		load_cfg_zk.map_key_value.clear();
        BOOST_FOREACH(boost::property_tree::ptree::value_type &v, pt.get_child("data")) {
              load_cfg_zk.map_key_value[v.first] = v.second.data();
              CLOG_STR_DEBUG("key: " << v.first << " value: " << v.second.data());
        }
        (g_map_appkey_to_zkcfginfo.value())[current_path.filename()]
                = load_cfg_zk;
        CLOG_STR_DEBUG("appkey: " << current_path.filename() << " version: "
                       << load_cfg_zk.version <<  " size: "
                       << load_cfg_zk.map_key_value.size() << " load over");
      }
    } catch (const exception &ex) {
      CLOG_STR_ERROR("recursive directory failed: " << ex.what());
    }
  }
}

//init

void InitCthriftClient(const int32_t &i32_reqest_timeout_ms,
                       const int32_t &i32_loop_interval_ms) {

  SERVER_INIT("com.sankuai.inf.mccsdk", "");

  ThreadLocalSingletonCthriftClientSharedPtr::instance() =
          boost::make_shared<CthriftClient>(CthriftSgagent::kStrSgagentAppkey,
                                            "com.sankuai.inf.mccsdk",
                                            i32_reqest_timeout_ms);

  ThreadLocalSingletonSGAgentClientSharedPtr::instance() =
          boost::make_shared<SGAgentClient>((ThreadLocalSingletonCthriftClientSharedPtr::instance())->GetCthriftProtocol());

  do{
    muduo::MutexLockGuard lock(file_mutex);
    LoadDiskFileCfg();
  }while(0);

  boost::unordered_map<string, FileCfgInfo>::const_iterator
          it = (g_map_appkey_filename_to_filecfginfo.value()).begin();
  while ((g_map_appkey_filename_to_filecfginfo.value()).end() != it) {
    CLOG_STR_DEBUG("appkey:filename " << it->first << " info: "
              << (it->second).ToString());
    ++it;
  }

  do{
    muduo::MutexLockGuard lock(zk_mutex);
    LoadDiskZKCfg();
  }while(0);

  boost::unordered_map<string, ZKCfgInfo>::const_iterator
          iter = (g_map_appkey_to_zkcfginfo.value()).begin();
  while ((g_map_appkey_to_zkcfginfo.value()).end() != iter) {
    CLOG_STR_DEBUG("appkey " << iter->first << " version "
              << (iter->second).version << " kv_size: " << (iter->second).map_key_value.size());
    ++iter;
  }
  CLOG_STR_DEBUG("Loop Done");

}

int8_t InitMCCClient(string *p_str_err_info,
                     const std::string &str_appkey,
                     const int32_t &i32_reqest_timeout_ms,
                     const int32_t &i32_loop_interval_ms){
  if (str_appkey.empty()) {
    string str_err_info("str_appkey is invalid");
    CLOG_STR_ERROR(str_err_info);
    p_str_err_info->assign(str_err_info);
    return -1;
  }

  if (0 != InitMCCClient(p_str_err_info, i32_reqest_timeout_ms, i32_loop_interval_ms)) {
    return -1;
  }

  CthriftSgagent::cat_appkey_ = str_appkey;
  pthread_once(&CthriftSgagent::cat_once_, &CthriftSgagent::InitCat);
  mcc_eventloop_p_->runEvery(60*5, boost::bind(
          &CthriftSgagent::VersionCollection, "OctoService.Version", "mcc_sdk-1.0.0"));

  return 0;
}


int8_t InitMCCClient(string *p_str_err_info,
                     const int32_t &i32_reqest_timeout_ms,
                     const int32_t &i32_loop_interval_ms) {

  CLOG_STR_DEBUG("i32_reqest_timeout_ms " << i32_reqest_timeout_ms
                                          << " i32_loop_interval_ms " << i32_loop_interval_ms);

  bool b_err_info_null = (NULL == p_str_err_info);
  bool b_reqest_timeout_invalid = (0 >= i32_reqest_timeout_ms);
  bool b_loop_interval_invalid = (0 >= i32_loop_interval_ms);
  if (CTHRIFT_UNLIKELY(
          b_err_info_null || b_reqest_timeout_invalid || b_loop_interval_invalid)) {
    string str_reqest_timeout_ms;
    string str_loop_interval_ms;

    try {
      str_reqest_timeout_ms.assign(boost::lexical_cast<string>(
              i32_reqest_timeout_ms));
      str_loop_interval_ms.assign(boost::lexical_cast<string>(
              i32_loop_interval_ms));
    } catch (boost::bad_lexical_cast &e) {
      CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                                                 << "i32_reqest_timeout_ms : " << i32_reqest_timeout_ms
                                                 << " i32_loop_interval_ms: " << i32_loop_interval_ms);
    }

    string str_err_info("err info string null: " + (b_err_info_null ?
                                                    string("true")
                                                                    :
                                                    string("false"))
                        + " request timeout invalid "
                        + str_reqest_timeout_ms
                        + " loop interval invalid: "
                        + str_loop_interval_ms);

    if (!b_err_info_null) {
      p_str_err_info->assign(str_err_info);
    }

    CLOG_STR_ERROR(str_err_info);
    return -1;
  }

  do {
    muduo::MutexLockGuard lock(init_mutex);
    if (NULL == mcc_eventloop_p_) {
      mcc_worker_thread_sp_ = boost::make_shared<muduo::net::EventLoopThread>(muduo::net::EventLoopThread::ThreadInitCallback(), "mcc_sdk");

      mcc_eventloop_p_ = mcc_worker_thread_sp_->startLoop();

      mcc_eventloop_p_->runInLoop(boost::bind(&InitCthriftClient,
                                              i32_reqest_timeout_ms,
                                              i32_loop_interval_ms));

      mcc_eventloop_p_->runEvery(static_cast<double>(i32_loop_interval_ms) / 1000.0,
                                 boost::bind(&RegularCheckFileCfg));

      mcc_eventloop_p_->runEvery(static_cast<double>(i32_loop_interval_ms) / 1000.0,
                                 boost::bind(&RegularCheckZKCfg));
    }
    ref_count_++;
  } while (0);

  return 0;
}

void DestroyMCCClient(void) {
  //仿造智能指针，使用引用计数管理资源释放。
  muduo::MutexLockGuard lock(init_mutex);
  if (0 == (--ref_count_) && mcc_eventloop_p_) {
    mcc_eventloop_p_->quit();
    usleep(500 * 1000);
    mcc_eventloop_p_ = NULL;
  }
}
}
