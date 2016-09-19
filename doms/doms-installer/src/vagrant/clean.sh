#!/bin/bash

# Reset vagrant state without having to rebuild vagrant image.

function drop(){
PGPASSWORD=$2 psql -U $1 $3 -h localhost -t -c "select 'drop table \"' || tablename || '\" cascade;' from pg_tables where schemaname = 'public'"  | PGPASSWORD=$2 psql -U $1 $3 -h localhost
}

function truncatedb() {
PGPASSWORD=$2 psql -U $1 $3 -c "truncate storeIndex,indexed;"
}

killall -9 java
drop domsFieldSearch domsFieldSearchPass domsFieldSearch
drop domsMPT domsMPTPass domsTripleStore
drop domsUpdateTracker domsuptrack domsUpdateTracker
rm -rf ~/7880-d*
rm -rf /tmp/updateTracker.progress*
#redis-cli flushall
truncatedb xmltapesIndex xmltapesIndexPass xmltapesObjectIndex
truncatedb xmltapesIndex xmltapesIndexPass xmltapesDatastreamIndex
