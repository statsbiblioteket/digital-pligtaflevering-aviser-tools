newspaper-bitrepository-ingester
================================

Provides bit preservation of the pdf files found in a batch by:
* Generated a unique FileID identifying the file in the repository.
* Ingest the file into the bit repository verifying the ingest using the checksum for the file.
* Register the archived file in the DOMS system. (This part is not done yet)

### Architecture
The ingester uses an injected TreeIterator to traverse a batch tree and add each detected jp file to the bit repository
system. The ingester acts as a bit repository put client.

It is also the responsibility of the ingester to register the batch as 'force-online', thereby preventing the
offline pillar from moving the files contained in a batch to tape. It is the responsibility of the QA process to
 release the batch for offline storage, when the batch has been approved.
 
 
The ingester uses the properties file config.properties for configuration of the application.

 
**Format expectation and application configuration**
 
Start the application with this parameter:
../digital-pligtaflevering-aviser-tools/newspaper/newspaper-bitrepository-ingester/src/main/config/config.properties
And insert a link to servers in the vagrant in config.properties
 
During ingestion the pitrepository-ingester expects that there is delivered components called ..*.pfd/content
Thease files is found in the filesystem and ingested together with the checksom found in the file MD5SUMS.txt

 
Example:
This component:
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf/content
 
Is using this file as source:
B20160811-RT1/verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf


 
 
**Test setup**
 
1. first copy the testnewspaper batch to /newspapr_batches
2. Start with a clean vagrant
3. Run the CreateBach with i.e. thease parameters:
   B20160811 1 register-batch-trigger http://localhost:7880/fedora fedoraAdmin fedoraAdminPass http://localhost:7880/pidgenerator-service
 
4. Update the doms by running 7880-doms/bin/doms.sh import
5. Run newspaper-prompt-doms-ingester/PromptDomsIngesterComponent
6. Run BitrepositoryIngesterExecutable with a link to the file config.vagrant.properties
 
After that fedora should be updated
