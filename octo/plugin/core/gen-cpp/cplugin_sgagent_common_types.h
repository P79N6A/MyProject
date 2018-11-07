/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef cplugin_sgagent_common_TYPES_H
#define cplugin_sgagent_common_TYPES_H

#include <Thrift.h>
#include <TApplicationException.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>

#include "cplugin_config_common_types.h"


namespace cplugin_sgagent{



struct fb_status {
  enum type {
    DEAD = 0,
    STARTING = 1,
    ALIVE = 2,
    STOPPING = 3,
    STOPPED = 4,
    WARNING = 5
  };
};

extern const std::map<int, const char*> _fb_status_VALUES_TO_NAMES;

struct HeartbeatSupportType {
  enum type {
    NoSupport = 0,
    P2POnly = 1,
    ScannerOnly = 2,
    BothSupport = 3
  };
};

extern const std::map<int, const char*> _HeartbeatSupportType_VALUES_TO_NAMES;

struct UptCmd {
  enum type {
    RESET = 0,
    ADD = 1,
    DELETE = 2
  };
};

extern const std::map<int, const char*> _UptCmd_VALUES_TO_NAMES;

struct CustomizedStatus {
  enum type {
    DEAD = 0,
    ALIVE = 2,
    STOPPED = 4
  };
};

extern const std::map<int, const char*> _CustomizedStatus_VALUES_TO_NAMES;

typedef std::map<std::string, std::string>  HttpProperties;

typedef struct _ServiceDetail__isset {
  _ServiceDetail__isset() : unifiedProto(false) {}
  bool unifiedProto;
} _ServiceDetail__isset;

class ServiceDetail {
 public:

  static const char* ascii_fingerprint; // = "5892306F7B861249AE8E27C8ED619593";
  static const uint8_t binary_fingerprint[16]; // = {0x58,0x92,0x30,0x6F,0x7B,0x86,0x12,0x49,0xAE,0x8E,0x27,0xC8,0xED,0x61,0x95,0x93};

  ServiceDetail() : unifiedProto(0) {
  }

  virtual ~ServiceDetail() throw() {}

  bool unifiedProto;

  _ServiceDetail__isset __isset;

  void __set_unifiedProto(const bool val) {
    unifiedProto = val;
  }

