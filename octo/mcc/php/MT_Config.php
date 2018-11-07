<?php
class MT_Config
{
    function __construct()
    {
        $args = func_get_args(); //获取构造函数中的参数
        $num = count($args);
        if (method_exists($this,$f='add_app'.$num))
        {
            call_user_func_array(array($this,$f),$args);
        }
    }
    public function add_app3($appkey, $env, $path)
    {
        $this -> m_appkey = $appkey;
        $this -> m_env = $env;
        $this -> m_path = $path;
        return $this -> _add_app();
    }
    public function add_app2($appkey, $path)
    {
        $this -> m_appkey = $appkey;
        $this -> m_path = $path;
        return $this -> _add_app();
    }

    public function add_app1($appkey)
    {
        $this -> m_appkey = $appkey;
        return $this -> _add_app();
    }

    public function get($key)
    {
        return sg_agent_config_get($key,
            $this -> m_appkey, $this -> m_env,
            $this -> m_path);
    }
    public function set($key, $value)
    {
        return sg_agent_config_set($key, $value,
            $this -> m_appkey, $this -> m_env, $this -> m_path);
    }

    private function _add_app()
    {
        return sg_agent_config_add_app($this -> m_appkey,
            $this -> m_env, $this -> m_path);
    }

    private $m_appkey;
    private $m_env = "";
    private $m_path = "/";
}
?>
