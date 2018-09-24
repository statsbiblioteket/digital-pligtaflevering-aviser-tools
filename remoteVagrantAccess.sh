#!/usr/bin/env bash

SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))

set -x
set -e

cd ${SCRIPT_DIR}
host=${1:-pc596}


JAVA7="env JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/"
JAVA8="env JAVA_HOME=/usr/lib/jvm/zulu-8-amd64"


#Port forwarding
# http://linuxcommand.org/lc3_man_pages/ssh1.html
FEDORA_PORT=7880
SBOI_PORT=58608
BITREPO_PORT=58709
ACTIVEMQ_PORT=61616
VERAPDF_PORT=8090
POSTGRES_PORT=5432
ZOOKEEPER_PORT=2121
ssh -A -L"$FEDORA_PORT:localhost:$FEDORA_PORT" -L"$SBOI_PORT:localhost:$SBOI_PORT" -L"$BITREPO_PORT:localhost:$BITREPO_PORT" -L"$ACTIVEMQ_PORT:localhost:$ACTIVEMQ_PORT" -L"$VERAPDF_PORT:localhost:$VERAPDF_PORT" "$host"  <<-EOF
set -e
set -x
cd $SCRIPT_DIR;
while true; do
	date;
	vagrant ssh -c '${JAVA7} ~/7880-doms/bin/doms.sh update';
	sleep 45;
done
EOF
