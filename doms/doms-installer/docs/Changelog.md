2016-??-?? Release 1.26
======================
 * Use newest base-object-ingester, which does not bundle newspaper titles in every page object when harvester for Mediestream
   * This needs an upgrade script, migrate_1.16.sh to be run

2016-01-04 Release 1.25
======================
 * Upgraded Doms Central to 1.21. This fixes the "Summa Dead causes Doms Dead" bug
 * Doms package renamed
 * Included the xmltapes-migrator
 * Use version 1.2 of xmlTapes. This includes a speedup, and the possibility to replace the Redis Index with Postgres
 
 Two new postgres databases are needed for the Redis Object Index and for the Redis Datastream Index.
 The can be created with these commands. Of course, replace the password with something appropriate
 
    psql -c "CREATE ROLE \"xmltapesIndex\" LOGIN PASSWORD 'xmltapesIndexPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';"

    psql -c "CREATE DATABASE \"xmltapesObjectIndex\"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER=\"xmltapesIndex\";"

    psql -c "CREATE DATABASE \"xmltapesDatastreamIndex\"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER=\"xmltapesIndex\";"

 These should then be set up with the correct schema

    psql -d xmltapesObjectIndex     -U xmltapesIndex -h localhost -f extras/xmltapes-*/config/sql/postgres-index-schema.sql
    psql -d xmltapesDatastreamIndex -U xmltapesIndex -h localhost -f extras/xmltapes-*/config/sql/postgres-index-schema.sql

 In services/fedora/server/config/spring/akubra-llstore.xml, change line 260 to from

    <property name="index" ref="redisDatastreamIndex"/>
 to
 
    <property name="index" ref="postgresDatastreamIndex"/>

and line 147 from 
    
    <property name="index" ref="redisObjectIndex"/>
to

    <property name="index" ref="postgresObjectIndex"/>
    
Update the eight new lines in services/fedora/server/config/spring/xmlTapesConfig.xml to match the database setup for both the objectIndex and the datastreamIndex
    
    <property name="dbDriver" value="org.postgresql.Driver"/>
    <property name="jdbcUrl" value="$XMLTAPES_OBJECTS_JDBC$"/>
    <property name="dbUser" value="$XMLTAPES_DBUSER$"/>
    <property name="dbPass" value="$XMLTAPES_DBPASS$"/>


To migrate an existing redis installation to postgres, first change the config files 
    
    extras/xmltapes-migrator-1.2/conf-datastreams/migrate.properties
    extras/xmltapes-migrator-1.2/conf-objects/migrate.properties
to match your postgres setup.
Stop the doms system.
Then run 
    
    extras/xmltapes-migrator-1.2/bin/migrate.sh extras/xmltapes-migrator-1.2/conf-datastreams redis-to-postgres 
    extras/xmltapes-migrator-1.2/bin/migrate.sh extras/xmltapes-migrator-1.2/conf-objects redis-to-postgres

The complete migration might take a while. The two migrations can easily be run in parallel. Consider putting postgres
into unsafe mode by disabling flushing. The redis server will not be changed in any way.
Then start the Doms system

You can also migrate from postgres to redis by replacing the last parameter with postgres-to-redis

2015-09-25 Release 1.24
===========================
 * Use newest doms-server that fixes the beanutils classpath problem

2015-09-14 Release 1.23
===========================
 * Renamed maven from installer to doms-installer
 * New version of central which fixes the domsgui search problem

2015-08-06 Release 1.22
=========================
* Numerous fixes to the testbed install.sh to better accomadate the devel deploy system.
* Added start/stop_summa and start/stop_doms options to the doms.sh for more finegrained restarting. Relevant when upgrading the testbed
* Made the testbed save the summa progress files in the storage dir, so they survice reinstall. Only relevant for the testbed instance
* Update central to 1.17, which fixes the forgotten update tracker events bug
* Updated update tracker to 1.10 which corresponds to central 1.17


2015-07-06 Release 1.21
=========================
* Update to central 1.16 and update tracker 1.9, which fixes an excessive logging issue, and avoids duplicate class inclusions

