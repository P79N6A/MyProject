namespace java com.sankuai.sgagent.thrift.model
namespace cpp com.sankuai.cmtrace

include  "./sgagent_common.thrift"

typedef sgagent_common.SwitchRequest SwitchRequest
typedef sgagent_common.SwitchResponse SwitchResponse

service SGAgentWorker
{
   /**
    * update switch value. 
    */
   SwitchResponse setRemoteSwitch(1:SwitchRequest req),

   /**
    * Suggest a shutdown to the sg_agent worker
    * return false while verifyCode is invalid; The proccess will exit while success.
    */
   bool shutdown(1: string verifyCode),
}

