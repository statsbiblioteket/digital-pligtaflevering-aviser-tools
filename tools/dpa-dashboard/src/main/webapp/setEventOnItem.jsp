<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryConfigurator" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<h1>Set event ${param.id}</h1>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryConfigurator().apply(map);
%>
<c:choose>
    <c:when test="${empty param.id}">
        Bad parameters.
    </c:when>
    <c:when test="${empty param.e}">
        Bad parameters.
    </c:when>
    <c:when test="${param.doIt == 'yes'}">
        <%

            String id = request.getParameter("id");
            String eventName = request.getParameter("e");

            final String outcomeParameter = request.getParameter("outcome");
            final String message = request.getParameter("message");

            boolean outcome = outcomeParameter == null ? true : Boolean.parseBoolean(outcomeParameter);

            DomsItem item = repository.lookup(new DomsId(id));
            request.setAttribute("item", item);

            item.appendEvent("dashboard", new java.util.Date(), message == null ? "" : message, eventName, outcome);

        %>
        <c:url value="showItem.jsp" var="showItemUrl">
            <c:param name="id" value="${param.id}"/>
        </c:url>
        <c:redirect url="${showItemUrl}"/>
    </c:when>
    <c:otherwise>
        <form action='setEventOnItem.jsp'>
            <label>Reason: <input type="text" name="message"/></label>
            <input type="hidden" name="id" value="${param.id}"/>
            <input type="hidden" name="e" value="${param.e}"/>
            <input type="hidden" name="doIt" value="yes"/>
            <input type='submit' value='Set ${param.e}'/>
        </form>
    </c:otherwise>
</c:choose>

<hr/>

<%= request.getParameterMap() %>
<%= new java.util.Date() %>
