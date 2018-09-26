#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/manualQA-completer $HOME/services/java-wrappers/manualQA-completer $HOME/services/conf/manualQA-completer.properties "$@"
