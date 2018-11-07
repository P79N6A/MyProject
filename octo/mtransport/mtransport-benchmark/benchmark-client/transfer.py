#!/usr/bin/env python
# -*- coding:utf-8 -*-
import os
import commands

def make_manifest(appname,projectPath):
    lines = []

    level = 0
    space = '  '
    lines.append(space*level+"version: v1"+'\n')

    lines.append(space*level+'common:'+'\n')
    level = 1
    lines.append(space*level+'os: centos7'+'\n')
    lines.append(space*level+'tools:'+'\n')
    level =2
    javaStr = raw_input("配置项目依赖Java的环境, 输入jdk版本 [7/8], 直接Enter默认为7: \n")
    if(len(javaStr)==0):
        javaStr = "oracle-jdk: 7"
    else :
        javaStr = "oracle-jdk: " + javaStr
    lines.append(space*level+javaStr+'\n')
    level = 1
    lines.append(space*level+'env:'+'\n')
    level = 2
    lines.append(space*level+'CURRENT_ENV: offline'+'\n')
    envIndex = len(lines) - 1
    envOnline = space*level+'CURRENT_ENV: online'+'\n'

    level = 0
    lines.append(space*level+"build: "+'\n')
    level = 1
    lines.append(space*level+'tools:'+'\n')
    level =2

    # mvnVersion = raw_input("指定项目编译时maven版本，目前支持版本号有3.3.3, 3.2.1 ,直接Enter默认为3.3.3,请输入: \n")
    # if(len(mvnVersion)==0):
    mvnVersion = "3.3.3"
    lines.append(space*level+'maven: '+mvnVersion+'\n')
    level = 1
    lines.append(space*level+'run:'+'\n')
    level = 2
    # projectPath = ""
    # while(len(projectPath)==0):
    #     projectPath = raw_input("请输入要发布功能模块相对于git仓库根目录的路径,例如 mtrace-web/ :\n")

    lines.append(space*level+'workDir: '+projectPath+'\n')
    lines.append(space*level+'cmd: '+'\n')
    level = 3
    lines.append(space*level+'- sh deploy/compile.sh'+'\n')

    level = 1
    lines.append(space*level+'target: '+'\n')
    level = 2
    lines.append(space*level+'distDir: '+projectPath+'target/'+'\n')
    lines.append(space*level+'files: '+'\n')
    level =3
    lines.append(space*level+'- ./*.war'+'\n')
    lines.append(space*level+'- ../deploy'+'\n')

    level = 0
    lines.append(space*level+'deploy: '+'\n')
    level = 1
    lines.append(space*level+'targetDir: /opt/meituan/apps/'+appname+'/'+'\n')
    lines.append(space*level+'tools:'+'\n')
    level = 1
    lines.append(space*level+'run:'+'\n')
    level = 2
    lines.append(space*level+'workDir: /opt/meituan/apps/'+appname+'/'+'\n')
    lines.append(space*level+'cmd: '+'\n')
    level = 3
    lines.append(space*level+'- sh deploy/run.sh'+'\n')
    print("生成的manifest.yml文件只是基本配置，可能需要RD做部分修改。具体可对照manifest_demo.yml,资料可参考wiki:http://wiki.sankuai.com/pages/viewpage.action?pageId=407025760  ")

    with open("manifest_offline.yml",'w') as fl:
        fl.writelines(lines)

    with open("manifest_online.yml",'w') as fl:
        lines[envIndex] = envOnline
        fl.writelines(lines)

def make_compileSh():
    lines = []
    lines.append('#!/bin/sh'+'\n')
    lines.append('\n')
    lines.append('# base command , you can add arguments for different environment, such as "-P offline"\n\n')
    lines.append('if [[ $CURRENT_ENV == "offline" ]]; then\n')
    lines.append('\tmvn clean -U package'+'\n')
    lines.append('else\n')
    lines.append('\tmvn clean -U package'+'\n')
    lines.append('fi\n')
    with open("compile.sh",'w') as fl:
        fl.writelines(lines)

