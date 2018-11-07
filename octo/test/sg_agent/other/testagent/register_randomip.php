<?php
$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

$local = "com.sankuai.inf.test4";
$remote = "com.sankuai.inf.test4";
$agent = "com.sankuai.inf.sg_agent";
$options = null;
$server = array('local' => $local, 'remote' => $agent, );

$thrift = new ThriftPoolN($server, $options, true, false, true);
//测试服务注册
$oservice = new SGService;
$oservice->version = "v0.4";
$oservice->port = 7001;
$oservice->weight = 10;
$oservice->status = 2;
$oservice->role = 0;
$oservice->env = 3;
$oservice->lastUpdateTime = time();
$oservice->extend = "";
$oservice->appkey = $remote;

function randomIp() {
    $ip2id= round(rand(600000, 2550000) / 10000); 
    $ip3id= round(rand(600000, 2550000) / 10000); 
    $ip4id= round(rand(600000, 2550000) / 10000); 
    //在以下数据中随机抽取ip的第一个字段
    $arr_1 = array("218","218","66","66","218","218","60","60","202","204","66","66","66","59","61","60","222","221","66","59","60","60","66","218","218","62","63","64","66","66","122","211"); 
    $randarr= mt_rand(0,count($arr_1)-1); 
    $ip1id = $arr_1[$randarr]; 
    $rId = $ip1id.".".$ip2id.".".$ip3id.".".$ip4id; 
    return $rId;
}
echo randomIp();
$num = 100;
for ($i = 0; $i <= $num; $i++)
{

    $oservice->ip = randomIp();
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
}
?>
