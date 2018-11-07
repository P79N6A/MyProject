//
// Created by ZhangXiang on 2018/3/29.
//

#ifndef CTHRIFT_CTHRIFT_ASYNC_CALLBACK_H
#define CTHRIFT_CTHRIFT_ASYNC_CALLBACK_H

#include <boost/function.hpp>
#include "cthrift_common.h"

namespace cthrift {

template<class CobClient>
class AsyncCallback {
public:
    //typedef void (*AsyncCobType) (CobClient *client);
    typedef boost::function<void(CobClient* client)> AsyncCobType;

    AsyncCallback() : cob_success_(boost::bind(&Default, _1)),
                      cob_timeout_(boost::bind(&Default, _1)),
                      cob_highwater_(boost::bind(&Default, _1)){

    }
    ~AsyncCallback() {

    }

    static void Default(CobClient *client) {
        CLOG_STR_DEBUG("Default callback function");
    }

    void Callback(CobClient *client) {
        AsyncState async_task_state = boost::any_cast<AsyncState>(
                (muduo::net::EventLoop::getEventLoopOfCurrentThread())->getContext());
        switch (async_task_state) {
            case TASK_SUCCESS :
                CLOG_STR_DEBUG("Call Success cob_: ");
                cob_success_(client);
                break;
            case TASK_TIMEOUT :
                CLOG_STR_DEBUG("Call Timeout cob_: ");
                cob_timeout_(client);
                break;
            case TASK_TOO_MANY :
                CLOG_STR_DEBUG("Call HighWater cob_: ");
                cob_highwater_(client);
                break;
            default:
                CLOG_STR_ERROR("Invalid callback type");
        }
    }

    void Success(const AsyncCobType& cob) {
        cob_success_ = cob;
    }
    void Timeout(const AsyncCobType& cob) {
        cob_timeout_ = cob;
    }
    void HighWater(const AsyncCobType& cob) {
        cob_highwater_ = cob;
    }

private:

    AsyncCobType cob_success_;
    AsyncCobType cob_timeout_;
    AsyncCobType cob_highwater_;
};
}

#endif //CTHRIFT_CTHRIFT_ASYNC_CALLBACK_H
