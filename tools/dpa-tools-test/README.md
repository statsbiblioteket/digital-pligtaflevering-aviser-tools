NOTE:  THE TESTS IN THIS MODULE ARE NOT _REAL_ TESTS
BUT INDIVIDUAL LAUNCHER CONFIGURATIONS DURING DEVELOPMENT!

THIS IS WHY THE TESTS ARE DISABLED IN pom.xml!  DO NOT
REENABLE!

This used to be the src/test tree in the dpa-tools module
but due to Curator requiring Guava 11.0 and Dagger 2 compiler
requiring Guava 19.0 causing Maven to put Guava 19 on the
runtime class path during testing TRA found it easier to separate the two steps
into separate Maven modules.  This may also make it easier 
to disable "launchers disguised as tests" from the actual build.


If Curator is lifted upstream from Netflix Curator to Apache Curator
(which includes a package change) it might be possible to remerge.


