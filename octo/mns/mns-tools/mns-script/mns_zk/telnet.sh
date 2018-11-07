#!/bin/bash
#TELNET=`telnet $1 $2 | grep Connected | wc -l`
TELNET=`telnet $1 $2`
echo $TELNET
