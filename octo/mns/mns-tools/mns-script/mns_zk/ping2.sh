#!/bin/bash
PING=`ping -w 1 $1 | grep '0 received' | wc -l`
echo $PING
