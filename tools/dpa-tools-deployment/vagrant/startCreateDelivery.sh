#!/usr/bin/env bash

#This script is designed to run the javaprocess for dpa-create-delivery once every one minute

while true ;
do
  ../bin/dpa-create-delivery ../properties/create-delivery.properties autonomous.agent=register-batch-trigger iterator.filesystem.batches.folder=/delivery-samples
  sleep 60 #3600
done





