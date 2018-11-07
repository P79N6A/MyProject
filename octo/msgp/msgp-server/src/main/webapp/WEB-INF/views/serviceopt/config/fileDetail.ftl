<div class="table-responsive">
    <div class="form-inline mb20">
        <#--<a id="detail_file_return" class="config-panel-return" href="javascript:void(0);">返回</a>-->
            <span><#include '/common/env.ftl'>
                <label></label>&nbsp;&nbsp;</span>
        <div class="btn-group">
            <button id="detail_file_return" class="btn btn-primary" style="margin-left: 10px">
                <i class='fa fa-step-backward'></i>
                返回
            </button>
        </div>
    </div>
    <div id="file-config-container-panel">
        <div class="divTableSpace">
            <button id="add-item" class="btn btn-small  v">
                <i class='fa fa-plus'></i>
                添加一项
            </button>
        </div>
        <table class="table">
            <thead>
            <tr>
            <#--<th><input id="all-check" type="checkbox">-->
                <th>
                </th>
                <th width="25%">文件名</th>
                <th width="30%">文件路径</th>
                <th width="15%">备注</th>
                <th width="30%">操作</th>
            </tr>
            </thead>
            <tbody class="J-config-panel-tbody" id="upload_body">
            <tr id="file-content-overlay">
                <td colspan="4">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <br>
    <div id="file-config-machine">
        <div class="divTableSpace">
            <button id="file-config-add-ip" class="btn btn-small btn-default" href="javascript:void(0)">
                <i class="fa fa-edit"></i>
                    修改主机
            </button>
            <button id="down-file-item" class="btn btn-small btn-default" style="margin-left:10px;">
                <i class='fa fa-download'></i>
                下发文件
            </button>
            <div class="input-append fileconfig_search" style="float:right">
                <input id="searchConfigIPInput" class="span3" placeholder="IP或主机名" type="text"/>
                <button class="btn btn-primary" type="button" id="search-config-ip">查询</button>
            </div>

            <div style="margin-right: 10px;float:right">
                <select id="fileconfig_idc" name="fileconfig_idc" title="机房">
                    <option value="全部">全部</option>
                </select>
            </div>
            <label style="float:right">机房：</label>
        </div>
        <table class="table" style="margin-bottom: 0px;">
            <thead>
            <tr>
                <th><input id="all-ipcheck" type="checkbox" style="margin-bottom: 3px;"/> &nbsp;&nbsp;文件下发主机
                </th>
            </tr>
            </thead>
        </table>
        <div style="max-height: 240px;overflow: auto;">
        <table class="table">
                <tbody class="J-config-panel-tbody" id="down_body">
                <tr id="ip-content-overlay">
                    <td>
                        <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                    </td>
                </tr>
                </tbody>
        </table>
        </div>
    </div>
    <div id="file-config-log" class="config-file">
        <h3>文件下发日志</h3>

        <div id="file-config-content" class="config-file log-content" style="overflow: auto;">


        </div>
        <div data-tag="0" id="file-config-redo" style="float: right; padding: 6px 0px;">
            <button id="file-config-enable" class="btn btn-success">
                <i class='fa fa-repeat'></i>
                重试
            </button>
            &nbsp;
            <button id="file-config-cancel" class="btn btn-success">
                <i class='fa fa-trash-o'></i>
                取消
            </button>
        </div>
    </div>
</div>