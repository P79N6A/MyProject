#!/bin/sh

path="../gen_cpp"

if [ ! -x "$path" ]; then
    mkdir "$path"
fi

rm -rf ../gen_cpp/*

thrift -r -out ../gen_cpp --gen cpp ../../common/idl/idl-sgagent/src/main/thrift/sgagent_service.thrift
thrift -r -out ../gen_cpp --gen cpp ../../common/idl/idl-sgagent/src/main/thrift/sgagent_worker_service.thrift

make clean; make -j 10;
make install
