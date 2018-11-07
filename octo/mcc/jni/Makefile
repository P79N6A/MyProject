CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
		-I../../.. \
		-I../.. \
		-I../thrift \
		-I../thrift/transport \
        -I../thrift/async \
        -I../thrift/concurrency \
        -I../thrift/processor \
        -I../thrift/protocol \
        -I../thrift/qt \
        -I../thrift/server \
		-I../../common/gen_cpp \
		-I../../sg_agent/comm/ \
		-I../../sg_agent/util/ \
		-I../../sg_agent/ \
		-I../comm \
		-I../config \
		-I../clog \
		-I../util \
		-I../cplus \
		-I/usr/local/java/include \
		-I/usr/local/java/include/linux \
		-I/System/Library/Frameworks/JavaVM.framework/Headers/ \

LIB = -L./ \
	-lpthread \
	-lm \

VPATH = ../../sg_agent/util \
		../../sg_agent/comm \
		../../common/gen_cpp \
		../thrift \
        ../thrift/async \
        ../thrift/concurrency \
        ../thrift/processor \
        ../thrift/protocol \
        ../thrift/qt \
        ../thrift/server \
        ../thrift/transport \
		../comm \
		../config \
		../clog \
		../util \
		../cplus \

CXXFLAG += -g -fPIC


OUTPUT = libSG_Agent.so

OUTPUT_MAC  = libSG_Agent.dylib

OBJS    = com_sankuai_meituan_config_client_SGAgent.o \
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
		SGAgent.o \
		quota_data_types.o \
		sgagent_data_types.o \
		log.o \
		call_back.o \
		md5.o \

all:$(OUTPUT)

mac:$(OUTPUT_MAC)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -o $@ $^

$(OUTPUT): $(OBJS)
	$(CXX) $(CFLAGS) -fPIC -shared -o $@ $^ ${LIB}

$(OUTPUT_MAC): $(OBJS)
	$(CXX) $(CFLAGS) -fPIC -dynamiclib -o $@ $^ ${LIB}


clean:
	rm -f *.o *.~ *.bak
	rm -f $(OUTPUT)
	rm -f $(OUTPUT_MAC)

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
