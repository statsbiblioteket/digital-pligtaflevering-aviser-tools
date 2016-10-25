#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/

INSTALL_DIR="$HOME/7880-doms"

STORAGE_DIR="$HOME/7880-data"
DATADIR_DIR="$STORAGE_DIR/data"
CACHEDIR_DIR="$STORAGE_DIR/cache"
SBOI_SUMMA_STORAGE_DIR="$STORAGE_DIR/sboi-summaStorage"
DOMSWUI_SUMMA_STORAGE_DIR="$STORAGE_DIR/domswui-summaStorage"
mkdir -p $STORAGE_DIR
export TMPDIR=$HOME/tmp
mkdir -p $TMPDIR

installer="/target/doms-installer-*-testbed.tar.gz"

echo "Getting doms from $installer"

# Expected CWD is $HOME
tar -xzf ${installer}
# rm -r $INSTALL_DIR

# Get name of unpacked testbed dir.
installerDir=$(find * -maxdepth 0 -type d -name 'doms-installer-*' | head -1)

echo "DATA_DIR=$DATADIR_DIR" >> $installerDir/bin/setenv.sh
echo "CACHE_DIR=$CACHEDIR_DIR" >> $installerDir/bin/setenv.sh
echo "SBOI_SUMMA_STORAGE_DIR=$SBOI_SUMMA_STORAGE_DIR" >> $installerDir/bin/setenv.sh
echo "DOMSWUI_SUMMA_STORAGE_DIR=$DOMSWUI_SUMMA_STORAGE_DIR" >> $installerDir/bin/setenv.sh
echo "USE_POSTGRESQL=true" >> $installerDir/bin/setenv.sh
echo "MAILER_RECIPIENT=null@example.com" >> $installerDir/bin/setenv.sh

echo "USE_REDIS=false" >> $installerDir/bin/setenv.sh
echo "XMLTAPES_OBJECTS_JDBC=jdbc:postgresql:xmltapesObjectIndex" >> $installerDir/bin/setenv.sh 
echo "XMLTAPES_DATASTREAMS_JDBC=jdbc:postgresql:xmltapesDatastreamIndex" >> $installerDir/bin/setenv.sh
echo "XMLTAPES_DBUSER=xmltapesIndex" >> $installerDir/bin/setenv.sh
echo "XMLTAPES_DBPASS=xmltapesIndexPass" >> $installerDir/bin/setenv.sh

PGPASSWORD=xmltapesIndexPass psql -d xmltapesObjectIndex -U xmltapesIndex -h localhost -f $installerDir/extras/xmltapes-*/config/sql/postgres-index-schema.sql
PGPASSWORD=xmltapesIndexPass psql -d xmltapesDatastreamIndex -U xmltapesIndex -h localhost -f $installerDir/extras/xmltapes-*/config/sql/postgres-index-schema.sql

$installerDir/bin/install.sh $INSTALL_DIR

# rm -r $installerDir
