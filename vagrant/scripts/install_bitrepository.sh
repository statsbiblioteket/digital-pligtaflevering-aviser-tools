#!/usr/bin/env bash

[ -e "$HOME/bitrepo_installed" ] && exit 0

curl -L -O http://archive.apache.org/dist/activemq/5.14.3/apache-activemq-5.14.3-bin.tar.gz
tar xvzf apache-activemq-5.14.3-bin.tar.gz

apache-activemq-5.14.3/bin/activemq start # goes in background

# unpack bitrepository quickstart prepared by maven earlier
tar xvzf /target/artifacts-copied/bitrepository-integration-quickstart.tar.gz

# fix collection name, file exchance settings and allow for "/" in file identifiers.
patch -p1 -l -d bitrepository-quickstart < /vagrant/bitrepository-quickstart.diff

# Use Zulu Java 8 for bitrepository
export JAVA_HOME=/usr/lib/jvm/zulu-8-amd64
bitrepository-quickstart/setup.sh

touch "$HOME/bitrepo_installed"