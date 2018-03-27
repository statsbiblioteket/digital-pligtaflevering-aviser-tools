#!/usr/bin/perl 
use strict;
use warnings FATAL => 'all';

# first argument is Java property file for environment (vagrant/stage/prod).
# Output is shell snippet for setting environment variables for traverse-delivery-in-doms.sh script.
#
# For ubuntu:   apt install libconfig-simple-perl
#
# Sample invocation:
# cat  ~/stage-uuids.txt |xargs ./env-invoke-validate-delivery-in-doms-from-properties.pl ~/dpa-mirzam.conf | sh -xv -

use Config::Simple;

my $cfg = new Config::Simple($ARGV[0] or die "first argument is property file name");
shift @ARGV;

my %Config = $cfg->vars();

for (@ARGV) {
    print "env FEDORA_USER=" . $cfg->param('default.doms.username');
    print " FEDORA_PASSWORD=" . $cfg->param('default.doms.password');
    print "  O=" . $cfg->param('default.doms.url') . "/objects";
    print " ./validate-delivery-files-in-doms.sh -v $_";
    print " \n";
}

1;
