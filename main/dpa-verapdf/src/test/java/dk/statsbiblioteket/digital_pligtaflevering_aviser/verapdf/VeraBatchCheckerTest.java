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
import java.util.List;

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


    //@Test// - disabled as this has hardcoded local file names.
    public void validateXML() throws Exception {


        String testBatch = "/home/mmj/temp";
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
                            VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(true);
                            List<String> ids = rulo.extractRejected(fip);
                            ValidationResults validationResults = rulo.validateResult(ids);
                            arl.add(new FailedPage(validationResults, file));
                        } catch (Exception e) {
                            System.out.println("WRONG FILE ---- " + filename);
                            //assertEquals(e.getMessage(), true, false);
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
                System.out.println("Manual inspect " + ar.getPagePathAsString());
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








    @Test// - disabled as this has hardcoded local file names.
    public void countItAll() throws Exception {


        String testBatch = "/home/mmj/temp";///dl_20161205_rt1/metroxpressoest/pages";
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
                            VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(true);
                            List<String> ids = rulo.extractRejected(fip);
                            ValidationResults validationResults = rulo.validateResult(ids);
                            arl.add(new FailedPage(validationResults, file));
                        } catch (Exception e) {
                            System.out.println("WRONG FILE ---- " + filename);
                            //assertEquals(e.getMessage(), true, false);
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



        int p61131 = 0;
        int p61122 = 0;
        int p6243 = 0;
        int p61111 = 0;
        int p61126 = 0;
        int p694 = 0;
        int p6532 = 0;
        int p645 = 0;
        int p646 = 0;
        int p643 = 0;
        int p644 = 0;
        int p6281 = 0;
        int p641 = 0;
        int p642 = 0;
        int p6181 = 0;
        int p6171 = 0;
        int p6352 = 0;
        int p6361 = 0;
        int p6351 = 0;
        int p6131 = 0;
        int p6341 = 0;
        int p6373 = 0;
        int p6353 = 0;



        HashSet<String> failedList = new HashSet<String>();
        for (FailedPage ar : arl) {
            ArrayList<ValidationResult> rules = ar.getValidationResult().getRulesBroken();

            for (ValidationResult res : rules) {

                switch(res.getParagraph()) {
                    case "6.1.13-1":
                        p61131++;
                        break;
                    case "6.1.12-2":
                        p61122++;
                        break;
                    case "6.2.4-3":
                        p6243++;
                        break;
                    case "6.1.11-1":
                        p61111++;
                        break;
                    case "6.1.12-6":
                        p61126++;
                        break;
                    case "6.9-4":
                        p694++;
                        break;
                    case "6.5.3-2":
                        p6532++;
                        break;
                    case "6.4-5":
                        p645++;
                        break;
                    case "6.4-6":
                        p646++;
                        break;
                    case "6.4-3":
                        p643++;
                        break;
                    case "6.4-4":
                        p644++;
                        break;
                    case "6.2.8-1":
                        p6281++;
                        break;
                    case "6.4-1":
                        p641++;
                        break;
                    case "6.4-2":
                        p642++;
                        break;
                    case "6.1.8-1":
                        p6181++;
                        break;
                    case "6.1.7-1":
                        p6171++;
                        break;
                    case "6.3.5-2":
                        p6352++;
                        break;
                    case "6.3.6-1":
                        p6361++;
                        break;
                    case "6.3.5-1":
                        p6351++;
                        break;
                    case "6.1.3-1":
                        p6131++;
                        break;
                    case "6.3.4-1":
                        p6341++;
                        break;
                    case "6.3.7-3":
                        p6373++;
                        break;
                    case "6.3.5-3":
                        p6353++;
                        break;

                }

            }
            manualInspectCounter++;

        }

        System.out.println("=================================================================");
        System.out.println("6.1.13-1 : " + p61131);
        System.out.println("6.1.12-2 : " + p61122);
        System.out.println("6.2.4-3 : " + p6243);
        System.out.println("6.1.11-1 : " + p61111 );
        System.out.println("6.1.12-6 : " + p61126 );
        System.out.println("6.9-4 : " + p694 );
        System.out.println("6.5.3-2 : " + p6532);
        System.out.println("6.4-5 : " + p645);
        System.out.println("6.4-6 : " + p646);
        System.out.println("6.4-3 : " + p643);
        System.out.println("6.4-4 : " + p644);
        System.out.println("6.2.8-1 : " + p6281);
        System.out.println("6.4-1 : " + p641);
        System.out.println("6.4-2 : " + p642);
        System.out.println("6.1.8-1 : " + p6181);
        System.out.println("6.1.7-1 : " + p6171);
        System.out.println("6.3.5-2 : " + p6352);
        System.out.println("6.3.6-1 : " + p6361);
        System.out.println("6.3.5-1 : " + p6351);
        System.out.println("6.1.3-1 : " + p6131);
        System.out.println("6.3.4-1 : " + p6341);
        System.out.println("6.3.7-3 : " + p6373);
        System.out.println("6.3.5-3 : " + p6353);

















        /*System.out.println("APPROVED " + approvedCounter);
        System.out.println("MANUAL   " + manualInspectCounter);
        System.out.println("INVALID  " + invalidCounter);
        System.out.println("UNKNOWN  " + unknownCounter);*/


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
