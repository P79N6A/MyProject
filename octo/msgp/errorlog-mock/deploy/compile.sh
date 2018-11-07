#!/bin/sh

# base command , you can add arguments for different environment, such as "-P offline"

if [[ $CURRENT_ENV == "online" ]]; then
	mvn clean -U package -P online
else
	mvn clean -U package -P offline
fi
