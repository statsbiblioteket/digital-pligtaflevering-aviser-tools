Providing configuration values to Java code
===

Experience has shown that it is a good thing to separate code from
configuration.  

Unfortunately this is frequently hard to do right in Java.
Deciding how to 
provide configuration parameters to Java code is not easy.  For most purposes
configuration can be provided as a Key-Value map (Java type `Map<String, String>`).
Typically the simplest way to code this is to provide the key where the
value is needed and then have a more or less magical lookup mechanism.

Java application (with a `main`)
--

Read in property file(s) and convert the properties to the configuration map.

Either make the configuration map globally available or use a Dependency Injection mechanism 
like Dagger2 to do the necessary magic.  

The autonomous components in the DPA project analyse the argument string array passed in
to the main method.  The first argument is the filename of the property file to read in.  The
remaining arguments are on the form `key=value` which take effect over the properties read in.



Tomcat WAR deployment configuration using InitParms.
-- 

At KB Aarhus we use a slightly customized Tomcat distribution.  The instructions here
should be generic.


Instead of just copying the WAR file to the Tomcat `webapps` directory for hot deployment
(or equivalent throught the administrator web interface), we use a context xml file which typically
is deployed along the WAR file outside the Tomcat directory.  

A sample context.xml file (an abridged version of `dpa-dashboard.xml`): 

```
<?xml version='1.0' encoding='utf-8'?>
<Context docBase="${user.home}/services/tomcat-apps/dpa-dashboard.war">
  <Parameter name="autonomous.sboi.url" value="http://localhost:58608/newspapr/sbsolr/"/>
  <Parameter name="doms.pidgenerator.url" value="http://localhost:7880/pidgenerator-service"/>
  <Parameter name="doms.url" value="http://localhost:7880/fedora"/>
  <Parameter name="pageSize" value="10"/>
  <Parameter name="jvm.dumpheap" value="false"/>
</Context>
```

There are three important things:

1. `<Context docBase="...` points to the absolute physical location of the WAR file.  Tomcat expands `${user.home}`.  
2. Multiple `<Parameter name="key" value="value"/>` lines provide one configuration key-value pair each.
3.  This is a completely standard Tomcat deployment descriptor, so there is no restrictions on what can go in here if needed by the application.
 

The programmer packages a version of this file as suitable for going on the programmer-controlled test system.  During
downstream deployment to stage and production, the values are replaced with the values
suitable for the target system.  This typically mean that URL's point at different servers, which
require different user names and passwords.  

 
The deployer then makes a symbolic link from `conf/Catalina/localhost` inside the Tomcat directory
to the actual location of the deployment descriptor.  This is also why the full path of
the WAR file must be put in there.  This needs to be done only once.  Tomcat redeploys if the WAR
file updates.


```
[dpaviser@achernar localhost]$ pwd
/home/dpaviser/tomcat/conf/Catalina/localhost
[dpaviser@achernar localhost]$ ls -l
totalt 0
lrwxrwxrwx 1 dpaviser dpaviser 53  5 apr 16:46 dpa-dashboard.xml -> /home/dpaviser/services/tomcat-apps/dpa-dashboard.xml
[dpaviser@achernar localhost]$ 

```

From Java code the configuration map can then be constructed along the lines of:

```
    public static Map<String, String> getInitParameterMap(ServletContext servletContext) {
        Map<String, String> map = new HashMap<>();
        for(String name : Collections.list(servletContext.getInitParameterNames())) {
            map.put(name, servletContext.getInitParameter(name));
        }
        return map;
    }
    
```

and used from there.

Jetty WAR deployment configuration using InitParms
---

To be written.


`mvn jetty:run`
---

To be written.

(It appears that according to http://www.eclipse.org/jetty/documentation/9.4.x/jetty-maven-plugin.html#jetty-run-goal the jettyEnvXml attribute
can be used to indicate the location of a `jetty-env.xml` file which contains java-as-xml which
then can reconfigure the server.  A sample is needed)

``

