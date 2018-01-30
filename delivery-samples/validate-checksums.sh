#!/usr/bin/env bash

# Validates md5sums in deliveries.
#
# sed is necessary to skip trailing \r as files are generated under Windows.
#
for i in dl_*
do
  (cd $i; echo "*** $(pwd) ***" ; sed 's/\r//g' checksums.txt |md5sum -c - --quiet)
done
