Changelog:
===

2018-03-26:

* "delivery.ready.filename" now controls name of file which must be
  present for a delivery to be initially created in DOMS.


2018-03-06:

* "bitrepository.ingester.urltobatchdir" must now be a valid URL.
* Environment variable ENABLE_GC_LOG now decides if gc logs are written.
* Don't use symbolic links for deliveries!  Tracked in DPA-132.
