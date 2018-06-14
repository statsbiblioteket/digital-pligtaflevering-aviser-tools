In order to test the autonomous components locally, this Maven project
extracts the deployment tar ball to a docker container with a time shortened
crontab and runs the jobs in cron.

It is expected that the host provides suitable services (or port
forwarding) to DOMS.  It is also expected that external links to
bitrepository files are functional _without_ file mapping (the URL
in DOMS must point to the file).  This is valid for the Vagrant image.


Run

    prepare.sh
    
to download and unpack the deployment tarball into the "target/for-docker" directory.

Run

    run.sh

to launch docker correctly

/tra 2018-05-07

