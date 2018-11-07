M.add('msgp-servicerep/server', function(Y){
    Y.namespace('msgp.servicerep').server = server;
    var tbody = Y.one('#server_wrap tbody'),
        colspan = 3;
    function server(owt,day){
        initTable(owt,day)
    }
    function initTable(owt,day) {
        setLoading();
        var url = '/repservice/server';
        Y.io(url, {
            method: 'get',
            data: {owt: owt,day:day},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillTableContent(data);
                        } else if (data.length === 0) {
                            setEmpty();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                        setError();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                    setError();
                }
            }
        });
    }

    function fillTableContent(data) {
        var html = '';
        for (var i = 0, l = data.length; i < l; i++) {
            var d = data[i];
            var intro = d.intro.length>10?d.intro.substr(0,7)+"..." :d.intro;
            html += ['<tr>',
                "<td><a target='_blank' href='/data/tabNav?appkey=" + d.appkey +"&type=source#source'>" +d.appkey +"</a></td>",
                '<td>' + intro + '</td>',
                '<td>' + d.count + '</td>',
                '</tr>'].join('');
        }
        tbody.setHTML(html);

    }
    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
    }

    function setError() {
        var errorHtml = '<tr><td colspan="' + colspan + '">获取失败</td></tr>';
        tbody.setHTML(errorHtml);
    }

    function setEmpty() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">没有内容</td></tr>';
        tbody.setHTML(emptyHtml);
    }

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-base'
    ]
});