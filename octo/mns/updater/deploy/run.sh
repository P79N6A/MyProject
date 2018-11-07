# ------------------------------------
# default jvm args if you do not config in /jetty/boot.ini
# ------------------------------------
JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"
JVM_ARGS=${JVM_ARGS}" -Denv="${CURRENT_ENV}

JVM_GC="-XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"
JVM_GC=$JVM_GC" -XX:CMSFullGCsBeforeCompaction=0 -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=80"
JVM_HEAP="-XX:SurvivorRatio=8 -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:ReservedCodeCacheSize=128m -XX:InitialCodeCacheSize=128m"
JVM_SIZE="-Xmx4g -Xms4g"


# ------------------------------------
# insert you init shell code here in preRun() function
# ------------------------------------
function preRun() {
	echo "preRun..."
	# if [[ $CURRENT_ENV == "offline" ]]; then
	# 	echo "preRun offline..."
	# else
	# 	echo "preRun online..."
	# fi
}


# ------------------------------------
# do not edit
# ------------------------------------
MODULE=updater
WORK_PATH=/opt/meituan/apps/updater
LOG_PATH=/opt/logs/apps/updater
WEB_ROOT=$WORK_PATH/webroot


function init() {
	preRun
	mkdir -p $LOG_PATH
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
#		ulimit -n 1024000
#		umask 000
        echo "..."
	else
		echo $EXEC_JAVA
	fi

	${EXEC_JAVA} -jar updater-release-1.0.0-SNAPSHOT.jar 2>&1
}


# ------------------------------------
# actually work
# ------------------------------------

init
run
