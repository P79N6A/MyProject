<?php

require_once dirname(__FILE__).'/sgagent/SGAgent.php';
require_once dirname(__FILE__)."/ThriftLocalN.php";

class ThriftPoolN
{
    private $serverGroups;      // 服务组，每组中有若干个IP和Port配置
    private $conf;              // 配置，包括超时时间等
    private $numRetries = 3;    // 当连接服务失败时的重试的服务次数

    private $sgagentHandler;   //SG agent的服务句柄
    private $sgagentHost = "192.168.4.252";
    #private $sgagentHost = "192.168.3.163";
    private $sgAgentPort = 5266;

    private $localAppKey;
    private $remoteAppkey;
    private $lastCheckTime;     //SG agent上次更新服务列表的时间
    private $localIp;           //本地的ip地址
    private $thriftLocal;       //本地缓存的配置

    private $persistent;
    private $randomize;
    private $nonblocking;

    /**
     * 构造函数
     *
     * @param Array('local'=>, 'remote'=>) $appKeys 服务appkey
     * @param Array(String key => Mixed val) $conf 配置数组
     * @param Boolean $persistent 是否为持久连接
     * @param Boolean $randomize 是否服务组内随机
     */
    public function __construct($appKeys = null, $conf = null,  $persistent = false, $randomize = false, $nonblocking = false)
    {
        if (!is_array($conf)) {
            $this->conf = array();
        } else {
            $this->conf = $conf;
        }

        $this->persistent = $persistent;
        $this->randomize = $randomize;
        $this->nonblocking = $nonblocking;

        $this->localIp = trim(`hostname`);
        $this->localAppKey = $appKeys['local'];
        $this->remoteAppKey = $appKeys['remote'];

        $this->thriftLocal = new ThriftLocalN($this->remoteAppKey);
        /**
         * 初始化sgAgent handler，并获取服务列表
         */
        $this->sgagentHandler = new ThriftN($this->sgagentHost, $this->sgAgentPort, true, false, true);

        $serviceList = $this->getServiceList();

        $this->serverGroups = array();
        $this->initialize($serviceList, $randomize, $persistent, $nonblocking);
    }

    public function __get($name)
    {
        if (isset($this->$name)) {
            return $this->$name;
        } else {
            return null;
        }
    }

    public function getServiceList()
    {
        //先从agent里面重新取一次
        $serviceList = array();
        try
        {
            $serviceList = $this->sgagentHandler->getResponse("SGAgentClient",
                                array($this->localAppKey, $this->remoteAppKey),
                                "getServiceList");

            //取成功了
            //判断是否有更新，有则写到文件里面去
            if($this->thriftLocal->hasChange($serviceList))
                $this->thriftLocal->Write($serviceList);
        }
        catch (TException $e)
        {
            //取失败了，从本地文件取
            $this->thriftLocal->Read($serviceList);
        }

        if(count($serviceList) == 0)
        {
            throw TException("service $this->remoteAppKey list empty");
        }

        //过滤service列表，去掉里面的status状态不对的服务列表
        $returnList = array();
        for($i = 0; $i < count($serviceList); $i++)
        {
            if($serviceList[$i]->status != 0)
                $returnList[] = $serviceList[$i];
        }

        return $returnList;
    }

    /**
     * 设置连接失败后的重试次数
     *
     * @param Integer $num 重试次数
     */
    public function setNumRetries($num)
    {
        $this->numRetries = $num;
    }


    /**
     * 初始化服务组
     *
     * @param Array(SGService()) $serverList 服务组
     * @param Boolean $persistent 是否为持久连接
     * @param Boolean $randomize 是否服务组内随机
     */
    private function initialize($serverList, $randomize, $persistent, $nonblocking)
    {
        $groupNum = count($serverList);
        for ($groupIdx = 0; $groupIdx < $groupNum; $groupIdx++) {
            $srv = $serverList[$groupIdx];

            $port = $srv->port;
            $ip = $srv->ip;
            $weight = $srv->weight;

            //初始化thrift，然后加入到服务列表里
            $thrift = new ThriftN($ip, $port, $persistent, $randomize, $nonblocking);
            $thrift->setOption($this->conf);
            $thrift->weight = $weight;

            $this->serverGroups[] = $thrift;
        }

        $this->lastCheckTime = time(0);
    }


    /**
     * 从服务端获得响应结果值
     *
     * @param Mixed $request 输入数据
     * @param String $funcName 接口函数名
     * @param Mixed $hash hash策略
	 * @param Array(String host, Integer port) $hostInfo 服务端ip:port
     * @return Mixed $dealsorted 返回结果
     * @exception TException
     */
    public function getResponse($request, $clientName, $funcName, $hash = null, &$hostInfo = null)
    {
        //$this->reloadServiceList(); //重新拉取一次服务列表

        if (!($hash instanceof HashStrategy)) {
            $hashStrategy = new RoundRobinWithWeightHash();
        } else {
            $hashStrategy = $hash;
        }
        $thriftGroups = $hashStrategy->doHash($this->serverGroups, $request);

        $errors = array();

        for($index = 0; $index < $this->numRetries && $index < count($thriftGroups); $index ++)
        {
            $thrift = $thriftGroups[$index];

            $client = new $clientName($thrift->transport);
            $start = $end = (int)(microtime(true) * 1000);  //获取ms

            $header = $this->createHeader(null, $thrift, $clientName, $funcName);

            try {
                $result = $thrift->getResponse($client, $request, $funcName, $hostInfo, $header);
                $end = (int)(microtime(true) * 1000);

                $this->sendModueInvokeInfo($thrift, $header, 0, $start, $end);

                if (isset($result)) {
                    return $result;
                }
            }
            catch (TException $e) {
                $errors[] = $e->getMessage();
                continue;
            }
        }

        $errMsg = preg_replace('/[\[\]]/', '', implode('; ', $errors));
        throw new TException($errMsg);
    }

