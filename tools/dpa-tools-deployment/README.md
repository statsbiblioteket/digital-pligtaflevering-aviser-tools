Executables are designed to be run in the directory where the log
files and various debug files are to be written.

An absolute path or relative path to the binary is
fine.  

The following description explains how to startup the setup with 3 different Vagrant images.

- Fedora-Vagrant-image.  contains fedora-commons and solr, this image is not part of the dpa project, but it is used to simulate the servere where the *.pdf and *.xml is stored

   Read doms/doms-installer/README.md to get started with the Vagrant image for Fedora-commons

- Kibana-Vagrant-image contains the Kibana setup, which recieves event-logs from the dpa-project

   This image can be pulled from the git-repository "ssh://git@sbprojects.statsbiblioteket.dk:7999/ist/elk.git"
 
- Dpa-Vagrant-image Can be started from this folder


In order to start the full setup 



Dpa-Vagrant-image:
===

1. Run "vagrant up" from this folder

2. login to image via "vagrant ssh"

3. run dpa/runningComponents.sh

