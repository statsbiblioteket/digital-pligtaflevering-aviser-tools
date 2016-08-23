newspaper-prompt-doms-ingester
==============================

Autonomous Component that ingests a batch directory directly to a DOMS object tree without any characterisation, validation, or modelling.

 * The IngesterInterface provides a method ingest() which takes as its only parameter the root directory of the batch
 to be ingested. The name of this directory provides the dc:identifier of the root object (preficed with "path:") and all sub-directories are
 ingested with identifiers consisting of their relative paths starting with the batch root directory. Metadata files
 are ingested as datastreams.
 * For the newspaper project there is factory method SimpleFedoraIngester.getNewspaperInstance() which returns an ingester
 configured for newspaper batch-ingest.
 
The ingester uses the properties file config.properties for configuration of the application.
The file config.properties.default is added for configuration suggestion during test.

**Format expectation and application configuration**

During ingestion the ingester expects that there is always delivered pdf and xml pair in the pages folder.
It then ingests the name of the two files as, and the full file-name.

Example:
This source:
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.xml

Is ingested as this:
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001


Ind this source:
B20160811-RT1/verapdf/udgave1/articles/20160811-verapdf-udgave1-article002.xml

Is ingested as this:
B20160811-RT1/verapdf/udgave1/articles/20160811-verapdf-udgave1-article002


**Test setup**

Start with a clean vagrant





