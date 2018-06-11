dpa-dashboard-cdi is the dashboard for interacting with DOMS for DPA.

It is a Java EE 7 WebProfile application (servlet 3.1, which is the
newest supported by Tomcat 8.5) which use CDI to inject configuration
strings.  It has been tested with Glassfish 4 + 5, and TomEE 7.0.4
webprofile.  

TRA's personal preference is developing against Glassfish.

Launch it, to have a browser window opened on the Dashboard
application.  Default is http://localhost:8080/

For Tomcat 8.5 deployments it is necessary to enclose Weld (the
reference CDI engine) in the WAR file.  This is being done in the
dpa-dashboard module using a Maven overlay, to keep this simple.

/tra 2018-02-11

