#!/usr/bin/env bash
for module_path in sg_agent sg_agent/src/sg_agent sg_agent/src/sg_agent_worker sg_agent/src/sgcommon sg_agent/src/mns sg_agent/src/test
    do
      for file_path in cmake_install.cmake CMakeCache.txt CMakeFiles Makefile install_manifest.txt
        do
           rm -rf ${module_path}/${file_path}
        done;
    done;

rm -rf sg_agent/build/ sg_agent/agent_bin/ sg_agent/lib/ sg_agent/bin/

find sg_agent/ -name .*.sw* -exec rm -rf {} \;
