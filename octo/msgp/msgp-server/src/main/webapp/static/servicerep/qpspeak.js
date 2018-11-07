M.add('msgp-servicerep/qpspeak', function(Y){
    Y.namespace('msgp.servicerep').qpspeak = qpspeak;
    var tbody = Y.one('#qpspeak_wrap tbody'),
        colspan = 8;

    function qpspeak(owt, day) {
        initTable(owt, day)
    }

    function initTable(owt, day) {
        setLoading();
        var url = '/repservice/qpspeak';
        Y.io(url, {
            method: 'get',
            data: {owt: owt, day: day},
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
            var qpsPeak = data[i];
            var tdDatas = Array();
            tdDatas.push("<a  target='_blank' href='/data/tabNav?appkey=" +qpsPeak.appkey +"&type=dashboard#dashboard'>" +qpsPeak.appkey +"</a>");
            var intro = qpsPeak.intro.length>10?qpsPeak.intro.substr(0,7)+"..." :qpsPeak.intro;
            tdDatas.push(intro);
            tdDatas.push(Math.round(qpsPeak.avgQps));
            tdDatas.push(Math.round(qpsPeak.maxHourQps));
            tdDatas.push(Math.round(qpsPeak.minHourQps));
            tdDatas.push(Math.round(qpsPeak.avgHostQps));
            tdDatas.push(Math.round(qpsPeak.maxHourHostQps));
            html+=  "<tr><td>" + tdDatas.join("</td><td>") + "</td></tr>"
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