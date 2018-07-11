#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/newspaper-weekdays-analyze $HOME/services/java-wrappers/newspaper-weekdays-analyze $HOME/services/conf/newspaper-weekdays-analyze.properties "$@"
