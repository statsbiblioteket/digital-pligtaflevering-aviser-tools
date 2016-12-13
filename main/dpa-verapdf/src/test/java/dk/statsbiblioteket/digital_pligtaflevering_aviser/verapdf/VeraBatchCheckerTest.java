package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXME:  Skulle vist ikke have været checket ind.
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



    //@Test
    public void validateXML() throws Exception {


        ArrayList<FailedPage> arl = new ArrayList<FailedPage>();


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
                            VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(true);
                            List<String> ids = rulo.extractRejected(fip);
                            ValidationResults validationResults = rulo.validateResult(ids);

                            arl.add(new FailedPage(validationResults, file));

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


        for(FailedPage ar : arl) {



            if(ar.getNo().getWorstBrokenRule().getValidationLevel()==30) {
                t30 ++;
            } else if(ar.getNo().getWorstBrokenRule().getValidationLevel()==20) {
                //System.out.println("MAN INSPECT " + ar.getPage());
                t20 ++;
            } else if(ar.getNo().getWorstBrokenRule().getValidationLevel()==0) {

                System.out.println("FAILED " + ar.getPage());
                t00 ++;
            }  else {
                //System.out.println("OTHER " + ar.getNo().getWorstBrokenRule().getValidationLevel());
            }


        }

        System.out.println(t30);
        System.out.println(t20);
        System.out.println(t00);





    }


    public class FailedPage {
        ValidationResults no;
        String page;

        public FailedPage(ValidationResults no, String page) {
            this.no = no;
            this.page = page;

        }

        public ValidationResults getNo() {
            return no;
        }

        public String getPage() {
            return page;
        }



    }


}
