<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@include file="header.jsp"%>


<h2><a href="listStates.jsp"><c:out value="${item.path}"/></a></h2>
<a href="<c:url value="${fedoraUrl}"/>">${item.domsId.id()}</a>

<p/>
<%@include file="listItemEvents.jsp" %>
<p/>
Data streams:

<table>
    <c:forEach var="datastream" items="${item.datastreams()}">
        <tr>
            <td><a href="<c:url value="${fedoraUrl}/datastreams/${datastream.id}"/>">${datastream.id}</a></td>
            <td><a href="<c:url value="${fedoraUrl}/datastreams/${datastream.id}/content"/>">${datastream.mimeType}</a></td>
        </tr>
    </c:forEach>
</table>

<%@include file="footer.jsp"%>
