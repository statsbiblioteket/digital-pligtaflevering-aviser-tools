package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 *
 */
public class BitrepositoryStubIngesterMainTest {
    // Commented out to keep documentation about needed parameters for IngesterMain.

//    @Test
//    public void bitrepositoryIngestB20160811_RT1() throws URISyntaxException {
//        String batchDirPathInWorkspace = "delivery-samples";
//        String bitrepoDirPathInWorkspace = "bitrepositorystub-storage";
//
//        // http://stackoverflow.com/a/320595/53897
//        URI l = BitrepositoryStubIngesterMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
//        Path startDir = Paths.get(l);
//
//        // Look for the first instance of batchDir in the directories towards the root of the file system.
//        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.
//
//        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
//
//        Path bitrepoPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, bitrepoDirPathInWorkspace);
//
//        BitRepositoryIngesterMain.main(new String[]{
//                "bitrepository-stub-ingester.properties",
//                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + batchPath.toAbsolutePath(),
//                URL_TO_BATCH_DIR_PROPERTY + "=" + batchPath.toAbsolutePath(),
//                "dpa.putfile.destinationpath=" + bitrepoPath.toAbsolutePath(),
//                BITMAG_BASEURL_PROPERTY + "=http://localhost:58709/"
//        });
//    }
}