  bool operator == (const ServiceDetail & rhs) const
  {
    if (!(unifiedProto == rhs.unifiedProto))
      return false;
    return true;
  }
  bool operator != (const ServiceDetail &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ServiceDetail & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _SGService__isset {
  _SGService__isset() : appkey(false), version(false), ip(false), port(false), weight(false), status(false), role(false), envir(false), lastUpdateTime(false), extend(false), fweight(false), serverType(false), protocol(false), serviceInfo(false), heartbeatSupport(false), swimlane(false), hostname(false), cell(false) {}
  bool appkey;
  bool version;
  bool ip;
  bool port;
  bool weight;
  bool status;
  bool role;
  bool envir;
  bool lastUpdateTime;
  bool extend;
  bool fweight;
  bool serverType;
  bool protocol;
  bool serviceInfo;
  bool heartbeatSupport;
  bool swimlane;
  bool hostname;
  bool cell;
} _SGService__isset;

class SGService {
 public:

  static const char* ascii_fingerprint; // = "BE038FED86004EC084ACFD2377089C9F";
  static const uint8_t binary_fingerprint[16]; // = {0xBE,0x03,0x8F,0xED,0x86,0x00,0x4E,0xC0,0x84,0xAC,0xFD,0x23,0x77,0x08,0x9C,0x9F};

  SGService() : appkey(""), version(""), ip(""), port(0), weight(0), status(0), role(0), envir(0), lastUpdateTime(0), extend(""), fweight(0), serverType(0), protocol(""), heartbeatSupport(0), swimlane(""), hostname(""), cell("") {
  }

  virtual ~SGService() throw() {}

  std::string appkey;
  std::string version;
  std::string ip;
  int32_t port;
  int32_t weight;
  int32_t status;
  int32_t role;
  int32_t envir;
  int32_t lastUpdateTime;
  std::string extend;
  double fweight;
  int32_t serverType;
  std::string protocol;
  std::map<std::string, ServiceDetail>  serviceInfo;
  int8_t heartbeatSupport;
  std::string swimlane;
  std::string hostname;
  std::string cell;

  _SGService__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_ip(const std::string& val) {
    ip = val;
  }

  void __set_port(const int32_t val) {
    port = val;
  }

  void __set_weight(const int32_t val) {
    weight = val;
  }

  void __set_status(const int32_t val) {
    status = val;
  }

  void __set_role(const int32_t val) {
    role = val;
  }

  void __set_envir(const int32_t val) {
    envir = val;
  }

  void __set_lastUpdateTime(const int32_t val) {
    lastUpdateTime = val;
  }

  void __set_extend(const std::string& val) {
    extend = val;
  }

  void __set_fweight(const double val) {
    fweight = val;
  }

  void __set_serverType(const int32_t val) {
    serverType = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  void __set_serviceInfo(const std::map<std::string, ServiceDetail> & val) {
    serviceInfo = val;
  }

  void __set_heartbeatSupport(const int8_t val) {
    heartbeatSupport = val;
  }

  void __set_swimlane(const std::string& val) {
    swimlane = val;
    __isset.swimlane = true;
  }

  void __set_hostname(const std::string& val) {
    hostname = val;
    __isset.hostname = true;
  }

  void __set_cell(const std::string& val) {
    cell = val;
    __isset.cell = true;
  }

  bool operator == (const SGService & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(ip == rhs.ip))
      return false;
    if (!(port == rhs.port))
      return false;
    if (!(weight == rhs.weight))
      return false;
    if (!(status == rhs.status))
      return false;
    if (!(role == rhs.role))
      return false;
    if (!(envir == rhs.envir))
      return false;
    if (!(lastUpdateTime == rhs.lastUpdateTime))
      return false;
    if (!(extend == rhs.extend))
      return false;
    if (!(fweight == rhs.fweight))
      return false;
    if (!(serverType == rhs.serverType))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    if (!(serviceInfo == rhs.serviceInfo))
      return false;
    if (!(heartbeatSupport == rhs.heartbeatSupport))
      return false;
    if (__isset.swimlane != rhs.__isset.swimlane)
      return false;
    else if (__isset.swimlane && !(swimlane == rhs.swimlane))
      return false;
    if (__isset.hostname != rhs.__isset.hostname)
      return false;
    else if (__isset.hostname && !(hostname == rhs.hostname))
      return false;
    if (__isset.cell != rhs.__isset.cell)
      return false;
    else if (__isset.cell && !(cell == rhs.cell))
      return false;
    return true;
  }
  bool operator != (const SGService &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const SGService & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ServiceNode__isset {
  _ServiceNode__isset() : serviceName(false), appkeys(false), lastUpdateTime(false) {}
  bool serviceName;
  bool appkeys;
  bool lastUpdateTime;
} _ServiceNode__isset;

class ServiceNode {
 public:

  static const char* ascii_fingerprint; // = "E6EB76A1BCBAF7124A43817D5BC322F7";
  static const uint8_t binary_fingerprint[16]; // = {0xE6,0xEB,0x76,0xA1,0xBC,0xBA,0xF7,0x12,0x4A,0x43,0x81,0x7D,0x5B,0xC3,0x22,0xF7};

  ServiceNode() : serviceName(""), lastUpdateTime(0) {
  }

  virtual ~ServiceNode() throw() {}

  std::string serviceName;
  std::set<std::string>  appkeys;
  int32_t lastUpdateTime;

  _ServiceNode__isset __isset;

  void __set_serviceName(const std::string& val) {
    serviceName = val;
  }

  void __set_appkeys(const std::set<std::string> & val) {
    appkeys = val;
  }

  void __set_lastUpdateTime(const int32_t val) {
    lastUpdateTime = val;
  }

  bool operator == (const ServiceNode & rhs) const
  {
    if (!(serviceName == rhs.serviceName))
      return false;
    if (!(appkeys == rhs.appkeys))
      return false;
    if (!(lastUpdateTime == rhs.lastUpdateTime))
      return false;
    return true;
  }
  bool operator != (const ServiceNode &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ServiceNode & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _Consumer__isset {
  _Consumer__isset() : ips(false), appkeys(false) {}
  bool ips;
  bool appkeys;
} _Consumer__isset;

class Consumer {
 public:

  static const char* ascii_fingerprint; // = "AA819045335EAFAB5C2FE456B4B9CC1C";
  static const uint8_t binary_fingerprint[16]; // = {0xAA,0x81,0x90,0x45,0x33,0x5E,0xAF,0xAB,0x5C,0x2F,0xE4,0x56,0xB4,0xB9,0xCC,0x1C};

  Consumer() {
  }

  virtual ~Consumer() throw() {}

  std::vector<std::string>  ips;
  std::vector<std::string>  appkeys;

  _Consumer__isset __isset;

  void __set_ips(const std::vector<std::string> & val) {
    ips = val;
  }

  void __set_appkeys(const std::vector<std::string> & val) {
    appkeys = val;
  }

  bool operator == (const Consumer & rhs) const
  {
    if (!(ips == rhs.ips))
      return false;
    if (!(appkeys == rhs.appkeys))
      return false;
    return true;
  }
  bool operator != (const Consumer &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const Consumer & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _CRouteData__isset {
  _CRouteData__isset() : id(false), name(false), appkey(false), env(false), category(false), priority(false), status(false), consumer(false), provider(false), updateTime(false), createTime(false), reserved(false) {}
  bool id;
  bool name;
  bool appkey;
  bool env;
  bool category;
  bool priority;
  bool status;
  bool consumer;
  bool provider;
  bool updateTime;
  bool createTime;
  bool reserved;
} _CRouteData__isset;

class CRouteData {
 public:

  static const char* ascii_fingerprint; // = "BA097AEDC13A13D0EF25D33F4578FC59";
  static const uint8_t binary_fingerprint[16]; // = {0xBA,0x09,0x7A,0xED,0xC1,0x3A,0x13,0xD0,0xEF,0x25,0xD3,0x3F,0x45,0x78,0xFC,0x59};

  CRouteData() : id(""), name(""), appkey(""), env(0), category(0), priority(0), status(0), updateTime(0), createTime(0), reserved("") {
  }

  virtual ~CRouteData() throw() {}

  std::string id;
  std::string name;
  std::string appkey;
  int32_t env;
  int32_t category;
  int32_t priority;
  int32_t status;
  Consumer consumer;
  std::vector<std::string>  provider;
  int32_t updateTime;
  int32_t createTime;
  std::string reserved;

  _CRouteData__isset __isset;

  void __set_id(const std::string& val) {
    id = val;
  }

  void __set_name(const std::string& val) {
    name = val;
  }

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const int32_t val) {
    env = val;
  }

  void __set_category(const int32_t val) {
    category = val;
  }

  void __set_priority(const int32_t val) {
    priority = val;
  }

  void __set_status(const int32_t val) {
    status = val;
  }

  void __set_consumer(const Consumer& val) {
    consumer = val;
  }

  void __set_provider(const std::vector<std::string> & val) {
    provider = val;
  }

  void __set_updateTime(const int32_t val) {
    updateTime = val;
  }

  void __set_createTime(const int32_t val) {
    createTime = val;
  }

  void __set_reserved(const std::string& val) {
    reserved = val;
  }

  bool operator == (const CRouteData & rhs) const
  {
    if (!(id == rhs.id))
      return false;
    if (!(name == rhs.name))
      return false;
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(category == rhs.category))
      return false;
    if (!(priority == rhs.priority))
      return false;
    if (!(status == rhs.status))
      return false;
    if (!(consumer == rhs.consumer))
      return false;
    if (!(provider == rhs.provider))
      return false;
    if (!(updateTime == rhs.updateTime))
      return false;
    if (!(createTime == rhs.createTime))
      return false;
    if (!(reserved == rhs.reserved))
      return false;
    return true;
  }
  bool operator != (const CRouteData &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const CRouteData & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _CProviderNode__isset {
  _CProviderNode__isset() : appkey(false), lastModifiedTime(false), mtime(false), cversion(false), version(false) {}
  bool appkey;
  bool lastModifiedTime;
  bool mtime;
  bool cversion;
  bool version;
} _CProviderNode__isset;

class CProviderNode {
 public:

  static const char* ascii_fingerprint; // = "9118FDD77343DD231D97C424A53D2DCF";
  static const uint8_t binary_fingerprint[16]; // = {0x91,0x18,0xFD,0xD7,0x73,0x43,0xDD,0x23,0x1D,0x97,0xC4,0x24,0xA5,0x3D,0x2D,0xCF};

  CProviderNode() : appkey(""), lastModifiedTime(0), mtime(0), cversion(0), version(0) {
  }

  virtual ~CProviderNode() throw() {}

  std::string appkey;
  int64_t lastModifiedTime;
  int64_t mtime;
  int64_t cversion;
  int64_t version;

  _CProviderNode__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_lastModifiedTime(const int64_t val) {
    lastModifiedTime = val;
  }

  void __set_mtime(const int64_t val) {
    mtime = val;
  }

  void __set_cversion(const int64_t val) {
    cversion = val;
  }

  void __set_version(const int64_t val) {
    version = val;
  }

  bool operator == (const CProviderNode & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(lastModifiedTime == rhs.lastModifiedTime))
      return false;
    if (!(mtime == rhs.mtime))
      return false;
    if (!(cversion == rhs.cversion))
      return false;
    if (!(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const CProviderNode &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const CProviderNode & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _CRouteNode__isset {
  _CRouteNode__isset() : appkey(false), lastModifiedTime(false), mtime(false), cversion(false), version(false) {}
  bool appkey;
  bool lastModifiedTime;
  bool mtime;
  bool cversion;
  bool version;
} _CRouteNode__isset;

class CRouteNode {
 public:

  static const char* ascii_fingerprint; // = "9118FDD77343DD231D97C424A53D2DCF";
  static const uint8_t binary_fingerprint[16]; // = {0x91,0x18,0xFD,0xD7,0x73,0x43,0xDD,0x23,0x1D,0x97,0xC4,0x24,0xA5,0x3D,0x2D,0xCF};

  CRouteNode() : appkey(""), lastModifiedTime(0), mtime(0), cversion(0), version(0) {
  }

  virtual ~CRouteNode() throw() {}

  std::string appkey;
  int64_t lastModifiedTime;
  int64_t mtime;
  int64_t cversion;
  int64_t version;

  _CRouteNode__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_lastModifiedTime(const int64_t val) {
    lastModifiedTime = val;
  }

  void __set_mtime(const int64_t val) {
    mtime = val;
  }

  void __set_cversion(const int64_t val) {
    cversion = val;
  }

  void __set_version(const int64_t val) {
    version = val;
  }

  bool operator == (const CRouteNode & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(lastModifiedTime == rhs.lastModifiedTime))
      return false;
    if (!(mtime == rhs.mtime))
      return false;
    if (!(cversion == rhs.cversion))
      return false;
    if (!(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const CRouteNode &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const CRouteNode & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ParamMCC__isset {
  _ParamMCC__isset() : appkey(false), zkNode(false), md5(false), fileName(false), path(false), lastUpdateTime(false), needToLocal(false), createTime(false), privilege(false), reserved(false), fileType(false) {}
  bool appkey;
  bool zkNode;
  bool md5;
  bool fileName;
  bool path;
  bool lastUpdateTime;
  bool needToLocal;
  bool createTime;
  bool privilege;
  bool reserved;
  bool fileType;
} _ParamMCC__isset;

class ParamMCC {
 public:

  static const char* ascii_fingerprint; // = "4C7065159F8A4E4B88CCEB0FC10B7731";
  static const uint8_t binary_fingerprint[16]; // = {0x4C,0x70,0x65,0x15,0x9F,0x8A,0x4E,0x4B,0x88,0xCC,0xEB,0x0F,0xC1,0x0B,0x77,0x31};

  ParamMCC() : appkey(""), zkNode(""), md5(""), fileName(""), path(""), lastUpdateTime(0), needToLocal(0), createTime(0), privilege(""), reserved(""), fileType("") {
  }

  virtual ~ParamMCC() throw() {}

  std::string appkey;
  std::string zkNode;
  std::string md5;
  std::string fileName;
  std::string path;
  int64_t lastUpdateTime;
  int32_t needToLocal;
  int64_t createTime;
  std::string privilege;
  std::string reserved;
  std::string fileType;

  _ParamMCC__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_zkNode(const std::string& val) {
    zkNode = val;
  }

  void __set_md5(const std::string& val) {
    md5 = val;
  }

  void __set_fileName(const std::string& val) {
    fileName = val;
  }

  void __set_path(const std::string& val) {
    path = val;
  }

  void __set_lastUpdateTime(const int64_t val) {
    lastUpdateTime = val;
  }

  void __set_needToLocal(const int32_t val) {
    needToLocal = val;
  }

  void __set_createTime(const int64_t val) {
    createTime = val;
  }

  void __set_privilege(const std::string& val) {
    privilege = val;
  }

  void __set_reserved(const std::string& val) {
    reserved = val;
  }

  void __set_fileType(const std::string& val) {
    fileType = val;
  }

  bool operator == (const ParamMCC & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(zkNode == rhs.zkNode))
      return false;
    if (!(md5 == rhs.md5))
      return false;
    if (!(fileName == rhs.fileName))
      return false;
    if (!(path == rhs.path))
      return false;
    if (!(lastUpdateTime == rhs.lastUpdateTime))
      return false;
    if (!(needToLocal == rhs.needToLocal))
      return false;
    if (!(createTime == rhs.createTime))
      return false;
    if (!(privilege == rhs.privilege))
      return false;
    if (!(reserved == rhs.reserved))
      return false;
    if (!(fileType == rhs.fileType))
      return false;
    return true;
  }
  bool operator != (const ParamMCC &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ParamMCC & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _proc_conf_param_t__isset {
  _proc_conf_param_t__isset() : version(false), conf(false), cmd(false), err(false), key(false), configNodeList(false), swimlane(false), token(false) {}
  bool version;
  bool conf;
  bool cmd;
  bool err;
  bool key;
  bool configNodeList;
  bool swimlane;
  bool token;
} _proc_conf_param_t__isset;

class proc_conf_param_t {
 public:

  static const char* ascii_fingerprint; // = "D4B92C4A6416894631611252BFE0659E";
  static const uint8_t binary_fingerprint[16]; // = {0xD4,0xB9,0x2C,0x4A,0x64,0x16,0x89,0x46,0x31,0x61,0x12,0x52,0xBF,0xE0,0x65,0x9E};

  proc_conf_param_t() : appkey(""), env(""), path(""), version(0), conf(""), cmd(0), err(0), key(""), swimlane(""), token("") {
  }

  virtual ~proc_conf_param_t() throw() {}

  std::string appkey;
  std::string env;
  std::string path;
  int64_t version;
  std::string conf;
  int32_t cmd;
  int32_t err;
  std::string key;
  std::vector<ConfigNode>  configNodeList;
  std::string swimlane;
  std::string token;

  _proc_conf_param_t__isset __isset;

  void __set_appkey(const std::string& val) {
    appkey = val;
  }

  void __set_env(const std::string& val) {
    env = val;
  }

  void __set_path(const std::string& val) {
    path = val;
  }

  void __set_version(const int64_t val) {
    version = val;
    __isset.version = true;
  }

  void __set_conf(const std::string& val) {
    conf = val;
    __isset.conf = true;
  }

  void __set_cmd(const int32_t val) {
    cmd = val;
    __isset.cmd = true;
  }

  void __set_err(const int32_t val) {
    err = val;
    __isset.err = true;
  }

  void __set_key(const std::string& val) {
    key = val;
    __isset.key = true;
  }

  void __set_configNodeList(const std::vector<ConfigNode> & val) {
    configNodeList = val;
    __isset.configNodeList = true;
  }

  void __set_swimlane(const std::string& val) {
    swimlane = val;
    __isset.swimlane = true;
  }

  void __set_token(const std::string& val) {
    token = val;
    __isset.token = true;
  }

  bool operator == (const proc_conf_param_t & rhs) const
  {
    if (!(appkey == rhs.appkey))
      return false;
    if (!(env == rhs.env))
      return false;
    if (!(path == rhs.path))
      return false;
    if (__isset.version != rhs.__isset.version)
      return false;
    else if (__isset.version && !(version == rhs.version))
      return false;
    if (__isset.conf != rhs.__isset.conf)
      return false;
    else if (__isset.conf && !(conf == rhs.conf))
      return false;
    if (__isset.cmd != rhs.__isset.cmd)
      return false;
    else if (__isset.cmd && !(cmd == rhs.cmd))
      return false;
    if (__isset.err != rhs.__isset.err)
      return false;
    else if (__isset.err && !(err == rhs.err))
      return false;
    if (__isset.key != rhs.__isset.key)
      return false;
    else if (__isset.key && !(key == rhs.key))
      return false;
    if (__isset.configNodeList != rhs.__isset.configNodeList)
      return false;
    else if (__isset.configNodeList && !(configNodeList == rhs.configNodeList))
      return false;
    if (__isset.swimlane != rhs.__isset.swimlane)
      return false;
    else if (__isset.swimlane && !(swimlane == rhs.swimlane))
      return false;
    if (__isset.token != rhs.__isset.token)
      return false;
    else if (__isset.token && !(token == rhs.token))
      return false;
    return true;
  }
  bool operator != (const proc_conf_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const proc_conf_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};


class ConfigUpdateRequest {
 public:

  static const char* ascii_fingerprint; // = "145FC7AA3B7C950BC67968D2F783C7D5";
  static const uint8_t binary_fingerprint[16]; // = {0x14,0x5F,0xC7,0xAA,0x3B,0x7C,0x95,0x0B,0xC6,0x79,0x68,0xD2,0xF7,0x83,0xC7,0xD5};

  ConfigUpdateRequest() {
  }

  virtual ~ConfigUpdateRequest() throw() {}

  std::vector<ConfigNode>  nodes;

  void __set_nodes(const std::vector<ConfigNode> & val) {
    nodes = val;
  }

  bool operator == (const ConfigUpdateRequest & rhs) const
  {
    if (!(nodes == rhs.nodes))
      return false;
    return true;
  }
  bool operator != (const ConfigUpdateRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigUpdateRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ZabbixSelfCheck__isset {
  _ZabbixSelfCheck__isset() : msgQueueBytes(false), agent_vmRss(false), worker_vmRss(false), agent_cpu(false), worker_cpu(false), zkConnections(false), mtConfigConnections(false), logCollectorConnections(false), bufferKeyNum(false), missBuffNum(false), extend(false), bufferSize(false), reqStat(false), registeStat(false), unregisteStat(false) {}
  bool msgQueueBytes;
  bool agent_vmRss;
  bool worker_vmRss;
  bool agent_cpu;
  bool worker_cpu;
  bool zkConnections;
  bool mtConfigConnections;
  bool logCollectorConnections;
  bool bufferKeyNum;
  bool missBuffNum;
  bool extend;
  bool bufferSize;
  bool reqStat;
  bool registeStat;
  bool unregisteStat;
} _ZabbixSelfCheck__isset;

class ZabbixSelfCheck {
 public:

  static const char* ascii_fingerprint; // = "1FCF20947AA45A0568355018BAB3D27F";
  static const uint8_t binary_fingerprint[16]; // = {0x1F,0xCF,0x20,0x94,0x7A,0xA4,0x5A,0x05,0x68,0x35,0x50,0x18,0xBA,0xB3,0xD2,0x7F};

  ZabbixSelfCheck() : agent_vmRss(0), worker_vmRss(0), agent_cpu(0), worker_cpu(0), zkConnections(0), mtConfigConnections(0), logCollectorConnections(0), bufferKeyNum(0), missBuffNum(0), extend("") {
  }

  virtual ~ZabbixSelfCheck() throw() {}

  std::map<int32_t, int64_t>  msgQueueBytes;
  int32_t agent_vmRss;
  int32_t worker_vmRss;
  double agent_cpu;
  double worker_cpu;
  int32_t zkConnections;
  int32_t mtConfigConnections;
  int32_t logCollectorConnections;
  int32_t bufferKeyNum;
  int32_t missBuffNum;
  std::string extend;
  std::map<int32_t, int32_t>  bufferSize;
  std::map<int32_t, double>  reqStat;
  std::map<int32_t, double>  registeStat;
  std::map<int32_t, double>  unregisteStat;

  _ZabbixSelfCheck__isset __isset;

  void __set_msgQueueBytes(const std::map<int32_t, int64_t> & val) {
    msgQueueBytes = val;
  }

  void __set_agent_vmRss(const int32_t val) {
    agent_vmRss = val;
  }

  void __set_worker_vmRss(const int32_t val) {
    worker_vmRss = val;
  }

  void __set_agent_cpu(const double val) {
    agent_cpu = val;
  }

  void __set_worker_cpu(const double val) {
    worker_cpu = val;
  }

  void __set_zkConnections(const int32_t val) {
    zkConnections = val;
  }

  void __set_mtConfigConnections(const int32_t val) {
    mtConfigConnections = val;
  }

  void __set_logCollectorConnections(const int32_t val) {
    logCollectorConnections = val;
  }

  void __set_bufferKeyNum(const int32_t val) {
    bufferKeyNum = val;
  }

  void __set_missBuffNum(const int32_t val) {
    missBuffNum = val;
  }

  void __set_extend(const std::string& val) {
    extend = val;
  }

  void __set_bufferSize(const std::map<int32_t, int32_t> & val) {
    bufferSize = val;
  }

  void __set_reqStat(const std::map<int32_t, double> & val) {
    reqStat = val;
  }

  void __set_registeStat(const std::map<int32_t, double> & val) {
    registeStat = val;
  }

  void __set_unregisteStat(const std::map<int32_t, double> & val) {
    unregisteStat = val;
  }

  bool operator == (const ZabbixSelfCheck & rhs) const
  {
    if (!(msgQueueBytes == rhs.msgQueueBytes))
      return false;
    if (!(agent_vmRss == rhs.agent_vmRss))
      return false;
    if (!(worker_vmRss == rhs.worker_vmRss))
      return false;
    if (!(agent_cpu == rhs.agent_cpu))
      return false;
    if (!(worker_cpu == rhs.worker_cpu))
      return false;
    if (!(zkConnections == rhs.zkConnections))
      return false;
    if (!(mtConfigConnections == rhs.mtConfigConnections))
      return false;
    if (!(logCollectorConnections == rhs.logCollectorConnections))
      return false;
    if (!(bufferKeyNum == rhs.bufferKeyNum))
      return false;
    if (!(missBuffNum == rhs.missBuffNum))
      return false;
    if (!(extend == rhs.extend))
      return false;
    if (!(bufferSize == rhs.bufferSize))
      return false;
    if (!(reqStat == rhs.reqStat))
      return false;
    if (!(registeStat == rhs.registeStat))
      return false;
    if (!(unregisteStat == rhs.unregisteStat))
      return false;
    return true;
  }
  bool operator != (const ZabbixSelfCheck &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ZabbixSelfCheck & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ProtocolRequest__isset {
  _ProtocolRequest__isset() : localAppkey(false), remoteAppkey(false), protocol(false), serviceName(false), swimlane(false), enableSwimlane2(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool protocol;
  bool serviceName;
  bool swimlane;
  bool enableSwimlane2;
} _ProtocolRequest__isset;

class ProtocolRequest {
 public:

  static const char* ascii_fingerprint; // = "D31A571B43CE2C1A4997769A063060A6";
  static const uint8_t binary_fingerprint[16]; // = {0xD3,0x1A,0x57,0x1B,0x43,0xCE,0x2C,0x1A,0x49,0x97,0x76,0x9A,0x06,0x30,0x60,0xA6};

  ProtocolRequest() : localAppkey(""), remoteAppkey(""), protocol(""), serviceName(""), swimlane(""), enableSwimlane2(0) {
  }

  virtual ~ProtocolRequest() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string protocol;
  std::string serviceName;
  std::string swimlane;
  bool enableSwimlane2;

  _ProtocolRequest__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  void __set_serviceName(const std::string& val) {
    serviceName = val;
  }

  void __set_swimlane(const std::string& val) {
    swimlane = val;
    __isset.swimlane = true;
  }

  void __set_enableSwimlane2(const bool val) {
    enableSwimlane2 = val;
    __isset.enableSwimlane2 = true;
  }

  bool operator == (const ProtocolRequest & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    if (!(serviceName == rhs.serviceName))
      return false;
    if (__isset.swimlane != rhs.__isset.swimlane)
      return false;
    else if (__isset.swimlane && !(swimlane == rhs.swimlane))
      return false;
    if (__isset.enableSwimlane2 != rhs.__isset.enableSwimlane2)
      return false;
    else if (__isset.enableSwimlane2 && !(enableSwimlane2 == rhs.enableSwimlane2))
      return false;
    return true;
  }
  bool operator != (const ProtocolRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ProtocolRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ProtocolResponse__isset {
  _ProtocolResponse__isset() : errcode(false), servicelist(false) {}
  bool errcode;
  bool servicelist;
} _ProtocolResponse__isset;

class ProtocolResponse {
 public:

  static const char* ascii_fingerprint; // = "018E4A367EAEB47750ACB402CA47C4C7";
  static const uint8_t binary_fingerprint[16]; // = {0x01,0x8E,0x4A,0x36,0x7E,0xAE,0xB4,0x77,0x50,0xAC,0xB4,0x02,0xCA,0x47,0xC4,0xC7};

  ProtocolResponse() : errcode(0) {
  }

  virtual ~ProtocolResponse() throw() {}

  int32_t errcode;
  std::vector<SGService>  servicelist;

  _ProtocolResponse__isset __isset;

  void __set_errcode(const int32_t val) {
    errcode = val;
  }

  void __set_servicelist(const std::vector<SGService> & val) {
    servicelist = val;
  }

  bool operator == (const ProtocolResponse & rhs) const
  {
    if (!(errcode == rhs.errcode))
      return false;
    if (!(servicelist == rhs.servicelist))
      return false;
    return true;
  }
  bool operator != (const ProtocolResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ProtocolResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _SwitchRequest__isset {
  _SwitchRequest__isset() : key(false), value(false), verifyCode(false), switchName(false) {}
  bool key;
  bool value;
  bool verifyCode;
  bool switchName;
} _SwitchRequest__isset;

class SwitchRequest {
 public:

  static const char* ascii_fingerprint; // = "A51B7D8E715DD8C5C5D67975205210A9";
  static const uint8_t binary_fingerprint[16]; // = {0xA5,0x1B,0x7D,0x8E,0x71,0x5D,0xD8,0xC5,0xC5,0xD6,0x79,0x75,0x20,0x52,0x10,0xA9};

  SwitchRequest() : key(0), value(0), verifyCode(""), switchName("") {
  }

  virtual ~SwitchRequest() throw() {}

  int32_t key;
  bool value;
  std::string verifyCode;
  std::string switchName;

  _SwitchRequest__isset __isset;

  void __set_key(const int32_t val) {
    key = val;
  }

  void __set_value(const bool val) {
    value = val;
  }

  void __set_verifyCode(const std::string& val) {
    verifyCode = val;
  }

  void __set_switchName(const std::string& val) {
    switchName = val;
  }

  bool operator == (const SwitchRequest & rhs) const
  {
    if (!(key == rhs.key))
      return false;
    if (!(value == rhs.value))
      return false;
    if (!(verifyCode == rhs.verifyCode))
      return false;
    if (!(switchName == rhs.switchName))
      return false;
    return true;
  }
  bool operator != (const SwitchRequest &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const SwitchRequest & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _SwitchResponse__isset {
  _SwitchResponse__isset() : errcode(false), msg(false) {}
  bool errcode;
  bool msg;
} _SwitchResponse__isset;

class SwitchResponse {
 public:

  static const char* ascii_fingerprint; // = "3F5FC93B338687BC7235B1AB103F47B3";
  static const uint8_t binary_fingerprint[16]; // = {0x3F,0x5F,0xC9,0x3B,0x33,0x86,0x87,0xBC,0x72,0x35,0xB1,0xAB,0x10,0x3F,0x47,0xB3};

  SwitchResponse() : errcode(0), msg("") {
  }

  virtual ~SwitchResponse() throw() {}

  int32_t errcode;
  std::string msg;

  _SwitchResponse__isset __isset;

  void __set_errcode(const int32_t val) {
    errcode = val;
  }

  void __set_msg(const std::string& val) {
    msg = val;
  }

  bool operator == (const SwitchResponse & rhs) const
  {
    if (!(errcode == rhs.errcode))
      return false;
    if (!(msg == rhs.msg))
      return false;
    return true;
  }
  bool operator != (const SwitchResponse &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const SwitchResponse & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _ConfigStatus__isset {
  _ConfigStatus__isset() : initStatus(false), runtimeStatus(false) {}
  bool initStatus;
  bool runtimeStatus;
} _ConfigStatus__isset;

class ConfigStatus {
 public:

  static const char* ascii_fingerprint; // = "69EF77542FBF7A8DD68310C9FF3B44BD";
  static const uint8_t binary_fingerprint[16]; // = {0x69,0xEF,0x77,0x54,0x2F,0xBF,0x7A,0x8D,0xD6,0x83,0x10,0xC9,0xFF,0x3B,0x44,0xBD};

  ConfigStatus() : initStatus((CustomizedStatus::type)0), runtimeStatus((CustomizedStatus::type)0) {
  }

  virtual ~ConfigStatus() throw() {}

  CustomizedStatus::type initStatus;
  CustomizedStatus::type runtimeStatus;

  _ConfigStatus__isset __isset;

  void __set_initStatus(const CustomizedStatus::type val) {
    initStatus = val;
  }

  void __set_runtimeStatus(const CustomizedStatus::type val) {
    runtimeStatus = val;
  }

  bool operator == (const ConfigStatus & rhs) const
  {
    if (!(initStatus == rhs.initStatus))
      return false;
    if (!(runtimeStatus == rhs.runtimeStatus))
      return false;
    return true;
  }
  bool operator != (const ConfigStatus &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const ConfigStatus & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

}

#endif
