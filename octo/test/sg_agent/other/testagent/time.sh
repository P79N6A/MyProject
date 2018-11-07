#!/bin/bash
time1=$(date +%s -d '1990-01-01 01:01:01')

#php Client.php

sleep 5;
time2=$(date +%s -d '1990-01-01 01:01:01')

time=$(( $time2 - $time1 ))
((i=$time2-$time1)) 

echo $i
