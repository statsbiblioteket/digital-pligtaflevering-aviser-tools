

How to run in dev environment
---
If you want to disable the singlesignon part, disable it in the web.xml
Disable CAS filter from:
<!--Start of CAS filter -->

to:
<!--End of CAS filter -->


Run this server starting "JettyRunner" in the testpackage


run config
---
It is possible to add a parameter to indicate that the application searches for validated deliveries

?validated=true

