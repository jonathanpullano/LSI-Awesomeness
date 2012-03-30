<%@ page import="server.SessionManager" %>
<%@ page import="server.SessionTable" %>
<%@ page import="java.util.Date" %>
<% SessionTable.Entry entry = SessionManager.sessionRequest(request.getServletContext(), request, response); %>
<html>
<body>
<br>&nbsp;<br>
<big><big><b>
<%= entry.message %>
<br>&nbsp;<br>
</b></big></big>
<form method=GET action="replace">
<input type=submit name=cmd value=Replace>&nbsp;&nbsp;
<input type=text name=NewText size=40 maxlength=512>&nbsp;&nbsp;
</form>
<form method=GET action="form.jsp">
<input type=submit value="Refresh">
</form>
<form method=GET action="logout">
<input type=submit name=cmd value=LogOut>
</form>
<p>
<%= new Date(entry.expiration) %>
</body>
</html>