rd /s /q "build"
cmake -B build -G "Visual Studio 18 2026" -A x64
cmake --build build --config Release