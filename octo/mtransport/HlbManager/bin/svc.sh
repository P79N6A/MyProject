#!/bin/sh

DIR=`pwd`
BIN=$DIR"/HlbManager"

for pid in $(ps -ef | grep $BIN | grep -v grep | awk '{print $2}')
do
    echo "kill process "$pid
    kill $pid
done

#sudo chown sankuai:sankuai /etc/nginx
#ll /etc/nginx/ -h |awk '{print $9}' |xargs sudo chown sankuai:sankuai

mkdir -p /var/sankuai/logs/HlbManager
mkdir -p /service/HlbManager

cp -fv /opt/meituan/apps/HlbManager/run /service/HlbManager/run
chmod +x /service/HlbManager/run
