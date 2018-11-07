#!/bin/sh
ulimit -n 1000000
cd /opt/meituan/apps/cplugin_server
NUM=`ps -ef |grep -v grep | grep -c /opt/meituan/apps/cplugin/cplugin_server`
if [ $NUM -lt 1 ]
then
    exec setuidgid sankuai /opt/meituan/apps/cplugin/cplugin_server > /dev/null 2>&1
fi
