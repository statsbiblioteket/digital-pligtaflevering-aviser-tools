#!/usr/bin/env bash

#!/bin/sh
SERVICE_NAME=undefined
PATH_TO_JAR=undefined
PID_PATH_NAME=undefined

case $1 in
    createDelivery)
        SERVICE_NAME=createTheDelivery
        PATH_TO_JAR=/usr/local/MyProject/MyJar.jar
        PID_PATH_NAME=/tmp/createTheDelivery-pid
    ;;
    process2)
        SERVICE_NAME=process2
        PATH_TO_JAR=/usr/local/MyProject/MyJar.jar
        PID_PATH_NAME=/tmp/process2-pid
    ;;
    process3)
        SERVICE_NAME=process3
        PATH_TO_JAR=/usr/local/MyProject/MyJar.jar
        PID_PATH_NAME=/tmp/process3-pid
    ;;
esac


case $2 in
    start)
        echo "Starting $2 ..."
        if [ ! -f $PID_PATH_NAME ]; then
            /home/vagrant/dpa/startCreateBatch.sh & echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            /home/vagrant/dpa/startCreateBatch.sh & echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
