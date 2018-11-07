CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H -O0

INCLUDE =-I../ \
		-I../../../ \
		-I../.. \
		-I../comm \
		-I../comm/include \
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
		-I../../sg_agent/src \
		-I../../sg_agent/src/util \
		-I../../sg_agent/src/log4cplus \
		-I../../sg_agent/src/comm \
		-I../config \

LIB = -L./ -L../libs -levent \
	-lpthread \
	-llog4cplus \
	-lm \

VPATH = ../../../util \
		../../sg_agent \
		../clog \
		../config \
		../comm/src \
		../gen_cpp \
		../../sg_agent/src/comm \
		../thrift \
        ../thrift/async \
        ../thrift/concurrency \
        ../thrift/processor \
        ../thrift/protocol \
        ../thrift/qt \
        ../thrift/server \
        ../thrift/transport \
		../../sg_agent/src/log4cplus \

OUTPUT = sg_agent_test

OUTPUT_SO = libPHPSG_Agent.so

OUTPUT_MAC  = libPHPSG_Agent.dylib

OUTPUT_LIBA  = libPHPSG_Agent.a

OBJS    = sg_agent_config_client.o \
		sg_agent_config_processor.o \
		sg_agent_config_instance.o \
		sg_agent_fileconfig_processor.o \
		sg_agent_fileconfig_instance.o \
		php_sg_agent_config_client.o \
		Thrift.o \
		TBufferTransports.o \
		TApplicationException.o \
		TSocket.o \
		main.o \
		SGAgent.o\
		config_common_types.o\
		config_common_constants.o\
		quota_common_types.o\
		quota_common_constants.o\
		sgagent_common_types.o\
		sgagent_common_constants.o\
		sgagent_service_types.o\
		sgagent_service_constants.o\
		sgagent_worker_service_constants.o\
		sgagent_worker_service_types.o\
		aggregator_common_types.o\
		json_reader.o \
		json_value.o \
		json_writer.o \
		md5.o \
		log4cplus.o \

all:$(OUTPUT)

so:$(OUTPUT_SO)

mac:$(OUTPUT_MAC)

liba:$(OUTPUT_LIBA)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -o $@ $^

$(OUTPUT): $(OBJS)
	$(CXX) $(CFLAGS) -o $@ $^ ${LIB}

$(OUTPUT_SO): $(OBJS)
	$(CXX) $(CFLAGS) -fPIC -shared -rdynamic -o $@ $^ ${LIB}

$(OUTPUT_LIBA): $(OBJS)
	ar -r -o $@ $^

$(OUTPUT_MAC): $(OBJS)
	$(CXX) $(CFLAGS) -fPIC -dynamiclib -o $@ $^ ${LIB}


clean:
	rm -f *.o *.~ *.bak
	rm -f $(OUTPUT)
	rm -f $(OUTPUT_SO)
	rm -f $(OUTPUT_MAC)

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
