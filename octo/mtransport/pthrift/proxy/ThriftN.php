<?php
/**
 * 封装thrift通讯
 *
 * @package frameworks
 */

require_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';
require_once $GLOBALS['THRIFT_ROOT'].'/protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TSocketPool.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TBufferedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/transport/TFramedTransport.php';

class ThriftN
{
    public $socket;

    public $transport;

    public $options = array();

    private $nonBlockingFlag;

    public $weight;    //此连接服务的权重

    /**
     * 构造函数
     *
     * @param String|Array(String, ..) $host
     * @param Integer|Array(Integer, ..) $port 使用数组时，$host和$port的sizeof()要一样多
     * @param Boolean $persist 是否使用持久socket连接 默认false
     * @param Boolean $randomize 在多个server时，是否随机使用一个服务，默认false表示优先用配置中的第一个，否则随机用一个
     * @throw TExpection
     * @return Void
     */
    public function __construct($host, $port, $persist = false, $randomize = false, $nonblocking = false)
    {
        if (is_array($host) && is_array($port)) {
            $this->socket = new TSocketPool($host, $port, $persist);
            $this->socket->setRandomize($randomize);
        } else {
            $this->socket = new TSocket($host, $port, $persist);
        }
        $this->nonBlockingFlg = $nonblocking;
    }

    /**
     * 设置连接选项
     * 设置选项$key的值是$value
     * 或者$key = array(key => value, ...)
     *
     * 可用选项
     * sendTimeout 毫秒
     * recvTimeout 毫秒
     *
     * @param Mixed $key
     * @param Mixed $value
     * @return Void
     */
    public function setOption($key, $value = null)
    {
        if (is_array($key)) {
            $arr = $key;
            foreach ($arr as $key => $value) {
                $this->options[$key] = $value;
            }
        } else {
            $this->options[$key] = $value;
        }
    }

    /**
     * 获取连接
     *
     * @throw TExpection
     * @return $protocol Thrift协议层
     */
    protected function getConn()
    {
        if (isset($this->options['sendTimeout'])) {
            $this->socket->setSendTimeout($this->options['sendTimeout']);
        }
        if (isset($this->options['recvTimeout'])) {
            $this->socket->setRecvTimeout($this->options['recvTimeout']);
        }
        if (!$this->nonBlockingFlg) {
            $this->transport = new TBufferedTransport($this->socket, 1024, 1024);
        } else {
            $this->transport = new TFramedTransport($this->socket, 1024, 1024);
        }
        $protocol = new TBinaryProtocolAccelerated($this->transport);
        return $protocol;

    }

    /**
     * 发送请求，返回应答
     * @deprecated
     * @see invoke
     * @param String $client
     * @param Object $req
     * @param String $method
     * @param Array(String host, Integer port) $hostInfo 服务端ip:port
     * @param RequestHeader $header trace的头信息
     * @throw TExpection
     * @return Object
     */
    public function getResponse($client, $req, $method = 'Request', &$hostInfo = null, $header = null)
    {
        $protocol = $this->getConn();
        if($header != null)
            $protocol->setHeader($header);

        if(!$this->transport->isOpen())
        {
            $this->transport->open();
        }

        $hostInfo = array($this->socket->getHost(), $this->socket->getPort());
        // 生成client 发送请求

        $client = new $client($protocol);
        if (!is_array($req)) {
            $req = array($req);
        }

        $rsp = call_user_func_array(array($client, $method), $req);
        $this->transport->close();
    
        return $rsp;
    }

    public function query($client, $method,  $req)
    {
        $protocol = $this->getConn();
        $this->transport->open();

        // 生成client 发送请求
        $client = new $client($protocol);

        // XXX this is ugly!
        switch (func_num_args()) {
            case 3:
                $a1 = func_get_arg(2);
                $rsp = $client->$method($a1);
                break;
            case 4:
                $a1 = func_get_arg(2);
                $a2 = func_get_arg(3);
                $rsp = $client->$method($a1, $a2);
                break;
            case 5:
                $a1 = func_get_arg(2);
                $a2 = func_get_arg(3);
                $a3 = func_get_arg(4);
                $rsp = $client->$method($a1, $a2, $a3);
                break;
            case 6:
                $a1 = func_get_arg(2);
                $a2 = func_get_arg(3);
                $a3 = func_get_arg(4);
                $a4 = func_get_arg(5);
                $rsp = $client->$method($a1, $a2, $a3, $a4);
                break;
            default:
                $rsp = null;
        }
        $this->transport->close();
        return $rsp;
    }

    /**
     * 执行client中的方法
     *
     * @param String $clientClass
     * @param String $methodName
     * @param Array $argts
     * @return Object
     */
    public function invoke($clientClass, $methodName, $args = array())
    {
        if (!class_exists($clientClass)) {
            trigger_error('Class ' . $clientClass . ' did not exist!', E_USER_ERROR);
            return null;
        }
        $protocol = $this->getConn();
        $this->transport->open();

        $client = new $clientClass($protocol);
        if (!method_exists($client, $methodName)) {
            trigger_error('Method '. $methodName .' for class ' .$clientClass . ' did not exist!', E_USER_ERROR);
            return null;
        }

        $result = call_user_func_array(array(&$client, $methodName), $args);
        $this->transport->close();
        return $result;
    }
}
