#!/usr/bin/env bash

set -e

# using target/ similar to Maven.
rm -rf target/

# Download an unpack launcher artifact to get bitrepository settings


# Download and unpack deployment tarball
mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.1.0:unpack \
  -Dartifact=dk.statsbiblioteket.digital_pligtaflevering_aviser.tools:dpa-tools-deployment:master-SNAPSHOT:tar.gz \
  -Dproject.basedir=. -DoutputDirectory=target/for-docker -Dsilent=true \
&& mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.1.0:unpack  \
  -Dartifact=dk.statsbiblioteket.digital_pligtaflevering_aviser.tools:dpa-tools-ide-launchers:master-SNAPSHOT:jar \
  -Dproject.basedir=. -DoutputDirectory=target/for-docker/bitrepository-settings/ -Dsilent=true \
&& du -sk target/for-docker


