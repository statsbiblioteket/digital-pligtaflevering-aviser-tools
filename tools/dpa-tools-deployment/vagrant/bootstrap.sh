#!/usr/bin/env bash



sudo mkdir dpa
sudo chmod 777 dpa
cd dpa

sudo mkdir logs
sudo chmod 777 logs


sudo cp /target/dpa-tools-deployment-master-SNAPSHOT-package.tar.gz /home/vagrant/dpa/dpa-tools-deployment-master-SNAPSHOT-package.tar.gz
tar -xf dpa-tools-deployment-master-SNAPSHOT-package.tar.gz

cp -R /vagrant/properties properties

sudo cp  /vagrant/init/initiateAutonomousComponents.sh /etc/init.d/initiateAutonomousComponents.sh
sudo chmod +x /etc/init.d/initiateAutonomousComponents.sh

cp  /vagrant/runningTheStuff.sh runningTheStuff.sh
sudo chmod +x runningTheStuff.sh


curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.0.0-linux-x86_64.tar.gz
tar -xf filebeat-5.0.0-linux-x86_64.tar.gz

sudo cp /vagrant/filebeat/vagrantfilebeat.yml /home/vagrant/dpa/filebeat-5.0.0-linux-x86_64/filebeat.yml
sudo cp /vagrant/filebeat/startFilebeat.sh /home/vagrant/dpa/filebeat-5.0.0-linux-x86_64/startFilebeat.sh


cd logs

cp  /vagrant/startCreateBatch.sh startCreateBatch.sh
sudo chmod +x startCreateBatch.sh

#cp  /vagrant/startHandler.sh startHandler.sh
#sudo chmod +x startHandler.sh




