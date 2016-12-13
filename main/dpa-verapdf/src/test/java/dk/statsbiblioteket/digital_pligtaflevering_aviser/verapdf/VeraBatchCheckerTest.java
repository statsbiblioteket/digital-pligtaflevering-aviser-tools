package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 */
public class VeraBatchCheckerTest {





    //@Test
    public void generateXMLaa() throws Exception {



                    try {

                        FileInputStream fip = new FileInputStream("/home/mmj/temp/test4.pdf");

                        VeraPDFValidator validator1b = new VeraPDFValidator("1b", false);
                        byte[] result =  validator1b.apply(fip);

                        FileUtils.writeByteArrayToFile(new File("/home/mmj/temp/temp/" + "filename" + ".xml"), result);



                    } catch (Exception e) {


                    }

    }




    //@Test
    public void generateXML() throws Exception {


        ArrayList<String> list = new ArrayList<String>();
        Files.walk(Paths.get("/home/mmj/temp/dl1_20161201_rt1")).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();
                int pointLocation = file.lastIndexOf(".");
                String filename = file.substring(0, pointLocation);
                String ext = file.substring(pointLocation);
                if(ext.equals(".pdf")) {

                    try {

                        FileInputStream fip = new FileInputStream(filePath.toFile());

                        VeraPDFValidator validator1b = new VeraPDFValidator("1b", false);
                        byte[] result =  validator1b.apply(fip);

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


        ArrayList<ValidationResults> arl = new ArrayList<ValidationResults>();


        ArrayList<String> list = new ArrayList<String>();
        Files.walk(Paths.get("/home/mmj/temp/dl1_20161201_rt1")).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();

//                System.out.println(file);

                int pointLocation = file.lastIndexOf(".");
                if(pointLocation != -1) {
//                System.out.println(pointLocation);

                String filename = file.substring(0, pointLocation);
                String ext = file.substring(pointLocation);
                if(ext.equals(".verapdf")) {



                        try {

                            FileInputStream fip = new FileInputStream(filePath.toAbsolutePath().toFile());
                            VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(fip, true);
                            ValidationResults validationResults = rulo.validateResult();

                            arl.add(validationResults);

                            /*if(validationResults.getWorstBrokenRule().getValidationLevel()==30) {
                                arl.add()
                                t30 ++;
                            } else if(validationResults.getWorstBrokenRule().getValidationLevel()==20) {
                                t20 ++;
                            }*/


                        } catch (Exception e) {

                        }

                    }

                } else {
                    System.out.println("-- " + filePath.toAbsolutePath().toString());
                }



            }
        });
        System.out.println(list.size());


        int t30 = 0;
        int t20 = 0;
        int t00 = 0;


        for(ValidationResults ar : arl) {



            if(ar.getWorstBrokenRule().getValidationLevel()==30) {
                t30 ++;
            } else if(ar.getWorstBrokenRule().getValidationLevel()==20) {
                t20 ++;
            } else if(ar.getWorstBrokenRule().getValidationLevel()==0) {
                t00 ++;
            }  else {
                System.out.println("OTHER " + ar.getWorstBrokenRule().getValidationLevel());
            }


        }

        System.out.println(t30);
        System.out.println(t20);
        System.out.println(t00);





    }


}
