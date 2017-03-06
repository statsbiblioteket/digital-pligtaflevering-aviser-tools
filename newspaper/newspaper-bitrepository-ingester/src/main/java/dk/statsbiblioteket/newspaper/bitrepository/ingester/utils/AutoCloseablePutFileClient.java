package dk.statsbiblioteket.newspaper.bitrepository.ingester.utils;

import org.bitrepository.modify.putfile.PutFileClient;

/**
 * In order to use a PutFileClient in a try-with-resources block to have the official bitrepository client
 * close its ActiveMQ threads we need a version which implements AutoCloseable.
 */
public interface AutoCloseablePutFileClient extends PutFileClient, AutoCloseable {
}
