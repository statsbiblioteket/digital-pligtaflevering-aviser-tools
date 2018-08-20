#!/usr/bin/env bash

# https://gist.github.com/willurd/5720255
cd bitrepository-quickstart/
#python3 -m http.server 58709 &
ps -ef | grep -v grep | grep -q 'busybox httpd -p 58709' || nohup busybox httpd -p 58709 &

