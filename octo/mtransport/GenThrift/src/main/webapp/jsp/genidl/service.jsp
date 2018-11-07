<%@ page import="com.meituan.service.mobile.thrift.model.IdlFile" %>
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

    <script LANGUAGE="JavaScript">
        $(document).ready(function () {
            getIdl();

            $("#cleanService").click(function () {
                $.get("/genidl/cleanService", function (data, status) {
                    location.href = '/genidl/service'
                })

            });

            $("#logout").click(function () {
                window.location.href = "/logout";
            });
        })

        function getIdl() {
            $.get("/genidl/idl", function (data, status) {
                document.getElementById("idlFile").innerHTML = data;
            });
        }

        var count = 0;
        var i;
        function addService() {
            var content = document.getElementById("content");
            var pNode = document.createElement("p");
            count = count + 1;
            i = 0;

            pNode.innerHTML = "<br>返回类型：<input name='methodType" + count + "' placeholder='type' required autofocus style='width: 100px'>";

            pNode.innerHTML += "&nbsp;&nbsp;&nbsp;&nbsp;方法名：<input type='text' name='methodName" + count + "' placeholder='method' required autofocus style='width: 100px'>";



            pNode.innerHTML += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button type='button' class='btn btn-default' onclick='addParam(count)'>添加参数 &raquo;</button>";
            pNode.innerHTML += "<div id='method" + count + "'><br></div>"
            content.appendChild(pNode);
        }


        function addParam(count) {

            var methodForm = document.getElementById("method" + count);
            i = i + 1;

            var pNode = document.createElement("p");

            pNode.innerHTML = "参数名：<input type='text' name='parameterName" + count + i + "' placeholder='name' required autofocus style='width: 100px'>";

            pNode.innerHTML += "&nbsp;&nbsp;&nbsp;&nbsp;参数类型：<input name='parameterType" + count + i + "' placeholder='type' required autofocus style='width: 100px'>";

            methodForm.appendChild(pNode);
        }

    </script>

    <title>GenThrift</title>

</head>

<body>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--生成Idlfile对象--%>
<%
    IdlFile idlFile = (IdlFile) session.getAttribute("idlFile");
    if (idlFile == null) {
        idlFile = new IdlFile();
        session.setAttribute("idlFile", idlFile);
    }
%>

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
                <a href="/genidl/namespace">自动生成IDL</a>
            </li>

            <li class="active">
                <a href="/help">使用说明</a>
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

<div class="container">
    <form action="/genidl/service" method="post">
        <br>

        <div class="row">

            <div class="col-md-1">
            </div>
            <div class="col-md-6">
                <br><br>
                <span class="label label-success" style="border-radius: 50%">4</span>&nbsp;&nbsp;
                <button type="button" class="btn btn-default" onclick="addService()">添加服务方法 &raquo;</button>

                &nbsp;&nbsp;&nbsp;&nbsp;服务名：<input type='text' name='serviceName' placeholder='Service' required
                                                   autofocus style='width: 100px'>

                <div id="content">
                    <br>
                </div>

            </div>
            <div class="col-md-5">
                <h4>IDL文件</h4>
                <textarea id="idlFile"
                          style="border: 2px solid #5cb85c;margin: 0px; width: 320px; height: 420px; resize: none;"></textarea>
            </div>

        </div>

        <div class="row">

            <div class="col-md-1">
            </div>

            <div class="col-md-5">

                <input id="lastStep" type="button" class="btn btn-default" value="上一步"
                       onclick="location.href = '/genidl/struct'"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input id="cleanService" type="button" class="btn btn-default" value="清空服务"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input id="submit" type="submit" class="btn btn-default" value="保 存"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input id="skip" type="button" class="btn btn-default" value="下一步"
                       onclick="location.href = '/genidl/compile'"/>

            </div>

            <div class="col-md-6">
            </div>

        </div>
    </form>
</div>


<div class="page-header">

</div>
</div>
<h4 style="text-align:center">Copyright 2014 by <a href="mailto:#">gaosheng@meituan.com</a></h4>
</form>
</body>
</html>
