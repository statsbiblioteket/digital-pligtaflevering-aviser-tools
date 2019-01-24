#!/bin/sh

set -e

cd $HOME/logs
flock -n ~/var/locks/pdf-content $HOME/services/java-wrappers/pdfcontent-invoke $HOME/services/conf/pdf-content.properties "$@"
