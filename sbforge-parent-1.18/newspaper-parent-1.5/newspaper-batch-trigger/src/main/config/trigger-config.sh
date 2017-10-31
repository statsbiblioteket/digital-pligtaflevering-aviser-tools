#!/bin/bash
#
# Config file for trigger-on-new-batch.sh
#
# Author: jrg
#

# Where the received newspaper batches are placed
path_to_dir_of_batches={avis.folder}

# Fedora location
url_to_doms=http://{doms.host}:{doms.port}/fedora

# Username for calls to DOMS
doms_username={doms.username}

# Password for calls to DOMS
doms_password={doms.password}

# Location of PID generator
url_to_pid_gen=http://{doms.host}:{doms.port}/pidgenerator-service

# Name of the trigger-script
trigger_name=register-batch-trigger

# Location of var folder for storing processed batches
donedir=$HOME/var/batches-done
