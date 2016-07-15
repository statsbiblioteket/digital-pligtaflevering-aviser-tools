# digital-pligtaflevering-aviser-tools

Digital Pligtaflevering af Aviser (DPA), autonomous preservation tools.

## Background

All Danish newspapers published are collected in physical form
in two copies and preserved by Statens Avissamling (https://www.statsbiblioteket.dk/nationalbibliotek/adgang-til-samlingerne/aviser/StatensAvissamling).  
The traditional procedure has then been microfilming the newspapers and make the
microfilms available to the general public.   A separate project - https://blog.avisdigitalisering.dk/ - has digitized the
old microfilms and made them available through http://www2.statsbiblioteket.dk/mediestream/avis 
We now want to start collecting new newspapers as PDF's instead.  This is what DPA is about, and
is based on the experiences gained.

## Minimal Effort Ingest

The traditional mindset for digital preservation is to do all the work for a given item, collecting metadata, normalizing
files, ensuring consistency and so on, and THEN store the result for eternity.  We have found that the amount of work
necessary to properly handle a given collection is rather large, regardless of the size of the collection, which mean
that especially small collections are delayed much longer than originally expected.  

Our conclusion has been that it is more important to get the collections preserved as they are and then doing the rest
when we have the time, than getting everything done and tying a bow on the virtual box before being put on the digital shelf.

Steps relevant for PDF's could be:

* _Ingest_ PDF's (preserve them in Bitmagasinet)
* Analyse if the PDF can be rendered to a series of images, one per page.
* Check if the PDF is suitable for digital preservation.  An example could be if all fonts and images are present 
inside the PDF
or if any need to be retrieved from the internet.  Such a resource may go away at any time, making the PDF incomplete. 
If we receive such PDF's a human must take action.
* Create reports on deliveries to get an overview of how the process is going.

The important part is that after the initial ingest the steps can be taken whenever the necessary resources are available,
or re-taken if found necessary.   New steps can also be added and run as appropriate at any time later.

This only modifies the OASIS model of digital preservation slightly.

## Design

At SB (Statsbiblioteket - _State and University Library_) we work with two backend systems:

1. "DOMS" (_Digital Object Management System_) - our homegrown metadata repository based on Fedora Commons 3 (not the 
operating system) for metadata storage in RDF form and Summa for searching.
2. "Bitmagasinet" (_Bit Repository_) - our long term bit preservation system in collaboration with several other Danish 
institutions which is where the actual files are stored.   
https://sbforge.org/display/BITMAG/The+Bit+Repository+project

All information _about_ the files is stored in DOMS.  All the _actual_ files are stored in Bitmagasinet,
and - based on a discussion TRA had with KTC and ABR on 2016-07-14 - 
we need to utilize the SB-specific implementation of the Bitmagasinet-pillar to access the physical
files efficiently similar to what has been done in Avisprojektet.

The steps taken so far on a given PDF is stored as metadata in the form of PREMIS events.  

Each step taken is implemented as a traditional stand-alone Java application which regularily asks DOMS through Summa
if there is any PDF's ready to be worked on, and if any, work on those.  The result of the work is added as an event and/or
a datastream if appropriate for later.

The workflow a PDF must pass
through is defined by configuring the steps so if step B must run after step A, then step B looks for the PREMIS event
that A has been run successfully.  This implicit assembly line way of thinking allows us to avoid a full blown 
centralized workflow engine and keep the complexity lower.

## Invocation

As of 2016-07-14 the only runnable code is the Main test in dpa-doms.
TRA reached the milestone of actually storing VeraPDF output in the
event and in the "VERAPDF" datastream today.  Note that both the doms-installer
vagrant machine and the VeraPDF rest server must be running for the
Main test to complete.

Plan from here is slowly migrating the Main test to the dpa-harness
module while building a Dependency Injection aware harness utilizing
Dagger 2 injecting all the needed constant strings as well as joining
the various components correctly.  When that work is done we should
be able to flesh out much better how the deliveries will look
and switch to work on one of those.




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

The default DOMS content created contains two newspaper batches that is triggered
by the search criteria, and is simulated to be run on dummy PDF files stored
as test resources.  VeraPDF is invoked as a REST service, and the output stored
in the event and as a datastream named VERAPDF on the newspaper roundtrip object.
