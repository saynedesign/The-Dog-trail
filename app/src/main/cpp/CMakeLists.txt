cmake_minimum_required(VERSION 3.22.1)
project("insight_engine")

add_library(
        insight_engine
        SHARED
        insight_engine.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        insight_engine
        ${log-lib}
)
