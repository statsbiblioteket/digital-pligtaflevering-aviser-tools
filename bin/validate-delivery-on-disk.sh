#!/usr/bin/env bash

# Validate that the checksums.txt file contains the exact same lines as what md5sum generate for delivery (except meta-files)
# Those not duplicated are printed.   Use "-v" to show directory names as they are processed.

if [ "$1" == "-v" ]
then
  VERBOSE=1
  shift
fi

if [ -z "$1" ]
then
  echo "usage:  delivery_dir [delivery_dir..]"
  exit 1
fi

for i in $*
do
  if [ -n "$VERBOSE" ]
  then
     echo $i
  fi
  (cd $i; find . -type f -print |  grep -E -v '^(\.|\./transfer_acknowledged[^ ]*|\./transfer_complete|.\/checksums.txt)$' | cut -c 3- |xargs md5sum \
   ; tr -d '\r' < checksums.txt \
  ) | LC_ALL=C sort | uniq -u
done
