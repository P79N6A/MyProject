INCLUDE_DIRECTORIES(${THRIFT_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${CORE_PATH})
INCLUDE_DIRECTORIES(${MUDUO_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${LOG4CPLUS_PATH})
INCLUDE_DIRECTORIES(${ZOOKEEPER_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${THRIFT_GEN_CPP_PATH})
INCLUDE_DIRECTORIES(${SGAGENT_SRC_PATH})

SET(PLUGIN_SRCS
        mns_impl.cpp
        mns_zk_client.cpp
        mns_zk_tools.cpp
        )

SET(SGAGENT_UTIL_SRCS
        ${UTIL_PATH}/falcon_mgr.cpp
        ${UTIL_PATH}/whitelist_mgr.cpp
				${UTIL_PATH}/json_zk_mgr.cpp
        ${UTIL_PATH}/zk_version_check.cpp
        ${UTIL_PATH}/appkey_path_operation.cpp
        ${UTIL_PATH}/gen_zk_path_operation.cpp
        ${UTIL_PATH}/sgagent_stat.cpp
        ${UTIL_PATH}/sgservice_misc.cpp
        ${UTIL_PATH}/sgagent_filter.cpp
        ${UTIL_PATH}/idc.cpp
        ${UTIL_PATH}/idc_util.cpp
        )

SET(SGAGENT_COMM_SRCS
        ${COMMON_PATH}/tinyxml2.cpp
				${COMMON_PATH}/md5.cpp
        ${COMMON_PATH}/cJSON.c
        ${COMMON_PATH}/log4cplus.cpp
        #        ${COMMON_PATH}/ip_router.cpp
		)

ADD_LIBRARY(mns SHARED ${MNS_PATH}/libmns.cpp ${SGAGENT_UTIL_SRCS} ${THRIFT_GENCPP_SRCS} ${PLUGIN_SRCS} ${SGAGENT_COMM_SRCS} ${THRIFT_GENCPP_SRCS})
#add_executable(mns main.cpp ${MNS_PATH}/libmns.cpp ${SGAGENT_UTIL_SRCS} ${PLUGIN_SRCS} ${SGAGENT_COMM_SRCS})
TARGET_LINK_LIBRARIES(mns -Xlinker "-(" ${MUDUO_NET_LIBRARIES} ${THRIFTNB_STATIC_LIB} ${THRIFT_STATIC_LIB} ${ZOOKEEPER_LIBRARY} log4cplus dl pthread rt -Xlinker "-)" -Wl,--version-script=mns_symbol.map)

