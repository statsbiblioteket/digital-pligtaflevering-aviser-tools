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

if [ "$USE_VALIDATOR_HOOK" == "true" ]; then
    MODIFY_OBJECT_HOOK='<param name="decorator3" value="dk.statsbiblioteket.doms.ecm.fedoravalidatorhook.FedoraModifyObjectHook"/>'
fi

if [ "$USE_POSTGRESQL" = "true" ]; then
  DATABASE_SYSTEM="localPostgreSQLPool"
else
  DATABASE_SYSTEM="localDerbyPool"
fi

if [ "$USE_UPDATETRACKER" == "true" ]; then
    UPDATETRACKER_HOOK='    <param name="decorator5" value="dk.statsbiblioteket.doms.updatetracker.DomsUpdateTrackerHook"/>     <param name="updateTrackerPoolName" value="'$DATABASE_SYSTEM'">            <comment>The storage pool used by the update tracker</comment>        </param>'
fi

if [ "$USE_MPTSTORE" = "true" ]; then
  TRIPLESTORE_SYSTEM=localPostgresMPTTriplestore
else
  TRIPLESTORE_SYSTEM=localMulgaraTriplestore
fi

if [ "$USE_REDIS" = "true" ]; then
  DATASTREAM_INDEX=redisDatastreamIndex
  OBJECT_INDEX=redisObjectIndex
else
  DATASTREAM_INDEX=postgresDatastreamIndex
  OBJECT_INDEX=postgresObjectIndex
fi



