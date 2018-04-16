package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateDelivery;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * @noinspection WeakerAccess
 */
public class CreateDeliveryMain {

    /**
     * Where to store the markers that a delivery has already been processed?
     */
    public static final String AUTONOMOUS_DONEDIR = "autonomous.filesystem.processed.deliverys";
    /**
     * How should this agent report itself?
     */
    public static final String AUTONOMOUS_AGENT = "autonomous.agent";
    /**
     * The parameter for the name of the file that MUST be present in the delivery tree root for
     * this delivery to be processed.
     */
    public static final String DELIVERY_READY_FILENAME = "delivery.ready.filename";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCreateDeliveryMain_CreateDeliveryComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CreateDeliveryModule.class})
    interface CreateDeliveryComponent {
        Tool getTool();
    }

    @Module
    static class CreateDeliveryModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(AUTONOMOUS_AGENT) String premisAgent,
                         @Named(DOMS_URL) String domsUrl,
                         @Named(DOMS_USERNAME) String domsUser,
                         @Named(DOMS_PASSWORD) String domsPass,
                         @Named(DOMS_PIDGENERATOR_URL) String urlToPidGen,
                         Provider<Stream<Path>> deliveriesToCreateProvider,
                         @Named(AUTONOMOUS_DONEDIR) String doneDir,
                         @Named(DELIVERY_READY_FILENAME) String deliveryReadyFilename) {

            // sanity check
            File doneDirFile = new File(doneDir);
            if (doneDirFile.exists() && doneDirFile.isDirectory() && doneDirFile.canWrite()) {
                // good, doneDir writable
            } else {
                throw new IllegalArgumentException(
                        "doneDir not writable directory: " + doneDir + " (" + doneDirFile.getAbsolutePath() + ")"
                );
            }

            return () -> {
                // Expected folderFormat
                Pattern pattern = Pattern.compile("^(.*)_rt([0-9]+)$");
                // Iterate through the deliveries on disk
                final Stream<Path> paths = deliveriesToCreateProvider.get();
                String joinedString = paths.map(deliveryItemDirectoryPath -> {
                    File deliveryItemDirectory = deliveryItemDirectoryPath.toFile();

                    final File deliveryReadyIndicatorFile = new File(deliveryItemDirectory, deliveryReadyFilename);
                    if (deliveryReadyIndicatorFile.exists()) {
                        // Split into a part with deliveryname and one with mutation
                        // Std. Delivery: dl_########_rt#
                        // Mutation: mt_########_no#
                        String deliveryItemDirectoryName = deliveryItemDirectory.getName();
                        Matcher matcher = pattern.matcher(deliveryItemDirectoryName);
                        if (matcher.matches()) {
                            String deliveryIdValue = matcher.group(1);
                            String roundtripValue = matcher.group(2);

                            final File directoryProcessedIndicatorFile = new File(doneDir, deliveryItemDirectoryName);

                            if (!directoryProcessedIndicatorFile.exists()) {
                                // invoke the slightly modified newspaper create batch routine.
                                CreateDelivery.main(new String[]{deliveryIdValue, roundtripValue, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen, deliveryItemDirectory.getAbsolutePath()});
                                Try.run(() -> touch(directoryProcessedIndicatorFile));
                            } else {
                                log.trace("already processed, skipping {}", deliveryItemDirectory.getName());
                            }
                        } else {
                            log.trace("file name did not match, skipping {}", deliveryItemDirectoryName);
                        }
                    } else {
                        log.debug("Skipping directory {}, since the delivery does not have '{}'",
                                deliveryItemDirectoryPath, deliveryReadyFilename);
                    }
                    return deliveryItemDirectoryPath.toString();
                }).collect(Collectors.joining(" "));

                return "processed CreateDelivery for " + joinedString;
            };
        }

        @Provides
        @Named(AUTONOMOUS_AGENT)
        String provideAgent(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_AGENT);
        }

        @Provides
        @Named(AUTONOMOUS_DONEDIR)
        String provideDoneDir(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_DONEDIR);
        }

        @Provides
        @Named(DELIVERY_READY_FILENAME)
        String provideTransferAcknowledgedFilename(ConfigurationMap map) {
            return map.getRequired(DELIVERY_READY_FILENAME);
        }

        /**
         * <p> Locate deliveries in the current folder and below in reverse order (by looking for the transfer completed
         * file). </p> <p> Reversing them mean that "A_rt2" will be processed before "A_rt1" so when "A_rt1" is
         * considered there is already "A_rt2" present for delivery "A". (This only works properly up to 9 roundtrips,
         * but that should hopefully be rare). </p>
         *
         * @param deliveryFolderName Path name for delivery folder (containing deliveries).
         * @return Reverse sorted stream of paths of deliveries.
         */
        @Provides
        Stream<Path> provideDeliveriesToCreate(@Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String deliveryFolderName,
                                               @Named(DELIVERY_READY_FILENAME) String deliveryReadyFilename) {
            return Try.of(() -> Files.walk(Paths.get(deliveryFolderName), 1)
                    .filter(Files::isDirectory)
                    // FIXME: Log those that failed.
                    .filter(p -> Files.exists(p.resolve(deliveryReadyFilename)))
                    .sorted(Comparator.reverseOrder())
            ).get();
        }
    }

    /**
     * Create an empty file to indicate that the batch has been run.  Last modified time is set to now. A simple
     * implementation of the Unix "touch" command.
     *
     * @param file file to "touch"
     * @throws IOException
     */
    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(System.currentTimeMillis());
    }

}
