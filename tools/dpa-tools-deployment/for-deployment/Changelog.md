Changelog:
===

2018-05-03:

* New autonomous component: Regenerate checksums.txt file for ingested delivery.
* New autonomous component: Check regenerated checksums.txt file contain same lines as the one delivered by Infomedia.
* Autonomous components now verify that items fitting the events as reported by SBOI actually do and skip them if not. 

2018-04-23:  version 2

* Updated version of manual control web app.

2018-04-11:  version 1

* "bitrepository.ingester.maxPutRetries" now controls maximum number of retries
   for a FAILED put file to Bitrepository.  Additional retries are noted with
   a WARN to the log.

2018-03-26:

* "delivery.ready.filename" now controls name of file which must be
  present for a delivery to be initially created in DOMS.

2018-03-06:

* "bitrepository.ingester.urltobatchdir" must now be a valid URL.
* Environment variable ENABLE_GC_LOG now decides if gc logs are written.
* Don't use symbolic links for deliveries!  Tracked in DPA-132.
