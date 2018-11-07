#!/usr/bin/env bash
WAIT_SECONDS=30
	k=1
	echo "check service......"
	if [ -z $TEST_URL ]; then
	    echo "Warning:Need a test_url!"
	    exit 0
	fi

	for k in $(seq 1 $WAIT_SECONDS)
	do
	    sleep 1
	    STATUS_CODE=`curl -o /dev/null -s -w %{http_code} $TEST_URL`
	    if [ "$STATUS_CODE" = "200" ];then
	        echo "request test_url:$TEST_URL succeeded!"
	        echo "response code:$STATUS_CODE"
	        exit 0
	    else
	        echo "request test_url:$TEST_URL failed!"
	        echo "response code: $STATUS_CODE"
	        echo "try one more time:the $k time....."
	    fi
	    if [ $k -eq $WAIT_SECONDS ];then
	        echo "have tried 30 times, no more try"
	        echo "failed"
	        exit -1
	    fi
	done