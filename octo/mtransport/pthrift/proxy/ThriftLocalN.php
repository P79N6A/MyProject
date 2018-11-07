<?php
/**
 * 此文件负责处理当agent不可用的时候，从本地读取配置，以实现容错性
 */

class ThriftLocalN
{
    public $path_ = "/tmp";
    public $appkey_ = "";

    public function __construct($appkey, $path = null)
    {
        $this->appkey_ = $appkey;
        if($path != null)
            $this->path_ = $path;
    }

    public function localFileName()
    {
        return $this->path_ . "/" .$this->appkey_ . ".tmp";
    }

    public function Write($serviceList)
    {
        $fileName = $this->localFileName();
        file_put_contents($fileName, serialize($serviceList), LOCK_EX);
    }

    public function Read(&$serviceList)
    {
        if(!$this->isExist())
        {
            $serviceList = array();
            return;
        }

        $fileName = $this->localFileName();
        $content = file_get_contents($fileName);

        $serviceList = unserialize($content);
    }

    public function isExist()
    {
        $fileName = $this->localFileName();
        return file_exists($fileName);
    }

    public function hasChange($serviceList)
    {
        $this->Read($localList);
        
        $localString = serialize($localList);
        $newString = serialize($serviceList);

        if($localString == $newString)
            return 0;

        return 1;
    }
}

?>
