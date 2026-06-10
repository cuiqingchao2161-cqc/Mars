cmake_minimum_required(VERSION 3.10.2)

project(qylog_sample_native)

add_library(qylog-sample SHARED native-lib.cpp)

find_library(log-lib log)

set(MARSXLOG_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmarsxlog.so")
if(EXISTS "${MARSXLOG_LIB}")
    add_library(marsxlog SHARED IMPORTED)
    set_target_properties(marsxlog PROPERTIES IMPORTED_LOCATION "${MARSXLOG_LIB}")
    target_link_libraries(qylog-sample ${log-lib} marsxlog)
else()
    message(WARNING "libmarsxlog.so not found at ${MARSXLOG_LIB}; building sample native module without linking marsxlog")
    target_link_libraries(qylog-sample ${log-lib})
endif()