function replace(){
sed \
-e 's|\$LOG_DIR\$|'"$LOG_DIR"'|g' \
-e 's|\$TOMCAT_DIR\$|'"$TOMCAT_DIR"'|g' \
-e 's|\$FEDORA_DIR\$|'"$FEDORA_DIR"'|g' \
-e 's|\$DATA_DIR\$|'"$DATA_DIR"'|g' \
-e 's|\$CACHE_DIR\$|'"$CACHE_DIR"'|g' \
-e 's|\$SCHEMA_DIR\$|'"$SCHEMA_DIR"'|g' \
-e 's|\$TOMCAT_CONFIG_DIR\$|'"$TOMCAT_CONFIG_DIR"'|g' \
-e 's|\$WEBAPPS_DIR\$|'"$WEBAPPS_DIR"'|g' \
-e 's|\$PORTRANGE\$|'"$PORTRANGE"'|g' \
-e 's|\$SBOI_SUMMA_PORTRANGE\$|'"$SBOI_SUMMA_PORTRANGE"'|g' \
-e 's|\$DOMSWUI_SUMMA_PORTRANGE\$|'"$DOMSWUI_SUMMA_PORTRANGE"'|g' \
-e 's|\$TOMCAT_SERVERNAME\$|'"$TOMCAT_SERVERNAME"'|g' \
-e 's|\$FEDORAADMIN\$|'"$FEDORAADMIN"'|g' \
-e 's|\$FEDORAADMINPASS\$|'"$FEDORAADMINPASS"'|g' \
-e 's|\$FEDORAUSER\$|'"$FEDORAUSER"'|g' \
-e 's|\$FEDORAUSERPASS\$|'"$FEDORAUSERPASS"'|g' \
-e 's|\$POSTGRESQL_DB\$|'"$POSTGRESQL_DB"'|g' \
-e 's|\$POSTGRESQL_SERVER\$|'"$POSTGRESQL_SERVER"'|g' \
-e 's|\$POSTGRESQL_USER\$|'"$POSTGRESQL_USER"'|g' \
-e 's|\$POSTGRESQL_PASS\$|'"$POSTGRESQL_PASS"'|g' \
-e 's|\$UPDATETRACKER_POSTGRESQL_DB\$|'"$UPDATETRACKER_POSTGRESQL_DB"'|g' \
-e 's|\$UPDATETRACKER_POSTGRESQL_SERVER\$|'"$UPDATETRACKER_POSTGRESQL_SERVER"'|g' \
-e 's|\$UPDATETRACKER_POSTGRESQL_USER\$|'"$UPDATETRACKER_POSTGRESQL_USER"'|g' \
-e 's|\$UPDATETRACKER_POSTGRESQL_PASS\$|'"$UPDATETRACKER_POSTGRESQL_PASS"'|g' \
-e 's|\$DATABASE_SYSTEM\$|'"$DATABASE_SYSTEM"'|g' \
-e 's|\$MPTSTORE_DB\$|'"$MPTSTORE_DB"'|g' \
-e 's|\$MPTSTORE_SERVER\$|'"$MPTSTORE_SERVER"'|g' \
-e 's|\$MPTSTORE_USER\$|'"$MPTSTORE_USER"'|g' \
-e 's|\$MPTSTORE_PASS\$|'"$MPTSTORE_PASS"'|g' \
-e 's|\$TRIPLESTORE_SYSTEM\$|'"$TRIPLESTORE_SYSTEM"'|g' \
-e 's|\$REDIS_HOST\$|'"$REDIS_HOST"'|g' \
-e 's|\$REDIS_PORT\$|'"$REDIS_PORT"'|g' \
-e 's|\$REDIS_DATABASE\$|'"$REDIS_DATABASE"'|g' \
-e 's|\$MODIFY_OBJECT_HOOK\$|'"$MODIFY_OBJECT_HOOK"'|g' \
-e 's|\$UPDATETRACKER_HOOK\$|'"$UPDATETRACKER_HOOK"'|g' \
-e 's|\$MAILER_SENDER\$|'"$MAILER_SENDER"'|g' \
-e 's|\$MAILER_RECIPIENT\$|'"$MAILER_RECIPIENT"'|g' \
-e 's|\$XMLTAPES_DATASTREAMS_JDBC\$|'"$XMLTAPES_DATASTREAMS_JDBC"'|g' \
-e 's|\$XMLTAPES_OBJECTS_JDBC\$|'"$XMLTAPES_OBJECTS_JDBC"'|g' \
-e 's|\$XMLTAPES_DBUSER\$|'"$XMLTAPES_DBUSER"'|g' \
-e 's|\$XMLTAPES_DBPASS\$|'"$XMLTAPES_DBPASS"'|g' \
-e 's|\$DATASTREAM_INDEX\$|'"$DATASTREAM_INDEX"'|g' \
-e 's|\$OBJECT_INDEX\$|'"$OBJECT_INDEX"'|g' \
<$1 > $2
}

CONFIG_TEMP_DIR=$TESTBED_DIR/tmp/config
mkdir -p $CONFIG_TEMP_DIR



