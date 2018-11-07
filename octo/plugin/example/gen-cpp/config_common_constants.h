/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef config_common_CONSTANTS_H
#define config_common_CONSTANTS_H

#include "config_common_types.h"



class config_commonConstants {
 public:
  config_commonConstants();

  std::string JSON;
  std::string PROPERTIES;
  std::string XML;
  std::string PROD;
  std::string STAGE;
  std::string TEST;
  int32_t SUCCESS;
  int32_t NO_CHANGE;
  int32_t UNKNOW_ERROR;
  int32_t PARAM_ERROR;
  int32_t NODE_NOT_EXIST;
  int32_t NOT_EXIST_VERSION;
  int32_t DEPRECATED_VERSION;
  int32_t NODE_DELETED;
};

extern const config_commonConstants g_config_common_constants;



#endif
