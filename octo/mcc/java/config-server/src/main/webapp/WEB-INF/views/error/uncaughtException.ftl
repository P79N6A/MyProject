<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -->
<title>系统故障</title>
<body>
<div class="right-all">
    <div class="info">
        <div class="error-page">
            <h3 class="error-reason">
                非常抱歉，系统出错了
                <br/>
                请简单描述一下您刚才的操作，帮助我们改进系统
                <br/>
                <form action="${request.getContextPath()}/bugreport" method="post">
                    <textarea style="width:580px;height:150px" name="bugDesc"></textarea>
                    <br/>
                    <input type="hidden" name="exceptionDesc" value="${exception}">
                    <input type="submit" value="提交报告">
                </form>
                <br/>
            </h3>
            <div class="error-sys"></div>
        </div>
    </div>
</div>
</body>
