/**
 * Created by yves on 16/8/8.
 */

M.add('msgp-component/cmpt_version', function (Y) {
    var tab_style = "";
    var _echart = null;
    var limitNumber = 10;
    var groupIdCache = {};
    var groupId;
    var artifactIdCache= {};
    var artifactId;
    var version;
    var everPaged = false,
        totalPage,
        totalCount;
    var pbody;
    var isPartial = false;
    var versionWrapper;
    var versionListWrapper;
    var versionTipsWrapper;

    Y.namespace('msgp.component').cmpt_version = cmpt_version;
    function cmpt_version(_tab_style, _groupId, _artifactId) {
        document.title = '组件版本分布';
        tab_style = _tab_style;
        groupId = _groupId;
        artifactId = _artifactId;
        versionWrapper = Y.one("#div_cmpt_version");
        versionListWrapper = versionWrapper.one("#version_list_wrapper");
        versionTipsWrapper = versionWrapper.one("#cmpt_version_tips");
        //initDatePicker();
        initWidget();
        bindWidget();
        initCharts();
        setDefaulValue();
        refreshData();
    }

    function refreshData() {
        getChartData();
        getTableData(false, 1);
    }

    function setDefaulValue() {
        versionWrapper.one("#groupId" ).set("value", groupId);
        versionWrapper.one("#artifactId" ).set("value", artifactId);
    }

    function initWidget() {
        $("#groupId" ).autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in groupIdCache ){
                    response(groupIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/group_id", {
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    groupIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                groupId = ui.item.value;
            }
        });
        $("#artifactId" ).autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in artifactIdCache ){
                    response(artifactIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/artifact_id", {
                    groupId: groupId,
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    artifactIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                artifactId = ui.item.value;
            }
        });
        initCmpt();
        pbody = versionWrapper.one('#paginator_version_wrapper');
        everPaged = false;
    }

    function initCmpt() {
        versionWrapper.one('#cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            versionWrapper.one('#cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        versionWrapper.one('#cmpt').set('value',groupId+',' + artifactId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                }
            }
        });
        var value = groupId+',' + artifactId;
        $('#cmpt').val(value);
    }

    function bindWidget() {
        versionWrapper.one('#searchBtn').on('click', function () {
            refreshData();
        });

        versionWrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "-1") {
                versionWrapper.one('#owt').empty();
                versionWrapper.one('#pdl').empty();
                versionWrapper.one('#owt').append('<option value=all>all</option>');
                versionWrapper.one('#pdl').append('<option value=all>all</option>');
                refreshData();
            }else {
                getOwt(business);
            }
        });

        versionWrapper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                versionWrapper.one('#pdl').empty();
                versionWrapper.one('#pdl').append('<option value=all>all</option>');
                refreshData();
            }else{
                getPdl(owt);
            }
        });

        versionWrapper.one('#pdl').on('change', function () {
            refreshData();
        });
        
        versionWrapper.one('#cmpt').on('change', function () {
            var cmpt = this.get('value').split(",");
            groupId = cmpt[0];
            artifactId = cmpt[1];
            versionWrapper.one("#groupId" ).set("value", groupId);
            versionWrapper.one("#artifactId" ).set("value", artifactId);
            refreshData();
        });

        versionWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            refreshData();
        }, '#base_type');
    }

    function getOwt(business) {
        versionWrapper.one('#owt').empty();
        versionWrapper.one('#owt').append('<option value=all>all</option>');
        versionWrapper.one('#pdl').empty();
        versionWrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/component/owt';
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
                            versionWrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        refreshData();
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
        versionWrapper.one('#pdl').empty();
        versionWrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/component/pdl';
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
                            versionWrapper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        refreshData();
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

    function initCharts() {
        var node = versionWrapper.one("#cmpt_version_charts").getDOMNode();
        _echart = echarts.init(node);
    }


    function getTableData(isPartial, pageNo) {
        showWaitMsg(versionListWrapper);
        var base = versionWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = versionWrapper.one("#business").get('value');
        var owt = versionWrapper.one("#owt").get('value') == "all"  ? "": versionWrapper.one("#owt").get('value');
        var pdl = versionWrapper.one("#pdl").get('value') == "all"  ? "": versionWrapper.one("#pdl").get('value');
        var versionNew = (version == "all" || !isPartial) ? "": version;
        artifactId = Y.Lang.trim(versionWrapper.one("#artifactId").get('value'));
        groupId =  Y.Lang.trim(versionWrapper.one("#groupId").get('value'));
        
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId,
            "version": versionNew,
            "base": base,
            "business":business,
            "owt":owt,
            "pdl":pdl,
            "pageNo" : pageNo,
            "pageSize" : 15
        };
        var url = '/component/version_detail';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if(data.length == 0){
                            showEmptyErrorMsg(versionListWrapper, false);
                        }else {
                            fillTable(data);
                            Y.Array.each(data, function (item, index) {
                                $("#version_app_" + index).tooltip({
                                    html: true,
                                    title: "GroupId: " + item.appGroupId + "<br/>ArtifactId: " + item.appArtifactId,
                                    delay: {
                                        hide: 100
                                    },
                                    container: $("#version_app_" + index)
                                });
                            });
                            if( !everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount ) {
                                refreshPaginator(pbody, pobj);
                            }
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        showEmptyErrorMsg(versionListWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(versionListWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillTable(data) {
        var tableTitle = (version == "") ? "所有版本": "版本号: " + version;
        var template =
            '<table id="version_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th>事业群</th> ' +
            '<th>业务线</th> ' +
            '<th>产品线</th> ' +
            '<th>项目名</th> ' +
            '<th>Appkey</th> ' +
            '<th>版本号</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="width: 15%"><%=item.business %></td>' +
            '<td style="width: 12%"><%=item.owt %></td>' +
            '<td style="width: 12%"><%=item.pdl %></td>' +
            '<td style="width: 18%" id = "version_app_<%= index %>"><%=item.appArtifactId %></td>' +
            '<td style="width: 21%"><a href="http://octo.sankuai.com/service/detail?appkey=' + '<%=item.appkey %>#outline" target="_Blank"><%=item.appkey %></a></td>' +
            '<td style="width: 22%"><%=item.version %></td>' +
            '</tr>' +
            '<% }); %>'+
            '</tbody> ' +
            '</table>';

        var micro = new Y.Template();
        var str = micro.render(template, {data : data});
        versionListWrapper.setHTML("");
        versionListWrapper.setHTML(str);
        Y.mt.widget.init();
    }

    function getChartData() {
        _echart.showLoading({
            text: "loading"
        });
        var base = versionWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = versionWrapper.one("#business").get('value');
        var owt = versionWrapper.one("#owt").get('value') == "all"  ? "": versionWrapper.one("#owt").get('value');
        var pdl = versionWrapper.one("#pdl").get('value') == "all"  ? "": versionWrapper.one("#pdl").get('value');
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId,
            "base": base,
            "business":business,
            "owt":owt,
            "pdl":pdl
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/version_count';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.length == 0){
                            _echart.hideLoading();
                            _echart.clear();
                            showEmptyErrorMsg(versionTipsWrapper, false);
                        }else {
                            versionTipsWrapper.empty();
                            fillCharts(data);
                        }
                    } else {
                        _echart.clear();
                        _echart.hideLoading();
                        showEmptyErrorMsg(versionTipsWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    _echart.clear();
                    _echart.hideLoading();
                    showEmptyErrorMsg(versionTipsWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillCharts(data) {
        var series = [];
        var legendData = [];
        var total = 0;
        Y.Array.each(data,function(element){
            series.push({
                name: element.version,
                value: element.count
            });
            legendData.push(element.version);
            total += element.count;
        });
        var option = {
            title : {
                text: '组件版本分布 [使用总数: ' + total + ']',
                subtext: groupId + ', ' + artifactId,
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "<b>版本名称:<b> {b}<br/><b>使用数量:<b> {c}<br/><b>使用比例:<b> {d}%",
                padding: 15
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: false},
                    restore: {show: false},
                    saveAsImage: {show: true}
                }
            },
            legend: {
                orient: 'vertical',
                x: 'left',
                show: false,
                data: legendData
            },
            series : [
                {
                    name: '版本分布',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data:series,
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        };

        _echart.on("click", function (parama) {
            version = parama.name;
            isPartial = true;
            getTableData(isPartial, 1);
            $('html,body').animate({scrollTop:$('#version_list_wrapper').offset().top}, 800);
        });
        _echart.hideLoading();
        _echart.clear();
        _echart.setOption(option);    
    }

    function refreshPaginator( pbody, pobj ){
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback : changePage
        });
    }
    function changePage(params){
        getTableData(isPartial, params.page);
    }
    
    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="text-align: center; font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>'+
            '</div>';
        node.append(html);
    }

}, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'template',
        'msgp-utils/common',
        'w-paginator',
        'msgp-utils/msgpHeaderTip'
    ]
});
