
Before starting:
===

Important:  The vagrant box now requires 8 GB RAM.  This mean you will
need at least 12 GB RAM to be able to do anything interesting.

This was tested with a manually downloaded VirtualBox 5.0.20->5.1.14
on Ubuntu 16.04 LTS->16.10 with OpenJDK 8 as java.

The version of Vagrant shipping with Ubuntu is too old even for 18.04 LTS. 
Install vagrant yourself from https://www.vagrantup.com/downloads.html.

Note:   Use the following command to get X11 forwarding (for e.g.
visualvm):

    vagrant ssh -- -Y
    
You must have the KB specific version of veraPDF-rest available
to Maven to build the box.   See /README.md for details.


Setting up the DOMS+Bitrepository vagrant box
===

Only needed once: Install the necessary vagrant plugins:
http://stackoverflow.com/a/28359455/4527948

    vagrant plugin install vagrant-timezone
    vagrant plugin install vagrant-scp

Invoke Maven to download the Bitrepository and DOMS distributions (as
they are stored in Nexus):

    mvn clean package

Start vagrant in any folder inside the project (using virtualbox provider):

    vagrant up

(may take 5-10 minutes and download quite a bit the first time).  

Now download Zulu OpenJDK 8, install Bit Repository and DOMS and run a
tiny web server on top of the Bit Repository file system so we can
retrieve the files stored:

    vagrant ssh -c "nohup bash -x /vagrant/install_bitrepository.sh; nohup bash -x /vagrant/install_doms.sh; nohup bash /vagrant/run-bitrepositorystub-webserver.sh; nohup bash /vagrant/run-verapdf-rest.sh"


Note that DOMS will take a while to initialize (SBOI and DOMS Wui Solr
have a lot to do).  The output from "nohup" is placed in the "nohup.out" file 
which can be followed with e.g.

    tail -F nohup.out

When finished the system is ready.  Note:  This is very brittle - if anything doesn't work as expected
there might be an error hiding in the log somewhere.  Download URL's tend to move around over time.

**You now have a local, empty DOMS and Bit repository**.

Create a snapshot to be able to easily revert to this point.

    vagrant snapshot save up

Do *not* use the `vagrant snapshot` command to restore snapshots.
Running programs and file system mappings are not properly handled.
Either use the VirtualBox GUI or the VBoxManage command to revert to
earlier snapshots.

Using the vagrant box from another machine:
===

* Ensure that the virtualbox instance uses bridged networking.
* Use the `bin/ssh-with-vagrant-port-forwarding.sh` script as inspiration for 
port forwarding from the development machine to the machine running the vagrant instance. 


Ingesting deliveries:
===

Not written yet.

Successful ingestion of a delivery is indicated with log lines on the
form `2016-07-12 ... PromptDomsIngesterComponent - success was: Worked
on ... successfully`

Updating Summa:
===

After adding or removing events in DOMS, the Summa index must be
updated for the autonomous components:

    vagrant ssh -c 'JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 bash -x 7880-doms/bin/doms.sh update'
    
Note that it may take several minutes before the index is ready.

Manually adding a file to Bit Repository
===

There is a sample command line client included with the bitrepository quickstart.  ssh inside the vagrant box
and invoke

    JAVA_HOME=$(echo $HOME/zulu8*-linux_x64/) bitrepository-quickstart/commandline/bin/bitmag.sh put-file -c dpaviser -f bitrepository-quickstart/commandline/logback.xml 

This will add the file "logback.xml".

After invoking "Start" for "CompleteIntegrityCheck" on
http://localhost:18080/bitrepository-webclient/integrity-service.html
"logback.xml" should be one of the files when clicking on the number
in the "Total number of files" column for each pillar.


Notes:
===

Do not use vagrant to stop/start/restore the vagrant box as the
scripts are not written for this.  Subtle errors will occur.

Links work both inside vagrant box and outside if the correct network
interface was chosed for bridging during "vagrant up" (depending on
host configuration vagrant may have asked).


Manually running the "restore the only currently running vagrant box to snapshot" looks similar to: (please improve)

    export V=$(VBoxManage list runningvms | cut -d\" -f2 | grep digital-pligtaflevering-aviser-tools_default); VBoxManage controlvm $V poweroff ; sleep 4 ; VBoxManage snapshot $V restore "XX"; VBoxManage startvm $V --type headless

Useful links:

1. See ingested PDF-files in DOMS:  http://localhost:7880/fedora/objects?pid=true&title=true&identifier=true&terms=*pdf&query=&maxResults=80
1. Access Fedora (fedoraAdmin/fedoraAdminPass): http://localhost:7880/fedora/objects
1. Access SBOI Solr: http://localhost:58608/newspapr/sbsolr/#/collection1/query
1. Access DOMS Wui Solr: http://localhost:58708/domswui/sbsolr/#/collection1/query
1. Local pid generator: http://localhost:7880/pidgenerator-service/rest/pids/generatePid


If for some reason Solr or Fedora is not running or responding, restart it inside vagrant with:

    ~/7880-doms/bin/doms.sh restart


VeraPdf in vagrant:
===
VeraPdf is running inside the vagrant image, but it often needs to get restarted

First login to running vagrant:
vagrant ssh

Then find running verapdf-process:
vagrant@vagrant:~$ ps aux | grep verapdf

Then kill the running process:
vagrant@vagrant:~$ kill <id>

Then start it again:
vagrant@vagrant:~$ export JAVA_HOME=/usr/lib/jvm/zulu-8-amd64
vagrant@vagrant:~$ /vagrant/run-verapdf-rest.sh 