#
# Configuring all the doms config files
#
echo "Creating config files from conf.sh"
for file in $BASEDIR/data/templates/*.template ; do
  newfile1=`basename $file`;
  newfile2=$CONFIG_TEMP_DIR/${newfile1%.template};
  replace $file $newfile2
  echo "Created config file $newfile2 from template file $file"
done



##
##  Set up the tomcat
##
echo ""
echo "TOMCAT INSTALL"
echo ""

echo "Configuring the tomcat"
mkdir -p $TOMCAT_CONFIG_DIR/

# Replace the tomcat server.xml with our server.xml
mkdir -p $TOMCAT_DIR/conf
cp -v $CONFIG_TEMP_DIR/server.xml $TOMCAT_DIR/conf/server.xml



# Insert tomcat setenv.sh
mkdir -p $TOMCAT_DIR/bin/
cp -v $CONFIG_TEMP_DIR/setenv.sh $TOMCAT_CONFIG_DIR/setenv.sh
rm $TOMCAT_DIR/bin/setenv.sh
ln -s $TOMCAT_CONFIG_DIR/setenv.sh $TOMCAT_DIR/bin/setenv.sh
chmod +x $TOMCAT_DIR/bin/*.sh




# Install context.xml configuration
cp -v $CONFIG_TEMP_DIR/context.xml.default $TOMCAT_CONFIG_DIR/tomcat-context-params.xml

# Install schemaStore "webservice" configuration
mkdir $TOMCAT_APPS_DIR
cp -v $CONFIG_TEMP_DIR/schemaStore.xml $TOMCAT_APPS_DIR/schemaStore.xml

# Set the session timeout to 1 min
mkdir -p $TOMCAT_DIR/conf
cp -v $CONFIG_TEMP_DIR/web.xml $TOMCAT_DIR/conf/web.xml


#if we used the odd Maintenance tomcat setup, symlink stuff together again
if [ ! $TOMCAT_CONFIG_DIR -ef $TOMCAT_DIR/conf ]; then

   #first, link to context.xml into the correct location
   mkdir -p $TOMCAT_DIR/conf/Catalina/localhost
   ln -s $TOMCAT_CONFIG_DIR/tomcat-context-params.xml $TOMCAT_DIR/conf/Catalina/localhost/context.xml.default

   ln -s $TOMCAT_APPS_DIR/schemaStore.xml $TOMCAT_DIR/conf/Catalina/localhost/schemaStore.xml
fi

echo "Tomcat setup is now done"
## Tomcat is now done

echo "Installing docs"
mkdir -p $DOCS_DIR
cp -r $BASEDIR/docs/* $DOCS_DIR
echo "Docs installed"



echo ""
echo "WEBSERVICE INSTALL"
echo ""
##
## Install the doms webservices
##
echo "Installing the doms webservices into tomcat"
mkdir -p $WEBAPPS_DIR

if [ "$USE_LDAP" == "true" ]; then
    for file in $BASEDIR/webservices/authchecker-service-*.war ; do
         newname=`basename $file`
         newname=`expr match "$newname" '\([^0-9]*\)'`;
         newname=${newname%-}.war;
         cp -v $file $WEBAPPS_DIR/$newname
         cp -v $CONFIG_TEMP_DIR/log4j.authchecker.xml $TOMCAT_CONFIG_DIR

    done
fi

if [ "$USE_CENTRAL" == "true" ]; then
    for file in $BASEDIR/webservices/centralWebservice-service-*.war ; do
         newname=`basename $file`
         newname=`expr match "$newname" '\([^0-9]*\)'`;
         newname=${newname%-}.war;
         cp -v $file $WEBAPPS_DIR/$newname
         cp -v $CONFIG_TEMP_DIR/log4j.centralDomsWebservice.xml $TOMCAT_CONFIG_DIR
    done
fi

if [ "$USE_SURVEILANCE" == "true" ]; then
    for file in $BASEDIR/webservices/surveillance-*.war ; do
         newname=`basename $file`
         newname=`expr match "$newname" '\([^0-9]*\)'`;
         newname=${newname%-}.war;
         cp -v $file $WEBAPPS_DIR/$newname
    done
    cp -v $CONFIG_TEMP_DIR/log4j.surveillance-*.xml $TOMCAT_CONFIG_DIR
fi

for file in $BASEDIR/webservices/pidgenerator-service-*.war ; do
     newname=`basename $file`
     newname=`expr match "$newname" '\([^0-9]*\)'`;
     newname=${newname%-}.war;
     cp -v $file $WEBAPPS_DIR/$newname
     cp -v $CONFIG_TEMP_DIR/log4j.pidgenerator.xml $TOMCAT_CONFIG_DIR
done
chmod 644 $WEBAPPS_DIR/*.war


cp -v $CONFIG_TEMP_DIR/updatetracker.hibernate.*.xml $TOMCAT_CONFIG_DIR


##
## Install Fedora
##
echo ""
echo "INSTALLING FEDORA"
echo ""

echo "Configuring fedora preinstall"

# Install Fedora
echo "Installing Fedora"
pushd $BASEDIR/data/fedora > /dev/null
java -jar $FEDORAJAR $CONFIG_TEMP_DIR/fedora.properties
popd > /dev/null

# Deploy stuff from fedoralib
echo "Repacking Fedora war files with changes"
pushd $FEDORA_DIR/install > /dev/null
unzip -q fedora.war -d fedorawar
cd fedorawar
mkdir -p WEB-INF/lib

# The mulgara timeout config
cp -v $CONFIG_TEMP_DIR/mulgara-x-config.xml WEB-INF/classes/

# The fedora libs
USE_CENTRAL=true

rm WEB-INF/lib/mulgara-core-*.jar
for file in $(find "$BASEDIR/fedoralib/mulgara/" -type f ); do
    cp "$file" WEB-INF/lib
done

if [ "$USE_VALIDATOR_HOOK" == "true" ]; then
    for file in $(find "$BASEDIR/fedoralib/validatorhook/" -type f ); do
       cp "$file" WEB-INF/lib
    done
fi

if [ "$USE_NO_OBJECT_POLICY" == "true" ]; then
    cp -r -v "$BASEDIR/fedoralib/fedoraSBadditions/"* WEB-INF/classes/
fi

if [ "$USE_LDAP" == "true" ]; then
    for file in $(find "$BASEDIR/fedoralib/fedoralogin/" -type f ); do
       cp "$file" WEB-INF/lib
    done
fi


if [ "$USE_XMLTAPES" == "true" ]; then
    for file in $(find "$BASEDIR/fedoralib/xmltapes/" -type f ); do
       cp "$file" WEB-INF/lib
    done
fi

if [ "$USE_UPDATETRACKER" == "true" ]; then
    for file in $(find "$BASEDIR/fedoralib/updateTracker/" -type f ); do
       cp "$file" WEB-INF/lib
    done
fi


# Utils is not optional at this time
for file in $(find "$BASEDIR/fedoralib/utils/" -type f ); do
   cp "$file" WEB-INF/lib
done


if [ "$USE_SURVEILANCE" == "true" ]; then
    #Update the web.xml
    FEDORAWEBXML=`mktemp`
    sed '/<\/web-app>/d' < WEB-INF/web.xml > $FEDORAWEBXML
    cat $CONFIG_TEMP_DIR/fedoraWebXmlInsert.xml >> $FEDORAWEBXML
    echo "</web-app>" >> $FEDORAWEBXML
    cp $FEDORAWEBXML WEB-INF/web.xml
    rm $FEDORAWEBXML
fi

#repackage
mv ../fedora.war ../fedora_original.war
zip -rq ../fedora.war *    > /dev/null
popd > /dev/null



echo "Install fedora.war into tomcat"
mkdir -p $WEBAPPS_DIR
cp -v $FEDORA_DIR/install/fedora.war $WEBAPPS_DIR

echo "Configuring fedora postinstall"

# Add logappender to Fedora logback configuration
if [ "$USE_SURVEILANCE" == "true" ]; then
    cp -v $CONFIG_TEMP_DIR/logback.xml $FEDORA_DIR/server/config/logback.xml
else
    cp -v $CONFIG_TEMP_DIR/logback_unsurveyed.xml $FEDORA_DIR/server/config/logback.xml
fi
cp -v $CONFIG_TEMP_DIR/logback_rebuild.xml $FEDORA_DIR/server/bin/logback.xml


# Add logappender to Fedora logback configuration
cp -v $CONFIG_TEMP_DIR/fedora.fcfg  $FEDORA_DIR/server/config/fedora.fcfg

# Install custom policies
mkdir -p $FEDORA_DIR/fedora-xacml-policies/repository-policies/
cp -rv $BASEDIR/data/policies/* $FEDORA_DIR/fedora-xacml-policies/repository-policies/

if [ "$USE_LDAP" == "true" ]; then
    # Fix jaas.conf so that we use the doms auth checker
    cp -v $CONFIG_TEMP_DIR/jaas.conf  $FEDORA_DIR/server/config/jaas.conf
fi
# Setup the custom users
cp -v $CONFIG_TEMP_DIR/fedora-users.xml $FEDORA_DIR/server/config/fedora-users.xml

# Setup the the lowlevel storage
if [ "$USE_XMLTAPES" == "true" ]; then
    cp -v $CONFIG_TEMP_DIR/akubra-llstore_xmltapes.xml $FEDORA_DIR/server/config/spring/akubra-llstore.xml
    cp -v $CONFIG_TEMP_DIR/xmlTapesConfig.xml $FEDORA_DIR/server/config/spring/xmlTapesConfig.xml
fi

# Install the "No object policy" rule
if [ "$USE_NO_OBJECT_POLICY" == "true" ]; then
    cp -v $CONFIG_TEMP_DIR/policy-enforcement.xml $FEDORA_DIR/server/config/spring/policy-enforcement.xml
fi

# Webapps are in non-standard place
cp -v $CONFIG_TEMP_DIR/env-server.sh $FEDORA_DIR/server/bin/env-server.sh

rm -rf $FEDORA_DIR/install

echo "Fedora setup complete"



echo "Installing Doms Schemas"
mkdir -p $SCHEMA_DIR
cp $BASEDIR/data/schemas/* $SCHEMA_DIR/



mkdir -p $BASEOBJS_DIR/bin
mkdir -p $BASEOBJS_DIR/scripts



cp -r $BASEDIR/extras/base-objects-ingester-*/scripts/*  $BASEOBJS_DIR/scripts

for file in $(find $BASEOBJS_DIR/scripts -type f ); do
  sed -i -e "s|http://naiad:7880/schemaStore/|http://${TOMCAT_SERVERNAME}:${PORTRANGE}80/schemaStore/|" "$file"
done


for file in $BASEDIR/extras/base-objects-ingester-*/bin/*.sh ; do
  replace $file $BASEOBJS_DIR/bin/`basename $file`
  chmod a+x $BASEOBJS_DIR/bin/`basename $file`
  echo "Created batch file $BASEOBJS_DIR/bin/`basename $file` from template file $file"
done

if [ -f $BASEDIR/ingester/$INGESTERZIP ]; then
  echo "Installing doms Radio-tv ingester"
  mkdir -p $INGEST_DIR
  unzip -q -n $BASEDIR/ingester/$INGESTERZIP -d $INGEST_DIR
  pushd `dirname $INGEST_DIR/radio-tv-*/bin` > /dev/null
  cp $CONFIG_TEMP_DIR/ingest_config.sh bin/
  mkdir -p files
  cp -r $BASEDIR/data/preingestfiles/ files/
  popd > /dev/null
  rm -rf $CONFIG_TEMP_DIR > /dev/null
fi

#installing the xmltapes migrator

echo "Installing xmltapes migrator"
mkdir -p $XMLTAPES_MIGRATOR_DIR/bin
mkdir -p $XMLTAPES_MIGRATOR_DIR/lib
cp -r $BASEDIR/extras/xmltapes-migrator-*/bin/*  $XMLTAPES_MIGRATOR_DIR/bin/
cp -r $BASEDIR/extras/xmltapes-migrator-*/lib/*  $XMLTAPES_MIGRATOR_DIR/lib/
mkdir -p $XMLTAPES_MIGRATOR_DIR/conf-datastreams
cp $CONFIG_TEMP_DIR/migrator.datastreams.logback.xml $XMLTAPES_MIGRATOR_DIR/conf-datastreams/logback.xml
cp -v $CONFIG_TEMP_DIR/migrator.datastreams.properties $XMLTAPES_MIGRATOR_DIR/conf-datastreams/migrator.properties
mkdir -p $XMLTAPES_MIGRATOR_DIR/conf-objects
cp $CONFIG_TEMP_DIR/migrator.objects.logback.xml $XMLTAPES_MIGRATOR_DIR/conf-objects/logback.xml
cp -v $CONFIG_TEMP_DIR/migrator.objects.properties $XMLTAPES_MIGRATOR_DIR/conf-objects/migrator.properties

echo "Install complete"

