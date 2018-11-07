<?php
    include "MT_Config.php";
    $appkey = "com.sankuai.inf.sg_agent";
    $key = "key";
    $appkey = "com.sankuai.inf.mcc_test";
    $key = "key";

    $config = new MT_Config($appkey);

    $ret = $config -> get($key);
    echo "IN PHP: get ret = ".$ret."\n";
?>
