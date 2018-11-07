#!/bin/sh

# base command , you can add arguments for different environment, such as "-P offline"

echo $CURRENT_ENV
if [[ $CURRENT_ENV == "online" ]]; then
    echo "use online"
	mvn clean -U package -P online
else
    echo "use offline"
	mvn clean -U package -P offline
fi
