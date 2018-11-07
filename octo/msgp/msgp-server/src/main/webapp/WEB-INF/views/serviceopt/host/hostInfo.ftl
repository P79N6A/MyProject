<div class="form-horizontal">

    <div class="control-group" style="margin-bottom: 10px;"><label class="control-label" style="width: 125px;">诊断信息：</label>
        <div id="checkinfo_loading" class="">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div class="controls" style="margin-left: 0px;">
            <span class="outline-content" id="checkinfo_content" style="text-align: justify; white-space: pre-wrap;">无</span>
        </div>
    </div>


    <div id = "detailinfo_div">
        &nbsp;&nbsp;&nbsp; <button class="btn btn-default" type="button"  id="detailinfo">详情</button><br>
    </div>

    <div id = "detail_div" >

        <div id = "errlog_div" class="control-group">
            <label id="errlog_label" class="control-label" style="width: 125px">错误信息：</label>

            <div id="errlog_loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content" id="errlog_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;"><label class="control-label" style="width: 125px;">所属机房：</label>
            <div id="idc_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content common-content" id="idc_content">无</span>
            </div>
        </div>



        <div class="control-group" style="margin-bottom: 10px;"><label class="control-label" style="width: 125px;">主机名：</label>

            <div id="hostname_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content common-content" id="hostname_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;"><label id="env_label" class="control-label" style="width: 125px;">OCTO环境：</label>

            <div id="env_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content common-content" id="env_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;"><label id="monitor_res_label" class="control-label detail-label" style="width: 125px;">monitor：</label>

            <div id="monitor_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="monitor_res_content">无</span>
            </div>
        </div>


        <div class="control-group" style="margin-bottom: 10px;">
            <label id="os_version_label" class="control-label detail-label" style="width: 125px;">系统版本：</label>
            <div id="os_version_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="os_version_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;"><label id="os_start_time_label" class="control-label detail-label" style="width: 125px;">系统启动时间：</label>

            <div id="os_start_time_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="os_start_time_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;"><label id="file_res_label" class="control-label detail-label" style="width: 125px;">文件完整性：</label>

            <div id="file_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="file_res_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;">
            <label id="rpc_res_label" class="control-label detail-label" style="width: 125px">RPC请求检测：</label>

            <div id="rpc_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="rpc_res_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;">
            <label id="puppet_res_label" class="control-label detail-label" style="width: 125px">puppet运行状态：</label>

            <div id="puppet_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="puppet_res_content">无</span>
            </div>
        </div>

        <div class="control-group" style="margin-bottom: 10px;">
            <label id="cplugin_running_res_label" class="control-label detail-label" style="width: 125px">cplugin运行状态：</label>

            <div id="cplugin_running_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="cplugin_running_res_content">无</span>
            </div>
        </div>


        <div class="control-group" style="margin-bottom: 10px;">
            <label id="sg_agent_installed_label" class="control-label detail-label" style="width: 130px;">sg_agent安装信息：</label>
            <div id="sg_agent_installed_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="sg_agent_installed_content">无</span>
                &nbsp;&nbsp;&nbsp;<button class="btn btn-default" type="button" disabled="disabled" id="install_sggent">安装sg_agent</button>
            </div>
        </div>


        <div class="control-group" style="margin-bottom: 10px;">
            <label id="sgagent_running_res_label"class="control-label detail-label" style="width: 130px">sg_agent运行状态：</label>

            <div id="sgagent_running_res_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="sgagent_running_res_content">无</span>
            </div>
        </div>


        <div class="control-group" style="margin-bottom: 10px;">
            <label id="sg_agent_log_label" class="control-label detail-label" style="width: 130px">sg_agent日志统计：</label>

            <div id="sg_agent_log_loading" class="detail-loading">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="controls" style="margin-left: 0px;">
                <span class="outline-content detail-content" id="sg_agent_log_content">无</span>
            </div>
        </div>

    </div>
</div>