2015-06-26 Release 1.20
============================
* Updated to central 1.15, which is just a bugfix to central 1.14. There was a bad bug in the
  update tracker delay feature introduced.

2015-06-25 Release 1.19
===============================
* Updated to central 1.14 which

  * Worklog events with age below "update tracker delay" are ignored. This should stop the short inconsistent states that DOMS can have.
  * Thread pool now names threads correctly
  * Fixed dependencies so that log4j can mail on errors
  * Handles caching correctly
  * uses the UTC timezone consistently when bundling objects for search
  * Update tracker do not log enormous lists which cause OutOfMemory

2015-06-15 Release 1.18
================================
* Use newest doms webservice, that always uses newest content model when updating web services

2015-05-22 Release 1.17
=====================================
* Fix bug where bin-directory was not bundled

2015-05-21 Release 1.16
=======================================
* Update tracker correctly tracks last modified
* Fix bug: Wider width of database column for method parameters in update tracker worklog
* Fix bug where fedora sets connections read-only
* In testbed, bundle summas with correct handling of last modified

2015-05-11 Release 1.15
====================================
* Pid generator updated to version that bundles correct dependencies.

2015-05-08 Release 1.14
======================================
* Use MPT Store, not Mulgara. This adds new configuration and a new database. See below. This requires a resource index rebuild on startup
* Include new update tracker. This adds new configuration and a new database. See below.
* No longer supports any other database than postgres
* Sends mail on serious errors from logging config.
* No longer bundles ingesters in testbed
* Now requires Java 7
* Updated a few dependencies
* Uses newest sboi in testbed
* Some default values have changed

A new database is needed for the doms triple store. It should be created like this:

            CREATE ROLE "$MPTSTORE_USER$" LOGIN PASSWORD '$MPTSTORE_PASS$'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';
            CREATE DATABASE "$MPTSTORE_DB$"
            WITH ENCODING='SQL_ASCII'
            OWNER="$MPTSTORE_USER$";

A new database is needed for the doms update tracker. It should have the schema found in
bin/rebuildupdatetracker/updatetrackerschema.sql 
of the base-objects-ingester.

A new table is needed in the existing doms database. It should be created with the sql script found in
bin/rebuildupdatetracker/updateTrackerLogs.sql
of the base-objects-ingester.

New configuration in context-default.xml:

    <Parameter name="fedora.worklog.database.driver" value="org.postgresql.Driver" override="false"/>
    <Parameter name="fedora.worklog.database.URL"
               value="jdbc:postgresql://$POSTGRESQL_SERVER$/$POSTGRESQL_DB$"
               override="false"/>
    <Parameter name="fedora.worklog.database.username" value="$POSTGRESQL_USER$" override="false"/>
    <Parameter name="fedora.worklog.database.password" value="$POSTGRESQL_PASS$" override="false"/>
    <Parameter name="fedora.updatetracker.web.URL"
               value="http://$TOMCAT_SERVERNAME$:$PORTRANGE$80/fedora"
               override="false"/>
    <Parameter name="fedora.updatetracker.web.username" value="$FEDORAUSER$" override="false"/>
    <Parameter name="fedora.updatetracker.web.password" value="$FEDORAUSERPASS$" override="false"/>
    <Parameter name="fedora.updatetracker.delay" value="10000" override="false"/>
    <Parameter name="fedora.updatetracker.period" value="30000" override="false"/>
    <Parameter name="fedora.updatetracker.limit" value="1000" override="false"/>
    <Parameter name="fedora.updatetracker.hibernateConfigFile"
               value="$TOMCAT_CONFIG_DIR$/updatetracker.hibernate.cfg.xml"
               override="false"/>
    <Parameter name="fedora.updatetracker.hibernateMappingsFile"
               value="$TOMCAT_CONFIG_DIR$/updatetracker.hibernate.mappings.xml"
               override="false"/>
    <Parameter name="fedora.updatetracker.viewbundleMaxThreads"
               value="2"
               override="false"/>
    <Parameter name="fedora.updatetracker.contentModelMaxThreads"
               value="2"
               override="false"/>

(No longer used is dk.statsbiblioteket.doms.updatetracker.fedoralocation)

New configuration in fedora.fcfg:

