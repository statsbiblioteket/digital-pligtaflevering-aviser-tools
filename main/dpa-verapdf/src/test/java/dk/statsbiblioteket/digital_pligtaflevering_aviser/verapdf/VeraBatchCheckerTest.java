package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class VeraBatchCheckerTest {


    //@Test
    public void generateXMLaa() throws Exception {


        try {

            FileInputStream fip = new FileInputStream("/home/mmj/temp/test4.pdf");

            VeraPDFValidator validator1b = new VeraPDFValidator("1b", false);
            byte[] result = validator1b.apply(fip);

            FileUtils.writeByteArrayToFile(new File("/home/mmj/temp/temp/" + "filename" + ".xml"), result);


        } catch (Exception e) {


        }

    }


    //@Test
    public void generateXML() throws Exception {


        ArrayList<String> list = new ArrayList<String>();
        Files.walk(Paths.get("/home/mmj/testdata/dl1_20161201_rt1")).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();
                int pointLocation = file.lastIndexOf(".");
                String filename = file.substring(0, pointLocation);
                String ext = file.substring(pointLocation);
                if (ext.equals(".pdf")) {

                    try {

                        FileInputStream fip = new FileInputStream(filePath.toFile());

                        VeraPDFValidator validator1b = new VeraPDFValidator("1b", false);
                        byte[] result = validator1b.apply(fip);

                        FileUtils.writeByteArrayToFile(new File("/home/mmj/temp/temp/" + filename + ".xml"), result);


                    } catch (Exception e) {


                    }

                }
            }
        });
        System.out.println(list.size());
    }


    @Test
    public void validateXML() throws Exception {


        String testBatch = "/home/mads/testdata/tstdelivery";
        String batchDirPathInWorkspace = "delivery-samples";
        Path p = Paths.get(batchDirPathInWorkspace).toAbsolutePath();

        // http://stackoverflow.com/a/320595/53897
        URI l = null;
        try {
            l = VeraBatchCheckerTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path startDir = Paths.get(l);


        ArrayList<FailedPage> arl = new ArrayList<FailedPage>();


        ArrayList<String> list = new ArrayList<String>();
        Files.walk(Paths.get(testBatch)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();

                int pointLocation = file.lastIndexOf(".");
                if (pointLocation > 0) {// only check real filenames

                    String filename = file.substring(0, pointLocation);
                    String ext = file.substring(pointLocation);
                    if (ext.equals(".verapdf")) {
                        try {
                            FileInputStream fip = new FileInputStream(filePath.toAbsolutePath().toFile());
                            VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(fip, true);
                            ValidationResults validationResults = rulo.validateResult();
                            arl.add(new FailedPage(validationResults, file));
                        } catch (Exception e) {
                            assertEquals(e.getMessage(), true, false);
                        }
                    }
                } else {
                    System.out.println("-- " + filePath.toAbsolutePath().toString());
                }
            }
        });


        int approvedCounter = 0;
        int manualInspectCounter = 0;
        int invalidCounter = 0;
        int unknownCounter = 0;


        HashSet<String> failedList = new HashSet<String>();


        for (FailedPage ar : arl) {


            if (ar.getValidationResult().getWorstBrokenRule().getValidationLevel() == ValidationResult.ValidationResultEnum.approved.getValidationLevel()) {
                approvedCounter++;
            } else if (ar.getValidationResult().getWorstBrokenRule().getValidationLevel() == ValidationResult.ValidationResultEnum.manualInspection.getValidationLevel()) {

                //fejler på 6.3.5 og 6.1.11
                //System.out.println("Manual inspect " + ar.getPagePathAsString());
                ArrayList<ValidationResult> rules = ar.getValidationResult().getRulesBroken();

                for (ValidationResult res : rules) {
                    failedList.add(res.getParagraph());
                }
                manualInspectCounter++;
            } else if (ar.getValidationResult().getWorstBrokenRule().getValidationLevel() == ValidationResult.ValidationResultEnum.invalid.getValidationLevel()) {

                //Fejler paa 6.9
                System.out.println("FAILED " + ar.getPagePathAsString());
                ArrayList<ValidationResult> rules = ar.getValidationResult().getRulesBroken();
                for (ValidationResult res : rules) {
                    failedList.add(res.getParagraph());
                }
                invalidCounter++;
            } else {

                System.out.println("UNKNOWN " + ar.getPagePathAsString() + ar.validationResult.getWorstBrokenRule());
                ArrayList<ValidationResult> rules = ar.getValidationResult().getRulesBroken();
                for (ValidationResult res : rules) {
                    failedList.add(res.getParagraph());
                }
                unknownCounter++;
                //System.out.println("OTHER " + ar.getValidationResult().getWorstBrokenRule().getValidationLevel());
            }
        }

        System.out.println("APPROVED " + approvedCounter);
        System.out.println("MANUAL   " + manualInspectCounter);
        System.out.println("INVALID  " + invalidCounter);
        System.out.println("UNKNOWN  " + unknownCounter);


        for (String failItem : failedList) {
            System.out.println(failItem);
        }
    }


    /** Class embedding
     *
     */
    public class FailedPage {
        ValidationResults validationResult;
        String pagePathAsString;

        public FailedPage(ValidationResults no, String page) {
            this.validationResult = no;
            this.pagePathAsString = page;
        }

        public ValidationResults getValidationResult() {
            return validationResult;
        }

        public String getPagePathAsString() {
            return pagePathAsString;
        }
    }
}
