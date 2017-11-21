#!/bin/bash

java -cp .:./lib/*:${LIFT_HOME}/build/jar/lift.jar com.lift.client.ClientLauncher "$1" "$2"
