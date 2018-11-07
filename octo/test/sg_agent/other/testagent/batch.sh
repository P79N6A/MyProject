#!/bin/bash

day=`date '+%s'`
newday=`echo $1*3600 | bc`
lastday_temp=`echo $newday+$day | bc`
#取小数中的整数部分
lastday=`echo ${lastday_temp%.*}`

#主循环：控制测试跑的时间长度
while [ $day -lt $lastday ]
do
    day=`date '+%s'`
    php ./Client.php
done 

