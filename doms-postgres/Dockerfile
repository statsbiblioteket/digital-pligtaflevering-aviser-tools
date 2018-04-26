# We need a DOMS user and schema.
FROM postgres:9.6
MAINTAINER tra@kb.dk

# https://hub.docker.com/_/postgres/ - search for "How to extend this image"

COPY 10-create-databases.sql /docker-entrypoint-initdb.d/
COPY 20-populate-xmltapes.sh /docker-entrypoint-initdb.d/
