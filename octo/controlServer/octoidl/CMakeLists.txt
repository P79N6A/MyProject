cmake_minimum_required(VERSION 2.6)

INCLUDE_DIRECTORIES(SYSTEM ${INCLUDE_OUTPUT_PATH}/thrift)

SET(OCTOIDL_SRC
        SGAgent.cpp
        appkey_constants.cpp
        appkey_types.cpp
        config_common_types.cpp
        config_common_constants.cpp
        quota_common_types.cpp
        quota_common_constants.cpp
        sgagent_common_types.cpp
        sgagent_common_constants.cpp
        sgagent_service_types.cpp
        sgagent_service_constants.cpp
        sgagent_worker_service_constants.cpp
        sgagent_worker_service_types.cpp
        aggregator_common_types.cpp
        aggregator_common_constants.cpp
        )

MESSAGE("OCTOIDL_SRC ${OCTOIDL_SRC}")

ADD_LIBRARY(octoidl STATIC ${OCTOIDL_SRC})
ADD_LIBRARY(octoidl_dynamic SHARED ${OCTOIDL_SRC})

SET_TARGET_PROPERTIES(octoidl_dynamic PROPERTIES OUTPUT_NAME "octoidl")

