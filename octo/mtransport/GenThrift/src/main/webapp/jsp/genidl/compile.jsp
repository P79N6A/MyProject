<%@ page import="com.meituan.service.mobile.thrift.model.IdlFile" %>
<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftResult" %>
<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftFlag" %>
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

            $("#compile").click(function () {


                var content = document.getElementById("idlFile").value;
                if(!IsNull(replaceHTML(content))){
                    alert("IDL文件为空！")
                    return ;
                }

                var form = $("<form>");
                form.attr("style", "display:none");
                form.attr("method", "post");
                form.attr("action", "/genidl/compile");

                var language = $("<input>");
                language.attr("type", "hidden");
                language.attr("name", "language");
                language.attr("value", $("#language").val());

                var idlFile = $("<input>");
                idlFile.attr("type", "hidden");
                idlFile.attr("name", "idlFile");
                idlFile.attr("value", $("#idlFile").val());

                var thriftVersion = $("<input>");
                thriftVersion.attr("type", "hidden");
                thriftVersion.attr("name", "thriftVersion");
                thriftVersion.attr("value", $("#thriftVersion").val());

                var genFrame = $("<input>");
                genFrame.attr("type", "hidden");
                genFrame.attr("name", "genFrame");
                genFrame.attr("value", $("#genFrame").val());

                $("body").append(form);
                form.append(language);
                form.append(idlFile);
                form.append(thriftVersion);
                form.append(genFrame);
                form.submit();
            });

            $("#compile").click(function () {
                setTimeout(submitHandle, 500)
            });

            $("#clean").click(function () {
                $.get("/genidl/cleanidl", function (data, status) {
                    location.href = '/genidl/compile'
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

        function submitHandle() {
            $.get("/result", function (data, status) {
                document.getElementById("result").innerHTML = data;
            });
        }


        function IsNull(str) {
            return (Trim(str) == "") ? false : true;
        }
        function Trim(str) {
            return str.replace(/(^\s*)|(\s*$)/g, "");
        }
        function replaceHTML(str){
            str = str.replace(/<[^>].*?>/g,"");
            str = str.replace(/ /g,"");
            return str;
        }



    </script>


    <style>
        .divcss {
            width: 550px;
            height: 200px;
            border: 2px solid #5cb85c
        }
    </style>


    <title>GenThrift</title>

</head>

<body id="body">
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
    <br>

    <div class="row">

        <div class="col-md-1">
        </div>
        <div class="col-md-7">
            <br><br>
            <span class="label label-success" style="border-radius: 50%">5</span>&nbsp;&nbsp;选择语言
            <select id="language" class="form-control" style="display: inline-block;width: 80px;">
                <option>java</option>
                <option>c++</option>
                <option>php</option>
                <option>python</option>
                <option>go</option>
            </select>

            <br><br>
            <span class="label label-success" style="border-radius: 50%">6</span>&nbsp;&nbsp;选择版本
            <select id="thriftVersion" class="form-control" style="display: inline-block;width: 80px;">
                <option>0.8.0</option>
                <%--<option>0.9.2</option>--%>
            </select>

            <br><br>
            <span class="label label-success" style="border-radius: 50%">7</span>&nbsp;&nbsp;生成框架
            <select id="genFrame" class="form-control" style="display: inline-block;width: 80px;">
                <option>no</option>
                <option>yes</option>
            </select>


            <br><br>
            <h4>执行结果</h4>

            <div class="divcss">
                <p id="result">
                    <%
                        GenThriftResult result = (GenThriftResult) session.getAttribute("result");

                        if (result != null) {
                            if (result.getError() != null && result.getFlag() == GenThriftFlag.RUNERROR)
                                out.println(result.getError());
                            result.setFlag(GenThriftFlag.NOTRUN);
                            result.setError("");
                        }
                    %>
                </p>
            </div>


        </div>
        <div class="col-md-4">
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
                   onclick="location.href = '/genidl/service'"/>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input id="clean" type="button" class="btn btn-default" value="清空IDL"/>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input id="compile" type="button" class="btn btn-default" value="编 译"/>
        </div>


        <div class="col-md-6">

        </div>

    </div>
</div>

<div class="page-header">

</div>
</div>
<h4 style="text-align:center">Copyright 2014 by <a href="mailto:#">gaosheng@meituan.com</a></h4>
</form>
</body>
</html>

