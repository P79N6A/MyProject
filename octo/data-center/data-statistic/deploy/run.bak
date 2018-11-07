#!/bin/bash

SERVICENAME=data-statistic
WORK_PATH=/opt/meituan/mobile/$SERVICENAME/deploy

cd /service/ || exit 1
if [ ! -e $SERVICENAME ]; then
    chmod 755 $WORK_PATH/run
    ln -s $WORK_PATH $SERVICENAME
    sleep 3
    sudo svc -du /service/$SERVICENAME
else
    sudo svc -du /service/$SERVICENAME
fi
