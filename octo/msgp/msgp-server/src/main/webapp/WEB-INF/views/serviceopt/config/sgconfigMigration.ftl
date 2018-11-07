<div class="form-inline mb20">

    <div class="control-group">
        <a>业务：</a>
        <div id="migration_business_line" class="btn-group">
            <a value="1" type="button" class="btn btn-default btn-primary"
               href="javascript:void(0)">非外卖</a>
            <a value="0" type="button" class="btn btn-default"
               href="javascript:void(0)">外卖</a>

        </div>
        <a style="padding-left:0.5em">环境：</a>

        <div id="migration_env" class="btn-group">
            <a value="3" type="button" class="btn btn-default btn-primary"
               href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default dyBtn"
               href="javascript:void(0)">stage</a>
            <a value="1" type="button" class="btn btn-default dyBtn"
               href="javascript:void(0)">test</a>
        </div>
        &nbsp;
        <a>sgconfig app：</a>
        <input id="sgconfig_app_input" class="mb5 span4" type="text"
               placeholder="sgconfig app"/>
        <button id="sgconfig_preview_button" class="btn btn-primary" type="button" style="margin-bottom: 5px;">配置预览
        </button>
        <button id="migration_button" class="btn btn-primary" type="button" style="margin-bottom: 5px;">导入</button>
        <button id="sgconfig_migration_return" class="btn btn-primary" type="button" style="margin-bottom: 5px;">
            <i class='fa fa-step-backward'></i>
            返回
        </button>
        <span class="tips" id="sgconfig_app_tips"></span>


        <a href="https://123.sankuai.com/km/page/39158025" target="_blank" style="float: right;">
            外卖sgconfig迁移<i class="fa fa-question-circle"></i>
        </a>
    </div>
    <div id="sgconfig_data_div" style="height: 350px;">
        <div id="sgconfig_data_loading">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">处理中...</span>
        </div>
     <textarea id="sgconfig_data_view" type="text" style="width: 100%;height: 350px; " readonly>
    </textarea>
    </div>

    <hr/>
    <p>配置预览：根据用户输入的app来获取配置信息</p>

    <p>导入：将app在sgconfig上的配置按照环境来导入到MCC中,原来在MCC上的配置将会被覆盖</p>

    <p>线下环境对应关系：dev -> prod; ppe -> stage; test -> test; 线上将sg.sankuai.com的配置全部导入到MCC的三个环境中,一式三份。</p>
</div>


