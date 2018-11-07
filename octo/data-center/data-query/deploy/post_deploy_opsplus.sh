#! /bin/bash
# ------------------------------------------
# Config your own app LOG_ROOT
# ------------------------------------------
LOG_ROOT=/opt/logs/mobile/data-query
SRV_ROOT=/service/data-query
RUN_FILE=/opt/meituan/apps/data-query/deploy/run.sh
CLEANLOG_DATE="3"
APPKEY="data-query"

# ------------------------------------------
# Init, check directory and file
# Suggest not Modify
# ------------------------------------------
function __init(){
    if [ ! -d "$LOG_ROOT" ];then
        mkdir -p $LOG_ROOT
    fi

    if [ ! -f "$SRV_ROOT/run" ];then
        mkdir $SRV_ROOT
        echo "#!/bin/bash" > $SRV_ROOT/run
        echo "exec $RUN_FILE" >> $SRV_ROOT/run
        chmod +x $SRV_ROOT/run
    fi

    rm $SRV_ROOT/clean.sh
    echo "#!/bin/bash" > $SRV_ROOT/clean.sh
    echo "rootpath='$LOG_ROOT';" >> $SRV_ROOT/clean.sh
    echo "find \$rootpath -mtime +2 -exec /bin/gzip {} \;" >> $SRV_ROOT/clean.sh
    echo "find \$rootpath -mtime +$CLEANLOG_DATE -exec rm -fr {} \;" >> $SRV_ROOT/clean.sh
    chmod +x $SRV_ROOT/clean.sh
}

# ------------------------------------------
# Add +x mod for RUN_FILE, service restart
# Suggest not Modify
# ------------------------------------------
function __restart(){
    chmod +x $RUN_FILE
    sudo svc -d $SRV_ROOT
    sudo svc -u $SRV_ROOT
}

# ------------------------------------------
# Check app work
# Suggest not Modify
# ------------------------------------------
function __check(){
    echo 'use daemontool monitor service'
    sudo svstat $SRV_ROOT
    cleanpath="$SRV_ROOT/clean.sh"
    (crontab -l|grep -v $cleanpath ; echo "58 05 * * * /bin/bash $SRV_ROOT/clean.sh > /dev/null 2>&1" ) | crontab
    k=1
    for k in $(seq 1 20)
    do
    	sleep 1
    	PID=`ps -ef | grep " -Djetty.appkey=$APPKEY " | grep -v grep | awk '{print $2}'`
	if [ "$PID" != "" ]; then
		break
	fi
    	echo $k
	if [ $k -eq 20 ]
	then
		echo 'process start time more than 20s, so abort'
		exit -1
	fi
    done
    sleep 15
    PID=`ps -ef | grep " -Djetty.appkey=$APPKEY " | grep -v grep | awk '{print $2}'`
    if [ "$PID" == "" ]; then
    	echo 'cannot found process '
    	exit -1
    fi
    echo 'start successfully !!!'
}

# ------------------------------------------
# Suggest not Modify
# ------------------------------------------
__init
__restart
__check