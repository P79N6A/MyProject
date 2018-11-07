#!/bin/sh
cmake CMakeLists.txt
make -j8

cplugin_bin_dir="cplugin_bin_dir"
if [ ! -x "$cplugin_bin_dir" ]; then
    mkdir "$cplugin_bin_dir"
fi

#cp 文件到cplugin
cp -f script/run $cplugin_bin_dir
cp -f conf/config.xml $cplugin_bin_dir
cp -f bin/cplugin $cplugin_bin_dir
cp -f bin/start $cplugin_bin_dir


