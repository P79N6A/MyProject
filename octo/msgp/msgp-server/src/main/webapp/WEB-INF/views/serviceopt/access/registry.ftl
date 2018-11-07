<div class="form-horizontal">
    <div class="control-group" style="margin-bottom: 10px;"><label class="control-label" style="width: 125px;">最后更新：</label>

        <div id="user_overlay">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div id="user_content" class="controls" style="margin-left: 0px;">
            <span class="outline-content" id="user_content_text">无</span>
        </div>
    </div>
    <div class="control-group" style="margin-bottom: 10px;"><label class="control-label"
                                      style="padding-top: 0px;width: 125px;">状态：</label>

        <div id="status_overlay">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div id="status_content" class="controls" style="margin-left: 0px; ">
            <input id="status_one_radio" value="0" type="radio" checked style="margin-bottom: 5px;"/>&nbsp;禁用
            &nbsp;&nbsp;<input id="status_one_radio" value="1" type="radio" style="margin-bottom: 5px;"/>&nbsp;启用
        </div>
    </div>
    <div class="control-group"><label class="control-label" style="width: 125px;">主机：</label>

        <div class="controls" style="margin-left: 0px;">
            <div class="input-append">
                <textarea id="registry_add_ip" class="span4" placeholder="IP地址" type="text" style="height: 25px;"></textarea>
                <button id="registry_add_ip_btn" class="btn btn-primary" style="height: 35px;margin-left: -1px;"
                        href="javascript:void(0)">添加
                </button>
            </div>
            <span class="tips" id="manual_add_ip_tips"></span>
        </div>
    </div>


    <div class="control-group"><label class="control-label" style="width: 125px;">白名单：</label>

        <div class="controls" style="margin-left: 0px;">
            <div class="access-ctrl-ip">
                <div id="white_overlay">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </div>
                <div id="add_white">
                </div>
            </div>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" style="width: 125px;"></label>
        <div class="controls" style="margin-left: 0px;">
            <button id="btn_save" class="btn btn-primary">
                <i class='fa fa-save'></i>
                保存
            </button>
            <span style="color: red;">编辑完成后请保存</span>
        </div>
    </div>
</div>