    /**
     * @param $thirft thrift连接
     * @param $header 类名
     * @param $ret 返回值
     * @param $start 开始时间ms
     * @param $end 结束时间ms
     */
    public function sendModueInvokeInfo($thrift, $header, $ret, $start, $end)
    {

        try
        {
            $cost = $end - $start;
            $info  = new SGModuleInvokeInfo();
            $info->traceId = $header->traceId;
            $info->spanId = $header->spanId;
            $info->spanName = $header->spanName;
            $info->localAppKey = $this->localAppKey;
            $info->localHost = $this->localIp;
            $info->localPort = 0;   
            $info->remoteAppKey = $this->remoteAppkey;
            $info->remoteHost = $thrift->socket->getHost();
            $info->remotePort = $thrift->socket->getPort();
            $info->start = $start;
            $info->cost = $end - $start;
            $info->type = 0;
            $info->status = 0;
            $info->count = 1;

            $this->sgagentHandler->getResponse("SGAgentClient", 
                        array($info), "uploadModuleInvoke");

        }catch (TException $e)
        {
            $errors[] = $e->getMessage();
            $errMsg = preg_replace('/[\[\]]/', '', implode('; ', $errors)); 
            //throw new TException($errMsg); //如果出错了，非关键流程，所以不报错
        }
    }

    public function create_uuid($prefix = "")
    {
        $str = md5(uniqid(mt_rand(), true));   
        $uuid  = substr($str,0,8) . '-';   
        $uuid .= substr($str,8,4) . '-';   
        $uuid .= substr($str,12,4) . '-';   
        $uuid .= substr($str,16,4) . '-';   
        $uuid .= substr($str,20,12);   
        return $prefix . $uuid;
    }


    public function reloadServiceList()
    {
        $serviceList = $this->getServiceList();

        $this->serverGroups = array();

        $this->initialize($serviceList, $this->randomize, $this->persistent, $this->nonblocking);
           
    }

    public function createHeader($header = null, $thrift = null, $clientName = null, $funcName = null)
    {
        if($header != null)
            return $header;

        $header = new RequestHeader();
        $header->traceId = $this->create_uuid();
        $header->spanId = "0";
        $header->clientAppkey = $this->remoteAppkey;
        $header->clientIp = $this->localIp;
        $header->spanName = $clientName.".".$funcName;
        $header->serverIpPort = $thrift->socket->getHost();
        $header->debug = 0;
        $header->sample = 0;
        $header->version = "php-version-0.0.1";

        return $header;
    }
}


interface HashStrategy {
    public function doHash($srvGroups, $request);
}


class RandomHash implements HashStrategy {
    public function doHash($srvGroups, $request = null)
    {
        shuffle($srvGroups);
        return $srvGroups;
    }
}

class UuidHash implements HashStrategy {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->uuid)) {
            $uuid = intval(substr($request->uuid, -2));
            $id = $uuid & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $serverGroups[$id] = $serverGroups[0];
                $serverGroups[0] = $thrift;
                return $serverGroups;
            }
        } else {
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class UserIdHash implements HashStrategy {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->usrid)) { // 登录用户会落到同一个服务
            $id = $request->usrid & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $serverGroups[$id] = $serverGroups[0];
                $serverGroups[0] = $thrift;
                return $serverGroups;
            }
        } else { // 非登录用户随机选择group
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class IpHash implements HashStrategy {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->ip)) {
            $id = $request->ip & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $serverGroups[$id] = $serverGroups[0];
                $serverGroups[0] = $thrift;
                return $serverGroups; 
            }
        } else {
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class RoundRobinWithWeightHash implements HashStrategy {
    public function doHash($srvGroups, $request)
    {
        $max = 0;
        $min = 0x7fffffff;

        foreach($srvGroups as $srv){
            $weight = $srv->weight;
            if($weight > $max)
                $max = $weight;
            if($weight < $min)
                $min = $weight;
        }

        if(count($srvGroups) == 0)
            return null;

        if(count($srvGroups) == 1)
            return $srvGroups;

        $gcd = $this->getGcdOfServiceList($srvGroups);

        while(true)
        {
            $this->i = ($this->i + 1) % count($srvGroups);
            if($this->i == 0)
            {
                $this->cw = $this->cw - $gcd;
                if($this->cw < 0)
                {
                    $this->cw = $max;
                    if($this->cw == 0)
                        return null;
                }
            }

            if($srvGroups[$this->i]->weight >= $this->cw)
            {
                $thrift = $srvGroups[$this->i];
                $serverGroups[$this->i] = $serverGroups[0];
                $serverGroups[0] = $thrift;
                return $srvGroups;
            }
        }
           
        return 0;
    }

    public function getGcdOfServiceList($srvGroups)
    {
        if(count($srvGroups) == 1)
        {
            return $srvGroups[0]->weight;
        }
        else
        {
            $gcd = $this->getGcd($srvGroups[0]->weight, $srvGroups[1]->weight);
            for($i = 2; $i < count($srvGroups); ++$i)
            {
                $gcd = $this->getGcd($gcd, $srvGroups[$i]->weight);
            } 

            return $gcd;
        }
    }

    public function getGcd($a, $b)
    {
        $c = $a % $b;
        if($a < $b)
        {
            $t = $b;
            $b = $a;
            $a = $t;
        }

        while($c != 0)
        {
            $a = $b;
            $b = $c;
            $c = $a % $b;
        }

        return $b;
    }

    private $i = -1;
    private $cw = 0;
}


