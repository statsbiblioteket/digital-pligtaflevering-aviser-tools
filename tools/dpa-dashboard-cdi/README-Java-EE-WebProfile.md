dpa-dashboard-cdi is the dashboard for interacting with DOMS for DPA.

It is a Java EE 6 WebProfile application (servlet 3.0) which use CDI to
inject configuration strings.    It has been tested with Glassfish 4 and
TomEE 7.0.2.

Launch it, to have a browser window opened on the Dashboard
application.  Default is http://localhost:8080/

For Tomcat deployments it is necessary to enclose Weld (the reference CDI engine) in the
WAR file.  This is being done in the dpa-manualcontrol module

/tra 2018-02-11

