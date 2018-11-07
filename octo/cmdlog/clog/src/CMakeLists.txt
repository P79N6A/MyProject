SET(CMAKE_CXX_FLAGS "-g -Wall -O2 -DMUDUO_STD_STRING -DHAVE_NETINET_IN_H -fPIC")

SET(SYSTEM_INCLUDE_PATH /usr/include/)
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH})
INCLUDE_DIRECTORIES(SYSTEM ${SYSTEM_INCLUDE_PATH}/thrift/)
INCLUDE_DIRECTORIES(SYSTEM ${CMAKE_SOURCE_DIR}/include)
INCLUDE_DIRECTORIES(SYSTEM ${CMAKE_CURRENT_SOURCE_DIR})
INCLUDE_DIRECTORIES(SYSTEM ${CMAKE_CURRENT_SOURCE_DIR}/../include)

SET(IPP_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/gen-cpp)
MESSAGE("----------" ${IPP_INCLUDE_DIR})
INCLUDE_DIRECTORIES(SYSTEM ${IPP_INCLUDE_DIR})

EXECUTE_PROCESS(COMMAND mkdir ${CMAKE_SOURCE_DIR}/lib)
SET(LIBRARY_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/lib)
MESSAGE(STATUS "LIBRARY_OUTPUT_PATH ${LIBRARY_OUTPUT_PATH}")


set (IPP_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/gen-cpp)
set (log4cplus_sources
        ${IPP_INCLUDE_DIR}/scribe_constants.cxx
        ${IPP_INCLUDE_DIR}/ScribeLog.cxx
        ${IPP_INCLUDE_DIR}/scribe_types.cxx
#        ${IPP_INCLUDE_DIR}/scribe_client.cxx
  appenderattachableimpl.cxx
  agentclog.cxx
  appender.cxx
  asyncappender.cxx
  clogger.cxx
  configurator.cxx
  connectorthread.cxx
  consoleappender.cxx
  cygwin-win32.cxx
  devinfohelper.cxx
  env.cxx
  factory.cxx
  fileappender.cxx
  fileinfo.cxx
  filter.cxx
  global-init.cxx
  hierarchy.cxx
  hierarchylocker.cxx
  layout.cxx
  log4judpappender.cxx
  lockfile.cxx
  log.cxx
  parse_config.cxx
  logger.cxx
  loggerimpl.cxx
  loggingevent.cxx
  loggingmacros.cxx
  loglevel.cxx
  loglog.cxx
  logloguser.cxx
  mdc.cxx
  ndc.cxx
  nullappender.cxx
  objectregistry.cxx
  patternlayout.cxx
  pointer.cxx
  property.cxx
  queue.cxx
  rootlogger.cxx
  scribe_client.cxx
  sleep.cxx
  snprintf.cxx
  socketappender.cxx
  socketbuffer.cxx
  socket.cxx
  stringhelper.cxx
  stringhelper-clocale.cxx
  stringhelper-cxxlocale.cxx
  stringhelper-iconv.cxx
  syncprims.cxx
  syslogappender.cxx
  tblog.cxx
  threads.cxx
  timehelper.cxx
  tls.cxx
  version.cxx)

#message (STATUS "Type: ${UNIX}|${CYGWIN}|${WIN32}")

if ("${UNIX}" OR "${CYGWIN}")
  set (log4cplus_sources ${log4cplus_sources}
    socket-unix.cxx)
elseif (WIN32)
  set (log4cplus_sources ${log4cplus_sources}
    nteventlogappender.cxx
    socket-win32.cxx
    win32consoleappender.cxx
    win32debugappender.cxx)

  #add_definitions (-DLOG4CPLUS_STATIC)
  #set (log4cplus_postfix "${log4cplus_postfix}S")
endif ()

if (UNICODE)
  add_definitions (-DUNICODE -D_UNICODE -UMBCS -U_MBCS)
