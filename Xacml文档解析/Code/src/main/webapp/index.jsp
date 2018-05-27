<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: DELL
  Date: 2018/4/22
  Time: 21:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Xacml文档管理系统</title>
</head>
<body>
<h2 align="center" valign="center">Xacml文档管理系统</h2>
<form action="form" enctype="multipart/form-data" method="post">
    <table align="center" valign="center">
        <tr>
            <td>文件描述:</td>
            <td><input type="text" name="description"></td>
        </tr>
        <tr>
            <td>请选择文件:</td>
            <td><input type="file" name="file"></td>
        </tr>
        <tr>

            <td><input type="button" value="查看axcml策略" type="submit" onclick="form.action='upload'; form.submit();"></td>
        </tr>
        <tr>
            <td><input type="button" value="新建xacml策略" type="submit" onclick="form.action='/toBuild'; form.submit();" ></td>
        </tr>
    </table>
</form>
</body>
</html>
