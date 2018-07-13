package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

public class VeraPDFAnalyzeAllLauncher {
    public static void main(String[] args) throws Exception {
        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");


        VeraPDFAnalyzeMain.main(new String[]{
                "verapdf-analyze-all.properties"
        });
    }
}
