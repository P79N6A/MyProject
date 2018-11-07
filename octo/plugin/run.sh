#!/bin/sh
cd ../common/cpp/lib/curl-7_42_0/
sh buildconf
./configure --without-libssh2
make