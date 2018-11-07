#!/bin/bash

#第一个参数代表循环的次数

#usage:sh stress_test.sh 10
function getTime(){

echo `$date | awk  '{print $4}' `
}

if [ $# == 1 ]
then
    num=$1
else
    num=1
fi
for((i=1; i<=$num; i++))
do
    python client.py
done
