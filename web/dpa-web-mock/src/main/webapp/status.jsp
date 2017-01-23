<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.web.mock.SampleData" %>
<html>
<body>
<h2>Digital pligtaflevering Aviser</h2>

Fejlet:

<ul>
    <% for (SampleData s : SampleData.successData) { %>
    <li><a href="delivery.jsp?id=<%= s.getDelivery() %>"><%= s.getDelivery() %></a></li>
    <% } %>
</ul>

Ok:

<ul>

    <li><a href="delivery.jsp?id=dl_20160201_rt1">dl_20160201_rt1</a></li>
    <li><a href="delivery.jsp?id=dl_20160202_rt1">dl_20160202_rt1</a></li>
    <li><a href="delivery.jsp?id=dl_20160203_rt1">dl_20160203_rt1</a></li>
    <li><a href="delivery.jsp?id=dl_20160204_rt1">dl_20160204_rt1</a></li>
</ul>
</body>

<hr/>
<%= new java.util.Date() %>
</html>
