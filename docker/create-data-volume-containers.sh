#!/usr/bin/env bash

set -e

# https://docs.docker.com/engine/tutorials/dockervolumes/#creating-and-mounting-a-data-volume-container

docker create -v /data -v /opt/activemq/data --name dpa_activemq_store busybox /bin/true
