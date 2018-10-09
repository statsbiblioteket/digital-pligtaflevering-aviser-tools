package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CleanDeliveriesOnApprovalMainTest {
    
    @Test
    public void testReportFiles() throws IOException, MessagingException, URISyntaxException {
    
        DomsItem approved_roundtrip = mock(DomsItem.class);
        when(approved_roundtrip.getPath()).thenReturn("dl_19990909_rt2");
        String approved_roundtrip_path = "dl_19990909_rt2";
        
        CleanDeliveriesOnApprovalMain.CleanDeliveriesOnApprovalModule.SimpleMailer simpleMailer = mock(
                CleanDeliveriesOnApprovalMain.CleanDeliveriesOnApprovalModule.SimpleMailer.class);
        
        CleanDeliveriesOnApprovalMain.CleanDeliveriesOnApprovalModule reporter
                = new CleanDeliveriesOnApprovalMain.CleanDeliveriesOnApprovalModule();
    
        String subjectpattern = "Delivery {0} approved, please delete files from {1}";
        String bodypattern
                = "Delivery {0} has been approved. Please delete the files from {1} from the bit repository\n\nThe list of {2} files to delete can found here: {3}. This file should have the checksum {4}";
        String urlPrefix = "http://localhost/";
        
        //Store folder is just the test-classes folder
        Path storeFolder = new File(Thread.currentThread().getContextClassLoader().getResource("testResourcesPlaceholder").toURI()).toPath().getParent().toAbsolutePath();
    
    
        String roundtripID = "dl_19990909_rt1";
        //Delete the test filelist if it already exists
        Path listFile = storeFolder.resolve(roundtripID+".files").toAbsolutePath();
        boolean result = Files.deleteIfExists(listFile);
        
        
        reporter.reportFiles(roundtripID,
                             approved_roundtrip,
                             Arrays.asList("file1","file2"),
                             simpleMailer,
                             subjectpattern,
                             bodypattern,
                             "nobody@kb.dk",
                             storeFolder,
                             urlPrefix
                             );
        verify(simpleMailer).sendMail(eq(Arrays.asList("nobody@kb.dk")),
                                      eq("Delivery "+approved_roundtrip_path+" approved, please delete files from "+roundtripID),
                                      eq("Delivery "+approved_roundtrip_path+" has been approved. Please delete the files from "+roundtripID+" from the bit repository\n\nThe list of 2 files to delete can found here: "+urlPrefix+roundtripID+".files"+". This file should have the checksum 0C5A1E3BCD9F8636A73D424B1E68DD3B"));
        
        
    }
    
}