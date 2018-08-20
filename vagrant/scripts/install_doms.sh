#!/bin/bash

# set -e # not tested yet

[ -e "$HOME/doms_installed" ] && exit 0
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

installer="/target/artifacts-copied/doms-installer-testbed.tar.gz"

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

# We need minor adjustments of some configuration files (http://stackoverflow.com/a/30614728/53897)
# 1) change from newspaper to DPA collection
# xmlstarlet ed -P --inplace --update '//entry[key/text()="collectionPID"]/value' -v doms:DPA_Collection $installerDir/data/templates/storage_newspapr.xml.template

# 2) make "doms.sh update" react quicker!
xmlstarlet ed -P --inplace --update '//Parameter[@name="fedora.updatetracker.delay"]/@value' -v 10000 $installerDir/data/templates/context.xml.default.template
xmlstarlet ed -P --inplace --update '//Parameter[@name="fedora.updatetracker.period"]/@value' -v 2000 $installerDir/data/templates/context.xml.default.template
# done


$installerDir/bin/install.sh $INSTALL_DIR

# rm -r $installerDir
touch "$HOME/doms_installed"