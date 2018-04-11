The binary tarball sent to IT-Drift will have this structure:

    bin/
    conf/
    java-wrappers/
    libs/
    tomcat-apps/
    

The file layout in devel (dpaviser@achernar)/stage/production 
will be similar to:

    logs/
    services/bin
    services/conf
    services/java-wrappers/
    services/libs
    services/tomcat-apps
    services/webapps
    var/

The individual folders contain the following:

 
logs/
---

Common log directory.  A log file for each invocation of each autonomous component is written, with the
name of the component in the file name.  As of 2017-04-03 a garbage collection log is also written in order to 
do offline diagnosis of the memory usage pattern over time.

drift cleans old files from this folder on a regular basis.

services/bin
---
Handwritten shell scripts invoking the java wrappers including the necessary flock's.

services/conf
---
The contents of `conf/` from the binary tarball, modified by drift as necessary.

services/java-wrappers
---

The generated java-wrappers responsible for invoking the main methods with
the appropriate arguments.  Untouched by man.

services/libs
---
The Maven artifacts needed to run the main methods.

services/webapps
---

Tomcat configuration files and deployment descriptors containing configuration strings and a pointer to the appropriate war.

services/wars
---
The contents of `wars/` from the binary tarball.

tomcat/conf/cacerts
---
The certificate from the tarball - services/wars/cacert

var/
---
Area for persisting information between runs.  Those autonomous components needing this must explicitly be told where it 
is through the appropriate configuration parameter.  
As of 2017-04-03 the only component needing this is the create delivery and dpa-manualcontrol.


/tra 2017-04-03


------------------------------------------------------------

The bin/* scripts are launchable from any directory.

Configuration files in conf/ are present on the classpath.

It is strongly suggested that relative file paths are
used for files to be written, and that the current
working directory is set to be the location where the
files are to be written before invoking a launcher script.

Examples could be log files, crash dump files, JVM diagnostic files etc.

So something like:

    cd ..../logs-for-foo
    ..../bin/foo ....


/tra 2016-08-26
