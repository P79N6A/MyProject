# ------------------------------------
# default jvm args if you do not config in /jetty/boot.ini 
# ------------------------------------
JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"
JVM_GC="-XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"
JVM_GC=$JVM_GC" -XX:CMSFullGCsBeforeCompaction=0 -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=80"
JVM_HEAP="-XX:SurvivorRatio=8 -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:ReservedCodeCacheSize=128m -XX:InitialCodeCacheSize=128m"
JVM_SIZE="-Xmx6g -Xms6g -Xmn4g -Xss256k"


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
MODULE=msgp-errorlog
WORK_PATH=/opt/meituan/apps/msgp-errorlog
LOG_PATH=/opt/logs/apps/msgp-errorlog
WEB_ROOT=$WORK_PATH/webroot
#异常日志目录
LOG_CENTER_ERRORLOG_PATH=/opt/logs/data_collector/octo_errorlog_logservice


function init() {
	preRun
	mkdir -p $LOG_PATH
	mkdir -p $LOG_CENTER_ERRORLOG_PATH
}


function run() {
	EXEC="exec"
	CONTEXT=/

	cd $WEB_ROOT

	if [ -e "WEB-INF/classes/release" ]; then
		cp -rf WEB-INF/classes/release/* WEB-INF/classes
	fi

	if [ -e "WEB-INF/classes/jetty/boot.ini" ]; then
		source WEB-INF/classes/jetty/boot.ini
	fi

	CLASSPATH=WEB-INF/classes
	for i in WEB-INF/lib/*
	do
		CLASSPATH=$CLASSPATH:$i
	done

	export CLASSPATH

	JAVA_ARGS="-Djetty.webroot=$WEB_ROOT"

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

	#EXEC_JAVA=$EXEC_JAVA"  -Dcore.zookeeper=t6:9331 -Dcore.step=$STEP -Dcore.version= -Dcore.tags=$TAGS -javaagent:/home/sankuai/jettyapps/jolokia-jvm-1.0.6-agent.jar=port=$JOLOKIA_PORT"
	#EXEC_JAVA=$EXEC_JAVA" -Djetty.home=$JETTY_HOME $JETTY_ARGS"
	EXEC_JAVA=$EXEC_JAVA" -Djetty.appkey=$MODULE -Djetty.context=$CONTEXT -Djetty.logs=$LOG_PATH"
	#-Djetty.port=$PORT"
	EXEC_JAVA=$EXEC_JAVA" $JAVA_ARGS"

	if [ "$UID" = "0" ]; then
		ulimit -n 1024000
		umask 000
	else
		echo $EXEC_JAVA
	fi

	${EXEC_JAVA} -jar msgp-errorlog.*.jar > /tmp/msgp-errorlog.start.log 2>&1
}


# ------------------------------------
# actually work
# ------------------------------------

init
run
bash