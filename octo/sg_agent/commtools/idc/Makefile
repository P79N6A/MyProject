CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
		-I../../src \
		-I../../src/util \
		-I../../src/comm \
		-I../../src/log4cplus \
		-I../../../common/cpp/lib/thrift-0.8.0_build/include/thrift \
		-I../../gen-cpp \

LIB = -L./ -L../libs -L../../../common/cpp/lib/thrift-0.8.0_build/lib\
	-levent \
	-lpthread \
	-llog4cplus \
	-lthrift \
	-lm \

VPATH = ../../src/util \
		../../src \
		../../src/comm \
		../../src/log4cplus \
		../../../src/log4cplus \

OUTPUT  = lib/libocto-idc.a

OBJS    = region_router.o \
		regions.o \
		log4cplus.o \
		ip_router.o \
	 	tinyxml2.o \


all:$(OUTPUT)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -o $@ $^

$(OUTPUT): $(OBJS)
	ar -r -o $@ $^

clean:
	rm -f *.o *.~ *.bak

dep:
	mkdep $(INCLUDE) $(SOURCES) > .depend

ifneq ($(wildcard .depend),)
include .depend
endif
