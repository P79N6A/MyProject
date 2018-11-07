Mns-Invoker
====================
Mns-Invoker定位为使用MNS, MCC的调用组件, 其核心功能: 
从 Mns 获取服务信息, 获取动态配置信息, 为调用SgAgent 提供连接管理.
[设计文档](http://wiki.sankuai.com/pages/viewpage.action?pageId=385981490)<br/> 


### 从 MNS 获取服务信息
* 服务注册 : 
* Thrift: MnsInvoker.registerThriftService(String appKey, int port);
* MnsInvoker.registerHttpService(String appKey, int port);
* 获取服务列表
* MnsInvoker.getServerList(final String localAppkey, final String remoteAppkey);

### 从 MCC 获取动态配置信息
* 写入配置项:
*   MnsInvoker.setConfig(String appkey, String data);
*   MnsInvoker.setConfig(String appkey, String env, String path, String data);
* 读取配置项目:
*   MNSInvoker.getConfig(String appkey);
*   MNSInvoker.getConfig(String appkey, String env, String path);
* 文件配置:
*   MNSInvoker.getFileConfig(String appkey, String filename)
*   MNSInvoker.fileConfigAddApp(String appkey)
*    MNSInvoker.addListener(String appkey, FileChangeListener listener);


###  Mtrace 通过 Mns-Invoker 上传信息
* 参考 Mtrace 内部实现

### 集成测试环境
* 