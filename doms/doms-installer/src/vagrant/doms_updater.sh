#!/usr/bin/env bash

while true ;
do
  JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 bash -x 7880-doms/bin/doms.sh update
  sleep 180

done

