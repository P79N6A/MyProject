#! /bin/bash
set -e
# 保证cwd永远在当前文件所在目录
pushd ${0%/*}

if [ -f $HOME/.bashrc ]; then
    source "$HOME/.bashrc" || true
fi
if ! [ -d "$HOME/.nvm" ]; then
    echo "downloading nvm";
    curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.29.0/install.sh | bash || true
    . "$HOME/.bashrc"
else
    echo "nvm exists";
    echo `nvm ls`;
fi

if nvm ls 4.0.0; then
    nvm use 4.0.0
else
    nvm install 4.0.0
    nvm use 4.0.0
fi


npm --registry=http://r.npm.sankuai.com install --production

if [ x"$1" = x ]; then
    echo "参数parm不存在或者为空值"
    exit 2
else
    echo "{env:'$1', builddate: '$(date +%s)'};" > ./WEB-INF/decorators/env.inc
    echo "save env.inc with content $1"
fi
NODE_ENV=$1 ./node_modules/.bin/grunt
popd
