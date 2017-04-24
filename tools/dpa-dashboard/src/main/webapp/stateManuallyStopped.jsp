<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static java.time.temporal.ChronoUnit.DAYS" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<html>
<body>

<h1>Manually stopped</h1>
<%
    {
        ConfigurationMap map = new ConfigurationMap(ServletContextHelper.getInitParameterMap(request.getServletContext()));

        DomsRepository repository = new RepositoryConfigurator().apply(map);

        DomsModule domsModule = new DomsModule();

        String pastSuccessfulEvents = "Manually_stopped";
        String futureEvents = "";
        String oldEvents = "";
        String itemTypes = "doms:ContentModel_DPARoundTrip";

        final QuerySpecification eventQuerySpecification = domsModule.providesWorkToDoQuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, itemTypes);
        List<DomsItem> l = repository.query(eventQuerySpecification).collect(Collectors.toList());
        request.setAttribute("l", l);
    }
%>

<c:forEach items="${l}" var="item">
    <%@include file="showItem.jsp" %>
    <form action='setEventOnItem.jsp' style='float:left;' method="get">
        <input type="hidden" name="id" value="${item.domsId.id()}"/>
        <input type="hidden" name="e" value="Data_Archived"/>
        <input type='submit' value='Set Data_Archived'/>
    </form>
</c:forEach>

<hr/>
<%= new java.util.Date() %>

</body>
</html>