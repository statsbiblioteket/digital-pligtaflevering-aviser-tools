<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.ServletContextHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="static java.time.temporal.ChronoUnit.DAYS" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<html>
<body>

<h1>Ingested files</h1>
<%
    {
        ConfigurationMap map = new ConfigurationMap(ServletContextHelper.getInitParameterMap(request.getServletContext()));

        DomsRepository repository = new RepositoryConfigurator().apply(map);

        DomsModule domsModule = new DomsModule();

        String pastSuccessfulEvents = "Data_Archived";
        String futureEvents = "Manually_stopped";
        String oldEvents = "";
        String itemTypes = "doms:ContentModel_DPARoundTrip";

        // domsModule.providesWorkToDoQuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, itemTypes);
        final QuerySpecification querySpecification = new SBOIQuerySpecification(
                "+item_model:\"doms:ContentModel_DPARoundTrip\" AND +success_event:Data_Archived"
        );
        List<DomsItem> l = repository.query(querySpecification).collect(Collectors.toList());
        request.setAttribute("l", l);
    }
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
                        <Disabledpre>${fn:substring(event.details, 0, 1000)}</Disabledpre>
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