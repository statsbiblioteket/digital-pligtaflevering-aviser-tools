#!/usr/bin/env bash

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-regenerate-checksumfile.sh $HOME/services/java-wrappers/dpa-regenerate-checksumfile $HOME/services/conf/regenerate-checksumfile.properties "$@"
