Developers Guide to DPA
===

This document contains information relevant to a developer who needs
to work on DPA.  

DPA requires a properly configured DOMS and Bitrepository.  The exact locations
are configured through properties provided to the main method.

DPA has been developed against a local installation
of DOMS+Bitrepository running in a VirtualBox image controlled by Vagrant. 
To get the full benefit a similar setup is recommended.  

Note:  You probably need at least 12 GB RAM to comfortably work with the Vagrant box
running.  


Java
--

Java 8 is required, as stream is used regularily.  Development has
been done primarily on OpenJDK 8/Oracle 8 JDK on Ubuntu 16.10.  It is
intended to be platform agnostic, and considered a bug if not.

Java 9 is *NOT* yet supported. 


Maven 
--

**Please notice that your local Maven settings must point to sbforge to
be able to resolve dependencies properly!**

DPA is a self-contained multi-module Maven project, which additionally
use a virtual machine with DOMS and a reference Bitrepository for
local development.  As of 2017-02-14 it is located under
"doms/doms-installer".  Please see the README.md there for further
information. 

This mean that all sources are available at once, and that all modules
have the same version.

Tomcat
---

As of 2017-04-06 it has been agreed that the configuration (which for
autonomous components is essentially loaded as a property file) is to
be provided as context parameters and avoid having a default value present
as Jens Henrik would like to be notified if a parameter is missing.  

TRA have done some initial work on getting IntelliJ to add this transparently
when deploying and not found an easy way, so for now use a "For DPA only" copy
of Tomcat when developing and add this snippet to 

    $CATALINA_BASE/conf/context.xml

just before the final `</Context>` line.  IntelliJ may require recreating the Run/Debug configuration
for Tomcat.

    <Parameter name="autonomous.sboi.url" value="http://localhost:58608/newspapr/sbsolr/"/>
    <Parameter name="doms.username" value="fedoraAdmin"/>
    <Parameter name="doms.password" value="fedoraAdminPass"/>
    <Parameter name="doms.pidgenerator.url" value="http://localhost:7880/pidgenerator-service"/>
    <Parameter name="doms.url" value="http://localhost:7880/fedora"/>
    <Parameter name="pageSize" value="10"/>
    <Parameter name="jvm.dumpheap" value="false"/>
    <Parameter name="bitrepository.ingester.baseurl" value="http://localhost:58709/"/>
    <Parameter name="autonomous.pastSuccessfulEvents" value="Data_Archived"/>
    <Parameter name="autonomous.oldEvents" value=""/>
    <Parameter name="autonomous.itemTypes" value="doms:ContentModel_DPARoundTrip"/>
    <Parameter name="autonomous.sboi.pageSize" value="100"/>
    <Parameter name="autonomous.futureEvents" value="XML_validated,Manually_stopped"/>
    <Parameter name="autonomous.thisEvent" value="XML_validated"/>
    <Parameter name="autonomous.component.fedoraRetries" value="10"/>
    <Parameter name="autonomous.component.fedoraDelayBetweenRetries" value="10"/>
    <Parameter name="doms.collection.pid" value="doms_sboi_dpaCollection"/>

The snippet is also present in dpa-dashboard/dashboard.xml which is packaged with the
war file in the deployment tarball.

(Note that the manual control web app written by MMJ has a embedded Jetty launcher running
as a normal Java application invoking `main(...)`).

Glassfish 4:
---

As of 2017-05-29 TRA found that for Glassfish 4 the simplest way to get the container configured, insert
the following in $GLASSFISH/domains/domain1/config/default-web.xml right after the `<web-app ...>` root tag.


    <context-param>
        <param-name>autonomous.sboi.url</param-name>
        <param-value>http://localhost:58608/newspapr/sbsolr/</param-value>
    </context-param>
    <context-param>
        <param-name>doms.username</param-name>
        <param-value>fedoraAdmin</param-value>
    </context-param>
    <context-param>
        <param-name>doms.password</param-name>
        <param-value>fedoraAdminPass</param-value>
    </context-param>
    <context-param>
        <param-name>doms.pidgenerator.url</param-name>
        <param-value>http://localhost:7880/pidgenerator-service</param-value>
    </context-param>
    <context-param>
        <param-name>doms.url</param-name>
        <param-value>http://localhost:7880/fedora</param-value>
    </context-param>
    <context-param>
        <param-name>pageSize</param-name>
        <param-value>10</param-value>
    </context-param>
    <context-param>
        <param-name>jvm.dumpheap</param-name>
        <param-value>false</param-value>
    </context-param>
    <context-param>
        <param-name>bitrepository.ingester.baseurl</param-name>
        <param-value>http://localhost:58709/</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.pastSuccessfulEvents</param-name>
        <param-value>Data_Archived</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.oldEvents</param-name>
        <param-value></param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.itemTypes</param-name>
        <param-value>doms:ContentModel_DPARoundTrip</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.sboi.pageSize</param-name>
        <param-value>100</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.futureEvents</param-name>
        <param-value>XML_validated,Manually_stopped</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.thisEvent</param-name>
        <param-value>XML_validated</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.component.fedoraRetries</param-name>
        <param-value>10</param-value>
    </context-param>
    <context-param>
        <param-name>autonomous.component.fedoraDelayBetweenRetries</param-name>
        <param-value>10</param-value>
    </context-param>
    <context-param>
        <param-name>doms.collection.pid</param-name>
        <param-value>doms_sboi_dpaCollection</param-value>
    </context-param>
    



IntelliJ
--

IntelliJ has been used as the primary IDE.  Use the usual File -> Open
on the root of the project to open it.   If some dependencies are
missing you most likely need to fix your `~/.m2/settings.xml` file.

See [tools/dpa-tools-ide-launchers](tools/dpa-tools-ide-launchers) 
project for launchers to be used inside IDE's to invoke the
autonomous components with the project resourcers (and the vagrant
image running).

The project should be IDE-agnostic and a pure Maven project.  If not, please fix as needed.  
Also do not put IDE specific files in the git repository.


Vagrant
---

TRA has worked with vagrant 1.8.6 downloaded directly from 
https://www.vagrantup.com/downloads.html - the version coming with 
Ubuntu was too old when the project began.

See [vagrant/README.md](vagrant/README.md) for further information.
