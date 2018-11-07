#!/bin/bash

set j=0
for((j=0;j<100;))

do                                                                                                                                                                                                   
sed -i 's;<IP>192.*<\/IP>;<IP>123456-123456<\/IP>;g' /opt/meituan/apps/sg_agent/idc.xml
sleep 1
echo "begin to recovery"
sed -i 's;<IP>123456-123456<\/IP>;<IP>192.168.0.0<\/IP>;g' /opt/meituan/apps/sg_agent/idc.xml

let "j=j+1"
sleep 1

done
echo "finish the modified the idc xml files"



