<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<table border="1" style="border-collapse:collapse;">
    <tr>
        <td>${event.success}</td>
        <td>${event.eventID}</td>
        <td>${event.date}</td>
        <td>
        </td>
    </tr>
    <c:if test="${not empty event.details}">
        <tr>
            <td colspan="4">
                <tt>${fn:substring(event.details, 0, 1000)}</tt>
            </td>
        </tr>
    </c:if>
</table>
