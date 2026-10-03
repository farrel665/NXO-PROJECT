cmake_minimum_required(VERSION 3.22.1)
project(imgui_overlay)
set(CMAKE_CXX_STANDARD 17)
set(IMGUI ${CMAKE_CURRENT_SOURCE_DIR}/third_party/imgui)

add_library(imgui_overlay SHARED native-lib.cpp
    ${IMGUI}/imgui.cpp ${IMGUI}/imgui_draw.cpp ${IMGUI}/imgui_tables.cpp ${IMGUI}/imgui_widgets.cpp
    ${IMGUI}/backends/imgui_impl_opengl3.cpp)

target_include_directories(imgui_overlay PRIVATE ${IMGUI} ${IMGUI}/backends)
find_library(log-lib log)
find_library(android-lib android)
find_library(egl-lib EGL)
find_library(gles-lib GLESv3)
target_link_libraries(imgui_overlay ${log-lib} ${android-lib} ${egl-lib} ${gles-lib})
