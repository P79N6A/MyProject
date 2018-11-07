Mns-Invoker CheckList:
====================
### MNS 
* 多线程并发服务注册: RegisterTests
* 多线程并发获取服务列表: getServerTests
* 多线程并发获取降级信息: DegradeTests

### MCC 
* 动态配置读写: FileConfigTests
* 文件配置的读取和写入: ConfigTests

### 网络连接
* 连接异常中断, 恢复后, 服务能否同步恢复: getServerTests 测试过程中, 断开网络, 再打开
* 本地SgAgent 可用 -> 不可用 -> 可用, 连接能否在本地和哨兵之前按预期切换

### 内部数据结构&工具
* MnsCache: MnsCacheTests
* MultiMap: MultiMapTests
* ProcessInfo: ProcessInfoUtilTests
* InvokeProxy: InvokeProxyTests
* 哨兵列表: SentinelTests

