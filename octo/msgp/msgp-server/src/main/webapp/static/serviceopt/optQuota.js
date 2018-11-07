M.add('msgp-serviceopt/optQuota', function (Y) {
    Y.namespace('msgp.serviceopt').optQuota = detailQuota;
    var inited = false;
    var addSupplierDialog;
    var setConsumerDialog;
    var setProviderDialog;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_quota'),
        tbody = wrapper.one('tbody'),
        pbody = wrapper.one('#paginator_quota');

    var everPaged = false,
        totalPage,
        totalCount;

    var spannameList,
        consumerAppkeyList;

    var colspan = 10;

    var capacityInput;
    var watchPeriodInput;
    var qpsQuotaSum = 0;

    var templateStr = [
        '<% Y.Array.each(this.data, function( item, index ){ %>',
        '<tr data-info="<%= Y.JSON.stringify(item) %>">',
        '<td hidden><%= item.id %></td>',
        '<td hidden><%= item.envDesc %></td>',
        '<td><%= item.method %></td>',
        '<td><% if(item.providerCountSwitch ==0){ %> appkey <% } else { %> 主机数 <% } %> </td>',
        '<td><%= item.qpsCapacity %> * <%= item.aliveNode %> = <%= item.aliveNode*item.qpsCapacity %>QPS</td>',
        //'<td><%= aliveNode %> </td>',
        //'<td><%= aliveNode*item.qpsCapacity %> QPS </td>',
        '<td><%= item.watchPeriod %> 秒</td>',
        '<td><% if(item.alarmStatus == 0){ %> 启用 <% } else { %> 停用 <% } %> </td>',
        '<td><% if(item.degradeStatus == 0){ %> 启用 <% } else { %> 停用 <% } %> </td>',
        //'<td><% if(item.degradeend == 0){ %> 服务端 <% } else { %> 客户端 <% } %> </td>',
        //'<td class="last-update-time"><%= Y.mt.date.formatDateByString( new Date(item.utime*1000), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td>',
        '<% Y.Array.each(item.consumers, function(str, index){%>',
        '<p> <% var s0 = str.split(";")[0];var s1 = str.split(";")[1]; var s2 = str.split(";")[2]; var s3 = str.split(";")[3]; if(s2== 0){ %> ' +
        '<%= s0+";"+s1 %>;Drop<%= s3 %> ' +
        '<% } else { %> <%= s0+";"+s1 %> ;Customize<%= s3 %> <% }%>',
        '<% }); %>',
        '</td>',
        '<td><input id="setProvider" type="button" class="btn btn-mini  btn-primary" value="提供者配置" >',
        '</td>',
        '<td> <input id="setConsumer" type="button" class="btn btn-mini  btn-primary" value="消费者配置">',
        '</td>',
        '<td><input id="delProvider" type="button" class="btn btn-mini  btn-del" value="删除">',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    //设置消费者配额对话框
    var dialogConsumerStr = [
        '<div id="consumer_dialog" class="table-responsive">',
        //'<div id="quota_id"><%= this.quotaId %></div>',
        '<p><b>appkey：</b>消费者consumer的appkey。</p>',
        '<p><b>qps比例(%)：</b>预设consumer对provider method的qps请求消耗占总qps比例，要求设置的qps比例总和不超过100，过载时按照预设值进行降级</p>',
        '<p><b>降级策略</b>0：Drop，1：自定义：捕获异常，按需处理</p>',
        //'<p><b>流量导向：</b>dorp方式可以不填，自定义(Customize)方式</p>',
        '<br>',
        '<table id="consumer_table" class="table table-striped table-hover">',
        '<thead id="<%= this.quotaId %>">',
        '<th>appkey</th>',
        '<th>qps比例(%)</th>',
        '<th>降级策略</th>',
        //'<th>流量导向</th>',
        '<th>删除</th>',
        '</thead>',
        '<% Y.Array.each(this.data, function( item, index ){ %>',
        '<tr id="consumer_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">',
        '<td id="appkey"><%= item.consumerAppkey %></td>',
        '<td id="ratio"><%= item.qpsRatio * 100 %></td>',
        '<td id="strategy"><% if(item.strategy == 0){ %> Drop <% } else{ %> Customize <% } %></td>',
        //'<td id="redirect"><%= item.redirect %></td>',
        '<td><input id="delConsumer" type="button" class="btn btn-mini btn-danger" value="-"></td>',
        '</tr>',
        '<% }); %>',
        '</table>',
        '<button id="addConsumer" class="btn btn-success J-config-panel-add">添加</button>',
        '</div>'
    ].join('');

    //新增提供者容量
    var dialogContentStr = [
        '<div id="add_supplier_form" class="form-horizontal">',
        '<div class="control-group"><label class="control-label">方法名：</label>',
        '<div class="controls">',
        '<select id="s_method">',
        '<%Y.Array.each(this.method, function(item, index){ %>',
        '<option value="<%= item %>"> <%= item %> </option>',
        '<% }); %>',
        '</select>',
        //'<input id="s_method" type="text" value="all" placeholder="必填" />',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">环境：</label>',
        '<div class="controls">',
        '<select id="s_env">',
        '<% Y.Object.each( this.data.env, function( value, key ){ %>',
        '<option value="<%= key %>" <%= key== 3 ?"selected":"" %>><%= value %></option>',
        '<% }); %>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">单节点QPS容量：</label>',
        '<div class="controls">',
        '<input id="s_capacity" type="text" value="100" placeholder="必填（数字）" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">节点计算方式：</label>',
        '<div class="controls">',
        '<select id="s_providerCountSwitch"/>',
        '<option value="0" selected="selected">appkey</option>',
        '<option value="1">主机数</option>',
        '</select>',
        '</div>',
        '</div>',
        //'<div class="control-group"><label class="control-label">服务有效节点数:</label>',
        //'<div class="controls">',
        //'<input id="s_aliveNode" type="text" value="<%=this.aliveNode%>" readonly="true" style="background-color:#F3EEEE;" />',
        //'<span class="tips"></span>',
        //'</div>',
        //'</div>',
        '<div class="control-group"><label class="control-label">是否报警：</label>',
        '<div class="controls">',
        '<select id="s_alarm_status">',
        '<option value="0" selected="selected">启用</option>',
        '<option value="1">停用</option>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">是否自动降级：</label>',
        '<div class="controls">',
        '<select id="s_degrade_status">',
        '<option value="0">启用</option>',
        '<option value="1" selected="selected">停用</option>',
        '</select>',
        '</div>',
        '</div>',
        //'<div class="control-group"><label class="control-label">过载保护方式：</label>',
        //'<div class="controls">',
        //'<select id="s_degradeend">',
        //'<option value="0">服务端</option>',
        //'<option value="1" selected="selected">客户端</option>',
        //'</select>',
        //'</div>',
        //'</div>',
        '<div class="control-group"><label class="control-label">监控周期（秒）：</label>',
        '<div class="controls">',
        '<input id="s_watch_period" type="text" value="10" placeholder="必填" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    //设置提供者容量
    var updateProviderStr = [
        '<div id="add_supplier_form" class="form-horizontal" data-info=<%= Y.JSON.stringify(this.data)%>>',
        '<div class="control-group"><label class="control-label">ID：</label>',
        '<div class="controls">',
        '<input id="s_id" type="text" value="<%= this.data.id %>" readOnly="true" style="background-color:#F3EEEE;"/>',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">方法名：</label>',
        '<div class="controls">',
        '<input id="s_method" type="text" value="<%= this.data.method %>" readOnly="true" style="background-color:#F3EEEE;"/>',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">环境：</label>',
        '<div class="controls">',
        '<input id= "s_env" type="text" value=<% if(this.data.env == 3){ %> prod <% } else if(this.data.env == 2){ %> stage <% } else if(this.data.env == 1){%> test <%}%> readOnly="true" style="background-color:#F3EEEE;"</td>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">单节点QPS容量：</label>',
        '<div class="controls">',
        '<input id="s_capacity" type="text" value="<%= this.data.qpsCapacity %>" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">节点计算方式：</label>',
        '<div class="controls">',
        '<select id="s_providerCountSwitch">',
        '<option value="0" <%= (0==this.data.providerCountSwitch)? "selected":""%>>appkey</option>',
        '<option value="1" <%= (1==this.data.providerCountSwitch)? "selected":""%>>主机数</option>',
        '</select>',
        '</div>',
        '</div>',
        //'<div class="control-group"><label class="control-label">服务有效节点数:</label>',
        //'<div class="controls">',
        //'<input id="s_aliveNode" type="text" value="<%=this.aliveNode%>" readonly="true" style="background-color:#F3EEEE;"/>',
        //'<span class="tips"></span>',
        //'</div>',
        //'</div>',
        '<div class="control-group"><label class="control-label">是否报警：</label>',
        '<div class="controls">',
        '<select id="s_alarm_status">',
        '<option value="0" <%= (0==this.data.alarmStatus)? "selected":""%>>启用</option>',
        '<option value="1" <%= (1==this.data.alarmStatus)? "selected":""%>>停用</option>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">是否自动降级：</label>',
        '<div class="controls">',
        '<select id="s_degrade_status">',
        '<option value="0" <%= (0==this.data.degradeStatus)? "selected":""%>>启用</option>',
        '<option value="1" <%= (1==this.data.degradeStatus)? "selected":""%>>停用</option>',
        '</select>',
        '</div>',
        '</div>',
        //'<div class="control-group"><label class="control-label">过载保护方式：</label>',
        //'<div class="controls">',
        //'<select id="s_degradeend">',
        //'<option value="0" <%= (0==this.data.degradeend)? "selected":""%> >服务端</option>',
        //'<option value="1" <%= (1==this.data.degradeend)?"selected":""%> >客户端</option>',
        //'</select>',
        //'</div>',
        //'</div>',
        '<div class="control-group"><label class="control-label">监控周期（秒）：</label>',
        '<div class="controls">',
        '<input id="s_watch_period" type="text" value="<%= this.data.watchPeriod %>"/>',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    function detailQuota(key, func1, func2) {
        if (!inited) {
            appkey = key;
            showOverlay = func1;
            showContent = func2;
            //getSpannames();
            bindGetAgain();
            bindAddProviderQuota();
            bindSetProvider();
            bindDelProvider();
            bindSetConsumer();
            bindEnvSelect();
            inited = true;
        }
        getQuota(1);
    }

    function bindAddProviderQuota() {
        // var url = '/monitor/' + appkey + '/spannames';
        var url = '/service/quota/' + appkey + '/spannames';
        var envval = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var env = "prod";
        if (envval == 3) {
            env = "prod";
        } else if (envval == 2) {
            env = "stage";
        } else if (envval == 1) {
            env = "test";
        }

        Y.io(url, {
            sync: true,
            method: 'get',
            data: {
                env: env
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    spannameList = ret.data;
                    wrapper.delegate('click', function () {
                        addSupplierDialog = addSupplierDialog ? addSupplierDialog : new Y.mt.widget.CommonDialog({
                            id: 'add_supplier_dialog',
                            title: '新增提供者的容量',
                            width: 640,
                            btn: {
                                pass: doAddProviderQuota
                            }
                        });
                        Y.msgp.service.commonMap(showDialogAfter);
                        function showDialogAfter(obj) {
                            var micro = new Y.Template();
                            var str = micro.render(dialogContentStr, {data: obj, method: spannameList});
                            addSupplierDialog.setContent(str);
                            addSupplierDialog.show();
                            initAddSupplierDialog();
                        }
                    }, '#add_supplier');

                },
                failure: function () {
                    //fillSpannames(['all']);
                }
            }
        });
    };

    function bindSetProvider() {
        setProviderDialog = setProviderDialog ? setProviderDialog : new Y.mt.widget.CommonDialog({
            id: 'set_supplier_dialog',
            title: '设置提供者的容量',
            width: 640,
            btn: {
                pass: doUpdateProviderQuota
            }
        });

        wrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var quotaId = getQuotaIdFromLine(line);

            Y.io('/service/quota/' + quotaId + '/provider/getInfo', {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.service.commonMap(showDialogAfter);
                            function showDialogAfter(obj) {
                                var micro = new Y.Template();
                                var str = micro.render(updateProviderStr, {data: ret.data[0]});
                                setProviderDialog.setContent(str);
                                setProviderDialog.show();
                                initAddSupplierDialog();
                            }
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '#setProvider');
    };

    function bindDelProvider() {
        wrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var quotaId = getQuotaIdFromLine(line);

            Y.io('/service/quota/' + quotaId + '/provider/delWithConsumer', {
                method: 'post',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            getQuota(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '#delProvider');
    }

    function bindSetConsumer() {
        setConsumerDialog = setConsumerDialog ? setConsumerDialog : new Y.mt.widget.CommonDialog({
            id: 'add_consumer_dialog',
            title: '设置消费者配额',
            width: 1024,
            btn: {
                pass: doAddConsumerQuota
            }
        });

        bindConsumerButtons(setConsumerDialog);

        wrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var quotaId = getQuotaIdFromLine(line);
            var spanname = getMethodFromLine(line);
            getConsumerAppkeyList(spanname);
            Y.io('/service/quota/' + quotaId + '/consumer/get', {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.service.commonMap(showDialogAfter);
                            function showDialogAfter(obj) {
                                var micro = new Y.Template();
                                var str = micro.render(dialogConsumerStr, {data: ret.data, quotaId: quotaId});

                                setConsumerDialog.setContent(str);
                                setConsumerDialog.show();
                            }
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                        getQuota(1);
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        getQuota(1);
                    }
                }
            });
        }, '#setConsumer');
    }

    function bindConsumerButtons() {
        var dbody = setConsumerDialog.getBody();
        dbody.delegate('click', function () {
            var table = document.getElementById("consumer_table");
            var rowid = table.rows.length - 1;
            var tr = document.createElement("tr");
            tr.id = "consumer_table_tr_" + rowid;
            var tbody = table.tBodies[0];
            /**
             * 此处进行渲染
             * */
            var td0 = document.createElement("td");
            var line = "";
            line += "<select id='appkey'>";
            for (var index = 0; index < consumerAppkeyList.length; index++) {
                //for (var index in consumerAppkeyList) {
                var appkey = consumerAppkeyList[index];
                line += "<option value='" + appkey + "'>" + appkey + "</option>"
            }
            line += "</select>";
            td0.innerHTML = line;
            //td0.innerHTML = "<input id=\"appkey\" type=\"text\" value=\"" + "" + "\" name=\"unit\"/>";
            tr.appendChild(td0);

            var td1 = document.createElement("td");
            // td1.innerHTML = "<input id=\"ratio\" type=\"text\" value=\"" + "10" + "\" name=\"name\" id=\"name\"  />";
            td1.innerHTML = "<input id=\"ratio\"  type=\"text\"  value=\"" + "10" + "\"onkeyup=\"javascript:this.value=this.value.replace(/[^\\d]/g,'');"
                + "if(this.value<0){this.value=0;}"
                + "if(this.value>100){this.value=10;}\" onchange=\"javascript:this.value=this.value.replace(/[^\\d]/g,'');"
                + "if(this.value<0){this.value=0;}"
                + "if(this.value>100){this.value=10;}\" placeholder=\"0-100之间整数，默认为10\" /> ";
            tr.appendChild(td1);

            var td2 = document.createElement("td");
            td2.innerHTML = "<select id='strategy'>" + "<option value='0'>Drop</option>" + "<option value='1'>Customize</option>" + "</select>";
            tr.appendChild(td2);

            //var td3 = document.createElement("td");
            //td3.innerHTML = "<input id=\"redirect\" type=\"text\" value=\"" + "" + "\" name=\"unit\"/>";
            //tr.appendChild(td3);

            var td4 = document.createElement("td");
            td4.innerHTML = "<input id='delConsumer' type=\"button\" class=\"btn btn-mini btn-danger\" value=\"-\"/>";
            tr.appendChild(td4);

            if (tbody == null) table.appendChild(tr);
            else tbody.appendChild(tr);
        }, '#addConsumer');

        dbody.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var table = document.getElementById("consumer_table");
            var tbody = table.tBodies[0];
            var tr = document.getElementById(line._stateProxy.id);
            tr.remove();
            if (line.getData('info') != null) {
                var consumerId = Y.JSON.parse(line.getData('info')).id;
                var url = '/service/quota/' + consumerId + '/consumer/del';

                Y.io(url, {
                    method: 'post',
                    data: {},
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                                //清空搜索框
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                        }
                    }
                });
            }
        }, '#delConsumer');
    }

    function bindEnvSelect() {
        wrapper.delegate('click', function () {
            Y.all('#supplier_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            //清空搜索框
            // wrapper.one('#searchBox').set('value', "");
            getQuota(1);
            bindAddProviderQuota();
        }, "#supplier_env_select a")
    }

    function bindGetAgain() {
        tbody.delegate('click', function () {
            getQuota(1);
        }, '.get-again');
        wrapper.delegate('click', function () {
            getQuota(1);
        }, '#refresh_supplier');
    }

    function doAddProviderQuota() {
        if (!capacityInput.node.getData('status')) {
            capacityInput.showMsg();
            return true;
        }
        if (!watchPeriodInput.node.getData('status')) {
            watchPeriodInput.showMsg();
            return true;
        }
        var url = '/service/quota/' + appkey + '/provider/add';
        var data = {
            appkey: appkey,
            id: 0,
            name: "",
            method: Y.one("#s_method").get('value'),
            alarmStatus: +Y.one('#s_alarm_status').get('value'),
            degradeStatus: +Y.one('#s_degrade_status').get('value'),
            degradeend: 0,//+Y.one('#s_degradeend').get('value'),
            env: +Y.one('#s_env').get('value'),
            qpsCapacity: +Y.one("#s_capacity").get('value'),
            providerCountSwitch: +Y.one("#s_providerCountSwitch").get('value'),
            watchPeriod: +Y.one("#s_watch_period").get('value'),
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000)
        };
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
                        addSupplierDialog.close();
                        //清空搜索框
                        getQuota(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                }
            }
        });
        return true;
    }

    function doUpdateProviderQuota() {

        if (!capacityInput.node.getData('status')) {
            capacityInput.showMsg();
            return true;
        }
        if (!watchPeriodInput.node.getData('status')) {
            watchPeriodInput.showMsg();
            return true;
        }

        var url = '/service/quota/' + appkey + '/provider/update';
        var envTmp = Y.one('#s_env').get('value');
        var envInt;
        if (envTmp == "prod") {
            envInt = 3;
        } else if (envTmp == "stage") {
            envInt = 2;
        } else {
            envInt = 1;
        }
        var data = {
            appkey: appkey,
            id: parseInt(Y.one("#s_id").get('value')),
            name: "",
            method: Y.one("#s_method").get('value'),
            alarmStatus: +Y.one('#s_alarm_status').get('value'),
            degradeStatus: +Y.one('#s_degrade_status').get('value'),
            degradeend: 0,//+Y.one('#s_degradeend').get('value'),
            env: +envInt,
            qpsCapacity: +Y.one("#s_capacity").get('value'),
            providerCountSwitch: +Y.one("#s_providerCountSwitch").get('value'),
            watchPeriod: +Y.one("#s_watch_period").get('value'),
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000)
        };
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getQuota(1);
                        setProviderDialog.close();
                        //清空搜索框
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getQuota(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getQuota(1);
                }
            }
        });
        return true;
    }

    function doAddConsumerQuota() {
        var url = '/service/quota/' + appkey + '/consumer/add';

        var tabledata = Y.one("#consumer_table");
        var listrow = tabledata.all("tr");
        var appQuotaId = parseInt(tabledata.one("thead").get('id'));

        var jsonData = {};
        jsonData.consumers = [];
        qpsQuotaSum = 0;
        listrow.each(function (item, index) {
            if (index != 0) {
                var qpsratioTmp;
                if (item.getData("info") != null) {
                    qpsratioTmp = getQPSQuotaFromLine(item);
                } else {
                    qpsratioTmp = Number(item.one("#ratio").get('value'));
                }
                qpsQuotaSum += qpsratioTmp;
            }
        });
        if (qpsQuotaSum > 100) {
            Y.msgp.utils.msgpHeaderTip('error', 'Consumer qps 配置总和超标,>100', 3);
            return true;
        }
        listrow.each(function (item, index) {
            if (index != 0) {
                var row = {};
                row.consumerAppkey = item.one("#appkey").get('value');
                row.qpsRatio = parseInt(item.one("#ratio").get('value')) / 100.0;
                row.strategy = parseInt(item.one("#strategy").get('value'));
                row.redirect = "";//item.one("#redirect").get('value');
                row.appQuotaId = appQuotaId;
                row.id = 0;

                if (row.consumerAppkey != "")
                    jsonData.consumers.push(row);
            }
        });

        if (jsonData.consumers.length == 0) {
            setConsumerDialog.close();
            getQuota(1);
            return true;
        }

        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(jsonData),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
                        setConsumerDialog.close();
                        //清空搜索框
                        getQuota(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
                        getQuota(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                    getQuota(1);
                }
            }
        });
        return true;
    }

    function getQuota(pageNo) {
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        showContent(wrapper);
        var url = '/service/quota/' + appkey + '/provider/get';

        Y.io(url, {
            method: 'get',
            data: {
                env: env,
                pageNo: pageNo,
                pageSize: 20
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillQuota(data, env);
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else if (data.length === 0) {
                            emptyOrError();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }

    function fillQuota(arr, env) {
        Y.msgp.service.commonMap(fillQuotaAfter);
        function fillQuotaAfter(obj) {
            for (var i = 0, l = arr.length; i < l; i++) {
                var tmp = arr[i];
                tmp.envDesc = obj.env[tmp.env] || tmp.env;
            }
            var html = wrapData(arr);
            tbody.setHTML(html);
            showContent(wrapper);
        }
    }

    function wrapData(arr) {
        var micro = new Y.Template();
        var str = micro.render(templateStr, {data: arr});
        return str;
    }

    function emptyOrError(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(html);
        pbody.empty();
        showContent(wrapper);
    }

    function getQuotaIdFromLine(line) {
        var data = line.getData('info');
        return Y.JSON.parse(data).id;
    }

    function getMethodFromLine(line) {
        var data = line.getData('info');
        return Y.JSON.parse(data).method;
    }

    function getQPSQuotaFromLine(line) {
        var data = line.getData('info');
        return Y.JSON.parse(data).qpsRatio * 100;
    }

    function initAddSupplierDialog() {

        capacityInput = Y.msgp.utils.check.init(Y.one('#s_capacity'), {
            type: 'int',
            minValue: 100,
            maxValue: 100000,
            warnMsg: '单点的QPS容量100-100000！',
            warnElement: Y.one('#s_capacity').next('')
        });
        watchPeriodInput = Y.msgp.utils.check.init(Y.one('#s_watch_period'), {
            type: 'int',
            minValue: 10,
            maxValue: 100000,
            warnMsg: '监控周期为10－100000秒！',
            warnElement: Y.one('#s_watch_period').next('')
        });
    }

    function refreshPaginator(pbody, pobj) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: changePage
        });
    }

    function changePage(params) {
        getQuota(params.page);
    }

    /**
     * 获取provider的方法列表
     * */
    //function getSpannames() {
    //    // var url = '/monitor/' + appkey + '/spannames';
    //    var url = '/service/quota/' + appkey + '/spannames';
    //    var envval = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
    //    var env = "prod";
    //    if (envval == 3) {
    //        env = "prod";
    //    } else if (envval == 2) {
    //        env = "stage";
    //    } else if (envval == 1) {
    //        env = "test";
    //    }
    //
    //    Y.io(url, {
    //        sync: true,
    //        method: 'get',
    //        data: {
    //            env: env
    //        },
    //        on: {
    //            success: function (id, o) {
    //                var ret = Y.JSON.parse(o.responseText);
    //                spannameList = ret.data;
    //
    //            },
    //            failure: function () {
    //                //fillSpannames(['all']);
    //            }
    //        }
    //    });
    //}

    /**查找所有服务的appkey的接口*/
    function getConsumerAppkeyList(var1) {
        var url = '/service/quota/' + appkey + '/consumerAppkeys';
        var envval = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var env = "prod";
        if (envval == 3) {
            env = "prod";
        } else if (envval == 2) {
            env = "stage";
        } else if (envval == 1) {
            env = "test";
        }

        Y.io(url, {
            sync: true,
            method: 'get',
            data: {
                spanname: var1,
                env: env
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    consumerAppkeyList = ret.data;
                },
                failure: function () {

                }
            }
        });
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap'
    ]
});
