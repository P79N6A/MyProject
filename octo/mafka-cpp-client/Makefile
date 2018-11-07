####################################################
# Generic makefile 
####################################################
#
TARGET := libmafka

LIB_DIR = ./dependencies/cthrift/cthrift_v2_build/lib
INCLUDE_DIR = ./dependencies/cthrift/cthrift_v2_build/include
CTHRIFT_INC = ${INCLUDE_DIR}/cthrift
BOOST_INC = ${INCLUDE_DIR}/boost
RAPIDJSON_INC = ${INCLUDE_DIR}/rapidjson
THRIFT08_INC = ${INCLUDE_DIR}/thrift
THRIFT_INC = ${INCLUDE_DIR}/thrift/thrift
SGAGENT_INC = ${INCLUDE_DIR}/octoidl
MUDUO_INC = ${INCLUDE_DIR}/muduo

LIBDIR := ./dependencies/librdkafka/src/ ${LIB_DIR}
LIBS := pthread cthrift rdkafka muduo_base muduo_http muduo_net thrift thriftnb octoidl uuid event rt z

#LIBS := qpid-proton pthread 
INCLUDES :=  ./dependencies/librdkafka/src/ ./src/ ${INCLUDE_DIR} \
            ${CTHRIFT_INC} ${BOOST_INC} ${RAPIDJSON_INC} ${THRIFT08_INC} ${THRIFT_INC} ${SGAGENT_INC} ${MUDUO_INC}
SRCDIR := ./src ./src/castle/gen-cpp/ ./src/sg_agent/gen-cpp
#
# # Now alter any implicit rules' variables if you like, e.g.:

CC:=g++
CFLAGS := -g -Wall -Wno-deprecated -fPIC -DHAVE_NETINET_IN_H -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -DMUDUO_LOG
CPPFLAGS := $(CFLAGS)
CPPFLAGS += $(addprefix -I,$(INCLUDES))
CPPFLAGS += -MMD

AR := ar 
#
# # The next bit checks to see whether rm is in your djgpp bin
# # directory; if not it uses del instead, but this can cause (harmless)
# # `File not found' error messages. If you are not using DOS at all,
# # set the variable to something which will unquestioningly remove
# # files.
#

RM-F := rm -f


# # You shouldn't need to change anything below this point.
#
SRCS := $(wildcard $(addsuffix /*.cpp, $(SRCDIR)))
OBJS := $(patsubst %.cpp,%.o,$(SRCS))
DEPS := $(patsubst %.o,%.d,$(OBJS))
MISSING_DEPS := $(filter-out $(wildcard $(DEPS)),$(DEPS))
MISSING_DEPS_SOURCES := $(wildcard $(patsubst %.d,%.cpp,$(MISSING_DEPS)))


.PHONY : all deps objs clean rebuild info

all: $(TARGET).so $(TARGET).a

deps : $(DEPS)

objs : $(OBJS)

clean :
	@$(RM-F) $(OBJS)
	@$(RM-F) $(DEPS)
	@$(RM-F) $(TARGET).so
	@$(RM-F) $(TARGET).a

rebuild: all
ifneq ($(MISSING_DEPS),)
$(MISSING_DEPS) :
	@$(RM-F) $(patsubst %.d,%.o,$@)
endif
-include $(DEPS)
$(TARGET).so : $(OBJS)
	$(CC) -rdynamic -shared -o $(TARGET).so $(OBJS) $(addprefix -L,$(LIBDIR)) $(addprefix -l,$(LIBS)) 


define BUILD_LIBRARY 
$(if $(wildcard $@),@$(RM) $@) 
$(if $(wildcard ar.mac),@$(RM) ar.mac) 
$(if $(filter %.a, $^), 
@echo CREATE $@ > ar.mac 
@echo SAVE >> ar.mac 
@echo END >> ar.mac 
@$(AR) -M < ar.mac 
) 
$(if $(filter %.o,$^),@$(AR) -q $@ $(filter %.o, $^)) 
$(if $(filter %.a, $^), 
@echo OPEN $@ > ar.mac 
$(foreach LIB, $(filter %.a, $^), 
@echo ADDLIB $(LIB) >> ar.mac 
) 
@echo SAVE >> ar.mac 
@echo END >> ar.mac 
@$(AR) -M < ar.mac 
@$(RM) ar.mac 
) 
endef 

$(TARGET).a : $(OBJS) ./dependencies/librdkafka/src/librdkafka.a
	$(BUILD_LIBRARY)

info:
	@echo $(SRCS)
	@echo $(OBJS)
	@echo $(DEPS)
	@echo $(MISSING_DEPS)
	@echo $(MISSING_DEPS_SOURCES)
