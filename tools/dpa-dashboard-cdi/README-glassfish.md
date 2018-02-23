In order to provide configuration strings to the web application as
init parameters, it is necessary to configure Glassfish.

As Glassfish apparently does not support something like Tomcats context.xml, we need to do it another way.

There are two approaches:

* `default-web.xml` for the domain
* `asadmin set-web-context-param`

The easiest long-term solution is to create a Glassfish domain for each deployment and set the default context parameters in each.  
For ad-hoc modifications use asadmin.


default-web.xml
---

The `glassfish/domains/DOMAIN/config/default-web.xml` file contains the
default web configuration for the domain.  This includes context parameters.

Use 

    bin/glassfish-default-web-xml-snippet-from-properties.pl < ~/DOMAIN.config
    
to generate a XML snippet of `<context-param>` tags to copy into the default-web.xml file 
under the `<web-app>` tag.   Restart the container for the changes to
apply.




asadmin set-web-context-param
---  


A running Glassfish domain can be reconfigured with asadmin on the form:

    bin/asadmin set-web-context-param --name=doms.username --value=DOMSUSERNAME dpa-dashboard-cdi-master-SNAPSHOT
    
The application must be running, and - as the configuration map is only read on startup - redeployed.  The
reconfiguration is done in the application deployment but not the server.

The bin/glassfish-set-web-context-params-from-properties.sh script reads in a property file and generates
shell commands which can be fed directly into bash.

Sample invocations:

    sh bin/glassfish-set-web-context-params-from-properties.sh  < tools/dpa-tools-ide-launchers/src/main/resources/statistics-vagrant.properties | sh -

The glassfish "asadmin" command must be in PATH.  This can be added on the command line like

    export PATH=$PATH:$HOME/Hentet/glassfish5/bin

TRA note:

    cat tools/dpa-tools-ide-launchers/src/main/resources/statistics-vagrant.properties ~/dpa-mirzam.config| sh bin/glassfish-set-web-context-params-from-properties.sh| PATH=$PATH:$HOME/Hentet/glassfish5/bin sh -
    cat tools/dpa-tools-ide-launchers/src/main/resources/statistics-vagrant.properties ~/dpa-aldebaran.config| sh bin/glassfish-set-web-context-params-from-properties.sh| PATH=$PATH:$HOME/Hentet/glassfish5/bin sh -



/tra 2018-02-15

