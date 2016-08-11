#!/bin/bash
#
# Config file for trigger-on-new-batch.sh
#
# Author: jrg
#

# Where the received newspaper batches are placed
path_to_dir_of_batches=/newspapr_batches

# Fedora location
url_to_doms=http://localhost:7880/fedora

# Username for calls to DOMS
doms_username=fedoraAdmin

# Password for calls to DOMS
doms_password=fedoraAdminPass

# Location of PID generator
url_to_pid_gen=http://localhost:7880/pidgenerator-service

# Name of the trigger-script
trigger_name=register-batch-trigger

# Location of var folder for storing processed batches
donedir=$HOME/done-batches
