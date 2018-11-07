#!/bin/sh

cmake CMakeLists.txt
make -j8
make install

all_env=("prod" "stage" "test");

for env in ${all_env[@]} 
do
agent_bin_dir=${env}"_agent_bin_offline"
if [ ! -x "$agent_bin_dir" ]; then
    mkdir "$agent_bin_dir"
fi

agent_bin_worker_dir=${env}"_worker_bin_offline"
if [ ! -x "$agent_bin_worker_dir" ]; then
    mkdir "$agent_bin_worker_dir"
fi

#cp 文件到agent_bin
cp -f build/bin/svc.sh $agent_bin_dir
cp -f build/bin/run_sgagent $agent_bin_dir/run
cp -f build/bin/log4cplus.conf $agent_bin_dir
cp -f build/bin/sg_agent $agent_bin_dir
cp -f build/bin/idc.xml $agent_bin_dir/idc.xml
cp -f build/bin/sg_agent_mutable_new_${env}.xml $agent_bin_dir/sg_agent_mutable.xml
cp -f build/bin/sg_agent_offline_servicelist.xml $agent_bin_dir/sg_agent_servicelist.xml

#cp 文件到agent_bin
cp -f build/bin/svc.sh $agent_bin_worker_dir
cp -f build/bin/run_sgagentworker $agent_bin_worker_dir/run
cp -f build/bin/log4cplus.conf $agent_bin_worker_dir
cp -f build/bin/sg_agent_worker $agent_bin_worker_dir
cp -f build/bin/idc.xml $agent_bin_worker_dir/idc.xml
cp -f build/bin/sg_agent_mutable_new_${env}.xml $agent_bin_worker_dir/sg_agent_mutable.xml
cp -f build/bin/sg_agent_offline_servicelist.xml $agent_bin_worker_dir/sg_agent_servicelist.xml
done

