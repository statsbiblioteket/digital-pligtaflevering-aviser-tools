docker-testbed
===

In order to test the autonomous components locally, this Maven project
extracts the deployment tar ball to a docker container with a time shortened
crontab and runs the jobs in cron.

## Vagrant image

It is expected that the host provides suitable services (or port
forwarding) to DOMS.  It is also expected that external links to
bitrepository files are functional _without_ file mapping (the URL
in DOMS must point to the file).  

This command line is valid for the Vagrant image.  Follow the instructions to create 
a valid instance, and then run the following shell command to update the search indexes every 
45 seconds.

    while true; do date; vagrant ssh -c 'JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 bash 7880-doms/bin/doms.sh update'; sleep 45; done


## Getting ready

First do a full `mvn clean install` on the whole project as described in the top README.md.  
This stores the necessary artifacts in the local Maven repository.  By default, the three
sample deliveries in /delivery-samples are processed - actual deliveries can be copied into
/delivery-samples before starting docker to create an actual workflow.

Run

    prepare.sh
    
to unpack the deployment tarball and bitrepository configuration files 
into the "target/for-docker" directory.

Run

    docker-compose up --build
    
to launch cron which then runs the autonomous components once every minute.  When the following
is printed (with other uuid's) all sample deliveries are ready for deletion:

    dpa-cron_1  | crond: wakeup dt=10
    dpa-cron_1  | dl_20160913_rt1	uuid:b83798a5-fd33-4565-aeef-b59e76c84163
    dpa-cron_1  | dl_20160811_rt1	uuid:4d457830-7f86-440b-805d-d3d0626209eb
    dpa-cron_1  | crond: wakeup dt=10

Tip: Use

    docker-compose exec dpa-cron /bin/sh
    
in another window to get a shell.

As of 2018-06-22 each invocation of an autonomous component writes its own log file 
to /root/logs.   A quick overview of how things go can be monitored in a separate 
shell with

    docker-compose exec dpa-cron watch grep 'Result: ' '/root/logs/*'

A small font will be useful.


## Helpful tips

Note:  To get telnet (busybox applet) and possibly others in the docker image, exec into it and run

    apk add busybox-extras



/tra 2018-06-21

