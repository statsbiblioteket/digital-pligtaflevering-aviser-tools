#!/bin/sh

# Copied from Mirzam 2018-06-19.

while read line ; do
    name=${line%%=*}
    value=${line#*=}
    for context in ~/services/tomcat-apps/*.xml ; do
        xmlstarlet ed \
                   --inplace \
                   --update '/Context/Parameter[@name="'$name'"]/@value' \
                   --value "$value" $context
    done
    for prop in ~/services/conf/*.properties ; do
        sed -i "s/^$name=.*/$name=${value//\//\\/}/" $prop
    done
done < <(sed 's/#.*//' < ~/config | grep =)
