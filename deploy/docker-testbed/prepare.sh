#!/usr/bin/env bash

set -e

# Download and unpack deployment tarball
mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.1.0:unpack \
  -Dartifact=dk.statsbiblioteket.digital_pligtaflevering_aviser.tools:dpa-tools-deployment:master-SNAPSHOT:tar.gz \
  -Dproject.basedir=. -DoutputDirectory=target/for-docker -Dsilent=true

# Create crontab file here.

# Build container

docker build -t dpa-cron .
