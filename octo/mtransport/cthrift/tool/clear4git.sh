#!/usr/bin/env bash

rm -rf ../cmake_install.cmake ../CMakeCache.txt ../CMakeFiles ../Makefile ../build/ ../install_manifest.txt
rm -rf ../src/cmake_install.cmake ../src/CMakeCache.txt ../src/CMakeFiles ../src/Makefile ../src/gen-cpp
rm -rf ../src/cthrift/cmake_install.cmake ../src/cthrift/CMakeCache.txt ../src/cthrift/CMakeFiles ../src/cthrift/Makefile
rm -rf ../src/cthrift/tests/cmake_install.cmake ../src/cthrift/tests/CMakeCache.txt ../src/cthrift/tests/CMakeFiles ../src/cthrift/tests/Makefile
rm -rf ../src/example/cmake_install.cmake ../src/example/CMakeCache.txt ../src/example/CMakeFiles ../src/example/Makefile

find ../ -name .*.sw* -exec rm -rf {} \;
