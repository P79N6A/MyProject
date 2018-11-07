IF(ENABLE_COVERAGE)
SET(CMAKE_CXX_FLAGS "-g -Wall -O0 -fPIC -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -DGTEST")
ELSE()
SET(CMAKE_CXX_FLAGS "-g -Wall -O2 -fPIC -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -DGTEST")
ENDIF()
SET(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake")
MESSAGE(STATUS "CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH}")

IF(ENABLE_COVERAGE)
    include(CodeCoverage)
	APPEND_COVERAGE_COMPILER_FLAGS()
ENDIF()
MESSAGE(STATUS "CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS}")

INCLUDE_DIRECTORIES(SYSTEM ${GTEST_INCLUDE_DIR})
INCLUDE_DIRECTORIES(SYSTEM ${INCLUDE_ECHO_OUTPUT_PATH})

INCLUDE_DIRECTORIES(/root/Git/octo/mtransport/cthrift/build/include)
LINK_DIRECTORIES(/root/Git/octo/mtransport/cthrift/build/lib)

#ADD_EXECUTABLE(feature_test ${ECHO_THRIFT_SRCS} feature_test.cc)
#TARGET_LINK_LIBRARIES(feature_test -Xlinker "-(" libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
#        libthrift.a ${GTEST_LIBRARY} -Xlinker "-)")

ADD_EXECUTABLE(mns_test mns_test.cc)
TARGET_LINK_LIBRARIES(mns_test -Xlinker "-(" gcov libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
        libthrift.a clog cmtraceV2 octoidl uuid ${GTEST_LIBRARY} -Xlinker "-)")

ADD_EXECUTABLE(mns_thread_test mns_thread_test.cc)
TARGET_LINK_LIBRARIES(mns_thread_test -Xlinker "-(" gcov libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
        libthrift.a clog cmtraceV2 octoidl ${GTEST_LIBRARY} uuid  -Xlinker "-)")
ADD_EXECUTABLE(mns_mcc_test mns_mcc_test.cc)
TARGET_LINK_LIBRARIES(mns_mcc_test -Xlinker "-(" gcov libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
        libthrift.a boost_filesystem boost_thread clog cmtraceV2 octoidl uuid ${GTEST_LIBRARY} -Xlinker "-)")

ADD_EXECUTABLE(mcc_sdk_unittest main.cc mcc_sdk_unittest.cc)
TARGET_LINK_LIBRARIES(mcc_sdk_unittest -Xlinker "-(" gcov libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
        libthrift.a boost_filesystem boost_thread clog cmtraceV2 octoidl uuid ${GTEST_LIBRARY} -Xlinker "-)")

ADD_EXECUTABLE(mcc_sdk_mocktest main.cc  mcc_sdk_mocktest.cc)
TARGET_LINK_LIBRARIES(mcc_sdk_mocktest -Xlinker "-(" libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event dl rt z libthriftnb.a
        libthrift.a boost_filesystem boost_thread clog cmtraceV2 octoidl uuid ${GTEST_LIBRARY} -Xlinker "-)")

ADD_EXECUTABLE(mcc_sdk_thread_test mcc_sdk_thread_test.cc)
TARGET_LINK_LIBRARIES(mcc_sdk_thread_test -Xlinker "-(" gcov libcthrift.a libmuduo_net.a libmuduo_base.a libmuduo_http.a pthread event rt z libthriftnb.a
        libthrift.a boost_filesystem boost_thread clog cmtraceV2 octoidl uuid ${GTEST_LIBRARY} -Xlinker "-)")
#[[ADD_EXECUTABLE(cthrift_client_worker_unittest ${ECHO_THRIFT_SRCS} cthrift_client_worker_unittest.cc)
#TARGET_LINK_LIBRARIES(cthrift_client_worker_unittest -Xlinker "-(" cthrift muduo_net muduo_base muduo_http pthread uuid event rt z thriftnb thrift ${GTEST_LIBRARY} -Xlinker "-)")]]

#[[
#INSTALL(CODE "execute_process(COMMAND find ${CTHRIFT_SRC_PATH}/cthrift/tests -name *_test*.cc -exec cp -t ${EXAMPLE_CTHRIFT_OUTPUT_PATH} {} \;)")]]
