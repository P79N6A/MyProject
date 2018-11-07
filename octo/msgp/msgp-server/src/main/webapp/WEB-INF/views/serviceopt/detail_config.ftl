<link type="text/css" rel="stylesheet" href="/static/css/config.css" media="all">
<link type="text/css" rel="stylesheet" href="/static/config/assets/panel.css" media="all">
<link type="text/css" rel="stylesheet" href="/static/config/assets/popup.css" media="all">
<script src="../../../static/config/Blob.js" type="text/javascript"></script>
<script src="../../../static/config/FileSaver.js" type="text/javascript"></script>
<script src="../../../static/config/tableExport.js" type="text/javascript"></script>
<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body" style="display:none;">
    <div id="wrapper-config-dynamic-file">
        <div id="wrapper-mcc-body">
            <div id="J-config-container-menu">
                <div class="form-inline mb20">
                    <a>配置类型：</a>

                    <div id="config_type" class="btn-group">
                        <a value="dynamic" type="button" class="btn btn-default typeBtn"
                           href="javascript:void(0)">动态</a>
                        <a value="file" type="button" class="btn btn-default typeBtn"
                           href="javascript:void(0)">文件</a>
                    </div>

                <#include "/common/env.ftl" >

                    <div id="dynamic_env" class="btn-group">
                        <a id="prod_btn" value="prod" type="button" class="btn btn-default dyBtn"
                           href="javascript:void(0)">prod</a>
                        <a id="stage_btn" value="stage" type="button" class="btn btn-default dyBtn"
                           href="javascript:void(0)">stage</a>
                        <#if isOffline>
                            <a id="test_btn" value="test" type="button" class="btn btn-default dyBtn"
                               href="javascript:void(0)">test</a>
                        </#if>
                    </div>
                    <div id="file_env" class="btn-group">
                        <a value="3" type="button" id="env_prod" class="btn btn-default fileEnvBtn"
                           href="javascript:void(0)">prod</a>
                        <a value="2" type="button" id="env_stage" class="btn btn-default fileEnvBtn"
                           href="javascript:void(0)">stage</a>
                        <#if isOffline>
                            <a value="1" type="button" id="env_test" class="btn btn-default fileEnvBtn"
                               href="javascript:void(0)">test</a>
                        </#if>
                    </div>
                    <div id="add_new_item" class="btn-group">
                        <button class="btn btn-primary add_group_btn">
                            <i class='fa fa-plus'></i>
                            <span>添加</span>
                        </button>
                    </div>
                  <div id="file_history_btn" class="btn-group">
                        <button class="btn btn-primary file_history_btn">
                            <i class='fa fa-exchange'></i>
                            <span>操作日志</span>
                        </button>
                    </div>
                    <div id="history_btn" class="btn-group">
                        <button class="btn btn-primary">
                            <i class='fa fa-exchange'></i>
                            <span>操作日志</span>
                        </button>
                    </div>

                    <div id="pr_reveiw_manage" class="btn-group">
                        <button class="btn btn-primary">
                            <i class='fa fa-tasks'></i>
                            <span>Review管理</span>
                        </button>
                    </div>

                    <div id="pr_config_settings" class="btn-group">
                        <button class="btn btn-primary">
                            <i class='fa fa-tasks'></i>
                            <span>设置</span>
                        </button>
                        <span class="corner corner-danger">New!</span>
                    </div>

                    <div style="float:right">
                        <a id="sgconfig_migrate" href="javascript:void(0);">
                            sgconfig迁移
                        </a>
                        &nbsp;
                        <a href="https://123.sankuai.com/km/page/28096516" target="_blank">
                            配置管理<i class="fa fa-question-circle"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div id="dynamic_config">
            <#include "config/dynamicConfig.ftl"/>
            </div>
            <div id="file_config">
            <#include "config/fileConfig.ftl"/>
            </div>
        </div>
        <div id="wrapper-sgconfig-migration">
            <div id="wrapper-sgconfig-migration-body">
            <#include "config/sgconfigMigration.ftl"/>
            </div>
        </div>
        <div id="wrapper-review">
            <#include "config/review.ftl"/>
        </div>
        <div id="wrapper-configset">
            <#include "config/configset.ftl"/>
        </div>
        <div id="wrapper-history">
            <#include "config/history.ftl"/>
        </div>
        <div id="wrapper-filelog">
        <#include "config/filelog.ftl"/>
        </div>
    </div>
</div>
