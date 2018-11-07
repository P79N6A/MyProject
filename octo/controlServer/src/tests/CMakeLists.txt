SET(CMAKE_CXX_FLAGS "-g -Wall -O0 -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -DGTEST")
MESSAGE(STATUS "CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS}")

INCLUDE_DIRECTORIES(SYSTEM ${GTEST_INCLUDE_DIR})
INCLUDE_DIRECTORIES(SYSTEM ${GTEST_MOCK_INCLUDE_DIR})

MESSAGE(STATUS "SYSTEM_INCLUDE_PATH ${SYSTEM_INCLUDE_PATH}")
SET(SYSTEM_INCLUDE_PATH /usr/include/)
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH})
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH}/thrift)


SET(IDL_THRIFT_SRCS
        ../gen-cpp/ControllerService.cpp
        ../gen-cpp/controller_types.cpp
        ../gen-cpp/controller_constants.cpp)

SET(PLUGIN_THRIFT_SRCS
        ../plugin/plugin_types.cpp
        ../plugin/plugin_constants.cpp
        ../plugin/Core.cpp)

SET(CONTROL_SRCS
        ../ops_fetcher.cpp
        ../common.cpp
        ../storage.cpp
        ../thread_pool.cpp)

SET(TEST_SRCS
        rpc_unittest.cpp)

ADD_EXECUTABLE(unittest main.cpp ${IDL_THRIFT_SRCS} ${PLUGIN_THRIFT_SRCS} ${CONTROL_SRCS} ${TEST_SRCS})
TARGET_LINK_LIBRARIES(unittest -Xlinker "-(" liboctoidl.a libthrift.a libcthrift.a libcmtraceV2.a libcatclient.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread mysqlpp clog curl rt z uuid boost_filesystem boost_thread ${GTEST_LIBRARY} ${GTEST_MOCK_LIBRARY} -Xlinker "-)")
