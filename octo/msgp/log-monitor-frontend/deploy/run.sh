# ------------------------------------
# default jvm args if you do not config in /jetty/boot.ini 
# ------------------------------------
JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"
JVM_ARGS=${JVM_ARGS}" -Denv="${CURRENT_ENV}

JVM_GC="-XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy"
JVM_GC=${JVM_GC}" -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JVM_HEAP="-XX:+HeapDumpOnOutOfMemoryError"
JVM_SIZE="-Xmx6g -Xms6g"

# ------------------------------------
# insert you init shell code here in preRun() function
# ------------------------------------
function preRun() {
	echo "preRun..."
	if [[ $CURRENT_ENV == "offline" ]]; then
		JVM_SIZE="-Xmx6g -Xms6g"
		echo "offline jvm: ${JVM_SIZE}"
	else
	 	JVM_SIZE="-Xmx6g -Xms6g"
	 	echo "online jvm: ${JVM_SIZE}"
    fi
}

# ------------------------------------
# do not edit
# ------------------------------------
MODULE=log-monitor-frontend
WORK_PATH=/opt/meituan/apps/log-monitor-frontend
LOG_PATH=/opt/logs/apps/log-monitor-frontend

function init() {
	preRun
	mkdir -p $LOG_PATH
}

function run() {
	EXEC="exec"

	EXEC_JAVA="$EXEC /usr/local/java8/bin/java $JVM_ARGS $JVM_SIZE $JVM_HEAP $JVM_JIT $JVM_GC"
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

	${EXEC_JAVA} -jar log-monitor-frontend.jar > /tmp/log-monitor-frontend.log 2>&1
}

# ------------------------------------
# actually work
# ------------------------------------

init
run
