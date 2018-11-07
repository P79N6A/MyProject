cmake_minimum_required(VERSION 2.6)

#SET(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/../bin)
SET(EXECUTABLE_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH})

IF (NOT EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/plugin)
    EXECUTE_PROCESS(COMMAND thrift --gen cpp plugin.thrift
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
    EXECUTE_PROCESS(COMMAND mv gen-cpp ./plugin
			WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
ENDIF (NOT EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/plugin)

IF (NOT EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/gen-cpp)
    EXECUTE_PROCESS(COMMAND thrift --gen cpp controller.thrift
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
ENDIF (NOT EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/gen-cpp)

MESSAGE(STATUS "SYSTEM_INCLUDE_PATH ${SYSTEM_INCLUDE_PATH}")
SET(SYSTEM_INCLUDE_PATH /usr/include/)
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH})
INCLUDE_DIRECTORIES(SYSTEM /usr/include/mysql/)
INCLUDE_DIRECTORIES(SYSTEM /usr/local/include/mysql++/)
INCLUDE_DIRECTORIES(SYSTEM ./gen-cpp/)
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH}/thrift)


SET(IDL_THRIFT_SRCS
        gen-cpp/ControllerService.cpp
        gen-cpp/controller_types.cpp
        gen-cpp/controller_constants.cpp)

SET(PLUGIN_THRIFT_SRCS
        plugin/plugin_types.cpp
        plugin/plugin_constants.cpp
        plugin/Core.cpp)

SET(SRCS
        main.cpp
        storage.cpp
        common.cpp
        ControlServer.cpp
        ops_fetcher.cpp
        task_queue.cpp
        thread_pool.cpp)

ADD_EXECUTABLE(cplugin_control_server ${IDL_THRIFT_SRCS} ${PLUGIN_THRIFT_SRCS} ${SRCS})
TARGET_LINK_LIBRARIES(cplugin_control_server -Xlinker "-(" libcthrift.a libclog.a libcmtraceV2.a libcatclient.a liboctoidl.a libthrift.a libmuduo_net.a
libmuduo_base.a curl pthread mysqlpp rt z uuid boost_filesystem boost_thread -Xlinker "-)")

ADD_SUBDIRECTORY(tests)
