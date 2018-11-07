cmake_minimum_required(VERSION 2.6)

INCLUDE_DIRECTORIES(SYSTEM ${INCLUDE_OUTPUT_PATH}/thrift)
INCLUDE_DIRECTORIES(SYSTEM ${RAPIDJSON_INCLUDE_DIR})
INCLUDE_DIRECTORIES(SYSTEM ${KMS_LIB_INCLUDE_PATH})
INCLUDE_DIRECTORIES(SYSTEM  ${OCTOIDL_SRC_PATH})
INCLUDE_DIRECTORIES(SYSTEM  ${CMTRACE_THRID_PATH}/../)
INCLUDE_DIRECTORIES(SYSTEM  ${CLOG_THRID_PATH}/../)
#INCLUDE_DIRECTORIES(SYSTEM ${CAT_INCLUDE_DIR})

SET(CTHRIFT_SRC
	cthrift_common.cc
	cthrift_svr.cc
	cthrift_sgagent.cc
	cthrift_transport.cc
	cthrift_client_worker.cc
	cthrift_client.cc
	cthrift_kms.cc
	cthrift_client_channel.cc
	mns_sdk/mns_sdk.cc
	mcc_sdk/mcc_sdk.cc
	mcc_sdk/config_dynamic.cc
	mcc_sdk/config_file.cc
	cthrift_uniform_protocol.cc
	uniform/unifiedProtocol_constants.cpp
	uniform/unifiedProtocol_types.cpp
   )

ADD_LIBRARY(cthrift STATIC ${CTHRIFT_SRC})
ADD_LIBRARY(cthrift_dynamic SHARED ${CTHRIFT_SRC})

SET_TARGET_PROPERTIES(cthrift_dynamic PROPERTIES OUTPUT_NAME "cthrift")

#DO NOT add ' OR " to the path
EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift -path  ${CTHRIFT_SRC_PATH}/cthrift/mns_sdk -prune -o -name *.h -print -exec cp -t ${INCLUDE_CTHRIFT_OUTPUT_PATH} {} \;)

#DO NOT add ' OR " to the path
EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift -path  ${CTHRIFT_SRC_PATH}/cthrift/mns_sdk -prune -o -name *.tcc -print -exec cp -t ${INCLUDE_CTHRIFT_OUTPUT_PATH} {} \;)

EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift/mns_sdk -name *.h -exec cp -t ${INCLUDE_MNS_OUTPUT_PATH} {} \;)
#DO NOT add ' OR " to the path
EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift -path ${CTHRIFT_SRC_PATH}/cthrift/mcc_sdk -prune -o -name *.h -print -exec cp -t ${INCLUDE_CTHRIFT_OUTPUT_PATH} {} \;)

#DO NOT add ' OR " to the path
EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift -path ${CTHRIFT_SRC_PATH}/cthrift/mcc_sdk -prune -o -name *.tcc -print -exec cp -t ${INCLUDE_CTHRIFT_OUTPUT_PATH} {} \;)

EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift/mcc_sdk -name *.h -exec cp -t ${INCLUDE_MCC_OUTPUT_PATH} {} \;)

EXECUTE_PROCESS(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift/uniform -name *.h -exec cp -t ${INCLUDE_UNIFORM_OUTPUT_PATH} {} \;)

if (NOT CMAKE_BUILD_NO_TESTS)
    ADD_SUBDIRECTORY(tests)
endif ()
