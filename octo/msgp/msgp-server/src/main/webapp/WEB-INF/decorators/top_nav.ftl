<div class="navbar navbar-inverse mt0 ml0">
    <div class="navbar-inner" style="width: 100% !important;">
        <ul id="menus" class="nav" style="margin-right: 0!important;">
        <#if menus??>
            <#assign currentParentMenuId = ((_currentParentMenuId!1))/>
            <#assign currentMenuId = ((_currentMenuId!1))/>
            <#list menus as m>
                <#if m.menus ?? && m.menus?size &gt; 0>
                    <li class="dropdown<#if currentParentMenuId == m.id> current<#else></#if>">
                        <a href="javascript:;" class="dropdown-toggle" data-hover="dropdown">${m.title}
                            <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <#list m.menus as i>
                                <#if i.title == "日志级别调整">
                                    <li><a id="menu-${i.id}" class="menu-list" href="javascript:void(0)"
                                           data-url="${i.url}${appkey!''}" data-appkey="${appkey!''}">${i.title}</a></li>
                                <#else>
                                    <li><a id="menu-${i.id}" class="menu-list" href="javascript:void(0)"
                                           data-url="${i.url}" data-appkey="${appkey!''}">${i.title}</a></li>
                                </#if>
                            </#list>
                        </ul>
                    </li>
                <#else>
                    <li <#if currentParentMenuId == m.id>class="current"</#if>>
                        <a id="menu-${m.id}" class="menu-list" href="javascript:void(0)"
                           data-url="${m.url}" data-appkey="${appkey!''}">${m.title}</a>
                    </li>
                </#if>
            </#list>
        </#if>
        </ul>
    </div>
</div>
