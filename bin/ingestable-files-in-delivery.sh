#!/usr/bin/env bash

if [ -z "$1" ]
then
    echo "usage: $0 delivery-directory"
    exit 1
fi

# This is a naive test - just so the output ends being "dl_XXXXX_rtX/..."
if [ -n "$(echo $1 | tr -c -d /)" ]
then
   echo "error: delivery directory '$1' contains a slash - outside current directory?"
   exit 1
fi

find $1 -type f | grep -E -v '/(transfer_acknowledged|transfer_complete|checksums.txt)$'|LC_ALL=C sort
