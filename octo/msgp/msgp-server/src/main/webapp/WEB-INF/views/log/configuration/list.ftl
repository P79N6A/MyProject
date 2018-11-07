<style>
    .errorlogAlarmConfig {
        margin-top: 15px;
    }

    .errorlogAlarmConfig .form-table table td,
    .errorlogAlarmConfig .form-table table th {
        vertical-align: middle;
        padding: 5px 0px 10px 10px;
    }

    .errorlogAlarmConfig .form-table table td {
        width: 430px;
    }
    .errorlogAlarmConfig .form-table table th {
        text-align: left;
    }

</style>

<title>异常监控</title>
<#include "/topLinkEvent.ftl" >

<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能配置报警</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li class="current"><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>

<div class="box box-noborder errorlogAlarmConfig">
<#if configuration?? && configuration.basicConfig??>
    <div class="box-header">
        修改异常监控
    </div>
    <div class="box-content" style="background: url(/api-wm/image/visible) 0 0 repeat;background-size: 300px 250px;">
        <form id="editConfigurationForm" action="#" class="form-table">
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">基本配置</caption>
                <input id="enabled" name="basicConfig.enabled" type="hidden"
                       value="${configuration.basicConfig.enabled?string('true', 'false')}"/>
                <tr>
                    <th>应用APPKEY：</th>
                    <td><input id="appkey" name="basicConfig.appkey" type="text" readonly="readonly"
                               value="${configuration.basicConfig.appkey!''}"/></td>
                </tr>
                <tr>
                    <th>报警间隔时间(s)：</th>
                    <td><input id="gapSeconds" name="basicConfig.gapSeconds" type="number"
                               value="${configuration.basicConfig.gapSeconds!3600}"/>最小60s
                    </td>
                    <input id="MIN_GAP_SECONDS" type="hidden" value="${MIN_GAP_SECONDS!10}"/>
                </tr>
            </table>
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">Falcon配置</caption>
                <tr>
                    <th>Falcon模板策略：</th>
                    <td>
                        <input id="trapper" name="basicConfig.trapper" type="text" readonly="readonly"
                               value="${configuration.basicConfig.trapper!''}"/>
                        </br>
                        <div style="color: red;">注意：需手动到Falcon配置该同名模板策略;</div>
                        说明：该策略配置在当前Appkey对应节点下, 若Appkey无对应节点请到服务树绑定
                    </td>
                </tr>
            </table>
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">报警级别配置</caption>
                <tr>
                    <th>报警级别</th>
                    <td>报警时间间隔中收集的日志数量
                        </br>
                        <div style="color: red;">注意：ERROR/WARNING/DISASTER 与日志级别无关！！
                        </div>
                        用于匹配Falcon报警策略发不同级别报警
                    </td>
                </tr>
                <tr>
                    <th>OK：</th>
                    <td><input id="severityOk" name="severityConfig.ok" type="number"
                               value="<#if configuration.severityConfig??>${configuration.severityConfig.ok!0}<#else>0</#if>"/>
                    </td>
                </tr>
                <tr>
                    <th>WARNING：</th>
                    <td><input id="severityWarning" name="severityConfig.warning" type="number"
                               value="<#if configuration.severityConfig??>${configuration.severityConfig.warning!0}<#else>0</#if>"/>
                    </td>
                </tr>
                <tr>
                    <th>ERROR：</th>
                    <td><input id="severityError" name="severityConfig.error" type="number"
                               value="<#if configuration.severityConfig??>${configuration.severityConfig.error!0}<#else>0</#if>"/>
                    </td>
                </tr>
                <tr>
                    <th>DISASTER：</th>
                    <td><input id="severityDisaster" name="severityConfig.disaster" type="number"
                               value="<#if configuration.severityConfig??>${configuration.severityConfig.disaster!0}<#else>0</#if>"/>
                    </td>
                </tr>
            </table>
            <div class="form-actions">
                <#if ! configuration.basicConfig.enabled>
                    <a id="startAlarmLink" class="btn btn-primary">启动</a>
                <#else>
                    <a id="restartAlarmLink" class="btn btn-primary">重启</a>
                    <a id="stopAlarmLink" class="btn btn-warning">停止</a>
                </#if>
            </div>
        </form>
    </div>
    <script>
        M.use("msgp-log", function (Y) {
            Y.msgp.Log.configurationList();
        });
    </script>
<#else>
    <div class="box-header">
        添加异常监控
    </div>
    <div class="box-content">
        <form id="addConfigurationForm" method="POST" action="#" class="form-table">
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">基本配置</caption>
                <input id="enabled" name="basicConfig.enabled" type="hidden" value="false"/>
                <tr>
                    <th>应用APPKEY：</th>
                    <td><input id="appkey" name="basicConfig.appkey" type="text" value="${appkey!''}"/></td>
                </tr>
                <tr>
                    <th>报警间隔时间(s)：</th>
                    <td><input id="gapSeconds" name="basicConfig.gapSeconds" type="number"
                               value="60"/>最小60s
                    </td>
                    <input id="MIN_GAP_SECONDS" type="hidden" value="${MIN_GAP_SECONDS!10}"/>
                </tr>
            </table>
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">Falcon配置</caption>
                <tr>
                    <th>Falcon模板策略：</th>
                    <td>
                        <input id="trapper" name="basicConfig.trapper" type="text" readonly="readonly"
                               value="sg.custom.error.status"/>
                        </br>
                        <div style="color: red;">注意：需手动到Falcon配置该同名模板策略;</div>
                        说明：该策略配置在当前Appkey对应节点下, 若Appkey无对应节点请到服务树绑定
                    </td>
                </tr>
            </table>
            <table style="width: 500px;" class="table table-bordered">
                <caption style="color: blue;">报警级别配置</caption>
                <tr>
                    <th>报警级别</th>
                    <td>报警时间间隔中收集的日志数量
                        </br>
                        <div style="color: red;">注意：ERROR/WARNING/DISASTER 与日志级别无关！！
                        </div>
                        用于匹配Falcon报警策略发不同级别报警
                    </td>
                </tr>
                <tr>
                    <th>OK：</th>
                    <td><input id="severityOk" name="severityConfig.ok" type="number" value="0"/></td>
                </tr>
                <tr>
                    <th>WARNING：</th>
                    <td><input id="severityWarning" name="severityConfig.warning" type="number" value="1"/></td>
                </tr>
                <tr>
                    <th>ERROR：</th>
                    <td><input id="severityError" name="severityConfig.error" type="number" value="2"/></td>
                </tr>
                <tr>
                    <th>DISASTER：</th>
                    <td><input id="severityDisaster" name="severityConfig.disaster" type="number" value="3"/></td>
                </tr>
            </table>
            <div class="form-actions">
                <a id="addConfigurationBtn" class="btn btn-primary">保存</a>
            </div>
        </form>
    </div>
    <script>
        M.use("msgp-log", function (Y) {
            document.title = "异常监控";
            Y.msgp.Log.addConfiguration();
        });
    </script>
</#if>
</div>
