#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/verapdf-analyze $HOME/services/java-wrappers/verapdf-analyze $HOME/services/conf/verapdf-analyze-all.properties "$@"
