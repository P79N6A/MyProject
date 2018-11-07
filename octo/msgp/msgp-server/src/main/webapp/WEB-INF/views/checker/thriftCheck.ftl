<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<script src="/static/js/tooltip.js"></script>
<style>
    .thriftCheck .tooltip-inner {
        max-width: 1000px !important;
    }

    .thriftCheck li {
        list-style: none;
        max-width: 350px;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .thriftCheck li input {
        position: absolute;
        left: 0;
        margin-left: 0;
        opacity: 0;
        z-index: 2;
        cursor: pointer;
        height: 1em;
        width: 1em;
        top: 0;
    }

    .thriftCheck input + ol {
        display: none;
    }

    .thriftCheck input + ol > li {
        height: 0;
        overflow: hidden;
        margin-left: -14px !important;
        padding-left: 1px;
    }

    .thriftCheck li label {
        cursor: pointer;
        display: block;
        padding-left: 17px;
        background: url(/static/img/toggle.png) no-repeat 0px 1px;
    }

    .thriftCheck input:checked + ol {
        margin: -22px 0 0 -44px;
        padding: 20px 0 0 80px;
        height: auto;
        display: block;
    }

    .thriftCheck input:checked + ol > li {
        height: auto;
    }

    .thriftCheck li span {
        cursor: pointer;
    }

    .thriftCheck li span:focus, .thriftCheck li span:active {
        font-weight: bold;
        color: #3fab99;
        text-decoration: underline;
    }

    .operWarning {
        color: red;
    }
</style>
<div class="thriftCheck">
    <div class="form-inline mb20">
        <div class="btn-group">
            <a type="button" class="httpCheckBtn btn btn-primary" href="javascript:void(0)">服务自检</a>
            <a type="button" class="httpInvokeBtn btn btn-default" href="javascript:void(0)">接口调用</a>
        </div>
        <span class="operWarning fa fa-warning ml10" style="display: none">避免污染数据谨慎操作！</span>
        <a style="margin-left:1em" href="https://123.sankuai.com/km/page/19180639" target="_blank">Mtthrift自检说明<i class="fa fa-question-circle"></i></a>
    </div>
    <div class="httpCheck">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="control-label" style="width: 125px; padding-top: 0px;">自检端：</label>
                <div class="checkRole controls" style="margin-left: 125px">
                    <input name="server" value="server" type="radio" style="vertical-align: top"/>
                    <span>&nbsp;服务端&nbsp;&nbsp;</span>
                    <input name="client" value="client" type="radio" style="vertical-align: top"/>
                    <span>&nbsp;调用端&nbsp;&nbsp;</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;">自检信息：</label>
                <select class="serverCheckType" title="自检信息">
                    <option value="serverInfo">服务信息(v1.8.5)</option>
                    <option value="authInfo">鉴权信息(v1.8.5)</option>
                    <option value="flowcopyInfo">流量录制信息(v1.8.5)</option>
                    <option value="methodInfo">服务方法信息(v1.8.5.4)</option>
                </select>
                <select class="clientCheckType" title="自检信息" style="display: none;">
                    <option value="providerInfo">服务提供者(v1.8.5)</option>
                    <option value="authInfo">鉴权信息(v1.8.5)</option>
                </select>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;">主机：</label>
                <div class="controls" style="margin-left: 0px;">
                    <input class="host span4" placeholder="主机名或IP" type="text" style="margin-bottom: 0px；"/>
                    <span class="tips" id="checkHostTip"></span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;">自检端口：</label>
                <div class="controls" style="margin-left: 0px;">
                    <div class="input-append">
                        <input class="httpPort span4" type="text" style="width: 50px;" readonly="true"
                               value="5080"/>
                        <button class="changePort btn btn-primary"
                                style="height: 30px; margin-left: -1px; font-size: 12px; padding: 5px;"
                                href="javascript:void(0)">修改
                        </button>
                    </div>
                    <span class="tips" id="checkPortTip"></span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <button class="submit btn btn-primary" style="font-size: 13px">
                    自检提交
                </button>
                <span class="submitTip tips"></span>
            </div>
            <div class="checkResult control-group" style="display: none;">
                <label class="control-label" style="width: 125px;">结果：</label>
                <div style="overflow: hidden;">
                    <pre class="resultContent"></pre>
                </div>
                <div class="resultLoad">
                    <i class="fa fa-spinner fa-spin text-blue" style="font-size: 25px;"></i><span
                        class="ml10">获取数据中...</span>
                </div>
            </div>
        </div>
    </div>
    <div class="httpInvoke" style="display: none;">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="control-label" style="width: 125px;">
                    <#include "/common/env.ftl" >
                </label>
                <div id="httpInvokeEnv" class="btn-group">
                    <a value="prod" type="button" class="httpInvoke_prod btn btn-primary"
                       href="javascript:void(0)">prod</a>
                    <a value="stage" type="button" class="httpInvoke_stage btn btn-default"
                       href="javascript:void(0)">stage</a>
                    <a value="test" type="button" class=" httpInvoke_test btn btn-default"
                       href="javascript:void(0)">test</a>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;">服务节点：</label>
                <input type="text" style="width: 180px;" placeholder="主机名或IP" value="${serverNodes!''}"
                       id="serverNodes">
                <span class="serverNodeTip tips" style="color: #f00"></span>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;">自检端口：</label>
                <div class="controls" style="margin-left: 0px;">
                    <div class="input-append">
                        <input class="httpInvokePort span4" type="text" style="width: 50px;" readonly="true"
                               value="5080"/>
                        <button class="invokeChangePort btn btn-primary"
                                style="height: 30px; margin-left: -1px; font-size: 12px; padding: 5px;"
                                href="javascript:void(0)">修改
                        </button>
                    </div>
                    <span class="tips" id="invokePortTip"></span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <button class="queryMethod btn btn-primary" style="font-size: 13px;">
                    获取服务接口
                </button>
                <span class="queryMethodTip tips"></span>
            </div>
            <div class="methodResult control-group" style="display: none; margin-left: 50px">
                <div class="methodQueryError" style="overflow: hidden;">
                    <pre style="color: #f00"></pre>
                </div>
                <div class="methodLoad">
                    <i class="fa fa-spinner fa-spin text-blue" style="font-size: 25px;"></i><span
                        class="ml10">获取数据中...</span>
                </div>
                <div class="methodInvoke">
                    <div class="queryResult" style="float: left;">
                    </div>
                    <div class="invokeParam form-horizontal" style="display: inline-block; float:left;">
                        <div class="control-group" style="margin-bottom: 10px">
                            <label class="control-label" style="width: 95px; padding-top: 0px;">调用方法：</label>
                            <input type="text" class="methodSign" placeholder="选择方法" readonly/>
                        </div>
                        <div class="params">
                        </div>
                        <div class="control-group">
                            <label class="control-label" style="width: 95px;"></label>
                            <button class="invokeBtn btn btn-primary" style="padding: 3px 5px 3px 5px; font-size: 13px">
                                调用
                            </button>
                            <span class="tips invokeBtnTip" style="color: #f00"></span>
                        </div>
                    </div>
                    <div class="invokeResult" style="display: inline-block;">
                        <div class="control-group" style="margin-bottom: 10px">
                            <label class="control-label" style="width: 100px; padding-top: 0px;">结果：</label>
                            <div style="overflow: hidden; max-width: 500px;">
                                <pre class="invokeResultContent"></pre>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>