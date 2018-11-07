<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="form-horizontal content-body" style="display:none;">

</div>
<div class="overlay-mask-process"></div>
<textarea id="text_detail_outline" style="display:none">
<div class="control-group" style="display:none;"><label class="control-label">服务名：</label>
    <div class="controls">
        <span id="outline_name" class="outline-content"><%= this.data.name %></span>
    </div>
</div>
        <div class="control-group"><label class="control-label">唯一标识：</label>
            <div class="controls">
                <span id="outline_appkey" class="outline-content"><%= this.data.appkey %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">负责人：</label>
            <div class="controls">
                <span id="outline_owners" class="outline-content"><%= this.data.ownersStr %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">关注人：</label>
            <div class="controls">
                <span id="outline_observers" class="outline-content"><%= this.data.observersStr %></span>
            </div>
        </div>
        <div class="control-group" style="display:none;"><label class="control-label">类型：</label>
            <div class="controls">
                <span id="outline_category" class="outline-content"><%= this.data.category %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">所属业务：</label>
            <div class="controls">
                <span id="outline_business" class="outline-content"><%= this.data.owt? (this.data.owt +(this.data.pdl ? " - "+this.data.pdl : "")) : (this.data.businessName + (this.data.group ? " - "+this.data.group : "")) %></span>
            </div>
        </div>
        <div class="control-group" style="display:none;"><label class="control-label">层级：</label>
            <div class="controls">
                <span id="outline_level" class="outline-content"><%= this.data.levelName %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">服务描述：</label>
            <div class="controls">
                <span id="outline_intro" class="outline-content"><%= this.data.intro %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">标签：</label>
            <div class="controls">
                <span id="outline_tags" class="outline-content"><%= this.data.tags %></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label">是否强制验证主机列表：</label>
            <div class="controls">
                <span id="outline_tags" class="outline-content"><% if(this.data.regLimit == 0){ %> 非强制 <% } else{ %> 强制 <% } %></span>
            </div>
        </div>
        <#--<div class="control-group"><label class="control-label">是否开启set：</label>
            <div class="controls">
                <span id="outline_tags" class="outline-content">
                    <input value="1" type="radio" name="set_cell_switch" <#if isCellOpen > checked </#if> style="vertical-align: top"/>
                    <span >&nbsp;开启&nbsp;&nbsp;</span>
                    <input value="0" type="radio" name="set_cell_switch" <#if !isCellOpen > checked </#if> style="vertical-align: top"/>
                    <span >&nbsp;关闭&nbsp;&nbsp;</span>
                </span>
            </div>
        </div>-->
        <div class="control-group"><label class="control-label">创建时间：</label>
            <div class="controls">
                <span id="outline_tags" class="outline-content"><%= Y.mt.date.formatDateByString( new Date(this.data.createTime * 1000), "yyyy-MM-dd hh:mm:ss" ) %></span>
            </div>
        </div>
        <div class="form-actions">
            <a class="btn btn-primary go-modify" href="/service/desc?appkey=<%= this.data.appkey %>">修改</a>&nbsp;&nbsp;
            <a id="delete_outline_service" style="display: none;" data-appkey=<%= this.data.appkey %> href="javascript:void(0);" class="btn btn-primary go-delete"><span>删除</span> </a>
            <a id="delete_outline_service_owner" style="display: none;" data-appkey=<%= this.data.appkey %> href="javascript:void(0);" class="btn btn-danger go-delete"><span>取消负责</span> </a>&nbsp;&nbsp;
            <a id="delete_outline_service_observer" data-appkey=<%= this.data.appkey %> href="javascript:void(0);" class="btn btn-warning go-delete"><span>取消关注</span> </a>
        </div>
</textarea>

