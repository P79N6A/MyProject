#!/usr/bin/env bash
# ------------------------------------
# default jvm args if you do not config in /jetty/boot.ini
# -----------------------------------

DEFAULT_ARGS=" -server
               -Dfile.encoding=UTF-8
               -Dsun.jnu.encoding=UTF-8
               -Djava.net.preferIPv6Addresses=false
               -Djava.io.tmpdir=/tmp
               -Djetty.defaultsDescriptor=WEB-INF/web.xml
               -Duser.timezone=GMT+08"

CUSTOM_JVM_OFFLINE=" -Xmx6g
             -Xms6g
             -Xmn2g
             -XX:PermSize=128m
             -XX:MaxPermSize=256m
             -XX:+PrintCommandLineFlags
             -XX:+UseConcMarkSweepGC
             -XX:CMSFullGCsBeforeCompaction=0
             -XX:+UseCMSCompactAtFullCollection
             -XX:CMSInitiatingOccupancyFraction=80"

CUSTOM_JVM_ONLINE=" -Xmx6g
             -Xms6g
             -Xmn4g
             -XX:PermSize=128m
             -XX:MaxPermSize=256m
             -XX:+PrintCommandLineFlags
             -XX:+UseConcMarkSweepGC
             -XX:CMSFullGCsBeforeCompaction=0
             -XX:+UseCMSCompactAtFullCollection
             -XX:CMSInitiatingOccupancyFraction=80
             -XX:MaxTenuringThreshold=15"

# ------------------------------------
# do not edit
# ------------------------------------

function init() {
    echo "init start...."

    if [ -z "$LOG_PATH" ]; then
        LOG_PATH="/var/sankuai/logs"
    fi

    DEFAULT_JVM=" -Xloggc:$LOG_PATH/$APP_KEY.gc.log.`date +%Y%m%d%H%M`
              -XX:ErrorFile=$LOG_PATH/$APP_KEY.vmerr.log.`date +%Y%m%d%H%M`
              -XX:HeapDumpPath=$LOG_PATH/$APP_KEY.heaperr.log.`date +%Y%m%d%H%M`
              -XX:+HeapDumpOnOutOfMemoryError
              -XX:+DisableExplicitGC
              -XX:+PrintGCDetails
              -XX:+PrintGCDateStamps"

    if [ -z "$WORK_PATH" ]; then
        WORK_PATH="/opt/meituan/apps/$PACKAGE_PATH"
    fi

    WEB_ROOT=$WORK_PATH/work

    # 如果工作目录存在则删除目录
    if [ -d "$WEB_ROOT" ]; then
        echo "rm -rf"$WEB_ROOT
        rm -rf $WEB_ROOT
    fi

    # 新建工作目录
    echo "mkdir -p"$WEB_ROOT
    mkdir -p $WEB_ROOT

    echo "unzip to "$WEB_ROOT

    unzip -o *.war -d $WEB_ROOT

    echo "unzip end..."

    # 用于nginx健康检查, 有些业务的nginx健康检查的url是 /status, 故需要做touch操作
    touch $WEB_ROOT/status
    # 创建log对应的目录
    mkdir -p $LOG_PATH

    # 定时清理日志，如果业务上没有日志清理程序，可将CLEAN_LOG参数设置为true。
    cleanpath="$WORK_PATH/clean.sh"
    if [ "$CLEAN_LOG" = "true" ]; then
        echo "create clean.sh start..."

        echo "#!/bin/bash" > $cleanpath
        echo "find $LOG_PATH -mtime +7 -exec /bin/gzip {} \;" >> $cleanpath
        echo "find $LOG_PATH -mtime +30 -exec rm -fr {} \;" >> $cleanpath
        chmod +x $cleanpath
        (crontab -l|grep -v $cleanpath ; echo "58 05 * * * /bin/bash $cleanpath > /dev/null 2>&1" ) | crontab

        echo "create clean.sh end..."
    fi

    # 生成crossdomain.xml，如果本身业务不需要跨域，以下逻辑可以省略。
    crossdomain="$WEB_ROOT/crossdomain.xml"
    if [ ! -f "$crossdomain" ]; then
        echo "<cross-domain-policy>" > $crossdomain
        echo "    <allow-access-from domain="*.meituan.net"/>" >> $crossdomain
        echo "    <allow-access-from domain="*.meituan.com"/>" >> $crossdomain
        echo "    <allow-access-from domain="*.sankuai.com"/>" >> $crossdomain
        echo "</cross-domain-policy>" >> $crossdomain
    fi

    echo "init end...."
}

