
Weld is now included in the war file so CDI will work in Tomcat 8+
(as opposed to before where it was in any CDI enabled container).

`context.xml` can be copied into the Tomcat configuration directory during development
(e.g. apache-tomcat-8.5.13/conf) to provide system wide configuration parameters. 


For deployment the necessary magic is present in the dpa-dashboard.xml file.

/tra 2017-11-28
