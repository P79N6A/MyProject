<%@ page import="com.meituan.service.mobile.thrift.model.MISInfo" %>
<%@ page import="com.meituan.service.mobile.thrift.utils.CommonFunc" %>
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

            $("#logout").click(function () {
                window.location.href = "/logout";
            });

        })

        function checkForm() {
            var content = document.form.remark.value;
            if (!IsNull(replaceHTML(content))) {
                document.form.remark.focus();
                alert("请输入备注!");
                return false;
            }
            return true;
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
                if (misInfo != null) {

                    out.println("<li class='label'> <h5><button  id='logout' class='btn-link' style='color: #ffffff;'>退出</button></h5></li>");
                    out.println("<li class='label'> <h5><button  class='btn-link' style='color: #ffffff;'>" + misInfo.getName() + "</button></h5></li>");
                }

            %>

        </ul>
    </div>
</div>

<div class="container">
    <div class="row">

        <div class="col-md-2">
        </div>
        <div class="col-md-9">
            <br><br>
            <h4>版本差异</h4>

            <div class="divcss">
                <p id="diff">
                    <%
                        String diff = (String) session.getAttribute("diff");
                        if (diff != null)
                            out.println(CommonFunc.addBr(diff));
                    %>
                </p>
            </div>
        </div>
        <div class="col-md-1">
        </div>

    </div>
    <br>

    <div class="row">

        <div class="col-md-2">
        </div>
        <div class="col-md-9">

            <h4>版本备注(必填)</h4>

            <form action="/addRemark" method="post" name="form" onsubmit="return checkForm();">
                <input name="remark" type="text"  style="width: 300px;" required autofocus>
                <br><br>
                <input id="cancel" type="button" class="btn btn-danger" style="width: 100px;" value="取消"
                       onclick="location.href = '/delete'"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input id="continue" type="submit" class="btn btn-success" style="width: 100px;" value="继续"/>
            </form>
        </div>
        <div class="col-md-1">
        </div>

    </div>


</div>
</body>
</html>

