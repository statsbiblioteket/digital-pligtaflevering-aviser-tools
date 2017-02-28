<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryProvider" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="dk.statsbiblioteket.medieplatform.autonomous.Event" %>
<%@ page import="javaslang.control.Try" %>
<%@ page import="java.time.Duration" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Stream" %>
<%@ page import="static java.time.temporal.ChronoUnit.DAYS" %>
<%@ page import="java.io.IOException" %>
<html>
<body>
<%= new java.util.Date() %>
<h1>Overview</h1>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryProvider().apply(map);

    DomsModule domsModule = new DomsModule();

    final JspWriter finalout = out;
    %>
<h2><a href="stateManuallyStopped.jsp">Manually stopped</a></h2>

<%=
repository.query(domsModule.providesQuerySpecification(
        "Manually_stopped", "", "", "doms:ContentModel_RoundTrip")
).count()
%>

<h2><a href="stateCreated.jsp">Ready to ingest</a></h2>

<%=
repository.query(domsModule.providesQuerySpecification(
        "", "Data_Archived,Manually_stopped", "", "doms:ContentModel_RoundTrip")
).count()
%>

<h2><a href="stateIngested.jsp">Ingested</a></h2>

<%=
repository.query(domsModule.providesQuerySpecification(
        "Data_Archived", "XML_validated,Manually_stopped", "", "doms:ContentModel_RoundTrip")
).count()
%>
<%
    repository.query(domsModule.providesQuerySpecification(
            "Data_Archived", "XML_validated,Manually_stopped", "", "doms:ContentModel_RoundTrip")
    ).forEach(i -> {
        try {
            finalout.println(i.getPath() + " ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

%>

<hr/>
<%= new java.util.Date() %>

<% repository.close(); %>
</body>
</html>
