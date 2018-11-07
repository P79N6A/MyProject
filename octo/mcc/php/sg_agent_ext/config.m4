dnl $Id$
dnl config.m4 for extension sg_agent_sdk

dnl Comments in this file start with the string 'dnl'.
dnl Remove where necessary. This file will not work
dnl without editing.

dnl If your extension references something external, use with:

dnl PHP_ARG_WITH(sg_agent_sdk, for sg_agent_sdk support,
dnl Make sure that the comment is aligned:
dnl [  --with-sg_agent_sdk             Include sg_agent_sdk support])

dnl Otherwise use enable:

PHP_ARG_ENABLE(sg_agent_sdk, whether to enable sg_agent_sdk support,
dnl Make sure that the comment is aligned:
[  --enable-sg_agent_sdk           Enable sg_agent_sdk support])

if test "$PHP_SG_AGENT_SDK" != "no"; then
  dnl Write more examples of tests here...

  dnl # --with-sg_agent_sdk -> check with-path
  dnl SEARCH_PATH="/usr/local /usr"     # you might want to change this
  dnl SEARCH_FOR="/include/sg_agent_sdk.h"  # you most likely want to change this
  dnl if test -r $PHP_SG_AGENT_SDK/$SEARCH_FOR; then # path given as parameter
  dnl   SG_AGENT_SDK_DIR=$PHP_SG_AGENT_SDK
  dnl else # search default path list
  dnl   AC_MSG_CHECKING([for sg_agent_sdk files in default path])
  dnl   for i in $SEARCH_PATH ; do
  dnl     if test -r $i/$SEARCH_FOR; then
  dnl       SG_AGENT_SDK_DIR=$i
  dnl       AC_MSG_RESULT(found in $i)
  dnl     fi
  dnl   done
  dnl fi
  dnl
  dnl if test -z "$SG_AGENT_SDK_DIR"; then
  dnl   AC_MSG_RESULT([not found])
  dnl   AC_MSG_ERROR([Please reinstall the sg_agent_sdk distribution])
  dnl fi

  dnl # --with-sg_agent_sdk -> add include path
  dnl PHP_ADD_INCLUDE($SG_AGENT_SDK_DIR/include)

  dnl # --with-sg_agent_sdk -> check for lib and symbol presence
  dnl LIBNAME=sg_agent_sdk # you may want to change this
  dnl LIBSYMBOL=sg_agent_sdk # you most likely want to change this 

  dnl PHP_CHECK_LIBRARY($LIBNAME,$LIBSYMBOL,
  dnl [
  dnl   PHP_ADD_LIBRARY_WITH_PATH($LIBNAME, $SG_AGENT_SDK_DIR/lib, SG_AGENT_SDK_SHARED_LIBADD)
  dnl   AC_DEFINE(HAVE_SG_AGENT_SDKLIB,1,[ ])
  dnl ],[
  dnl   AC_MSG_ERROR([wrong sg_agent_sdk lib version or lib not found])
  dnl ],[
  dnl   -L$SG_AGENT_SDK_DIR/lib -lm
  dnl ])
  dnl

  PHP_REQUIRE_CXX()    dnl 通知Make使用g++
  PHP_ADD_INCLUDE(../../../sg_agent)
  PHP_ADD_INCLUDE(../../../sg_agent/util)
  PHP_ADD_INCLUDE(../../../sg_agent/comm)
  PHP_ADD_INCLUDE(../../../common/gen_cpp)
  PHP_ADD_INCLUDE(../../config)
  PHP_ADD_INCLUDE(../../thrift)
  PHP_ADD_INCLUDE(../../comm)
  PHP_ADD_INCLUDE(../../clog)
  PHP_ADD_INCLUDE(../../util)
  PHP_ADD_INCLUDE(../../cplus)
  PHP_ADD_INCLUDE(../)
  PHP_SUBST(SG_AGENT_SDK_SHARED_LIBADD)
  PHP_ADD_LIBRARY(stdc++, 1, EXTRA_LDFLAGS)    dnl 加入C++标准库
  PHP_ADD_LIBRARY_WITH_PATH(PHPSG_Agent, ../, SG_AGENT_SDK_SHARED_LIBADD)    dnl 加入C++标准库
  PHP_ADD_LIBRARY_WITH_PATH(log4cplus, ../, SG_AGENT_SDK_SHARED_LIBADD)    dnl 加入C++标准库

  PHP_NEW_EXTENSION(sg_agent_sdk, sg_agent_sdk.cpp, $ext_shared)
fi
