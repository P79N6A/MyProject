namespace java com.sankuai.sgagent.thrift.model
namespace cpp com.sankuai.cmtrace
/*
 * sg_agent的开关接口
 */
enum Switch {
        SwitchConfig = 1,    //  getLocalConfig 开关
        SwitchMtConfig   //  getConfig 开关
        SwitchLog        //  sendLog开关
        SwitchCommonLog  //  commonLog开关
        SwitchModuleInvoke // sendInvoke开关
        SwitchQuota      //   Quota开关
        SwitchSelfCheck  //   selfCheck开关
        SwitchMNSCache   //   MNSCache开关
        SwitchFileConfig //   getFileConfig开关
        SwitchHlb        //   getHlb开关
        SwitchAuth       //   服务访问控制开关开关
        SwitchProperties //   Http健康检查的开关
        SwitchEnv        //   环境切换开关
        SwitchAutoStage  // 环境切换到stage开关
        SwitchAutoTest   // 环境切换到test开关
        SwitchAutoRoute  // 动态自动归组
        SwitchMafka      // mafka client开关
}



