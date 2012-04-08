<%@ page import="java.util.Date" %>
<html>
<body>
<br>&nbsp;<br>
<big><big><b>
${data.message}
<br>&nbsp;<br>
</b></big></big>
<form method=GET action="replace">
<input type=submit name=cmd value=Replace>&nbsp;&nbsp;
<input type=text name=NewText size=40 maxlength=512>&nbsp;&nbsp;
</form>
<form method=GET action="memberRefresh">
<input type=submit value="Member Refresh">
</form>
<form method=GET action="serverCrash">
<input type=submit value="Server Crash">
</form>
<form method=GET action="form">
<input type=submit value="Refresh">
</form>
<form method=GET action="logout">
<input type=submit name=cmd value=LogOut>
</form>
<p>
${data.expiration}
</p>
<p>
ServerID: ${data.serverID}
</p>
<p>
MemberSet: ${data.memberSet}
</p>
${data.HTML}
${data.eviction}
</body>
</html>