#!/bin/bash

# $Id install.sh $
# $Author$
# $Date:   2008-08-21$
#
# Script for installing the testbed
#
# USAGE: After unpacking, edit setenv.sh to suit your needs, run
# then run this script.

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

pushd $SCRIPT_DIR > /dev/null
if [ -z "$SETENV_SOURCED" ]; then
    source setenv.sh
fi
popd > /dev/null


USE_SURVEILANCE=false
USE_LDAP=false
USE_CENTRAL=false
USE_VALIDATOR_HOOK=false
USE_NO_OBJECT_POLICY=true
USE_XMLTAPES=true



##
##  Set up the tomcat
##
echo ""
echo "TOMCAT INSTALL"
echo ""

# Do the ingest of the base objects
source $SCRIPT_DIR/install_basic_tomcat.sh

source $SCRIPT_DIR/package.sh


pushd $TESTBED_DIR > /dev/null
wget http://download.redis.io/releases/redis-2.8.5.tar.gz
tar -xvzf redis-*.tar.gz
rm redis-*.tar.gz
cd redis-*
make
src/redis-server &> redis.log &
popd > /dev/null

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
source $BASEOBJS_DIR/bin/createBasicObjects.sh
source $BASEOBJS_DIR/bin/createNewspaperObjects.sh

