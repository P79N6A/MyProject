#ifndef __THRIFT_CLIENT_HANDLER_H__
#define __THRIFT_CLIENT_HANDLER_H__

#include <pthread.h>

#include "sgagent_service_types.h"
#include "AggregatorService.h"
#include "aggregator_service_types.h"
#include "config_service_types.h"
#include "MNSCacheService.h"
#include "MtConfigService.h"
#include "mnsc_data_types.h"
#include "comm/inc_comm.h"

enum ProcType {
  COMMONLOG,
  MTCONFIG,
  MNSCache
};

class ThriftClientHandler {
 public:
  ThriftClientHandler();
  ~ThriftClientHandler();
  int init(const std::string &host, int port, ProcType proc_type);
  int checkConnection();
  int createConnection();
  int closeConnection();
  int checkHandler();
  void *getClient() {
    return m_client;
  }

  bool m_closed;
  std::string m_host;
  int m_port;
  void *m_client;
  shared_ptr<TSocket> m_socket;
  shared_ptr<TTransport> m_transport;
  shared_ptr<TProtocol> m_protocol;
  ProcType type;
};

#endif

