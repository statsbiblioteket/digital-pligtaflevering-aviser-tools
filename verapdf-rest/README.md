VeraPDF is used to analyze the contents of the PDF files ingested.

Due to various problems it needs to run in a separate JVM.  See https://sbprojects.statsbiblioteket.dk/jira/browse/DPA-124
for details.  It was found that the easiest way to get a production ready 
REST service was to improve on https://github.com/veraPDF/veraPDF-rest.

Currently it resides in https://github.com/statsbiblioteket/veraPDF-rest until
the changes are merged back upstream.

Ensure the artifact is available to Maven (e.g. installed in the local repository) and then use:

    mvn -B -q dependency:copy -Dartifact=org.verapdf:verapdf-rest:0.2.0-SNAPSHOT -DoutputDirectory=. -Dmdep.stripClassifier -Dmdep.stripVersion 

to download to verapdf-rest.jar to the current directory.  

When deployed use an invocation similar to:

    java -Ddw.server.applicationConnectors[0].port=8090 -Ddw.server.adminConnectors[0].port=8091 -jar vera-rest.jar > logfile

in a restarting wrapper.
