<?php
$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

$local = "com.sankuai.inf.chenxin";
$remote = "com.sankuai.inf.logCollector";
$agent = "com.sankuai.inf.sg_agent";
$options = null;
$server = array('local' => $local, 'remote' => $agent, );

$thrift = new ThriftPoolN($server, $options, true, false, true);
//测试服务注册
$oservice = new SGService;
$oservice->version = "v0.4";
$oservice->port = 8920;
$oservice->weight = 55;
$oservice->status = 2;
$oservice->role = 0;
$oservice->env = 3;
$oservice->lastUpdateTime = time();
$oservice->extend = "";

$oservice->appkey = $remote;
$oservice->ip = "192.168.3.163";
$resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
print_r($resp);
?>
