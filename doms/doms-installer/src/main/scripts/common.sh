#!/bin/bash

#
# Check for install-folder and potentially create it.
#
function parseTestbedDir(){
    if [ -z "$TESTBED_DIR" ]; then
        TESTBED_DIR=$1
        if [ -z "$TESTBED_DIR" ]; then
            echo "install-dir not specified. Bailing out." 1>&2
            usage
        fi
        if [ -d $TESTBED_DIR ]; then
            echo ""
        else
            mkdir -p $TESTBED_DIR
        fi
        pushd $TESTBED_DIR > /dev/null
        TESTBED_DIR=$(pwd)
        popd > /dev/null
    fi
}

#
# Check for install-folder and potentially create it.
#
function parseDataDir(){
    DATA_DIR=$2
    if [ -d $DATA_DIR ]; then
        echo ""
    else
        mkdir -p $DATA_DIR
    fi
    pushd $DATA_DIR > /dev/null
    DATA_DIR=$(pwd)
    popd > /dev/null
}