def make_runSh(appname):
    lines = []
    lines.append('# ------------------------------------'+'\n')
    lines.append('# default jvm args if you do not config in /jetty/boot.ini '+'\n')
    lines.append('# ------------------------------------'+'\n')
    lines.append('JVM_ARGS="-server -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Djava.io.tmpdir=/tmp -Djava.net.preferIPv6Addresses=false"'+'\n')
    lines.append('JVM_GC="-XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"'+ '\n')
    lines.append('JVM_GC=$JVM_GC" -XX:CMSFullGCsBeforeCompaction=0 -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=80"'+'\n')
    lines.append('JVM_HEAP="-XX:SurvivorRatio=8 -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:ReservedCodeCacheSize=128m -XX:InitialCodeCacheSize=128m"'+'\n')
    lines.append('JVM_SIZE="-Xmx4g -Xms4g"'+'\n')
    lines.append('\n\n')

    lines.append('# ------------------------------------'+'\n')
    lines.append('# insert you init shell code here in preRun() function'+'\n')
    lines.append('# ------------------------------------'+'\n')
    lines.append('function preRun() {'+'\n')
    lines.append('\techo "preRun..."\n')
    lines.append('\t# if [[ $CURRENT_ENV == "offline" ]]; then\n')
    lines.append('\t# \techo "preRun offline..."'+'\n')
    lines.append('\t# else\n')
    lines.append('\t# \techo "preRun online..."'+'\n')
    lines.append('\t# fi\n')
    lines.append('}'+'\n')
    lines.append('\n\n')

    lines.append('# ------------------------------------'+'\n')
    lines.append('# do not edit'+'\n')
    lines.append('# ------------------------------------'+'\n')
    lines.append('MODULE='+appname+'\n')
    lines.append('WORK_PATH=/opt/meituan/apps/'+appname+'\n')
    lines.append('LOG_PATH=/opt/logs/apps/'+appname+'\n')
    lines.append('WEB_ROOT=$WORK_PATH/webroot'+'\n')
    lines.append('\n\n')

    lines.append('function init() {'+'\n')
    lines.append('\tpreRun'+'\n')
    lines.append('\tunzip -o *.war -d webroot'+'\n')
    lines.append('\tmkdir -p $LOG_PATH'+'\n')
    lines.append('}'+'\n')

    lines.append('\n\n')

    lines.append('function run() {'+'\n')
    lines.append('\tEXEC="exec"'+'\n')
    lines.append('\tCONTEXT=/'+'\n\n')

    lines.append('\tcd $WEB_ROOT'+'\n\n')
    lines.append('\tif [ -e "WEB-INF/classes/release" ]; then'+'\n')
    lines.append('\t\tcp -rf WEB-INF/classes/release/* WEB-INF/classes'+'\n')
    lines.append('\tfi'+'\n\n')

    lines.append('\tif [ -e "WEB-INF/classes/jetty/boot.ini" ]; then'+'\n')
    lines.append('\t\tsource WEB-INF/classes/jetty/boot.ini'+'\n')
    lines.append('\tfi'+'\n\n')

    lines.append('\tCLASSPATH=WEB-INF/classes'+'\n')
    lines.append('\tfor i in WEB-INF/lib/*'+'\n')
    lines.append('\tdo'+'\n')
    lines.append('\t\tCLASSPATH=$CLASSPATH:$i'+'\n')
    lines.append('\tdone'+'\n')
    lines.append('\n')

    lines.append('\texport CLASSPATH'+'\n')
    lines.append('\n')

    lines.append('\tJAVA_ARGS="-Djetty.webroot=$WEB_ROOT"'+'\n')
    lines.append('\n')
    lines.append('\tEXEC_JAVA="$EXEC java $JVM_ARGS $JVM_SIZE $JVM_HEAP $JVM_JIT $JVM_GC"'+'\n')
    lines.append('\tEXEC_JAVA=$EXEC_JAVA" -Xloggc:$LOG_PATH/$MODULE.gc.log -XX:ErrorFile=$LOG_PATH/$MODULE.vmerr.log -XX:HeapDumpPath=$LOG_PATH/$MODULE.heaperr.log"'+'\n')
    lines.append('\tif [ "$JVM_JMX" != "" ]; then'+'\n')
    lines.append("\t\tJVM_JMX_PORT=`expr $PORT '+' 10000`"+'\n')
    lines.append('\t\tEXEC_JAVA=$EXEC_JAVA" -Dcom.sun.management.jmxremote.port=$JVM_JMX_PORT $JVM_JMX"'+'\n')
    lines.append('\tfi'+'\n')
    lines.append('\n')

    lines.append('\tif [ "$JVM_DEBUG" != "" ]; then'+'\n')
    lines.append("\t\tJVM_DEBUG_PORT=`expr $PORT '+' 20000`"+'\n')
    lines.append('\t\tEXEC_JAVA=$EXEC_JAVA" $JVM_DEBUG,address=$JVM_DEBUG_PORT"'+'\n')
    lines.append('\tfi'+'\n')
    lines.append('\n')

    lines.append('\t#EXEC_JAVA=$EXEC_JAVA"  -Dcore.zookeeper=t6:9331 -Dcore.step=$STEP -Dcore.version= -Dcore.tags=$TAGS -javaagent:/home/sankuai/jettyapps/jolokia-jvm-1.0.6-agent.jar=port=$JOLOKIA_PORT"'+'\n')
    lines.append('\t#EXEC_JAVA=$EXEC_JAVA" -Djetty.home=$JETTY_HOME $JETTY_ARGS"'+'\n')
    lines.append('\tEXEC_JAVA=$EXEC_JAVA" -Djetty.appkey=$MODULE -Djetty.context=$CONTEXT -Djetty.logs=$LOG_PATH"'+'\n')
    lines.append('\t#-Djetty.port=$PORT"'+'\n')
    lines.append('\tEXEC_JAVA=$EXEC_JAVA" $JAVA_ARGS"'+'\n')
    lines.append('\n')

    lines.append('\tif [ "$UID" = "0" ]; then'+'\n')
    lines.append('\t\tulimit -n 1024000'+'\n')
    lines.append('\t\tumask 000'+'\n')
    lines.append('\telse'+'\n')
    lines.append('\t\techo $EXEC_JAVA'+'\n')
    lines.append('\tfi'+'\n')
    lines.append('\n')

    lines.append('\t$EXEC_JAVA com.sankuai.mms.boot.Bootstrap 2>&1 &'+'\n')
    lines.append('}'+'\n')
    lines.append('\n')
    lines.append('\n')

    lines.append('# ------------------------------------'+'\n')
    lines.append('# actually work'+'\n')
    lines.append('# ------------------------------------'+'\n')
    lines.append('\n')
    lines.append('init'+'\n')
    lines.append('run'+'\n')
    lines.append('bash'+'\n')

    with open("run.sh",'w') as fl:
        fl.writelines(lines)


