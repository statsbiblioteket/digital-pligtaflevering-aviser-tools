#!/bin/sh

cd /tmp

sleep 10

# Note - if server is unresponsive from outside it may have crashed and need to be restarted.
/usr/lib/jvm/zulu-8-amd64/bin/java -Ddw.server.applicationConnectors[0].port=8090 -Ddw.server.adminConnectors[0].port=8091 -jar /target/artifacts-copied/verapdf-rest.jar server > verapdf-rest.log 2> verapdf-rest.err &