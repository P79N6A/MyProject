#!/bin/sh

rm -rf ./deploy
./activator clean dist
cp ./target/*.jar -d ./deploy
cd deploy