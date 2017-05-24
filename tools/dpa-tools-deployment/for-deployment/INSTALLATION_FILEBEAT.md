
Installation of Filebeat:
---
1) First copy filebeat tarball to the server

curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.0.0-linux-x86_64.tar.gz

2) Next unpack filebeat on the server
tar -xf filebeat-5.0.0-linux-x86_64.tar.gz

3) Configure filebeat 
Overwrite til filebeat.yml from the unpacked filebeat-tarball [filebeat-5.0.0-linux-x86_64.tar.gz] with filebeat.yml from the tarball delivered by the DPA-project 
[dpa-tools-deployment-master-SNAPSHOT-package.tar.gz/conf/filebeat.yml]

4) Change filebeat.yml with the path to logfiles and the serverpath to logstash.

5) Add startupscript to start filebeat with [./filebeat start] and make it start on serverstartup

