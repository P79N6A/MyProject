# FindMafka
# --------
#
# Find Mafka
#
# Find the Mafka Client includes and library.  Once done this will define
#
#   MAFAK_INCLUDE_DIR      - where to find muduo include, etc.
#   MAFAK_LIBRARIE         - List of libraries when using muduo_http.
#   MAFAK_FOUND             - True if muduo found.
#
set(MAFKA_INCLUDE_DIR ${MAFKA_MODULE_PATH}/include/)
set(MAFKA_LIBRARY ${MAFKA_MODULE_PATH}/lib/libmafka_client.a)


#message(${MAFKA_INCLUDE_DIRS})
find_path(MAFKA_INCLUDE_DIR NAMES mafka)
find_library(MAFKA_LIBRARY NAMES mafka)

mark_as_advanced(MAFKA_LIBRARY MAFKA_INCLUDE_DIR)

# handle the QUIETLY and REQUIRED arguments and set MAFKA_FOUND to TRUE if
# all listed variables are TRUE
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Mafka REQUIRED_VARS MAFKA_LIBRARY MAFKA_INCLUDE_DIR)

set(MAFKA_FOUND TRUE)
if(MAFKA_FOUND)
    set(MAFKA_INCLUDE_DIR ${MAFKA_INCLUDE_DIR})
    set(MAFKA_LIBRARY ${MAFKA_LIBRARY})
endif()

