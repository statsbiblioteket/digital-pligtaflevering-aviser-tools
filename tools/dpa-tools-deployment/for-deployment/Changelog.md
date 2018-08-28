
2018-08-28
=============

Verapdf-invoke
--------------
* Everything failed due to an invalid config value, which cascaded errors onwards. To fix, set `bitrepository.sbpillar.mountpoint=/avisbits/dpaviser/` in `verapdf-invoke-all.properties` 


Newspaper statistics
--------------------
* Newspaper statistics xml files were generated with the a `www.sb.dk`-namespace. As we do not own that domain, this needed to be changed.
  Current version use the namespace `kb.dk/dpa/delivery-statistics`.
  
* Plan to fix the namespace of older deliveries
    1. We stop the `generate-statistics` component
    2. MMJ will use the Manual Control WUI to remove the event `Statistics_generated` from all old deliveries
    3. Use the url <http://avior:58608/newspapr/sbsolr/collection1/select?q=item_model%3A%22doms%3AContentModel_DPARoundtrip%22%0AAND%0Aevent%3A%22Statistics_generated%22&rows=1000&fl=item_uuid&wt=csv&indent=true> (replace avior with prod search host) to verify that all events have been removed.
    3. Replace the component `generate-statistics` with the fixed version from this release.
    4. Start the component again.
  

Newspaper Weekdays Analyzer
----------------------------

* DeliveryPattern.xml is now read from config, rather than being hardcoded.

* Multiple delivery patterns can be specified in `newspaper-weekdays-analyze.properties` with the property

        #first pattern:until date,second pattern:until date.....
        dpa.delivery-pattern=/root/services/conf/DeliveryPattern-none.xml:dl_20180101_rt0,/root/services/conf/DeliveryPattern1.xml:dl_20180922_rt0,/root/services/conf/DeliveryPattern2.xml

* WeekDayAnalyze to use an enum for the day-names (so there can be only 7), rather than strings.
    * This caused a slight change in DeliveryPattern.xml, where days are now fully named (MONDAY) rather than shortened (Mon)

* Process delivery from Doms, not from (temporary) filesystem. Thus, we can perform this check after the delivery is no longer cached.

* Use library to format the JSON result, rather than string concat, so we do not fail on weird characters and the like.


Manualcontrol web ui
--------------------

* fix UI performance

* Better icons for Delivery Overview

* Delivery Overview tooltips

Test setup
-----------

* Scripts to setup restartable remote vagrant 


Overall
--------
* Deployalble package final name: is now of the form `dpa_${timestamp}_${git.branch}_${git.commit.id.abbrev}` where timestamp is the ISO formattet timestamp denoting the build time. An example filename would thus be `dpa_20180725T142827Z_master_52d1b7.tar.gz`

* better md5sum stream reader with better closing responsibility. Should work the same


2018-07-11:
==========

* New autonomous component: invoke verapdf via rest on all PDF files in roundtrip and store result in datastream.
* New autonomous component: analyze verapdf output from above and generate full report on child, and bad-enough report on roundtrip.
* New autonomous component: analyze roundtrip on disk to see correspondence with spread sheet.
* "dashboard" webapp removed as all interesting functionality has been moved to "manual control" webapp.

2018-05-03:
===========

* New autonomous component: Regenerate checksums.txt file for ingested delivery.
* New autonomous component: Check regenerated checksums.txt file contain same lines as the one delivered by Infomedia.
* Autonomous components now verify that items fitting the events as reported by SBOI actually do and skip them if not. 

2018-04-23:  version 2
======================

* Updated version of manual control web app.

2018-04-11:  version 1
======================

* "bitrepository.ingester.maxPutRetries" now controls maximum number of retries
   for a FAILED put file to Bitrepository.  Additional retries are noted with
   a WARN to the log.

2018-03-26:
===========

* "delivery.ready.filename" now controls name of file which must be
  present for a delivery to be initially created in DOMS.

2018-03-06:
===========

* "bitrepository.ingester.urltobatchdir" must now be a valid URL.
* Environment variable ENABLE_GC_LOG now decides if gc logs are written.
* Don't use symbolic links for deliveries!  Tracked in DPA-132.

2018-04-24:
==========

* DPA-140: Checksum regenerator autonomous component added to do integrity check against
checksum file in delivery.
* DPA-143: Configuration property files normalized so those used in development are those included in the deployment.

