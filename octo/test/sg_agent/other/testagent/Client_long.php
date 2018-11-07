<?php

$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';

include dirname(__FILE__).'/sgagent/SGAgent.php';
#include dirname(__FILE__).'/sgagent/sgagent_types.php';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

$server = array('local' => "com.sankuai.octo.testSgagentClientPhp", "remote" => "com.sankuai.inf.sg_agent");
$options = null;
$local = "abc";
$remote = "com.sankuai.inf.sg_agent";
#$remote = "com.sankuai.inf.logCollector";
//$Param = json_encode($mccParam);
$Param = '{
    "uuid": "1923912131123",    
    "time": 1415859016123,
    "appkey": "com.sankuai.inf.sg_agent",
    "nodes": [
        "192.168.166.182",
        "192.168.11.91"
    ],
    "details": {
        "id":21d046a3d7740267ba0b8f8c75392208@192.168.22.196", 
        "md5": "assdadwasxsdasawsdswsdaswdasdesde",
        "fileName": "test.txt",
        "path": "/tmp",
        "lastUpdateTime": 0,
        "createTime": 0,
        "needToLocal": 1,
        "privilege": 1,
        "fileType": "",
        "reserved": ""
    }
}';
$thrift = new ThriftPoolN($server, $options, true, false, true);

while(1) 
{

    $resp = $thrift->getResponse(array($local, $remote), 'SGAgentClient', 'getServiceList'); 
    print_r($resp);
    if (is_array($resp)) 
    {
        print_r($resp);
    } else {
        echo "getServicelist err!";
        break;
    }

    $Param2 = '{  "appkey":       "mobile-sinai", "version":      "mtthrift-v1.5.3",      "ip":   "10.64.45.220", "port": 10620,  "weight":       10,     "status":       0,      "role": 0,      "env":  3,      "lastUpdateTime":       1425283508,     "extend":       "mix|slowStartSeconds:180"}';

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
    $oservice->ip = "192.168.22.90";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }

    $oservice->appkey = "com.sankuai.xzj.test_agent";
    $oservice->ip = "192.168.22.91";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }

    $oservice->appkey = "com.sankuai.xzj.test_agent";
    $oservice->ip = "192.168.22.92";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }

    $oservice->appkey = "com.sankuai.xzj.test_agent.fail";
    $oservice->ip = "192.168.22.90";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    print_r($resp);
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }

    $oservice->appkey = "com.sankuai.xzj.test_agent.fail";
    $oservice->ip = "192.168.22.91";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }

    $oservice->appkey = "com.sankuai.xzj.test_agent.fail";
    $oservice->ip = "192.168.22.92";
    $resp = $thrift->getResponse(array($oservice), 'SGAgentClient', 'registService'); 
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "register  err!";
        break;
    }
    #$resp = $thrift->getResponse(array(21, $Param), 'SGAgentClient', 'callBackMCC'); 
    #print_r($resp);

    $appkey = "com.sankuai.inf.sg_agent";
    $appkey = "com.sankuai.octo.testMTthrift";
    $ip = "192.168.166.12";
    #$ip = "192.168.22.196";
    $resp = $thrift->getResponse(array($appkey, $ip), 'SGAgentClient', 'getLocalConfig'); 
    print_r($resp);
    if ($resp == 0) 
    {
        print_r($resp);
    } else {
        echo "getLocalConfig  err!";
        break;
    }
}

?>
