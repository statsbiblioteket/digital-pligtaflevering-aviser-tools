newspaper-prompt-doms-ingester
==============================

Autonomous Component that ingests a batch directory directly to a DOMS object tree without any characterisation, validation, or modelling.

 * The IngesterInterface provides a method ingest() which takes as its only parameter the root directory of the batch
 to be ingested. The name of this directory provides the dc:identifier of the root object (preficed with "path:") and all sub-directories are
 ingested with identifiers consisting of their relative paths starting with the batch root directory. Metadata files
 are ingested as datastreams.
 * For the newspaper project there is factory method SimpleFedoraIngester.getNewspaperInstance() which returns an ingester
 configured for newspaper batch-ingest.
