#!/usr/bin/env bash

#  Copied from XMLTapes version 1.2

# https://github.com/statsbiblioteket/xmlTapesForFedora/tree/master/xmltapes/src/main/config/sql

cat > /tmp/postgres-index-schema.sql << EOF
CREATE TABLE storeIndex (
	id VARCHAR(255) PRIMARY KEY,
	tapename VARCHAR(255) NOT NULL,
	tapeoffset BIGINT NOT NULL
);

CREATE TABLE indexed (
	tapename VARCHAR(255) NOT NULL PRIMARY KEY
);
EOF

PGPASSWORD=xmltapesIndexPass psql -d xmltapesObjectIndex -U xmltapesIndex -h localhost -f /tmp/postgres-index-schema.sql
PGPASSWORD=xmltapesIndexPass psql -d xmltapesDatastreamIndex -U xmltapesIndex -h localhost -f /tmp/postgres-index-schema.sql


