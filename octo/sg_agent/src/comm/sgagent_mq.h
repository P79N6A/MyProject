#ifndef     __SGAGENT_MQ__
#define     __SGAGENT_MQ__

#include <sys/ipc.h>
#include <sys/msg.h>
#include "log4cplus.h"
#include "util/msgparam.h"
#include "thrift_serialize.h"
#include <errno.h>
#include <string.h>

namespace sg_agent {

//添加与获取消息重试次数
#define RETRY_COUNT 3

template<class type>
class SgagentMQ {
 public:
  SgagentMQ() {
    m_iMsgQueueId = -1;
    m_bInitSucc = false;
  }

  //初始化创建消息队列
  bool init(const int iMsgQueueKey);

  //获取指定type的消息
  int getMsg(int msgType, type &req, int &len);

  //添加消息
  int setMsg(const type &reqMsg, int len);

 private:
  // 新建消息队列
  int _createMQ();
  // 删除消息队列
  int _removeMQ(const int msqid);
  //消息队列key
  int m_iMsgQueueKey;
  //消息队列qId
  int m_iMsgQueueId;
  //初始化是否成功
  bool m_bInitSucc;
};

template<class type>
bool SgagentMQ<type>::init(const int iMsgQueueKey) {
  if (m_bInitSucc)
    return true;

  m_iMsgQueueKey = iMsgQueueKey;

  if (0 != _createMQ()) {
    LOG_FATAL("failed to create MQ in init");
    return false;
  }
  m_bInitSucc = true;

  LOG_INFO("create MQ OK, iMsgQueueKey : " << iMsgQueueKey);
  return true;
}

template<class type>
int SgagentMQ<type>::getMsg(int msgType, type &req, int &len) {
  bool flag = false;
  int iRcv = 0;
  for (int i = 0; i < RETRY_COUNT; i++) {
    iRcv = msgrcv(m_iMsgQueueId, &req,
                  sizeof(req) - sizeof(long),
                  msgType, IPC_NOWAIT);

    if (iRcv != -1) {
      flag = true;
      break;
    }

    if (errno == EIDRM || errno == EINVAL) {
      if (0 != _createMQ()) {
        LOG_ERROR("failed to create MQ in getMsg");
        return -1;
      }
    } else if ((errno != ENOMSG) && (errno != EAGAIN)) {
      //如果返回其他错误，则直接退出
      LOG_ERROR("get MQ fail, errno : " << errno << " , errmsg is : " << strerror(errno));
      break;
    }
  }

  //通过引用返回获取到的消息体大小
  len = iRcv;

  if (!flag) {
    return -2;
  }

  return 0;
}

template<class type>
int SgagentMQ<type>::setMsg(const type &reqMsg, int len) {
  bool flag = false;
  int iRcv = 0;
  for (int i = 0; i < RETRY_COUNT; i++) {
    if (msgsnd(m_iMsgQueueId, &reqMsg, len, IPC_NOWAIT) != -1) {
      flag = true;
      break;
    }

    if (errno == EIDRM || errno == EINVAL) {
      if (0 != _createMQ()) {
        LOG_ERROR("failed to re-create MQ in setMsg");
        return -1;
      }
    } else if (errno != EAGAIN) {
      //如果返回非EAGAIN，表示消息队列占满之外的错误则直接退出，不重试;
      LOG_ERROR("setMsg MQ fail! errno : " << errno << " , errmsg is : " << strerror(errno));
      break;
    } else if (errno == EAGAIN) {
      //如果返回错误码为11（EAGAIN），则删除对应mq；
      //修复返回11错误码，无法serMsg的bug
      LOG_FATAL("setMsg MQ fail , start to delete mq! errno : " << errno << " , errmsg is : " << strerror(errno));
      msgctl(m_iMsgQueueId, IPC_RMID, NULL);
      //重新创建mq
      if (0 != _createMQ()) {
        LOG_ERROR("failed to create MQ in setMsg, after delete old mq");
        return -3;
      }
      return ERR_MQ_FULL;
    }
  }

  if (!flag) {
    LOG_FATAL("setMsg MQ fail after 3 times retry ! errno : " << errno << " , errmsg is : " << strerror(errno));
    return -2;
  }

  return 0;
}

template<class type>
int SgagentMQ<type>::_createMQ() {
  //如果消息队列被删除，或者无效的qid参数，则重新创建mq, 并重试send
  m_iMsgQueueId = msgget(m_iMsgQueueKey, IPC_CREAT | 0666);
  if (m_iMsgQueueId == -1) {
    LOG_ERROR("get MQ fail, m_iMsgQueueKey : " << m_iMsgQueueKey
                                               << "; errno: " << errno);
  } else if (0 == m_iMsgQueueId) {
    if (0 != _removeMQ(m_iMsgQueueId)) {
      LOG_ERROR("remove MQ failed, iMsgQueueKey: " << m_iMsgQueueKey
                                                   << "; msqid = " << m_iMsgQueueId);
      return -1;
    }
  } else {
    return 0;
  }

  m_iMsgQueueId = msgget(m_iMsgQueueKey, IPC_CREAT | 0666);
  if (m_iMsgQueueId == -1 || 0 == m_iMsgQueueId) {
    LOG_ERROR("get MQ fail again, m_iMsgQueueKey : " << m_iMsgQueueKey
                                                     << "; msqid = " << m_iMsgQueueId
                                                     << "; errno: " << errno);
    return -2;
  }
  return 0;
}

template<class type>
int SgagentMQ<type>::_removeMQ(const int msqid) {
  int ret = -1;
  for (int i = 0; i < RETRY_COUNT; ++i) {
    if (0 == msgctl(msqid, IPC_RMID, 0)) {
      ret = 0;
      break;
    }
  }

  return ret;
}
} // namespace

#endif // __SGAGENT_MQ__

