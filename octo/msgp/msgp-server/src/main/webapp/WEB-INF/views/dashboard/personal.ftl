<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css"/>
<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'personal'
    })
</script>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: #3fab99;
    }
    .common-popdialog {
        overflow: scroll;
    }

    .multiselect-all label {
        font-weight: bold;
    }
    .multiselect-search {
        margin-left: 10px;
    }

    .input-group-btn {
        display: none;
    }

    .multiselect-container li{
        text-align: left;
    }

    .dropdown-menu>li>a:hover, .dropdown-menu>li>a:focus, .dropdown-submenu:hover>a, .dropdown-submenu:focus>a{
        background-color: #3fab99;
    }

    .dropdown-menu>.active>a, .dropdown-menu>.active>a:hover, .dropdown-menu>.active>a:focus {
        background-color: #3fab99;
    }

</style>
<title>自定义导航</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="page-header">
        <div class="input-append">
            <input id="search_input" class="span6" type="text" placeholder="appkey、负责人、标签，为空表示查询全部"/>
            <button id="search_button" class="btn btn-primary" type="button">查询服务</button>
        </div>
        <script type="text/javascript" src="/static/dashboard/bindSearch.js"></script>
        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28178492" target="_blank">
                服务如何接入OCTO<i class="fa fa-question-circle"></i>
            </a>
            <br>
            <a href="https://123.sankuai.com/km/page/28125601" target="_blank">
                基础服务架构协议<i class="fa fa-question-circle"></i>
            </a>
        </div>

    </div>
    <div class="tab-box">
        <ul class="nav nav-tabs widget-edit-tab">
            <li class="current"><a href="/personal">个人主页</a></li>
            <li><a href="/subscribe">订阅中心</a></li>
            <li><a href="/error">报错大盘</a></li>
            <li><a href="/database">数据库大盘</a></li>
            <li><a href="/tair">缓存大盘</a></li>
        </ul>
    </div>
</div>
<div id="personal-wrapper">
    <table style="width: 100%; margin-top: 20px;">
        <tr>
            <td style="width: 75%; vertical-align: top;" rowspan="2">
                <div  style="color: #888;
                    background-color: #fafafa;
                    border-bottom: 1px solid #ebebeb;
                    border-radius: 5px 5px 0 0 ;
                    font-weight: 700;
                    min-height: 20px;
                    line-height: 30px;
                    text-align: center;
                    text-shadow: 1px 1px 0 #fff;
                    font-size: 18px;">常用服务
                </div>
                <div style="position: relative; overflow: hidden">
                    <div style="padding-top: 20px;">
                        <table class="table table-striped table-hover" id="favorite-appkey-table">
                            <colgroup>
                                <col width="30%"></col>
                                <col width="60%"></col>
                                <col width="10%"></col>
                            </colgroup>
                            <thead>
                            <tr>
                                <th>服务标识</th>
                                <th>常用功能</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#if (favorite?size == 0) >
                            <tr style="background-color:#f6f7fa;">
                            <tr>
                                <td colspan="3" style="text-align: center; padding-top: 20px;"><a class="favorite-appkey-add" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 添加一项</a></td>
                            </tr>
                            </tr>
                            <#else>
                                <#list favorite as appkey>
                                <tr data-appkey="${appkey}">
                                    <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">${appkey}</td>
                                    <td>
                                        <a href="${hostUrl}/data/tabNav?appkey=${appkey}&type=performance#performance" target="_blank"><i class="fa fa-bar-chart-o"> 性能指标</i></a>
                                        <a style="margin-left: 20px;" href="${hostUrl}/service/detail?appkey=${appkey}#supplier" target="_blank"><i class="fa fa-sitemap"> 服务提供者</i></a>
                                        <a style="margin-left: 20px;" href="${hostUrl}/log/report?appkey=${appkey}" target="_blank"><i class="fa fa-volume-up"> 异常日志统计</i></a>
                                        <a style="margin-left: 5px;" href="${hostUrl}/serverOpt/operation?appkey=${appkey}#config" target="_blank"><i class="fa fa-gear"> 配置管理</i></a>
                                        <a style="margin-left: 5px;" href="${hostUrl}/repservice/daily" target="_blank"><i class="fa fa-list"> 服务治理日报</i></a>
                                        <a style="margin-left: 5px;" href="${hostUrl}/serverOpt/operation?appkey=${appkey}#hulkOption" target="_blank"><i class="fa fa-share-square-o"> 弹性伸缩</i></a>
                                    </td>
                                    <td style="width: 4%;">
                                        <a class="delete-favorite-appkey" href="javascript:;"><i class="fa fa-trash-o"> 移除</i></a>
                                    </td>
                                </tr>
                                </#list>
                                <tr>
                                    <td colspan="3" style="text-align: center; padding-top: 20px;">
                                        <a class="favorite-appkey-add" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 添加服务</a>
                                    </td>
                                </tr>
                            </#if>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="3" id="favorite-appkey-add-selector-wrapper" style="text-align: center; border: none; height: 300px; padding: 20px 0 30px 0; display: none;">
                                        <select id="favorite-appkey-add-selector" multiple="multiple"></select>
                                        <button class="btn btn-small" id="favorite-appkey-save">保存</button>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </td>
            <td style="width: 25%; vertical-align: top;" rowspan="1">
                <div>
                    <div style="color: #888;
                    background-color: #fafafa;
                    border-bottom: 1px solid #ebebeb;
                    border-radius: 5px 5px 0 0 ;
                    font-weight: 700;
                    min-height: 20px;
                    line-height: 30px;
                    text-align: center;
                    text-shadow: 1px 1px 0 #fff;
                    font-size: 18px;">新功能推荐</div>
                    <div style="padding: 10px 10px 17px; position: relative; overflow: hidden">
                        <div style="text-align: left; padding-top: 10px;">
                            <ul>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="${hostUrl}/serverOpt/operation?appkey=#appkeyAuth" target="_blank" id="count_guide">服务鉴权</a></li>
