
Weld is now included in the war file so CDI will work in Tomcat 8+ (as
opposed to before where it was in any CDI enabled container).

For local development, download and unpack a suitable Apache Tomcat
8.5 distribution.  Copy `context.xml` into the Tomcat configuration
directory (e.g. apache-tomcat-8.5.13/conf) to provide system wide
configuration parameters pointing to local vagrant instance.

For IntelliJ with the Tomcat plugin:

* Use `Run -> Edit Configurations` to open the Run/Debug
  Configurations panel.
* Use the green `+` to add a new configuration.
* Select `Tomcat Server -> Local` to create an Unnamed configuration.
* Change the name, and `Configure` the application server to the
  download above.  Strongly consider using another port than 8080!
  Click `Fix` to add "dpa-dashboard:exploded" to the deployed
  artifacts.  (Tip: On the Server pane, locate "On frame deactivation"
  and set it to "Update classes and resources" to have changes
  deployed when switching to the browser).  `Ok` saves the
  configuration.  

Launch it, to have a browser window opened on the Dashboard
application.  Default is http://localhost:8080/

For deployment the necessary magic is present in the dpa-dashboard.xml file.  
Default values are for achernar installation. Change as necessary.

/tra 2018-01-25
