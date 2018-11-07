SET(CMAKE_CXX_FLAGS "-g -Wall -O0 -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -DGTEST")
MESSAGE(STATUS "CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS}")

INCLUDE_DIRECTORIES(SYSTEM ${GTEST_INCLUDE_DIR})

SET(SYSTEM_INCLUDE_PATH /usr/include/)
MESSAGE(STATUS "SYSTEM_INCLUDE_PATH ${SYSTEM_INCLUDE_PATH}")

INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH})
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH}/thrift)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/core/gen-cpp)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/third_party/glog/include)

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

SET(TEST_SOURCES
         unit_test.cpp
		 unit_test_octoidl.cpp
		 unit_test_host_manager.cpp
		 unit_test_control_server_list.cpp
        )


	ADD_EXECUTABLE(unittest main.cpp ${UTIL_SOURCES} ${GEN_SOURCES} ${CORE_SOURCES} ${TEST_SOURCES})
TARGET_LINK_LIBRARIES(unittest -Xlinker "-("  ${GLOG_LIBRARY} thriftnb log4cplus thrift ${MUDUO_NET_LIBRARY} ${MUDUO_BASE_LIBRARY}
         boost_system ${LIBCURL_LIBRARY} event pthread dl ssl libgtest.a -Xlinker "-)")
