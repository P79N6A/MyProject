M.add('msgp-servicerep/topqps', function (Y) {
    Y.namespace('msgp.servicerep').topqps = topqps;
    var tbody = Y.one('#topqps_wrap tbody'),
        colspan = 9;

    function topqps(owt, day) {
        getHead(day)
        initTable(owt, day)
    }

    function getHead(day) {
        var html = "<th>appkey</th><th>服务描述</th><th>";
        var days = getWeek(day);
        html += days.join('</th><th>');
        html += '</th>';
        Y.one('#topqps_wrap thead').setHTML(html)
    }

    function getWeek(day) {
        var url = '/repservice/week';
        var request = Y.io(url, {
            method: 'get',
            sync: true,
            data: {day: day}

        });
        var days = Array();
        var ret = Y.JSON.parse(request.responseText);
        if (ret.isSuccess) {
            var data = ret.data;
            if (Y.Lang.isArray(data) && data.length !== 0) {
                days = data;
            }
        }
        return days;
    }

    function initTable(owt, day) {
        setLoading();
        var url = '/repservice/qps';
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
            var appkeyDatas = data[i];
            var appkey = appkeyDatas[0].appkey
            var tdDatas = Array();
            tdDatas.push("<a target='_blank' href='/data/tabNav?appkey=" +appkey +"'>" +appkey +"</a>");
            var intro = appkeyDatas[0].intro.length>10?appkeyDatas[0].intro.substr(0,7)+"..." :appkeyDatas[0].intro;
            tdDatas.push(intro)
            for (var j = 0, al = appkeyDatas.length; j < al; j++) {
                var qps = appkeyDatas[j].qps;
                if(qps.length>0){
                    qps = parseFloat(Math.round(qps)).toLocaleString()
                }else{
                    qps="N/A"
                }
                tdDatas.push(qps)
            }
            html += "<tr><td>" + tdDatas.join("</td><td>") + "</td></tr>"
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
    requires: [
        'mt-base',
        'mt-io',
        'w-base'
    ]
});