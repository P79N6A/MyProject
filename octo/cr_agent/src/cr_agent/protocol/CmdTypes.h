//
// Created by smartlife on 2017/8/7.
//

#ifndef WORKSPACE_CMDTYPES_H
#define WORKSPACE_CMDTYPES_H
#define MAX_COMMAND_NUM 4
#define MAX_INDEX 9
#define MAX_BANNER_COMMAND 13
#define MAX_COMMAND_LEN 64

typedef enum cmd {
  INVLID_CMD_TYPE = -1,
  SCHEDULE_SEND_TASK,
  SCHEDULE_RECEIVE_RESULT,
  ATTEMPT_KILL_SEND_TASK,
  ATTEMPT_KILL_RECEIVE_RESULT,
  ATTEMPT_RUNNING_TASK,
  ATTEMPT_RUNNING_RESULT,
  EXTEND_COMMAND
} cmd_type;
enum index2value {
  INVALID_INDEX = -1,
  CMD_INDEX,
  TASKNAME_INDEX,
  JOBCODE_INDEX,
  TRACEID_INDEX,
  RUNSTATE_INDEX,
  CALLBACKADDR_INDEX,
  RUNCMD_INDEX,
  NODE_INDEX,
  IPPORT_INDEX
};

typedef enum rt_code {
  ERROR_CODE_NULL = 0,
  SUCESS_CODE,
  ERROR_CODE_MISPATCH,
  INVALID_RUN_CMD,
  NET_CMD_CHECK_ERROR,

} rt_code_type;


#endif //WORKSPACE_CMDTYPES_H