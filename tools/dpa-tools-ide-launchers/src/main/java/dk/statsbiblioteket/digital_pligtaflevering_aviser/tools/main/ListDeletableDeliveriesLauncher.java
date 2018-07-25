package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * List deletable deliveries for JHKL.  No events are set.
 */
public class ListDeletableDeliveriesLauncher {

    public static void main(String[] args) throws Exception {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        ListDeletableDeliveriesMain.main(new String[]{
                "list-deletable-deliveries.properties",
        });
    }
}
