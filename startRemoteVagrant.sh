#!/usr/bin/env bash

SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))

set -x

host=${1:-pc596}

#Checkout project on remote machine
ssh -A -t "$host" << EOF
set -x
mkdir -p ~/Projects/digital-pligtaflevering-aviser-tools
cd ~/Projects/digital-pligtaflevering-aviser-tools
git clone git@github.com:statsbiblioteket/digital-pligtaflevering-aviser-tools.git .
EOF

#sshfs so you can edit the project directly
mkdir -p "$HOME/$host"
mount | grep -q " $HOME/$host " || sshfs "$host":. "$HOME/$host"


JAVA7="env JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/"
JAVA8="env JAVA_HOME=/usr/lib/jvm/zulu-8-amd64"

#Start vagrant
ssh -A -t "$host" <<-EOF
	cd ~/Projects/digital-pligtaflevering-aviser-tools;


	vagrant plugin list | grep vagrant-timezone || vagrant plugin install vagrant-timezone
	vagrant plugin list | grep vagrant-scp || vagrant plugin install vagrant-scp
	vagrant up

	vagrant ssh -- <<-EOV
		set -x
		set -e
		${JAVA8} /vagrant/install_bitrepository.sh
		${JAVA8} /vagrant/run-bitrepositorystub-webserver.sh

		${JAVA7} /vagrant/install_doms.sh
		echo export CATALINA_PID=~/7880-doms/catalina.pid >> ~/7880-doms/tomcat/bin/setenv.sh

		${JAVA8} /vagrant/run-verapdf-rest.sh
	EOV

	vagrant ssh  -- <<-EOV
		${JAVA7} ~/7880-doms/tomcat/bin/startup.sh
		${JAVA7} ~/7880-doms/sboi-summarise/bin/start_resident.sh
		${JAVA8} ~/bitrepository-quickstart/quickstart.sh restart
	EOV

EOF


#    vagrant snapshot save up


#Port forwarding
# http://linuxcommand.org/lc3_man_pages/ssh1.html
FEDORA_PORT=7880
SBOI_PORT=58608
BITREPO_PORT=58709
ACTIVEMQ_PORT=61616
VERAPDF_PORT=8090
POSTGRES_PORT=5432
ZOOKEEPER_PORT=2121
ssh -A -L"$FEDORA_PORT:localhost:$FEDORA_PORT" -L"$SBOI_PORT:localhost:$SBOI_PORT" -L"$BITREPO_PORT:localhost:$BITREPO_PORT" -L"$ACTIVEMQ_PORT:localhost:$ACTIVEMQ_PORT" -L"$VERAPDF_PORT:localhost:$VERAPDF_PORT" "$host"  <<-EOF
set -e
set -x
cd ~/Projects/digital-pligtaflevering-aviser-tools
while true; do
	date;
	vagrant ssh -c '${JAVA7} ~/7880-doms/bin/doms.sh update';
	sleep 45;
done
EOF


#export V=$(VBoxManage list runningvms | cut -d\" -f2 | grep digital-pligtaflevering-aviser-tools_default); VBoxManage controlvm $V poweroff ; sleep 4 ; VBoxManage snapshot $V restore "XX"; VBoxManage startvm $V --type headless