# FindPlugin
# --------
#
# Find plugin
#
# Find the plugin includes and library.  Once done this will define
#
#   PLUGIN_INCLUDE_DIR      - where to find plugin include, etc.
#   PLUGIN_LIBRARY    - List of libraries when using plugin_base.
#   PLUGIN_FOUND             - True if plugin found.
#
set(PLUGIN_INCLUDE_DIR ${PLUGIN_SRC_PATH}/plugin)
set(PLUGIN_LIBRARY ${LIBRARY_OUTPUT_PATH}/libplugin.a)

find_path(PLUGIN_INCLUDE_DIR NAMES plugin_include)
find_library(PLUGIN_LIBRARY NAMES plugin_lib)

mark_as_advanced(PLUGIN_LIBRARY PLUGIN_INCLUDE_DIR)

# handle the QUIETLY and REQUIRED arguments and set PLUGIN_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Plugin REQUIRED_VARS PLUGIN_LIBRARY PLUGIN_INCLUDE_DIR)

if(PLUGIN_FOUND)
    set(PLUGIN_INCLUDE_DIR ${PLUGIN_INCLUDE_DIR})
    MESSAGE(STATUS "PLUGIN_INCLUDE_DIR ${PLUGIN_INCLUDE_DIR}")
    set(PLUGIN_LIBRARY ${PLUGIN_LIBRARY})
    MESSAGE(STATUS "PLUGIN_LIBRARY ${PLUGIN_LIBRARY}")
endif()