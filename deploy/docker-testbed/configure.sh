#!/bin/sh

# Adapted from Mirzam 2018-06-19.

sed 's/#.*//' | grep = | \
while read line ; do
    name=${line%%=*}
    value=${line#*=}
# -- no xmlstarlet in image yet, and we do not run these.
#    for context in ~/services/tomcat-apps/*.xml ; do
#        xmlstarlet ed \
#                   --inplace \
#                   --update '/Context/Parameter[@name="'$name'"]/@value' \
#                   --value "$value" $context
#    done
    for prop in ~/services/conf/*.properties ; do
        sed -i "s!^$name=.*!$name=$value!" $prop
    done
done

