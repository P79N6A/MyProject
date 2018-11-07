#!/bin/sh

DIR=`pwd`
BIN=$DIR"/HlbManager"

for pid in $(ps -ef | grep $BIN | grep -v grep | awk '{print $2}')
do
    echo "kill process "$pid
    kill $pid
done

mkdir -p /service/HlbManager

cp -fv /opt/meituan/apps/HlbManager/run /service/HlbManager/run
chmod +x /service/HlbManager/run
