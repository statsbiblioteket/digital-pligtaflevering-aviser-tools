#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/approve-cleaner $HOME/services/java-wrappers/approve-cleaner $HOME/services/conf/approve-cleaner.properties "$@"
