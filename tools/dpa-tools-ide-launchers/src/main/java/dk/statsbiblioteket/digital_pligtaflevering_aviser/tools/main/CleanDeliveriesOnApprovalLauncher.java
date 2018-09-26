package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;



/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 */
public class CleanDeliveriesOnApprovalLauncher {

    public static void main(String[] args) throws Exception {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        
        
        CleanDeliveriesOnApprovalMain.main(new String[]{
                "approve-cleaner.properties",
                "approve-delete.email.from.address="+System.getenv("USER")+"@kb.dk",
                "approve-delete.email.addresses="+System.getenv("USER")+"@kb.dk"
                
        });
    }
}
