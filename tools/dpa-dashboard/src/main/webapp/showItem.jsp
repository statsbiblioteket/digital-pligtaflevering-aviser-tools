<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryProvider" %>
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

            DomsRepository repository = new RepositoryProvider().apply(map);
            item = repository.lookup(new DomsId(request.getParameter("id")));
            request.setAttribute("item", item);
        }
    }
%>
<h2><c:out value="${item.path}"/></h2>
<table border="1">
    <c:forEach var="event" items="${item.originalEvents}">
        <tr>
            <td><c:out value="${event.success}"/></td>
            <td><c:out value="${event.eventID}"/></td>
            <td><c:out value="${event.date}"/></td>
            <td><form action="foo"><input type="submit" value="Set ${event.eventID}"/></form></td>
        </tr>
        <c:if test="${not empty event.details}">
            <tr>
                <td colspan="4"><c:out value="${event.details}"/></td>
            </tr>
        </c:if>
    </c:forEach>
</table>
