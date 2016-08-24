newspaper-bitrepository-ingester
================================

Provides bit preservation of the jp2 files found in a batch by:
* Generated a unique FileID identifying the file in the repository.
* Ingest the file into the bit repository verifying the ingest using the checksum for the file.
* Register the archived file in the DOMS system.

### Architecture
The ingester uses an injected TreeIterator to traverse a batch tree and add each detected jp file to the bit repository
system. The ingester acts as a bit repository put client.

It is also the responsibility of the ingester to register the batch as 'force-online', thereby preventing the
offline pillar from moving the files contained in a batch to tape. It is the responsibility of the QA process to
 release the batch for offline storage, when the batch has been approved.