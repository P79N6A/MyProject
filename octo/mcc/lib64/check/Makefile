CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I../ \
	-I../thrift \
	-I../../../common/gen_cpp/ \

LIB = -L.. \
	-levent \
	-lpthread \
	-lsg_agent \
	-lm \

VPATH = ../


OUTPUT  = sg_agent_test

OBJS    = main.o \


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
