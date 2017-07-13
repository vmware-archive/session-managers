<%@page contentType="text/plain"
        pageEncoding="UTF-8"
        import="java.lang.management.*"
%><%
    for (String arg: ManagementFactory.getRuntimeMXBean().getInputArguments()) {
        out.println(arg);
    }
%>
