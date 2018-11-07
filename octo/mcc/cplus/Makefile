CFLAGS = -g -fPIC -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I../ \
		-I./include \
		-I../../../ \
		-I../.. \
		-I../clog \
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
		-I../../../util \
		-I../../sg_agent/src \
		-I../../sg_agent/src/util \
		-I../../sg_agent/src/comm \
		-I../../sg_agent/src/log4cplus \
		-I../config \
		-I../cplus \
		-I/usr/include/octoidl \

LIB = -L./ \
	-levent \
	-lpthread \
	-lm \
	-loctoidl

VPATH = ../../../util \
		./src \
		../config \
		../cplus \
		../comm/src \
		../../sg_agent/comm/ \
		../thrift \
        ../thrift/async \
        ../thrift/concurrency \
        ../thrift/processor \
        ../thrift/protocol \
        ../thrift/qt \
        ../thrift/server \
        ../thrift/transport \
		../../sg_agent/src/log4cplus \
		../../sg_agent/src/comm \
		../../sg_agent/src \

OUTDIR = lib
OUTPUTSO  = $(OUTDIR)/liboctomcc.so
OUTPUTA  = $(OUTDIR)/liboctomcc.a

OBJS    = sg_agent_config_client.o \
		sg_agent_config_client.o \
		sg_agent_config_processor.o \
		sg_agent_config_instance.o \
		sg_agent_fileconfig_processor.o \
		sg_agent_fileconfig_instance.o \
		sg_agent_fileconfig_instance.o \
		file_config_client.o \
		kv_config_client.o \
		Thrift.o \
		TBufferTransports.o \
		TApplicationException.o \
		TSocket.o \
		json_reader.o \
		json_value.o \
		json_writer.o \
		log4cplus.o \
		md5.o \


all:$(OUTPUTSO) $(OUTPUTA)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -o $@ $^

$(OUTPUTSO): $(OBJS)
	$(CXX) -shared -o $@ $^

$(OUTPUTA): $(OBJS)
	ar -r -o $@ $^

clean:
	rm -f *.o *.~ *.bak
	rm -f $(OUTPUTSO)
	rm -f $(OUTPUTA)

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
