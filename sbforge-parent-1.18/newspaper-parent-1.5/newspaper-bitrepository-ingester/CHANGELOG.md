### 1.11
* Fix the 'fail random' bug, that caused missing (incomplete) ingest of files. 

### 1.10
* Add auto-retry functionality, to avoid failed batches when Bitrepository Messages are lost
* Update to version 2.7 of batch-event-framework

### 1.9
* Fixed the bug that caused the component to hang upon certain errors from the bitrepository client

### 1.8
* Seperated the action of registering a file in doms from the put operations

### 1.7
* Added trace logging for detailing time spend on registering files in doms

### 1.6
* Use newest version of item event framework. No functional changes for this module.
* Configuration has been extended and changed and example config has been updated. Please update your configuration files.

### 1.5
* Update to version 1.3 of bitrepository reference code
* Update to version 1.10 of batch event framework dependencies
* Catch exceptions from doms and report them as failures, and by that prevent the ingester from hanging on doms faults. 

### 1.4
* Update to newspaper-parent 1.2
* Update to version 1.2 of bitrepository reference code
* Update to version 1.6 of batch event framework dependencies

### 1.3.1
* Update to newspaper-batch-event-framework 1.4.4, to make the component respect a maximum of reported failures

### 1.3
* Update component to newspaper-batch-event-framework to make it quiet
* Add details to failure report when components fail. 

### 1.2
* Make the ingester skip a batch if it cannot successfully force the files online
* Update to depend on newspaper-batch-event-framework 1.4

### 1.1
* Updated to use Bitrepository 1.1.1 (to prevent thread leaks)
* Updated to autonomous component framework 1.3
* Limit the amount of heap space the ingester is allowed to use
* Remove messages on stdout

### 1.0
* Updated to use Bitrepository 1.1
* After a file has been ingested into the repository, the file will be registered in the configured doms.
* A batch will be marked as 'forceonline' before ingest is begun. This means the configured 'forceonline' script is called
which should prevent the files for the batch from beingen moved from disk cache to tape.

### 0.1
Initial release

