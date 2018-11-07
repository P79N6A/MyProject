#ifndef _service_mq_operation_H__
#define _service_mq_operation_H__

#include "comm/sgagent_mq.h"

using namespace sg_agent;

class SGServiceMq {
public:
    int init(int key);

    SGServiceMq();

    ~SGServiceMq();
    
    int sendServiceMsg(const std::string &remoteAppkey, const std::string& protocol);

private:
    SgagentMQ<sg_msgbuf>* mSgagentServiceMQ;
};

#endif
