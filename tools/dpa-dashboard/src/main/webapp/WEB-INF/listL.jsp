<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h1>${h}</h1>

<% // "l" is expected to hold a list of DomsItems. %>

<c:forEach items="${l}" var="item">
    <h2><a href="showItem.jsp?id=${item.domsId.id()}">${item.path}</a></h2>
    <%@include file="/WEB-INF/listItemEvents.jsp" %>
</c:forEach>

