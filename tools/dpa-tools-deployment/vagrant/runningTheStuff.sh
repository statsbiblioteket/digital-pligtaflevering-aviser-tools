#!/usr/bin/env bash


/home/vagrant/dpa/filebeat-5.0.0-linux-x86_64/startFilebeat.sh & disown

(cd logs; ./startCreateBatch.sh)
