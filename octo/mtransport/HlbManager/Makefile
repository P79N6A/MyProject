CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H -v
#CFLAGS = -g -O0 -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
         -I./thrift \
		 -I./hlb_gen_cpp \
		 -I./utils \

LIB = -L./ \
	  -lssh2 \
	  -Wl,-Bstatic -llog4cplus \
	  -Wl,-Bdynamic -lcurl \
	  -Wl,-Bstatic -levent \
	  -Wl,-Bdynamic -lgcc_s -lrt \
	  -lctemplate -pthread \
      -ljsoncpp \

VPATH = ./thrift \
        ./thrift/async \
        ./thrift/concurrency \
        ./thrift/processor \
        ./thrift/protocol \
        ./thrift/qt \
        ./thrift/server \
        ./thrift/transport \
		./hlb_gen_cpp \
		./utils \

OUTPUT  = HlbManager 

OBJS    = HlbManager.o \
          HttpClient.o \
		  Config.o \
		  DyupsDataManager.o \
		  OriginalUpstreamManager.o \
		  TengineTriger.o \
		  SgAgentClient.o \
		  MNSCacheClient.o \
		  TemplateRender.o \
		  HlbPreparation.o \
		  sgagent_common_types.o \
		  sgagent_common_constants.o \
		  quota_common_types.o \
		  quota_common_constants.o \
		  config_common_types.o \
		  config_common_constants.o \
		  aggregator_common_types.o \
		  aggregator_common_constants.o \
		  sgagent_service_constants.o \
          sgagent_service_types.o \
		  SGAgent.o \
		  mnsc_data_constants.o \
		  mnsc_data_types.o \
		  mnsc_service_constants.o \
		  mnsc_service_types.o \
		  sandbox_common_constants.o \
		  sandbox_common_types.o \
		  MNSCacheService.o \
		  Thrift.o \
		  TBufferTransports.o \
          TApplicationException.o \
          TSocket.o \
		  log4cplus.o \
		  tinyxml2.o \
		  CommonTool.o \
		  

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

idl:
	rm -f ./hlb_gen_cpp/*
	thrift -r --gen cpp -out ./hlb_gen_cpp ../../common/idl/idl-sgagent/src/main/thrift/sgagent_service.thrift
	thrift -r --gen cpp -out ./hlb_gen_cpp ../../common/idl/idl-mnsc/src/main/thrift/mnsc_service.thrift	
#thrift -r --gen cpp -out ./hlb_gen_cpp ../../common/idl/idl-common/src/main/thrift/quota_common.thrift

install:
	cp -fv $(OUTPUT) ./bin/
	cp -fv $(OUTPUT) ./bin_dev/
	cp -fv $(OUTPUT) ./testCase/

ifneq ($(wildcard .depend),)
include .depend
endif
