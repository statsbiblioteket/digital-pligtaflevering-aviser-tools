This folder contains WAR files for deployment
along with Tomcat configuration XML files.

Note that DPA web applications as of 2017-04-18 expect the web container
to provide configuration values through the
ServletContext.getInitParameterNames() and 
ServletContext.getInitParameter(name) methods.  

These must be defined outside the web application, usually
in the context descriptor or inside the web container itself.  
For local development, copy the init parameters from the
XML file here to $CATALINA/conf/context.xml and restart Tomcat.



