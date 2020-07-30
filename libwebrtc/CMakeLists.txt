# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -pedantic")

include_directories(${PROJECT_SOURCE_DIR}/include)

add_definitions(-DFIXED_POINT -DUSE_KISS_FFT -DHAVE_CONFIG_H)

add_library( # Sets the name of the library.
        WRtcAudio

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        # Associated headers in the same location as their source
        # file are automatically included.
        src/main/cpp/web_rtc.cpp

        src/main/cpp/ns/noise_suppression.c
        src/main/cpp/ns/fft4g.c
        src/main/cpp/ns/ns_core.c
        src/main/cpp/ns/dot_product_with_scale.cc
        src/main/cpp/ns/checks.cc
        )


# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        WRtcAudio

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
