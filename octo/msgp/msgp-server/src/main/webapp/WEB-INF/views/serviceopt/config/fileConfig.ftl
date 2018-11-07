<div id="file_config_group">
    <div class="form-inline mb20">
    </div>
    <table class="table">
        <colgroup>
            <col width="25%"></col>
            <col width="25%"></col>
            <col width="25%"></col>
            <col width="25%"></col>
        </colgroup>
        <thead>
        <tr>
            <th>分组名</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody class="J-config-panel-tbody">
        <tr id="groups_loading">
            <td colspan="4">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </td>
        </tr>
        <tr id="group_error_data">
            <td colspan="4"></td>
        </tr>
        </tbody>
    </table>
    <div id="page_navigation_groups"></div>
    <hr/>
    <p>default分组：系统自动生成；可编辑其IP列表；一个服务通过MCC客户端访问某个appkey的配置文件，当该服务所在的主机的IP不在自定义分组中时会自动保存到该appkey的默认分组中。</p>

    <p>自定义分组：用户自定义分组的IP列表和文件列表；同一个IP不能同时出现在多个分组中。</p>

    <p>【NOTE】一台主机首次获取某个appkey的配置文件时，MCC将返回系统中当前最新的文件内容，并缓存到本地；此后再次读取时，将会直接读取本地文件，而非系统中最新的文件；可通过文件下发的方式更新本地缓存。</p>
</div>
<div id="file_config_detail_wrap">
<#include "fileDetail.ftl">
</div>