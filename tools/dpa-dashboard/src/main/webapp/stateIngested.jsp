<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryProvider" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper" %>
<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static java.time.temporal.ChronoUnit.DAYS" %>
<html>
<body>

<h1>Ingested files</h1>
<%
    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryProvider().apply(map);

    DomsModule domsModule = new DomsModule();

    String pastSuccessfulEvents = "Data_Archived";
    String futureEvents = "XML_validated,Manually_stopped";
    String oldEvents = "";
    String itemTypes = "doms:ContentModel_RoundTrip";

    final QuerySpecification querySpecification = domsModule.providesQuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, itemTypes);
    List<DomsItem> l = repository.query(querySpecification).collect(Collectors.toList());
    request.setAttribute("l", l);
%>

<c:forEach items="${l}" var="item">
    <h2><c:out value="${item.path}"/></h2>
    <table border="1">
        <c:forEach var="event" items="${item.originalEvents}">
            <tr>
                <td><c:out value="${event.success}"/></td>
                <td><c:out value="${event.eventID}"/></td>
                <td><c:out value="${event.date}"/></td>
            </tr>
            <c:if test="${not empty event.details}">
                <tr>
                    <td colspan="3"><c:out value="${event.details}"/></td>
                </tr>
            </c:if>
        </c:forEach>
    </table>
    <c:url value="setEventOnItem.jsp" var="url1">
        <c:param name="id" value="${item.domsId.id()}"/>
        <c:param name="e" value="FooBar"/>
    </c:url>
    <form action='${url1}' style='float:left;'><input type='submit' value='Redo X!'/></form>
    <form action='not-implemented' style='float:left;'><input type='submit' value='...'/></form>
    <form action='not-implemented'><input type='submit' value='...'/></form>

</c:forEach>
<%
    /*
    final JspWriter finalout = out;
    r.forEach(o -> Try.run(() -> {
        final List<Event> events = o.originalEvents();

        finalout.print("<h2>" + o.getPath() + "</h2><table border='1'>");
        for (Event event : events) {
            final Instant eventInstant = event.getDate().toInstant();
            LocalDateTime now = LocalDateTime.now();
            final LocalDateTime localDateTime = LocalDateTime.ofInstant(eventInstant, ZoneId.systemDefault());
            long daysAgo = DAYS.between(localDateTime.toLocalDate(), now.toLocalDate());

            Duration timeAgo = Duration.between(localDateTime, now);

            String then = daysAgo > 0 ? daysAgo + " days " : // months? years?
                    timeAgo.toString();

            finalout.print("<tr>" + "<td>" + event.isSuccess() + "<td>" + event.getEventID()
                    + "<td>" + event.getDate() + " (" + then + " ago)"
                    + "</tr>");
            if (event.getDetails().trim().length() > 0) {
                finalout.print("<tr><td colspan='3'><pre>" + event.getDetails() + "</pre></tr>");
            }
        }
        finalout.print("</table>\n" +
                "<form action='not-implemented' style='float:left;'><input type='submit' value='Redo X'/></form>" +
                "<form action='not-implemented' style='float:left;'><input type='submit' value='Redo Z'/></form>" +
                "<form action='not-implemented'><input type='submit' value='Manual'/></form>" +
                "<br/>" +
                "\n");
    }));
    */

    /*
    repository.lookup(new DomsId("uuid:08eccdec-6b59-46e5-974c-1768485beb1f")).allChildren().forEach(new Consumer<Object>() {
        @Override
        public void accept(final Object o) {
            try {
                finalout.print("<p>" + o + "</p>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
    */
%>
<hr/>
<%= new java.util.Date() %>

</body>
</html>
