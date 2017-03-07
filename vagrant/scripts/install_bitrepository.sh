#!/usr/bin/env bash

set -e

curl -L -O http://archive.apache.org/dist/activemq/5.14.3/apache-activemq-5.14.3-bin.tar.gz
tar xvzf apache-activemq-5.14.3-bin.tar.gz

apache-activemq-5.14.3/bin/activemq start # goes in background

# OpenJDK 8 is not available on Ubuntu 14.04 through official repositories.
# Download one from Azul instead.
# (when download link breaks, look into migrating to Zulu repository http://repos.azulsystems.com)

curl -L -O http://cdn.azul.com/zulu/bin/zulu8.19.0.1-jdk8.0.112-linux_x64.tar.gz
tar xvzf zulu8.19.0.1-jdk8.0.112-linux_x64.tar.gz

# unpack bitrepository quickstart prepared by maven earlier
tar xvzf /target/artifacts-copied/bitrepository-integration-quickstart.tar.gz

# fix collection name, file exchance settings and allow for "/" in file identifiers.
patch -p1 -l -d bitrepository-quickstart < /vagrant/bitrepository-quickstart.diff

export JAVA_HOME=$HOME/zulu8.19.0.1-jdk8.0.112-linux_x64
bitrepository-quickstart/setup.sh

