@echo off

set arg1=%1
set arg2=%2

set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;./lib/*;./build/jar/lift.jar

set java_cmd="%JAVA_HOME%\bin\java"

%java_cmd% -Xms128m -Xmx384m -Xnoclassgc com.lift.daemon.Daemon
