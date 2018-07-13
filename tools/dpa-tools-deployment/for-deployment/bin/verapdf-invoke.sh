#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/verapdf-invoke $HOME/services/java-wrappers/verapdf-invoke $HOME/services/conf/verapdf-invoke-all.properties "$@"
