#!/usr/bin/env bash
for k in $( seq 1 1 )
do
    curl -V http://data.octo.vip.sankuai.com/api/query?provider=com.sankuai.inf.data.statistic&spanname=all&env=prod&start=1441507423&end=1441507424
#    /root/chaoshu/cthrift/bin/thrift_echo_cli 9091 10000
#    sleep 10 
done
