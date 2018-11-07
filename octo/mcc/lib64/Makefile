CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H -fPIC

INCLUDE =-I../ \
		-I../../../ \
		-I../.. \
		-I../clog \
		-I../comm \
		-I../util \
		-I../thrift \
		-I../thrift/transport \
        -I../thrift/async \
        -I../thrift/concurrency \
        -I../thrift/processor \
        -I../thrift/protocol \
        -I../thrift/qt \
        -I../thrift/server \
		-I../gen_cpp \
		-I../../../util \
		-I../../sg_agent \
		-I../../sg_agent/util \
		-I../../sg_agent/log4cplus \
		-I../../sg_agent/comm \
		-I../config \

LIB = -L./ -levent \
	-lpthread \
	-llog4cplus \
	-lm \

VPATH = ../../../util \
		../../sg_agent \
		../clog \
		../config \
		../gen_cpp \
		../../sg_agent/comm/ \
		../thrift \
        ../thrift/async \
        ../thrift/concurrency \
        ../thrift/processor \
        ../thrift/protocol \
        ../thrift/qt \
        ../thrift/server \
        ../thrift/transport \


OUTPUT  = lib64_linux/libsg_agent.a

OUTPUT_MAC  = lib64_mac/libsg_agent.a

OBJS    = sg_agent_config_client.o \
		sg_agent_config_client.o \
		sg_agent_config_processor.o \
		sg_agent_config_instance.o \
		sg_agent_fileconfig_processor.o \
		sg_agent_fileconfig_instance.o \
		sg_agent_fileconfig_instance.o \
		Thrift.o \
		TBufferTransports.o \
		TApplicationException.o \
		TSocket.o \
		log.o \
		log4cplus.o \
		md5.o \
		SGAgent.o \
		config_common_types.o\
		config_common_constants.o\
		quota_common_types.o \
		sgagent_common_types.o \
		sgagent_common_constants.o\
		aggregator_common_types.o \
		aggregator_common_constants.o \


all:$(OUTPUT)

mac:$(OUTPUT_MAC)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -o $@ $^

$(OUTPUT): $(OBJS)
	ar -r -o $@ $^

$(OUTPUT_MAC): $(OBJS)
	ar -r $@ $^

clean:
	rm -f *.o *.~ *.bak

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
