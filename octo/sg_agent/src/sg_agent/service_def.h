#ifndef _SERVICE_DEF_
#define _SERVICE_DEF_

#include <vector>
#include <string>
#include "sgagent_service_types.h"

extern "C" {
#include <zookeeper/zookeeper.h>
#include "comm/cJSON.h"
}
#include <boost/shared_array.hpp>

namespace sg_agent {

using boost::shared_ptr;

typedef struct ZkGetRequest {
  std::string path;
  int watch;
} ZkGetRequest;

typedef struct ZkGetResponse {
  std::string buffer;
  int buffer_len;
  struct Stat stat;
  int err_code;
} ZkGetResponse;

typedef struct ZkGetInvokeParams {
  ZkGetRequest zk_get_request;
  ZkGetResponse zk_get_response;
} ZkGetInvokeParams;

typedef struct ZkWGetRequest {
  std::string path;
  watcher_fn watch;
  void *watcherCtx;
} ZkWGetRequest;

typedef struct ZkWGetResponse {
  std::string buffer;
  int buffer_len;
  struct Stat stat;
  int err_code;
} ZkWGetResponse;

typedef struct ZkWGetInvokeParams {
  ZkWGetRequest zk_wget_request;
  ZkWGetResponse zk_wget_response;
} ZkWGetInvokeParams;

typedef struct ZkWGetChildrenRequest {
  std::string path;
  watcher_fn watch;
  void *watcherCtx;
} ZkWGetChildrenRequest;

typedef struct ZkWGetChildrenResponse {
  int err_code;
  int count;
  std::vector<std::string> data;
} ZkWGetChildrenResponse;

typedef struct ZkWGetChildrenInvokeParams {
  ZkWGetChildrenRequest zk_wgetchildren_request;
  ZkWGetChildrenResponse zk_wgetchildren_response;
} ZkWGetChildrenInvokeParams;

typedef struct ZkCreateRequest {
  std::string path;
  std::string value;
  int value_len;
} ZkCreateRequest;

typedef struct ZkCreateInvokeParams {
  ZkCreateRequest zk_create_request;
  int zk_create_response;
} ZkCreateInvokeParams;

typedef struct ZkSetRequest {
  std::string path;
  std::string buffer;
  int version;
} ZkSetRequest;

typedef struct ZkExistsRequest {
  std::string path;
  int watch;
} ZkExistsRequest;

typedef struct MNSCacheRequest {
  std::vector<SGService> *serviceList;
  int providerSize;
  const std::string *appkey;
  const std::string *version;
  const std::string *env;
  const std::string *protocol;
} MNSCacheRequest;

enum ServiceNameType {
  ZK_GET = 1,
  ZK_WGET,
  ZK_WGET_CHILDREN,
  GET_MNS_CACHE,

};

int InvokeService(int service_name, void *service_params);

} // namespace sg_agent

#endif // SERVICE_DEF_
