<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.SBOIConstants.Q_READY_TO_INGEST" %>
<html>
<body>

<h1>Klar til Ingest</h1>
<% pageContext.setAttribute("newline", "\n"); %>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");
    DomsRepository repository = new RepositoryConfigurator().apply(map);

    List<DomsItem> l = repository.query(new SBOIQuerySpecification(Q_READY_TO_INGEST))
            .collect(Collectors.toList());
    request.setAttribute("l", l); // so ${l} works
%>


<c:forEach items="${l}" var="item">
    <h2><a href="showItem.jsp?id=${item.domsId.id()}">${item.path}</a></h2>
    <table border="1" style="border-collapse:collapse;">
        <c:forEach var="event" items="${item.originalEvents}">
            <tr>
                <td>${event.success}</td>
                <td>${event.eventID}</td>
                <td>${event.date}</td>
                <td>
                    <form action="deleteEventOnItem.jsp">
                        <input type="submit" value="Delete ${event.eventID}"/>
                        <input type="hidden" name="e" value="${event.eventID}"/>
                        <input type="hidden" name="id" value="${item.domsId.id()}"/>
                    </form>
                </td>
            </tr>
            <c:if test="${not empty event.details}">
                <tr>
                    <td colspan="4">
                        <c:set var="detailsLines" value="${fn:split(event.details, newline)}"/>
                        <c:choose>
                            <c:when test="${fn:length(detailsLines) > 10}">
                                ${fn:length(detailsLines)} linier
                                <c:forEach var="i" begin="1" end="10">
                                    <pre>${detailsLines[i]}</pre>
                                </c:forEach>
                                ...
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="detailsLine" items="${detailsLines}">
                                    <pre>${detailsLine}</pre>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:if>
        </c:forEach>
    </table>
    <form action='setEventOnItem.jsp' method="get">
        <input type="hidden" name="id" value="${item.domsId.id()}"/>
        <input type="hidden" name="e" value="Manually_stopped"/>
        <input type='submit' value='Set Manually_stopped'/>
    </form>
</c:forEach>

<hr/>
<%= new java.util.Date() %>

</body>
</html>
