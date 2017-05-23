#!/usr/bin/env bash

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-create-deliveries $HOME/services/java-wrappers/dpa-create-delivery $HOME/services/conf/create-deliveries.properties iterator.filesystem.batches.folder=$HOME/deliveries "$@"
