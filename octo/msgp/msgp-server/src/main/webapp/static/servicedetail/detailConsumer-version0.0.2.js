/* jshint indent : false */
M.add('msgp-servicedetail/detailConsumer-version0.0.2', function (Y) {
    Y.namespace('msgp.servicedetail').detailConsumer = detailConsumer;
    var inited = false;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_consumer'),
        tbody = wrapper.one('#consumer_content_body'),
        pbody = wrapper.one('#paginator_consumer');
    var colspan = 4;
    var appChart, hostChart;
    var templateStr = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr>',
        '<td><%= item.appkey %> <% if(item.appkey=="unknownService"){%><a href="https://123.sankuai.com/km/page/28354817" target="_blank"><i class="fa fa-question-circle"></i></a> <%}%></td>',
        '<td><% if(item.host=="external") { %> <%= item.host %> <% } else { %> <a href="http://ops.sankuai.com/srvset/?query=<%= item.host %>#!<%= item.tag %>/node" target="_blank"><%= item.host %></a> <% } %></td>',
        '<td><%= item.ip %></td>',
        '<td><%= item.count %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    function detailConsumer(key, f1, f2) {
        if (!inited) {
            appkey = key;
            showOverlay = f1;
            showContent = f2;
            bindGetAgain();
            Y.msgp.service.setEnvText('consumer_env_select');
        }
        initWidget();
        bindOutlineBtn();
        outlineDisplay();
        getConsumer();
        getTags();
    }

    function initWidget() {
        wrapper.one('.menu-special')._node.style.display = 'none';
        appChart = echarts.init(document.getElementById('consumerApp'));
        hostChart = echarts.init(document.getElementById('consumerHost'));
    }

    function bindGetAgain() {
        wrapper.delegate('click', function () {
            Y.all('#consumer_range_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getConsumer();
            outlineOpenProcess();
        }, '#consumer_range_select a');
        wrapper.delegate('click', function () {
            Y.all('#consumer_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getConsumer();
            outlineOpenProcess();
        }, "#consumer_env_select a");
        tbody.delegate('click', function () {
            getConsumer();
        }, '.get-again');
        Y.one("#query_btn").on("click", function () {
            outlineDisplay();
            getConsumer();
        });
    }

    function emptyOrError(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(html);
        pbody.empty();
        showContent(wrapper);
    }

    function getConsumer() {
        var env = Y.one('#consumer_env_select a.btn-primary').getAttribute('value');
        var range = Y.one('#consumer_range_select a.btn-primary').getAttribute('value');
        var remoteApp = Y.one("#remoteApp").get("value");
        var remoteHost = Y.one("#remoteHost").get("value");
        var opt = {
            appkey: Y.one("#apps_select").get("value"),
            env: env,
            range: range,
            remoteApp: remoteApp,
            remoteHost: remoteHost
        };
        Y.msgp.utils.urlAddParameters(opt);
        //showOverlay(wrapper);
        tbodyShowLoading();
        var url = '/service/consumer';
        Y.io(url, {
            method: 'get',
            data: {
                appkey: appkey,
                range: range,
                env: env,
                remoteApp: remoteApp,
                remoteHost: remoteHost
            },
            on: {
                start: function () {
                    Y.one("#query_btn").set('disabled', true);
                    Y.one("#query_btn").setStyle('background', '#ccc');
                },
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillConsumer(data);
                        } else if (data.length === 0) {
                            emptyOrError();
                        }
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                },
                complete: function () {
                    Y.one("#query_btn").set('disabled', false);
                    Y.one("#query_btn").setStyle('background', '#3fab99');
                }
            }
        });
    }

    function getTags() {
        var appkey = Y.one('#apps_select').get("value");
        var env = Y.one('#consumer_env_select a.btn-primary').getAttribute('value');
        var remoteApp = Y.one("#remoteApp").get("value");
        var remoteHost = Y.one("#remoteHost").get("value");
        var url = '/data/tags';
        Y.io(url, {
            method: 'get',
            data: {
                appkey: appkey,
                env: env,
                source: "server"
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (data.length !== 0) {
                            var remoteAppKeys = data.remoteAppKeys;
                            var remoteHosts = data.remoteHosts;
                            $("#remoteApp").autocomplete({
                                source: remoteAppKeys,
                                minLength: 0
                            });

                            $("#remoteHost").autocomplete({
                                source: remoteHosts,
                                minLength: 0
                            });
                        } else {
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

    function fillConsumer(arr) {
        var html = wrapData(arr);
        tbody.setHTML(html);
        showContent(wrapper);
    }

    function wrapData(arr) {
        var micro = new Y.Template();
        var str = micro.render(templateStr, {data: arr});
        return str;
    }


    function bindOutlineBtn() {
        wrapper.delegate('click', function (e) {
            if (this.one('span').hasClass('fa-angle-down')) {
                this.one('span').removeClass('fa-angle-down').addClass('fa-angle-up');
                wrapper.one('.menu-special')._node.style.display = 'block';
                outlineDisplay();
            }else{
                this.one('span').removeClass('fa-angle-up').addClass('fa-angle-down');
                //wrapper.one('#currentIdc').hide();
                //getSupplier(1);
                wrapper.one('.menu-special')._node.style.display = 'none';
            }
        }, '.overview-btn');
    }

    function outlineDisplay() {
        appChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });
        hostChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });

        var appkey = Y.one('#apps_select').get("value");
        var env = Y.one('#consumer_env_select a.btn-primary').getAttribute('value');
        var range = Y.one('#consumer_range_select a.btn-primary').getAttribute('value');
        var url = '/service/consumer/outline';
        Y.io(url, {
            method: 'get',
            data: {
                appkey: appkey,
                env: env,
                range: range
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (data.appList.length !== 0 || data.hostCount.length !== 0) {
                            wrapper.one('.overview-btn').one('span').removeClass('fa-angle-down').addClass('fa-angle-up');
                            wrapper.one('.menu-special').show();
                            appChart.clear();
                            hostChart.clear();
                            appChart.setOption(getOption(data.appList, data.appCount, '服务'));
                            hostChart.setOption(getOption(data.hostList, data.hostCount, '主机'));
                            appChart.hideLoading();
                            hostChart.hideLoading();
                        } else {
                            wrapper.one('.overview-btn').one('span').removeClass('fa-angle-up').addClass('fa-angle-down');
                            wrapper.one('.menu-special').hide();
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

    function getOption(nameList, countList, type) {
        var seriesData = [];
        var length = nameList.length;
        var show = length <= 40;
        for (var i = 0; i < length; i++) {
            seriesData.push({
                value: countList[i],
                name:nameList[i]
            })
        }

        return  {
            title : {
                text: type + '调用次数',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} : {c} ({d}%)"
            },
            legend: {
                show: false,
                x : 'left',
                data: nameList
            },
            toolbox: {
                show : false,
                feature : {
                    mark : {show: true},
                    dataView : {show: true, readOnly: false},
                    magicType : {
                        show: true,
                        type: ['pie', 'funnel'],
                        option: {
                            funnel: {
                                x: '25%',
                                width: '50%',
                                funnelAlign: 'left',
                                max: 1548
                            }
                        }
                    },
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            calculable : true,
            series : [
                {
                    name:'调用次数',
                    type:'pie',
                    radius : '55%',
                    selectedMode :true | 'single',
                    center: ['50%', '60%'],
                    data: seriesData,
                    itemStyle : {
                        normal : {
                            label : {
                                show : show
                            },
                            labelLine : {
                                show : show
                            }
                        },
                        emphasis : {
                            label : {
                                show : true
                            },
                            labelLine : {
                                show : true
                            }
                        }
                    }
                }
            ]
        };
    }

    function outlineOpenProcess(){
        if(wrapper.one('.overview-btn').one('span').hasClass('fa-angle-up')){
            outlineDisplay();
        }
    }

    function tbodyShowLoading() {
        var loadingHtml = '<tr><td colspan="4" style="text-align: center;"><div class="content-overlay" style="text-align: center">' +
            '<i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>' +
            '</div></td></tr>';
        tbody.setHTML(loadingHtml);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        "node-event-simulate",
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap',
        'msgp-utils/common'
    ]
});
