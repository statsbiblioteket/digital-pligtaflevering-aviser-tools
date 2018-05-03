#!/usr/bin/env bash

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-ingest-deliveries bash -c "$HOME/services/java-wrappers/dpa-ingester $HOME/services/conf/ingest-deliveries.properties "$@" ; sleep 1200"
