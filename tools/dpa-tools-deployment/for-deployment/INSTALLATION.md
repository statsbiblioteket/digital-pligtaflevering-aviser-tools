Installation of DPA:
===

Installation guide for Digital Pligtaflevering af Aviser.

Prerequisite software:
---

1) Oracle JDK installed and JAVA_HOME environment variable set appropriately.

Configuration of tomcat:
---
1) Add file setenv.sh to /tomcat/bin

2) Configure link to certificate: export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/home/dpaviser/tomcat/conf/cacerts/cacert"

3) Configure configuration-files in /tomcat/conf/Catalina/localhost
  - Including dpa-manualcontrol, where the following parameters should replace localhost with current url
     - "serverName"
     - "service"


Add folders and giwe write acess
---
/home/dpaviser/var/locks
/home/dpaviser/var/dpa-manualcontrol
/home/dpaviser/logs
...




