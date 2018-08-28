## Essentials

The bin/* scripts are launchable from any directory.

Configuration files in conf/ are present on the classpath.

It is strongly suggested that relative file paths are
used for files to be written, and that the current
working directory is set to be the location where the
files are to be written before invoking a launcher script.

Examples could be log files, crash dump files, JVM diagnostic files etc.

So something like:

    cd ..../logs-for-foo
    ..../bin/foo ....


/del_2016-08-26

## Deployment

After compilation 

* Copy the generated tarball to `dpaviser@achernar`:

As `dpaviser@achernar`

Simple redeployment can be done without stopping Tomcat.  Just unpack the tarball to `services/`
and let Tomcat detect the redeployment.  This requires a changed datestamp
on the war file.

Complex redeployment requires stopping Tomcat.

* Stop tomcat with `tomcat/sbbin/tomcat-init.sh stop`
* Unpack the generated tarball to `services/`
* If necessary, modify the Tomcat context descriptor files in services/tomcat-apps
* Start tomcat with `tomcat/sbbin/tomcat-init.sh start` 

As of 2017-04-05 the initial start link is at

http://achernar:9021/dpa-dashboard/listStates.jsp