A new hook needs to be inserted:

     <param name="decorator5" value="dk.statsbiblioteket.doms.updatetracker.DomsUpdateTrackerHook"/>
     <param name="updateTrackerPoolName" value="localPostgreSQLPool">
     <comment>The storage pool used by the update tracker</comment>


The datastore parameter for the resource index should be set to MPTStore:

    <param name="datastore" value="localPostgresMPTTriplestore">

And the following should be added to enable the MPTStore:

    <datastore id="localPostgresMPTTriplestore">
        <comment>
            Example local MPTStore backed by Postgres.
            To use this triplestore for the Resource Index:
            1) In fedora.fcfg, change the "datastore" parameter of the
            ResourceIndex module to localPostgresMPTTriplestore.
            2) Login to your Postgres server as an administrative user and
            run the following commands:
            CREATE ROLE "$MPTSTORE_USER$" LOGIN PASSWORD '$MPTSTORE_PASS$'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';
            CREATE DATABASE "$MPTSTORE_DB$"
            WITH ENCODING='SQL_ASCII'
            OWNER="$MPTSTORE_USER$";
            3) Make sure you can login to your Postgres server as fedoraAdmin.
            4) Download the appropriate Postgres JDBC 3 driver from
            http://jdbc.postgresql.org/download.html
            and make sure it's accessible to your servlet container.
            If you're running Tomcat, putting it in common/lib/ will work.
        </comment>
        <param name="connectorClassName" value="org.trippi.impl.mpt.MPTConnector"/>
        <param name="ddlGenerator" value="org.nsdl.mptstore.impl.postgres.PostgresDDLGenerator"/>
        <param name="jdbcDriver" value="org.postgresql.Driver"/>
        <param name="jdbcURL" value="jdbc:postgresql://$MPTSTORE_SERVER$/$MPTSTORE_DB$"/>
        <param name="username" value="$MPTSTORE_USER$"/>
        <param name="password" value="$MPTSTORE_PASS$"/>
        <param name="poolInitialSize" value="3"/>
        <param name="poolMaxSize" value="10"/>
        <param name="backslashIsEscape" value="true"/>
        <param name="fetchSize" value="1000"/>
        <param name="autoFlushDormantSeconds" value="5"/>
        <param name="autoFlushBufferSize" value="1000"/>
        <param name="bufferFlushBatchSize" value="1000"/>
        <param name="bufferSafeCapacity" value="2000"/>
    </datastore>


A new configuration file updatetracker.hibernate.cfg.xml is needed:
    
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE hibernate-configuration PUBLIC
            "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
            "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
    <hibernate-configuration>
        <session-factory>
            <!-- database connection settings, change these for drift-->
            <property name="hibernate.connection.driver_class">org.postgresql.Driver</property>
            <property name="hibernate.connection.url">jdbc:postgresql://$UPDATETRACKER_POSTGRESQL_SERVER$/$UPDATETRACKER_POSTGRESQL_DB$</property>
            <property name="hibernate.connection.username">$UPDATETRACKER_POSTGRESQL_USER$</property>
            <property name="hibernate.connection.password">$UPDATETRACKER_POSTGRESQL_PASS$</property>
            <property name="hibernate.default_schema">PUBLIC</property>
            <property name="hibernate.dialect">org.hibernate.dialect.PostgreSQL9Dialect</property>
    
    
            <property name="hibernate.current_session_context_class">thread</property>
            <property name="hibernate.c3p0.min_size">1</property>
            <property name="hibernate.c3p0.max_size">10</property>
            <property name="hibernate.c3p0.timeout">3000</property>
            <property name="hibernate.c3p0.max_statements">50</property>
            <property name="hibernate.c3p0.idle_test_period">300</property>
    
            <!-- Drop and re-create the database schema on startup -->
            <property name="hibernate.hbm2ddl.auto">update</property>
    
            <!-- helper debug settings -->
            <property name="hibernate.use_sql_comments">false</property>
            <property name="hibernate.show_sql">false</property>
            <property name="hibernate.format_sql">false</property>
    
            <mapping class="dk.statsbiblioteket.doms.updatetracker.improved.database.datastructures.Record"/>
            <mapping class="dk.statsbiblioteket.doms.updatetracker.improved.database.datastructures.LatestKey"/>
        </session-factory>
    </hibernate-configuration>

