package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static java.util.stream.Collectors.toList;

public class CleanDeliveriesOnApprovalMain {
    
    protected static final Logger log = LoggerFactory.getLogger(CleanDeliveriesOnApprovalMain.class);
    
    
    public static void main(String[] args) {
        
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCleanDeliveriesOnApprovalMain_CleanDeliveriesOnApprovalComponent.builder()
                                                                                           .configurationMap(m)
                                                                                           .build()
                                                                                           .getTool()
        );
    }
    
    @Component(modules = {
            ConfigurationMap.class,
            CommonModule.class,
            DomsModule.class,
            CleanDeliveriesOnApprovalMain.CleanDeliveriesOnApprovalModule.class
    })
    interface CleanDeliveriesOnApprovalComponent {
        Tool getTool();
    }
    
    /**
     * @noinspection WeakerAccess, Convert2MethodRef
     */
    @Module
    protected static class CleanDeliveriesOnApprovalModule {
        Logger log = LoggerFactory.getLogger(CleanDeliveriesOnApprovalMain.class);  // short name
        
        /**
         * @noinspection PointlessBooleanExpression, UnnecessaryLocalVariable, unchecked
         */
        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         DefaultToolMXBean mxBean,
                         @Named("approve-delete.doms.commit.comment")
                                 String commitComment,
                         @Named("approve-delete.doms.batch.to.roundtrip.relation")
                                 String relation,
                         SimpleMailer mailer,
                         @Named("approve-delete.email.body.pattern")
                                 String emailBodyPattern,
                         @Named("approve-delete.email.addresses") String emailRecipients,
                         @Named("approve-delete.email.subject.pattern")
                                 String emailSubjectPattern
        ) {
            
            //approve-delete.email.subject.pattern=Delivery dl_{0}_rt{1,number,integer} approved, please delete files from dl_{0}_rt{2,number,integer}
            //approve-delete.email.subject.pattern=Delivery dl_{0}_rt{1,number,integer} approved, please delete files from dl_{0}_rt{2,number,integer}
            
            
            final String agent = CleanDeliveriesOnApprovalMain.class.getSimpleName();
            
            List<DomsItem> result = new ArrayList<>();
            
            
            Tool tool = () -> {
                //Find the roundtrips
                List<DomsItem> roundtripItems = Stream.of(workToDoQuery)
                                                      .flatMap(domsRepository::query)
                                                      .peek(o -> log.trace("Query returned: {}", o))
                                                      .collect(toList());
                for (DomsItem approvedRoundtrip : roundtripItems) {
    
                    DomsItem deliveryItem;
                    String approved_roundtrip_date = approvedRoundtrip.getPath().split("_")[1];
                    try {
                        
                        deliveryItem = domsRepository.getItemFromPath("dl_" + approved_roundtrip_date);
                    } catch (NoSuchElementException e){
                        throw new RuntimeException("Roundtrip item "+approvedRoundtrip+" have no delivery item",e);
                    }
                    
                    log.info("Found batch object, pid: '{}'", deliveryItem);
                    Stream<DomsItem> allRoundTripsForThisDelivery = deliveryItem.children();
                    
                    log.info("All roundtrips for batch '{}': '{}'",
                              deliveryItem.getPath(),
                              allRoundTripsForThisDelivery);
                    
                    List<DomsItem> oldRoundtrips =
                            allRoundTripsForThisDelivery
                                    .filter(roundTrip -> !roundTrip.equals(approvedRoundtrip))
                                    .filter(roundTrip -> {
                                        //TODO read the event name from config
                                        if (roundTrip.events()
                                                     .stream()
                                                     .anyMatch(Event -> Event.getEventType()
                                                                             .equals("Roundtrip_approved"))) {
                                            log.error(
                                                    "Duplicate approval: {} and {} are both approved",
                                                    approvedRoundtrip.getPath(),
                                                    roundTrip.getPath());
                                            return false;
                                        }
                                        return true;
                                    })
                                    .filter(roundTrip -> {
                                        if (roundTrip.getPath()
                                                     .compareTo(approvedRoundtrip.getPath())
                                            > 0) {
                                            log.error(
                                                    "A roundtrip '{}' with a higher roundtrip number than '{}' exists! It will not be cleaned.",
                                                    roundTrip,
                                                    approvedRoundtrip);
                                            return false;
                                        }
                                        return true;
                                    })
                                    .collect(Collectors.toList());
                    
                    
                    log.info("Old roundtrips up for cleaning: '{}'", oldRoundtrips);
                    for (DomsItem roundtrip : oldRoundtrips) {
                        
                        log.info("Starting cleaning of roundtrip: '{}'", roundtrip);
                        Collection<String> files = deleteRoundTrip(roundtrip, deliveryItem, commitComment);
                        log.info("Finished cleaning of roundtrip: '{}'", roundtrip);

                        reportFiles(roundtrip,
                                    approvedRoundtrip,
                                    files,
                                    mailer, emailSubjectPattern,
                                    emailBodyPattern,
                                    emailRecipients);
                        
                        result.add(roundtrip);
                    }
                    String deleted_roundtrips = oldRoundtrips.stream().map(domsItem -> domsItem.getPath()).collect(
                            Collectors.joining(","));
                    
                    //TODO what about exceptions here?
                    approvedRoundtrip.appendEvent(new DomsEvent(agent,
                                                                new Date(),
                                                                "deleted roundtrips " + deleted_roundtrips,
                                                                eventName,
                                                                true));
                    
                }
                return result;
            };
            
            return tool;
        }
        
        
        private static Pair<Integer, Integer> getNumbers(DomsItem roundTrip) {
            Pattern pattern = Pattern.compile("dl_([\\d]{8})_rt(\\d+)$");
            Matcher matcher = pattern.matcher(roundTrip.getPath());
            if (!matcher.matches()) {
                return null;
            } else {
                return new Pair<>(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            }
        }
        
        
        /**
         * Send a mail to the recipients about the approval of the approved_roundtrip and the resulting deletion of the older approved_roundtrip
         *
         * @param roundTrip           the old approved_roundtrip which is to be deleted
         * @param approved_roundtrip  the approved_roundtrip that have been approved
         * @param files               the files in the old approved_roundtrip which should be deleted
         * @param mailer
         * @param emailSubjectPattern
         * @param emailBodyPattern
         * @param emailRecipients
         * @throws MessagingException if sending the mail failed
         *                            dk.statsbiblioteket.medieplatform.autonomous.Batch)
         */
        protected void reportFiles(DomsItem roundTrip,
                                   DomsItem approved_roundtrip,
                                   Collection<String> files,
                                   SimpleMailer mailer,
                                   String emailSubjectPattern,
                                   String emailBodyPattern,
                                   String emailRecipients)
                throws MessagingException {
            if (!files.isEmpty()) {
                String subject = formatSubject(emailSubjectPattern, roundTrip, approved_roundtrip);
                log.trace("Mail subject cleaning of approved_roundtrip '{}' with pid '{}': '{}'",
                          roundTrip.getPath(),
                          roundTrip.getDomsId(),
                          subject);
                String body = formatBody(emailBodyPattern, roundTrip, approved_roundtrip, files);
                log.trace("Mail body cleaning of approved_roundtrip '{}' with pid '{}': '{}'",
                          roundTrip.getPath(),
                          roundTrip.getDomsId(),
                          body);
                mailer.sendMail(Arrays.asList(emailRecipients.split(",")), subject, body);
            }
        }
        
        
        private Collection<String> deleteRoundTrip(DomsItem roundtrip,
                                                   DomsItem delivery,
                                                   String deleteComment) {
            
            List<String> bitRepoFiles = new ArrayList<>();
            
            roundtrip.allChildren()
                     .peek(domsItem -> {
                         try {
                             String bitRepoFileId = domsItem.datastream("CONTENTS").getLabel();
                             bitRepoFiles.add(bitRepoFileId);
                         } catch (NoSuchElementException e) {
                             //So no Contents Datastream. This is ok.
                         }
                     })
                     .forEach(domsItem -> domsItem.delete(deleteComment));
            
            roundtrip.delete(deleteComment);
            delivery.unlinkChild(roundtrip, deleteComment);
            
            
            return bitRepoFiles;
        }
        
        
        /**
         * Format the body of the mail
         *
         * @param fileDeletionBody   The pattern for the mail body
         * @param roundtrip          the old approved_roundtrip which is to be deleted
         * @param approved_roundtrip the approved_roundtrip that have been approved
         * @param files              the files in the old approved_roundtrip which should be deleted
         * @return the body as a string
         * @see #formatFiles(java.util.Collection)
         */
        protected String formatBody(String fileDeletionBody,
                                    DomsItem roundtrip,
                                    DomsItem approved_roundtrip,
                                    Collection<String> files) {
            String approved_roundtrip_id = approved_roundtrip.getPath().split(":")[1].split("/")[0];
            String roundtrip_id = roundtrip.getPath().split(":")[1].split("/")[0];
            return MessageFormat.format(
                    fileDeletionBody,
                    approved_roundtrip_id,
                    roundtrip_id,
                    formatFiles(files));
            //TODO this set is BIG, does it work??
        }
        
        /**
         * Format the set to of files as a string. Will perform the "/" to "_" nessesary when working with the
         * bitrepository
         *
         * @param files the files
         * @return the set of files as a string
         */
        protected String formatFiles(Collection<String> files) {
            StringBuilder result = new StringBuilder();
            for (String file : files) {
                log.trace("Adding file '{}' to mail", file);
                result.append("\n").append(file.replaceAll("/", "_"));
            }
            return result.toString();
        }
        
        /**
         * Format the subject for the message
         *
         * @param fileDeletionSubject the pattern for the subject
         * @param roundtrip           the old approved_roundtrip which is to be deleted
         * @param approved_roundtrip  the approved_roundtrip that have been approved
         * @return the subject as a string
         */
        protected String formatSubject(String fileDeletionSubject,
                                       DomsItem roundtrip,
                                       DomsItem approved_roundtrip) {
            String approved_roundtrip_id = approved_roundtrip.getPath().split(":")[1].split("/")[0];
            String roundtrip_id = roundtrip.getPath().split(":")[1].split("/")[0];
            
            return MessageFormat.format(
                    fileDeletionSubject,
                    approved_roundtrip_id,
                    roundtrip_id);
        }
        
        
        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.doms.commit.comment")
        String provideCommitComment(ConfigurationMap map) {
            return map.getRequired("approve-delete.doms.commit.comment");
        }
        
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.doms.batch.to.roundtrip.relation")
        String provideRelation(ConfigurationMap map) {
            return map.getRequired("approve-delete.doms.batch.to.roundtrip.relation");
        }
        
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.email.addresses")
        String provideRecipientsEmail(ConfigurationMap map) {
            return map.getRequired("approve-delete.email.addresses");
        }
        
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.email.subject.pattern")
        String provideEmailSubject(ConfigurationMap map) {
            return map.getRequired("approve-delete.email.subject.pattern");
        }
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.email.body.pattern")
        String provideBodyPattern(ConfigurationMap map) {
            return map.getRequired("approve-delete.email.body.pattern");
        }
        
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("smtp.port")
        String provideSMTPPort(ConfigurationMap map) {
            return map.getRequired("smtp.port");
        }
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("smtp.host")
        String provideSMTPHost(ConfigurationMap map) {
            return map.getRequired("smtp.host");
        }
        
        @Provides
        @javax.enterprise.inject.Produces
        @Named("approve-delete.email.from.address")
        String provideFromAddress(ConfigurationMap map) {
            return map.getRequired("approve-delete.email.from.address");
        }
        
        @Provides
        protected SimpleMailer getMailer(@Named("approve-delete.email.from.address") String fromAddress,
                                         @Named("smtp.host") String smtpHost,
                                         @Named("smtp.port") String smtpPort) {
            
            
            return new SimpleMailer(fromAddress, smtpHost, smtpPort);
        }
        
        /**
         * A simple mail-sending utility.
         */
        class SimpleMailer {
            
            private final Session session;
            private String from;
            
            /**
             * Constructor for this class.
             *
             * @param from the from address field for emails.
             * @param host the smtp host to use.
             * @param port the smtp port of the host.
             */
            public SimpleMailer(String from, String host, String port) {
                this.from = from;
                Properties props = new Properties();
                props.setProperty("mail.smtp.host", host);
                props.setProperty("mail.smtp.port", port);
                session = Session.getDefaultInstance(props);
                
            }
            
            /**
             * Send a mail.
             *
             * @param to      A list of recipients.
             * @param subject The text of the email subject.
             * @param text    The text of the email.
             * @throws MessagingException
             */
            public void sendMail(List<String> to, String subject, String text) throws MessagingException {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                for (String recipient : to) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                }
                message.setSubject(subject);
                message.setText(text);
                
                Transport.send(message);
            }
        }
        
    }
    
    
}
