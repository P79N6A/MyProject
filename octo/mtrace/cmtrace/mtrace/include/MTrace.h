/**
* @file mtrace.h
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/

#ifndef __MTRACE_H__
#define __MTRACE_H__

#include <stdint.h>
#include <pthread.h>
#include <iostream>
#include <string>
#include <map>
#include "RemoteProcessCall.h"
#include "ExceptionLog.h"
using namespace std;

enum CallType
{
	CLIENT,
	SERVER
};

//2017.04.23 conflict enum SERVER & CLIENT with cthrift
enum MTraceCallType {
    MTRACE_CLIENT,
    MTRACE_SERVER
};

namespace TraceNameSpace
{
	extern string m_localIp;
    extern bool m_init;
    extern pthread_once_t once_control;
	extern pthread_key_t m_client;
	extern pthread_key_t m_server;
	extern pthread_key_t m_debug; 

	extern pthread_key_t m_clientLocal;
	extern pthread_key_t m_clientRemoteAppKey;

	extern pthread_key_t m_serverLocal;
	extern pthread_key_t m_serverRemoteAppKey;

    /**
    * @Brief 
    * @param localAppKey
    * @param remoteAppKey
    */
	void clientInit(const string& localAppKey, const string& remoteAppKey);

    /**
    * @Brief 
    * @param localAppKey
    * @param remoteAppKey
    */
    void serverInit(const string& localAppKey, const string& remoteAppKey);
	
    /**
    * @Brief tls变量初始化 
    */
    void tlsInit();
    
	/**
	* @Brief 非0则为全采样, 0则表示不采样
	* @param debug
	*/
	void setDebug(int debug);
	int * getDebug();
	void clearDebug();

	/**
	* @Brief 使用m_server的三个过程，设置－》获取－》清除
	* @param p_rpc
	*/
	void setServerRpc(RemoteProcessCall* p_rpc);
	RemoteProcessCall * getServerRpc();
	void clearServerRpc();
	
	/**
	* @Brief 使用m_client的三个过程，设置－》获取－》清除
	* @param p_rpc
	*/
	void setClientRpc(RemoteProcessCall * p_rpc);
	RemoteProcessCall * getClientRpc();
	void clearClientRpc();

    /**
    * @Brief client Local 相关
    * @param pLocal
    */
    void setClientLocal(EndPoint * pLocal);
    EndPoint * getClientLocal();
    void clearClientLocal();

    /**
    * @Brief server local 相关
    * @param pLocal
    */
    void setServerLocal(EndPoint * pLocal);
    EndPoint * getServerLocal();
    void clearServerLocal();

    /**
    * @Brief client remote 相关 
    * @param pLocal
    */
    void setClientRemoteAppKey(string * pClientRemoteAppKey);
    string * getClientRemoteAppKey();
    void clearClientRemoteAppKey();

    /**
    * @Brief server remote 相关 
    * @param pLocal
    */
    void setServerRemoteAppKey(string * pServerRemoteAppKey);
    string * getServerRemoteAppKey();
    void clearServerRemoteAppKey();

    class Configure
    {
    public:
        Configure();
        void setThreshold(string appKey, string spanName, int threshold);
        void setThreshold(string appKey, int threshold);         
    
    public:
        pthread_rwlock_t rwlock;
        map<string, int> identify2Limit;
    };
    extern Configure CONFIG;
}

class Trace
{
public:
	static Trace * getInstance();

	/**
	* @Brief 发送埋点，通过type字段去区分是server还是client
	* @param type 0表示client，1表示server
	* @param local 本地appkey相关信息
	* @param remote 远程appkey相关信息
	* @param rpcName 业务给此次调用的命名
	* @param traceId 贯穿整个调用的一个标示id
	* @param rpcId 贯穿整个调用链中某次调用的一个标示id
	* @param sample 是否采样 1标示采样命中 0标示采样不命中（不具体区分是全采样还是普通采样）
	* @param debug 是否是debug模式
	* @param extend 保留字段
	* @return 
	*/
	int sendRpc(int type, const EndPoint& local, const EndPoint& remote, string rpcName = "", string traceId = "", string rpcId = "", bool sample = false, int debug = 0, string extend="");

	/**
	* @Brief 发送埋点，通过type字段去区分是server还是client
	* @param type 0表示client，1表示server
	* @param ipAndPort 远程ip及其端口信息，传输格式为ip:port
	* @param rpcName 给本次调用取的方法名
	* @param traceId 贯穿整个调用的一个标示
	* @param rpcId 贯穿整个调用链中某次调用的一个标示
	* @param sample 是否采样 1标示采样命中 0标示采样不命中（不具体区分是全采样还是普通采样）
	* @param debug 是否是debug模式
	* @param extend 保留字段
	* @return 
	*/
	int sendRpc(int type, const string& ipAndPort, const string& rpcName, string traceId = "", string rpcId = "", bool sample = false, int debug = 0, string extend = "");
    
    /**
     * @Brief 发送埋点，通过type字段去区分是server还是client
     * @param type 0表示client，1表示server
     * @param appkey 远程appkey
     * @param ip 远程ip
     * @param port 远程port
     * @param rpcName 给本次调用取的方法名
     * @param traceId 贯穿整个调用的一个标示
     * @param rpcId 贯穿整个调用链中某次调用的一个标示
     * @param sample 是否采样 1标示采样命中 0标示采样不命中（不具体区分是全采样还是普通采样）
     * @param debug 是否是debug模式
     * @param extend 保留字段
     * @return 
     */
    int sendRpcWithAppkey(int type, const string& appkey, const string& ip, int port, const string& rpcName, string traceId = "", string rpcId = "", bool sample = false, int debug = 0, string extend = "");	
	/**
	* @Brief 接收埋点 
	* @param type 接收埋点，通过type字段去区分是server还是client
	* @param status 上层应用设置的状态码
	* @param fileName 配置文件名 
	* @return 
	*/
	int recvRpc(int type, int status, const char * fileName = NULL); ///<设置end，status.

	/**
	* @Brief 发送异常日志，格式参照wiki
	* @param appKey 
	* @param content 
	* @param level 默认为1
	* @return 
	*/
	int sendLog(const string &appKey, const string& content, int32_t level = 1);
private:
	Trace(){};
	static Trace * m_instance; 
};

#endif
