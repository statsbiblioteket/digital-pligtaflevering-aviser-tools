Next release
=============


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














