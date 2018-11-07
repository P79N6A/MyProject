cmake_minimum_required(VERSION 2.6)

SET(THRIFT_GEN_CPP_PATH ${CTHRIFT_SRC_PATH}/gen-cpp/)
MESSAGE(STATUS "THRIFT_GEN_CPP_PATH ${THRIFT_GEN_CPP_PATH}")

SET(ECHO_THRIFT_SRCS
        ${THRIFT_GEN_CPP_PATH}/echo_constants.cpp      ${THRIFT_GEN_CPP_PATH}/echo_types.cpp
 ${THRIFT_GEN_CPP_PATH}/Echo.cpp)
MESSAGE(STATUS "ECHO_THRIFT_SRCS ${ECHO_THRIFT_SRCS}")


#DO NOT set ECHO_THRIFT_SRCS inside if since non-global avaliable
if (NOT CMAKE_BUILD_NO_EXAMPLES OR NOT CMAKE_BUILD_NO_TEST)
    EXECUTE_PROCESS(COMMAND thrift --gen cpp ${CTHRIFT_SRC_PATH}/example/echo.thrift
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})

    EXECUTE_PROCESS(COMMAND mkdir ${INCLUDE_ECHO_OUTPUT_PATH})

    EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/gen-cpp/ -name *.h -exec cp -t ${INCLUDE_ECHO_OUTPUT_PATH} {} \;)

    EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/gen-cpp/ -name *.cpp -exec cp -t ${INCLUDE_ECHO_OUTPUT_PATH} {} \;)
endif ()

ADD_SUBDIRECTORY(cthrift)

#[[
#if (NOT CMAKE_BUILD_NO_EXAMPLES)
#    ADD_SUBDIRECTORY(example)
#endif ()

#INSTALL(CODE "execute_process(COMMAND find ${CTHRIFT_SRC_PATH}/gen-cpp/ -name *.h -exec cp -t ${INCLUDE_ECHO_OUTPUT_PATH} {} \;)")]]

