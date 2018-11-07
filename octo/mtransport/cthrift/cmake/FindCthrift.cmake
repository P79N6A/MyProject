# FindCthrift
# --------
#
# Find cthrift
#
# Find the cthrift includes and library.  Once done this will define
#
#   CTHRIFT_INCLUDE_DIR      - where to find cthrift include, etc.
#   CTHRIFT_LIBRARY    - List of libraries when using cthrift_base.
#   CTHRIFT_FOUND             - True if cthrift found.
#
set(CTHRIFT_INCLUDE_DIR ${CTHRIFT_SRC_PATH}/cthrift)
set(CTHRIFT_LIBRARY ${LIBRARY_OUTPUT_PATH}/libcthrift.a)

find_path(CTHRIFT_INCLUDE_DIR NAMES cthrift_include)
find_library(CTHRIFT_LIBRARY NAMES cthrift_lib)

mark_as_advanced(CTHRIFT_LIBRARY CTHRIFT_INCLUDE_DIR)

# handle the QUIETLY and REQUIRED arguments and set CTHRIFT_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Cthrift REQUIRED_VARS CTHRIFT_LIBRARY CTHRIFT_INCLUDE_DIR)

if(CTHRIFT_FOUND)
    set(CTHRIFT_INCLUDE_DIR ${CTHRIFT_INCLUDE_DIR})
    set(CTHRIFT_LIBRARY ${CTHRIFT_LIBRARY})
endif()

