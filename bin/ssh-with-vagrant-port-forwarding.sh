#!/usr/bin/env bash

# Used to ensure that doms and sboi can be reached under the hostname localhost, even if they run on another machine.
# This ensures that config files do not need to be fixed.

# Create ssh connection with port forwardings for DOMS and Summa.
# Provide [user@]machine as argument if not tra.
HOST=${1:-pc591.sb}

# http://linuxcommand.org/lc3_man_pages/ssh1.html
ssh -X -A -L7880:localhost:7880 -L58608:localhost:58608 -L58709:localhost:58709 $HOST
