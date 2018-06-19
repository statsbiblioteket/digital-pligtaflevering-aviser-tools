#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/dpa-ingest-deliveries $HOME/services/java-wrappers/dpa-ingester $HOME/services/conf/ingest-deliveries.properties "$@"
