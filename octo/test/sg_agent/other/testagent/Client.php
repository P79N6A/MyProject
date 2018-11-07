<?php

$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';

include dirname(__FILE__).'/sgagent/SGAgent.php';
#include dirname(__FILE__).'/sgagent/sgagent_types.php';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

$server = array('local' => "com.sankuai.octo.testSgagentClientPhp", "remote" => "com.sankuai.inf.sg_agent");
$options = null;
$local = "abc";
#$remote = "com.sankuai.inf.sg_agent";
$remote = "com.sankuai.inf.chenxin";
//$Param = json_encode($mccParam);
$thrift = new ThriftPoolN($server, $options, true, false, true);

//测试获取服务列表接口
$resp = $thrift->getResponse(array($local, $remote), 'SGAgentClient', 'getServiceList'); 
#print_r($resp);

/*
//测试服务注册
$oservice = new SGService;
$oservice->version = "v0.4";
$oservice->port = 52661;
$oservice->weight = 2196;
$oservice->status = 0;
$oservice->role = 0;
$oservice->env = 3;
$oservice->lastUpdateTime = 1425283508;
$oservice->extend = "";

$oservice->appkey = "com.sankuai.xzj.test_agent";
$oservice->ip = "192.168.22.93";
#$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
#print_r($resp);

#sleep 10毫秒
usleep(10000);
$oservice->appkey = "com.sankuai.xzj.test_agent";
$oservice->ip = "192.168.22.95";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
print_r($resp);

#sleep 10毫秒
usleep(10000);

$oservice->appkey = "com.sankuai.xzj.test_agent";
$oservice->ip = "192.168.22.95";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 

#sleep 10毫秒
usleep(10000);

$oservice->appkey = "com.sankuai.xzj.test_agent.fail";
$oservice->ip = "192.168.22.90";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
print_r($resp);

#sleep 10毫秒
usleep(10000);

$oservice->appkey = "com.sankuai.xzj.test_agent.fail";
$oservice->ip = "192.168.22.91";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 

#sleep 10毫秒
usleep(10000);

$oservice->appkey = "com.sankuai.xzj.test_agent.fail";
$oservice->ip = "192.168.22.92";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 

#sleep 10毫秒
usleep(10000);

$appkey = "com.sankuai.inf.sg_agent";
$appkey = "com.sankuai.octo.testMTthrift";
$ip = "192.168.166.12";
#$ip = "192.168.22.196";
#$resp = $thrift->getResponse(array($appkey, $ip), 'SGAgentClient', 'getLocalConfig'); 
#print_r($resp);
*/

?>
