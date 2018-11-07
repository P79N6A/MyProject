#!/bin/
set -e

# 前端验证,如果业务上不需要前端验证,不传入NODE_ENV参数即可
# $PACKAGE_PATH为包名, $NODE_ENV线下development,线上production
if [ -n "$NODE_ENV" ]; then
    echo "NODE_ENV:"$NODE_ENV
    bash src/main/webapp/deployfe.sh $NODE_ENV
fi

if [ "$SKIP_TEST" = "false" ]; then
    SKIP_TEST=false
else
    SKIP_TEST=true
fi

mvn clean -U package -am -P $PROFILE -Dmaven.test.skip=$SKIP_TEST