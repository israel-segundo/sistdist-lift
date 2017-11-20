@echo off

set ant_cmd="%ANT_HOME%\bin\ant"

del build\jar\lift.jar

%ant_cmd% clean compile jar
