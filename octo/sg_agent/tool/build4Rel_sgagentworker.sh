#!/bin/sh

cmake sg_agent/CMakeLists.txt
cd sg_agent
make -j8
make install

agent_bin_dir="agent_worker_bin"
if [ ! -x "$agent_bin_dir" ]; then
    mkdir "$agent_bin_dir"
fi


cp -f build/bin/svc.sh $agent_bin_dir
cp -f build/bin/run_sgagentworker $agent_bin_dir/run
cp -f build/bin/log4cplus.conf $agent_bin_dir
cp -f build/bin/sg_agent_worker $agent_bin_dir

env
#根据自定义环境变量拷贝相应的配置文件
cp -f build/bin/$agent_env $agent_bin_dir/sg_agent_mutable.xml
cp -f build/bin/$config_env $agent_bin_dir/sg_agent_servicelist.xml
cp -f build/bin/idc.xml $agent_bin_dir/idc.xml
