cmake_minimum_required(VERSION 3.4.1)

#add_library( # Sets the name of the library.
#        native-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        src/main/cpp/native-lib.cpp )

include_directories ("src/main/cpp/include")
add_library( # Sets the name of the library.
        soundtouch-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/soundtouch-jni.cpp
        src/main/cpp/SoundTouch/AAFilter.cpp
        src/main/cpp/SoundTouch/BPMDetect.cpp
        src/main/cpp/SoundTouch/cpu_detect_x86.cpp
        src/main/cpp/SoundTouch/FIFOSampleBuffer.cpp
        src/main/cpp/SoundTouch/FIRFilter.cpp
        src/main/cpp/SoundTouch/InterpolateCubic.cpp
        src/main/cpp/SoundTouch/InterpolateLinear.cpp
        src/main/cpp/SoundTouch/InterpolateShannon.cpp
        src/main/cpp/SoundTouch/mmx_optimized.cpp
        src/main/cpp/SoundTouch/PeakFinder.cpp
        src/main/cpp/SoundTouch/RateTransposer.cpp
        src/main/cpp/SoundTouch/SoundTouch.cpp
        src/main/cpp/SoundTouch/sse_optimized.cpp
        src/main/cpp/SoundTouch/TDStretch.cpp
        src/main/cpp/SoundStretch/WavFile.cpp
        )


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

#target_link_libraries( # Specifies the target library.
#        native-lib
#
#        # Links the target library to the log library
#        # included in the NDK.
#        ${log-lib} )

target_link_libraries( # Specifies the target library.
        soundtouch-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )