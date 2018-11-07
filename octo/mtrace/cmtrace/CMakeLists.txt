cmake_minimum_required(VERSION 2.6)


SET(HOME_DIR ./)
SET(MTRACE_SRC_DIR  ${HOME_DIR}/mtrace/src)
#INCLUDE_DIRECTORIES(SYSTEM  ${HOME_DIR})
INCLUDE_DIRECTORIES(SYSTEM  ${HOME_DIR}/thrift)
INCLUDE_DIRECTORIES(SYSTEM  ${HOME_DIR}/mtrace/include)

file(GLOB_RECURSE GEN_CPP   mtrace/cmtrace-gen-cpp/*.cpp)

SET(MTRACE_SRC
         ${MTRACE_SRC_DIR}/CommonTools.cpp 
		 ${MTRACE_SRC_DIR}/EndPoint.cpp 
		 ${MTRACE_SRC_DIR}/ExceptionLog.cpp 
		 ${MTRACE_SRC_DIR}/RemoteProcessCall.cpp 
		 ${MTRACE_SRC_DIR}/Sample.cpp 
		 ${MTRACE_SRC_DIR}/MTrace.cpp 
		 ${MTRACE_SRC_DIR}/InfomationCollect.cpp
        )

	ADD_LIBRARY(cmtraceV2 STATIC ${MTRACE_SRC} ${GEN_CPP})
	ADD_LIBRARY(cmtraceV2_dynamic SHARED ${MTRACE_SRC} ${GEN_CPP})

SET_TARGET_PROPERTIES(cmtraceV2_dynamic PROPERTIES OUTPUT_NAME "cmtraceV2")

