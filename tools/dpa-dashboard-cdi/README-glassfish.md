In order to provide configuration strings to the web application as
init parameters, it is necessary to configure Glassfish.

As Glassfish apparently does not support something like Tomcats context.xml, we need to do it another way.

Glassfish itself can be reconfigured with on the form:

    bin/asadmin set-web-context-param --name=doms.username --value=DOMSUSERNAME dpa-dashboard-cdi-master-SNAPSHOT
    
The application must be running, and - as the configuration map is only read on startup - redeployed.  The
reconfiguration is done in the application deployment but not the server.

The bin/glassfish-set-web-context-params-from-properties.sh script reads in a property file and generates
shell commands which can be fed directly into bash.

Sample invocation:

    sh bin/glassfish-set-web-context-params-from-properties.sh  < tools/dpa-tools-ide-launchers/src/main/resources/statistics-vagrant.properties | sh -

The glassfish "asadmin" command must be in PATH.  This can be added on the command line like

    export PATH=$PATH:$HOME/Hentet/glassfish5/bin


/tra 2018-02-15

