// =====================================================================================
// 
//       Filename:  zkclient.cpp
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时43分26秒
//       Revision:  none
// 
// =====================================================================================

#include "comm/inc_comm.h"
#include "SGAgentErr.h"
#include "zk_version_check.h"

namespace sg_agent {
ZkVersionCheck::ZkVersionCheck()
    : zk_update_time_(1800) {

  key_buffer_ = boost::shared_ptr < BufferMgr < int > > (new BufferMgr<int>());

  //生成100以内的随机数,作为随机时间，防止所有机器同一时间去访问ZK
  srand(time(0));
  //更新quota随机时间
  zk_update_rand_time_ = rand() % 100;
}

ZkVersionCheck::~ZkVersionCheck() {
}

int ZkVersionCheck::CheckZkVersion(const std::string &key, const std::string &zk_version, std::string &version) {
  int last_time = 0;
  int hasKey = key_buffer_->get(key, last_time);

  //如果版本相同，并且appkey在set集合中，则不更新;
  int curr_time = time(0);
  if ((version == zk_version) && 0 == hasKey) {
    //本地version与ZK上相同，如果超过半小时都没更新，则主动更新一次
    if ((curr_time - last_time) > (zk_update_time_ + zk_update_rand_time_)) {
      LOG_INFO("start to update zk quota data! appkey :  " << key);
    } else {
      //缓存中数据与ZK完全相同,则无需更新
      LOG_DEBUG("ZK degradeActions is the same as buf, key : " << key
                                                               << ", version : " << version);
      return ERR_ZK_LIST_SAME_BUFFER;
    }
  }
  key_buffer_->insert(key, curr_time);
  LOG_INFO("version is different from zk_version,need to update!");
  //如果ZK中版本与buf中不相同，则直接更新返回参数version
  version = zk_version;

  return SUCCESS;
}
}

