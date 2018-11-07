M.add('msgp-manage/check_thrift', function (Y) {
    Y.namespace('msgp.manage').check_thrift = check_thrift;
    var wrapper = Y.one('#wrap_thrift');
    var waiting_message = Y.one('#waiting_message_wrapper');
    var mtthrift_self_check_version = wrapper.one('#mtthrift_self_check_version').getDOMNode(),
        cthrift_self_check_version = wrapper.one('#cthrift_self_check_version').getDOMNode();

    String.prototype.trim=function(){
        return this.replace(/(^\s*)|(\s*$)/g, "");
    };

    String.prototype.startWith = function(str){
        if(str==null||str==""||this.length==0||str.length>this.length)
            return false;
        if(this.substr(0,str.length)==str)
            return true;
        else
            return false;
        return true;
    };

    function check_thrift(key, func1, func2) {
        bindWidget();
        versionCheck();
    }

    function bindWidget() {
        wrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "-1") {
                wrapper.one('#owt').empty();
                wrapper.one('#pdl').empty();
                wrapper.one('#owt').append('<option value=all>all</option>');
                wrapper.one('#pdl').append('<option value=all>all</option>');
                versionCheck();

            }else{
                getOwt(business);
            }
        });
        wrapper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                wrapper.one('#pdl').empty();
                wrapper.one('#pdl').append('<option value=all>all</option>');
                versionCheck();
            }else{
                getPdl(owt);
            }
        });

        wrapper.one('#pdl').on('change', function () {
            versionCheck();
        });
    }

    function getOwt(business) {
        wrapper.one('#owt').empty();
        wrapper.one('#owt').append('<option value=all>all</option>');
        wrapper.one('#pdl').empty();
        wrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/common/owt';
        Y.io(url, {
            method: 'get',
            data: {
                "business" : business
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            wrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        versionCheck();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                }
            }
        });
    }

    function getPdl(owt) {
        wrapper.one('#pdl').empty();
        wrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/common/pdl';
        Y.io(url, {
            method: 'get',
            data: {
                "owt" : owt
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            wrapper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        versionCheck();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                }
            }
        });
    }

    function versionCheck() {
        showWaitMsg(waiting_message);
        var business = wrapper.one("#business").get('value');
        var owt = wrapper.one("#owt").get('value') == "all"  ? "": wrapper.one("#owt").get('value');
        var pdl = wrapper.one("#pdl").get('value') == "all"  ? "": wrapper.one("#pdl").get('value');
        var url = '/manage/thriftSelfCheck';
        Y.io(url, {
            method: 'get',
            data: {
                business: business,
                owt: owt,
                pdl: pdl
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillCharts(ret.data);
                        clearWaitMsg(waiting_message);
                    }
                }
            }
        });
    }

    function fillCharts(data) {
        data.forEach(function (item) {
           var thriftType = item.thriftType;
           var pieData  = item.pieData;
           var obj;
           switch(thriftType) {
               case "mtthrift" :
                   obj = mtthrift_self_check_version;
                   break;
               case "cthrift" :
                   obj = cthrift_self_check_version;
                   break;
           }
           var title = thriftType + "版本分布";
           fillPie(pieData, obj, title);
        });
    }

    function fillPie(data,node,title) {
        var totalNode = 0;
        data.series.forEach(function (item) {
            totalNode += item.value
        });
        var option = {
            title: {
                text: title + ' [使用节点数: ' + totalNode + ']',
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
            legend: {
                orient: 'vertical',
                x: 'left',
                show:false,
                data: data.legend
            },
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
                    center: ['50%', '45%'],
                    data: data.series
                }
            ]
        };
        var myEchart = echarts.init(node);
        myEchart.setOption(option);
        //绑定点击饼图点击事件
        function eConsole(param) {
            var type=param.name.trim().startWith("cthrift")?"cthrift":"mtthrift";
            var url = '/manage/octoProvide?envId=0&version=' + param.name + '&thrifttype='+type;
            window.open(url, "_blank")
        }
        myEchart.on("click", eConsole);
    }

    function showWaitMsg(node) {
        var html = '<div id = "waiting_message" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>'+
            '</div>';
        node.append(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
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