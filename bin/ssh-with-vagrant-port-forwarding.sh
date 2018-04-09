#!/usr/bin/env bash

# Create ssh connection with port forwardings for DOMS and Summa.
# Provide [user@]machine as argument if not tra.
HOST=${1:-pc591.sb}
PORTFORWARD=${2:-localhost}

# http://linuxcommand.org/lc3_man_pages/ssh1.html
ssh -X -A -L7880:${PORTFORWARD}:7880 -L58608:${PORTFORWARD}:58608 $HOST
