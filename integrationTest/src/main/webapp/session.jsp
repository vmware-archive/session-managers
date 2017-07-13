<%@ page contentType="text/plain"
    import="java.net.InetAddress"
    import="java.util.Date"
%><%
    Integer counter = (Integer) session.getAttribute("counter");
    if (counter == null) {
        counter = 0;
    }
    ++counter;
    session.setAttribute("counter", counter);
%>server info      : <%= request.getServletContext().getServerInfo() %>
java info        : <%= System.getProperty("java.runtime.name") %>/<%= System.getProperty("java.runtime.version") %>
inet address      : <%= InetAddress.getLocalHost() %>
session id       : <%= request.getSession().getId() %>
session new?     : <%= request.getSession().isNew() %>
session created  : <%= new Date(request.getSession().getCreationTime()) %>
session accessed : <%= new Date(request.getSession().getLastAccessedTime()) %>
session counter  : <%= counter %>
