#!/bin/bash

ISILON=/net/zone1.isilon.sblokalnet/ifs/archive/sbftp-home/infomed
DELIVERIES=$HOME/deliveries

DELIVERY=$1

SOURCE=$ISILON/$DELIVERY
TARGET=$DELIVERIES/$DELIVERY

if [ ! -d "$SOURCE" ]; then
  echo "not found: $SOURCE"
  exit 1
fi

if [  -h "$TARGET" ]; then
  echo "already linked: $TARGET"
  exit 1
fi

if [ ! -d "$DELIVERIES" ]; then
  echo "creating $DELIVERIES"
  mkdir -p "$DELIVERIES"
fi

ln -sv "$SOURCE" "$TARGET"
exit 0

