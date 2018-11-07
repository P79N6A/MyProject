#ifndef TASK_CONTEXT_H_
#define TASK_CONTEXT_H_

#include <unistd.h>
#include <stddef.h>
#include <boost/shared_ptr.hpp>
#include "../cplugin_def.h"
#include <sstream>

namespace cplugin {

    static const long kSleepInterval = 10 * 1000; // us

    class RequestParams_t{
    public:
        std::string plugin_name;
        std::string plugin_version;
        int32_t plugin_id;
        int32_t task_id;
        CPLUGIN_OPERATION_TYPE type;
        RequestParams_t(std::string name, std::string version,
                        int32_t plugin, int32_t task,
                        CPLUGIN_OPERATION_TYPE type_x)
                :plugin_name(name),
                 plugin_version(version),
                 plugin_id(plugin),
                 task_id(task),
                 type(type_x) {
        }

        const std::string ToString() const {
            char buf[1024] = {'0'};
            snprintf(buf,1024,"name:%s, version:%s, plugin_id:%d, task_id:%d",
                     plugin_name.c_str(),plugin_version.c_str(), plugin_id, task_id);

            return buf;
        }
    };


    class SyncRequestBase{
    public:
        SyncRequestBase(){

        }

        ~SyncRequestBase(){

        }

        CPLUGIN_OPERATION_TYPE type_;
    };

    class SyncMonitorParams_t : public SyncRequestBase {
    public:

        std::vector<std::string> ags;

        SyncMonitorParams_t(CPLUGIN_OPERATION_TYPE type_x, const std::vector<std::string>& ags_x)
                : ags(ags_x)
        {
            SyncRequestBase::type_ =type_x;
        }
    };


    class SyncResponseBase{
    public:
        SyncResponseBase(){

        }

        ~SyncResponseBase(){

        }

        CPLUGIN_OPERATION_TYPE type_;
    };

    class SyncMonitorResponse_t : public SyncResponseBase {
    public:
        std::map<std::string,std::string> info;


        SyncMonitorResponse_t(CPLUGIN_OPERATION_TYPE type_x, const std::map<std::string,std::string>& info_x)
                : info(info_x)
        {
            SyncResponseBase::type_ = type_x;
        }
    };

    template <class Request, class Response>
    class TaskContext {
    public:
        explicit TaskContext(const Request& req)
                : req_(req), result_ready_(false) {}
        ~TaskContext() {}


        void WaitResult(long timeout) { // 单位us
            long time_spent = 0;

            while (true) {
                if (result_ready_ || time_spent > timeout) {
                    break;
                }
                time_spent += kSleepInterval;
                usleep(kSleepInterval);
            }
        }

        void set_response(const Response& rsp) {
            rsp_ = rsp;
            result_ready_ = true;
        }

        const Request* get_request() const {
            return &req_;
        }

        const Response* get_response() const {
            if (result_ready_) {
                return &rsp_;
            }
            return NULL;
        }
    private:
        Request req_;
        Response rsp_;
        volatile bool result_ready_;
    };


} // namespace cplugin

#endif // TASK_CONTEXT_H_
