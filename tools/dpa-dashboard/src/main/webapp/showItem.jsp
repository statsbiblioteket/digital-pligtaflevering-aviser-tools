<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%
    {
        DomsItem item = (DomsItem) request.getAttribute("item");
        if (item == null) {
            item = (DomsItem) pageContext.getAttribute("item");  // c:forEach var="item"
        }
        if (item == null) {
            ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

            DomsRepository repository = new RepositoryConfigurator().apply(map);
            item = repository.lookup(new DomsId(request.getParameter("id")));
            request.setAttribute("item", item);
        }
    }
%>
<h2><c:out value="${item.path}"/></h2>
Id: ${item.domsId.id()}

<p/>
<table border="1" style="border-collapse:collapse;">
    <c:forEach var="event" items="${item.originalEvents}">
        <tr>
            <td><c:out value="${event.success}"/></td>
            <td><c:out value="${event.eventID}"/></td>
            <td><c:out value="${event.date}"/></td>
        </tr>
        <c:if test="${not empty event.details}">
            <tr>
                <td colspan="3"><i>${event.details}</i></td>
            </tr>
        </c:if>
    </c:forEach>
</table>
<p/>
Data streams:

<table border="1" style="border-collapse:collapse;">
    <c:forEach var="datastream" items="${item.datastreams()}">
        <tr>
            <td>${datastream.id}</td>
            <td>${datastream.mimeType}</td>
            <td>${datastream.url}</td>
        </tr>
    </c:forEach>
</table>

