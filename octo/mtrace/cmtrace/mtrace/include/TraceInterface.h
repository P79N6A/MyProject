#ifndef __TRACEINTERFACE_H__
#define __TRACEINTERFACE_H__

#include "Sample.h"
#include "MTrace.h"
using TraceNameSpace::CONFIG;

#define CMTraceVersion 0.3.0

/**
* @Brief 客户端初始化接口
* @param localAppKey 可以传递char *, 但是调用者需要控制非NULL
* @param remoteAppKey 可以传递char *, 但是调用着需要控制非NULL 
* @return 
*/
#define CLIENT_INIT(localAppKey, remoteAppKey) {TraceNameSpace::clientInit(localAppKey, remoteAppKey);}

/**
* @Brief 服务端初始化接口
* @param localAppKey 可以传递char *, 但是调用者需要控制非NULL
* @param remoteAppKey 可以传递char *, 但是调用着需要控制非NULL 
* @return 
*/
#define SERVER_INIT(localAppKey, remoteAppKey) {TraceNameSpace::serverInit(localAppKey, remoteAppKey);}

/**
* @Brief 客户端发送接口
* @param ipAndPort ip和端口，格式为ip:port
* @param rpcName 调用方法名
* @return 
*/
#define CLIENT_SEND(ipAndPort, rpcName) {Trace::getInstance()->sendRpc(CLIENT, ipAndPort, rpcName);}
#define CLIENT_SEND_V2(ipAndPort, rpcName) {Trace::getInstance()->sendRpc(MTRACE_CLIENT, ipAndPort, rpcName);}
#define CLIENT_SEND_WITH_APPKEY_V2(appkey, ip, Port, rpcName) {Trace::getInstance()->sendRpcWithAppkey(MTRACE_CLIENT, appkey, ip, Port, rpcName);}


/**
* @Brief 客户端接收端口
* @param status 状态码
* @return 
*/
#define CLIENT_RECV(status) {Trace::getInstance()->recvRpc(CLIENT, status, NULL);}
#define CLIENT_RECV_V2(status) {Trace::getInstance()->recvRpc(MTRACE_CLIENT, status, NULL);}


/**
* @Brief 服务端发送接口(无调用链信息)
* @param ipAndPort ip和端口，格式为ip:port
* @param rpcName 调用方法名
* @return 
*/
#define SERVER_SEND(ipAndPort, rpcName) {Trace::getInstance()->sendRpc(SERVER, ipAndPort, rpcName);}
#define SERVER_SEND_V2(ipAndPort, rpcName) {Trace::getInstance()->sendRpc(MTRACE_SERVER, ipAndPort, rpcName);}
#define SERVER_SEND_WITH_APPKEY_V2(appkey, ip, Port, rpcName) {Trace::getInstance()->sendRpcWithAppkey(MTRACE_SERVER, appkey, ip, Port, rpcName);}


/**
* @Brief 服务端发送接口(含有调用链信息) 
* @param ipAndPort ip和端口，格式为ip:port
* @param rpcName 调用方法名
* @param traceId 贯穿整个调用链中某次调用的一个标示id
* @param rpcId 贯穿整个调用的一个标示id
* @return 
*/
#define SERVER_SEND_TRACE(ipAndPort, rpcName, traceId, rpcId)\
 {Trace::getInstance()->sendRpc(SERVER, ipAndPort, rpcName, traceId, rpcId);}
#define SERVER_SEND_TRACE_V2(ipAndPort, rpcName, traceId, rpcId)\
{Trace::getInstance()->sendRpc(MTRACE_SERVER, ipAndPort, rpcName, traceId, rpcId);}
#define SERVER_SEND_TRACE_WITH_APPKEY_V2(appkey, ip, Port, rpcName, traceId, rpcId)\
{Trace::getInstance()->sendRpcWithAppkey(MTRACE_SERVER, appkey, ip, Port, rpcName, traceId, rpcId);}

/**
* @Brief 服务端接收窗口 
* @param status 状态码 
* @return 
*/
#define SERVER_RECV(status) {Trace::getInstance()->recvRpc(SERVER, status, NULL);}
#define SERVER_RECV_V2(status) {Trace::getInstance()->recvRpc(MTRACE_SERVER, status, NULL);}


/**
* @Brief 设置是否采样
* @param flag flag非0，则表示全部发送，不进行任何采样
* @return 
*/
#define SET_DEBUG(flag) {TraceNameSpace::setDebug(flag);}

#endif
