#!/bin/bash

#1.生成动态链接库
HOME_DIR=".."
THRIFT_INCLUDE_DIR="${HOME_DIR}/thrift"
MTRACE_INCLUDE_DIR="${HOME_DIR}/mtrace/include"
MTRACE_SRC_DIR="${HOME_DIR}/mtrace/src"
MTRACE_LIB_DIR="${HOME_DIR}/lib"
DESTINATION_DIR="${HOME_DIR}/bin"
LIB_DIR="/usr/local/lib"
BIN_FILE="${DESTINATION_DIR}/test_so"
SRC_FILE=" $MTRACE_SRC_DIR/CommonTools.cpp $MTRACE_SRC_DIR/EndPoint.cpp $MTRACE_SRC_DIR/ExceptionLog.cpp $MTRACE_SRC_DIR/RemoteProcessCall.cpp $MTRACE_SRC_DIR/Sample.cpp $MTRACE_SRC_DIR/MTrace.cpp $MTRACE_SRC_DIR/InfomationCollect.cpp"

rm -rf *.o
rm -rf ${BIN_FILE}

g++ -O0 -DHAVE_NETINET_IN_H ${SRC_FILE} -I${THRIFT_INCLUDE_DIR} -I${MTRACE_INCLUDE_DIR} -fPIC -shared -o ${MTRACE_LIB_DIR}/libpromtrace.so

#2.根据动态连接库生成bin文件
export LD_LIBRARY_PATH=${HOME_DIR}/lib:${LD_LIBRARY_PATH}

mkdir -p ${DESTINATION_DIR}

g++ -O0 -g -D HAVE_NETINET_IN_H test.cpp -o ${BIN_FILE} -I${MTRACE_INCLUDE_DIR} -L${LIB_DIR} -L${MTRACE_LIB_DIR} -lpromtrace -pthread -lthrift -luuid -loctoidl

ulimit -c unlimited
${BIN_FILE}
