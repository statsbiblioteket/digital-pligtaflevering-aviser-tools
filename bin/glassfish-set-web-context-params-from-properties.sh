#!/usr/bin/env bash

# Expects properties on standard in, and asadmin in $PATH.  Optional argument is web app name in glassfish

WEBAPP=${1:-dpa-dashboard-cdi-master-SNAPSHOT}

while IFS='=' read -r key value
do
    if [ -n "$key" ]
    then
# bin/asadmin set-web-context-param --name=doms.username --value=DOMSUSERNAME dpa-dashboard-cdi-master-SNAPSHOT
       echo "asadmin set-web-context-param --name=$key --value='$value' $WEBAPP"
    fi
done
