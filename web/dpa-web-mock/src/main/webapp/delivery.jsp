<%@ page import="dk.statsbiblioteket.digital_pligtaflevering_aviser.web.mock.SampleData" %><%

    String s = request.getParameter("id");
    SampleData delivery = SampleData.successData.stream()
            .filter(d -> d.getDelivery().equals(s))
            .findAny()
            .orElseThrow(() -> new RuntimeException("Unknown delivery " + s));
    %>

<a href="status.jsp">status</a>
<p>
delivery <b><%= delivery %></b>!

events:
<ul>
    <% for(SampleData.SampleEvent e: delivery.getEvents()) { %>

    <li><%= e %>!</li>


    <% } %>
    </ul>


</p>
