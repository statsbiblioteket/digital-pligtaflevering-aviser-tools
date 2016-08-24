package dk.statsbiblioteket.newspaper.bitrepository.ingester;


import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.testng.annotations.Test;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutJob;

public class DomsJP2FileUrlRegisterTest {
    public static final String FILE_URL = "http://bitfinder.statsbiblioteket.dk/newspaper/foo";
    public static final String BASE_URL = "http://bitfinder.statsbiblioteket.dk/newspaper/";
    public static final int MAX_THREADS = 1;
    public static final String FILE_NAME = "foo";
    public static final String FILE_PATH = "path:B400022028241-RT1/400022028241-14/1795-06-13-01/foo";
    public static final String CHECKSUM = "abcd";

    @Test
    public void goodCaseRegistrationTest() throws BackendInvalidCredsException, BackendMethodFailedException,
            BackendInvalidResourceException, DomsObjectNotFoundException, InterruptedException, MalformedURLException {
        EnhancedFedora mockCentral = mock(EnhancedFedora.class);
        ResultCollector mockResultCollector = mock(ResultCollector.class);
        String TEST_PID = "pidA";
        when(mockCentral.findObjectFromDCIdentifier(anyString())).thenReturn(Arrays.asList(TEST_PID));
        IngestableFile ingestableFile = new IngestableFile(FILE_NAME, new URL(FILE_URL), getChecksum(CHECKSUM), null, FILE_PATH);
        PutJob job = new PutJob(ingestableFile);
        try (DomsJP2FileUrlRegister register = new DomsJP2FileUrlRegister(null,
                                                                                 mockCentral, BASE_URL, mockResultCollector, MAX_THREADS,
                                                                                 10000)) {
            register.registerJp2File(job);
        }

        
        verify(mockCentral).findObjectFromDCIdentifier(FILE_PATH);
        verify(mockCentral).addExternalDatastream(eq(TEST_PID), eq("CONTENTS"), eq(FILE_NAME), eq(FILE_URL),
                anyString(), eq(DomsJP2FileUrlRegister.JP2_MIMETYPE), anyListOf(String.class) ,anyString());
        verify(mockCentral).addRelation(eq(TEST_PID), eq("info:fedora/" + TEST_PID + "/CONTENTS"),
                eq(DomsJP2FileUrlRegister.RELATION_PREDICATE), eq(CHECKSUM), eq(true), anyString());
        verifyNoMoreInteractions(mockCentral);
    }
    
    @Test
    public void multiplePidTest() throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException, 
    		InterruptedException, MalformedURLException {
        EnhancedFedora mockCentral = mock(EnhancedFedora.class);
        ResultCollector mockResultCollector = mock(ResultCollector.class);
        String TEST_PID_A = "pidA";
        String TEST_PID_B = "pidA";
        when(mockCentral.findObjectFromDCIdentifier(anyString())).thenReturn(Arrays.asList(TEST_PID_A, TEST_PID_B));
        IngestableFile ingestableFile = new IngestableFile(FILE_NAME, new URL(FILE_URL), getChecksum(CHECKSUM), null, FILE_PATH);
        PutJob job = new PutJob(ingestableFile);
        
        try (DomsJP2FileUrlRegister register = new DomsJP2FileUrlRegister(null, mockCentral,
                                                                                 BASE_URL,
                                                                                 mockResultCollector,
                                                                                 MAX_THREADS, 10000)) {
            register.registerJp2File(job);
        }

        verify(mockCentral).findObjectFromDCIdentifier(FILE_PATH);
        verifyNoMoreInteractions(mockCentral);
        verify(mockResultCollector).addFailure(eq(job.getIngestableFile().getFileID()), eq("exception"), anyString(), anyString());
        
    }
    
    private ChecksumDataForFileTYPE getChecksum(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        checksumData.setCalculationTimestamp(CalendarUtils.getNow());
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpec);
        return checksumData;
    }
}
