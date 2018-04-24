Changelog:
===

2018-03-06:

* "bitrepository.ingester.urltobatchdir" must now be a valid URL.
* Environment variable ENABLE_GC_LOG now decides if gc logs are written.
* Don't use symbolic links for deliveries!  Tracked in DPA-132.

2018-04-24:

* DPA-140: Checksum regenerator autonomous component added to do integrity check against
checksum file in delivery.
* DPA-143: Configuration property files normalized so those used in development are those included in the deployment.

