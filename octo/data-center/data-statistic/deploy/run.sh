#!/bin/bash
# ------------------------------------
# default jvm args if you do not config in /jetty/boot.ini
# ------------------------------------
JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"
JVM_ARGS=${JVM_ARGS}" -Denv="${CURRENT_ENV}
JVM_GC="-XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"
JVM_GC=$JVM_GC" -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=75 -XX:CMSMaxAbortablePrecleanTime=30000 -XX:+ExplicitGCInvokesConcurrent -XX:+CMSParallelRemarkEnabled"
JVM_HEAP="-XX:+HeapDumpOnOutOfMemoryError -XX:+UseStringDeduplication -XX:+PrintStringDeduplicationStatistics -XX:StringTableSize=400003 -XX:+PrintStringTableStatistics"
JVM_SIZE="-Xmx14g -Xms14g -Xmn8g -XX:SurvivorRatio=4 -XX:MaxTenuringThreshold=15"

# ------------------------------------
# insert you init shell code here in preRun() function
# ------------------------------------
function preRun() {
    if [[ ${CURRENT_ENV} == "offline" ]]; then
        echo "preRun offline..."
        echo ' # meituan hbase test env
            10.4.234.138   data-hbase-test01.office.mos     data-hbase-test01
            10.4.235.204   data-hbase-test02.office.mos     data-hbase-test02
            10.4.238.95    data-hbase-test03.office.mos     data-hbase-test03
            10.4.234.130   data-hbase-test04.office.mos     data-hbase-test04
        ' >> /etc/hosts
    else
        echo "preRun online..."
    fi
}


# ------------------------------------
# do not edit
# ------------------------------------
MODULE=data-statistic
WORK_PATH=/opt/meituan/mobile/data-statistic
LOG_PATH=/opt/logs/mobile/data-statistic
WEB_ROOT=$WORK_PATH/webroot

function init() {
    preRun
    #   提取出octo.keytab用于hbase登录
    unzip -o data-statistic-release-0.2.0.jar octo.keytab
    mkdir -p ${LOG_PATH}
}


function run() {
    EXEC="exec"
    EXEC_JAVA="$EXEC java $JVM_ARGS $JVM_SIZE $JVM_HEAP $JVM_JIT $JVM_GC"
    EXEC_JAVA=$EXEC_JAVA" -Xloggc:$LOG_PATH/$MODULE.gc.log -XX:ErrorFile=$LOG_PATH/$MODULE.vmerr.log -XX:HeapDumpPath=$LOG_PATH/$MODULE.heaperr.log"
    if [ "$JVM_JMX" != "" ]; then
        JVM_JMX_PORT=`expr $PORT '+' 10000`
        EXEC_JAVA=$EXEC_JAVA" -Dcom.sun.management.jmxremote.port=$JVM_JMX_PORT $JVM_JMX"
    fi

    if [ "$JVM_DEBUG" != "" ]; then
        JVM_DEBUG_PORT=`expr $PORT '+' 20000`
        EXEC_JAVA=$EXEC_JAVA" $JVM_DEBUG,address=$JVM_DEBUG_PORT"
    fi

    EXEC_JAVA=$EXEC_JAVA" $JAVA_ARGS"

    if [ "$UID" = "0" ]; then
    #       ulimit -n 1024000
    #       umask 000
        echo "..."
    else
        echo $EXEC_JAVA
    fi

    ${EXEC_JAVA} -jar data-statistic-release-0.2.0.jar > /tmp/$MODULE.start.log 2>&1
}

# ------------------------------------
# actually work
# ------------------------------------

cd $WORK_PATH
init
run