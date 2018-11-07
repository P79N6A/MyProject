<%@ page import="com.meituan.service.mobile.thrift.model.IdlFile" %>
<%--
  Created by IntelliJ IDEA.
  User: gaosheng
  Date: 14-01-20
  Time: 上午11:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    IdlFile idlFile = (IdlFile) session.getAttribute("idlFile");
    if(idlFile != null)
        out.println(idlFile.getContent());

%>
