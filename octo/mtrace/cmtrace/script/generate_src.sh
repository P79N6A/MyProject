#!/bin/bash

HOME_DIR=".."
THRIFT_INCLUDE_DIR="${HOME_DIR}/thrift"
MTRACE_INCLUDE_DIR="${HOME_DIR}/mtrace/include"
MTRACE_SRC_DIR="${HOME_DIR}/mtrace/src"
MTRACE_LIB_DIR="${HOME_DIR}/lib"
DESTINATION_DIR="${HOME_DIR}/bin"
LIB_DIR="/usr/local/lib"
BIN_FILE="${DESTINATION_DIR}/test_src"
SRC_FILE=" $MTRACE_SRC_DIR/CommonTools.cpp $MTRACE_SRC_DIR/EndPoint.cpp $MTRACE_SRC_DIR/ExceptionLog.cpp $MTRACE_SRC_DIR/RemoteProcessCall.cpp $MTRACE_SRC_DIR/Sample.cpp $MTRACE_SRC_DIR/MTrace.cpp $MTRACE_SRC_DIR/InfomationCollect.cpp"

rm -rf *.o
rm -rf ${BIN_FILE}

#2.根据动态连接库生成bin文件
export LD_LIBRARY_PATH=${HOME_DIR}/lib:${LD_LIBRARY_PATH}

mkdir -p ${DESTINATION_DIR}
g++ -g -DHAVE_NETINET_IN_H -o ${BIN_FILE} -I${THRIFT_INCLUDE_DIR} -I${MTRACE_INCLUDE_DIR} -L${LIB_DIR} -L${MTRACE_LIB_DIR} -pthread -loctoidl -lthrift -luuid ${SRC_FILE} test.cpp

ulimit -c unlimited
${BIN_FILE}
#valgrind --tool=memcheck --leak-check=full ${BIN_FILE}