A new configuration file updatetracker.hibernate.mappings.xml
    
    <?xml version="1.0"?>
    <!DOCTYPE hibernate-mapping PUBLIC
            "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
            "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
    
    <hibernate-mapping>
        <database-object>
            <create>create index memberships_objects_idx on PUBLIC.MEMBERSHIPS (OBJECTPID)</create>
            <drop>drop index if exists membership_objects_idx</drop>
        </database-object>
    
        <database-object>
            <create>create index memberships_records_idx on PUBLIC.MEMBERSHIPS (VIEWANGLE,ENTRYPID,COLLECTION)</create>
            <drop>drop index if exists memberships_records_idx</drop>
        </database-object>
    
    
        <database-object>
            <create>create index entrypid_idx on PUBLIC.RECORDS (ENTRYPID)</create>
            <drop>drop index if exists entrypid_idx</drop>
        </database-object>
    
        <database-object>
            <create>create index records_all_idx on PUBLIC.RECORDS (LASTMODIFIED, VIEWANGLE, COLLECTION)</create>
            <drop>drop index if exists records_all_idx</drop>
        </database-object>
    
        <database-object>
            <create>create index records_active_notnull_idx on PUBLIC.RECORDS (LASTMODIFIED, VIEWANGLE, COLLECTION) where
                DELETED is not null or ACTIVE is not null
            </create>
            <drop>drop index if exists records_active_notnull_idx</drop>
        </database-object>
        <database-object>
            <create>create index records_inactive_notnull_idx on PUBLIC.RECORDS (LASTMODIFIED, VIEWANGLE, COLLECTION) where
                DELETED is not null or INACTIVE is not null
            </create>
            <drop>drop index if exists records_inactive_notnull_idx</drop>
        </database-object>
    
        <database-object>
            <create>create index records_deleted_notnull_idx on PUBLIC.RECORDS (LASTMODIFIED, VIEWANGLE, COLLECTION) where
                DELETED is not null
            </create>
            <drop>drop index if exists records_deleted_notnull_idx</drop>
        </database-object>
    
    </hibernate-mapping>

Some log configurations have been updated. Especially, make sure you set the email sender and recepient in log4j.centralDomsWebservice.xml




2014-12-12 Release 1.13
=========================
* Now with xml tapes that do not index twice

2014-11-14 Release 1.12
========================
* Use functional Summa

2014-11-12 Release 1.11
==========================
* Use newest SBOI that supports the new Item-based autonomous components
* Use the newest base-object-ingester, that defines Items and newspaper titles
* Use newest central library that removes EVENTS from cloned templates

Release 1.10
======================
 * Save the last 12 logs
 * The new mulgara 2.1.14-SB is added and included in the installer
 * Package the fedora sb additions in classes as this way we can override fedora classes
 * Fixed the log config to correctly log to fedora.log.warn
 * Log the mulgara stuff much more so that we can actually find the errors and track back to what the system is doing
 * Better logfile, which excludes the AttributeFinder and makes a warn log

2014-08-23 Release 1.9
============================
* summarise doms-wui is now version 1.1
* Fixed the SQL Like bug. This require a database rebuild of an existing system.
* Base Objects Ingester to version 1.7
 * Use the right Jpylyzer for the newspaper data model
 * Include a Newspaper Title template object
 * Use the newest and correct version of the Film schema
 * Update to Summa relations
 * Use the correct Mods 3.5 schema
* XmlTapes to version 1.0.11
 * Only create a new tape after we have checked the newest tape... Stupid bug
 * Fixed the error that would appear in the log if the archive was closed. Now closing require the WriteLock, meaining that it waits for the taper thread.
 * Added a more comprehensive test


2014-07-23 Release 1.8
=============================
* Updated to version 1.0.9 of xmltapes, which fixed the "Corrupt tapes on purge" bug.

2014-06-23 Release 1.7
============================
* Updated to version 1.0.8 of xmltapes. Removing open files leak on errors, and stops urlencoding filenames in tapes.

