<%-- 
    Document   : index
    Created on : 20-06-2017, 14:25:47
    Author     : tra
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title></title>
</head>
<body>
<h1>Digital pligtaflevering af aviser</h1>

<blockquote border="1">VÆR OPMÆRKSOM PÅ AT DATA HER VISES UD FRA ET SBOI SUMMA SØGEINDEKS
SOM KUN OPDATERES PERIODISK.  ÆNDRINGER VIL DERFOR TAGE TID INDEN DE SLÅR IGENNEM!</blockquote>

Denne brugerflade giver tilgang til deliveries, så man fx kan

<ul>
    <li>Se resultater fra kørsler af de autonome komponter,
    <li>Sætte deliveries under manuel styring.</li>
    <li>Fjerne events så en eller flere autonome komponenter køres igen.</li>
</ul>


<a href="listStates.jsp">Oversigt over deliveries i forskellige tilstande</a>.
<hr/>
<%= new java.util.Date() %>
</body>
</html>
