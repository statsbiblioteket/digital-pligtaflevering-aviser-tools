Developers Guide to DPA
===

This document contains information relevant to a developer who needs
to work on DPA.  

DPA requires a properly configured DOMS and Bitrepository.  The exact locations
are configured through properties provided to the main method.

DPA has been developed against a local installation
of DOMS+Bitrepository running in a VirtualBox image controlled by Vagrant. 
To get the full benefit a similar setup is recommended.  

Note:  You probably need at least 12 GB RAM to comfortably work with the Vagrant box
running.  


Java
--

Java 8 is required, as stream is used regularily.  Development has
been done primarily on OpenJDK 8/Oracle 8 JDK on Ubuntu 16.10.  It is
intended to be platform agnostic, and considered a bug if not.

Maven 
--

**Please notice that your local Maven settings must point to sbforge to
be able to resolve dependencies properly!**

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

See [tools/dpa-tools-ide-launchers](tools/dpa-tools-ide-launchers) 
project for launchers to be used inside IDE's to invoke the
autonomous components with the project resourcers (and the vagrant
image running).

The project should be IDE-agnostic and a pure Maven project.  If not, please fix as needed.  
Also do not put IDE specific files in the git repository.


Vagrant
---

TRA has worked with vagrant 1.8.6 downloaded directly from 
https://www.vagrantup.com/downloads.html - the version coming with 
Ubuntu was too old when the project began.

See [vagrant/README.md](vagrant/README.md) for further information.
