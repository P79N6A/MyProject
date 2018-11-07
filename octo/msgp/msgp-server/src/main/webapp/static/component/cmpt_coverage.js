/**
 * Created by yves on 16/9/21.
 */

M.add('msgp-component/cmpt_coverage', function (Y) {
    var tab_style = "";
    var groupId;
    var artifactId;
    var groupIdCache = {};
    var artifactIdCache= {};
    var limitNumber = 10;
    var everPaged = false,
        totalPage,
        totalCount;
    var pbody;
    var coverageWrapper;
    var checkListWrapper;
    var total_count;
    var detail_count;
    Y.namespace('msgp.component').cmpt_coverage = cmpt_coverage;

    function cmpt_coverage(_tab_style, _groupId, _artifactId) {
        document.title = '组件覆盖清单';
        tab_style = _tab_style;
        groupId = _groupId;
        artifactId = _artifactId;
        coverageWrapper = Y.one('#div_cmpt_coverage');
        checkListWrapper = coverageWrapper.one("#check_list_wrapper");
        total_count = echarts.init(document.getElementById('cmpt_total_count'));
        detail_count = echarts.init(document.getElementById('cmpt_detail_count'));

        initWidget();
        bindWidget();
        setDefaulValue();
    }

    function initCmpt() {
        coverageWrapper.one('#cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            coverageWrapper.one('#cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        coverageWrapper.one('#cmpt').set('value',groupId+',' + artifactId);
                        getVersion(groupId, artifactId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                }
            }
        });
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
                $("#artifactId" ).attr("value", "");
                $('#version').empty();
                $('#version').append('<option value=all>all</option>');
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
                getVersion(groupId, artifactId);
            }
        });
    }


    function setDefaulValue() {
        pbody = coverageWrapper.one('#paginator_checklist_wrapper');
        everPaged = false;
        coverageWrapper.one("#groupId" ).set("value", groupId);
        coverageWrapper.one("#artifactId" ).set("value", artifactId);
        initCmpt();
    }

    function bindWidget() {
        coverageWrapper.one('#searchBtn').on('click', function () {
            getChartAndTableData(-1);
        });
        coverageWrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "") {
                coverageWrapper.one('#owt').empty();
                coverageWrapper.one('#pdl').empty();
                coverageWrapper.one('#owt').append('<option value=all>all</option>');
                coverageWrapper.one('#pdl').append('<option value=all>all</option>');
                getChartAndTableData(-1);

            }else{
                getOwt(business);
            }
        });
        coverageWrapper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                coverageWrapper.one('#pdl').empty();
                coverageWrapper.one('#pdl').append('<option value=all>all</option>');
                getChartAndTableData(-1);
            }else{
                getPdl(owt);
            }
        });

        coverageWrapper.one('#pdl').on('change', function () {
            getChartAndTableData(-1);
        });
        
        coverageWrapper.one('#cmpt').on('change', function () {
            var cmpt = this.get('value').split(",");
            groupId = cmpt[0];
            artifactId = cmpt[1];
            coverageWrapper.one("#groupId" ).set("value", groupId);
            coverageWrapper.one("#artifactId" ).set("value", artifactId);
            getVersion(groupId, artifactId);
        });

        coverageWrapper.one('#version').on('change', function () {
            var version =  coverageWrapper.one("#version").get('value');
            if(version == "all"){
                Y.one("#matching_type").set('disabled', true);
            }else{
                Y.one("#matching_type").set('disabled', false);
            }
            getChartAndTableData(-1);
        });

        coverageWrapper.one('#matching_type').on('change', function () {
            getChartAndTableData(-1);
        });

        coverageWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getChartAndTableData(-1);
        }, '#base_type');
    }
    
    function getVersion(groupId, artifactId) {
        coverageWrapper.one('#version').empty();
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId
        };
        var url = '/component/version';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        coverageWrapper.one('#version').append('<option value=all>all</option>');
                        Y.each(ret.data, function (item) {
                            coverageWrapper.one('#version').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartAndTableData(-1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                }
            }
        });
    }

    function getOwt(business) {
        coverageWrapper.one('#owt').empty();
        coverageWrapper.one('#owt').append('<option value=all>all</option>');
        coverageWrapper.one('#pdl').empty();
        coverageWrapper.one('#pdl').append('<option value=all>all</option>');
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
                            coverageWrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartAndTableData(-1);
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
        coverageWrapper.one('#pdl').empty();
        coverageWrapper.one('#pdl').append('<option value=all>all</option>'); 
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
                            coverageWrapper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartAndTableData(-1);
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

    function getChartOption(dept, names, values, type) {
        var seriesData = [];
        Y.Array.each(names,function(name, index){
            var nameNew = name;
            switch(name) {
                case -1 :
                    nameNew = "未使用该组件";
                    break;
                case 1 :
                    nameNew = "使用组件其他版本";
                    break;
                case 2 :
                    nameNew = "已使用该组件";

                    /*
                                        nameNew = "使用当前版本";
                    */
                    break;
                case 3 :
                    nameNew = "已使用该组件";
                    break;
                //echarts存在bug, 当name为0时, formator 中 params.name为空
                default:
                    break;
            }
            seriesData.push({
                name: nameNew,
                value: values[index]
            })
        });
        var series = [];
        if(type == 'total') {
            series.push({
                name: '使用分布',
                type: 'pie',
                radius : '55%',
                center: ['50%', '60%'],
                data: seriesData,
                itemStyle: {
                    normal : {
                        label : {
                            show : true,
                            formatter: '{b}: {c} ({d}%)'
                        },
                        labelLine : {
                            show : true
                        }
                    },
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            })
        }else{
            series.push({
                name: '使用分布',
                type: 'pie',
                radius : '55%',
                center: ['50%', '60%'],
                data: seriesData,
                itemStyle: {
                    normal : {
                        label : {
                            show : true,
                            formatter: function (params) {
                                var name = params.name;
                                if(includeChineseChar(name)) {
                                    name = name.slice(0, 4)
                                }
                                return name + ': ' + params.value + ' (' + params.percent + '%)'
                            }
                        },
                        labelLine : {
                            show : true
                        }
                    },
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            })
        }

        var version =  coverageWrapper.one("#version").get('value') == "all"  ? "" : coverageWrapper.one("#version").get('value');
        var title = '';
        if(type == 'total') {
            title = dept + ' 组件使用概要';
        }else if (version == "" ){
            title = '使用该组件的服务分布';
        }else{
            title = '使用该版本组件的服务分布';
        }
        return option = { 
            title : {
                text: title,
                subtext: groupId + ', ' + artifactId,
                x:'center'
            },
            noDataLoadingOption : {
                text: '暂无数据'
            },
            tooltip : {
                trigger: 'item',
                formatter: function (params) {
                    var name = params.name;
                    var value = params.value;
                    var percent = params.percent + "%";
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">名称: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + name + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">数量: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">比例: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + percent + '</strong></td> ' +
                        '</tr> ' +
                        '</table>' ;

                    return result
                },
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
                show: false,
                orient: 'vertical',
                x: 'left',
                data: names
            },
            series : series
        };
    }

    function includeChineseChar(str){
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    }

    function fillCharts(outline) {
        total_count.clear();
        detail_count.clear();
        total_count.setOption(getChartOption(outline.dept, outline.total_count_names, outline.total_count_values, 'total'));
        if(outline.typeValues.length != 0) {
            detail_count.setOption(getChartOption(outline.dept, outline.typeNames, outline.typeValues, 'detail'));
        }
        total_count.hideLoading();
        detail_count.hideLoading();
    }

    function getChartAndTableData(pageNo){
        if(pageNo == -1) {
            total_count.showLoading({
                text: '正在加载',
                effect: 'bar',
                textStyle: {
                    fontSize: 20
                }
            });
            detail_count.showLoading({
                text: '正在加载',
                effect: 'bar',
                textStyle: {
                    fontSize: 20
                }
            });
        }else{
            showWaitMsg(checkListWrapper);
        }
        var base = coverageWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = coverageWrapper.one("#business").get('value');
        var owt = coverageWrapper.one("#owt").get('value') == "all"  ? "": coverageWrapper.one("#owt").get('value');
        var pdl = coverageWrapper.one("#pdl").get('value') == "all"  ? "": coverageWrapper.one("#pdl").get('value');
        var version =  coverageWrapper.one("#version").get('value') == "all"  ? "" : coverageWrapper.one("#version").get('value');
        var matching_type =  coverageWrapper.one("#matching_type").get('value');
        artifactId = Y.Lang.trim(coverageWrapper.one("#artifactId").get('value'));
        groupId =  Y.Lang.trim(coverageWrapper.one("#groupId").get('value'));

        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId,
            "base": base,
            "business":business,
            "owt": owt,
            "pdl": pdl,
            "version": version,
            'matching_type' : matching_type,
            "pageNo" : pageNo,
            "pageSize" : 15
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/coverage';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if(data.details.length == 0){
                            total_count.hideLoading();
                            detail_count.hideLoading();
                            Y.msgp.utils.msgpHeaderTip('info', '未产生数据', 3);
                        }else {
                            if(pageNo == -1) {
                                fillCharts(data.outline);
                            }
                            fillTable(data.details);
                            Y.Array.each(data.details, function (item, index) {
                                $("#coverage_app_" + index).tooltip({
                                    html: true,
                                    title: "GroupId: " + item.appGroupId + "<br/>ArtifactId: " + item.appArtifactId,
                                    delay: {
                                        hide: 100
                                    },
                                    container: $("#coverage_app_" + index)
                                });
                            });
                            if($('#check_list').length > 0) {
                                if( !everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount ) {
                                    refreshPaginator(pbody, pobj);
                                }
                            }
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        showEmptyErrorMsg(checkListWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(checkListWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }
    
    function  fillTable(data) {
        var template =
            '<table id="check_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">事业群</th> ' +
            '<th style="width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">业务线</th> ' +
            '<th style="width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">产品线</th> ' +
            '<th style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">项目名</th> ' +
            '<th style="width: 20%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">Appkey</th> ' +
            '<th style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">使用情况</th> ' +
            '<th style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">组件版本</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.business %></td>' +
            '<td style="width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.owt %></td>' +
            '<td style="width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.pdl %></td>' +
            '<td style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" id = "coverage_app_<%= index %>"><%=item.appArtifactId %></td>' +
            '<td style="width: 20%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><a href="http://octo.sankuai.com/service/detail?appkey=' + '<%=item.appkey %>#outline" target="_Blank"><%=item.appkey %></a></td>' +
            '<% if(item.isUsed == 3) { %>' +
            '<td style="width: 15%; color: #4fbba9; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">已使用该组件</td>' +
            '<% }else if (item.isUsed == 2) {%>' +
            '<td style="width: 15%; color: #4fbba9; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">已使用该组件</td>' +
            '<% }else if (item.isUsed == 1) {%>' +
            '<td style="width: 15%; color: #333; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">使用组件其他版本</td>' +
            '<% }else{ %>' +
            '<td style="width: 15%; color: #f00; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">未使用该组件</td>' +
            '<% } %>' +
            '<td style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.version %></td>' +
            '</tr>' +
            '<% }); %>'+
            '</tbody> ' +
            '</table>';
        var micro = new Y.Template();
        var str = micro.render(template, {data : data});
        checkListWrapper.setHTML("");
        checkListWrapper.setHTML(str);
        Y.mt.widget.init();
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
        getChartAndTableData(params.page);
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
        'w-paginator',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