function run() {
    echo "run start...."

    #根据java版本,决定java命令的位置
    JAVA_CMD=$JAVA_VERSION
    if [ -z "$JAVA_VERSION" ]; then
        JAVA_CMD="java" #系统默认的java命令
    else
        JAVA_CMD="/usr/local/$JAVA_VERSION/bin/java"
    fi

    echo "JAVA_CMD:"${JAVA_CMD}

    EXEC="exec"
    CONTEXT=/

    # 根据主机名判断是否是线上环境
    IS_ONLINE=`is_online`
    echo "IS_ONLINE:"$IS_ONLINE

    # 线上环境
    if [ "$IS_ONLINE" = "true" ]; then
        ENV="online"
        JVM_ARGS=$CUSTOM_JVM_ONLINE
        MEDIS_ENV="online"
        ZK_ADDR="sgconfig-zk.vip.sankuai.com"
    else
        ENV="test"
        JVM_ARGS=$CUSTOM_JVM_OFFLINE
        MEDIS_ENV="test"
        ZK_ADDR="sgconfig-zk.sankuai.com:9331"
    fi

    cd $WEB_ROOT

    # 得到boot.ini文件配置的自定义参数
    CUSTOM_ARGS=""
    if [ -e "WEB-INF/classes/jetty/boot.ini" ]; then
        CUSTOM_JVM=`awk -F'CUSTOM_JVM' '{if(($2~/=/)&&($1!~/;/))print $2}' WEB-INF/classes/jetty/boot.ini`
        echo "WEB-INF/classes/jetty/boot.ini CUSTOM_JVM:"${CUSTOM_JVM}

        if [ -n "$CUSTOM_JVM" ]; then
            JVM_ARGS=${CUSTOM_JVM#*=}
        fi

        CUSTOM_ARGS=`awk -F'CUSTOM_ARGS' '{if(($2~/=/)&&($1!~/;/))print $2}' WEB-INF/classes/jetty/boot.ini`
        echo "WEB-INF/classes/jetty/boot.ini CUSTOM_ARGS:"${CUSTOM_ARGS}

        if [ -n "$CUSTOM_ARGS" ]; then
            CUSTOM_ARGS=${CUSTOM_ARGS#*=}
        fi
    fi
    echo "JVM_ARGS:"${JVM_ARGS}
    echo "CUSTOM_ARGS:"${CUSTOM_ARGS}

    # 得到boot.properties文件配置的端口号
    PORT=`awk -F'=' '{if($1~/jetty.port/) print $2}' WEB-INF/classes/jetty/boot.properties`
    echo "PORT:"${PORT}

    # 得到主机名
    HOST=`hostname |awk -F. '{print $1}'`
    echo "HOST:"${HOST}

    # 得到本机IP
    IP=`get_local_ip`
    echo "IP:"${IP}

    JAVA_ARGS="-Djetty.webroot=$WEB_ROOT"

    COMMON_ARGS=" -Denvironment=$ENV
              -Dmedis_environment=$ENV
              -Dcore.zookeeper=$ZK_ADDR
              -Dapp.key=$APP_KEY
              -Djetty.appkey=$APP_KEY
              -Dapp.host=$HOST
              -Djetty.host=$HOST
              -Dapp.ip=$IP
              -Dapp.port=$PORT
              -Djetty.port=$PORT
              -Dapp.context=$CONTEXT
              -Djetty.context=$CONTEXT
              -Dapp.workdir=$WEB_ROOT
              -Dapp.logdir=$LOG_PATH
              -Djetty.logs=$LOG_PATH"

    EXEC_JAVA="$EXEC $JAVA_CMD $DEFAULT_ARGS $DEFAULT_JVM $COMMON_ARGS $CUSTOM_ARGS $JVM_ARGS"
    EXEC_JAVA=$EXEC_JAVA" $JAVA_ARGS"

    # 解决云主机中文乱码问题,http://wiki.sankuai.com/pages/viewpage.action?pageId=368984944
    export LANG=en_US.UTF-8
    export LC_CTYPE="en_US.UTF-8"
    export LC_NUMERIC="en_US.UTF-8"
    export LC_TIME="en_US.UTF-8"
    export LC_COLLATE="en_US.UTF-8"
    export LC_MONETARY="en_US.UTF-8"
    export LC_MESSAGES="en_US.UTF-8"
    export LC_PAPER="en_US.UTF-8"
    export LC_NAME="en_US.UTF-8"
    export LC_ADDRESS="en_US.UTF-8"
    export LC_TELEPHONE="en_US.UTF-8"
    export LC_MEASUREMENT="en_US.UTF-8"
    export LC_IDENTIFICATION="en_US.UTF-8"
    export LC_ALL=

    if [ "$UID" = "0" ]; then
        ulimit -n 1024000
        umask 000
    else
        echo $EXEC_JAVA
    fi

    # 必须使用mms-boot 1.2.0及以上版本方式
    for i in WEB-INF/lib/*
    do
        if [[ $i == *$mms-boot-* ]]; then
             MMS_BOOT_JAR=$i
        fi
    done

    if [ -z "$MMS_BOOT_JAR" ]; then
        echo "找不到mms-boot的jar包"
    else
        echo "加载的mms-boot的jar包:"$WEB_ROOT/$MMS_BOOT_JAR
    fi

    # 通过mms启动,将启动日志重定向到start.log日志文件
    $EXEC_JAVA -cp $MMS_BOOT_JAR com.sankuai.mms.boot.Bootstrap >> $LOG_PATH/$APP_KEY.start.log.`date +%Y%m%d` 2>&1

    echo "run end..."
}

# 根据主机名区分线上线下环境
function is_online() {
    # 得到主机名
    HOST_NAME=`hostname`

    if [[ $HOST_NAME =~ ".office.mos" ]] || [[ $HOST_NAME =~ ".corp.sankuai.com" ]]; then
        echo false
    elif [[ $HOST_NAME =~ ".sankuai.com" ]]; then
        echo true
    else
        echo false
    fi
}

# 获取主机IP
function get_local_ip() {
    IPS=`/sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"`
    IP_ARR=(${IPS// / })
    length=${#IP_ARR[@]}
    if [[ $length -eq "1" ]]; then
        IP=$IPS
    else
        IP=${IP_ARR[0]}
    fi
    echo ${IP}
}

# 若之前的发布项与新的服务在/service下同名,则第一次启动服务时使用该方法杀掉一些已有的进程
# 需要在创建任务时添加环境变量参数:IS_CLEAN_SERVICE,值为true
function clean(){
    if [ "$IS_CLEAN_SERVICE" = "true" ]; then
        echo "clean start..."

        set +e
        ps -ef|grep "Bootstrap"|grep "app.key=$APP_KEY"|grep -v grep|awk '{print $2}'|xargs kill
        sleep 3
        set -e

        echo "clean end..."
    fi
}

# ------------------------------------
# actually work
# ------------------------------------
clean
init
run
