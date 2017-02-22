<%@ page import="com.sun.jersey.api.client.WebResource" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="dk.statsbiblioteket.doms.central.connectors.EnhancedFedora" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.Delivery" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorage" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.Item" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.ItemFactory" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex" %>
<%@ page import="javaslang.control.Try" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
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
    String fedoraLocation = domsModule.provideDomsURL(map);
    String domsPidgeneratorUrl = domsModule.provideDomsPidGeneratorURL(map);

    int fedoraRetries = domsModule.getFedoraRetries(map);
    int fedoraDelayBetweenRetries = domsModule.getFedoraDelayBetweenRetries(map);

    EnhancedFedora efedora = domsModule.provideEnhancedFedora(domsUserName, domsPassword, fedoraLocation, domsPidgeneratorUrl, fedoraRetries, fedoraDelayBetweenRetries);

    String type = null;

    String eventsDataStream = null;
    String deliveryTemplate = null;
    String roundTripTemplate = null;
    String hasPart_relation = null;
    ItemFactory<Delivery> itemFactory = null;
    DeliveryDomsEventStorage deliveryDomsEventStorage = Try.of(() -> new DeliveryDomsEventStorage(efedora, type, deliveryTemplate, roundTripTemplate, hasPart_relation, eventsDataStream, itemFactory)).get();

    String summaLocation = null;
    PremisManipulatorFactory<Item> premisManipulatorFactory = null;
    int pageSize = 0;

    SBOIEventIndex sboiEventIndex = new SBOIEventIndex(summaLocation, premisManipulatorFactory, deliveryDomsEventStorage, pageSize);
    WebResource webResource = null;

    DomsEventStorage<Item> domsEventStorage = null;
    DomsRepository repository = new DomsRepository(sboiEventIndex, webResource, efedora, domsEventStorage);

    List<String> pastSuccessfulEvents = new ArrayList<>();
    List<String> futureEvents = new ArrayList<>();
    List<String> oldEvents = new ArrayList<>();
    List<String> types = new ArrayList<>();
    boolean details = true;
    Stream<DomsItem> r = repository.query(new QuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, types, details));

    r.forEach(System.out::println);  // fixme: go to web page

%>
</body>
</html>
