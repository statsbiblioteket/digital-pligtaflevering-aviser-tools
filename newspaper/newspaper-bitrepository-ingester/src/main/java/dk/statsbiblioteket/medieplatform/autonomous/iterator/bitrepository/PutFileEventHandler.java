package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;

/**
 * This is just an empty implementation of an eventHandler, this is used for injecting into PutfileClient
 * PutfileClient supports running asyncronus, and this Handler should be expanded if we start using PutfileClient asyncronus
 */
public class PutFileEventHandler implements EventHandler {

    public PutFileEventHandler() {
    }


    @Override
    public void handleEvent(OperationEvent event) {
    }
}
