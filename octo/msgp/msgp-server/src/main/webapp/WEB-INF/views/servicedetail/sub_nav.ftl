<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'service-provider'
    })
</script>
<div class="tab-box">
    <ul id="tab_trigger" class="nav nav-tabs">
        <li><a href="#supplier">服务提供者</a></li>
        <li><a href="#consumer">服务消费者</a></li>
        <li><a href="#outline">服务概要</a></li>
        <li><a href="#component">组件依赖</a></li>
        <li><a href="#oncall">值班管理</a></li>
        <li><a href="/realtime/entry?appkey=${appkey}">实时日志</a></li>
    </ul>
    <div id="content_wrapper">
        <div id="wrap_supplier" class="sheet" style="display:none;">
        <#include "detail_supplier.ftl" >
        </div>
        <div id="wrap_consumer" class="sheet" style="display:none;">
        <#include "detail_consumer.ftl" >
        </div>
        <div id="wrap_outline" class="sheet" style="display:none;">
        <#include "detail_outline.ftl" >
        </div>
        <div id="wrap_component" class="sheet" style="display:none;">
        <#include "../component/cmpt_detail.ftl" >
        </div>
        <div id="wrap_oncall" class="sheet" style="display:none;">
        <#include "oncall.ftl" >
        </div>
    </div>
</div>