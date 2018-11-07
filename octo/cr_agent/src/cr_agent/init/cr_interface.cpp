//
// Created by smartlife on 2017/8/12.
//
#include "cr_interface.h"
#include <sys/types.h> 
#include <sys/stat.h>

CCInterface* m_CCInterfaceInstance=NULL;

pthread_mutex_t CCInterface::m_CMutex=PTHREAD_MUTEX_INITIALIZER;
CCInterface *CCInterface::GetCCInstance() {

  pthread_mutex_lock(&m_CMutex);
  static CCInterface m_CCInterfaceInstance;
  pthread_mutex_unlock(&m_CMutex);
  return &m_CCInterfaceInstance;

}
CCInterface::CCInterface(){

  pthread_mutex_init(&m_CMutex,NULL);

}
CCInterface::~CCInterface(){

  pthread_mutex_destroy(&m_CMutex);

}
extern "C" {
 int InitHost(const std::map<std::string, HANDLE> &preload_map,
                 const std::vector<PluginInfo> &plugins);
 int upgradeplugins(const std::vector<PluginInfo> &plugins);
 int GetPluginInfos(std::vector<PluginInfo> *plugins);
 int UnInitHost();
}
 int UnInitHost(){
	return 0;
}
 int GetPluginInfos(std::vector<PluginInfo> *plugins){

     return 0;
}

int upgradeplugins(const std::vector<PluginInfo> &plugins){ 
  
     return 0;
}
void *GetLoadedByModuleType(LOADED_MODULE_TYPE cLoadType) {
  switch (cLoadType) {
    case CRANE_CLIENT_MODULE:{
      return NULL;
      }
    case EXTEND_CLIENT_MODULE:{
	  return NULL;
	}
    default:{
      return NULL;
	}
  }
  return NULL;
}
//to free the loaded resource
int FreeLoadedByModuleType(LOADED_MODULE_TYPE cLoadType) {
  return 0;
}

void* StartCraneAgent(void* args){
  
   int listenPort=5288;
	if(NULL != CCInterface::GetCCInstance()) { 
        
   CCInterface::GetCCInstance()->GetConfigIns()->LoadConf();
	 listenPort=CCInterface::GetCCInstance()->GetConfigIns()->GetListenPort();
	}
  	LOG_INFO("start crane agent");
  	boost::shared_ptr<TcpServer> craneTcpServer
		          = boost::make_shared<TcpServer>(listenPort);
	craneTcpServer->StartServer();
}

void* StartCraneMonitor(void* args){
  
	LOG_INFO("start crane monitor");
  prctl(PR_SET_NAME,"cr_agent_monitor");
  CraneServiceMonitor cMonitorSevice;

  LOG_INFO("StartCraneMonitor");
  cMonitorSevice.StartServiceMonitor();
}

int InitHost(const std::map<std::string, HANDLE> &preload_map,
		const std::vector<PluginInfo> &plugins) {

	pthread_t mTid,sTid;
	int status; 
	status=mkdir("/var/sankuai/logs/crane_agent", 0777);
  if(0==status){
		printf("创建目录成功\r\n");
	//	LOG_INFO("create the crane_agent logs dir");
	}else{
		printf("创建目录失败=%d\r\n",status);
	//	LOG_INFO("logs dir has created,or no accessed rights");
	}	
  
	log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(
				LOG_SERVER_CONF.c_str()));
	int mRet=0,sRet=0;
	if(IS_HEALTHY==CrAgentInit::CheckHealthy()){

		prctl(PR_SET_NAME,"cr_agent_parent");
		mRet=pthread_create(&mTid,NULL,StartCraneAgent,NULL);
		if(mRet<0){
			LOG_ERROR("create the main thread failed");
			return INIT_FAILED;
		}
		sRet=pthread_create(&sTid,NULL,StartCraneMonitor,NULL);
		if(sRet<0){
			LOG_ERROR("create the monitor thread failed");
			return INIT_FAILED;
		}
		LOG_INFO("InitHost sucess");
		return INIT_SUCESS;
	}else{
		LOG_INFO("InitHost failed");
		return INIT_FAILED;
	}
	return INIT_SUCESS;
}


