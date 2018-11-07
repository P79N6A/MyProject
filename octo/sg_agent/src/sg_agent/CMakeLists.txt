cmake_minimum_required(VERSION 2.8)

INCLUDE_DIRECTORIES(${BOOST_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${MUDUO_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${MAFKA_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${CTHRIFT_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${THRIFT_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${THRIFT_INCLUDE_DIR}/thrift)
INCLUDE_DIRECTORIES(${LOG4CPLUS_PATH})
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR})
INCLUDE_DIRECTORIES(${ZOOKEEPER_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${THRIFT_GEN_CPP_PATH})
INCLUDE_DIRECTORIES(${CORE_PATH})
INCLUDE_DIRECTORIES(${SGAGENT_SRC_PATH})

SET(LIBSG_AGENT_SRCS
        sgcommon_invoker.cpp
        auth_client.cpp
        config_client.cpp
        file_config_client.cpp
        quota_client.cpp
        regist_client.cpp
        common_log_collector.cpp
        status_mgr.cpp
        version_operation.cpp
        sg_agent_conf.cpp
        mns.cpp
        sg_agent_init.cpp
        http_service.cpp
        count_request.cpp
        )

SET(ZOOKEEPER_SRCS
        zk_client.cpp
        quota_zk_client.cpp
        registe_zk_client.cpp
        zk_tools.cpp
        )

SET(COLLECTOR_SRCS
        remote_log_collector.cpp
        thrift_client_handler.cpp
        quota_client.cpp
        mtconfig_server_client.cpp
        ../remote/mafka/mafka_client.cpp
        mnscache_client.cpp
        )

SET(SGAGENT_LIB_SRCS
        SGAgentHandler.cpp
        )

SET(THRIFT_GENCPP_SRCS
        ${THRIFT_GEN_CPP_PATH}/SGAgent.cpp
        ${THRIFT_GEN_CPP_PATH}/config_common_types.cpp
        ${THRIFT_GEN_CPP_PATH}/config_common_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/config_data_types.cpp
        ${THRIFT_GEN_CPP_PATH}/config_data_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/quota_common_types.cpp
        ${THRIFT_GEN_CPP_PATH}/quota_common_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_common_types.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_common_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_service_types.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_service_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_worker_service_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/sgagent_worker_service_types.cpp
        ${THRIFT_GEN_CPP_PATH}/aggregator_common_types.cpp
        ${THRIFT_GEN_CPP_PATH}/aggregator_common_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/aggregator_service_types.cpp
        ${THRIFT_GEN_CPP_PATH}/AggregatorService.cpp
        ${THRIFT_GEN_CPP_PATH}/MtConfigService.cpp
        ${THRIFT_GEN_CPP_PATH}/MNSCacheService.cpp
        ${THRIFT_GEN_CPP_PATH}/mnsc_data_constants.cpp
        ${THRIFT_GEN_CPP_PATH}/mnsc_data_types.cpp
        ${THRIFT_GEN_CPP_PATH}/sandbox_common_types.cpp
        ${THRIFT_GEN_CPP_PATH}/appkey_types.cpp
        ${THRIFT_GEN_CPP_PATH}/appkey_constants.cpp
        )

SET(SGAGENT_UTIL_SRCS
        ${UTIL_PATH}/sg_agent_env.cpp
        ${UTIL_PATH}/sgagent_stat.cpp
        ${UTIL_PATH}/sgagent_filter.cpp
        ${UTIL_PATH}/switch_operate.cpp
        ${UTIL_PATH}/appkey_path_operation.cpp
        ${UTIL_PATH}/gen_zk_path_operation.cpp
        ${UTIL_PATH}/sgagent_shutdown.cpp
        ${UTIL_PATH}/sgservice_misc.cpp
        ${UTIL_PATH}/sgservice_manager.cpp
        ${UTIL_PATH}/falcon_mgr.cpp
        ${UTIL_PATH}/whitelist_mgr.cpp
        ${UTIL_PATH}/zk_version_check.cpp
        ${UTIL_PATH}/json_zk_mgr.cpp
        ${UTIL_PATH}/idc.cpp
        ${UTIL_PATH}/idc_util.cpp
        ${UTIL_PATH}/global_def.cpp
        ${UTIL_PATH}/config_adapter.cpp


        )

SET(SGAGENT_COMM_SRCS
        ${COMMON_PATH}/tinyxml2.cpp
        ${COMMON_PATH}/md5.cpp
        ${COMMON_PATH}/cJSON.c
        ${COMMON_PATH}/log4cplus.cpp
        )

SET(FALCON_SRCS
        ../remote/falcon/falcon_counter.cpp
        ../remote/falcon/falcon_collector.cpp
        ../remote/monitor/monitor_collector.cpp
        )

SET(CPLUGIN_SRCS
        ${CORE_PATH}/core/plugin_manager.cc count_request.h count_request.cpp)
#add_executable(sg_agent main.cpp libsgagent.cpp ${CLIENT_SRCS}
add_library(sg_agent SHARED libsgagent.cpp ${LIBSG_AGENT_SRCS}
        ${COLLECTOR_SRCS} ${SGAGENT_LIB_SRCS} ${THRIFT_GENCPP_SRCS}
        ${SGAGENT_UTIL_SRCS} ${SGAGENT_COMM_SRCS} ${ZOOKEEPER_SRCS} ${CPLUGIN_SRCS} ${FALCON_SRCS})
target_link_libraries(sg_agent -Xlinker "-(" ${MUDUO_NET_LIBRARIES}
        ${MAFKA_LIBRARY} ${THRIFTNB_STATIC_LIB} ${THRIFT_STATIC_LIB} ${CTHRIFT_LIBRARY} ${CLOG_LIBRARY}
        ${ZOOKEEPER_LIBRARY} ${CURL_LIBRARY} log4cplus boost_system event uuid ssl dl pthread rt -Xlinker "-)")
