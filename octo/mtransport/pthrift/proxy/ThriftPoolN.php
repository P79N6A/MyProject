<?php

require_once dirname(__FILE__).'/sgagent/SGAgent.php';
require_once dirname(__FILE__)."/ThriftLocalN.php";

class ThriftPoolN
{
    private $serverGroups;      // 服务组，每组中有若干个IP和Port配置
    private $conf;              // 配置，包括超时时间等
    private $numRetries = 3;    // 当连接服务失败时的重试的服务次数

    private $sgagentHandler;   //SG agent的服务句柄
    private $sgagentHost = "127.0.0.1";
    private $sgAgentPort = 5266;
    private $localAppKey;
    private $remoteAppkey;
    private $remotePort = 0;    //远端服务的端口，在特殊情况下使用
    private $lastAgentTime;     //SG agent上次更新服务列表的时间
    private $lastConfigTime;    //上一次获取配置的时间
    private $isDebug;           //是否开启debug
    private $localIp;           //本地的ip地址
    private $thriftLocal;       //本地缓存的配置

    private $persistent;
    private $randomize;
    private $nonblocking;
    private $SERVICE_STATUS_ALIVE = 2; //服务状态可用
    private $CACHE_TIME = 10;           //本地缓存的时间
    private $pthrift_version = "phthrift-v1.0.2"; //phthrift的版本

    private $isUserServiceListSet = false;  //是否是手动设置了服务列表
    private $userServiceList;
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
        if(isset($appKeys['remote_port']))
            $this->remotePort = $appKeys['remote_port'];

        $this->thriftLocal = new ThriftLocalN($this->remoteAppKey);
        /**
         * 初始化sgAgent handler，并获取服务列表
         */
        $this->sgagentHandler = new ThriftN($this->sgagentHost, $this->sgAgentPort, true, false, true);
        $this->sgagentHandler->socket->setSendTimeout(500);
        $this->sgagentHandler->socket->setRecvTimeout(500);

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

    public function setUserServiceList($serviceList)
    {
        $this->isUserServiceListSet = true;
        $this->userServiceList = $serviceList;
        $this->initialize($this->userServiceList, $this->randomize, $this->persistent, $this->nonblocking);
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
            //不再抛这个异常，因为有可能用户会手动设置服务列表
            //throw new TException("service $this->remoteAppKey list empty");
        }

        //过滤service列表，去掉里面的status状态不对的服务列表
        $returnList = array();
        for($i = 0; $i < count($serviceList); $i++)
        {
            //过滤端口不符合的服务列表
            if($this->remotePort != 0 && $this->remotePort !=  $serviceList[$i]->port)
                continue;

            if($serviceList[$i]->status == $this->SERVICE_STATUS_ALIVE)
                $returnList[] = $serviceList[$i];
        }
        return $returnList;
    }


    public function getLocalConfig()
    {
        if($this->lastConfigTime + $this->CACHE_TIME > time(0))
            return $this->isDebug;

        $this->lastConfigTime = time(0);
        try
        {
            $returnJson = $this->sgagentHandler->getResponse("SGAgentClient",
                            array($this->localAppKey, $this->localIp),
                            "getLocalConfig");
            if($returnJson != "")
            {
                $obj = json_decode($returnJson);
                if(isset($obj->mtraceDebug))
                {
                    $this->isDebug = $obj->mtraceDebug;
                    return $obj->mtraceDebug;
                }
                else
                {
                    $this->isDebug = false;
                    return false;
                }
            }
        }catch(Exception $e){
            $this->isDebug = false;
            return false;
        }
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

        $this->lastAgentTime = time(0);
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
        $this->reloadServiceList(); //重新拉取一次服务列表

        if (!($hash instanceof HashStrategy)) {
            $hashStrategy = new RoundRobinWithWeightHashN();
        } else {
            $hashStrategy = $hash;
        }
        $thriftGroups = $hashStrategy->doHash($this->serverGroups, $request);

        if(count($thriftGroups) == 0)
            throw new TException("Server list empty");


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

                try
                {
                    $this->sendModueInvokeInfo($thrift, $header, 0, $start, $end);
                }
                catch (Exception $e)
                {
                    //do nothing//
                }

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
            $info->remoteAppKey = $this->remoteAppKey;
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
        //如果用户是手动指定的服务列表，则不做任何事情了
        if($this->isUserServiceListSet)
            return;

        //如果是时间没有到，则继续使用上次的服务列表
        if($this->lastAgentTime + $this->CACHE_TIME > time(0))
            return;

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
        $header->debug = $this->getLocalConfig();
        $header->sample = 0;
        $header->version = $this->pthrift_version;

        return $header;
    }
}


interface HashStrategyN {
    public function doHash($srvGroups, $request);
}


class RandomHashN implements HashStrategyN {
    public function doHash($srvGroups, $request = null)
    {
        shuffle($srvGroups);
        return $srvGroups;
    }
}

class UuidHashN implements HashStrategyN {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->uuid)) {
            $uuid = intval(substr($request->uuid, -2));
            $id = $uuid & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $srvGroups[$id] = $srvGroups[0];
                $srvGroups[0] = $thrift;
                return $srvGroups;
            }
        } else {
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class UserIdHashN implements HashStrategyN {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->usrid)) { // 登录用户会落到同一个服务
            $id = $request->usrid & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $srvGroups[$id] = $srvGroups[0];
                $srvGroups[0] = $thrift;
                return $srvGroups;
            }
        } else { // 非登录用户随机选择group
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class IpHashN implements HashStrategyN {
    public function doHash($srvGroups, $request)
    {
        if (($request instanceof Login_Info) && isset($request->ip)) {
            $id = $request->ip & (count($srvGroups) - 1);
            if ($id != 0) {
                $thrift = $srvGroups[$id];
                $srvGroups[$id] = $srvGroups[0];
                $srvGroups[0] = $thrift;
                return $srvGroups; 
            }
        } else {
            shuffle($srvGroups);
        }
        return $srvGroups;
    }
}

class RoundRobinWithWeightHashN implements HashStrategyN {
    public function doHash($srvGroups, $request)
    {
        $sum = 0;       //记录总的权重值
        $arr = array(); //记录一个权重的区间
        for($i = 0; $i < count($srvGroups); $i++)
        {
            //第一个从0开始
            $sub = array();
            $sub['start'] = $sum + 1;
            $sub['end'] = $sum + $srvGroups[$i]->weight;
            $sum += $srvGroups[$i]->weight;

            $arr[$i] = $sub;
        }

        //算随机值
        $r = rand(1, $sum);
        for($i = 0; $i < count($srvGroups); $i++)
        {
            if($r >= $arr[$i]['start'] && $r <= $arr[$i]['end'])
            {
                $thrift = $srvGroups[$i];
                $srvGroups[$i] = $srvGroups[0];
                $srvGroups[0] = $thrift;
                return $srvGroups;
            }
        }

        return null;
    }
}


