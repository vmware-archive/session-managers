<%@page contentType="text/plain"
        pageEncoding="UTF-8"
        import="java.text.*,java.util.*"
%><%
    SortedSet<String> names = new TreeSet<String>(System.getProperties().stringPropertyNames());
    for (String name : names) {
        out.print(name);
        out.print('=');
        out.println(System.getProperty(name));
    }
%>