2014-06-19 Release 1.6
======================
* Fixed a bad bug in tapes (1.0.7),  that made upgrading from an old doms impossible

2014-06-18 Release 1.5
======================
* Update to 1.0.6 of xmltapes. This fixes the bug that would cause data loss by deleting source metadata files regardless of them being succesfully written to the tapes or not. 
* The testbed has two summa instances deployed (one for SBOI, another for searching doms, as used by DomsGUI).
* The project has been vagrant enabled, i.e. making it easy to setup a virtual machine with a running doms, for development purposes. 
* Enable the validator hook.
* Use the newest base objects ingester 1.5, with various improvements to newspaper datamodel and various fixes for radiotv datamodel
* Use version 1.3 of doms-server for improved error messages, and better search
* Use version 1.1 of ecm module to get exceptions from failed validations

2014-04-04 Release 1.4
======================
* Updated to 1.0.5 of xmltapes. This fixes a nasty bug introduced in the previous release, where some tapes used the extension ".gz.tar" and others the extension ".tar". To upgrade, rename all the xmltapes to .tar. Then update the akubra-config.xml to set rebuild true and restart the doms. After this first restart, you can set rebuild to false again.
* Doms can now shut down correctly again.

2014-03-31 Release 1.3
======================
* Updated to xml tapes 1.0.4 which have configurable tape names
* Tape archives are now named '.tar', not '.tgz'. Existing files must be renamed and the redis database rebuild when upgrading
* Updated to centralWebservice 1.2 which fixes a timezone bug when retrieving old versions of a datastream
* Vagranised
* Use the newest base objects ingester 1.4
* Made a very configurable install procedure
* Made the small.sh script to enable an install with very few features, perfect for integration tests

2014-01-16 Release 1.2
======================
* Upgraded to version 1.0.3 of CentralWebservice. This should fix the bug for summa indexing of objects
* with managed datastreams
* Updated to Summa newspapr 1.2

2014-01-08 Release 1.1
======================
* Updated to Summa newspapr 1.1.1
* Updated to XmlTapes v. 1.0.2
* Summa folders (dump,storage,index...) is no longer created in the install dir, but rather in the data dir

2013-11-22 Release 1.0
======================
Versions:

* Update all dependencies to 1.0 released versions
* Use the newer 0.0.9 radio tv ingester
* Removed the "deny change of published objects" and the "deny purge" policy

Summarise:

* Comment where to find summarise repo
* Update Summarise config template to include SBOI
* Update Summarise dependency
* Should bundle and install the summa integration now
* Updated to version 0.5 of domsgui summarise
* New summarise domsgui site

Bugs:

* Fix the cannot install problem when outside SB
* Hopefully fixed the logback no line break error
* Create the summix storage folder before using it
* Log all doms events, not just the ones from central
* Don't sync updates
* Included the mulgara timeout fix

XmlTapes:

* The datastream store is also taped now, as we will use managed datastreams
* Use Xml Tapes

General:

* Updated to use fedora 3.6.2
* Update scripts to match production


3/4/2013 Release version 0.12.0
===============================

* Update docs and configuration

22/3/2013 Release version 0.11.3
===============================
* Remove web services for obsolete bit repository
* Remove web services for update tracker and ecm, now integrated as libraries in central doms web service
* Update authchecker dependency to version 0.0.12
    * support for authorization on object pids rather than URL's
* Update doms central webservice dependency to 0.0.27
    - use a Summa indexing engine for searching
    - support for object behaviours (methods and links)
    - support for historic views of object bundles
    - remove old bit repository webservice dependencies
    - Internalize ecm and update tracker dependencies as libraries, rather than use them as web services
* Require a Summa indexing engine

2/5/2011 Release version 0.11.2
===============================
* Build both packages with and wothout ingesters when packaging.
* Fix logging configuration issues.

24/1/2012 Release version 0.11.1
===============================
* Updated to 0.26 central webservice , fixing a memory bug when loading content model labels

* Updated the shard metadata schema

13/12/2011 Release version 0.11.0
===============================
* Added the FFPROBE datastream to radiotv file objects

* Use updatetracker 0.0.14
 * Cleaned up the branch/revert naming problems, and renamed some modules

