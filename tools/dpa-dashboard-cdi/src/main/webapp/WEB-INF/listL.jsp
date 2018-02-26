<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@include file="header.jsp"%>
<% // fixme: use c:url %>
<a href="listStates.jsp"><h1>${h}</h1></a>

<% // "l" is expected to hold a list of DomsItems. %>

<c:forEach items="${l}" var="item">
    <h2><a href="ShowItem?id=${item.domsId.id()}">${item.path}</a></h2>
    <%@include file="/WEB-INF/listItemEvents.jsp" %>
</c:forEach>

<%@include file="footer.jsp"%>
