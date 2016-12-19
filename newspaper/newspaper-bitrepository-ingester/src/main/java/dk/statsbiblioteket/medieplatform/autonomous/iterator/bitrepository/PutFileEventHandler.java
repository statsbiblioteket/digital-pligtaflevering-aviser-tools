package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is just an empty implementation of an eventHandler, this is used for injecting into PutfileClient
 * PutfileClient supports running asyncronus, and this Handler should be expanded if we start using PutfileClient asyncronus
 */
public class PutFileEventHandler implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());


    public PutFileEventHandler() {
    }


    @Override
    public void handleEvent(OperationEvent event) {
    }
}
