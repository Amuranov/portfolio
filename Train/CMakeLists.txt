cmake_minimum_required(VERSION 3.10)
project(Train)

set(CMAKE_CXX_STANDARD 11)

include_directories(engine)
include_directories(solver)

add_executable(Train
        engine/graph.cpp
        engine/graph.hpp
        engine/main.cpp
        engine/parser.cpp
        engine/parser.hpp
        engine/tool.cpp
        engine/tool.hpp
        maps/be.txt
        maps/cycle.txt
        maps/slow-fast.txt
        solver/Global.hpp
        solver/Heap.hpp
        solver/Solver.cpp
        solver/Solver.hpp
        solver/SolverTypes.hpp
        solver/Sort.hpp
        solver/VarOrder.hpp)
