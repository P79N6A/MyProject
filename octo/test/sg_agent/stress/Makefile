#local thrift file
BOOST_DIR = /usr/local/boost/include/boost-1_33_1/
LIB_DIR = /usr/local/lib
THRIFT_DIR = /usr/include/thrift/

CFLAGS += -g -Wno-deprecated -D HAVE_NETINET_IN_H -pthread 
LIB = -L${LIB_DIR} /root/soft/thrift-0.8.0/lib/cpp/.libs/libthrift.a -lpthread  

INCLUDE =-I./ \
		 -I${BOOST_DIR} -I${THRIFT_DIR} \
		 -I../common \
		 -I../../../common/gen_cpp \

OUTPUT  = stress

VPATH = ../common \
		../../../common/gen_cpp \

OBJS    = main.o \
		  test_sg_agent.o \
		  sgagent_types.o \
		  sgagent_constants.o \
		  sgagent_data_types.o \
		  sgagent_data_constants.o \
		  SGAgent.o \
		  MtConfig_types.o \
		  quota_data_types.o \

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
