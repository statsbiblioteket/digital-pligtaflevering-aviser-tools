#!/bin/bash

# $Id install.sh $
# $Author$
# $Date:   2008-08-21$
#
# Script for installing the testbed
#

#
# Check whether $1 is set
#
if [ -z "$1" ] ; then
    echo "Usage: $0 <install_dir> [<data_dir>]"
    exit 1
fi

#
# Detect Java version
#
if [ -z "$JAVA_HOME" ] ; then
    JAVA_EXEC=java
else
    JAVA_EXEC="$JAVA_HOME/bin/java"
fi


#
# Set up basic variables
#
SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))
pushd $SCRIPT_DIR > /dev/null
SCRIPT_DIR=$(pwd)
popd > /dev/null
BASEDIR=$SCRIPT_DIR/..



#
# Import settings
#
pushd $SCRIPT_DIR > /dev/null
source common.sh
popd > /dev/null

parseTestbedDir "$@"

parseDataDir "$@"


pushd $SCRIPT_DIR > /dev/null
if [ -z "$SETENV_SOURCED" ]; then
    source setenv.sh
fi
popd > /dev/null


# Do the ingest of the base objects
$SCRIPT_DIR/install_basic_tomcat.sh $TESTBED_DIR

# Do the big package procedure
$SCRIPT_DIR/package.sh $TESTBED_DIR


#
# Start the tomcat server
#
echo ""
echo "Starting the tomcat server"
$TOMCAT_DIR/bin/startup.sh > /dev/null
sleepSeconds=10
echo "Sleep $sleepSeconds"
sleep $sleepSeconds


# Do the ingest of the base objects
$BASEOBJS_DIR/bin/createAll.sh -q


SBOI_SUMMARISE_SOURCE_DIR="$BASEDIR/data/sboi-summarise"
DOMSWUI_SUMMARISE_SOURCE_DIR="$BASEDIR/data/domswui-summarise"
CONFIG_TEMP_DIR=$TESTBED_DIR/tmp/config

if [ -e "$SBOI_SUMMARISE_SOURCE_DIR" ] ; then
    echo "Installing SBOI Summa"
    unzip -q "$SBOI_SUMMARISE_SOURCE_DIR/newspapr-*.zip" -d "$SBOI_SUMMARISE_DIR"

    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/data"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/data" "$SBOI_SUMMARISE_DIR/data"


    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/index"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/index" "$SBOI_SUMMARISE_DIR/index"

    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/suggest"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/suggest" "$SBOI_SUMMARISE_DIR/suggest"

    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/storage"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/storage" "$SBOI_SUMMARISE_DIR/storage"

    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/dump"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/dump" "$SBOI_SUMMARISE_DIR/dump"

    mkdir -p "$SBOI_SUMMA_STORAGE_DIR/progress"
    ln -s "$SBOI_SUMMA_STORAGE_DIR/progress" "$SBOI_SUMMARISE_DIR/progress"


    mkdir -p "$SBOI_SUMMARISE_DIR/summix-storage/"
    cp "$SBOI_SUMMARISE_SOURCE_DIR"/summix-*.zip "$SBOI_SUMMARISE_DIR/summix-storage/"
    cp "$BASEDIR/data/tomcat/"apache-tomcat-*.zip "$SBOI_SUMMARISE_DIR/"
    echo "Configuring SBOI Summa"
    sed -i -e "s/^site.portrange=[0-9]{3}$/site.portrange=$SBOI_SUMMA_PORTRANGE/" "$SBOI_SUMMARISE_DIR/site.properties"
    sed -i -e 's|^\(\s*<value class="string">\)\(progress_in[^<]*\.xml\)\(</value>\s*\)$|\1'$SBOI_SUMMARISE_DIR'\/progress\/\2\3|' $SBOI_SUMMARISE_DIR/config/in*.xml

    cp -v "$CONFIG_TEMP_DIR/storage_newspapr.xml" "$SBOI_SUMMARISE_DIR/config/storage_newspapr.xml"
    $SBOI_SUMMARISE_DIR/bin/setup.sh
    $SBOI_SUMMARISE_DIR/bin/deploy.sh
fi

if [ -e "$DOMSWUI_SUMMARISE_SOURCE_DIR" ] ; then
    echo "Installing DOMSWUI Summa"
    unzip -q "$DOMSWUI_SUMMARISE_SOURCE_DIR/domswui-*.zip" -d "$DOMSWUI_SUMMARISE_DIR"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/data"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/data" "$DOMSWUI_SUMMARISE_DIR/data"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/index"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/index" "$DOMSWUI_SUMMARISE_DIR/index"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/suggest"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/suggest" "$DOMSWUI_SUMMARISE_DIR/suggest"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/storage"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/storage" "$DOMSWUI_SUMMARISE_DIR/storage"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/dump"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/dump" "$DOMSWUI_SUMMARISE_DIR/dump"

    mkdir -p "$DOMSWUI_SUMMA_STORAGE_DIR/progress"
    ln -s "$DOMSWUI_SUMMA_STORAGE_DIR/progress" "$DOMSWUI_SUMMARISE_DIR/progress"


    mkdir -p "$DOMSWUI_SUMMARISE_DIR/summix-storage/"
    cp "$DOMSWUI_SUMMARISE_SOURCE_DIR"/summix-*.zip "$DOMSWUI_SUMMARISE_DIR/summix-storage/"
    cp "$BASEDIR/data/tomcat/"apache-tomcat-*.zip "$DOMSWUI_SUMMARISE_DIR/"
    echo "Configuring DOMSWUI Summa"
    sed -i -e "s/^site.portrange=[0-9]{3}$/site.portrange=$DOMSWUI_SUMMA_PORTRANGE/" "$DOMSWUI_SUMMARISE_DIR/site.properties"
    sed -i -e 's|^\(\s*<value class="string">\)\(progress_in[^<]*\.xml\)\(</value>\s*\)$|\1'$DOMSWUI_SUMMARISE_DIR'\/progress\/\2\3|' $DOMSWUI_SUMMARISE_DIR/config/in*.xml

    cp -v "$CONFIG_TEMP_DIR/storage_domswui.xml" "$DOMSWUI_SUMMARISE_DIR/config/storage_domswui.xml"
    $DOMSWUI_SUMMARISE_DIR/bin/setup.sh
    $DOMSWUI_SUMMARISE_DIR/bin/deploy.sh

fi


BIN_DIR="$TESTBED_DIR/bin"
echo "Creating control script in $BIN_DIR"
mkdir -p "$BIN_DIR"
cp "$SCRIPT_DIR"/doms.sh "$BIN_DIR/"

$BIN_DIR/doms.sh start_summa
$BIN_DIR/doms.sh update

echo "Install complete"