def main():
    # pwd = os.system("pwd")
    projectPath = ""
    while(len(projectPath)==0):
        print("请输入要发布功能模块相对于git仓库根目录的路径")
        projectPath = raw_input("对于单模块应用，直接Enter即可。\n 对于多模块应用,例如要发布git仓库根目录下mtrace-web子模块，则输入mtrace-web/:\n ")
        projectPath = projectPath.strip()
        if(len(projectPath)==0):
            projectPath = "./"
        if not projectPath.endswith('/'):
            projectPath += "/"
        if projectPath.startswith('/'):
            projectPath = projectPath[1:]
            print(projectPath)
    if projectPath == "./":
        pathArr = filter(lambda x:len(x)>0 , commands.getoutput('pwd').split('/'))#raw_input("请输入要发布的项目的项目名:\n")
    else:
        pathArr = filter(lambda x:len(x)>0 , projectPath.split('/'))
        # print(pathArr)
    projectName = pathArr.pop()
    print("step 1: create deploy directory")
    if os.path.exists("./deploy"):
        answer = raw_input("deploy directory already exists in your project, manifest.yml/compile.sh/run.sh will be overwrite.Are you sure[y/n]?")
        if(answer == 'y' or answer == 'Y'):
            os.chdir("./deploy")
        else:
            print("transfer process quit!")
            return
    else :
        os.mkdir("./deploy")
        os.chdir("./deploy")

    print("step 2: create manifest.yml file")
    make_manifest(projectName, projectPath)
    print("create manifest.yml  end!\n")
    print("step 3: create compile shell  file")
    make_compileSh()
    print("create compile shell  file  end!\n")
    print("step 4: create deploy shell  file")
    make_runSh(projectName)
    print("create deploy shell  file  end!\n")

if __name__ == '__main__':
    main()