cmake_minimum_required(VERSION 3.8)
project(Boost_Client_Final)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        include/connectionHandler.h
        src/connectionHandler.cpp
        src/echoClient.cpp)

add_executable(Boost_Client_Final ${SOURCE_FILES})