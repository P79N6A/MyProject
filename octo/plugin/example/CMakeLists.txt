SET(CMAKE_CXX_FLAGS "-g -Wall -O2 -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H")
MESSAGE(STATUS "CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS}")

SET(THRIFT_GEN_CPP_PATH ${CMAKE_SOURCE_DIR}/example/gen-cpp)
SET(EXAMPLE_SRC_PATH ${CMAKE_SOURCE_DIR}/example)

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
   ) 

INCLUDE_DIRECTORIES(/usr/include)
INCLUDE_DIRECTORIES(/usr/include/thrift)
#INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/thrift/include)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/glog/include)
INCLUDE_DIRECTORIES(${THRIFT_GEN_CPP_PATH})
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR})

ADD_EXECUTABLE(client ${EXAMPLE_SRC_PATH}/client.cc ${EXAMPLE_SRC_PATH}/SGAgentHandler_v2.cc ${THRIFT_GENCPP_SRCS})
TARGET_LINK_LIBRARIES(client core thrift thriftnb event)

ADD_EXECUTABLE(start ${EXAMPLE_SRC_PATH}/start.cc)
TARGET_LINK_LIBRARIES(start core thriftnb thrift event)

ADD_EXECUTABLE(restart ${EXAMPLE_SRC_PATH}/restart.cc)
TARGET_LINK_LIBRARIES(restart core thriftnb thrift event)

ADD_EXECUTABLE(stop ${EXAMPLE_SRC_PATH}/stop.cc)
TARGET_LINK_LIBRARIES(stop core thrift thriftnb event)

ADD_EXECUTABLE(idc ${EXAMPLE_SRC_PATH}/idc.cc)
TARGET_LINK_LIBRARIES(idc core thrift thriftnb event)

ADD_EXECUTABLE(cplugin_xml ${EXAMPLE_SRC_PATH}/cplugin.cc)
TARGET_LINK_LIBRARIES(cplugin_xml core thrift thriftnb event)

ADD_EXECUTABLE(upgrade ${EXAMPLE_SRC_PATH}/upgrade.cc)
TARGET_LINK_LIBRARIES(upgrade core thrift thriftnb event)

ADD_EXECUTABLE(updatefile ${EXAMPLE_SRC_PATH}/updatefile.cc)
TARGET_LINK_LIBRARIES(updatefile core thrift thriftnb event)


ADD_EXECUTABLE(rollback ${EXAMPLE_SRC_PATH}/rollback.cc)
TARGET_LINK_LIBRARIES(rollback core thrift thriftnb event)

ADD_EXECUTABLE(testServer ${EXAMPLE_SRC_PATH}/testServer.cc)
TARGET_LINK_LIBRARIES(testServer core thriftnb thrift event)

ADD_EXECUTABLE(getMonitor ${EXAMPLE_SRC_PATH}/getMoniter.cc)
TARGET_LINK_LIBRARIES(getMonitor core thriftnb thrift event)


#ADD_LIBRARY(server SHARED ${EXAMPLE_SRC_PATH}/libserver.cc)
#TARGET_LINK_LIBRARIES(server -Xlinker "-(" ${GLOG_LIBRARY} ${THRIFT_LIBRARY} ${THRIFTNB_LIBRARY} ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} event dl pthread rt -Xlinker "-)")

#ADD_LIBRARY(sgagent_v1 SHARED ${EXAMPLE_SRC_PATH}/libsgagent_v1.cc ${EXAMPLE_SRC_PATH}/SGAgentHandler_v1.cc ${THRIFT_GENCPP_SRCS})
#TARGET_LINK_LIBRARIES(sgagent_v1 -Xlinker "-(" ${GLOG_LIBRARY} ${THRIFT_LIBRARY} ${THRIFTNB_LIBRARY} ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} event dl pthread rt -Xlinker "-)")

#ADD_LIBRARY(sgagent_v2 SHARED ${EXAMPLE_SRC_PATH}/libsgagent_v2.cc ${EXAMPLE_SRC_PATH}/SGAgentHandler_v2.cc ${THRIFT_GENCPP_SRCS})
#TARGET_LINK_LIBRARIES(sgagent_v2 -Xlinker "-(" ${GLOG_LIBRARY} ${THRIFT_LIBRARY} ${THRIFTNB_LIBRARY} ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} event dl pthread rt -Xlinker "-)")

ADD_LIBRARY(calc_test SHARED ${EXAMPLE_SRC_PATH}/libcalc_test.cc ${EXAMPLE_SRC_PATH}/../core/plugin_manager.cc)
TARGET_LINK_LIBRARIES(calc_test -Xlinker "-(" ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} dl pthread -Xlinker "-)") 

ADD_LIBRARY(calc_v1 SHARED ${EXAMPLE_SRC_PATH}/libcalc_v1.cc)
TARGET_LINK_LIBRARIES(calc_v1 -Xlinker "-(" ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} dl pthread -Xlinker "-)") 

ADD_LIBRARY(calc_v2 SHARED ${EXAMPLE_SRC_PATH}/libcalc_v2.cc)
TARGET_LINK_LIBRARIES(calc_v2 -Xlinker "-(" ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY} dl pthread -Xlinker "-)")
