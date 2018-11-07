M.add('msgp-manage/check_sgAgent', function (Y) {
    Y.namespace('msgp.manage').check_sgAgent = check_sgAgent;
    var wrapper = Y.one('#wrap_sgAgent');
    var sg_agent_test_check_version = wrapper.one('#sg_agent_test_check_version').getDOMNode(),
        sg_agent_stage_check_version = wrapper.one('#sg_agent_stage_check_version').getDOMNode(),
        sg_agent_prod_check_version = wrapper.one('#sg_agent_prod_check_version').getDOMNode();
    var wrapper_sg_version_detail = wrapper.one("#wrapper_sg_version_detail");
    var wrapper_sg_version_outline = wrapper.one('#wrapper_sg_version_outline');
    var count = 0;

    var templateStr = [
        '<% Y.Array.each(this.data, function( item, index ){ %>',
        '<tr class="tr_machine_node">',
        '<td><%= item.name %></td>',
        '<td><%= item.ip %></td>',
        '<td><%= item.port %></td>',
        '<td><%= item.weight %></span></td>',
        '<td><%= item.status %></td>',
        '<td class="last-update-time"><%= Y.mt.date.formatDateByString( new Date(item.lastUpdateTime*1000), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');


    var processingDialog, isDialogOpen = false;

    function check_sgAgent(key, func1, func2) {
        initSelfCheck();
        bind();
        getMnsZkUrl();
    }

    function wrapData(arr) {
        var micro = new Y.Template();
        var str = micro.render(templateStr, {data: arr});
        return str;
    }

    function showDialog(msg) {
        processingDialog = processingDialog ? processingDialog : new Y.mt.widget.CommonDialog({
            id: 'del_service_processing_dialog',
            width: 400,
            content: '<i class="fa fa-spinner fa-spin text-blue mr10"></i>' + msg
        });
        processingDialog.show();
        wrapper.one('.overlay-mask-process').show();
        isDialogOpen = true;
    }

    function hideDialog() {
        processingDialog.close();
        wrapper.one('.overlay-mask-process').hide();
    }


    function initSelfCheck(region) {
        showDialog("正在获取数据");
        count = 0;
        disabledButton(region);
        //版本分布
        sgAgentCheck(sg_agent_prod_check_version, "prod版本分布", 3, region);
        sgAgentCheck(sg_agent_test_check_version, "test版本分布", 1, region);
        sgAgentCheck(sg_agent_stage_check_version, "stage版本分布", 2, region);

    }

    function sgAgentCheck(obj, title, envId, region) {
        var url = '/manage/com.sankuai.inf.sg_agent/sgAgentSelfCheck';
        Y.io(url, {
            method: 'get',
            data: {
                envId: envId,
                region: region,
                self: self
            },
            on: {
                success: function (id, o, self) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        count++;
                        fillPie(title, ret.data, obj, envId, region);
                        if (count === 3) {
                            enabledButton();
                            hideDialog();
                        }
                    }
                }
            }
        });
    }

    function fillPie(title, data, obj, envId, region) {
        var series = data.series;
        series.sort(function (a, b) {
            return a.value - b.value;
        });
        var option = {
            animation: false,
            title: {
                text: title,
                textStyle: {
                    fontSize: 14,
                    color: '#6a6a6a'
                },
                padding: [5, 5, 15, 5],
                x: 'center',
                y: 'bottom'
            },
            tooltip: {
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)"
            },
            //legend: {
            //    orient: 'vertical',
            //    x: 'left',
            //    data: data.legend
            //},
            toolbox: {
                show: true,
                feature: {
                    dataView: {show: true, readOnly: false},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            calculable: true,
            series: [
                {
                    type: 'pie',
                    radius: '55%',
                    center: ['50%', '55%'],
                    data: series
                }
            ]
        };
        var myEchart = echarts.init(obj);
        myEchart.setOption(option);
        //绑定点击饼图点击事件
        function eConsole(param) {
            region = (region === undefined) ? 'all' : region;

            // var url = '/manage/com.sankuai.inf.sg_agent/sgAgentProvide?envId=' + envId + '&version=' + param.name + "&thrifttype=mtthrift" + "&region=" + region;
            //window.open(url,"_blank")
            var url = '/manage/com.sankuai.inf.sg_agent/provideGroupByVersion';
            showContentPage('detail');
            showVersionDetail('loading');
            wrapper_sg_version_detail.one('#detail_version').set('text', param.name);
            Y.io(url, {
                method: 'GET',
                data: {
                    envId: envId,
                    version: param.name,
                    thrifttype: 'mtthrift',
                    region: region
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            console.log(ret.data.providerList);
                            var html = wrapData(ret.data.providerList);
                            wrapper_sg_version_detail.one('#machine_number').set('text',ret.data.providerList.length);
                            showVersionDetail('data',html);
                        } else {
                            wrapper_sg_version_detail.one("#content_overlay").set("text","服务器错误");
                            showVersionDetail('error');
                        }
                    },
                    failure: function (id, o) {
                        showVersionDetail('error');
                    }
                }
            });
        }

        myEchart.on("click", eConsole);
    }

    function showVersionDetail(type,data){
        var overlay = wrapper_sg_version_detail.one("#content_overlay");
        var error = wrapper_sg_version_detail.one("#content_error");
        var allMachine = wrapper_sg_version_detail.all(".tr_machine_node").remove();

        overlay.hide();
        error.hide();
        allMachine.hide();
        switch(type){
            case 'error':
                error.show();
                break;
            case 'loading':
                overlay.show();
                break;
            case 'data':
                wrapper_sg_version_detail.one("tbody").append(data);
                break;
        }
    }

    function showContentPage(type) {
        wrapper_sg_version_detail.hide();
        wrapper_sg_version_outline.hide();
        switch (type) {
            case 'detail':
                wrapper_sg_version_detail.show();
                break;
            case 'outline':
                wrapper_sg_version_outline.show();

        }
    }

    function bind() {
        wrapper.delegate('click', function () {
            if (this.hasClass('btn-primary')) {
                return;
            }
            var region = this.getAttribute("value");
            this.ancestor('div').all('button').removeClass('btn-primary');
            disabledButton(region);
            this._node.removeAttribute('disabled');
            this.addClass("btn-primary");
            initSelfCheck(region);
        }, '#sgAgent_version_region button');

        wrapper_sg_version_detail.delegate('click',function(){
            showContentPage('outline');
        },"#sg_version_detail");
    }

    function enabledButton() {
        var aArr = wrapper.one('#sgAgent_version_region').all('button')._nodes;
        Y.Array.each(aArr, function (node) {
            node.removeAttribute('disabled');
        });
    }

    function disabledButton(region) {
        var aArr = wrapper.one('#sgAgent_version_region').all('button')._nodes;
        Y.Array.each(aArr, function (node, index) {
            if ((region === undefined && index != 0) || region) {
                node.setAttribute('disabled', 'disabled');
            }
        });
    }

    function getMnsZkUrl() {
        var url = "/manage/falcon/mnszk";
        Y.io(url, {
            method: "GET",
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        wrapper.one('#mnszkfaclonurl').setAttribute("href", ret.data);
                    }
                }
            }
        });
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'transition',
        'mt-base'
    ]
});

