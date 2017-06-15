#!/usr/bin/env bash

set -e

# First add the extra disk and extend the root volume.
#
# Adapted from https://www.rootusers.com/how-to-increase-the-size-of-a-linux-lvm-by-adding-a-new-disk/

fdisk /dev/sdb << EOF
n
p
1


t
8e
w
EOF

pvcreate /dev/sdb1
vgextend vagrant-vg /dev/sdb1
lvextend -r /dev/vagrant-vg/root /dev/sdb1

# ---
# Now continue installing stuff

ln -s /vagrant/nohup.out /home/vagrant/nohup.out



apt-get update > /dev/null
apt-get install -y zip unzip openjdk-7-jdk zookeeperd git xmlstarlet

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/

# May be deleteable, check with ABR.
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

mkdir /newspapr_batches
chown vagrant /newspapr_batches
chmod 755 /newspapr_batches
