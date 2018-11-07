<?php
$GLOBALS['THRIFT_ROOT'] = '/root/workspace/thrift';
include dirname(__FILE__).'/ThriftPoolN.php';
include dirname(__FILE__).'/ThriftN.php';

$local = "abc";
$remote = "com.sankuai.inf.chenxin1";
$agent = "com.sankuai.inf.sg_agent";

//local是自己的appkey， remote是要调用的appkey
$server = array('local' => $local, 'remote' => $agent, );

/**
 * 设置发送和接收的超时时间，单位ms
  */
$options = array('sendTimeout' => 1000, 'recvTimeout' => 100, );

$thrift = new ThriftPoolN($server, $options, true, false, true);

try {
    $resp = $thrift->getResponse(array($local, $remote), 'SGAgentClient', 'getServiceList');
}
catch(Exception $e) {
    error_log($e->getMessage(), 3, "/root/pthrift-errors.log");
    print $e->getMessage();
}
//$nums = count($resp);
print_r($resp);
?>
