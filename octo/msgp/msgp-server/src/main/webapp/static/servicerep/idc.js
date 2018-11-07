M.add('msgp-servicerep/idc', function (Y) {
        Y.namespace('msgp.servicerep').idc = idc;
        var tbody = Y.one('#idc_wrap tbody'),
            colspan = 8;

        function idc(owt, day) {
            initTable(owt, day)
        }

        function initTable(owt, day) {
            setLoading();
            var url = '/repservice/idc';
            Y.io(url, {
                method: 'get',
                data: {owt: owt, day: day},
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            if (Y.Lang.isObject(data) && data.length !== 0) {
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
            var idcHead = data.idcHead;
            var headhtml = "<th>appkey</th><th>服务描述</th><th>调用量</th>";
            var str_idcHead = idcHead.join(":");
            headhtml += '<th>' + str_idcHead + ' 调用量</th><th>' + str_idcHead + ' 主机数</th>';
            headhtml += '<th>' + str_idcHead + ' 调用量比例</th><th>' + str_idcHead + ' 主机比例</th>';
            Y.one('#idc_wrap thead').setHTML(headhtml);
            //set Content
            var idcDatas = data.idcTraffics;
            var dataHtml = "";
            for (var i = 0, l = idcDatas.length; i < l; i++) {
                var idcData = idcDatas[i];
                var dataTr = [];
                dataTr.push("<a target='_blank' href='/data/tabNav?appkey=" + idcData.appkey + "&type=dashboard#dashboard'>" + idcData.appkey + "</a>");
                var intro = idcData.intro.length > 10 ? idcData.intro.substr(0, 7) + "..." : idcData.intro;
                dataTr.push(intro);
                dataTr.push(parseFloat(idcData.totalCount).toLocaleString());
                var totalCount = idcData.totalCount;
                var hostCount = 0;
                var tdIdcCount = [];
                var tdSidcCount = [];
                var tdIdcHostCount = [];
                Y.Array.each(idcData.simpleIdcCounts, function (item, index) {
                    var count = parseFloat(item.count).toLocaleString();
                    var sCount = getSimpleIdcCount(item.count);
                    var hCount = parseFloat(item.hostCount).toLocaleString();
                    tdIdcCount.push(count);
                    tdSidcCount.push(sCount);
                    tdIdcHostCount.push(hCount);
                    hostCount += parseInt(item.hostCount);
                });

                var htmlIdcCount = '<span title="' + tdIdcCount.join(" : ") + '">' + tdSidcCount.join(" : ") + '</span>';
                dataTr.push(htmlIdcCount);
                dataTr.push(tdIdcHostCount.join(" : "));
                var hostK = [];
                var countK = [];
                var avgQps = totalCount / hostCount;
                var countBad = false;
                if (totalCount == 0) {
                    totalCount = 1;
                }
                if (hostCount == 0) {
                    hostCount = 1;
                }
                Y.Array.each(idcData.simpleIdcCounts, function (item, index) {
                    var hk = Math.round(item.count / totalCount * 100);
                    var ck = Math.round(item.hostCount / hostCount * 100);
                    hostK.push(hk);
                    countK.push(ck);
                    if (item.hostCount == 0) {
                        item.hostCount = 1;
                    }
                    var avgIdcQps = item.count / item.hostCount;
                    var k = avgIdcQps / avgQps;
                    if (k > 2 || (k > 0 && 1 / k > 2)) {
                        countBad = true;
                    }
                });
                hostK = hostK.join(" : ");
                countK = countK.join(" : ");
                if (countBad) {
                    hostK = "<span style='color: red;'>" + hostK + "</span>";
                    countK = "<span style='color: red;'>" + countK + "</span>"
                }
                dataTr.push(hostK);
                dataTr.push(countK);
                dataHtml += "<tr><td>" + dataTr.join('</td><td>') + "</td></tr>"
            }
            tbody.setHTML(dataHtml)
        }

        function getSimpleIdcCount(idcCount) {
            var count = idcCount;
            if (idcCount > 100000000) {//亿
                count = parseInt(idcCount / 100000000) + "亿+"
            } else if (idcCount > 10000) {//万
                count = parseInt(idcCount / 10000) + "万+"
            }
            return count;
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

    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base'
        ]
    }
)
;