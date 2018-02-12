<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<table border="1" style="border-collapse:collapse;">
    <c:forEach var="event" items="${item.originalEvents}">
        <tr>
            <td>${event.success}</td>
            <td>${event.eventID}</td>
            <td>${event.date}</td>
            <td>
                <form action="deleteEventOnItem.jsp">
                    <input type="submit" value="Fjern ${event.eventID}"/>
                    <input type="hidden" name="e" value="${event.eventID}"/>
                    <input type="hidden" name="id" value="${item.domsId.id()}"/>
                </form>
            </td>
        </tr>
        <c:if test="${not empty event.details}">
            <tr>
                <td colspan="4">
            <tt>${fn:substring(event.details, 0, 1000)}</tt>
        </td>
    </tr>
</c:if>
</c:forEach>
</table>
<form action='setEventOnItem.jsp' method="get">
    <input type="hidden" name="id" value="${item.domsId.id()}"/>
    <input type="hidden" name="e" value="Manually_stopped"/>
    <input type='submit' value='S&aelig;t Manually_stopped'/>
</form>