* Shard is not entry content model for view BES

* DomsCentral 0.0.24
  * Use the 0.0.14 updatetracker




2/11/2011 Release version 0.10.24
===============================
* Updated Radio TV Program Schema with GUI annotations

* Pulled in new releases of doms webservices

* ShardMetadata - channelId optional

* Added schema for Shard metadata

* Reverted to the old working version of the updatetracker

* Integrated with AD system - completed

* New version of DomsCentral 0.0.23
    * Updated to newest doms.pom
    * fixed broken methods (relation add/remove)
    * ObjectProfile with support for more types of objects (Collections, File objects)
    * GetObjectsInCollection



19/9/2011 Release version 0.10.23
===============================
* Updated authchecker to version 0.0.11
   * Creating Added admin user
   * Admin users remain for 2 days
   * Fixed a Rest interface problem

* Integrated with AD system - begun

* Updated base objects with GUI annotations

* New version of DomsCentral 0.0.22
   * with fedora fieldsearch support (to be removed when summa integration is complete)
   * DatastreamProfile method
   * ObjectProfile method
   * Inverse relations
   * Temp Usercreation - AD stuff


30/6/2011 Release version 0.10.22
===============================
* Disabled checksum checking on template datastreams that are updated

* Actually fixed the things that made the ingester go boom (remove checksum checking from template datastreams with IDs).

20/6/2011 Release version 0.10.21
===============================
* Updated to use the newest ECM (0.0.18). The previous one had a bug when cloning files.

* Updated the batch objects, there were numerous errors in these.

* Updated the radio tv policy to include the faculty group and other fixes to license..

* Fixed some things that made the ingester go boom.

* Removed the default // stuff in paths

* Removed the empty services/conf/fedora dir, that was not used any more

* Added the staff@hum.au.dk role to the radio tv license

11/4/2011 Release of version 0.10.20.1
===============================

* Fixed a very minor bug in package.sh that prevented install with postgresql database

8/4/2011 Release of version 0.10.20
===============================

* Made the doms ingester optional, so it will not be bundled in the release

* Fixed the errors in the log4j config files

* Removed the version numbers from the war files, and removed the symlink stuff

* Moved schemas to live in services/

* Moved schemaStore.xml to live in services/tomcat-apps

* Removed the services/tomcatapps folder. The warfiles are now in services/webapps

* Brought the changelog up2date

1/4/2011 Release of version 0.10.19
===============================
* Changed the build system to maven, instead of ant

* Renamed the doms webservices (rather, a natural consequence of maven)

* Updated log4j config files to use rolling file appenders

* Added the ingester as an dependency, and some example preingest files

* Updated the system to create basic objects, to use the fedora batch process

* Added the SchemaStore pseudo process

* Updated the authchecker service to 0.0.10
    - changed the interface
    - Made the tickets carry more info


28/1/2011 Release of version 0.10.18
===============================
* Changed logging levels back to DEBUG

* Upgraded all services, so that they include the proper surveillance libs

* Fixed the xacml path problem in fedora.fcfg

26/1/2011 Release of version 0.10.17 - Second official Release
===============================
* updated the logging configurations, so that we log on INFO in
individuals files, and WARN in the doms.log file.

* Updated base objects so that
   1. removed the big pbcore placeholder from template_program, as it was clogging up the summa
   2. made all RELS-EXT datastreams versionable
   3. Split the RadioTV_License into adminOnlyLicense, and inhouseLicense
   4. Updated all the objects to not use POLICY datastreams, if they were only referring to the default open License. This speeds up ingests x2.
   5. Fixed the wrong formatURI/mimetype in the POLICY datastreams

*  Updated fedora to version 3.4.2.Should not have any incompabilities aas we were using a maintenance snapshot before

*  Updated ECM to version 0.0.13, so that it does not cause "deprecated" warnings in fedora.log

*  Removed unneeded things from fedora.fcfg

*  Policies moved from TOMCAT_CONF_DIR/fedora to FEDORA_DIR

*  Added fedora.home to context.xml

17/1/2011 Release of version 0.10.16 - First official release, changelog begins here
===============================
