#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-check-regenerated-checksumfile.sh $HOME/services/java-wrappers/dpa-check-regenerated-checksumfile $HOME/services/conf/check-regenerated-checksumfile.properties "$@"
