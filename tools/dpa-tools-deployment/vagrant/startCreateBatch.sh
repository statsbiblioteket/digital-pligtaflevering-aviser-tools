#!/usr/bin/env bash

while true ;
do
  ./bin/dpa-create-delivery properties/create-delivery.properties autonomous.agent=register-batch-trigger iterator.filesystem.batches.folder=/delivery-samples
  sleep 60 #3600
#    echo "dummy"
#    sleep 1
done





