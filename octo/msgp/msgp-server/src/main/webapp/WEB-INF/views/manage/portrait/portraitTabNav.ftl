<title xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">服务画像展示</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#include "/topLinkEvent.ftl" >

    <div class="form-inline mt20 mb20" style="padding: 0px 0px 0px">
        <div class="control-group">
            <div style="float: right;">
                <a href="https://123.sankuai.com/km/page/14995870" target="_blank">
                    服务画像整体介绍<i class="fa fa-question-circle"></i>
                </a>
            </div>
        </div>

    </div>


    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs">
            <li><a href="#serviceResource">服务资源</a></li>
            <li><a href="#serviceProperty">服务性能</a></li>
            <#--<li><a href="#serviceStatus">服务状态</a></li>-->
        </ul>
        <div id="content_wrapper">
            <div id="wrap_serviceResource" class="sheet" style="display:none;">
            <#include "serviceResource.ftl" >
            </div>
            <div id="wrap_serviceProperty" class="sheet" style="display:none;">
            <#include "serviceProperty.ftl" >
            </div>
            <#--<div id="wrap_serviceStatus" class="sheet" style="display:none;">-->
            <#--<#include "serviceStatus.ftl" >-->
            <#--</div>-->
        </div>
        <script>
            var key1 = '${appkey}';
            var appkeytmp = [key1];
        </script>
        <script src="/static/js/tooltip.js"></script>
        <script type="text/javascript" src="/static/manage/portrait/portraitTabNav.js"></script>
    </div>
</div>
</div>