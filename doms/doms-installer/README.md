Before starting:
===

This was tested with a manually downloaded VirtualBox 5.0.20 on Ubuntu 16.04 LTS with OpenJDK 8 as java.

The version of Vagrant shipping with Ubuntu 16.04 is 1.7.4.  Snapshot functionality in vagrant itself requires
1.8.1.  If you need this install vagrant yourself from https://www.vagrantup.com/downloads.html.


Note:  For now, use the VirtualBox GUI to restore snapshots.  "bootstrap.sh" is not properly
written to reprovision the virtual machine as invoked with "vagrant snapshot restore".

Outside vagrant:
===

After cloning checkout devel-config using:

    git -C src/vagrant/ clone -b TRA ssh://git@sbprojects.statsbiblioteket.dk:7999/avis/devel-config.git

Create artifacts to deploy:

    mvn clean install

Install the necessary vagrant plugins: http://stackoverflow.com/a/28359455/4527948

    vagrant plugin install vagrant-timezone
    vagrant plugin install vagrant-scp

Start vagrant (using virtualbox provider):

    cd src/vagrant
    vagrant up


Before including modified bitrepository build the version which supports hashes and slashes in filenames for ingest
    FIXME: This is not needed any more when this version of bitrepository has been released
    checkout "git@github.com:Madsmj/reference.git -b filenamesContainingHashcharacter" and run "mvn clean install"

(may take 5-10 minutes and download quite a bit the first time).

    vagrant ssh -c "nohup bash -x /vagrant/install_bitrepository.sh; nohup bash -x /vagrant/install_doms.sh; nohup bash -x /vagrant/setup-newspapers.sh; nohup bash /vagrant/run-bitrepositorystub-webserver.sh"

SBOI and DOMS Wui Solr will take a while to initialize.  
The output from "nohup" is placed in the "nohup.out" file 
which can be followed with e.g.

    tail -F nohup.out

Check the URLs below to see when they are ready and responsive.

**You now have a local, empty DOMS and Bit repository**.

Create a snapshot to be able to easily revert to this point.

    vagrant snapshot save up

Do not use the VirtualBox gui to save and restore snapshots.  The
file system mappings will not be properly handled.


Adding batches placed inside vagrant:
===

Use something along these lines for copying in an existing, unpacked delivery into `/newspapr_batches` 
_inside_ the vagrant machine: (links valid for PC591)
 
1. Two sample batches from LLO:

    `vagrant scp ~/ownCloud/2016-02-29/llo/standard\ pakker\ til\ repo/avis/Fjerritslev\ avis/. /newspapr_batches`

2. Copy in digital-pligtaflevering-aviser-tools sample delivery:

    `vagrant scp ~/git/digital-pligtaflevering-aviser-tools/delivery-samples/B20160811-RT1 /newspapr_batches/`
    
After changes update SBIO index, and run each of the autonomous components:

    vagrant ssh -c /vagrant/newspapers_poll.sh

This command invokes several batch scripts to ensure all the work has been done.
It is very important to do so, as the index must be up to date for the DOMS
query routines to see any updates.  Repeat after each change to DOMS data.

Repeat several times until both batches have been ingested.  This is indicated with log
lines on the form
`2016-07-12 ... PromptDomsIngesterComponent - result was: Worked on ... successfully`


Adding batches placed outside vagrant: 
===

This is typical when developing code.

1. Ensure vagrant machine is running.

2. Invoke Java code inside e.g. IntelliJ adding events to DOMS.
 
3. Update SBOI to ensure that search indexes are up to date.

.

    vagrant ssh -c 'JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 bash -x 7880-doms/bin/doms.sh update'
    
    
start process to keep solr updated: 
===    

There is placed a script for updating solr every 3 minutes, this can be started by logging into the Vagrant-image and running the following script 

./doms_updater.sh

Manually adding a file to Bitrepository
===

There is a sample command line client included with the bitrepository quickstart.  ssh inside the vagrant box
and invoke

    JAVA_HOME=$(echo $HOME/zulu8*-linux_x64/) bitrepository-quickstart/commandline/bin/bitmag.sh put-file -c dpaviser -f bitrepository-quickstart/commandline/logback.xml 

This will add the file "logback.xml".

After invoking "Start" for "CompleteIntegrityCheck" on  http://localhost:8080/bitrepository-webclient/integrity-service.html
"logback.xml" should be one of the files when clicking on the number in the "Total number of files" column for each pillar.







Notes:
===

Use

    vagrant suspend

to stop working instead of "vagrant halt" as efforts have not yet been
done to ensure production quality of this image.


Links work both inside vagrant box and outside if the correct network interface
was chosed for bridging during "vagrant up" (depending on host configuration
vagrant may have asked).

Access Fedora (fedoraAdmin/fedoraAdminPass):

    http://localhost:7880/fedora/objects

Access SBOI Solr:

    http://localhost:58608/newspapr/sbsolr/#/collection1/query

Access DOMS Wui Solr:

    http://localhost:58708/domswui/sbsolr/#/collection1/query

Local pid generator:

    http://localhost:7880/pidgenerator-service/rest/pids/generatePid

If for some reason Solr or Fedora is not running or responding, restart it inside vagrant with:

    ~/7880-doms/bin/doms.sh restart


Manually running the "restore the only currently running vagrant box to snapshot" looks similar to: (please improve)

    export V=$(VBoxManage list runningvms | cut -d\" -f2 | grep vagrant); VBoxManage controlvm $V poweroff ; sleep 4 ; VBoxManage snapshot $V restore "doms ingested"; VBoxManage startvm $V --type headless


