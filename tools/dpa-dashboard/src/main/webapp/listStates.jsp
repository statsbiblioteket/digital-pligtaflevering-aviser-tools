<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.SBOIConstants" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.ServletContextHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<html>
<body>
<i>Bem&aelig;rk: Data fremvist afspejler den aktuelle tilstand i SBIO Summa. Data &aelig;ndres direkte i DOMS, og
    der g&aring;r "noget tid" inden SBOI Summa-indekset opdateres!</i>
<p/>
<%= new java.util.Date() %><%!
%>
<h1>Oversigt</h1>
<%
    ConfigurationMap map = new ConfigurationMap(ServletContextHelper.getInitParameterMap(request.getServletContext()));

    DomsRepository repository = new RepositoryConfigurator().apply(map);

    DomsModule domsModule = new DomsModule();

    final JspWriter finalout = out;
%>
<table border="1" style="border-collapse:collapse;">
    <th>Tilstand</th>
    <th>Antal</th>
    <tr>
        <td>
            <a href="StateCreated">Klar til ingest</a>
        </td>
        <td align="right">
            <%=
            repository.count(new SBOIQuerySpecification(SBOIConstants.Q_READY_TO_INGEST))
            %>
        </td>
    </tr>
    <tr>
        <td>
            <a href="StateIngested">Ingestet</a>
        </td>
        <td align="right">
            <%=
            repository.count(new SBOIQuerySpecification("recordBase:doms_sboi_dpaCollection"))
            %>
        </td>
    </tr>
    <tr>
        <td>
            <a href="StateIngestFailed">Ingest fejlet</a>
        </td>
        <td align="right">
            <%=
            repository.count(new SBOIQuerySpecification(SBOIConstants.Q_INGEST_FAILED))
            %>
        </td>
    </tr>
    <tr>
        <td>
            <a href="StateManualControl">Manuelt styret</a>
        </td>
        <td align="right">
            <%=
            repository.count(new SBOIQuerySpecification(SBOIConstants.Q_MANUAL_CONTROL))
            %>
        </td>
    </tr>
</table>

<hr/>
<%= new java.util.Date() %>

<% repository.close(); %>
</body>
</html>
