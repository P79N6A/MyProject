cmake_minimum_required(VERSION 2.8)

INCLUDE_DIRECTORIES(${BOOST_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${MUDUO_INCLUDE_DIR})

SET(SG_COMMON_SRC
  ${SG_COMMON_PATH}/event_loop_proxy.cc
  ${SG_COMMON_PATH}/event_loop_thread_proxy.cc
)

add_library(sgcommon SHARED ${SG_COMMON_SRC})
target_link_libraries(sgcommon ${MUDUO_NET_LIBRARIES} event pthread rt)
