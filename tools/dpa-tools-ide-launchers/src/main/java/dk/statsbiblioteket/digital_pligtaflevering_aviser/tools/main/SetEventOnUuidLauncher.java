package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * List deletable deliveries for JHKL.  No events are set.
 */
public class SetEventOnUuidLauncher {

    public static void main(String[] args) throws Exception {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        // ListDeletableDeliveriesLauncher can list valid uuid's to use here.

        SetEventOnUuidMain.main(new String[]{
                "set-event-on-uuid.properties",
                "event=Delivery_deleted_from_disk",
                "message=Deleted by launcher",
                "outcome=true",
                "item_uuid=uuid:b19ed3de-1fff-4500-93a5-154ba98349d8" // Replace with actual uuid as found in current DOMS.
        });
    }
}
