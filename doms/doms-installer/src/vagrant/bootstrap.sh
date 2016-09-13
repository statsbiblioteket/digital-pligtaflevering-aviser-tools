#!/usr/bin/env bash

apt-get update > /dev/null
apt-get install -y zip unzip

# Download and install Oracle Java 7

apt-get install -y python-software-properties
add-apt-repository ppa:webupd8team/java
apt-get update > /dev/null
echo debconf shared/accepted-oracle-license-v1-1 select true |  sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true |  sudo debconf-set-selections
apt-get install -yq oracle-java7-installer oracle-java7-set-default
export JAVA_HOME=/usr/lib/jvm/java-7-oracle/


apt-get install -y redis-server

apt-get install -y postgresql postgresql-contrib

# Postgres:  Listen and accept login on all network interfaces.
echo "host    all             all             0.0.0.0/0               md5" >> /etc/postgresql/9.3/main/pg_hba.conf
echo "listen_addresses = '*'" >> /etc/postgresql/9.3/main/postgresql.conf
service postgresql restart


#TODO remember to check database encoding with IT Drift
sudo -u postgres psql -c " CREATE ROLE \"domsFieldSearch\" LOGIN PASSWORD 'domsFieldSearchPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';"

sudo -u postgres psql -U postgres -c "CREATE DATABASE \"domsFieldSearch\"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER=\"domsFieldSearch\";"


sudo -u postgres psql -c " CREATE ROLE \"domsMPT\" LOGIN PASSWORD 'domsMPTPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';"

sudo -u postgres psql -U postgres -c "CREATE DATABASE \"domsTripleStore\"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER=\"domsMPT\";"

sudo -u postgres psql -c " CREATE ROLE \"domsUpdateTracker\" LOGIN PASSWORD 'domsuptrack'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';"

sudo -u postgres psql -U postgres -c "CREATE DATABASE \"domsUpdateTracker\"
            WITH
            TEMPLATE=template0
            ENCODING='SQL_ASCII'
            OWNER=\"domsUpdateTracker\";"

sudo -u postgres psql -c " CREATE ROLE \"xmltapesIndex\" LOGIN PASSWORD 'xmltapesIndexPass'
            NOINHERIT CREATEDB
            VALID UNTIL 'infinity';"

sudo -u postgres psql -U postgres -c "CREATE DATABASE \"xmltapesObjectIndex\"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER=\"xmltapesIndex\";"

sudo -u postgres psql -U postgres -c "CREATE DATABASE \"xmltapesDatastreamIndex\"
            WITH
            TEMPLATE=template0
            ENCODING='UTF8'
            OWNER=\"xmltapesIndex\";"

#PGPASSWORD=xmltapesIndexPass psql -d xmltapesObjectIndex -U xmltapesIndex -h localhost -f /vagrant/postgres-index-schema.sql
#PGPASSWORD=xmltapesIndexPass psql -d xmltapesDatastreamIndex -U xmltapesIndex -h localhost -f /vagrant/postgres-index-schema.sql

# For DOMS Wui Vagrant virtual network.
echo "192.168.50.2 doms-testbed" >> /etc/hosts
echo "192.168.50.4 domswui-testbed" >> /etc/hosts

# and create batch folder

sudo mkdir /newspapr_batches
sudo chown vagrant /newspapr_batches
sudo chmod 755 /newspapr_batches
