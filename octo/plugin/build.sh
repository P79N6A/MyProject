#!/bin/sh

yum install cmake
yum install log4cplus-devel

CPU_CORE_NUM=`cat /proc/cpuinfo | grep processor | wc -l`

cmake . && make -j"$CPU_CORE_NUM"