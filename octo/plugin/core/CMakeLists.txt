SET(CMAKE_CXX_FLAGS "-g -Wall -O2 -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H")
MESSAGE(STATUS "CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS}")

#SET(CURL_LIBRARY ${CMAKE_SOURCE_DIR}/third_party/curl/lib/libcurl.a)

INCLUDE_DIRECTORIES(/usr/include)
INCLUDE_DIRECTORIES(/usr/include/thrift)
#INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/thrift/include)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/core/gen-cpp)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/core)
#INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/curl/include)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/glog/include)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/../common/cpp/lib/rapidjson/include)

SET(CORE_SOURCES_PATH ${CMAKE_SOURCE_DIR}/core)

SET(UTIL_SOURCES_PATH ${CMAKE_SOURCE_DIR}/core/util)

SET(UTIL_SOURCES
        ${UTIL_SOURCES_PATH}/tinyxml2.cc
        ${UTIL_SOURCES_PATH}/http_client.cc
        ${UTIL_SOURCES_PATH}/downloader.cc
        ${UTIL_SOURCES_PATH}/net_util.cc
        ${UTIL_SOURCES_PATH}/config_util.cc
        ${UTIL_SOURCES_PATH}/ipc_message.cc
        ${UTIL_SOURCES_PATH}/perf.cc
        ${UTIL_SOURCES_PATH}/ops.cc
        ${UTIL_SOURCES_PATH}/process.cc
        ${UTIL_SOURCES_PATH}/cplugin_common.cc
        ${UTIL_SOURCES_PATH}/sg_agent.cc
        ${UTIL_SOURCES_PATH}/md5_util.cpp
        ${UTIL_SOURCES_PATH}/json_util.cpp
        ${UTIL_SOURCES_PATH}/log4cplus.cpp
        )
SET(GEN_SOURCES
        ${CORE_SOURCES_PATH}/gen-cpp/cplugin_config_common_types.cpp
        ${CORE_SOURCES_PATH}/gen-cpp/cplugin_sgagent_common_types.cpp
        ${CORE_SOURCES_PATH}/gen-cpp/Core.cpp
        ${CORE_SOURCES_PATH}/gen-cpp/core_constants.cpp
        ${CORE_SOURCES_PATH}/gen-cpp/core_types.cpp
        ${CORE_SOURCES_PATH}/controlServer/ControllerService.cpp
        ${CORE_SOURCES_PATH}/controlServer/controlServer_constants.cpp
        ${CORE_SOURCES_PATH}/controlServer/controlServer_types.cpp
        )

SET(CORE_SOURCES
  ${CORE_SOURCES_PATH}/host_manager.cc
  ${CORE_SOURCES_PATH}/host_process.cc
  ${CORE_SOURCES_PATH}/core_server.cc
  ${CORE_SOURCES_PATH}/version.cc
  ${CORE_SOURCES_PATH}/thrift_client_handler.cc
  ${CORE_SOURCES_PATH}/controlServerManager.cc
  )

ADD_LIBRARY(core ${CORE_SOURCES} ${GEN_SOURCES} ${UTIL_SOURCES})
#TARGET_LINK_LIBRARIES(core ${CURL_LIBRARY} ${GLOG_LIBRARY} ${THRIFTNB_LIBRARY} ${THRIFT_LIBRARY} ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} event pthread dl ssl)

ADD_EXECUTABLE(cplugin ${CORE_SOURCES_PATH}/main.cc)
TARGET_LINK_LIBRARIES(cplugin core  ${GLOG_LIBRARY} thriftnb log4cplus thrift ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} boost_system ${LIBCURL_LIBRARY} event pthread dl ssl crypto z)

# tests
SET(CORE_TEST_SOURCES
  ${CORE_SOURCES_PATH}/test/http_client_unittest.cc
  ${CORE_SOURCES_PATH}/test/plugin_process_unittest.cc)

INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR})

#ADD_EXECUTABLE(unittest ${CORE_SOURCES_PATH}/test/unittest.cc ${CORE_TEST_SOURCES})
#TARGET_LINK_LIBRARIES(unittest core /usr/lib64/libgtest.so) 
