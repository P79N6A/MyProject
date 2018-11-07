<?php

$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';

include dirname(__FILE__).'/sgagent/SGAgent.php';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

/**
 * 这里local定位为自己的appkey，remote为要调用的服务的appkey
 * appkey是在msgp里面申请的，http://msgp.sankuai.com里面申请
 */
$server = array('local' => "test", "remote" => "sgagent");

/**
 * 设置发送和接收的超时时间，单位ms
 */
$options = array (
        'sendTimeout' => 1000,
        'recvTimeout' => 1000,
    );

/**
 * 要传入的参数，根据每个业务不同，自己设置参数
 */
$localAppKey = "test";
$remoteAppKey = "regproxy";

/**
 * new 一个ThrifPoolN的类，参数参考ThriftPoolN的类定义
 */
$thrift = new ThriftPoolN($server, $options, false, false, true);

/**
 * 如果在开发下，或者需要手动设置服务列表的情况下，可以这做
 * new 一个SGService的类，填入ip，port，weight（权重）调用setUserServiceList设置一下就可以了
 * 在这种情况下，调用的服务不会根据octo的agent来获取服务列表，而是用户指定的这个服务列表
 */
/*
$service = new SGService();
$service->ip = "192.168.11.90";
$service->port = 9092;
$service->weight = 10;
$serviceArray[] = $service;
$thrift->setUserServiceList($serviceArray);
*/

/**
 * 调用接口，沿用之前的调用方式
 */
$resp = $thrift->getResponse(array($localAppKey, $remoteAppKey,), 'SGAgentClient', 'getServiceList');


print_r($resp);
?>
