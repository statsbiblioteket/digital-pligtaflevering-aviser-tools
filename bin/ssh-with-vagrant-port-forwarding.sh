#!/usr/bin/env bash

# Create ssh connection with port forwardings for DOMS and Summa.
# Provide [user@]machine as argument if not tra.
HOST=${1:-pc591.sb}

# http://linuxcommand.org/lc3_man_pages/ssh1.html
ssh -X -A -L7880:localhost:7880 -L58608:localhost:58608 -L58709:localhost:58709 $HOST
