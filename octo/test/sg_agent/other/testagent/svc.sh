#!/bin/sh
BIN=sg_agent
for pid in $(ps -ef | grep $BIN | grep -v grep | awk '{print $2}')
do
    echo "kill process "$pid
    kill -9 $pid
done

touch /data/log/no_log



