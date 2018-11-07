# FindLibCurl
# --------
#
# Find libcurl
#
# Find the libcurl includes and library.  Once done this will define
#
#   LIBCURL_INCLUDE_DIR      - where to find libcurl include, etc.
#   LIBCURL_LIBRARY    - List of libraries when using libcurl.
#   LIBCURL_FOUND             - True if libcurl found.
#
SET(THIRD_RESOURCE_DIRECTORY ../common/cpp/lib/)
SET(THIRD_CURL_RESOURCE_DIRECTORY ${THIRD_RESOURCE_DIRECTORY}/curl-7_42_0/)

set(LIBCURL_INCLUDE_DIRS  ${THIRD_CURL_RESOURCE_DIRECTORY}/include/curl/)
set(LIBCURL_LIBRARYS ${THIRD_CURL_RESOURCE_DIRECTORY}/lib/.libs/)
#find_path(LIBCURL_INCLUDE_DIR NAMES curl)
#find_path(LIBCURL_LIBRARY NAMES libcurl.a)

find_path(LIBCURL_INCLUDE_DIR_X NAMES  curl.h  PATHS ${LIBCURL_INCLUDE_DIRS})
find_library(LIBCURL_LIBRARY NAMES libcurl.a  PATHS ${LIBCURL_LIBRARYS})


if(LIBCURL_LIBRARY  STREQUAL  "LIBCURL_LIBRARY-NOTFOUND")
  EXECUTE_PROCESS(COMMAND sh run.sh)

  find_path(LIBCURL_INCLUDE_DIR_X NAMES  curl.h  PATHS ${LIBCURL_INCLUDE_DIRS})
  find_library(LIBCURL_LIBRARY NAMES libcurl.a  PATHS ${LIBCURL_LIBRARYS})
endif()

# handle the QUIETLY and REQUIRED arguments and set LIBCURL_FOUND to TRUE if
# all listed variables are TRUE
mark_as_advanced(LIBCURL_LIBRARY LIBCURL_INCLUDE_DIR_X)


include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(LIBCURL REQUIRED_VARS LIBCURL_INCLUDE_DIR_X LIBCURL_LIBRARY)
if(LIBCURL_FOUND)
  set(LIBCURL_INCLUDE_DIR ${LIBCURL_INCLUDE_DIR_X})
  set(LIBCURL_LIBRARY ${LIBCURL_LIBRARY})
endif()