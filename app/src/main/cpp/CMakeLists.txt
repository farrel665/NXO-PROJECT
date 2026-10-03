cmake_minimum_required(VERSION 3.22.1)
project(imgui_overlay)

set(IMGUI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/imgui)
set(IMGUI_VERSION 1.92.2b)
set(IMGUI_TARBALL ${CMAKE_CURRENT_BINARY_DIR}/imgui.tar.gz)

if(NOT EXISTS ${IMGUI_DIR}/imgui.cpp)
    file(MAKE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party)
    message(STATUS "Downloading Dear ImGui ${IMGUI_VERSION}...")
    file(DOWNLOAD
        https://github.com/ocornut/imgui/archive/refs/tags/v${IMGUI_VERSION}.tar.gz
        ${IMGUI_TARBALL}
        SHOW_PROGRESS
        TLS_VERIFY ON
    )
    execute_process(COMMAND ${CMAKE_COMMAND} -E tar xzf ${IMGUI_TARBALL} WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR} RESULT_VARIABLE EXTRACT_RESULT)
    if(NOT EXTRACT_RESULT EQUAL 0)
        message(FATAL_ERROR "Failed to extract Dear ImGui")
    endif()
    file(RENAME ${CMAKE_CURRENT_BINARY_DIR}/imgui-${IMGUI_VERSION} ${IMGUI_DIR})
endif()

add_library(imgui_overlay SHARED native-lib.cpp
    ${IMGUI_DIR}/imgui.cpp
    ${IMGUI_DIR}/imgui_draw.cpp
    ${IMGUI_DIR}/imgui_tables.cpp
    ${IMGUI_DIR}/imgui_widgets.cpp
    ${IMGUI_DIR}/backends/imgui_impl_opengl3.cpp
)

target_include_directories(imgui_overlay PRIVATE ${IMGUI_DIR} ${IMGUI_DIR}/backends)
target_compile_definitions(imgui_overlay PRIVATE IMGUI_IMPL_OPENGL_ES3=1)
find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv3)
target_link_libraries(imgui_overlay ${log-lib} ${android-lib} ${gles-lib})
