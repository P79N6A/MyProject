CFLAGS = -g -O0 -v -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
		 -I../ \
         -I../thrift \
		 -I../hlb_gen_cpp \
		 -I../utils \

LIB = -L./ \
      -lssh2 \
	  -Wl,-Bstatic -llog4cplus \
	  -Wl,-Bdynamic -lcurl \
	  -Wl,-Bstatic -levent \
	  -Wl,-Bdynamic -lgcc_s -lrt \
	  -lctemplate -pthread \
	  -ljsoncpp

VPATH = ../thrift \
        ../thrift/async \
        ../thrift/concurrency \
        ../thrift/processor \
        ../thrift/protocol \
        ../thrift/qt \
        ../thrift/server \
        ../thrift/transport \
		../hlb_gen_cpp \
		../utils \
		../ \
		./ \

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -o $@ $^

ThriftBaseOBJS = sgagent_common_types.o \
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


TemplateRenderOBJS    = TemplateRenderTest.o \
						TemplateRender.o \
						log4cplus.o \

TemplateRenderOUTPUT  = TemplateRenderTest
$(TemplateRenderOUTPUT): $(TemplateRenderOBJS) $(ThriftBaseOBJS)
	$(CXX) $(CFLAGS) -o $@ $^ ${LIB}


SgAgentClientOBJS  = SgAgentClientTest.o \
					 SgAgentClient.o \
					 Config.o \
					 log4cplus.o \
					 tinyxml2.o \
					 CommonTool.o \

SgAgentClientOUTPUT = SgAgentClientTest 
$(SgAgentClientOUTPUT): $(SgAgentClientOBJS) $(ThriftBaseOBJS)
	    $(CXX) $(CFLAGS) -o $@ $^ ${LIB}


MNSCacheClientOBJS = MNSCacheClientTest.o \
					 MNSCacheClient.o \
					 SgAgentClient.o \
					 Config.o \
					 log4cplus.o \
					 tinyxml2.o \
					 CommonTool.o \

MNSCacheClientOUTPUT = MNSCacheClientTest
$(MNSCacheClientOUTPUT): $(MNSCacheClientOBJS) $(ThriftBaseOBJS)
	    $(CXX) $(CFLAGS) -o $@ $^ ${LIB}


DyupsDataManagerOBJS  = DyupsDataManagerTest.o \
                     DyupsDataManager.o \
					 TengineTriger.o \
                     SgAgentClient.o \
                     Config.o \
                     log4cplus.o \
                     tinyxml2.o \
                     CommonTool.o \
					 HttpClient.o \

DyupsDataManagerOUTPUT = DyupsDataManagerTest
$(DyupsDataManagerOUTPUT): $(DyupsDataManagerOBJS) $(ThriftBaseOBJS)
	    $(CXX) $(CFLAGS) -o $@ $^ ${LIB}


OriginalUpstreamManagerOBJS = OriginalUpstreamManagerTest.o \
                     OriginalUpstreamManager.o \
                     TengineTriger.o \
					 MNSCacheClient.o \
                     SgAgentClient.o \
                     Config.o \
                     log4cplus.o \
                     tinyxml2.o \
                     CommonTool.o \
                     HttpClient.o \

OriginalUpstreamManagerOUTPUT = OriginalUpstreamManagerTest
$(OriginalUpstreamManagerOUTPUT): $(OriginalUpstreamManagerOBJS) $(ThriftBaseOBJS)
	    $(CXX) $(CFLAGS) -o $@ $^ ${LIB}


TengineTrigerTestOBJS  = TengineTrigerTest.o\
                     TengineTriger.o \
                     SgAgentClient.o \
                     Config.o \
                     Thrift.o \
                     log4cplus.o \
                     tinyxml2.o \
                     CommonTool.o \
                     HttpClient.o \

TengineTrigerTestOUTPUT = TengineTrigerTest
$(TengineTrigerTestOUTPUT): $(TengineTrigerTestOBJS)
	    $(CXX) $(CFLAGS) -o $@ $^ ${LIB}


all:$(DyupsDataManagerOUTPUT)

clean:
	rm -f *.o *.~ *.bak
	rm -f $(TemplateRenderOUTPUT) $(SgAgentClientOUTPUT) $(DyupsDataManagerOUTPUT) $(TengineTrigerTestOUTPUT) \
		$(OriginalUpstreamManagerOUTPUT) $(MNSCacheClientOUTPUT) 
	rm -f HlbManager.debug* HlbManager.info* HlbManager.error* HlbManager.warn* HlbManager.stat* 

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
