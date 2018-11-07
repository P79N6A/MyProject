/* jshint indent : false */
M.add('msgp-serviceopt/optRoutes-version0.0.18', function (Y) {
        Y.namespace('msgp.serviceopt').optRoutes = detailRoutes;
        //事件绑定等只有在第一次进来时才执行，后续借助此标志位避免重复绑定
        var inited = false;
        var appkey,
            showOverlay,
            showContent,
            wrapper = Y.one('#wrap_routes'),
            tbody = wrapper.one('tbody'),
            thead = wrapper.one('thead'),
            pbody = wrapper.one('#paginator_routes'),
            micro_Template = new Y.Template(),
            pre_category = "0",
            _providerList,
            _consumerList,
            autoCompleteSelect;
        var colspan = 7;
        var existPriority = [];
        //默认的环境选项
        var envType = 3;
        var protocolType = '1';
        var envSelectInit = false;
        var WAIT_PLACEHOLDER = '正在加载数据，请稍候...',
            OK_SINGLE_PLACEHOLDER = '输入服务名搜索或向下滚动选择',
            OK_CHECK_PLACEHOLDER = '可多选，请用控件选择';

        var dialog;
        var isOpen;

        var ip_idc_select = {
            "ip": {
                "id": "optRoutes_apps_select",
                name: "optRoutes_apps_select_auto",
                callback: function (data) {
                    var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
                    var akey = data.name;
                    getSuppier(akey);
                    body.one('#optRoutes_ip_select').set('value', '');
                }
            },
            "idc": {
                "id": "optRoutes_idc_apps_select",
                name: "optRoutes_idc_apps_select_auto",
                callback: function (data) {
                    var akey = data.name;
                    getIdc(akey, idc_consumer_callback);
                }
            }
        };

        Array.prototype.distinct = function () {
            if (this.length <= 0) return;
            this.sort();
            var re = [this[0]];
            for (var i = 1; i < this.length; i++) {
                if (this[i] !== re[re.length - 1]) {
                    re.push(this[i]);
                }
            }
            this.length = re.length;
            for (var i = 0; i < re.length; i++) {
                this[i] = re[i];
            }
        }

        var envSelectStr = [
            '<% Y.Object.each( this.data.env, function( value, key ){ %>',
            '<option value="<%= key %>" <%= key==3?"selected":"" %>><%= value %></option>',
            '<% }); %>'
        ].join('');

        var trTemplate = Y.one('#text_routers').get('text');

        var httpTrTemplate = [
            '<% Y.Array.each(this.data, function(item,index){ %>',
            '<tr data-info="<%= Y.JSON.stringify(item) %>" data-envdesc="<%= item.envDesc %>" data-id="<%=item.name%>">',
            '<td>',
            '<span class="no-change" title="默认分组不能修改"><%= item.name %></span>',
            '</td>',
            '<td>',
            '<a class="do-edit-http config-panel-read" href="javascript:;"  ><i class="fa fa-edit"></i>编辑</a>',
            '<a class="do-delete config-panel-delete" href="javascript:;">&nbsp;&nbsp;&nbsp;<i class="fa fa-trash-o"></i> 删除</a>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');

        var httpTrOldTemplate = [
            '<% Y.Array.each(this.data, function(item,index){ %>',
            '<tr data-info="<%= Y.JSON.stringify(item) %>" data-envdesc="<%= item.envDesc %>" data-id="<%=item.name%>">',
            '<td>',
            '<span class="no-change" title="默认分组不能修改"><%= item.name %></span>',
            '</td>',
            '<td>',
            '<a class="desgroup" href="javascript:;"  ><i class="fa fa-edit"></i>老分组不支持变更同步</a>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');

        // 增加分组，编辑分组，删除分组，查看详情的dialog，为了复用
        var addRouteDialog,
            editRouteDialog,
            providerDiffDialog,
            deleteRouteDialog,
            detailDialog,
            addHttpRouteDialog,
            editHttpRouteDialog,
            deleteHttpRouteDialog;
        // dialog内的用户输入框校验对象，为了校验与复用
        var dNameInput,
            dPriorityInput;
        //可用consumer，provider信息
        var cpData;
        var dialogTemplate;
        var httpDialogTemplate = [
            '<div id="add_supplier_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label"><b>分组名称：</b></label>',
            '<div class="controls">',
            '<input id="name_input" type="text" value="<%= this.name %>" placeholder="分组名称，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            // '<div class="control-group"><label class="control-label"><b>分组描述：</b></label>',
            // '<div class="controls">',
            // '<input id="desc_input" type="text" value="<%= this.desc %>" placeholder="分组描述" />',
            // '<span class="tips"></span>',
            '</div>',
            '</div>',
            '</div>',
            '<div class="block-middle">',
            '<h4 class="text-center mt0 mb10">服务节点</h4>',
            '<div id="provider_ips_ul" class="box dialog-ul-wrapper provider-ul-wrapper" style="overflow: auto;height: 400px;margin-left: 95px;width:370px">',
            '<span id="ip-all-http-check-node" style="cursor:pointer;color: #3fab99;">全选/</span>',
            '<span id="ip-all-http-uncheck-node" style="cursor:pointer;color: #3fab99;">反选/</span>',
            '<span class="ip-yf-http-check-node" style="cursor:pointer;color: #3fab99;">yf(永丰)/</span>',
            '<span class="ip-dx-http-check-node" style="cursor:pointer;color: #3fab99;">dx(大兴)/</span>',
            '<span class="ip-gh-http-check-node" style="cursor:pointer;color: #3fab99;">gh(光环)/</span>',
            '<span class="ip-cq-http-check-node" style="cursor:pointer;color: #3fab99;">cq(次渠)/</span>',
            '<span class="ip-gq-http-check-node" style="cursor:pointer;color: #3fab99;">gq(桂桥)</span>',
            '</div>',
            '</div>',
            '</div>',
            '</div>'
        ].join('');
        var listTemplate = [
            '<ul class="unstyled dialog-ul">',
            '<% var self=this;Y.Array.each(this.data, function(item,index){ %>',
            '<li class="">',
            '<label class="checkbox mb0">',
            '<input type="checkbox" <%= (self.arr && Y.Array.indexOf(self.arr, item) >= 0)? "checked":"" %> />',
            '<span><%= item %></span>',
            '</label>',
            '</li>',
            '<% }); %>',
            '</ul>'
        ].join('');

        var providerDetailTemplate = [
            '<div id="provider-detail-table" class="form-horizontal">',
            '<table class="table table-bordered">',
            '<thead>',
            '<tr>',
            '<th>修改前</th>',
            '<th>修改后</th>',
            '</tr>',
            '</thead>',
            '<tbody>',
            '<tr>',
            '<td width="50%" style="word-break:break-all;"><pre style="height:250px;overflow-y:auto" class="providerOldValue"></pre></td>',
            '<td width="50%" style="word-break:break-all;overflow-y:hidden"><pre style="height:250px;overflow-y:auto" class="providerNewValue"></pre></td>',
            '</tr>',
            '</tbody>',
            '</table>',
            '</div>'
        ].join('');


        var providerTemplateDetail = Y.one('#text_provider_dialog_ul').get('text');

        var consumerTemplateDetail = Y.one('#text_consumer_dialog_ul').get('text');

        var idcTemplateDetail = Y.one('#text_idc_dialog_ul').get('text');
        var listTemplateDetail = Y.one('#text_dialog_ul').get('text');
        var routeThriftProviderDetail = Y.one('#text_route_thrift_provider_ul').get('text');

        function detailRoutes(key, func1, func2) {
            if (!inited) {
                appkey = key;
                showOverlay = func1;
                showContent = func2;

                //绑定thrift/http选择按钮
                bindProtocolSelect();
                //绑定环境选择按钮
                bindEnvSelect();
                //绑定『增加分组』『编辑分组』事件
                bindAddEditRoute();
                //绑定http『增加分组』『编辑分组』事件
                bindAddEditHttpRoute();
                wrapper.one('#add_route_http').hide();
                //绑定『启用按机房自动归组』按钮事件
                //bindEnableDefault();
                //表格内的事件绑定
                bindTableEvent();
                //弹窗内的事件绑定
                bindDialogEvent();

                inited = true;

                Y.msgp.service.setEnvText('routes_env_select');
            }
            getRoutes(1);
        }

        var checkIPs;

        function manualAddIPs(body) {
            if (!checkIPs.isValid()) {
                checkIPs.showMsg();
                return;
            }
            var textArea = body.one('#add_ip_textarea');
            var ips = textArea.get('value').split('\n');
            var ips_arr = new Array();
            Y.Array.each(ips, function (item) {
                var ip = Y.Lang.trim(item);
                if (ip.length > 0) {
                    ips_arr.push(ip);
                }
            });
            ips_arr.distinct();
            var consumer_block = body.one('#consumer_ips_ul');
            var existIPs = consumer_block.all('input');
            var ips_result = new Array();
            var isExist = false;
            Y.Array.each(ips_arr, function (x) {
                isExist = false;
                Y.Array.each(existIPs._nodes, function (y) {
                    var ip_temp = y.getAttribute('value');
                    if (ip_temp === x) {
                        isExist = true;
                    }
                });
                if (!isExist) {
                    ips_result.push(x);
                }
            });
            var micro = new Y.Template();
            var html = micro.render(listTemplateDetail, {
                data: ips_result
            });
            consumer_block.append(html);
            textArea.set('value', '');
        }

        function checkManualAddIPs() {
            var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
            var textArea = body.one('#add_ip_textarea');
            var ips = textArea.get('value').split('\n');

            Y.Array.each(ips, function (item, index) {
                var ip = Y.Lang.trim(item);
                var rowNum = index + 1;
                if ((ip.length > 0) && (!Y.msgp.utils.checkIP(ip))) {
                    _setMsg(checkIPs, "第" + rowNum + "行的IP地址[" + ip + "]非法", false);
                }

            });


            function _setMsg(element, msg, type) {
                element._setStatus(type);
                element.opt.warnElement.setHTML(msg).setStyle('color', '#f00');
            }
        }

        //绑定『增加分组』『编辑分组』事件
        function bindAddEditRoute() {
            wrapper.delegate('click', function () {
                addRouteDialog = addRouteDialog ? addRouteDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_route_dialog',
                    title: '增加分组',
                    width: 1000,
                    btn: {
                        pass: doAddRoute
                    }
                });
                var url = '/service/' + appkey + '/group/providers';
                Y.msgp.serviceopt.getConsumerProvider(url, {env: envType}, "正在获取服务节点列表...", showDialogAfter);
                function showDialogAfter(arr) {
                    var tempArray = new Array();
                    Y.Array.each(arr, function (x) {
                        var node = new ipTag();
                        node.ip = x.ip + ":" + x.port;
                        node.showText = x.name + ":" + x.port;
                        tempArray.push(node);
                    });
                    sortIpArrEdit(tempArray);

                    _providerList = tempArray;
                    dialogTemplate = getRouterDialog("0");
                    var html = micro_Template.render(dialogTemplate, {
                        data: tempArray,
                        isAdd: true,
                        envType: envType,
                        priority: getRandomPriority(),
                        reserved: "route_limit:0"
                    });
                    addRouteDialog.setContent(html);
                    addRouteDialog.getBody().one("#exclude")._node.style.display = 'inline';
                    addRouteDialog.getBody().one("#cell-feature")._node.style.display = 'none';
                    addRouteDialog.getBody().one("#force")._node.style.display = 'inline';
                    addRouteDialog.show();
                    //绑定ip、idc 事件
                    var body = addRouteDialog.getBody();
                    bindIpIdcChange(body);
                    bindExcludeGroup(body);
                    initAddIpRouter(true);
                }
            }, '#add_route');

            tbody.delegate('click', function () {
                var line = this.ancestor('tr');
                pre_category = this.getData("category")

                editRouteDialog = editRouteDialog ? editRouteDialog : new Y.mt.widget.CommonDialog({
                    id: 'edit_route_dialog',
                    title: '编辑分组',
                    width: 760,
                    btn: {
                        pass: doEditRoute,
                        passName: "保存"
                    }
                });

                var router_detail_url = '/service/' + appkey + '/group/detail';
                var data_param = {
                    id: line.getData('id'),
                    env: envType
                };
                    Y.io(router_detail_url, {
                        method: 'get',
                        data: data_param,
                        sync: true,
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (!ret.isSuccess) {
                                    Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                                    return;
                                }
                                var data = ret.data;
                                var count = 0;
                                data.provider.forEach(function (item) {
                                    if (-1 != item.indexOf("*")) {
                                        count++;
                                    }
                                });
                                if (0 != count) {
                                    pre_category = "2";
                                }
                            },
                            failure: function (id, o) {
                                hideDialog();
                                Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                            }
                        }
                    });
                dialogTemplate = getRouterDialog(pre_category);
                var routerAfter = pre_category == "0" || pre_category == "4" ? ipRouterAfter : idcRouterAfter;
                getRouteDetail(router_detail_url, data_param, "正在获取调用者和服务提供者列表...", routerAfter);

                function ipRouterAfter(routerData) {
                    var providersUrl = '/service/' + appkey + '/group/providers';
                    var data2 = {
                        env: envType
                    };
                    Y.io(providersUrl, {
                        method: 'get',
                        data: data2,
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (!ret.isSuccess) {
                                    Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                                    return;
                                }
                                var providerIPs = ret.data;
                                hideDialog();

                                if (!routerData.provider) {
                                    routerData = Y.JSON.parse(routerData);
                                    var providerArr = [];
                                    routerData.server.forEach(function (item) {
                                        providerArr.push(item.ip + ':' + item.port);
                                    });
                                    routerData.provider = providerArr;
                                }

                                var tempArray = new Array();
                                var tempArray1 = new Array();
                                for (var i = 0; i < routerData.provider.length; ++i) {
                                    tempArray[i] = 0;
                                }
                                Y.Array.each(providerIPs, function (x) {
                                    var tmp = new ipTag();
                                    tmp.ip = x.ip + ":" + x.port;
                                    tmp.showText = x.name + ":" + x.port;
                                    for (var i = 0; i < routerData.provider.length; ++i) {
                                        var it = routerData.provider[i];
                                        if (it === tmp.ip) {
                                            tmp.isExist = true;
                                            tempArray[i] = 1;
                                            break;
                                        }
                                    }
                                    tempArray1.push(tmp);
                                });
                                Y.Array.each(tempArray, function (item, index) {
                                    if (0 == item) {
                                        var tmp = new ipTag();
                                        tmp.showText = routerData.provider[index];
                                        tmp.ip = routerData.provider[index];
                                        tmp.isExist = true;
                                        tempArray1.push(tmp);
                                    }
                                });
                                routerData.providerIPTags = tempArray1;
                                //填充数据，填充触发事件
                                cpData = routerData;
                                if (routerData.providerIPTags) {
                                    sortIpArrEdit(routerData.providerIPTags);
                                    editRouteDialog.text = Y.JSON.stringify(routerData.provider);
                                }
                                var html = micro_Template.render(dialogTemplate, {
                                    envDesc: line.getData('envdesc'),
                                    envType: envType,
                                    name: routerData.name,
                                    isAdd: false,
                                    priority: routerData.priority,
                                    reserved: routerData.reserved,
                                    pre_category: pre_category
                                });

                                editRouteDialog.setContent(html);
                                editRouteDialog.getBody().one("#exclude")._node.style.display = 'inline';
                                editRouteDialog.getBody().one("#cell-feature")._node.style.display = 'none';
                                editRouteDialog.getBody().one("#force")._node.style.display = 'inline';

                                editRouteDialog.show();
                                _providerList = routerData.providerIPTags;
                                _consumerList = routerData.consumer.ips
                                addRouteDialog = null;
                                initAddIpRouter(false);
                            },
                            failure: function (id, o) {
                                Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                            }
                        }
                    });
                }

                function idcRouterAfter(routerData) {
                    var url = '/service/provider/idclist';
                    var data2 = {
                        appkey: appkey,
                        type: 1
                    };
                    Y.io(url, {
                        method: 'get',
                        data: data2,
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (!ret.isSuccess) {
                                    Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                                    return;
                                }
                                var providerIdc = ret.data;
                                hideDialog();
                                Y.Array.each(providerIdc, function (x) {
                                    var ip = x.ipprefix;
                                    for (var i = 0; i < routerData.provider.length; ++i) {
                                        var it = routerData.provider[i];
                                        if (it == ip) {
                                            x.isExist = true;
                                            break;
                                        }
                                    }
                                });

                                cpData = routerData;
                                editRouteDialog.text = Y.JSON.stringify(routerData.provider);
                                var html = micro_Template.render(dialogTemplate, {
                                    envDesc: line.getData('envdesc'),
                                    envType: envType,
                                    name: routerData.name,
                                    isAdd: false,
                                    priority: routerData.priority,
                                    reserved: routerData.reserved
                                });
                                //console.log(html)
                                editRouteDialog.setContent(html);
                                editRouteDialog.getBody().one("#exclude")._node.style.display = 'none';
                                editRouteDialog.getBody().one("#cell-feature")._node.style.display = 'inline';
                                editRouteDialog.getBody().one("#force")._node.style.display = 'inline';
                                editRouteDialog.show();
                                var consumer_idc_ips = routerData.consumer.ips;
                                var consumer_idcs = routerData.consumer.idcs;
                                var consumer_array = new Array();
                                for (var i = 0; i < consumer_idc_ips.length; ++i) {
                                    var ipprefix = consumer_idc_ips[i];
                                    var idc = consumer_idcs[i];
                                    var obj = {"ipprefix": ipprefix, "idc": idc, "isExist": true}
                                    consumer_array.push(obj)
                                }

                                addRouteDialog = null;
                                idc_consumer_callback(consumer_array);
                                idc_provider_callback(providerIdc);
                                initDialogCheck(editRouteDialog.getBody());
                            },


                            failure: function (id, o) {
                                Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                            }

                        }
                    });
                }

            }, '.do-edit');
        }


        function getRouterDialog(category) {
            var text_addrouter_dialog = Y.one('#text_addrouter_dialog').get('value');
            var text_ip_consumer_provider = Y.one('#text_consumer_provider_' + category).get('value');
            var dialogTemplate_text = text_addrouter_dialog.replace("#div_consumer_provider#", text_ip_consumer_provider);
            return dialogTemplate_text;
        }

        function bindIpIdcChange(dialogbody) {
            dialogbody.delegate('click', function () {
                Y.all('#routes_ip_idc a').removeClass('btn-primary');
                this.addClass("btn-primary");
                var category = this.getAttribute('value');
                if (pre_category == category) {
                    return;
                }
                var text_consumer_provider = Y.one('#text_consumer_provider_' + category).get('value');
                dialogbody.one("#div_consumer_provider").setHTML(text_consumer_provider);
                pre_category = category;
                if (category == "0") {
                    dialogbody.one("#exclude")._node.style.display = 'inline';
                    dialogbody.one("#cell-feature")._node.style.display = 'none';
                    dialogbody.one("#force")._node.style.display = 'inline';
                    initAddIpRouter(true);
                } else {
                    dialogbody.one("#exclude")._node.style.display = 'none';
                    dialogbody.one("#cell-feature")._node.style.display = 'inline';
                    dialogbody.one("#force")._node.style.display = 'inline';
                    initAddIdcRouter();
                    getAllService(dialogbody);
                }
            }, "#routes_ip_idc a")
        }

        function bindExcludeGroup(dialogbody) {
            var exclude_group = Y.all('#exclude-group');
            exclude_group.set("checked", false);
            Y.msgp.utils.addTooltipWithContent("#exclude-group-desc", '选中服务端排他分组后，分组内的服务节点只允许分组内的调用方节点访问，其他非组内指定节点无法访问。');
            Y.msgp.utils.addTooltipWithContent("#force-change-desc", '选中强制，当分组中服务节点不可用时，将调用失败。');
            dialogbody.delegate('click', function () {
                var check = Y.one('#exclude-group').get("checked");
                pre_category = check == false ? '0' : '4';
            }, '#exclude-group');
        }

        /**
         *
         * @param isAdd 默认是 add
         */
        function initAddIpRouter(isAdd) {
            var body = isAdd ? addRouteDialog.getBody() : editRouteDialog.getBody();
            var providerHtml = micro_Template.render(routeThriftProviderDetail, {
                data: _providerList
            });
            body.one('#provider_ips_ul').append(providerHtml);
            var textArea = body.one('#add_ip_textarea');
            if (!isAdd) {
                var consumerHtml = micro_Template.render(consumerTemplateDetail, {
                    data: _consumerList
                });
                body.one('#consumer_ips_ul').setHTML(consumerHtml);
            }
            checkIPs = Y.msgp.utils.check.init(textArea, {
                chineseOk: false,
                spaceOk: true,
                emptyOk: true,
                callback: checkManualAddIPs,
                warnElement: Y.one('#manual_add_ip_tips')
            });

            body.delegate('click', function () {
                manualAddIPs(body);
            }, '#add_IPs_manual');
            body.delegate('change', function (e) {
                var searchValue = this.get('value');
                var providerUlDom = this.ancestor('div').one('#provider_ips_ul');
                var lis = providerUlDom.all('li');
                if (null == searchValue || undefined == searchValue || '' === searchValue.trim()) {
                    // nothing search, show all. nothing to do.
                    lis.show();
                } else {
                    lis.hide();
                    var keys = searchValue.split(' ');
                    Y.each(lis, function (li) {
                        var liText = li.one('span').get('text');
                        keys.forEach(function (key) {
                            if ('' !== key.trim()) {
                                if (liText.indexOf(key.trim()) >= 0) {
                                    li.show();
                                }
                            }
                        });
                    });
                }
            }, '#route_provider_search_input');
            body.delegate('click', function () {
                body.one("#provider_ips_ul").all('li input').each(function (node) {
                    if (!node.ancestor('li').get("hidden")) {
                        node.set("checked", true);
                    }
                });
            }, '#ip-all-check-node');
            body.delegate('click', function () {
                body.one("#provider_ips_ul").all('li input').each(function (nd) {
                    if (!nd.ancestor('li').get("hidden")) {
                        var isChecked = !this.get('checked');
                        nd.set("checked", isChecked);
                    }
                });
            }, '#ip-all-uncheck-node');

            body.delegate('click', function () {
                var ifChecked = this.get("checked");
                if (!ifChecked) {
                    body.one('#route-instructions').hide();
                    body.one('#route-instructions').hide();
                } else {
                    body.one('#route-instructions').show();
                }
            }, '#show-instructions');

            initDialogCheck(body);
            getAllService(body);
        }

        function initAddIdcRouter() {
            getIdc(appkey, idc_provider_callback)
        }

        function checkPriority() {
            var isEdit = (null != addRouteDialog) ? false : true;
            var body = isEdit ? editRouteDialog.getBody() : addRouteDialog.getBody();
            var priority_input = body.one('#priority_input');
            var currentPrioriy = Number(priority_input.get('value'));
            var isExist = checkPriorityExist(currentPrioriy);
            if (!isExist) return;
            if (isEdit && currentPrioriy === Number(cpData.priority)) {
                return;
            }
            _setMsg(dPriorityInput, "优先级已存在", false);
            function _setMsg(element, msg, type) {
                element._setStatus(type);
                element.opt.warnElement.setHTML(msg).setStyle('color', '#f00');
            }
        }

        //初始化弹窗内的input输入框的校验
        function initDialogCheck(body) {
            dNameInput = Y.msgp.utils.check.init(body.one('#name_input'), {
                type: 'string',
                maxLength: 100,
                spaceOk: true,
                warnMsg: '最多100个字符',
                warnElement: body.one('#name_input').next()
            });
            dPriorityInput = Y.msgp.utils.check.init(body.one('#priority_input'), {
                type: 'custom',
                customRegExp: /^[1-9]\d*$/,
                warnMsg: '必须是正整数',
                callback: checkPriority,
                warnElement: body.one('#priority_input').next()
            });
        }


        //弹窗增加分组的『确定』按钮回调
        function doAddRoute() {
            if (!checkDialogInput()) {
                return true;
            }

            var cpObj = checkDialogUl(false);
            if (!cpObj) {
                return true;
            }
            var data = patchDialogData(cpObj);
            data.status = 1;
            refreshLineData(data, successCallback, errorCallback);
            function successCallback() {
                Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                addRouteDialog.close();
                getRoutes(1);
            }

            function errorCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('error', msg || '添加失败', 3);
            }
        }

        //弹窗编辑分组『确定』按钮回调
        function doEditRoute() {
            if (!checkDialogInput(true)) {
                return true;
            }
            var cpObj = checkDialogUl(true);
            if (!cpObj) {
                return true;
            }
            var oldProviderListText = Y.JSON.parse(editRouteDialog.text).join("\n");
            var newProviderListText = cpObj.provider.join("\n");
            providerDiffDialog = providerDiffDialog ? providerDiffDialog : new Y.mt.widget.CommonDialog({
                id: 'detail_provider_dialog',
                title: '服务提供者修改对比',
                width: 450,
                btn: {
                    pass: confirmEditRoute
                }
            });
            var micro = new Y.Template();
            var html = micro.render(providerDetailTemplate, {
                oldContent: oldProviderListText,
                newContent: newProviderListText
            });
            providerDiffDialog.setContent(html);
            providerDiffDialog.show();
            var body = providerDiffDialog.getBody(),
                tbody = body.one('#provider-detail-table tbody');
            tbody.one('.providerOldValue').set("text", oldProviderListText);
            tbody.one('.providerNewValue').set("text", newProviderListText);

            function confirmEditRoute() {
                cpObj = checkDialogUl(true);
                var data = patchDialogData(cpObj);
                data.id = cpData.id;
                data.createTime = cpData.createTime;
                data.status = cpData.status;
                var url = '/service/' + appkey + '/group/edit';
                Y.io(url, {
                    method: "POST",
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(data),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                                getRoutes(1);
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '编辑失败', 3);
                            }

                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '编辑失败', 3);
                        }
                    }
                });
            }
        }

        //封装增加，编辑分组请求的数据
        function patchDialogData(cpObj) {
            var obj = {
                appkey: appkey,
                name: dNameInput.node.get('value'),
                priority: +dPriorityInput.node.get('value'),
                env: envType,//+Y.one('#dialog_env_select').get('value'),
                reserved: cpObj.reserved,
                category: cpObj.category,
                consumer: cpObj.consumer,
                provider: cpObj.provider
            };
            return obj;
        }

        //检查dialog输入框内的值是否合法
        //编辑分组时需要比增加做更复杂的优先级重复校验
        function checkDialogInput(isEdit) {
            if (!dNameInput.node.getData('status') || !dPriorityInput.node.getData('status')) {
                dNameInput.showMsg();
                dPriorityInput.showMsg();
                return false;
            }
            var pVal = +dPriorityInput.node.get('value');
            var isExist = checkPriorityExist(pVal);
            if (!isExist) return true;

            if (isEdit && pVal === cpData.priority) return true;
            dPriorityInput.node.next().setHTML('优先级数值已存在').setStyle('color', '#f00');
            return false;
        }

        //检查增加或修改分组时，consumer与provider是否选中内容
        function checkDialogUl(isEdit) {
            var category_customize;
            var obj;
            var dBody = isEdit ? editRouteDialog.getBody() : addRouteDialog.getBody();
            var cWrapper = dBody.one('#consumer_ips_ul') ? dBody.one('#consumer_ips_ul') : dBody.one('#consumer_idc_ul'),
                pWrapper = dBody.one('#provider_ips_ul') ? dBody.one('#provider_ips_ul') : dBody.one('#provider_idc_ul');

            var ipClicked = cWrapper.all('input:checked'),
                providerClicked = pWrapper.all('input:checked');

            if (0 === providerClicked.size()) {
                Y.msgp.utils.msgpHeaderTip('error', '服务节点不能为空', 3);
                return false;
            }
            if (0 === ipClicked.size()) {
                Y.msgp.utils.msgpHeaderTip('error', '调用方不能为空', 3);
                return false;
            }

            obj = {
                consumer: {
                    ips: [],
                    idcs: [],
                    appkeys: [],
                },
                provider: []
            };

            if(cWrapper == dBody.one('#consumer_ips_ul') && pWrapper == dBody.one('#provider_ips_ul')){
                category_customize = "0";
            } else {
                category_customize = "2";
            }

            var t = dBody.one('#force-change').get("checked");
            var force = t ? "route_limit:1" : "route_limit:0";
            obj.reserved = force;
            var check = dBody.one('#exclude-group').get("checked");
            pre_category = check == false ? category_customize : "4";

            obj.category = parseInt(pre_category);
            ipClicked.each(function (item) {
                var ip = item.getAttribute('value');
                var idc = item.getData('idc');
                if (idc != undefined) {
                    obj.consumer.idcs.push(idc);
                }
                obj.consumer.ips.push(ip);
            });
            providerClicked.each(function (item) {
                var ip = item.getAttribute('value');
                obj.provider.push(ip);
            });

            return obj;
        }

        //绑定启用，停用默认分组功能
        function bindEnableDefault() {
            Y.one('#enable_default_group').on('click', function () {
                toggleDefaultGroup(true);
            });
        }

        function bindEnvSelect() {
            wrapper.delegate('click', function () {
                Y.all('#routes_env_select a').removeClass('btn-primary');
                this.addClass("btn-primary");
                envType = parseInt(this.getAttribute('value'));
                getRoutes(1);
            }, "#routes_env_select a")
        }

        //绑定table里的相关事件
        function bindTableEvent() {
            //绑定重新获取事件
            tbody.delegate('click', function () {
                getRoutes(1);
            }, '.get-again');
            //绑定『查看详情』事件
            tbody.delegate('click', function () {
                var line = this.ancestor('tr');
                showDetailDialog(line);
            }, '.see-detail');
            //绑定『禁用』『启用』按钮事件
            wrapper.delegate('click', function () {
                var msg = this.getData('status') === '0' ? '禁用' : '启用';
                if (this.hasClass('active')) {
                    return;
                }
                clickAbleButton(this, msg);
            }, '.btn-enabled .btn');
            //绑定默认分组的强制非强制
            wrapper.delegate('click', function () {
                var el = this;
                if (el.hasClass('active')) return;
                var force = el.getData('force');
                if (el.ancestor("div").hasClass("is-multicenter")) {
                    forceMultiCenterGroup(force);
                } else {
                    forceDefaultGroup(force);
                }
            }, '.btn-forced .btn');
            //绑定 删除分组 事件
            tbody.delegate('click', function () {
                var line = this.ancestor('tr');
                deleteRouteDialog = deleteRouteDialog ? deleteRouteDialog : new Y.mt.widget.CommonDialog({
                    title: '删除分组',
                    width: 400,
                    content: '你确定要删除这个分组吗？',
                    btn: {
                        pass: doDeleteRoute
                    }
                });
                deleteRouteDialog.tr = line;
                var info = patchDataForChangeLine(line);
                deleteRouteDialog.setContent('你确定要删除分组 <span class="text-red">' + info.name + '</span> 吗？');
                deleteRouteDialog.show();
            }, '.do-delete');
            //绑定环境select change的事件
            wrapper.delegate('change', function () {
                var env = this.get('value');
                envType = +env;
                getRoutes(1);
            }, '#env_select_routes');
        }

        //显示 consumer provider 弹窗
        function showDetailDialog(line) {
            var info = patchDataForChangeLine(line);
            info.envDesc = line.getData('envdesc');
            detailDialog = detailDialog ? detailDialog : new Y.mt.widget.CommonDialog({
                id: 'detail_dialog',
                width: 760,
                btn: {
                    passName: '修改',
                    pass: function () {
                        detailToEdit();
                    }
                }
            });
            detailDialog.setTitle('查看详情 - ' + info.name);
            //把这一行的dom节点绑定到弹窗上，以便在修改时使用
            detailDialog.line = line;
            var micro = new Y.Template();
            var html = micro.render(dialogTemplate, {
                isDetail: true,
                envType: envType,
                envDesc: info.envDesc,
                info: info,
                reserved: info.reserved
            });
            detailDialog.setContent(html);

            var ipHtml = micro.render(listTemplateDetail, {
                data: info.consumer.ips
            });
            var providerHtml = micro.render(listTemplateDetail, {
                data: info.provider
            });
            var dBody = detailDialog.getBody();
            dBody.one('.ips-ul-wrapper').setHTML(ipHtml);
            //dBody.one('.appkeys-ul-wrapper').setHTML( appkeyHtml );
            dBody.one('.provider-ul-wrapper').setHTML(providerHtml);

            detailDialog.show();
        }

        function detailToEdit() {
            //从详情弹窗上获取到这一行的dom节点
            var line = detailDialog.line;
            //做个延时，避免中间状态的mask被错误的删除掉
            setTimeout(function () {
                //模拟一个点击事件，以便开启编辑窗口
                line.one('.do-edit').simulate('click');
            }, 10);
        }

        //切换默认分组的启用与禁用
        var reminderDialogTemplate = [
            '<div>',
            '<%= this.data %>',
            '</div>'
        ].join('');

        function toggleDefaultGroup(status) {
            var url = '/service/' + appkey + '/group/default';
            var data = {
                env: envType,
                action: status ? "enable" : "disable"
            };
            //当启用自动分组时需要提示风险
            if (status) {
                var verifyUrl = '/service/' + appkey + '/group/verify';
                Y.io(verifyUrl, {
                    method: 'get',
                    data: {
                        envId: envType
                    },
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                //不需要提示风险，直接执行启用
                                doToggleDefaultGroup(url, data)
                            } else {
                                //提示风险
                                var micro = new Y.Template();
                                var html = micro.render(reminderDialogTemplate, {
                                    data: ret.msg
                                });
                                var reminderDialog = new Y.mt.widget.CommonDialog({
                                    id: 'reminder_dialog',
                                    title: '启用默认分组提示',
                                    width: 450,
                                    btn: {
                                        passName: '启用',
                                        pass: function () {
                                            doToggleDefaultGroup(url, data);
                                        }
                                    }
                                });
                                reminderDialog.setContent(html);
                                reminderDialog.show();
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                        }
                    }
                });
            } else {
                doToggleDefaultGroup(url, data);
            }
        }

        function toggleDefaultMultiCenterGroup(status) {
            var url = '/service/' + appkey + '/group/multicenter';
            var data = {
                env: envType,
                action: status ? "enable" : "disable"
            };
            //当启用自动分组时需要提示风险
            if (status) {
                var verifyUrl = '/service/' + appkey + '/group/verify/multicenter';
                Y.io(verifyUrl, {
                    method: 'get',
                    data: {
                        envId: envType
                    },
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                //不需要提示风险，直接执行启用
                                doToggleMultiCenterGroup(url, data)
                            } else {
                                //提示风险
                                var micro = new Y.Template();
                                var html = micro.render(reminderDialogTemplate, {
                                    data: ret.msg
                                });
                                var reminderDialog = new Y.mt.widget.CommonDialog({
                                    id: 'reminder_dialog',
                                    title: '启用多中心提示',
                                    width: 450,
                                    btn: {
                                        passName: '启用',
                                        pass: function () {
                                            doToggleMultiCenterGroup(url, data);
                                        }
                                    }
                                });
                                reminderDialog.setContent(html);
                                reminderDialog.show();
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                        }
                    }
                });
            } else {
                doToggleMultiCenterGroup(url, data);
            }
        }

        function forceMultiCenterGroup(force) {
            var url = '/service/' + appkey + '/group/multicenter/force';
            var data = {
                env: envType,
                reserved: force
            };
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                            getRoutes(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    }
                }
            });
        }

        //真正切换默认分组启用与禁用
        function doToggleMultiCenterGroup(url, data) {
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                            getRoutes(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    }
                }
            });
        }

        //默认分组强制、非强制
        function forceDefaultGroup(force) {
            var url = '/service/' + appkey + '/group/default/force';
            var data = {
                env: envType,
                reserved: force
            };
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                            getRoutes(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    }
                }
            });
        }

        //真正切换默认分组启用与禁用
        function doToggleDefaultGroup(url, data) {
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                            getRoutes(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    }
                }
            });
        }


        //删除分组弹窗确定的回调
        function doDeleteRoute() {
            var line = deleteRouteDialog.tr;
            var isThrift = '1' === protocolType;
            var url = isThrift ? '/service/' + appkey + '/group/' + patchDataForChangeLine(line).id
                : '/hlb/group/delete?appkey=' + appkey + '&env=' + getEnvStr(envType) +
                '&group_name=' + patchDataForChangeLine(line).name;

            if (isThrift) {
                Y.io(url, {
                    method: 'delete',
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if ((isThrift && ret.isSuccess) || (!isThrift && ret.code === 200)) {
                                Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                                line.transition({
                                    opacity: 0,
                                    duration: 0.6
                                }, function () {
                                    line.remove();
                                });
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                        }
                    }
                });
            } else{

                var url1 = '/hlb/group/search?appkey=' + appkey;
                var data1 = {
                    env: getEnvStr(envType),
                    group_name: patchDataForChangeLine(line).name
                };

                deleteRouteDetail(url1, data1, showAfter);
                function showAfter(data) {
                    cpData = data;
                    var cpObj = deleteHttpDialogUl(cpData);
                    if (!cpObj) {
                        return true;
                    }
                    //var data = patchHttpDialogData(dataList);
                    //var url = '/hlb/group/prod/create';
                    var url = '/service/' + appkey + '/provider/' + 2 + '/list';
                    refreshHttpLineData(cpObj, url, successCallback, errorCallback);

                    function successCallback(msg) {
                        Y.msgp.utils.msgpHeaderTip('success', "删除成功", 3);
                        getRoutes(1);
                    }

                    function errorCallback(msg) {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }

                }

            }
        }

        function deleteRouteDetail(url, data1, callback) {

            Y.io(url, {
                method: 'get',
                data: data1,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var data = ret.data

                        var providerArr = [];
                        for (var i = 0, l = data.length; i < l; i++) {
                            if (data[i]['groupInfo'] == data1.group_name) {
                                //providerArr.push(data[i]['ip'] + ':' + data[i]['port']);
                                providerArr.push(data[i]);
                            }
                        }
                        ret.data = providerArr;
                        if (ret.isSuccess) {
                            callback && callback(ret.data);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '加载数据失败', 3);
                        }
                    },
                    failure: function () {
                        hideDialog();
                        Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                    }
                }
            });
        }

        //绑定弹窗内的事件，包含列表的高亮切换，环境select change事件
        function bindDialogEvent() {
            Y.one(document).delegate('click', function () {
            }, '#add_route_dialog ul li,#edit_route_dialog ul li');
        }

        //没有内容或者获取失败渲染内容
        function emptyOrError(isError) {
            colspan = ('1' === protocolType) ? 8 : 3;
            var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            tbody.setHTML(html);
            pbody.empty();
            showContent(wrapper);
        }

        function setLoading() {
            colspan = ('1' === protocolType) ? 8 : 3;
            var html = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>';
            tbody.setHTML(html);
            pbody.empty();
            showContent(wrapper);
        }

        //获取分组的ajax请求逻辑
        function getRoutes(pageNo) {
            setLoading();
            var isThrift = '1' === protocolType;
            var url = isThrift ? ('/service/' + appkey + '/group') :
                ('/hlb/group/list?appkey=' + appkey);
            var env = isThrift ? envType : getEnvStr(envType);
            var data1
            if(!isThrift){
                var url1 = '/hlb/group/listold?appkey=' + appkey;
                Y.io(url1, {
                    method: 'get',
                    data: {
                        env: env,
                        pageNo: pageNo,
                        pageSize: 10000
                    },
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if ((isThrift && ret.isSuccess) || (!isThrift)) {
                                 data1 = ret.data;
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
            Y.io(url, {
                method: 'get',
                data: {
                    env: env,
                    pageNo: pageNo,
                    pageSize: 10000
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if ((isThrift && ret.isSuccess) || (!isThrift)) {
                            displayThs(isThrift);
                            //清空已存在优先级数组
                            existPriority.length = 0;
                            var data = ret.data;
                            if (Y.Lang.isArray(data) && data.length !== 0 ) {
                                if (isThrift) {
                                    fillRoutes(data);
                                } else {
                                    fillHttpRoutes(ret.data,data1);
                                }
                            } else if (data.length === 0) {
                                emptyOrError();
                            }

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

        //获取分组后往table内填充
        function fillRoutes(arr) {
            Y.msgp.service.commonMap(fillRoutesAfter);
            function fillRoutesAfter(obj) {
                for (var i = 0, l = arr.length; i < l; i++) {
                    var tmp = arr[i];
                    tmp.envDesc = Y.one('#routes_env_select a.btn-primary').get('text');
                    var categoryDesc = "默认分组";
                    switch (tmp.category) {
                        case 0:
                            categoryDesc = "自定义IP分组";
                            break;
                        case 1:
                            categoryDesc = "同机房优先分组";
                            break;
                        case 2:
                            categoryDesc = "自定义机房分组";
                            break;
                        case 3:
                            categoryDesc = "同中心优先分组";
                            break;
                        case 4:
                            categoryDesc = "服务端排他分组";
                            break;
                    }
                    tmp.categoryDesc = categoryDesc;
                    //保存已存在优先级数组，为之后的优先级重复校验
                    existPriority.push(tmp.priority);
                }
                var html = wrapData(arr);
                tbody.setHTML(html);
                showContent(wrapper);
                initLocalEdit();
            }
        }

        //封装需要填充到table内的html
        function wrapData(arr) {
            var micro = new Y.Template();
            var str = micro.render(trTemplate, {data: arr});
            return str;
        }

        //初始化所有的需要原地编辑的内容，包含分组名称，优先级
        function initLocalEdit() {
            var reg = /^[1-9]\d*$/g;
            var msg = '必须是正整数';
            Y.msgp.utils.localEdit('.change-priority', doChangePriority, reg, msg);
            var reg_1 = /^.{1,100}$/g;
            var msg_1 = '不能为空，最长100个字符';
            Y.msgp.utils.localEdit('.change-name', doChangeName, reg_1, msg_1);
        }

        //禁用启用切换的逻辑
        function doChangeStatus(el, status) {
            var line = el.ancestor('tr');
            var data = patchDataForChangeLine(line);
            data.status = status;
            refreshLineData(data, successCallback, errorCallback);
            function successCallback(newData) {
                Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                refreshLineUI(line, newData);
            }

            function errorCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
            }
        }

        //修改优先级的逻辑
        function doChangePriority(node, oldValue, newValue) {
            var isExist = checkPriorityExist(+newValue);
            if (isExist) {
                Y.msgp.utils.msgpHeaderTip('error', '优先级数值已存在', 3);
                node.setHTML(oldValue);
                return;
            }
            var line = node.ancestor('tr');
            var data = patchDataForChangeLine(line);
            data.priority = +newValue;
            refreshLineData(data, successCallback, errorCallback);
            function successCallback(newData) {
                Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                refreshExistPriority(oldValue, newValue);
                refreshLineUI(line, newData);
            }

            function errorCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
                node.setHTML(oldValue);
            }
        }

        //修改分组名称的逻辑
        function doChangeName(node, oldValue, newValue) {
            var line = node.ancestor('tr');
            var data = patchDataForChangeLine(line);
            data.name = newValue;
            refreshLineData(data, successCallback, errorCallback);
            function successCallback(newData) {
                Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                refreshLineUI(line, newData);
            }

            function errorCallback() {
                Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                node.setHTML(oldValue);
            }
        }

        //检查优先级是否已经存在
        function checkPriorityExist(val) {
            return Y.Array.indexOf(existPriority, val) >= 0;
        }

        //获取一个随机的priority，目前最大值+1
        function getRandomPriority() {
            var max = Math.max.apply(null, existPriority);
            return max + 1;
        }

        //刷新所有已存在优先级的缓存
        function refreshExistPriority(oldValue, newValue) {
            var index = Y.Array.indexOf(existPriority, oldValue);
            if (index >= 0) {
                existPriority[index] = newValue;
            } else {
                existPriority.push(newValue);
            }
        }

        //更新一行数据，ajax请求
        //增加一行数据也使用这个方法
        function refreshLineData(data, sc, ec) {
            var url = '/service/' + appkey + '/group';
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            sc && sc(ret.data);
                        } else {
                            ec && ec(ret.msg);
                        }
                    },
                    failure: function () {
                        ec && ec();
                    }
                }
            });
        }

        //封装请求数据
        function patchDataForChangeLine(line) {
            var obj;
            var info = line.getData('info');
            obj = Y.Lang.isString(info) ? Y.JSON.parse(info) : info;
            return obj;
        }

        //数据更新后，刷新页面UI
        function refreshLineUI(line, data) {
            var tds = line.all('td');
            line.setData('info', data);
            tds.item(5).setHTML(Y.mt.date.formatDateByString(new Date(data.updateTime), "yyyy-MM-dd hh:mm:ss"));
            var ltd = tds.item(6);
            ltd.one('.active').removeClass('active');
            ltd.one('[data-status="' + data.status + '"]').addClass('active');
        }

        //初始化table表头部分的环境选择select，数据从后台请求，所有环境相关内容做到前端透明
        function initEnvSelect(obj) {
            var envSelect = Y.one('#env_select_routes');
            var micro = new Y.Template();
            var html = micro.render(envSelectStr, {data: obj});
            envSelect.setHTML(html);
            //envSelect.getDOMNode().innerHTML = html;
            envSelectInit = true;
        }

        //刷新分页
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
            getRoutes(params.page);
        }


        function clickAbleButton(el, msg) {
            var dialog = new Y.mt.widget.CommonDialog({
                id: '_dialog',
                title: '确认',
                content: '确认' + msg + '吗',
                width: 300,
                btn: {
                    pass: doAbleButtonPass
                }
            });
            if ('启用' == msg) {
                if (el.ancestor().hasClass('is-default')) {
                    dialog.setContent('确认启用吗, 该操作会禁用同中心优先分组');
                } else if (el.ancestor().hasClass('is-multicenter')) {
                    dialog.setContent('确认启用吗, 该操作会禁用同机房优先分组');
                }
            } else {
                dialog.setContent('确认禁用吗?');
            }

            dialog.show();
            function doAbleButtonPass() {
                var status = +el.getData('status');
                //如果这行数据是默认分组数据，需要做特殊处理
                if (el.ancestor().hasClass('is-default')) {
                    toggleDefaultGroup(!!status);
                    if (status) {
                        toggleDefaultMultiCenterGroup(!status);
                    }

                } else if (el.ancestor().hasClass('is-multicenter')) {
                    toggleDefaultMultiCenterGroup(!!status);
                    if (status) {
                        toggleDefaultGroup(!status);
                    }
                } else {
                    doChangeStatus(el, status);
                }
            }
        }

        function sortIpArr(arr) {
            arr.sort(function (a, b) {
                var compare1 = a.split('.')[0] - b.split('.')[0];
                var compare2 = a.split('.')[1] - b.split('.')[1];
                var compare3 = a.split('.')[2] - b.split('.')[2];
                var compare4 = a.split('.')[3].split(':')[0] - b.split('.')[3].split(':')[0];
                var compare5 = a.split(':')[1] - b.split(':')[1];
                return compare1 ? compare1 : compare2 ? compare2 : compare3 ? compare3 : compare4 ? compare4 : compare5;
            });
        }

        function sortIpArrEdit(data) {
            data.sort(function (a, b) {
                var compare1 = a.ip.split('.')[0] - b.ip.split('.')[0];
                var compare2 = a.ip.split('.')[1] - b.ip.split('.')[1];
                var compare3 = a.ip.split('.')[2] - b.ip.split('.')[2];
                var compare4 = a.ip.split('.')[3].split(':')[0] - b.ip.split('.')[3].split(':')[0];
                var compare5 = a.ip.split(':')[1] - b.ip.split(':')[1];
                return compare1 ? compare1 : compare2 ? compare2 : compare3 ? compare3 : compare4 ? compare4 : compare5;
            });
        }

        function optRoutesAutoCompleteList(list, opt_select) {
            var obj = [];
            for (var i = 0, l = list.length; i < l; i++) {
                obj.push({id: i, name: list[i]});
            }

            autoCompleteSelect = new Y.mt.widget.AutoCompleteList({
                id: opt_select.name,
                node: Y.one("#" + opt_select.id),
                listParam: 'name',
                objList: obj,
                showMax: obj.length,
                matchMode: 'fuzzy',
                more: "",
                callback: opt_select.callback
            })
            Y.one("#" + opt_select.name).one(".widget-autocomplete-complete-list").setStyle("height", "400px");
            Y.one("#" + opt_select.name).one(".widget-autocomplete-menu-operator").remove();
        }

        function getAllServiceOwn(body) {
            var opt_select = ip_idc_select["ip"];
            if (pre_category == "2") {
                opt_select = ip_idc_select["idc"];
            }
            var inputNode = body.one('#' + opt_select.id);
            inputNode.setAttribute('placeholder', WAIT_PLACEHOLDER);
            var url = '/service/owner';
            Y.io(url, {
                method: 'get',
                cache: false,
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.isSuccess) {
                            var data = res.data;
                            optRoutesAutoCompleteList(data, opt_select);
                            inputNode.setAttribute('placeholder', OK_SINGLE_PLACEHOLDER);
                        }
                    }
                }
            });
        };

        function getAllService(body) {
            var opt_select = ip_idc_select["ip"];
            if (pre_category == "2") {
                opt_select = ip_idc_select["idc"];
            }
            var inputNode = body.one('#' + opt_select.id);
            inputNode.setAttribute('placeholder', WAIT_PLACEHOLDER);
            var url = '/api/apps';
            Y.io(url, {
                method: 'get',
                cache: false,
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.isSuccess) {
                            var data = res.data;
                            optRoutesAutoCompleteList(data, opt_select);
                            inputNode.setAttribute('placeholder', OK_SINGLE_PLACEHOLDER);
                        }
                    }
                }
            });
        };

        function getSuppier(appkey) {
            var inputNode = Y.one('#optRoutes_ip_select');
            inputNode.setAttribute('placeholder', WAIT_PLACEHOLDER);
            var url = '/service/' + appkey + '/provider/iplist';
            Y.io(url, {
                method: 'get',
                data: {
                    type: 1,
                    env: envType,
                    status: '-1',
                    sort: -8,
                    pageSize: -1
                },
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.isSuccess) {
                            var data = res.data;
                            setOptRoutesIpList(data);
                            inputNode.setAttribute('placeholder', OK_CHECK_PLACEHOLDER);
                        }
                    }
                }
            });
        };
        function setOptRoutesIpList(list, callbackFun) {
            var obj = [];
            for (var i = 0, l = list.length; i < l; i++) {
                obj.push({id: i, name: list[i], checked: true});
            }
            var aclist = new Y.mt.widget.AutoCompleteList({
                id: "optRoutes_ip_select_auto",
                node: Y.one("#optRoutes_ip_select"),
                listParam: 'name',
                objList: obj,
                //showMax: obj.length,
                matchMode: 'fuzzy',
                more: "",
                showCheckbox: true,
                showCheckedInList: true,
                fillTarget: true,
                fillTargetInput: '10.4.243.121'
            });
            aclist.on('afterShowResultBox', function () {
                var checkboxs = Y.one("#optRoutes_ip_select_auto").one('.widget-autocomplete-list').all('input[name="widgetCheckbox"]');
                checkboxs.setAttribute('checked', 'checked');
            });
            Y.one("#optRoutes_ip_select_auto").one(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
            Y.one("#optRoutes_ip_select_auto").one(".widget-autocomplete-complete-list").one('.widget-autocomplete-menu-operator').delegate('click', function () {
                ipSelectedIps();
            }, 'li');
        }

        function ipSelectedIps(aclist) {
            var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
            var checked = [];
            var checkboxs = Y.one("#optRoutes_ip_select_auto").one('.widget-autocomplete-list').all('input[name="widgetCheckbox"]:checked');
            checkboxs.each(function (item) {
                var curIp = item.ancestor('label').one('span').get('text');
                checked.push(curIp);
            });
            body.one('#optRoutes_ip_select').set('value', checked.join(','));
            var dataval = body.one('#optRoutes_ip_select').get('value');
            var dataarr = dataval.split(',');
            var textArea = body.one('#add_ip_textarea');
            if ((null || undefined || '' || '无') === dataval) {
                return;
            }
            if ((null || undefined || '') === textArea.get('value')) {
                textArea.set('value', dataarr.join('\n'));
            } else {
                dataarr = checkIpExist(dataarr);
                textArea.set('value', textArea.get('value') + '\n' + dataarr.join('\n'));
            }
        }

        function checkIpExist(selectedIps) {
            var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
            var textArea = body.one('#add_ip_textarea');
            var ips = textArea.get('value').split('\n');
            var res = false;
            Y.Array.each(ips, function (item, index) {
                var ip = Y.Lang.trim(item);
                if (selectedIps && selectedIps.length > 0) {
                    Y.Array.each(selectedIps, function (selectIp, index) {
                        if (selectIp === ip) {
                            selectedIps.splice(index, 1);
                            return;
                        }
                    })
                }
            });
            return selectedIps;
        }


        function bindProtocolSelect() {
            wrapper.delegate('click', function () {
                Y.all('#routes_thrift_http a').removeClass('btn-primary');
                this.addClass("btn-primary");
                protocolType = this.getAttribute('value');
                getRoutes(1);
                if ('1' === protocolType) {
                    wrapper.one('#add_route').show();
                    wrapper.one('#add_route_http').hide();
                } else {
                    wrapper.one('#add_route').hide();
                    wrapper.one('#add_route_http').show();
                }
            }, "#routes_thrift_http a")
        }

        function fillHttpOldRoutes(arr) {
            var ths = thead.all('th');
            ths.item(2).hide();
            ths.item(3).hide();
            ths.item(4).hide();
            ths.item(5).hide();
            Y.msgp.service.commonMap(fillHttpRoutesAfter);
            var obj;
            function fillHttpRoutesAfter() {
                var obj = [];
                if(typeof(arr) != "undefined"){
                    for (var i = 0, l = arr.length; i < l; i++) {
                        var tmp = {};
                        tmp.name = arr[i];
                        obj.push(tmp);
                        }
                }
                html = wrapHttpOldData(obj);

            }
            return html
        }

        //获取分组后往table内填充
        function fillHttpRoutes(arr,arr1) {
            var ths = thead.all('th');
            ths.item(2).hide();
            ths.item(3).hide();
            ths.item(4).hide();
            ths.item(5).hide();
            Y.msgp.service.commonMap(fillHttpRoutesAfter);

            function fillHttpRoutesAfter() {
                var htmlold = fillHttpOldRoutes(arr1)
                var obj = [];
                var rep = {};
                for (var i = 0, l = arr.length; i < l; i++) {
                    var tmp = {};
                    tmp.name = arr[i]['groupInfo'];
                    if (tmp.name!=null && tmp.name.length!=0 && !rep[tmp.name]){
                        obj.push(tmp);
                        rep[tmp.name] =1;
                    }

                }
                if (obj.length != 0) {
                    var html = htmlold + wrapHttpData(obj);
                    tbody.setHTML(html);
                    showContent(wrapper);
                    initLocalEdit();

                }else if(typeof(arr1) != "undefined" && arr1.length !=0){
                    var html = htmlold ;
                    tbody.setHTML(html);
                    showContent(wrapper);
                    initLocalEdit();
                } else {
                    emptyOrError();
                }
            }
        }

        //封装需要填充到table内的html
        function wrapHttpData(arr) {
            var micro = new Y.Template();
            var str = micro.render(httpTrTemplate, {data: arr});
            return str;
        }

        //封装需要填充到table内的html
        function wrapHttpOldData(arr) {
            var micro = new Y.Template();
            var str = micro.render(httpTrOldTemplate, {data: arr});
            return str;
        }

        //绑定『增加分组』『编辑分组』事件
        function bindAddEditHttpRoute() {
            var edit = -1;
            wrapper.delegate('click', function () {
                addHttpRouteDialog = addHttpRouteDialog ? addHttpRouteDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_route_dialog',
                    title: '增加分组',
                    width: 600,
                    btn: {
                        pass: doAddHttpRoute
                    }
                });

                // var url = '/service/' + appkey + '/provider/all';
                // var param = {env: envType, type: 'http'};


                if(edit < 0) {
                    edit = 1;
                }
                var url = '/hlb/group/search?appkey=' + appkey;
                var param = {env: getEnvStr(envType)};
                Y.msgp.serviceopt.getHttpConsumerProvider(url, param, "正在获取服务节点列表...", showDialogAfter);
                function showDialogAfter(arr) {
                    sortIpArr(arr);
                    cpData = arr;
                    var micro = new Y.Template();
                    var html = micro.render(httpDialogTemplate, {
                        data: arr,
                        isAdd: true,
                        envType: envType
                    });
                    addHttpRouteDialog.setContent(html);
                    addHttpRouteDialog.show();
                    var providerHtml = micro.render(listTemplateDetail, {
                        data: arr
                    });
                    addHttpRouteDialog.getBody().one('#provider_ips_ul').append(providerHtml);
                    var body = addHttpRouteDialog.getBody();
                    body.delegate('click', function () {
                        body.one('#provider_ips_ul').all('li input').set("checked", true);
                    }, '#ip-all-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=永丰'
                        var sp = document.getElementsByClassName("ip-yf-http-check-node");
                        sp[1-edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-yf-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=大兴'
                        var sp = document.getElementsByClassName("ip-dx-http-check-node");
                        sp[1-edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-dx-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=光环'
                        var sp = document.getElementsByClassName("ip-gh-http-check-node");
                        sp[1-edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-gh-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=次渠'
                        var sp = document.getElementsByClassName("ip-cq-http-check-node");
                        sp[1-edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-cq-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=桂桥（点评）'
                        var sp = document.getElementsByClassName("ip-gq-http-check-node");
                        sp[1-edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-gq-http-check-node');
                    body.delegate('click', function () {
                        body.one('#provider_ips_ul').all('li input').each(function (nd) {
                            var isChecked = !this.get('checked');
                            nd.set("checked", isChecked);
                        });
                    }, '#ip-all-http-uncheck-node');
                    initHttpDialogCheck(body);
                }
            }, '#add_route_http');

            tbody.delegate('click', function () {
                var line = this.ancestor('tr');
                editHttpRouteDialog = editHttpRouteDialog ? editHttpRouteDialog : new Y.mt.widget.CommonDialog({
                    id: 'edit_route_dialog',
                    title: '编辑分组',
                    width: 500,
                    btn: {
                        pass: doEditHttpRoute,
                        passName: "保存"
                    }
                });
                if(edit < 0) {
                    edit = 0;
                }
                var url1 = '/hlb/group/search?appkey=' + appkey;
                var data1 = {
                    env: getEnvStr(envType),
                    group_name: line.getData('id')
                };
                var url2 = '/service/' + appkey + '/provider/all';
                var data2 = {
                    env: envType,
                    type: 'http'
                };
                Y.msgp.serviceopt.getRouteDetail(url1, url2, data1, data2, "正在获取调用者和服务提供者列表...", showAfter);

                function showAfter(data) {
                    cpData = data;
                    if (data.providerIPTags) {
                        sortIpArrEdit(data.providerIPTags);
                    }
                    var micro = new Y.Template();
                    var html = micro.render(httpDialogTemplate, {
                        envDesc: line.getData('envdesc'),
                        envType: envType,
                        name: data1.group_name,
                        desc: data.desc
                    });

                    editHttpRouteDialog.setContent(html);
                    editHttpRouteDialog.show();
                    var body = editHttpRouteDialog.getBody();
                    var providerHtml = micro.render(providerTemplateDetail, {
                        data: data.providerIPTags
                    });
                    body.one('#provider_ips_ul').append(providerHtml);
                    body.delegate('click', function () {
                        body.one('#provider_ips_ul').all('li input').set("checked", true);
                    }, '#ip-all-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=永丰'
                        var sp = document.getElementsByClassName("ip-yf-http-check-node");
                        sp[edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-yf-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=大兴'
                        var sp = document.getElementsByClassName("ip-dx-http-check-node");
                        sp[edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-dx-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=光环'
                        var sp = document.getElementsByClassName("ip-gh-http-check-node");
                        sp[edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-gh-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=次渠'
                        var sp = document.getElementsByClassName("ip-cq-http-check-node");
                        sp[edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-cq-http-check-node');
                    body.delegate('click', function () {
                        var url = '/service/' + appkey + '/provider/querybyidc?idcname=桂桥（点评）'
                        var sp = document.getElementsByClassName("ip-gq-http-check-node");
                        sp[edit].setAttribute("style", "cursor:pointer;color: #ff4100;");
                        var re =getHttpidcProvider(url,showAfter)
                        function showAfter(re) {
                            body.one('#provider_ips_ul').all('li input').each(function (nd) {
                                var name = this.get('defaultValue');
                                if (isInArray(re,name)) {
                                    nd.set("checked", true);
                                }
                            });
                        }
                    }, '.ip-gq-http-check-node');
                    body.delegate('click', function () {
                        body.one('#provider_ips_ul').all('li input').each(function (nd) {
                            var isChecked = !this.get('checked');
                            nd.set("checked", isChecked);
                        });
                    }, '#ip-all-http-uncheck-node');
                    body.one('#name_input').setAttribute("readonly", "readonly");
                    initHttpDialogCheck(body);
                }
            }, '.do-edit-http');
        }

        function isInArray(arr,value){
            for(var i = 0; i < arr.length; i++){
                if(value === arr[i]){
                    return true;
                }
            }
            return false;
        }

        function getHttpidcProvider(url,callback) {
            Y.io(url, {
                data: {
                    type: 2,
                    env: envType,
                    status: status,
                    pageNo: 1,
                    pageSize: 10000,
                    sort: -8
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var re = [];
                            for (var i = 0; i < data.length; i++) {
                                re.push(data[i]["ip"]+":"+data[i]["port"])
                            }
                            callback(re)
                        } else {
                            emptyOrError(true);
                        }
                    },
                    failure: function () {
                        if (!isAbort)
                            emptyOrError(true);
                    }
                }
            });
        }


        //初始化弹窗内的input输入框的校验
        function initHttpDialogCheck(body) {
            dNameInput = Y.msgp.utils.check.init(body.one('#name_input'), {
                type: 'string',
                maxLength: 100,
                spaceOk: true,
                warnMsg: '最多100个字符',
                warnElement: body.one('#name_input').next()
            });
        }

        //弹窗增加分组的『确定』按钮回调
        function doAddHttpRoute() {
            if (!checkHttpDialogInput()) {
                return true;
            }

            var cpObj = checkHttpDialogUl(false);
            if (!cpObj) {
                return true;
            }
            //var data = patchHttpDialogData(dataList);
            //var url = '/hlb/group/prod/create';
            var url = '/service/' + appkey + '/provider/' + 2 + '/list';
            refreshHttpLineData(cpObj, url, successCallback, errorCallback);
            function successCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('success', "添加成功", 3);
                addHttpRouteDialog.close();
                getRoutes(1);
            }

            function errorCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
            }
        }

        //检查dialog输入框内的值是否合法
        function checkHttpDialogInput() {
            if (!dNameInput.node.getData('status')) {
                dNameInput.showMsg();
                return false;
            }
            return true;
        }

        function isInArray(arr,value){
            for(var i = 0; i < arr.length; i++){
                if(value == arr[i]){
                return true;
                }
            }
            return false;
        }

        function groupNameCheck (value){
            var reg = /^[0-9a-zA-Z-_]+$/
            if(!reg.test(value))
            {
              return false
            }
            return true

        }

        //检查增加或修改分组时，consumer与provider是否选中内容
        function checkHttpDialogUl(isEdit) {
            var obj;
            var dBody = isEdit ? editHttpRouteDialog.getBody() : addHttpRouteDialog.getBody();
            var desc = dBody.one('#name_input').get('value');

            if(!groupNameCheck(desc)){
                Y.msgp.utils.msgpHeaderTip('error', '分组数据包含非法字符', 3);
                return false;
            }

            var pWrapper = dBody.one('#provider_ips_ul');
            var providerClicked = pWrapper.all('input:checked');
            var providerUnclicked =pWrapper.all('input');
            if (0 === providerClicked.size()) {
                Y.msgp.utils.msgpHeaderTip('error', '服务节点不能为空', 3);
                return false;
            }

            obj = {
                provider: []
            };
            // var desc = dBody.one('#desc_input').get('value');
            // obj.desc = desc;
            var dataList = [];
            var replist = [];
            providerClicked.each(function (item) {
                var node = item.next().getHTML().split(':');
                var ipport = item.next().getHTML()
                var nodeObj = {
                    ip: node[0],
                    port: +node[1]
                };
                replist.push(ipport);
                var data = patchHttpDialogData(nodeObj);
                dataList.push(data);
                //obj.provider.push(nodeObj);
            });
            providerUnclicked.each(function (item) {
                var node = item.next().getHTML().split(':');
                var ipport = item.next().getHTML()
                var nodeObj = {
                    ip: node[0],
                    port: +node[1]
                };

                if(!isInArray(replist,ipport)) {
                    var data = patchHttpNoDialogData(nodeObj);
                    dataList.push(data);
                }
                //obj.provider.push(nodeObj);
            });

            return dataList;
        }

        //删除分组时，consumer与provider是否选中内容
        function deleteHttpDialogUl(data) {

            var dataList = [];
            for (var i = 0, l = data.length; i < l; i++) {
                var nodeObj = {
                    ip: data[i]['ip'],
                    port:data[i]['port']
                };
                var data1 = patchdeleteHttpDialogData(nodeObj);
                dataList.push(data1);
            }

            return dataList;
        }


        //删除分组请求的数据
        function patchdeleteHttpDialogData(cpObj) {
            var obj = {
                appkey: appkey,
                groupInfo:"",
                //desc: cpObj.desc,
                env: envType,//+Y.one('#dialog_env_select').get('value'),
                ip: cpObj.ip,
                port: cpObj.port
            };
            return obj;
        }

        //封装增加，编辑分组请求的数据
        function patchHttpDialogData(cpObj) {
            var obj = {
                appkey: appkey,
                groupInfo: dNameInput.node.get('value'),
                //desc: cpObj.desc,
                env: envType,//+Y.one('#dialog_env_select').get('value'),
                ip: cpObj.ip,
                port: cpObj.port
            };
            return obj;
        }

        function patchHttpNoDialogData(cpObj) {
            var obj = {
                appkey: appkey,
                groupInfo: "",
                //desc: cpObj.desc,
                env: envType,//+Y.one('#dialog_env_select').get('value'),
                ip: cpObj.ip,
                port: cpObj.port
            };
            return obj;
        }

        //更新一行数据，ajax请求
        //增加一行数据也使用这个方法
        function refreshHttpLineData(data, url, sc, ec) {
            Y.io(url, {
                method: 'put',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            sc && sc(ret.data);
                        } else {
                            ec && ec(ret.msg);
                        }
                    },
                    failure: function () {
                        ec && ec();
                    }
                }
            });
        }

        //弹窗编辑分组『确定』按钮回调
        function doEditHttpRoute() {
            if (!checkHttpDialogInput(true)) {
                return true;
            }
            var cpObj = checkHttpDialogUl(true);
            if (!cpObj) {
                return true;
            }
            //var data = patchHttpDialogData(dataList);
            //var url = '/hlb/group/prod/create';
            var url = '/service/' + appkey + '/provider/' + 2 + '/list';
            refreshHttpLineData(cpObj, url, successCallback, errorCallback);
            function successCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('success', "编辑成功", 3);
                editHttpRouteDialog.close();
                getRoutes(1);
            }

            function errorCallback(msg) {
                Y.msgp.utils.msgpHeaderTip('error', '编辑失败', 3);
            }
        }

        function displayThs(isThrift) {
            if (isThrift) {
                var ths = thead.all('th');
                ths.item(1).show();
                ths.item(2).show();
                ths.item(3).show();
                ths.item(4).show();
                ths.item(5).show();
                wrapper.one('#group_detail_statement').show();
                wrapper.one('#group_http_statement').hide();
            } else {
                var ths = thead.all('th');
                ths.item(1).hide();
                ths.item(2).hide();
                ths.item(3).hide();
                ths.item(4).hide();
                ths.item(5).hide();
                wrapper.one('#group_http_statement').show();
                wrapper.one('#group_detail_statement').hide();
            }
        }

        function getEnvStr() {
            switch (envType) {
                case 3:
                    return "prod";
                case 2:
                    return "stage";
                case 1:
                    return "test";
                default:
                    return "prod";
            }
        }

        /**
         * 添加IDC选择生产者的模板
         * "全选选择事件"
         * @param appkey
         */
        function getIdc(appkey, callback_function) {
            var url = '/service/provider/idclist';
            Y.io(url, {
                method: 'get',
                data: {
                    appkey: appkey,
                    type: 1
                },
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.isSuccess) {
                            var data = res.data;
                            callback_function(data)
                        }
                    }
                }
            });
        }

        function idc_provider_callback(data) {
            var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
            //设置provider的html
            var providerHtml = micro_Template.render(idcTemplateDetail, {
                data: data
            });
            body.one('#provider_idc_ul').append(providerHtml);
            //绑定全选，反选事件
            body.delegate('click', function () {
                body.one('#provider_idc_ul').all('li input').set("checked", true);
            }, '#provider-idc-all-check-node');

            body.delegate('click', function () {
                body.one('#provider_idc_ul').all('li input').each(function (nd) {
                    var isChecked = !this.get('checked');
                    nd.set("checked", isChecked);
                });
            }, '#provider-idc-all-uncheck-node');
        }


        function idc_consumer_callback(data) {
            var body = (null != addRouteDialog) ? addRouteDialog.getBody() : editRouteDialog.getBody();
            //设置provider的html
            var consumerHtml = micro_Template.render(idcTemplateDetail, {
                data: data
            });
            body.one('#consumer_idc_ul').append(consumerHtml);
            //绑定全选，反选事件
            body.delegate('click', function () {
                body.one('#consumer_idc_ul').all('li input').set("checked", true);
            }, '#consumer-idc-all-check-node');

            body.delegate('click', function () {
                body.one('#consumer_idc_ul').all('li input').each(function (nd) {
                    var isChecked = !this.get('checked');
                    nd.set("checked", isChecked);
                });
            }, '#consumer-idc-all-uncheck-node');
            getAllService(body);
        }


        function getRouteDetail(urlRouteDetailUrl, param_data, msg, callback) {
            showDialog(msg);
            Y.io(urlRouteDetailUrl, {
                method: 'get',
                data: param_data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (!ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                            return;
                        }
                        var data = ret.data;
                        callback(data)
                    },
                    failure: function (id, o) {
                        hideDialog();
                        Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                    }
                }
            });
        }



        function showDialog(msg) {
            dialog = dialog ? dialog : new Y.mt.widget.CommonDialog({
                width: 400,
                title: '加载数据',
                content: '<i class="fa fa-spinner fa-spin text-blue mr10"></i>' + msg,
                closeCallback: function () {
                    isOpen = false;
                }
            });
            dialog.show();
            isOpen = true;
        }

        function hideDialog() {
            dialog.close();
        }

        function ipTag() {
            this.ip = "";
            this.showText = "";
            this.isExist = false;
        }
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'mt-date',
            'w-base',
            'w-paginator',
            'node-event-simulate',
            'template',
            'transition',
            'msgp-utils/msgpHeaderTip',
            'msgp-utils/check',
            'msgp-utils/localEdit',
            'msgp-service/commonMap',
            'msgp-serviceopt/getConsumerProvider-version0.0.3',
            'msgp-utils/checkIP'
        ]
    }
)
;
