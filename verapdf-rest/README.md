VeraPDF is used to analyze the contents of the PDF files ingested.

Due to various problems it needs to run in a separate JVM.  See https://sbprojects.statsbiblioteket.dk/jira/browse/DPA-124
for details.  It was found that the easiest way to get a production ready 
REST service was to improve on https://github.com/veraPDF/veraPDF-rest.

Currently it resides in https://github.com/statsbiblioteket/veraPDF-rest until
the changes are merged back upstream.




