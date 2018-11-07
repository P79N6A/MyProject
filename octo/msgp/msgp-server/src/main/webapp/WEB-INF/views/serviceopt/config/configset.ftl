<div class="form-inline mb20">
    <button id="reminder_return" class="btn btn-primary" type="button">
        <i class='fa fa-step-backward'></i>
        返回
    </button>
</div>
<div>
    <div id="configset_menu" class="form-horizontal">
        <div class="control-group" style="margin-top: 10px;margin-bottom: 10px;">
            <label class="control-label" style="padding-top: 0px;width: 100px;">大象提醒：</label>
            <div class="controls" style="margin-left: 0px;">
                <input id="reminder_one_radio" value="false" type="radio" style="margin-bottom: 5px;">&nbsp;禁用
                &nbsp;&nbsp;<input id="reminder_one_radio" value="true" type="radio" style="margin-bottom: 5px;">&nbsp;启用
                <span style="margin-left:50px;color:red;">每次修改配置都会发送大象消息</span>
            </div>
        </div>
        <div class="control-group" style="margin-top: 10px;margin-bottom: 10px;">
            <label class="control-label" style="padding-top: 0px;width: 100px;">覆盖校验：</label>
            <div class="controls" style="margin-left: 0px;">
                <input id="version_one_radio" value="false" type="radio" style="margin-bottom: 5px;">&nbsp;禁用
                &nbsp;&nbsp;<input id="version_one_radio" value="true" type="radio" style="margin-bottom: 5px;">&nbsp;启用
                <span style="margin-left:50px;color:red;">每次配置修改前校验是否已经有其他人修改过</span>
            </div>
        </div>

        <div id="config_auth_div">
            <div class="control-group" style="margin-top: 10px;margin-bottom: 10px;">
                <label class="control-label" style="padding-top: 0px;width: 150px;">API修改配置鉴权：</label>
                <div class="controls" style="margin-left: 0px;">
                    <input id="setvalue_auth_one_radio" value="false" type="radio" style="margin-bottom: 5px;">&nbsp;禁用
                    &nbsp;&nbsp;<input id="setvalue_auth_one_radio" value="true" type="radio"
                                       style="margin-bottom: 5px;">&nbsp;启用
                </div>
            </div>
            <div id="setvalue_auth_menu" class="control-group" style="margin-top: 10px;margin-bottom: 10px">
                <label class="control-label" style="padding-top: 0px;width: 107px;">token设置：</label>
                <div class="controls" style="margin-left: 0px;">
                    <input id="setvalue_auth_one_text" value="" type="text" style="margin-bottom: 5px;width: 350px">
                    <button id="make_config_token" class="btn btn-primary" style="margin-bottom: 5px;">
                        生成密文
                    </button>
                </div>
            </div>
        </div>

        <div class="controls" style="margin-left: 30px;">
            <button id="btn_config_save" class="btn btn-primary">
                <i class="fa fa-save"></i>
                保存
            </button>
            <span style="color: red;">选择后请保存</span>
        </div>
    </div>
</div>

