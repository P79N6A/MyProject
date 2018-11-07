

#ifndef SG_AGENT_MNS_COMM_H
#define SG_AGENT_MNS_COMM_H
namespace sg_agent {
struct ServListAndCache {
  int origin_servlist_size;
  int filte_servlist_size;
  int origin_cache_size;
  int filte_cache_size;
  std::vector<SGService> origin_servicelist;
  std::vector<SGService> filte_servicelist;
  ServListAndCache() :
      origin_servlist_size(0),
      filte_servlist_size(0),
      origin_cache_size(0),
      filte_cache_size(0),
      origin_servicelist(std::vector<SGService>()),
      filte_servicelist(std::vector<SGService>()){}
};

//TODO: distinguish the struct using the types
struct SgCollectorMonitorInfo {
  int pid;
  int vmRss;
  int cpu;
  int zkConnections;
  int mtConfigConnections;
  int logCollectorConnections;
  int fileConfigQueueLen;
  int kvConfigQueueLen;
  int serviceListQueueLen;
  int routeListQueueLen;
  int serviceNameQueueLen;
  int commonLogQueueLen;
  int moduleInvokerQueueLen;
  int registeQueueSizeLen;
  std::string extend;
  SgCollectorMonitorInfo():
      pid(0),
      vmRss(0),
      cpu(0),
      zkConnections(0),
      mtConfigConnections(0),
      logCollectorConnections(0),
      fileConfigQueueLen(0),
      kvConfigQueueLen(0),
      serviceListQueueLen(0),
      routeListQueueLen(0),
      serviceNameQueueLen(0),
      commonLogQueueLen(0),
      moduleInvokerQueueLen(0),
      registeQueueSizeLen(0),
      extend("") {}
};
}

#endif //SG_AGENT_MNS_COMM_H
