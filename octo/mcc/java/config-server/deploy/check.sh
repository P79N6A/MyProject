#!/bin/sh
echo "check service......"
if [ -z $TEST_URL ]; then
    TEST_URL="/api/monitor/alive"
fi

if [ -z "$WORK_PATH" ]; then
    WORK_PATH="/opt/meituan/apps/$PACKAGE_PATH"
fi
cd $WORK_PATH/work
echo $WORK_PATH/work

PORT=`awk -F'=' '{if($1~/jetty.port/) print $2}' WEB-INF/classes/jetty/boot.properties`
TEST_URL="http://localhost:"${PORT}${TEST_URL}

STATUS_CODE=`curl -o /dev/null -s -w %{http_code} $TEST_URL`

if [ "$STATUS_CODE" = "200" ];then
    echo "request test_url:$TEST_URL succeeded!"
    echo "response code:$STATUS_CODE"
    exit 0;
else
    echo "request test_url:$TEST_URL failed!"
    echo "response code: $STATUS_CODE"
    exit -1;
fi
