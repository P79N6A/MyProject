<title xmlns="http://www.w3.org/1999/html">实时日志</title>


<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<link rel="stylesheet" type="text/css" href="/static/css/realtime.css"/>
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css"/>

<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
<#include "/topLinkEvent.ftl" >
    <attr
    <div class="tab-box" >
        <ul class="nav nav-tabs widget-edit-tab">
            <li><a href="/service/detail?appkey=${appkey}#supplier">服务提供者</a></li>
            <li><a href="/service/detail?appkey=${appkey}#consumer">服务消费者</a></li>
            <li><a href="/service/detail?appkey=${appkey}#outline">服务概要</a></li>
            <li><a href="/service/detail?appkey=${appkey}#component">组件依赖</a></li>
            <li><a href="/service/detail?appkey=${appkey}#oncall">值班管理</a></li>
            <li class="current"><a href="javascript:;">实时日志</a></li>
        </ul>
    </div>

    <form id="rtForm" method="get">

    <div class="form-inline mt20 mb20">
        <div class="control-group">
            <label style=" padding-right: 20px; float: right;">
                <a href="https://123.sankuai.com/km/page/28210909"
                   target="_blank">问题帮助&使用反馈</a>
                <a href="https://123.sankuai.com/km/page/28209994"
                   target="_blank"> | 实时日志使用手册</a>
               </label>
                <a style="padding-left:23px" href="https://123.sankuai.com/km/page/28253677" target="_blank" id="yui_3_17_2_3_1469849388346_24">环境<i class="fa fa-question-circle" id="yui_3_17_2_3_1469849388346_25"></i>：</a>
                <label for="env_select" style="padding-right: 10px;">
                    <div id="env_select" class="btn-group">
                        <a value="3" type="button" class="btn <#if (!env?? || env= 3 )>btn-primary</#if>" href="javascript:void(0)">prod</a>
                        <a value="2" type="button" class="btn btn-default <#if (env?exists && env= 2 )>btn-primary</#if>" href="javascript:void(0)">stage</a>
                        <#if isOffline>
                            <a value="1" type="button" class="btn btn-default <#if (env?exists && env= 1)>btn-primary</#if>" href="javascript:void(0)">test</a>
                        </#if>
                    </div>
                </label>

                <input id="submitBtn" class="btn btn-primary ml20" type="button" value="查询"/><#--</button>-->
                <input id="stopBtn" name="stopBtn" class="btn btn-primary ml20" type="button"
                       value="停止"/>
            <strong><span id= "tips" style="color: red;font-size: 14px;margin-left: 30px;"></span></strong>

            <span id= "appkey" hidden >${appkey}</span>
            <span id= "userName" hidden>${userName}</span>
            <span id= "logMonitorServer" hidden>${logMonitorServer}</span>

        </div>
        <div class="control-group">
            <label class="control-label" for="hosts">主机列表<span style="color:red;">*</span>：</label>
            <select id="hosts" name="hosts" title="请选择主机" multiple="multiple">
            <#list providers as provider>
                <option value="${provider.ip}">${provider.hostname}(${provider.ip})</option>
            </#list>
            </select>

            <label for="filePath" style="padding-left:1em">日志路径<span style="color:red;">*</span>：</label>
            <input id="filePath" name="filePath" type="text" title="" value="${logPath!""}" style="width: 400px;"
                   placeholder= "第一次使用请输入日志完整路径 若已输入过可忽略"/>

            <label class="ml20"> 关键字：</label>
            <input id="filter" name="filter" placeholder="支持自定义 关键字以 , 间隔"
                   type="text" class="span3" title="" style="width: 180px;"/>
        </div>
    </form>

    <div class="control-group" id="realtime_content" style="padding-top: 20px;">
            <div id="web_client">
                <div id="log_controls">
                <#--<a class="select_mode active" href="#log_control_streams">Nodes</a>-->

                    <div id="log_control_streams" class="object_controls">
                        <div class="groups">
                            <div class="group">
                                <div class="items">
                                    <div class="item">
                                    <#--<div class="screen_buttons">

                                        <input type="checkbox" checked="checked" title="screen-c10">

                                    </div>-->
                                        <div class="diode floatl active color1"></div>
                                    <#--<div class="item_name floatl">logio_webserver01</div>-->
                                        <div style="clear: both;"></div>
                                        <div style="clear: both;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="log_screens" style="width: 1200px;overflow:auto;">
                    <div class="log_screens">
                        <div class="log_screen">
                        <div class="params" style=" color:lightgrey;background-color: #111;word-wrap: break-word;overflow: auto;height: 120px; text-indent: 5px;">
                         </div>
                            <div class="messages"
                                 style=" color:lightgrey;background-color: #111;word-wrap: break-word;overflow: auto;height: 600px; text-indent: 5px;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <hr/>
    <div id="charts_outer" class="clearfix">
    </div>
</div>

<script type="text/javascript" src="/static/js/jquery-2.2.3.min.js"></script>
<script type="text/javascript" src="/static/js/jquery-ui.js"></script>

<script type="text/javascript" src="/static/js/jquery.validate.min.js"></script>
<script type="text/javascript" src="/static/js/messages_zh.min.js"></script>
<script type="text/javascript" src="/static/js/socket.io-1.4.5.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>
<script type="text/javascript" src="/static/realtime/realtime.js"></script>
