/**
 * Created by lhmily on 10/28/2015.
 */
M.add('msgp-manage/sgagent/addBatchProviders', function (Y) {
    Y.namespace('msgp.manage').addBatchProviders = addBatchProviders;
    var appkey;
    var wrapper = Y.one('#wrap_addBatchProviders');

    function addBatchProviders(key) {
        console.log("into batch");
        appkey = key;
        bindBatchProvider();
    }

    function bindBatchProvider(app) {
        appkey = app;
        wrapper.delegate('click', function () {
            var h3 = this.ancestor('h3');
            var input_env = Y.Lang.trim(h3.one('#input_env').get('value'));
            var host_name_prefix = Y.Lang.trim(h3.one('#host_name_prefix').get('value'));
            var input_port = Y.Lang.trim(h3.one('#input_port').get('value'));
            var start_num = Y.Lang.trim(h3.one('#start_num').get('value'));
            var end_num = Y.Lang.trim(h3.one('#end_num').get('value'));
            var url = '/manage/agent/' + appkey + '/provider/batch';
            var data = {
                appkey: appkey,
                version: "original",
                ip: "10",
                port: Number(input_port),
                weight: 10,
                status: 2,
                role: 0,
                env: Number(input_env),
                lastUpdateTime: ~~(new Date() / 1000),
                unixtime: ~~(new Date() / 1000),
                extend: '',
                prefix: host_name_prefix,
                startNum: Number(start_num),
                endNum: Number(end_num)
            };
            Y.io(url, {
                method: 'POST',
                headers : {'Content-Type':"application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', "成功添加:" + ret.data, 100);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 100);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                    }
                }
            });
        }, '#add_button');

        wrapper.delegate('click', function () {
            var h3 = this.ancestor('h3');
            var host_name_prefix = Y.Lang.trim(h3.one('#search_host_name_prefix').get('value'));
            var start_num = Y.Lang.trim(h3.one('#search_start_num').get('value'));
            var end_num = Y.Lang.trim(h3.one('#search_end_num').get('value'));
            var url = '/manage/agent/' + appkey + '/host/name2ip';
            var data = {
                prefix: host_name_prefix,
                startNum: Number(start_num),
                endNum: Number(end_num)
            };
            Y.io(url, {
                method: 'GET',
                data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var div = wrapper.one('#show_ips');
                            div.all('#one_ip').remove();
                            data.forEach(function (item) {
                                div.append('<div id="one_ip">' + item + '</div>');
                            });
                            console.log(data);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 100);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                    }
                }
            });
        }, '#search_button')
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'w-date',
        'mt-date',
        'transition',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/checkIP',
        'msgp-service/commonMap'
    ]
});