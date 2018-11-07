M.add('msgp-servicerep/error', function(Y){
    Y.namespace('msgp.servicerep').error = error;
    var tbody = Y.one('#error_wrap tbody'),
        colspan = 5;
    function error(owt,day){
        initTable(owt,day)
    }
    function initTable(owt,day) {
        setLoading();
        var url = '/repservice/error';
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
            var count = parseFloat(d.count).toLocaleString()
            var errorCount = parseFloat(d.errorCount).toLocaleString()
            var intro = d.intro.length>10?d.intro.substr(0,7)+"..." :d.intro;
            html += ['<tr>',
                "<td><a target='_blank' href='/log/report?appkey=" + d.appkey +"'>" +d.appkey +"</a></td>",
                '<td>' + intro + '</td>',
                '<td>' + count + '</td>',
                '<td>' + errorCount + '</td>',
                '<td>' + Math.round(d.ratio) + '</td>',
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