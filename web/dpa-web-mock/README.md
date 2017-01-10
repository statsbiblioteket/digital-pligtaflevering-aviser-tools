Development have been done with deploying to a Tomcat 8.5.9 server in
IntelliJ 2016.2.5.

Instructions on how to set up a Tomcat server in IntelliJ

https://intellij-support.jetbrains.com/hc/en-us/community/posts/206225249-Setup-multi-module-maven-tomcat-project-#5466173

Crucial part is to set "On frame deactivation" to "Update classes and resources"
as this mean every time you switch to another application IntelliJ ensures the
classes and JSP-pages have been updated.  In other words, switch to the browser and perhaps wait a fraction of a second and reload the page.

/tra 2017-01-10

