M.add('msgp-serviceopt/getConsumerProvider-version0.0.3', function (Y) {
    Y.namespace('msgp.serviceopt').getConsumerProvider = getConsumerProvider;
    Y.namespace('msgp.serviceopt').getHttpConsumerProvider = getHttpConsumerProvider;
    Y.namespace('msgp.serviceopt').getRouteDetail = getRouteDetail;
    var dialog;
    var isOpen;

    function getConsumerProvider(url, data, msg, callback) {
        showDialog(msg);
        //var url = '/service/' + appkey + '/group/attributes';
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    if (!isOpen) return;
                    hideDialog();
                    var ret = Y.JSON.parse(o.responseText);
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


    function ipTag() {
        this.ip = "";
        this.isExist = false;
    }

    function getHttpConsumerProvider(url, data, msg, callback) {
        showDialog(msg);
        //var url = '/service/' + appkey + '/group/attributes';
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    if (!isOpen) return;
                    hideDialog();
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data
                    var providerArr = [];
                    for (var i = 0, l = data.length; i < l; i++) {
                        if (!data[i]['groupInfo'] || data[i]['groupInfo'] == "") {
                            providerArr.push(data[i]['ip'] + ':' + data[i]['port']);
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


    function getRouteDetail(urlRouteDetailUrl, providersUrl, data1, data2, msg, callback) {
        showDialog(msg);
        Y.io(urlRouteDetailUrl, {
            method: 'get',
            data: data1,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (!ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                        return;
                    }
                    // var data = fillHttpRoutes(ret.data,data1.group_name);
                    var data = ret.data;
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

                                if(!data.provider) {
                                    //console.log(data)
                                    //data = Y.JSON.parse(data);
                                    var providerArr = [];
                                    var tempArray1 = new Array();
                                    for (var i = 0, l = data.length; i < l; i++){

                                         if(data[i]['groupInfo'] == data1.group_name)
                                        {
                                             providerArr.push(data[i]['ip'] + ':' +data[i]['port']);
                                            var tmp = new ipTag();
                                            tmp.ip = data[i]['ip'] + ':' + data[i]['port']
                                            tmp.isExist = true;
                                            tempArray1.push(tmp);
                                         }
                                    }
                                    // data.server.forEach(function(item) {
                                    //     providerArr.push(item.ip + ':' +item.port);
                                    // });
                                    data.provider = providerArr;
                                }

                                var tempArray = new Array();

                                for (var i = 0, l = data.length; i < l; i++) {
                                    if (!data[i]['groupInfo'] || data[i]['groupInfo'] == "") {
                                        var tmp = new ipTag();
                                        tmp.ip = data[i]['ip'] + ':' + data[i]['port']
                                        tmp.isExist = false;
                                        tempArray1.push(tmp);
                                    }
                                }


                                data.providerIPTags = tempArray1;
                                callback(data);
                            },
                            failure: function (id, o) {
                                Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                            }
                        }
                    });
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


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'msgp-utils/msgpHeaderTip'
    ]
});
