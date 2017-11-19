#!/bin/bash

LIFT_HOME=/d/repositories/sistdist-lift/lift


if [ "${CLASSPATH}" == "" ]; then
    CLASSPATH="."
fi

CLASSPATH="${CLASSPATH}:/c/Users/vsegundo/Desktop/gson-2.6.2.jar"
CLASSPATH="${CLASSPATH}:${LIFT_HOME}/build/classes/com/lift/client/*"
CLASSPATH="${CLASSPATH}:${LIFT_HOME}/build/classes/com/lift/common/*"
CLASSPATH="${CLASSPATH}:${LIFT_HOME}/build/classes/com/lift/daemon/*"

export CLASSPATH
echo $CLASSPATH

java -cp "{CLASSPATH}" com/lift/client/ClientLauncher
