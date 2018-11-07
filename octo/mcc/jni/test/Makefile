CFLAGS = -g -Wno-deprecated -DHAVE_NETINET_IN_H

INCLUDE =-I./ \
		-I../ \

LIB = -L./ \
	-lpthread \
	-lm \

VPATH = ./ \
		../ \

CXXFLAG += -g -fPIC


OUTPUT = test

OBJS    = call_back.o \
		main.o \

all:$(OUTPUT)

.SUFFIXES: .o .cpp
.cpp.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -c $^

.o:
	$(CXX) $(CFLAGS) $(INCLUDE) -fPIC -o $@ $^

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
