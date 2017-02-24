<%@ page import="com.sun.jersey.api.client.WebResource" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="dk.statsbiblioteket.doms.central.connectors.EnhancedFedora" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.Item" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.ItemFactory" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.function.Consumer" %>
<%@ page import="java.util.stream.Stream" %>
<html>
<body>
<h2>Hello World!</h2>
<%= new java.util.Date() %>

<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/home/tra/git/digital-pligtaflevering-aviser-tools/tools/dpa-tools-ide-launchers/src/main/resources/xmlvalidate-vagrant.properties");

    CommonModule commonModule = new CommonModule();
    DomsModule domsModule = new DomsModule();
    BitRepositoryModule bitRepositoryModule = new BitRepositoryModule();

    String domsUserName = domsModule.provideDomsUserName(map);
    String domsPassword = domsModule.provideDomsPassword(map);
    final String domsURL = domsModule.provideDomsURL(map);
    String fedoraLocation = domsModule.provideDomsURL(map);
    String domsPidgeneratorUrl = domsModule.provideDomsPidGeneratorURL(map);

    int fedoraRetries = domsModule.getFedoraRetries(map);
    int fedoraDelayBetweenRetries = domsModule.getFedoraDelayBetweenRetries(map);

    EnhancedFedora efedora = domsModule.provideEnhancedFedora(domsUserName, domsPassword, fedoraLocation, domsPidgeneratorUrl, fedoraRetries, fedoraDelayBetweenRetries);

    ItemFactory<Item> itemFactory = new ItemFactory<Item>() {
        @Override
        public Item create(String id) {
            return new Item();
        }
    };

    String summaLocation = domsModule.provideSummaLocation(map);
    PremisManipulatorFactory<Item> premisManipulatorFactory = domsModule.providePremisManipulatorFactory(itemFactory);

    DomsEventStorage<Item> domsEventStorage = domsModule.provideDomsEventStorage(domsURL, domsPidgeneratorUrl, domsUserName, domsPassword, itemFactory);
    int pageSize = domsModule.providePageSize(map);

    SBOIEventIndex sboiEventIndex = new SBOIEventIndex(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize);
    WebResource webResource = domsModule.provideConfiguredFedoraWebResource(domsURL, domsUserName, domsPassword);

    DomsRepository repository = new DomsRepository(sboiEventIndex, webResource, efedora, domsEventStorage);

    String pastSuccessfulEvents = domsModule.providePastSuccesfulEvents(map);
    String futureEvents = domsModule.provideFutureEvents(map);
    String oldEvents = domsModule.provideOldEvents(map);
    String itemTypes = domsModule.provideItemTypes(map);

    final QuerySpecification querySpecification = domsModule.providesQuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, itemTypes);
    Stream<DomsItem> r = repository.query(querySpecification);

    final JspWriter finalout = out;

    out.println("-----<br/>");

    r.forEach(new Consumer<Object>() {

        @Override
        public void accept(final Object o) {
            try {
                finalout.print("<p>" + o + "</p>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });  // fixme: go to web page

    out.println("-----<br/>");
    repository.lookup(new DomsId("uuid:08eccdec-6b59-46e5-974c-1768485beb1f")).allChildren().forEach(new Consumer<Object>() {
        @Override
        public void accept(final Object o) {
            try {
                finalout.print("<p>" + o + "</p>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
%>
</body>
</html>
