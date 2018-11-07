CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

#INCLUDE =-I./ \
#		-I../../../../thrift \
#		-I../../../../util \
#		-I../../../../mtconfig \
#		-I../../config \

INCLUDE = -I../\
		  -I../../config

LIB = -L./ -L../../libs\
	-lPHPSG_Agent \
	-levent -lthriftnb -lthrift\
	-llog4cplus \
#	-lpthread \
#	-lm \

VPATH = ./ \


OUTPUT  = sg_agent_conf

OBJS    = main.o


all:$(OUTPUT)

liba:$(OUTPUT)

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