endif (UNICODE)
if (WIN32)
  add_definitions (-DMINGW_HAS_SECURE_API=1)
  add_definitions (-D_WIN32_WINNT=${_WIN32_WINNT})

  if (BUILD_SHARED_LIBS)
    set(log4cplus_build_shared 1)
  else ()
    set(log4cplus_build_shared 0)
  endif ()
  configure_file(${CMAKE_CURRENT_SOURCE_DIR}/log4cplus.rc.in ${CMAKE_CURRENT_BINARY_DIR}/log4cplus.rc @ONLY)
  list(APPEND log4cplus_sources ${CMAKE_CURRENT_BINARY_DIR}/log4cplus.rc)
endif (WIN32)

set(log4cplus clog)
#add_library (${log4cplus} STATIC ${log4cplus_sources})
#add_library (${log4cplus} SHARED ${log4cplus_sources})

add_library (clog STATIC ${log4cplus_sources})
add_library (clog_dynamic SHARED ${log4cplus_sources})
SET_TARGET_PROPERTIES(clog_dynamic PROPERTIES OUTPUT_NAME "clog")

set (log4cplus_LIBS ${CMAKE_THREAD_LIBS_INIT})
if (LIBRT)
  list (APPEND log4cplus_LIBS ${LIBRT})
endif ()
if (LIBPOSIX4)
  list (APPEND log4cplus_LIBS ${LIBPOSIX4})
endif ()
if (LIBCPOSIX)
  list (APPEND log4cplus_LIBS ${LIBCPOSIX})
endif ()
if (LIBSOCKET)
  list (APPEND log4cplus_LIBS ${LIBSOCKET})
endif ()
if (LIBNSL)
  list (APPEND log4cplus_LIBS ${LIBNSL})
endif ()
if (LOG4CPLUS_WITH_ICONV AND LIBICONV)
  list (APPEND log4cplus_LIBS ${LIBICONV})
endif ()
#target_link_libraries (${log4cplus} -lcmtraceV2 -loctoidl -lthrift -luuid ${log4cplus_LIBS})
#target_link_libraries (${log4cplus} ${log4cplus_LIBS})

if (ANDROID)
  # Android does not seem to have SO version support.
  set_target_properties (${log4cplus} PROPERTIES
    COMPILE_FLAGS "-DINSIDE_LOG4CPLUS")
else ()
  set_target_properties (${log4cplus} PROPERTIES
    VERSION "${log4cplus_version_major}.${log4cplus_version_minor}.${log4cplus_version_patch}"
    SOVERSION "${log4cplus_soversion}"
    COMPILE_FLAGS "-DINSIDE_LOG4CPLUS")
endif ()

if (WIN32)
  set_target_properties (${log4cplus} PROPERTIES
    DEBUG_POSTFIX "D")
  target_link_libraries (${log4cplus} ws2_32 advapi32)
endif ()

###
# Installation (https://github.com/forexample/package-example)

set(config_install_dir "lib/cmake/${PROJECT_NAME}")

set(generated_dir "${CMAKE_CURRENT_BINARY_DIR}/generated")

# Configuration
set(version_config "${generated_dir}/${PROJECT_NAME}ConfigVersion.cmake")
set(project_config "${generated_dir}/${PROJECT_NAME}Config.cmake")
set(targets_export_name "${PROJECT_NAME}Targets")
set(namespace "${PROJECT_NAME}::")

include(CMakePackageConfigHelpers)

write_basic_package_version_file(
    "${version_config}"
    VERSION "${log4cplus_version_major}.${log4cplus_version_minor}.${log4cplus_version_patch}"
    COMPATIBILITY SameMajorVersion
)

# Note: variable 'targets_export_name' used
#configure_file("cmake/Config.cmake.in" "${project_config}" @ONLY)

#install(
#    FILES "${project_config}" "${version_config}"
#    DESTINATION "${config_install_dir}"
#)

install(
    EXPORT "${targets_export_name}"
    NAMESPACE "${namespace}"
    DESTINATION "${config_install_dir}"
)

#add_subdirectory(gen-cpp)
install(TARGETS ${log4cplus} EXPORT "${targets_export_name}"
                          INCLUDES DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
                          LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
                          ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
                          RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR})

install(FILES ../include/clog/log.h
			  ../include/clog/agentclog.h
		DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}/clog)
