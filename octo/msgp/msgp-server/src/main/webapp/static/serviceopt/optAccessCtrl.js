M.add('msgp-serviceopt/optAccessCtrl', function (Y) {
    Y.namespace('msgp.serviceopt').optAccessCtrl = detailAccessCtrl;
    var appkey,
        wrapper = Y.one('#wrap_accessCtrl');
    var consumer_wrap = wrapper.one('#access_consumer_wrap');
    var registry_wrap = wrapper.one('#access_registry_wrap');
    var access_ctrl_title = Y.one('#access_ctrl_title');

    var currentPage = -1, currentEnv = 3;
    var colspan = 5;

    var currentAjax, currentAccessAjax;
    var currentRegistryIPs = null, currentAccessIPs = null;

    Array.prototype.distinct = function () {
        if (this.length <= 0)return;
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
    var whileListTemplate = [
        '<table style="width:100%;">',
        '<colgroup>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '<col width="12.5%"></col>',
        '</colgroup>',
        '<% var nullItemNum=0;Y.Array.each(this.data, function(subList,index){ %>',
        '<tr>',
        '<% nullItemNum=0; Y.Array.each(subList, function(item, z){ %>',
        '<%if(null==item||""==item){++nullItemNum;}else{%>',
        '<td><input id="one_ipcheck" <%if(item.tag){%>checked<%}%> type="checkbox" style="margin-bottom: 3px;" value="<%=item.ip%>"/>&nbsp;<%=item.ip%></td>',
        '<% }}); %>',
        '<% for(var i=0;i < 5 - subList.length-nullItemNum; ++i){%>',
        '<td id="fill_up_ip"></td>',
        '<% } %>',
        '</tr>',

        '<% }); %>',
        '</table>'
    ].join('');

    var registryCheckIPs, consumerCheckIPs;

    function detailAccessCtrl(key) {
        appkey = key;
        bindSelect();
        wrapper.one('#access_page')._node.click();
        initCheckIPs();
        bindSave();
        registry_wrap.one('#registry_add_ip_btn').on('click', clickRegistryAddIP);
        consumer_wrap.one('#access_add_ip_btn').on('click', clickAccessAddIP);
        Y.msgp.service.setEnvText('env_select');
    }

    function bindSave(){
        wrapper.delegate('click',function(){
            switch (currentPage) {
                case 0:
                    clickAccessSaveBtn();
                    break;
                case 1:
                    clickRegistrySaveBtn();
                    break;
            }
        },'#btn_save');
    }

    function initCheckIPs() {
        var registryTextArea = registry_wrap.one('#registry_add_ip');
        registryCheckIPs = Y.msgp.utils.check.init(registryTextArea, {
            chineseOk: false,
            spaceOk: true,
            emptyOk: false,
            callback: checkRegistryAddIP,
            warnElement: registry_wrap.one('#manual_add_ip_tips')
        });
        var consumerTextArea = consumer_wrap.one('#access_add_ip');
        consumerCheckIPs = Y.msgp.utils.check.init(consumerTextArea, {
            chineseOk: false,
            spaceOk: true,
            emptyOk: false,
            callback: checkAccessAddIP,
            warnElement: consumer_wrap.one('#manual_add_ip_tips')
        });
    }

    function clickAccessSaveBtn() {

        var status = consumer_wrap.one('#status_one_radio:checked');
        var status_value = Number(status.get('value'));
        var ipSelected = consumer_wrap.all('#one_ipcheck:checked');
        var ips = new Array();
        ipSelected.each(function (item) {
            ips.push(item.get('value'));
        });
        var url = '/service/accessctrl/' + appkey + '/saveData/' + currentEnv + '/consumer';
        var data = {
            status: status_value,
            ips: ips
        };
        Y.io(url, {
            method: 'POST',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        getAccessData();
                        Y.msgp.utils.msgpHeaderTip('success', '保存成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);

                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 3);
                }
            }
        });
    }

    function clickAccessAddIP() {
        if (!consumerCheckIPs.isValid()) {
            consumerCheckIPs.showMsg();
            return;
        }
        var textArea = consumer_wrap.one('#access_add_ip');
        var ips = textArea.get('value').split('\n');
        var ips_arr = new Array();

        Y.Array.each(ips, function (item) {
            var ip = Y.Lang.trim(item);
            if (ip.length > 0) {
                ips_arr.push(ip);
            }
        });
        ips_arr.distinct();

        if (null == currentAccessIPs) {
            Y.msgp.utils.msgpHeaderTip('info', "正在加载中...，请稍等", 3);
            return;
        }

        var ips_result = new Array();
        var isExist = false;
        Y.Array.each(ips_arr, function (x) {
            isExist = false;
            for (var i = 0; i < currentAccessIPs.length; ++i) {
                if (currentAccessIPs[i].ip == x) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                ips_result.push(x);
            }
        });
        handleAccessAddIPs(ips_result)
        textArea.set('value', '');
    }

    function checkAccessAddIP() {
        var textArea = consumer_wrap.one('#access_add_ip');
        var ips = textArea.get('value').split('\n');

        Y.Array.each(ips, function (item, index) {
            var ip = Y.Lang.trim(item);
            var rowNum = index + 1;

            if ((ip.length > 0) && (!checkIP(ip))) {
                _setMsg(consumerCheckIPs, "第" + rowNum + "行的IP地址[" + ip + "]非法", false);
            }

        });
    }

    function handleAccessAddIPs(ips) {
        if (ips.length <= 0)return;
        var item_temp;
        Y.Array.each(ips, function (item) {
            item_temp = new IPTag();
            item_temp.ip = item;
            item_temp.tag = true;
            currentAccessIPs.push(item_temp);
        });
        var micro = new Y.Template();
        var html = micro.render(whileListTemplate, {data: handleIPs(currentAccessIPs)});
        consumer_wrap.one('#add_white').setHTML(html);
    }

    function clickRegistryAddIP() {
        if (!registryCheckIPs.isValid()) {
            registryCheckIPs.showMsg();
            return;
        }
        var textArea = registry_wrap.one('#registry_add_ip');
        var ips = textArea.get('value').split('\n');
        var ips_arr = new Array();

        Y.Array.each(ips, function (item) {
            var ip = Y.Lang.trim(item);
            if (ip.length > 0) {
                ips_arr.push(ip);
            }
        });
        ips_arr.distinct();

        if (null == currentRegistryIPs) {
            Y.msgp.utils.msgpHeaderTip('info', "正在加载中...，请稍等", 3);
            return;
        }

        var ips_result = new Array();
        var isExist = false;
        Y.Array.each(ips_arr, function (x) {
            isExist = false;
            for (var i = 0; i < currentRegistryIPs.length; ++i) {
                if (currentRegistryIPs[i].ip == x) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                ips_result.push(x);
            }
        });
        handleRegistryAddIPs(ips_result)
        textArea.set('value', '');
    }

    function handleRegistryAddIPs(ips) {
        if (ips.length <= 0)return;
        var item_temp;
        Y.Array.each(ips, function (item) {
            item_temp = new IPTag();
            item_temp.ip = item;
            item_temp.tag = true;
            currentRegistryIPs.push(item_temp);
        });
        var micro = new Y.Template();
        var html = micro.render(whileListTemplate, {data: handleIPs(currentRegistryIPs)});
        registry_wrap.one('#add_white').setHTML(html);
    }

    function checkRegistryAddIP() {
        var textArea = registry_wrap.one('#registry_add_ip');
        var ips = textArea.get('value').split('\n');

        Y.Array.each(ips, function (item, index) {
            var ip = Y.Lang.trim(item);
            var rowNum = index + 1;

            if ((ip.length > 0) && (!checkIP(ip))) {
                _setMsg(registryCheckIPs, "第" + rowNum + "行的IP地址[" + ip + "]非法", false);
            }

        });
    }

    function _setMsg(element, msg, type) {
        element._setStatus(type);
        element.opt.warnElement.setHTML(msg).setStyle('color', '#f00');
    }

    function checkIP(ip) {
        var exp = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
        return ip.match(exp);
    }

    function clickRegistrySaveBtn() {
        var status = registry_wrap.one('#status_one_radio:checked');
        var status_value = Number(status.get('value'));
        var ipSelected = registry_wrap.all('#one_ipcheck:checked');
        var ips = new Array();
        ipSelected.each(function (item) {
            ips.push(item.get('value'));
        });
        var url = '/service/accessctrl/' + appkey + '/saveData/' + currentEnv + '/provider';
        var data = {
            status: status_value,
            ips: ips
        };
        Y.io(url, {
            method: 'POST',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        getData();
                        Y.msgp.utils.msgpHeaderTip('success', '保存成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);

                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 3);
                }
            }
        });
    }


    function handleIPs(data) {
        var row_num = data.length / colspan;
        var ret = new Array();
        for (var j = 0; j < row_num; ++j) {
            ret.push(data.slice(colspan * j, colspan * j + colspan));
        }
        return ret;
    }

    function IPTag() {
        this.ip = "";
        this.tag = false;
    }

    function fillRegistryWhiteList(providerData, registryData) {
        var dataList = new Array();
        var item_temp = null;
        Y.Array.each(providerData, function (x) {
            item_temp = new IPTag();
            item_temp.ip = x;
            Y.Array.each(registryData.ips, function (y) {
                if (x == y) {
                    item_temp.tag = true;
                }
            });
            dataList.push(item_temp);
        });

        var isExist = false;
        Y.Array.each(registryData.ips, function (x) {
            isExist = false;
            Y.Array.each(providerData, function (y) {
                if (x == y) {
                    isExist = true;
                }
            });
            if (!isExist) {
                item_temp = new IPTag();
                item_temp.ip = x;
                item_temp.tag = true;
                dataList.push(item_temp);
            }
        });
        currentRegistryIPs = dataList;
        registryShowContent('data', dataList, registryData);
    }


    function bindSelect() {
        wrapper.delegate('click', function () {
            var tempPage = Number(this._node.getAttribute('value'));
            currentPage = tempPage;

            var parent = this.ancestor('div');
            parent.all('a').removeClass('btn-primary');
            this.addClass('btn-primary');
            switch(currentEnv){
                case 1:
                    wrapper.one('#access_test')._node.click();
                    break;
                case 2:
                    wrapper.one('#access_stage')._node.click();
                    break;
                case 3:
                    wrapper.one('#access_prod')._node.click();
                    break;
            }


            switch (currentPage) {
                case 0:
                    registry_wrap.hide();
                    consumer_wrap.show();
                    accessShowContent('loading');
                    getAccessData();
                    break;
                case 1:
                    consumer_wrap.hide();
                    registry_wrap.show();
                    registryShowContent('loading');
                    getData();
                    break;

            }
        }, '.btn-access-page');
        wrapper.delegate('click', function () {
            var tempEnv = Number(this._node.getAttribute('value'));
            if (tempEnv == currentEnv)return;
            currentEnv = tempEnv;
            var parent = this.ancestor('div');
            parent.all('a').removeClass('btn-primary');
            this.addClass('btn-primary');
            switch (currentPage) {
                case 0:
                    accessShowContent('loading');
                    getAccessData();
                    break;
                case 1:
                    registryShowContent('loading');
                    getData();
                    break;
            }

        }, '.btn-access-env');

        registry_wrap.delegate('click', function () {
            registry_wrap.all('#status_one_radio').set("checked", false);
            this.set("checked", true);
        }, '#status_one_radio');

        consumer_wrap.delegate('click', function () {
            consumer_wrap.all('#status_one_radio').set("checked", false);
            this.set("checked", true);
        }, '#status_one_radio');
    }

    function loading(wrap) {
        wrap.one('#user_content').hide();
        wrap.one('#user_overlay').show();
        wrap.one('#status_content').hide();
        wrap.one('#status_overlay').show();
        wrap.one('#white_overlay').show();
        var add_white_table = wrap.one('#add_white table');
        if (null != add_white_table) {
            add_white_table.remove();
        }
        var add_white_span = wrap.one('#add_white span');
        if (null != add_white_span)
            add_white_span.remove();
    }

    function setData(wrap, data) {
        wrap.one('#user_overlay').hide();
        wrap.one('#status_overlay').hide();
        if ("" == data) {
            wrap.one('#user_content_text').set('text', "无");
        } else {
            wrap.one('#user_content_text').set('text', data.user+" "+Y.mt.date.formatDateByString(new Date(data.updateTime * 1000), "yyyy-MM-dd hh:mm:ss"));
            var status_el = wrap.one('#status_one_radio');
            status_el = (0 == Number(data.status)) ? status_el : status_el.next();
            status_el._node.click();
            wrap.one('#status_one_radio')
        }
        wrap.one('#status_content').show();
        wrap.one('#user_content').show();
    }

    function accessShowContent(type, data) {
        switch (type) {
            case 'loading':
                loading(consumer_wrap);
                break;
            case 'data':

                setData(consumer_wrap, data);
                var micro = new Y.Template();
                var dataList = new Array();
                var ip_temp;
                Y.Array.each(data.ips, function (item) {
                    ip_temp = new IPTag();
                    ip_temp.ip = item;
                    ip_temp.tag = true;
                    dataList.push(ip_temp);
                });
                currentAccessIPs = dataList;
                consumer_wrap.one('#white_overlay').hide();
                var html = micro.render(whileListTemplate, {data: handleIPs(dataList)});
                consumer_wrap.one('#add_white').setHTML(html);
                break;
            case 'error':
                var html = '<span>获取失败</span>';
                consumer_wrap.one('#add_white').setHTML(html);
                break;
        }
    }

    function registryShowContent(type, data, registryData) {
        switch (type) {
            case 'loading':
                loading(registry_wrap);
                break;
            case 'data':
                setData(registry_wrap, registryData);
                var micro = new Y.Template();
                registry_wrap.one('#white_overlay').hide();
                var html = micro.render(whileListTemplate, {data: handleIPs(data)});
                registry_wrap.one('#add_white').setHTML(html);

                break;
            case 'error':
                var html = '<span>获取失败</span>';
                registry_wrap.one('#add_white').setHTML(html);
                break;
        }
    }

    function getAccessData() {
        currentAccessIPs = null;
        if (typeof(currentAccessAjax) != "undefined") {
            currentAccessAjax.abort();
        }
        var url = '/service/accessctrl/' + appkey + '/accessData';
        currentAccessAjax = Y.io(url, {
            method: 'GET',
            data: {
                env: currentEnv,
                type: currentPage
            },
            on: {
                success: function (id, o) {
                    var ret = JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        accessShowContent('data', data)
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }
                },
                failure: function (id, o) {
                    if ('abort' != o.statusText) {
                        Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 3);
                    }
                }
            }
        });
    }


    function getData() {
        currentRegistryIPs = null;
        var url = '/service/accessctrl/' + appkey + '/provider/all';
        if (typeof(currentAjax) != "undefined") {
            currentAjax.abort();
        }
        currentAjax = Y.io(url, {
            method: 'GET',
            data: {
                env: currentEnv
            },
            on: {
                success: function (id, o) {
                    var ret = JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var provderData = ret.data;
                        url = '/service/accessctrl/' + appkey + '/accessData';
                        Y.io(url, {
                            method: 'GET',
                            data: {
                                env: currentEnv,
                                type: currentPage
                            },
                            on: {
                                success: function (id, o) {
                                    var ret_x = JSON.parse(o.responseText);
                                    if (ret_x.isSuccess) {
                                        var x_data = ret_x.data;
                                        fillRegistryWhiteList(provderData, x_data);
                                    } else {
                                        Y.msgp.utils.msgpHeaderTip('error', ret_x.msg, 3);
                                    }
                                },
                                failure: function (id, o) {
                                    registryShowContent('error');
                                }
                            }
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }

                },
                failure: function (id, o) {
                    if ('abort' != o.statusText) {
                        Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 3);
                    }
                }
            }
        });
    }
}, '0.0.1', {
    required: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-service/commonMap'
    ]
});