#!/bin/sh

set -e

cd $HOME/logs
$HOME/services/java-wrappers/set-event-on-uuid $HOME/services/conf/set-event-on-uuid.properties "$@"

