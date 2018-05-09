package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;


import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.CreateDeliveryMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.FrontpageReadWrite;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
//TODO:MMJ CHECK THIS

/**
 * Simple unittest for validation of the datamodel
 */
public class TestPdfConversion {


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Validate that it is possible to create thumbnalis of frontpages
     * @throws Exception
     */
    @Test
    public void testMarshalUnmarshalDeliveryTitle() throws Exception {

        Path destinationFolder = Files.createTempDirectory("testfolder");

        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = null;
        try {
            l = CreateDeliveryMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.
        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        FrontpageReadWrite.mountpoint = batchPath.toString();


        String month = "08";
        String title = "verapdf";

        List<Path> pageFolderList = new ArrayList<Path>();
        List<File> frontPageList = new ArrayList<File>();
        List<Path> arl = FrontpageReadWrite.folderFinder("dl_2016"+month+"+(.*?)_rt+(.*?)");
        for(Path o : arl) {
            Path subPath = Paths.get(o.toString(), title + "/pages");
            pageFolderList.add(subPath);
        }

        for(Path o : pageFolderList) {
            File frontpageFile = FrontpageReadWrite.findFrontpages(o);
            frontPageList.add(frontpageFile);
        }

        FrontpageReadWrite.writeImages(frontPageList, destinationFolder.toFile().getAbsolutePath());
        assertEquals(2, Files.walk(destinationFolder).count());//validate that actual frontpage and ".." exists

    }

}
