<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<%--
  Created by IntelliJ IDEA.
  User: DELL
  Date: 2018/4/22
  Time: 21:48
  To change this template use File | Settings | File Templates.
--%>

<html>
<head>
    <title>上传文件</title>
</head>
<body>
<h2 align="center" valign="center">文件上传</h2>
<h2>文件上传实例</h2>

<form action="/upload" method="post" enctype="multipart/form-data">
    选择文件:<input type="file" name="file">
    <input type="submit" value="提交">
</form>
</body>
</html>
