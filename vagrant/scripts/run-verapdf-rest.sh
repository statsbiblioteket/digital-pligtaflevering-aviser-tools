#!/bin/sh

cd /tmp

command="$JAVA_HOME/bin/java -Ddw.server.applicationConnectors[0].port=8090 -Ddw.server.adminConnectors[0].port=8091 -jar /target/artifacts-copied/verapdf-rest.jar server"

# Note - if server is unresponsive from outside it may have crashed and need to be restarted.
ps -ef | grep -v grep | grep "$command" || nohup ${command} > $HOME/verapdf-rest.log 2> $HOME/verapdf-rest.err &
