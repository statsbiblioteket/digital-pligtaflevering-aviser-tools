#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-generate-statistics $HOME/services/java-wrappers/dpa-generate-statistics $HOME/services/conf/generate-statistics.properties "$@"
