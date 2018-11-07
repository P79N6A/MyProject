<%@ page import="com.meituan.service.mobile.thrift.model.MISInfo" %>
<!DOCTYPE html>

<html lang="en">
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

    <!-- 新 Bootstrap 核心 CSS 文件 -->
    <link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
    <script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>

    <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
    <script src="/bootstrap/js/bootstrap.min.js"></script>

    <script>
        $(document).ready(function () {

            $("#logout").click(function () {
                window.location.href = "/logout";
            });


        })
    </script>

    <title>GenThrift</title>

</head>

<body>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class='navbar navbar-inverse'>
    <div class='navbar-inner nav-collapse' style="height: auto;">

        <ul class="nav navbar-nav">
            <li class="active">
                <a href="/">首页</a>
            </li>
            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=102631218">Thrift接口描述语言</a>
            </li>
            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=115420285">MTthrift1.5版本使用说明</a>
            </li>
            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=125409156">Thrift单元测试</a>
            </li>
            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=128131493">MTthrift业务代码框架</a>
            </li>
            <li class="active">
                <a href="/genidl/namespace.jsp">自动生成IDL</a>
            </li>
            <li class="active">
                <a href="/help.html">使用说明</a>
            </li>

            <li class="label">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </li>
            <li class="label">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </li>
            <li class="label">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </li>

            <%
                MISInfo misInfo = (MISInfo) session.getAttribute("misInfo");
                if(misInfo != null){

                    out.println("<li class='label'> <h5><button  id='logout' class='btn-link' style='color: #ffffff;'>退出</button></h5></li>");
                    out.println("<li class='label'> <h5><button  class='btn-link' style='color: #ffffff;'>"+misInfo.getName()+"</button></h5></li>");
                }

            %>

        </ul>
    </div>
</div>

<div class="page-header">
    <h3 style="text-align:center">提示</h3>
</div>
<br>

<h1 style="text-align:center">服务端出现错误!</h1>

<br><br><br><br><br><br><br><br><br><br><br><br><br><br>

<div class="page-header">
</div>


<h4 style="text-align:center">Copyright 2014 by <a href="mailto:#">gaosheng@meituan.com</a></h4>

</body>
</html>

