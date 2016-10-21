package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Function;

/**  <p>
 * DomsItemDatastreams give a view of datastreams on a given item as a Map.  Currently
 * only the <code>put</code> operation is supported.
 * </p>
 * <p>
 *     <ol>
 *         <li>We want to consider saving metadata as JSON or XML.  This mean that the mime-type must be providable. </li>
 *         <li></li>
 *     </ol>
 * </p>
 * // https://github.com/statsbiblioteket/newspaper-batch-event-framework/issues/1
 */
public class DomsItemDatastreams extends HashMap<String, String> {

    private final DomsId domsId;
    private final DomsEventStorage<Item> domsEventStorage;
    private Function<String, String> commentFor;
    private final EnhancedFedora fedora;

    public DomsItemDatastreams(DomsId domsId, DomsEventStorage<Item> domsEventStorage, Function<String, String> commentFor) {
        this.domsId = domsId;
        this.domsEventStorage = domsEventStorage;
        this.commentFor = commentFor;

        // domsEventStorage.fedora is private in sboi-doms-event-framework-2.10 - circumvent using reflection.
        try {
            Field field = domsEventStorage.getClass().getDeclaredField("fedora");
            field.setAccessible(true);
            this.fedora = (EnhancedFedora) field.get(domsEventStorage);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("cannot retrieve 'fedora' through reflection", e);
        }
    }

    @Override
    public String put(String key, String value) {
        // semantics are save value immediately

        try {
            // FIXME:  Is this exposed in ObjectProfile?
            fedora.modifyDatastreamByValue(domsId.id(),
                    key,
                    null, // no checksum
                    null, // no checksum
                    value.getBytes(StandardCharsets.UTF_8),
                    null,
                    "text/plain", // "text/xml",
                    commentFor.apply(key),
                    null);
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new RuntimeException("put(" + key + " -> " + value + " failed", e);
        }
        System.out.println(key + " -> " + value);
        return super.put(key, value); // Will we use this?
    }
}