<#--
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="${hostUrl}/serverOpt/operation?appkey=#httpCutFlow" target="_blank" id="count_guide">HTTP截流</a></li>
-->
                            </ul>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        <tr>
            <td style="width: 25%; vertical-align: top;" rowspan="1">
                <div>
                    <div style="color: #888;
                    background-color: #fafafa;
                    border-bottom: 1px solid #ebebeb;
                    border-radius: 5px 5px 0 0 ;
                    font-weight: 700;
                    min-height: 20px;
                    line-height: 30px;
                    text-align: center;
                    text-shadow: 1px 1px 0 #fff;
                    font-size: 18px;">常用文档</div>
                    <div style="padding: 10px 10px 17px; position: relative; overflow: hidden">
                        <div style="text-align: left; padding-top: 10px;">
                            <ul>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28326937" target="_blank" id="count_guide">疑难自查手册</a></li>
                                <#--<li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="http://faq.inf.sankuai.com/octo" target="_blank" id="count_guide">常见问题反馈</a></li>-->
                            </ul>
                            <ul>
                                <li style="list-style-type:none;">.&nbsp;</li>
                            </ul>
                            <ul>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28178492" target="_blank" id="count_guide">服务接入OCTO</a></li>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28326385" target="_blank" id="count_guide">服务接入异常日志</a></li>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28327894" target="_blank" id="count_guide">服务可用率说明</a></li>
                            </ul>
                            <ul>
                                <li style="list-style-type:none;">.&nbsp;</li>
                            </ul>
                            <ul>
                                <li><a style=" display: inline-block; height: 20px; line-height: 20px;" href="/more/doc" target="_blank" id="count_guide"><i class="fa fa-arrow-right"> 查看更多文档</a></i></li>
                            </ul>

                        </div>
                    </div>
                </div>
            </td>
        </tr>
    </table>
</div>


<textarea id="text-favorite-appkey-table-tbody" style="display:none">
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-appkey="<%= item.appkey %>">
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.appkey %></td>
        <td>
            <a href="<%= item.hostUrl %>/data/tabNav?appkey=<%= item.appkey %>&type=performance#performance" target="_blank"><i class="fa fa-bar-chart-o"> 性能指标</i></a>
            <a style="margin-left: 5px;" href="<%= item.hostUrl %>/service/detail?appkey=<%= item.appkey %>#supplier" target="_blank"><i class="fa fa-sitemap"> 服务提供者</i></a>
            <a style="margin-left: 5px;" href="<%= item.hostUrl %>/log/report?appkey=<%= item.appkey %>" target="_blank"><i class="fa fa-volume-up"> 异常日志统计</i></a>
            <a style="margin-left: 5px;" href="<%= item.hostUrl %>/serverOpt/operation?appkey=<%= item.appkey %>#config" target="_blank"><i class="fa fa-gear"> 配置管理</i></a>
            <a style="margin-left: 5px;" href="<%= item.hostUrl %>/repservice/daily" target="_blank"><i class="fa fa-list"> 服务治理日报</i></a>
            <a style="margin-left: 5px;" href="<%= item.hostUrl %>/serverOpt/operation?appkey=<%= item.appkey %>#hulkOption" target="_blank"><i class="fa fa-share-square-o"> 弹性伸缩</i></a>
        </td>
        <td style="width: 4%;">
            <a class="delete-favorite-appkey" href="javascript:;"><i class="fa fa-trash-o"> 移除</i></a>
        </td>
    </tr>
<% }); %>
<tr>
    <td colspan="3" style="text-align: center; padding-top: 20px;"><a class="favorite-appkey-add" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 添加一项</a></td>
</tr>
</textarea>

<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>

<script>
    M.use('msgp-dashboard/personal', function (Y) {
        var hostUrl = "${hostUrl!'https://octo.sankuai.com/'}";
        var appkeys = [<#list favorite![] as appkey>'${appkey}',</#list>];
        Y.msgp.dashboard.personal(appkeys, hostUrl);
    });
</script>
