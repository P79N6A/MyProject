# FindCmtrace
# --------
#
# Find Cmtracet
#
# Find the Cmtrace includes and library.  Once done this will define
#
#   CMTRACE_INCLUDE_DIRS      - where to find cmtrace include, etc.
#   CMTRACE_FOUND             - True if cmtrace found.
#
set(CMTRACE_INCLUDE_DIR ${CMTRACE_MODULE_PATH}/)

#message(${CMTRACE_INCLUDE_DIR})
find_path(CMTRACE_INCLUDE_DIR NAMES cmtraceV2)

MESSAGE("-------------" ${CMTRACE_INCLUDE_DIR})

mark_as_advanced(cmtrace_INCLUDE_DIR)

# handle the QUIETLY and REQUIRED arguments and set CMTRACE_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Cmtrace REQUIRED_VARS CMTRACE_INCLUDE_DIR)

if(CMTRACE_FOUND)
	set(CMTRACE_INCLUDE_DIRS ${CMTRACE_INCLUDE_DIR})
endif()

set(CMTRACE_LIBRARY ${SGAGENT_MODULE_PATH}/cmtraceV2/lib/libcmtraceV2.a)
