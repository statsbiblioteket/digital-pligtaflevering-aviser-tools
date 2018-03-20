<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@include file="header.jsp"%>
<% // fixme: use c:url %>
<h1><a href="listStates.jsp">${h}</a></h1>

<% // "l" is expected to hold a list of DomsItems. %>

<c:forEach items="${l}" var="item">
    <c:set var="event" value="${item.originalEvents[fn:length(item.originalEvents) - 1]}" />
    <!--c:set var="event" value="${item.originalEvents[0]}" /-->
    <h2><a href="ShowItem?id=${item.domsId.id()}">${item.path}</a> - ${fn:length(item.originalEvents)} events</h2>
    <%@include file="/WEB-INF/listSingleItemEvent.jsp" %>
</c:forEach>

<%@include file="footer.jsp"%>
