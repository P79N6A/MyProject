//
// Created by smartlife on 2017/8/7.
//
#include "cr_agent_init.h"
 int CrAgentInit::InitEnv() {
    
	 return 0;
}
static int CheckFilePath(){
   
	   return 0;
}
int CrAgentInit::CheckHealthy(void){

	int mHealthyBit=0;
	if (0 != InitEnv()) {
		LOG_ERROR("fail to init crane env");
	} else {
		LOG_INFO("env set pass");
		mHealthyBit|=2;
	}

	tinyxml2::XMLDocument crConfXml;
	LOG_INFO("config path"<<CR_AGENT_CONF.c_str());
	tinyxml2::XMLError crConfXmlRet = crConfXml.LoadFile(CR_AGENT_CONF.c_str());
	if (tinyxml2::XML_SUCCESS != crConfXmlRet) {
		LOG_ERROR("fail to load ");
	} else {
		LOG_INFO("xml set pass");
		mHealthyBit|=4;
	}

	if(0!=CheckFilePath()){
    LOG_INFO("the file path check is not used current");
	}else{
		LOG_INFO("CheckFilePath pass");
		mHealthyBit|=8;

	}
	if(IS_HEALTHY==mHealthyBit){

	}else{
		LOG_ERROR("Crane agent is not healthy");
	}
	return mHealthyBit;
}

