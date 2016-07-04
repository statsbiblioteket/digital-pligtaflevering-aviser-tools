The "harness" is the common application code responsible for
providing the necessary functionality for a given Task can run.

1. Reading in the configuration map when started, and do initial debug logging.
2. Create a stream of items given by the repository the Task can be mapped over.
3. Handle exceptions thrown during processing.  The Task is expected to handle all situations known to itself properly.  Hence if an exception
is thrown it indicates an unforseen situation and therefor the harness logs this and shuts down properly.
4. Optionally be able to process items in parallel (just locally, not hadoop yet).

It is expected that a given deployed application will consist of an
 instance of this harness configured with e.g. Dagger 2.