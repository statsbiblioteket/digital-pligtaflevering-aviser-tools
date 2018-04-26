--  Adapted from vagrant/scripts/bootstrap.sh with ABR

--  NOTE:  If this file is not correct, an error is reported, but next
-- build does NOT rerun this file.

-- Fedora web gui search support index database and owner.

CREATE ROLE "domsFieldSearch" LOGIN PASSWORD 'domsFieldSearchPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';

CREATE DATABASE "domsFieldSearch"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER="domsFieldSearch";

-- Triple store database and owner

 CREATE ROLE "domsMPT" LOGIN PASSWORD 'domsMPTPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';

CREATE DATABASE "domsTripleStore"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER="domsMPT";

--  Update tracker (so we can ask about "last modified objects") role and database

CREATE ROLE "domsUpdateTracker" LOGIN PASSWORD 'domsuptrack'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';

CREATE DATABASE "domsUpdateTracker"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER="domsUpdateTracker";

-- XML tapes (tar file based backend for Fedora objects as opposed to every object in its own file) role and database.

CREATE ROLE "xmltapesIndex" LOGIN PASSWORD 'xmltapesIndexPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';

CREATE DATABASE "xmltapesObjectIndex"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER="xmltapesIndex";

-- handles Fedora _managed_ datastreams.

CREATE DATABASE "xmltapesDatastreamIndex"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER="xmltapesIndex";
