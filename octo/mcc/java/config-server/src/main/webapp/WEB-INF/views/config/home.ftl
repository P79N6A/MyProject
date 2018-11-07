<div class="row-fluid">
    <div id="J-config-container-header" class="span12">
        <div class="page-header">
            <h1 id="J-config-header-h1" class="pull-left"></h1>
            <div class="pull-left config-header-controller">
                <input type="text" class="text" id="space-text" placeholder="点击切换空间(可以滚动)" />
            </div>
            <div class="pull-left config-header-controller">
                <button id="J-config-header-space-admin" class="btn btn-primary">
                    配置空间管理员
                </button>
            </div>
            <div class="pull-right config-header-controller">
                <button id="J-config-header-delete" class="btn btn-danger">
                    <i class="fa fa-trash-o"></i>
                    删除此空间
                </button>
            </div>
            <div class="pull-right config-header-controller">
                <button id="J-config-header-add" class="btn btn-success">
                    <i class="fa fa-plus"></i>
                    添加空间
                </button>
            </div>
            <div class="pull-right config-header-controller">
                <button id="J-config-header-space-settings" class="btn btn-primary">
                    空间配置设置
                </button>
            </div>
            <div class="pull-right config-header-controller">
                <a href="/config/user/admin" class="btn btn-primary">配置超级管理员</a>
            </div>
        </div>
    </div>
</div>
<div class="row-fluid">
    <div id="J-config-container-tree" class="span4 config-tree">
    </div>
    <div id="J-config-container-panel" class="span8">
        <form class="form-table">
            <table class="table">
                <thead>
                    <tr>
                        <th width="20%">Key</th>
                        <th width="50%">Value</th>
                        <th width="20%">Comment</th>
                        <th width="10%">操作</th>
                    </tr>
                </thead>
                <tbody class="J-config-panel-tbody">
                </tbody>
                <tfoot>
                    <tr class="form-actions">
                        <td colspan="4">
                            <button class="btn btn-success J-config-panel-add">
                                <i class='fa fa-plus'></i>
                                添加一项
                            </button>
                            <button class="btn btn-primary pull-right J-config-panel-submit">
                                确认
                            </button>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </form>
    </div>
</div>

<script>
    YUI().use('config-config');
</script>
