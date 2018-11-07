echo "rm cmake cache begin..."
rm ./cmake_install.cmake
rm -r ./CMakeFiles
rm ./Makefile
rm ./CMakeCache.txt
cd test/ && ./clean.sh
echo "rm cmake cache end..."
