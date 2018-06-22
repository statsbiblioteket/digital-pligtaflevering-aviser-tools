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
    
to launch cron which then runs the autonomous components once every minute.  

As of 2018-06-22 each invocation of an autonomous component writes its own log file 
to /root/logs.   A quick overview of how things go can be monitored in a separate 
shell with

    docker-compose exec dpa-cron watch grep 'Result: ' '/root/logs/*'

A small font will be useful.


## Helpful tips

Note:  To get telnet (busybox applet) and possibly others in the docker image, exec into it and run

    apk add busybox-extras



/tra 2018-06-21

