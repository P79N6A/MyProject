#local thrift file
MODULE = ../../../common/cpp/lib
BOOST_DIR = ${MODULE}/boost_1_59_0/boost/#/usr/local/boost/include/boost-1_33_1/
THRIFT_DIR = ${MODULE}/thrift-0.8.0_build/include/thrift/#/usr/include/thrift/
ZOOKEEPER_DIR = ${MODULE}/zookeeper/include/
THRIFT_GEN = ../../../sg_agent/gen-cpp/ 

CFLAGS += -g -Wno-deprecated -D HAVE_NETINET_IN_H -pthread 
LIB = -L./ -L${MODULE}/zookeeper -lzookeeper_mt -lthrift -lgtest -lpthread  

INCLUDE =-I./ \
		 -I${BOOST_DIR} -I${THRIFT_DIR} -I${ZOOKEEPER_DIR}\
		 -I../common \
		 -I${THRIFT_GEN}\
		 -I../../../sg_agent/gen-cpp/ \
		 -I../../../sg_agent/src/util/ \

OUTPUT  = test

VPATH = ../common \
		${THRIFT_GEN}\

OBJS    = main.o \
		  test_zk_client.o \
		  test_sg_agent.o \
		  test_worker.o \
		  interface_base.o \
		  cJSON.o \
		  tinyxml2.o \
		  md5.o \
		  sgagent_service_types.o \
		  sgagent_service_constants.o \
		  sgagent_common_types.o \
		  operation_common_types.o \
		  sgagent_common_constants.o \
		  SGAgent.o \
		  SGAgentWorker.o \
		  config_common_types.o \
		  config_common_constants.o \
		  quota_common_types.o \
		  quota_common_constants.o \
		  sg_agent_timeout_test.o \
		  sg_agent_swimlane_test.o \
		  sg_agent_protocol_timeout_test.o \
		  sg_agent_hlb_test.o \
		  sg_agent_register_test.o \
		  basic_interface_test.o \
		  zk_agent_other_test.o \
		  zk_agent_restart_test.o \
		  sg_work_restart_test.o \
		  sg_agent_switch_test.o \
		  sg_agent_multi_appkey_test.o \
		  mt_config_multi_appkey_test.o \
		  mt_config_timeout_test.o \
		  sg_agent_auth_test.o \
		  service_group_test.o \
		  common_log_collector_test.o \
		  mono_restart_log_collector_test.o \
		  all_restart_test.o \
		  self_check_test.o \
		  mt_config_test.o \
		  quota_timeout_test.o \
		  file_config_test.o \
		  aggregator_common_types.o\
		  sg_agent_getEnv_test.o\
		  sg_agent_region_test.o\
		  sg_agent_backup_test.o\
		  restart_sg_agent_full_env_test.o \
      sg_agent_shutdown_test.o \
		  sg_agent_protocol_test.o \
		  sg_agent_registerwithCmd_test.o \
		  falcon_test.o \

all:$(OUTPUT) 

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -c $^ 

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -o $@ $^

$(OUTPUT): $(OBJS)
	$(CXX) $(CFLAGS) -o $@ $^ ${LIB}
	
clean:
	rm -f *.o *.~ *.bak
	rm -f $(OUTPUT)
