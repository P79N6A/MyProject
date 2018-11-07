<#--
    contentBox: 分页组件的容器ID
    formId: 分页组件关联的formId
    url: 分页组件关联的form url
-->
<#macro initPaginator contentBox formId='' url='${request.getRequestUri()}'>
    <#if page?exists>
    <div id="${contentBox}" data-widget="paginator"
         data-params="{totalCount: ${page.totalCount!''},max: ${page.totalPageCount!0},index: ${page.pageNo!0},page: ${page.pageSize!0},contentBox: '#${contentBox}', step:5, jump:true , handlePage:{url:'${url}', formId:'${formId}'}}"></div>
    </#if>
</#macro>
