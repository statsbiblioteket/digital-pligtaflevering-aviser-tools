#!/usr/bin/env bash

set -e

cd $HOME/logs
$HOME/services/java-wrappers/list-deletable-deliveries $HOME/services/conf/list-deletable-deliveries.properties "$@"

