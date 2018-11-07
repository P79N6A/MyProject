enum ProviderStatus {
   DEAD = 0,
   STARTING = 1,
   ALIVE = 2,
   STOPPING = 3,
   STOPPED = 4,
   WARNING = 5,
 }

service UpdaterService {
     /*
      * providerPath:服务提供者的zk路径
      * status:Detector的探测结果
      */
     void doubleCheck(1:string providerPath, 2:ProviderStatus status)
 }