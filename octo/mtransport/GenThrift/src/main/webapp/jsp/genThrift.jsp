<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftResult" %>
<%@ page import="com.meituan.service.mobile.thrift.result.GenThriftFlag" %>
<%@ page import="com.meituan.service.mobile.thrift.model.MISInfo" %>
<%@ page import="com.meituan.jmonitor.JMonitor" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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


            $("#submit").click(function () {
                setTimeout(submitHandle, 500)
            });

            $("#logout").click(function () {
                window.location.href = "/logout";
            });


        })


        function showAppKeyFile() {
            var appkey = document.getElementById("appkey").value;
            if (appkey.toString().trim() == "简单使用，没有AppKey") {
                document.getElementById("appKeyHref").innerHTML = "";
                return;
            }


            $.get("/exist?appkey=" + appkey, function (data, status) {
                document.getElementById("appKeyHref").innerHTML = data;
            });

        }

        function genByAppKey() {
            var appkey = document.getElementById("appkey").value;
            if (appkey.toString().trim() == "简单使用，没有AppKey")
                return;

            var form = $("<form>");
            form.attr("style", "display:none");
            form.attr("method", "post");
            form.attr("action", "/genByAppKey");

            var input = $("<input>");
            input.attr("type", "hidden");
            input.attr("name", "appkey");
            input.attr("value", appkey);

            $("body").append(form);
            form.append(input);
            form.submit();

            document.getElementById("result").innerHTML = "Success!目标文件已经下载到本地！";
            return;
        }

        //ajax请求执行结果
        function submitHandle() {
            $.get("/result", function (data, status) {
                document.getElementById("result").innerHTML = data;
            });
        }

        function CheckForm() {
            if (document.form.filename.value == "") {
                alert("请选择目标文件!");
                return false;
            }
            return true;
        }

        function addIncludeFiles() {
            var includeFiles = document.getElementById("includeFiles").files;
            var content = document.getElementById("includeFilesDiv");
            var pNode = document.createElement("p");
            for (var i = 0; i < includeFiles.length; i++) {
                pNode.innerHTML += "<h5>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + includeFiles[i].name + "</h5>";
            }
            content.appendChild(pNode);
        }

        function setFilename() {
            var targetFiles = document.getElementById("targetFiles").files;
            document.getElementById("filename").value = targetFiles[0].name;
        }
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

<div class="page-header">
    <h3 style="text-align:center">欢迎使用GenThrift</h3>
</div>

<form:form modelAttribute="genThriftForm" action="gen" method="post" name="form" enctype="multipart/form-data"
           onsubmit="return CheckForm();">
    <div class="container">

        <div class="row">

            <div class="col-md-1">
            </div>
            <div class="col-md-5">

                <h4><span class="label label-success" style="border-radius: 50%">1</span>&nbsp;&nbsp;选择AppKey
                    <br><br>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <select id="appkey" name="appkey" class="form-control" style="display: inline-block;width: 200px;"
                            onChange="showAppKeyFile();">
                            <%--<select id="appkey" name="appkey" class="form-control" style="display: inline-block;width: 200px;">--%>
                        <option>简单使用，没有AppKey</option>
                        <%
                            String appkeys = (String) session.getAttribute("appkeys");
                            if (appkeys != null) {
                                String appkey[] = appkeys.split(",");
                                for (int i = 0; i < appkey.length; i++) {
                                    out.println("<option>" + appkey[i] + "</option>");
                                }
                            }
                        %>
                    </select>
                    &nbsp;&nbsp;&nbsp;
                        <%--<button type="button" class="btn btn-default" onclick="location.href = '/oauth'">授权</button>--%>
                    <strong style="font-size: 15px;color:red">目前该功能禁用</strong>
                </h4>

                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a id="appKeyHref" onclick="genByAppKey()">

                </a>

                <br>
                <h4><span class="label label-success" style="border-radius: 50%">2</span>&nbsp;&nbsp;添加目标文件</h4>

                <label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
                    <%--<input type="text" name="filename" placeholder="filename" required autofocus style="width: 120px">&ndash;%&gt;--%>
                    <%--<span style="font-size: 20px">.thrift</span>--%>
                    <%--隐藏的filename表单--%>
                <input type="hidden" id="filename" name="filename">
                <input id="targetFiles" type='file' style="display: inline-block;width:200px;" name='idlFiles'
                       onchange="setFilename()"/>
                <br><br>

                <h4>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;添加include文件(请一次性批量添加)</h4>
                <label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
                <input id="includeFiles" type='file' style="display: inline-block;width:200px;" name='idlFiles'
                       multiple='multiple' onchange="addIncludeFiles()"/>

                <div id="includeFilesDiv">
                </div>

                <br><br>
                <h4><span class="label label-success" style="border-radius: 50%">3</span>&nbsp;&nbsp;选择语言(支持多选)
                        <%--<select name="language" class="form-control" style="display: inline-block;width: 80px;">--%>
                        <%--<option>java</option>--%>
                        <%--<option>c++</option>--%>
                        <%--<option>php</option>--%>
                        <%--<option>python</option>--%>
                        <%--<option>go</option>--%>
                        <%--</select>--%>
                </h4>

                <h4>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <label><input name="languages" type="checkbox" value="java" checked="true"/>java</label>
                    &nbsp;
                    <label><input name="languages" type="checkbox" value="php"/>php</label>
                    &nbsp;
                    <label><input name="languages" type="checkbox" value="c++"/>c++</label>
                    &nbsp;
                    <label><input name="languages" type="checkbox" value="python"/>python</label>
                    &nbsp;
                    <label><input name="languages" type="checkbox" value="go"/>go</label>
                    &nbsp;
                    <label><input name="languages" type="checkbox" value="node.js"/>node.js</label>
                </h4>
                <br>

            </div>

            <div class="col-md-6">

                <br>
                <h4><span class="label label-success" style="border-radius: 50%">4</span>&nbsp;&nbsp;选择Thrift版本
                    <select name="thriftVersion" class="form-control" style="display: inline-block;width: 80px;">
                        <option>0.8.0</option>
                        <option>0.9.2</option>
                    </select>
                    &nbsp;&nbsp;&nbsp;
                    <strong style="font-size: 15px; color:red">Mtthrift默认0.8.0,其他按需</strong>
                </h4>
                <br>
                <h4><span class="label label-success" style="border-radius: 50%">5</span>&nbsp;&nbsp;
                    <input id="submit" type="submit" class="btn btn-default" style="width: 80px"/></h4>
                <br>
                <h4>执行结果</h4>

                <textarea id="result"
                          style="border: 2px solid #5cb85c;margin: 0px; width: 500px; height: 200px; resize: none;">
                    <%
                        GenThriftResult result = (GenThriftResult) session.getAttribute("result");

                        if (result != null) {
                            if (result.getError() != null && result.getFlag() == GenThriftFlag.RUNERROR)
                                out.println(result.getError().trim());
                            result.setFlag(GenThriftFlag.NOTRUN);
                            result.setError("");
                        }

                        JMonitor.add("jsp.test");

                    %>
                </textarea>
            </div>

        </div>
    </div>
    <div class="page-header">
    </div>
    <h4 style="text-align:center">© Copyright 2015 by meituan</h4>
</form:form>
</body>
</html>

