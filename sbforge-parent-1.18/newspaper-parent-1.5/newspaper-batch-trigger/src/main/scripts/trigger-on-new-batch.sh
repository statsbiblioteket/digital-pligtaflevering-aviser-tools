#!/bin/bash
#
# Check for newly arrived newspaper batch(es), and initiate creation in DOMS
#
# Parameters:
# $1 : path to config file
#
# Author: jrg, ktc
#

# Use null-globbing so * can expand to empty and we can avoid nasty surprises
shopt -s nullglob

SCRIPT_PATH=$(dirname $(readlink -f $0))

trigger_file=transfer_acknowledged

config="$1"
if [ -z "$config" ]; then
	echo "config not received" >&2
	echo "usage: $(basename $0) /path/to/config.sh" >&2
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

for batch_dirname in *; do
	# Check format of dirname
	if [[ ! "$batch_dirname" =~ ^B[^-]+\-RT[0-9]+$ ]]; then
		# Dirname not recognized as a batch, skip it
		continue
	fi

	# Check for trigger-file
	if [ ! -f "$batch_dirname/$trigger_file" ]; then
		# Trigger-file does not exist, so batch is not ready for us, skip it
		continue
	fi

	# Skip batches that are already done
	if [ -f "$donedir/$batch_dirname" ]; then
		continue
	fi

	# Mark batch as done, by creating an empty file with the batch's name
	touch "$donedir/$batch_dirname"

	batch_id=$(echo "$batch_dirname" | sed -r 's/^B([^-]+).+/\1/g')
	roundtrip=$(echo "$batch_dirname" | sed -r 's/^B[^-]+\-RT([0-9]+)/\1/g')

	# Create batch in DOMS
	java -classpath $SCRIPT_PATH/../conf/:$SCRIPT_PATH/../lib/'*' dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateBatch "$batch_id" "$roundtrip" "$trigger_name" "$url_to_doms" "$doms_username" "$doms_password" "$url_to_pid_gen"
done

