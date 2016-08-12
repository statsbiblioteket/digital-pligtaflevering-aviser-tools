#!/bin/bash
#
# Initiate creation of a specific batch roundtrip in DOMS
#
# Parameters:
# $1 : path to config file
# $2 : batch id
# $3 : batch roundtrip number
#
# Author: jrg, ktc, kfc
#

# Use null-globbing so * can expand to empty and we can avoid nasty surprises
shopt -s nullglob

SCRIPT_PATH=$(dirname $(readlink -f $0))

trigger_file=transfer_acknowledged

config="$1"
batch_id=$2
roundtrip=$3
if [ -z "$roundtrip" ]; then
	echo "config, batch or roundtrip_id not received" >&2
	echo "usage: $(basename $0) /path/to/config.sh batch_id roundtrip_number" >&2
	exit 1
fi

source "$config"

# Check that mandatory settings are atleast defined
for var in path_to_dir_of_batches donedir trigger_name url_to_doms doms_username doms_password url_to_pid_gen
do
    [ -z "${!var}" ] && echo "ERROR: $config must define \$$var" && exit 2
done

exec 200> "$donedir/trigger.lock"
flock -n 200 || exit

cd "$path_to_dir_of_batches"

batch_dirname="B${batch_id}-RT${roundtrip}"

# Check directory exists
if [ ! -d "$batch_dirname" ]; then
	echo "This batch does not exist. Exiting."
	exit 1
fi

# Check for trigger-file
if [ ! -f "$batch_dirname/$trigger_file" ]; then
	# Trigger-file does not exist, so batch is not ready for us, skip it
	echo "This batch is not acknowledged. Exiting."
	exit 1
fi

# Skip batches that are already done
if [ -f "$donedir/$batch_dirname" ]; then
	echo "This batch is already triggered. Exiting."
	exit 1
fi

# Mark batch as done, by creating an empty file with the batch's name
touch "$donedir/$batch_dirname"

# Create batch in DOMS
java -classpath $SCRIPT_PATH/../conf/:$SCRIPT_PATH/../lib/'*' dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateBatch "$batch_id" "$roundtrip" "$trigger_name" "$url_to_doms" "$doms_username" "$doms_password" "$url_to_pid_gen"

