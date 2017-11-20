#!/bin/bash

java -cp .:./lib/*:/home/vsegundo/sistdist-lift/lift/build/jar/lift.jar com.lift.client.ClientLauncher "$1" "$2"
