# FindRapidjson
# --------
#
# Find rapidjson
#
# Find the rapidjson includes and library.  Once done this will define
#
#   RAPIDJSON_INCLUDE_DIRS      - where to find rapidjson include, etc.
#   RAPIDJSON_FOUND             - True if rapidjson found.
#
set(RAPIDJSON_INCLUDE_DIR ${COMMON_LIB_PATH}/rapidjson/include)

#message(${RAPIDJSON_INCLUDE_DIRS})
find_path(RAPIDJSON_INCLUDE_DIR NAMES rapidjson)

mark_as_advanced(RAPIDJSON_INCLUDE_DIR)

# handle the QUIETLY and REQUIRED arguments and set RAPIDJSON_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(rapidjson REQUIRED_VARS RAPIDJSON_INCLUDE_DIR)

if(RAPIDJSON_FOUND)
  set(RAPIDJSON_INCLUDE_DIRS ${RAPIDJSON_INCLUDE_DIR})
endif()
