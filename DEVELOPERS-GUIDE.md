Developers Guide to DPA
===

This document contains information relevant to a developer who needs
to work on DPA.

Java
--

Java 8 is required, as stream is used regularily.  Development has
been done primarily on OpenJDK 8/Oracle 8 JDK on Ubuntu 16.10.  It is
intended to be platform agnostic, and considered a bug if not.

Maven 
--

Please notice that your local Maven settings must point to sbforge to
be able to resolve dependencies properly!

DPA is a self-contained multi-module Maven project, which additionally
use a virtual machine with DOMS and a reference Bitrepository for
local development.  As of 2017-02-14 it is located under
"doms/doms-installer".  Please see the README.md there for further
information. 

This mean that all sources are available at once, and that all modules
have the same version.

IntelliJ
--

IntelliJ has been used as the primary IDE.  Use the usual File -> Open
on the root of the project to open it.   If some dependencies are
missing you most likely need to fix your `~/.m2/settings.xml` file.

See the <a
href="tools/dpa-tools-ide-launchers">`dpa-tools-ide-launchers`
project</a> for launchers to be used inside IDE's to invoke the
autonomous components with the project resourcers (and the vagrant
image running).


Deployment
--

`dpa-tools` has the main methods for the autonomous components.
`dpa-tools-deployment` has the "generate shell scripts and create
bundle" functionality.

See <a
href="tools/dpa-tools-ide-launchers/README.md">tools/dpa-tools-ide-launchers/README.md</a>
for details.



