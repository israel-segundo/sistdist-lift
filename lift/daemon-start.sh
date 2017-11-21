#!/bin/bash

java -cp .:./lib/*:${LIFT_HOME}/build/jar/lift.jar com.lift.daemon.Daemon
