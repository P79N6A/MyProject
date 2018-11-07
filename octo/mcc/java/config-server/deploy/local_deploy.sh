#!/bin/bash

set -e

export DIST_DIR="target"
export PRE_DEPLOY_SCRIPT="config-server/deploy/pre_deploy.sh"
export PROD_DIR="/Users/Jason/work/mtconfig/target"
export MODULE="config-server"
export APP_NAME="mtconfig"
export APP_KEY="mtconfig"
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_65.jdk/Contents/Home"
export LOG_BASE_DIR="/Users/Jason/Downloads"
export PROFILE="local"
export JAVA_VERSION="7"
export POST_DEPLOY_SCRIPT="target/post_deploy.sh"
export PRE_DEPLOY_ENV_CHECK_LATEST_BRANCH="False"
export PRE_DEPLOY_ENV_IS_DELETE_FILE="True"
export COMPILE_JAVA_HOME=${JAVA_HOME}
export LOG__LEVEL="debug"

rm -rf "${DIST_DIR}"
python /Users/Jason/work/mtdeploy2/pre_deploy.py
python ${PROD_DIR}/deploy/start_app.py