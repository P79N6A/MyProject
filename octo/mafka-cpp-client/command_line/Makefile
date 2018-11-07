####################################################
# Generic makefile for NuclearMQ Transfer
####################################################
#
TARGET := cpp-client
LIBDIR := ../ ../dependencies/librdkafka/src
LIBS := pthread thrift mafka 
#LIBS := qpid-proton pthread 
INCLUDES := ../src/
SRCDIR := ./
#
# # Now alter any implicit rules' variables if you like, e.g.:

CC:=g++
CFLAGS := -g -Wall -Wno-deprecated -O3
CPPFLAGS := $(CFLAGS)
CPPFLAGS += $(addprefix -I,$(INCLUDES))
CPPFLAGS += -MMD
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

all: $(TARGET)

deps : $(DEPS)

objs : $(OBJS)

clean :
	@$(RM-F) $(OBJS)
	@$(RM-F) $(DEPS)
	@$(RM-F) $(TARGET)

rebuild: all
ifneq ($(MISSING_DEPS),)
$(MISSING_DEPS) :
	@$(RM-F) $(patsubst %.d,%.o,$@)
endif
-include $(DEPS)
$(TARGET) : $(OBJS)
	$(CC) -rdynamic -o $(TARGET) $(OBJS) $(addprefix -L,$(LIBDIR)) $(addprefix -l,$(LIBS)) 

info:
	@echo $(SRCS)
	@echo $(OBJS)
	@echo $(DEPS)
	@echo $(MISSING_DEPS)
	@echo $(MISSING_DEPS_SOURCES)
