

    <context-param>
        <description>Deploy status</description>
        <param-name>productionMode</param-name>
        <param-value>false</param-value>
    </context-param>
    
    
    mvn idea:idea -DdeploymentDescriptorFile=src/webapp/WEB-INF/web.xml



Run this command to start the server

tools/dpa-dmanualcontrol$ mvn package jetty:run


run config
---
It is possible to add a parameter to indicate that the application searches for validated deliveries

?validated=true

