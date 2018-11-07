#!/bin/sh

# base command , you can add arguments for different environment, such as "-P offline"

if [[ $CURRENT_ENV == "offline" ]]; then
	mvn clean -U package
else
	mvn clean -U package
fi
