<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftResult" %>
<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftFlag" %>
<%--
  Created by IntelliJ IDEA.
  User: gaosheng
  Date: 14-12-18
  Time: 上午11:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    GenThriftResult result = (GenThriftResult) session.getAttribute("result");
    if(result != null)
        if(GenThriftFlag.RUNSUCCESS == result.getFlag())
            out.println("Success!目标文件已经下载到本地！");
%>