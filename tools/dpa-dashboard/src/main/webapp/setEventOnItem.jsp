<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryProvider" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<h1>Set event on item</h1>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryProvider().apply(map);
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

            DomsItem item = repository.lookup(new DomsId(id));
            request.setAttribute("item", item);

            String doIt = request.getParameter("doIt");
        %>
        DO IT!
    </c:when>
    <c:otherwise>
        <c:url value="setEventOnItem.jsp" var="url">
            <c:param name="id" value="${param.id}"/>
            <c:param name="e" value="${param.e}"/>
            <c:param name="doIt" value="yes"/>
        </c:url>
        <a href="${url}">Please confirm you want to set the event <c:out value="${param.e}"/> for <c:out value="${item}"/>.</a>
    </c:otherwise>
</c:choose>

<hr/>

<%= request.getParameterMap() %>
<%= new java.util.Date() %>
