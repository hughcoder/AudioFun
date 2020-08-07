cmake_minimum_required(VERSION 3.4.1)

file(GLOB SRC_FILES
        */*.cc
        */*/*.c
        */*/*.cc
        */*/*/*/*.c
        agc-lib.cpp
        )

add_library(legacy_agc-lib SHARED ${SRC_FILES})
include_directories(./)
add_definitions(
        -DWEBRTC_ANDROID
        -DWEBRTC_POSIX
)

find_library(log-lib log)

target_link_libraries(legacy_agc-lib ${log-lib})