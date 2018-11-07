#!/bin/sh

# base command , you can add arguments for different environment, such as "-P offline"

if [[ $CURRENT_ENV == "offline" ]]; then
#	gradle clean build --refresh-dependencies -Penv=offline
	mvn clean -U package -P offline -Dmaven.test.skip=true
else
	mvn clean -U package -P online -Dmaven.test.skip=true
fi
