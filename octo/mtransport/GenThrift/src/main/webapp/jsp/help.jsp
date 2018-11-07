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

    <title>GenThrift</title>

    <script>
        $(document).ready(function () {

            $("#logout").click(function () {
                window.location.href = "/logout";
            });


        })
    </script>

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
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=356201750">MTthrift 快速入门</a>
            </li>
            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=125409156">Thrift单元测试</a>
            </li>

            <li class="active">
                <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=128131493">MTthrift业务代码框架</a>
            </li>

            <%--<li class="active">--%>
            <%--<a href="/genidl/namespace">自动生成IDL</a>--%>
            <%--</li>--%>

            <li class="active">
                <a href="/help" style="color: #5cb85c">使用说明</a>
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
                if (misInfo != null) {

                    out.println("<li class='label'> <h5><button  id='logout' class='btn-link' style='color: #ffffff;'>退出</button></h5></li>");
                    out.println("<li class='label'> <h5><button  class='btn-link' style='color: #ffffff;'>" + misInfo.getName() + "</button></h5></li>");
                }

            %>

        </ul>
    </div>
</div>

<div class="container">

    <div class="page-header">
        <h3 style="text-align:center">GenThrift使用说明</h3>
    </div>
    <br>

    <div class="alert alert-success" role="alert">
        <strong style="font-size: 15px">1.如果目标文件中使用include语法包含了其他thrift文件，请添加相应文件，支持批量上传</strong>
    </div>

    <div class="alert alert-success" role="alert">
        <strong style="font-size: 15px">2.点击提交按钮，目标代码会自动下载到本地，默认压缩格式</strong>
    </div>

    <div class="alert alert-success" role="alert">
        <strong style="font-size: 15px">3.选择AppKey，会自动根据AppKey进行IDL文件版本管理，若不想使用此功能，可选择简单使用</strong>
    </div>

    <div class="alert alert-success" role="alert">
        <strong style="font-size: 15px">4.若编译语言为java，下载文件中的ThriftTestDemo包含了自动生成的测试代码框架，只需实现相应的方法，便可Junit单元测试</strong>
    </div>

    <div class="alert alert-success" role="alert">
        <strong style="font-size: 15px">5.若编译语言为java，下载文件中的BusinessFrame包含了自动生成的MTthrift业务代码框架，只需实现相应的方法，便可使用MTthrift</strong>
    </div>

    <div class="alert alert-danger" role="alert">
        <strong style="font-size: 15px">注：项目正在成长，若有问题请及时反馈，谢谢！大象：高升</strong>
    </div>


</div>

</div>
<h4 style="text-align:center">© Copyright 2015 by meituan</h4>
</body>
</html>

