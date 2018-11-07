<div class="row-fluid">
    <div id="J-config-container-tree" class="span2 config-tree">
    </div>

    <div id="J-config-container-panel" class="span10" style="margin-top: -10px">
        <div style="font-size: 0;margin-top: 10px;margin-bottom: 10px;">
            <div class="btn-group">
                <button class="btn btn-small btn-alive J-config-panel-configrollback"><i class="fa fa-arrow-left"></i>回滚
                </button>
            </div>
            <div class="btn-group">
                <button class="btn btn-small btn-alive J-config-panel-upload"><i class="fa fa-upload"></i>导入</button>
                <button class="btn btn-small btn-dead J-config-panel-export"><i class="fa fa-download"></i>导出</button>
            </div>
            <div class="btn-group">
                <button class="btn btn-small btn-alive J-config-panel-submit"><i class="fa fa-save"></i>全部保存</button>
                <button class="btn btn-small btn-dead J-config-panel-delete-all"><i class="fa fa-trash-o"></i>全部删除
                </button>
            </div>
            <div class="btn-group ">
                <button class="btn btn-small btn-alive J-config-panel-create-pr"><i class="fa fa-sign-in"></i>创建PR
                </button>
            </div>
            <div class="btn-group">
                <button class="btn btn-small btn-alive J-config-panel-add"><i class="fa fa-plus"></i>添加一项</button>
            </div>
            <div class="btn-group" id="sync-prod-btn" style="display:none;">
                <button class="btn btn-small btn-alive"><i class="fa fa-random"></i>同步至Prod</button>
            </div>
            <input id="mcc_dynamic_file" name="file" type="file" style="display:none;">
        </div>

        <div class="table-responsive">

            <form>
                <table class="table">
                    <thead>
                    <tr>
                        <th width="25%">Key</th>
                        <th width="35%">Value</th>
                        <th width="15%">Comment</th>
                        <th width="10%">操作</th>
                    </tr>
                    </thead>
                    <tbody class="J-config-panel-tbody">
                    </tbody>
                </table>
            </form>

        </div>
        <div id="dynamic_env_loading" class="content-overlay">
            <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div class="config_backToTop">
            <button id="backToTop" class="btn btn-small btn-primary">
                <span class="fa fa-chevron-up"></span></button>
        </div>
    </div>
</div>