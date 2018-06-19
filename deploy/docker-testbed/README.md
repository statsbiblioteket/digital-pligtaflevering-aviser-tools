In order to test the autonomous components locally, this Maven project
extracts the deployment tar ball to a docker container with a time shortened
crontab and runs the jobs in cron.

## Vagrant image

It is expected that the host provides suitable services (or port
forwarding) to DOMS.  It is also expected that external links to
bitrepository files are functional _without_ file mapping (the URL
in DOMS must point to the file).  

This is valid for the Vagrant image.  Follow the instructions to create 
a valid instance, and then run the following shell command to update the search indexes every 
45 seconds.

    while true; do date; vagrant ssh -c 'JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 bash 7880-doms/bin/doms.sh update'; sleep 45; done


## Getting ready

First do a full `mvn clean install` on the whole project.  This stores the necessary
artifacts in the local Maven repository.

Run

    prepare.sh
    
to unpack the deployment tarball and bitrepository configuration files 
into the "target/for-docker" directory.

Run

    docker-compose up --build
    
to launch cron.


/tra 2018-06-18

