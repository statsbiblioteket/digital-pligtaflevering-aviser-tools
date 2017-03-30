<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="java.io.IOException" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification" %>
<html>
<body>
<i>Note:  New events require a Summa update job to run before being reflected in this overview!</i>

<%= new java.util.Date() %>
<h1>Overview</h1>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryConfigurator().apply(map);

    DomsModule domsModule = new DomsModule();

    final JspWriter finalout = out;
    %>
<h2><a href="stateManuallyStopped.jsp">Manually stopped</a></h2>

<%=
repository.count(new SBOIQuerySpecification("recordBase:doms_sboi_dpaCollection"))
%>

<h2><a href="stateCreated.jsp">Ready to ingest</a></h2>

<%=
repository.count(new SBOIQuerySpecification("recordBase:doms_sboi_dpaCollection"))
%>

<h2><a href="stateIngested.jsp">Ingested</a></h2>


<%=
repository.count(new SBOIQuerySpecification("recordBase:doms_sboi_dpaCollection"))
%>
<%
    repository.query(domsModule.providesWorkToDoQuerySpecification(
            "Data_Archived", "Manually_stopped", "", "doms:ContentModel_DPARoundTrip")
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
