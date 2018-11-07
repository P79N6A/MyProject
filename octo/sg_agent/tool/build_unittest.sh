#!/bin/sh
sh sg_agent/tool/clear4git.sh

cmake -DCMAKE_BUILD_TYPE=debug sg_agent/CMakeLists.txt
cd sg_agent/src/test
make -j8
make install

mv -f ../../bin/unittest ./


