Dorado-Parent
====================
![dorado](http://wiki.sankuai.com/download/attachments/196868879/Dorado.png?version=1&modificationDate=1449561906931&api=v2 "dorado")  

### dorado-core 核心模块
* 基础数据机构定义
* 异步处理Handler
* Server 处理 Handler
* Server、Client 初始化
* 网络层：TCP、HTTP、UDP 以及连接管理

### dorado-codec 编解码模块
* 编解码格式定义 
* 序列化模块：Thrift TBinary、Hessian，兼容 Pigeon
* 参考：[协议设计](http://wiki.sankuai.com/pages/viewpage.action?pageId=376640732)<br/> 

### dorado-cluster 集群化模块
* 服务注册、路由，调用 Mns-Invoker
* 负载均衡：RoundRobin + weight、Consistent Hashing
* 容错

### dorado-service 业务服务模块
* 服务发布，调用的代理
* 接口调用拦截
* Thrift Annotation 支持
* 服务鉴权（auth）
* 请求过滤（filter）

### dorado-testing 集成测试模块
* 自动的集成测试、回归测试，性能测试
* 借助 jekins 实现自动化，生成测试报告

### dorado-monitor 监控模块
* Server 自身信息上报
* 提供监控接口，供 Scanner 调用

### dorado-demo 框架使用示例
*  提供业务参考的 demo


  