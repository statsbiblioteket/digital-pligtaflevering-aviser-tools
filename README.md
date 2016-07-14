# digital-pligtaflevering-aviser-tools

Digital Pligtaflevering af Aviser, autonomous preservation tools.

# Flow

Summarized discussion with KFC 2016-07-01:

_The overall "keep running autonomous components on suitable items" approach
is only valid as long as no problems has been found.  If a problem is found,
the item is marked with a special event indicating "manual triage necessary"
which prohibits further automatic processing until a human has investigated.
What happens then will be fleshed out later._

After ingest, we run VeraPDF with the least restrictive profile on each PDF.
The output will be similar to:

    {"totalAssertions":250,"pdfaflavour":"PDFA_1_B","testAssertions":[],"compliant":true}

For failures the `testAssertions` list will contain details.  This output will
be stored as metadata for the PDF-file, and an "veraPDF has been run" event added.

Another autonomous component is responsible for analyzing the VeraPDF output (in
order to decouple the invocation - which might be expensive - from the analysis).
The "Digital Bevaringsgruppe" has a list of which issues to consider important.

If the PDF does not have any of these, it is given the "verapdf approved" event
and we are done.

If it does, then the "manual triage necessary" event is added, and the PDF must
be triaged by a human before automatic processing can continue.

# Manual triaging

If a "manual triage necessary" event is present on a PDF item in DOMS, a human
need to decide what needs to be done for the item.  (Tooling for this needs to be discussed)

An autonomous component may not work on the item until this has been done.

The human may find one of (currently) three things:

1. The PDF is considered acceptable as is.  The "verapdf approved" event is added manually,
overriding "manual triage necessary" allowing automatic processing to continue.

2. The PDF is found to require code alterations in order to be processed correctly.
This requires involving developers and releasing a new version.

3. The PDF is found to be unusable.  The human initiates actions that procures a new
and more usable PDF to replace the current file.  As this is expected to be a very rare situation,
the exact steps to take are currently undefined.






# VeraPDF

As of 2016-06-30 the initial prototype uses the VeraPDF REST interface.

With the current state of the VeraPDF sources the easiest way
to get an executable jar is:

Go to the latest build of the rest service on the Open Preservation jenkins:

[http://jenkins.openpreservation.org/job/veraPDF-rest/lastBuild/org.verapdf$verapdf-rest/](http://jenkins.openpreservation.org/job/veraPDF-rest/lastBuild/org.verapdf$verapdf-rest/)

Download the jar file, e.g. verapdf-rest-0.1.22.jar.

Run it manually with

    java -jar verapdf-rest-0.1.22.jar server

By default it listens on port 8000.

# DOMS

From doms-installer/README.md: Access Fedora (fedoraAdmin/fedoraAdminPass):

[http://localhost:7880/fedora/objects](http://localhost:7880/fedora/objects)


