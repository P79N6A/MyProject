/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef sgagent_worker_service_TYPES_H
#define sgagent_worker_service_TYPES_H

#include <Thrift.h>
#include <TApplicationException.h>
#include <protocol/TProtocol.h>
#include <transport/TTransport.h>

#include "aggregator_common_types.h"
#include "sgagent_common_types.h"
#include "quota_common_types.h"
#include "config_common_types.h"




struct RegistCmd {
  enum type {
    REGIST = 0,
    UNREGIST = 1
  };
};

extern const std::map<int, const char*> _RegistCmd_VALUES_TO_NAMES;

typedef class  ::SGModuleInvokeInfo SGModuleInvokeInfo;

typedef class  ::CommonLog CommonLog;

typedef class  ::SGLog SGLog;

typedef class  ::SGService SGService;

typedef class  ::CRouteData CRouteData;

typedef class  ::DegradeAction DegradeAction;

typedef  ::HttpProperties HttpProperties;

typedef struct _properties_res_param_t__isset {
  _properties_res_param_t__isset() : businessLineCode(false), httpProperties(false), version(false), errCode(false) {}
  bool businessLineCode;
  bool httpProperties;
  bool version;
  bool errCode;
} _properties_res_param_t__isset;

class properties_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "091C516EB55F7A7B385216823FC1A20B";
  static const uint8_t binary_fingerprint[16]; // = {0x09,0x1C,0x51,0x6E,0xB5,0x5F,0x7A,0x7B,0x38,0x52,0x16,0x82,0x3F,0xC1,0xA2,0x0B};

  properties_res_param_t() : businessLineCode(0), version(""), errCode(0) {
  }

  virtual ~properties_res_param_t() throw() {}

  int32_t businessLineCode;
  std::map<std::string, HttpProperties>  httpProperties;
  std::string version;
  int32_t errCode;

  _properties_res_param_t__isset __isset;

  void __set_businessLineCode(const int32_t val) {
    businessLineCode = val;
  }

  void __set_httpProperties(const std::map<std::string, HttpProperties> & val) {
    httpProperties = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_errCode(const int32_t val) {
    errCode = val;
  }

  bool operator == (const properties_res_param_t & rhs) const
  {
    if (!(businessLineCode == rhs.businessLineCode))
      return false;
    if (!(httpProperties == rhs.httpProperties))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(errCode == rhs.errCode))
      return false;
    return true;
  }
  bool operator != (const properties_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const properties_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _appkey_req_param_t__isset {
  _appkey_req_param_t__isset() : businessLineCode(false), version(false) {}
  bool businessLineCode;
  bool version;
} _appkey_req_param_t__isset;

class appkey_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "3F5FC93B338687BC7235B1AB103F47B3";
  static const uint8_t binary_fingerprint[16]; // = {0x3F,0x5F,0xC9,0x3B,0x33,0x86,0x87,0xBC,0x72,0x35,0xB1,0xAB,0x10,0x3F,0x47,0xB3};

  appkey_req_param_t() : businessLineCode(0), version("") {
  }

  virtual ~appkey_req_param_t() throw() {}

  int32_t businessLineCode;
  std::string version;

  _appkey_req_param_t__isset __isset;

  void __set_businessLineCode(const int32_t val) {
    businessLineCode = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  bool operator == (const appkey_req_param_t & rhs) const
  {
    if (!(businessLineCode == rhs.businessLineCode))
      return false;
    if (!(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const appkey_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const appkey_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _appkey_res_param_t__isset {
  _appkey_res_param_t__isset() : businessLineCode(false), errCode(false), version(false), appKeyList(false) {}
  bool businessLineCode;
  bool errCode;
  bool version;
  bool appKeyList;
} _appkey_res_param_t__isset;

class appkey_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "632DB2A50EFAE11F88B02D974622BF6E";
  static const uint8_t binary_fingerprint[16]; // = {0x63,0x2D,0xB2,0xA5,0x0E,0xFA,0xE1,0x1F,0x88,0xB0,0x2D,0x97,0x46,0x22,0xBF,0x6E};

  appkey_res_param_t() : businessLineCode(0), errCode(0), version("") {
  }

  virtual ~appkey_res_param_t() throw() {}

  int32_t businessLineCode;
  int32_t errCode;
  std::string version;
  std::vector<std::string>  appKeyList;

  _appkey_res_param_t__isset __isset;

  void __set_businessLineCode(const int32_t val) {
    businessLineCode = val;
  }

  void __set_errCode(const int32_t val) {
    errCode = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_appKeyList(const std::vector<std::string> & val) {
    appKeyList = val;
  }

  bool operator == (const appkey_res_param_t & rhs) const
  {
    if (!(businessLineCode == rhs.businessLineCode))
      return false;
    if (!(errCode == rhs.errCode))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(appKeyList == rhs.appKeyList))
      return false;
    return true;
  }
  bool operator != (const appkey_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const appkey_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _quota_req_param_t__isset {
  _quota_req_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
} _quota_req_param_t__isset;

class quota_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "AB879940BD15B6B25691265F7384B271";
  static const uint8_t binary_fingerprint[16]; // = {0xAB,0x87,0x99,0x40,0xBD,0x15,0xB6,0xB2,0x56,0x91,0x26,0x5F,0x73,0x84,0xB2,0x71};

  quota_req_param_t() : localAppkey(""), remoteAppkey(""), version("") {
  }

  virtual ~quota_req_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;

  _quota_req_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  bool operator == (const quota_req_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    return true;
  }
  bool operator != (const quota_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const quota_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _quota_res_param_t__isset {
  _quota_res_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false), actions(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
  bool actions;
} _quota_res_param_t__isset;

class quota_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "502C7D5E105E9141CEFE0F614811AED5";
  static const uint8_t binary_fingerprint[16]; // = {0x50,0x2C,0x7D,0x5E,0x10,0x5E,0x91,0x41,0xCE,0xFE,0x0F,0x61,0x48,0x11,0xAE,0xD5};

  quota_res_param_t() : localAppkey(""), remoteAppkey(""), version("") {
  }

  virtual ~quota_res_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  std::vector<DegradeAction>  actions;

  _quota_res_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_actions(const std::vector<DegradeAction> & val) {
    actions = val;
  }

  bool operator == (const quota_res_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(actions == rhs.actions))
      return false;
    return true;
  }
  bool operator != (const quota_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const quota_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _locconf_req_param_t__isset {
  _locconf_req_param_t__isset() : localAppkey(false), ip(false) {}
  bool localAppkey;
  bool ip;
} _locconf_req_param_t__isset;

class locconf_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "07A9615F837F7D0A952B595DD3020972";
  static const uint8_t binary_fingerprint[16]; // = {0x07,0xA9,0x61,0x5F,0x83,0x7F,0x7D,0x0A,0x95,0x2B,0x59,0x5D,0xD3,0x02,0x09,0x72};

  locconf_req_param_t() : localAppkey(""), ip("") {
  }

  virtual ~locconf_req_param_t() throw() {}

  std::string localAppkey;
  std::string ip;

  _locconf_req_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_ip(const std::string& val) {
    ip = val;
  }

  bool operator == (const locconf_req_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(ip == rhs.ip))
      return false;
    return true;
  }
  bool operator != (const locconf_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const locconf_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _locconf_res_param_t__isset {
  _locconf_res_param_t__isset() : localAppkey(false), ip(false), conf(false) {}
  bool localAppkey;
  bool ip;
  bool conf;
} _locconf_res_param_t__isset;

class locconf_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "AB879940BD15B6B25691265F7384B271";
  static const uint8_t binary_fingerprint[16]; // = {0xAB,0x87,0x99,0x40,0xBD,0x15,0xB6,0xB2,0x56,0x91,0x26,0x5F,0x73,0x84,0xB2,0x71};

  locconf_res_param_t() : localAppkey(""), ip(""), conf("") {
  }

  virtual ~locconf_res_param_t() throw() {}

  std::string localAppkey;
  std::string ip;
  std::string conf;

  _locconf_res_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_ip(const std::string& val) {
    ip = val;
  }

  void __set_conf(const std::string& val) {
    conf = val;
  }

  bool operator == (const locconf_res_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(ip == rhs.ip))
      return false;
    if (!(conf == rhs.conf))
      return false;
    return true;
  }
  bool operator != (const locconf_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const locconf_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getservice_req_param_t__isset {
  _getservice_req_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false), protocol(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
  bool protocol;
} _getservice_req_param_t__isset;

class getservice_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "C93D890311F28844166CF6E571EB3AC2";
  static const uint8_t binary_fingerprint[16]; // = {0xC9,0x3D,0x89,0x03,0x11,0xF2,0x88,0x44,0x16,0x6C,0xF6,0xE5,0x71,0xEB,0x3A,0xC2};

  getservice_req_param_t() : localAppkey(""), remoteAppkey(""), version(""), protocol("") {
  }

  virtual ~getservice_req_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  std::string protocol;

  _getservice_req_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getservice_req_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getservice_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getservice_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getservice_res_param_t__isset {
  _getservice_res_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false), serviceList(false), protocol(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
  bool serviceList;
  bool protocol;
} _getservice_res_param_t__isset;

class getservice_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "DB7AB5C4D6F371B1AA37CBB5E33736F3";
  static const uint8_t binary_fingerprint[16]; // = {0xDB,0x7A,0xB5,0xC4,0xD6,0xF3,0x71,0xB1,0xAA,0x37,0xCB,0xB5,0xE3,0x37,0x36,0xF3};

  getservice_res_param_t() : localAppkey(""), remoteAppkey(""), version(""), protocol("") {
  }

  virtual ~getservice_res_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  std::vector<SGService>  serviceList;
  std::string protocol;

  _getservice_res_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_serviceList(const std::vector<SGService> & val) {
    serviceList = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getservice_res_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(serviceList == rhs.serviceList))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getservice_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getservice_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getservicename_req_param_t__isset {
  _getservicename_req_param_t__isset() : localAppkey(false), servicename(false), version(false), protocol(false) {}
  bool localAppkey;
  bool servicename;
  bool version;
  bool protocol;
} _getservicename_req_param_t__isset;

class getservicename_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "C93D890311F28844166CF6E571EB3AC2";
  static const uint8_t binary_fingerprint[16]; // = {0xC9,0x3D,0x89,0x03,0x11,0xF2,0x88,0x44,0x16,0x6C,0xF6,0xE5,0x71,0xEB,0x3A,0xC2};

  getservicename_req_param_t() : localAppkey(""), servicename(""), version(""), protocol("") {
  }

  virtual ~getservicename_req_param_t() throw() {}

  std::string localAppkey;
  std::string servicename;
  std::string version;
  std::string protocol;

  _getservicename_req_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_servicename(const std::string& val) {
    servicename = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getservicename_req_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(servicename == rhs.servicename))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getservicename_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getservicename_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getservicename_res_param_t__isset {
  _getservicename_res_param_t__isset() : localAppkey(false), servicename(false), version(false), appkeys(false), protocol(false) {}
  bool localAppkey;
  bool servicename;
  bool version;
  bool appkeys;
  bool protocol;
} _getservicename_res_param_t__isset;

class getservicename_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "029F9D059094419E5A20EF21917997A5";
  static const uint8_t binary_fingerprint[16]; // = {0x02,0x9F,0x9D,0x05,0x90,0x94,0x41,0x9E,0x5A,0x20,0xEF,0x21,0x91,0x79,0x97,0xA5};

  getservicename_res_param_t() : localAppkey(""), servicename(""), version(""), protocol("") {
  }

  virtual ~getservicename_res_param_t() throw() {}

  std::string localAppkey;
  std::string servicename;
  std::string version;
  std::set<std::string>  appkeys;
  std::string protocol;

  _getservicename_res_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_servicename(const std::string& val) {
    servicename = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_appkeys(const std::set<std::string> & val) {
    appkeys = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getservicename_res_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(servicename == rhs.servicename))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(appkeys == rhs.appkeys))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getservicename_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getservicename_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getroute_req_param_t__isset {
  _getroute_req_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false), protocol(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
  bool protocol;
} _getroute_req_param_t__isset;

class getroute_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "C93D890311F28844166CF6E571EB3AC2";
  static const uint8_t binary_fingerprint[16]; // = {0xC9,0x3D,0x89,0x03,0x11,0xF2,0x88,0x44,0x16,0x6C,0xF6,0xE5,0x71,0xEB,0x3A,0xC2};

  getroute_req_param_t() : localAppkey(""), remoteAppkey(""), version(""), protocol("") {
  }

  virtual ~getroute_req_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  std::string protocol;

  _getroute_req_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getroute_req_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getroute_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getroute_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getroute_res_param_t__isset {
  _getroute_res_param_t__isset() : localAppkey(false), remoteAppkey(false), version(false), routeList(false), protocol(false) {}
  bool localAppkey;
  bool remoteAppkey;
  bool version;
  bool routeList;
  bool protocol;
} _getroute_res_param_t__isset;

class getroute_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "61123EDDD00927E85F89381EEBDBC37F";
  static const uint8_t binary_fingerprint[16]; // = {0x61,0x12,0x3E,0xDD,0xD0,0x09,0x27,0xE8,0x5F,0x89,0x38,0x1E,0xEB,0xDB,0xC3,0x7F};

  getroute_res_param_t() : localAppkey(""), remoteAppkey(""), version(""), protocol("") {
  }

  virtual ~getroute_res_param_t() throw() {}

  std::string localAppkey;
  std::string remoteAppkey;
  std::string version;
  std::vector<CRouteData>  routeList;
  std::string protocol;

  _getroute_res_param_t__isset __isset;

  void __set_localAppkey(const std::string& val) {
    localAppkey = val;
  }

  void __set_remoteAppkey(const std::string& val) {
    remoteAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_routeList(const std::vector<CRouteData> & val) {
    routeList = val;
  }

  void __set_protocol(const std::string& val) {
    protocol = val;
  }

  bool operator == (const getroute_res_param_t & rhs) const
  {
    if (!(localAppkey == rhs.localAppkey))
      return false;
    if (!(remoteAppkey == rhs.remoteAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(routeList == rhs.routeList))
      return false;
    if (!(protocol == rhs.protocol))
      return false;
    return true;
  }
  bool operator != (const getroute_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getroute_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _regist_req_param_t__isset {
  _regist_req_param_t__isset() : retry_times(false), sgservice(false), uptCmd(false), regCmd(false) {}
  bool retry_times;
  bool sgservice;
  bool uptCmd;
  bool regCmd;
} _regist_req_param_t__isset;

class regist_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "D6ABD016C549193867FF3ECA4BA26208";
  static const uint8_t binary_fingerprint[16]; // = {0xD6,0xAB,0xD0,0x16,0xC5,0x49,0x19,0x38,0x67,0xFF,0x3E,0xCA,0x4B,0xA2,0x62,0x08};

  regist_req_param_t() : retry_times(0), uptCmd(0), regCmd((RegistCmd::type)0) {
  }

  virtual ~regist_req_param_t() throw() {}

  int32_t retry_times;
  SGService sgservice;
  int32_t uptCmd;
  RegistCmd::type regCmd;

  _regist_req_param_t__isset __isset;

  void __set_retry_times(const int32_t val) {
    retry_times = val;
  }

  void __set_sgservice(const SGService& val) {
    sgservice = val;
  }

  void __set_uptCmd(const int32_t val) {
    uptCmd = val;
  }

  void __set_regCmd(const RegistCmd::type val) {
    regCmd = val;
  }

  bool operator == (const regist_req_param_t & rhs) const
  {
    if (!(retry_times == rhs.retry_times))
      return false;
    if (!(sgservice == rhs.sgservice))
      return false;
    if (!(uptCmd == rhs.uptCmd))
      return false;
    if (!(regCmd == rhs.regCmd))
      return false;
    return true;
  }
  bool operator != (const regist_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const regist_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _log_req_param_t__isset {
  _log_req_param_t__isset() : retry_times(false), log(false) {}
  bool retry_times;
  bool log;
} _log_req_param_t__isset;

class log_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "2C1B45B207A39E7AD7B3A949E051F931";
  static const uint8_t binary_fingerprint[16]; // = {0x2C,0x1B,0x45,0xB2,0x07,0xA3,0x9E,0x7A,0xD7,0xB3,0xA9,0x49,0xE0,0x51,0xF9,0x31};

  log_req_param_t() : retry_times(0) {
  }

  virtual ~log_req_param_t() throw() {}

  int32_t retry_times;
  SGLog log;

  _log_req_param_t__isset __isset;

  void __set_retry_times(const int32_t val) {
    retry_times = val;
  }

  void __set_log(const SGLog& val) {
    log = val;
  }

  bool operator == (const log_req_param_t & rhs) const
  {
    if (!(retry_times == rhs.retry_times))
      return false;
    if (!(log == rhs.log))
      return false;
    return true;
  }
  bool operator != (const log_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const log_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _common_log_req_param_t__isset {
  _common_log_req_param_t__isset() : retry_times(false), commonlog(false) {}
  bool retry_times;
  bool commonlog;
} _common_log_req_param_t__isset;

class common_log_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "96E40A8435FB81FEF6F406DF6C9AC989";
  static const uint8_t binary_fingerprint[16]; // = {0x96,0xE4,0x0A,0x84,0x35,0xFB,0x81,0xFE,0xF6,0xF4,0x06,0xDF,0x6C,0x9A,0xC9,0x89};

  common_log_req_param_t() : retry_times(0) {
  }

  virtual ~common_log_req_param_t() throw() {}

  int32_t retry_times;
  CommonLog commonlog;

  _common_log_req_param_t__isset __isset;

  void __set_retry_times(const int32_t val) {
    retry_times = val;
  }

  void __set_commonlog(const CommonLog& val) {
    commonlog = val;
  }

  bool operator == (const common_log_req_param_t & rhs) const
  {
    if (!(retry_times == rhs.retry_times))
      return false;
    if (!(commonlog == rhs.commonlog))
      return false;
    return true;
  }
  bool operator != (const common_log_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const common_log_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _invoke_req_param_t__isset {
  _invoke_req_param_t__isset() : retry_times(false), info(false) {}
  bool retry_times;
  bool info;
} _invoke_req_param_t__isset;

class invoke_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "B1B78622E302D64A7BC61848281B46E8";
  static const uint8_t binary_fingerprint[16]; // = {0xB1,0xB7,0x86,0x22,0xE3,0x02,0xD6,0x4A,0x7B,0xC6,0x18,0x48,0x28,0x1B,0x46,0xE8};

  invoke_req_param_t() : retry_times(0) {
  }

  virtual ~invoke_req_param_t() throw() {}

  int32_t retry_times;
  SGModuleInvokeInfo info;

  _invoke_req_param_t__isset __isset;

  void __set_retry_times(const int32_t val) {
    retry_times = val;
  }

  void __set_info(const SGModuleInvokeInfo& val) {
    info = val;
  }

  bool operator == (const invoke_req_param_t & rhs) const
  {
    if (!(retry_times == rhs.retry_times))
      return false;
    if (!(info == rhs.info))
      return false;
    return true;
  }
  bool operator != (const invoke_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const invoke_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getauth_req_param_t__isset {
  _getauth_req_param_t__isset() : targetAppkey(false), version(false), role(false) {}
  bool targetAppkey;
  bool version;
  bool role;
} _getauth_req_param_t__isset;

class getauth_req_param_t {
 public:

  static const char* ascii_fingerprint; // = "343DA57F446177400B333DC49B037B0C";
  static const uint8_t binary_fingerprint[16]; // = {0x34,0x3D,0xA5,0x7F,0x44,0x61,0x77,0x40,0x0B,0x33,0x3D,0xC4,0x9B,0x03,0x7B,0x0C};

  getauth_req_param_t() : targetAppkey(""), version(""), role(0) {
  }

  virtual ~getauth_req_param_t() throw() {}

  std::string targetAppkey;
  std::string version;
  int32_t role;

  _getauth_req_param_t__isset __isset;

  void __set_targetAppkey(const std::string& val) {
    targetAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_role(const int32_t val) {
    role = val;
  }

  bool operator == (const getauth_req_param_t & rhs) const
  {
    if (!(targetAppkey == rhs.targetAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(role == rhs.role))
      return false;
    return true;
  }
  bool operator != (const getauth_req_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getauth_req_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};

typedef struct _getauth_res_param_t__isset {
  _getauth_res_param_t__isset() : targetAppkey(false), version(false), role(false), content(false) {}
  bool targetAppkey;
  bool version;
  bool role;
  bool content;
} _getauth_res_param_t__isset;

class getauth_res_param_t {
 public:

  static const char* ascii_fingerprint; // = "AC16F1200213405F9A9267FCBF95F39A";
  static const uint8_t binary_fingerprint[16]; // = {0xAC,0x16,0xF1,0x20,0x02,0x13,0x40,0x5F,0x9A,0x92,0x67,0xFC,0xBF,0x95,0xF3,0x9A};

  getauth_res_param_t() : targetAppkey(""), version(""), role(0), content("") {
  }

  virtual ~getauth_res_param_t() throw() {}

  std::string targetAppkey;
  std::string version;
  int32_t role;
  std::string content;

  _getauth_res_param_t__isset __isset;

  void __set_targetAppkey(const std::string& val) {
    targetAppkey = val;
  }

  void __set_version(const std::string& val) {
    version = val;
  }

  void __set_role(const int32_t val) {
    role = val;
  }

  void __set_content(const std::string& val) {
    content = val;
  }

  bool operator == (const getauth_res_param_t & rhs) const
  {
    if (!(targetAppkey == rhs.targetAppkey))
      return false;
    if (!(version == rhs.version))
      return false;
    if (!(role == rhs.role))
      return false;
    if (!(content == rhs.content))
      return false;
    return true;
  }
  bool operator != (const getauth_res_param_t &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const getauth_res_param_t & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

};



#endif
