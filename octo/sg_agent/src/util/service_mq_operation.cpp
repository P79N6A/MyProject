#include "service_mq_operation.h"
#include "sgagent_worker_service_types.h"
SGServiceMq::SGServiceMq() {
    mSgagentServiceMQ = NULL;
}

SGServiceMq::~SGServiceMq() {
    SAFE_DELETE(mSgagentServiceMQ);
}

int SGServiceMq::init(int key) {
    mSgagentServiceMQ = new SgagentMQ<sg_msgbuf>();
    if(mSgagentServiceMQ)
    {
        if(!mSgagentServiceMQ -> init(key))
        {
            LOG_ERROR("failed to init serviceMQ. key = " << key);
            return -1;
        }
        return 0;
    }
}

int SGServiceMq::sendServiceMsg(const std::string &remoteAppkey, const std::string& protocol) {
    //构造结构体
    shared_ptr<getservice_req_param_t> pReq(new getservice_req_param_t());
    pReq->__set_localAppkey("watcher_trigger");
    pReq->__set_remoteAppkey(remoteAppkey);
    pReq->__set_protocol(protocol);

    std::string content = Thrift2String(*pReq.get());
    int strlen = content.length();
    if (DEFAULT_BUF_LEN < strlen)
    {
        LOG_WARN("mq content is longer than DEFAULT_BUF_LEN: " << strlen
                 << ", remoteAppkey = " << remoteAppkey
                 << ", localAppkey = " << protocol);
        return ERR_MQCONTENT_TOOLONG;
    }

    sg_msgbuf req_service;
    req_service.mtype = MQ_SG_AGENT;
    memcpy(req_service.mtext, content.c_str(), strlen);

    int ret = mSgagentServiceMQ -> setMsg(req_service, strlen);
    if (0 != ret)
    {
        LOG_ERROR("failed to send msg to MQ, appkey = " << remoteAppkey
                   << ", protocol = " << protocol
                   << ", ret = " << ret);
        return ERR_FAILEDSENDMSG;
    }
    return 0;
}
