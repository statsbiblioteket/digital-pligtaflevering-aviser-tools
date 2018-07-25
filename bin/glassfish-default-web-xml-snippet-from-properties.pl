#!/usr/bin/perl -nl

use strict;
use warnings FATAL => 'all';

if (/^ *#/) {
# Ignore comments.
} else {
    if (/([^=]+)\=(.*)/) {
	print <<"EOF"
  <context-param>
      <param-name>$1</param-name>
      <param-value>$2</param-value>
  </context-param>
EOF
    }
}
