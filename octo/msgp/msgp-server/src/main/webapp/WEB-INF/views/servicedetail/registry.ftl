<#if isEdit?exists && isEdit = true>
<title>服务修改</title>
<#else>
<title>服务注册</title>
</#if>
<link type="text/css" rel="stylesheet" href="/static/css/select2.css"/>
<style>
    .select2-selection__choice {
        float: left;
    }
</style>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#if isEdit?exists && isEdit = true>
    <h3 class="page-header">修改服务:
        <input class="mb5 span6" type="text" id='apps_select' value='${appkey!""}' autocomplete="off"/>
    </h3>
<#else>
    <h3 class="page-header">服务注册</h3>
</#if>
    <div style="float: right;" id="yui_3_17_2_3_1454393720202_36">
        <a href="https://123.sankuai.com/km/page/28326379" target="_blank"
           id="yui_3_17_2_3_1454393720202_35">
            权限说明<i class="fa fa-question-circle"></i>
        </a>
    </div>
    <hr/>

    <div id="registring" class="content-overlay" style="display: none;">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">正在注册中...</span>
    </div>
    <div id="registry_result" style="display: none;">
        <h3></h3>
    </div>
    <div id="registry_error" style="display: none;">
        <span>注册失败</span> <a href="javascript:;" id="registry_again" class="get-again">重来</a>
    </div>
    <div id="registryForm" class="form-horizontal">
        <input type="hidden" id="base" value="${base!'0'}"/>
        <div class="control-group mb5"><label class="control-label"></label>

            <div class="controls"><span id="result-tip" class="text-error"></span></div>
        </div>

        <div class="control-group">
            <label class="control-label" style="padding-top:0px"><span style="color:red;">提示*</span>：</label>

            <div class="controls">
                北京侧 普通应用请到<a title="点击注册" href="http://ops.sankuai.com/workflow/#/start/service-apply">服务树注册</a>,
                已有服务申请新appkey请到<a href="http://ops.sankuai.com/workflow/#/start/octo">Octo自助流程申请</a>,
                其他需求请联系 OCTO技术支持(infocto)<br/>
                上海侧
                新服务注册<a title="点击注册" href="http://ttt.dp/#!/start/5">应用树注册</a>,
                已有服务绑定<a title="点击绑定" href="http://ttt.dp/#!/start/23">应用树绑定</a><br>
                <a href="https://123.sankuai.com/km/page/28125601" target="_blank">
                    基础服务架构协议<i class="fa fa-question-circle"></i>
                </a>
            </div>
        </div>

        <div class="control-group" style="display:none;"><label class="control-label">服务名：</label>

            <div class="controls">
                <input id="name" type="text" value="${name!''}" placeholder="name"/>
                <span class="tips"></span>
            </div>
        </div>
    <#if isEdit || force>
        <#if !isEdit>
            <div class="control-group"><label class="control-label">appkey<span style="color:red;">*</span>：</label>
                <!-- TODO: 调用后端接口检测是否冲突 -->
                <div class="controls">
                    <select id="sankuai_meituan">
                        <option value="com.sankuai.">com.sankuai.</option>
                    </select>
                    <input id="appkey" type="text"
                           value="${appkey!''}" ${(isEdit?exists && isEdit==true)?string('disabled','')}
                           title="${(isEdit?exists && isEdit==true)?string('不可修改','')}"/>
                    <span class="tips" id="appkey_tips"></span>
                </div>
            </div>

            <div class="control-group"><label class="control-label"></label>

                <div class="controls">
                    <span class="tips">范式：com.sankuai.业务线.具体服务  样例：com.sankuai.waimai.poi</span>
                </div>
            </div>
        </#if>
        <div class="control-group" id="business_wrap">
            <label class="control-label">所属业务<span style="color:red;">*</span>：</label>

            <div class="controls">
                <input id="owtVal" style="display: none;" type="text" value="${owtVal!''}">
                <input id="pdlVal" style="display: none;" type="text" value="${pdlVal!''}">

                <select id="owt_name">
                    <#list owts![] as owt>
                        <option value="${owt}"
                                <#if (owtVal?exists && owtVal==owt) || (!owtVal?exists && item_index == 0) >selected</#if>>${owt}</option>
                    </#list>
                </select>

                <select id="pdl_name">
                    <#list pdls![] as pdl>
                        <option value="${pdl.pdl}"
                                <#if (pdl?exists && pdlVal==pdl.pdl) || (!pdlVal?exists && item_index == 0) >selected</#if>>${pdl.pdl}</option>
                    </#list>
                </select>
                <span class="tips" id="business_group_tips"></span>
            <#--<a  target="_blank" id="owtpdlowner" href="#">-->
            <#--业务线负责人-->
            <#--</a>-->
            </div>
        </div>

        <div id="owner_wrap" class="control-group"><label class="control-label">负责人<span
                style="color:red;">*</span>：</label>
            <!-- TODO: 账号拼音如zhangxi，输入时实现提示补全，可填写多个？ -->
            <div class="controls">
                <select id="owner" multiple="multiple" data-name="clainUserNames"
                        data-node="userInput" style="width: 80%; height: 30px;">
                </select>
                <input type="hidden" id="ownerId" data-name="claimUsers" value="${ownerId!''}"/>
                <span class="tips"></span>
            </div>
        </div>

        <div id="observer_wrap" class="control-group"><label class="control-label">关注人：</label>

            <div class="controls">
                <select id="observer" multiple="multiple" data-name="clainObserverUserNames"
                        data-node="observerUserInput" style="width: 80%; height: 30px">
                </select>
                <input type="hidden" id="observerId" data-name="claimUsers" value="${observerId!''}"/>
                <span class="tips"></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">服务描述<span style="color:red;">*</span>：</label>

            <div class="controls">
                <textarea id="intro" style="width:430px;height: 100px;" type="text" placeholder="简单服务描述信息"
                          style="height:100px;">${intro!''}</textarea>
                <span class="tips" id="intro_tips"></span>
            </div>
        </div>

        <div class="control-group"><label class="control-label">是否强制验证主机列表：</label>
            <div class="controls">
                <#if regLimitPermission?exists && regLimitPermission = true >
                <select id="regLimit" name="regLimit">
                <#else>
                <select id="regLimit" name="regLimit" disabled="disabled">
                </#if>
                <#if regLimitPermission?exists && regLimit = 0 >
                    <option value="0" selected="selected">非强制</option>
                    <option value="1">强制</option>
                <#else>
                    <option value="0">非强制</option>
                    <option value="1" selected="selected">强制</option>
                </#if>
            </select>
                <#if regLimitPermission?exists && regLimitPermission = false >
                    <span style="padding-left: 20px; color: grey;">非OCTO管理员和SRE, 禁止更改主机列表验证选项</span>
                </#if>
            </div>
        </div>

        <!-- TODO：类型：http、thrift checkbox -->
        <!-- TODO：业务线：select -->
        <!-- tags -->
        <div class="control-group"><label class="control-label">标签：</label>
            <!-- TODO: 标签支持多个，修改为添加后再展示的形式 -->
            <div class="controls">
                <input id="tags" style="width:430px;" type="text" value="${tags!''}" placeholder="可填多个，请以英文逗号(,)分隔"/>
                <span class="tips"></span>
            </div>
        </div>
        <input id="createTime" class="hidden" type="text" value="${createTime!0}">
        <div class="form-actions"><input id="submit-btn" class="btn btn-primary" type="submit" value="保存"/></div>
    </#if>
    </div>

    <!--刷新块-->
    <div class="spinner" style="display:none" id="spinner">
        <div class="spinner-container container1">
                
            <div class="circle1"></div>
                
            <div class="circle2"></div>
                
            <div class="circle3"></div>
                
            <div class="circle4"></div>
              
        </div>
        <div class="spinner-container container2">
                
            <div class="circle1"></div>
                
            <div class="circle2"></div>
                
            <div class="circle3"></div>
                
            <div class="circle4"></div>
              
        </div>
        <div class="spinner-container container3">
                
            <div class="circle1"></div>
                
            <div class="circle2"></div>
                
            <div class="circle3"></div>
                
            <div class="circle4"></div>
              
        </div>
    </div>
    <!--刷新块结束-->
    <script type="text/javascript" src="/static/js/jquery.min.js"></script>
    <script type="text/javascript" src="/static/js/lodash.js"></script>
    <script>
        var isEdit = ${(isEdit?exists && isEdit==true)?string('true', 'false')};
        var appkey = '${appkey!""}';
        var owtsSize = ${owts?size};
        hasOwt = owtsSize > 0 ? true : false;
        var ownerStr = '${owner!""}';
        var observerStr = '${observer!""}';
    </script>
    <script type="text/javascript" src="/static/js/select2.min.js"></script>
    <script type="text/javascript" src="/static/servicedetail/registry-1.1.js"></script>
</div>
