#!/usr/bin/perl  -l
use strict;
use warnings FATAL => 'all';

# first argument is Java property file for environment (vagrant/stage/prod).
#
# Output are pids for objects returned by DOMS.
#
# For ubuntu:   apt install libconfig-simple-perl liburi-encode-perl
#
# Sample invocation:
#   ./pids_for < ~/dpa-mirzam.conf path:dl_20080101_rt1

use URI::Encode;

#  Gave up on Config::Simple
my %cfg;
while (<STDIN>) {
    if (my ($key, $value) = m/^([^#=]+)=([^\r\n]*)/) {
	$cfg{$key} = $value;
    }
}

my $uriEncoder = URI::Encode->new({encode_reserved => 0});

for (@ARGV) {
    my $urlString = $cfg{'doms.url'}
    ."/objects?pid=true&resultFormat=xml&identifier=true&maxResults=99999&terms="
	.$uriEncoder->encode($_);

    print "curl -u$cfg{'doms.username'}:$cfg{'doms.password'} '$urlString' "
	. "|  xmlstarlet sel -N x=http://www.fedora.info/definitions/1/0/types/ -t -m '/x:result/x:resultList/x:objectFields/x:pid' -v '.' -n";
}
#for (@ARGV) {
#    print "env FEDORA_USER=" . $cfg->param('default.doms.username');
#    print " FEDORA_PASSWORD=" . $cfg->param('default.doms.password');
#    print "  O=" . $cfg->param('default.doms.url') . "/objects";
#    print " ./checksums-for-delivery-in-doms.sh    $_";
#    print " \n";
#}

1;
