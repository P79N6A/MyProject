CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
		-I../../ \
		-I.. \
		-I./clog \
		-I./comm \
		-I./util \
		-I./thrift \
		-I./thrift/transport \
        -I./thrift/async \
        -I./thrift/concurrency \
        -I./thrift/processor \
        -I./thrift/protocol \
        -I./thrift/qt \
        -I./thrift/server \
		-I../common/gen_cpp \
		-I../../util \
		-I../sg_agent \
		-I../sg_agent/util \
		-I../sg_agent/comm \
		-I./config \
		-I./cplus \
		-I/usr/local/java/include \
		-I/usr/local/java/include/linux \

LIB = -L./ -levent \
	-lpthread \
	-lm \

VPATH = ../../util \
		./clog \
		./config \
		./cplus \
		../common/gen_cpp \
		../sg_agent/comm/ \
		./thrift \
        ./thrift/async \
        ./thrift/concurrency \
        ./thrift/processor \
        ./thrift/protocol \
        ./thrift/qt \
        ./thrift/server \
        ./thrift/transport \


OUTPUT  = sg_agent_conf

OBJS    = sg_agent_config_client.o \
		sg_agent_config_client.o \
		MtConfig_types.o \
		sg_agent_config_processor.o \
		sg_agent_config_instance.o \
		sg_agent_fileconfig_processor.o \
		sg_agent_fileconfig_instance.o \
		file_config_client.o \
		Thrift.o \
		TBufferTransports.o \
		TApplicationException.o \
		TSocket.o \
		main.o \
		SGAgent.o \
		quota_data_types.o \
		sgagent_data_types.o \
		log.o \
		md5.o \


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

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
