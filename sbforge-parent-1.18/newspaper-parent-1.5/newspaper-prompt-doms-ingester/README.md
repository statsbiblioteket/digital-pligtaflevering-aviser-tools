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

1. Start with a clean vagrant
2. Run the CreateBach with i.e. thease parameters:
  B20160811 1 register-batch-trigger http://localhost:7880/fedora fedoraAdmin fedoraAdminPass http://localhost:7880/pidgenerator-service

3. Update the doms by running 7880-doms/bin/doms.sh import
4. Run PromptDomsIngesterComponent with a link to the file config.properties

After that fedora should be updated


TIP:  For easy rollback to an earlier snapshot "createbatch" use something similar to:

    VBoxManage controlvm vagrant_default_1475672637969_89753 poweroff && VBoxManage snapshot vagrant_default_1475672637969_89753 restore createbatch && VBoxManage startvm vagrant_default_1475672637969_89753

(replace name of virtual machine as appropriate)
