/**
 * Created by nero on 18/5/18.
 */
M.add('msgp-dashboard/subs', function (Y) {
        Y.namespace('msgp.dashboard').subs = subs;
        var wrapper = Y.one('#subs_wrap');
        var content_overlay = wrapper.one('.content-overlay');
        var pbody = wrapper.one('#paginator_subs');
        var tbody = wrapper.one('#subs_content_body');
        var everPaged = false,
            totalPage,
            totalCount,
            pageSize = 20;
        var colspan = 14;

        var templateStr = ['<% Y.Array.each(this.data, function( item, index ){ %>',
            '<tr data-info="<%= Y.JSON.stringify(item) %>" class="tr_machine_node">',
            '<td><input id="one-checkbox" type="checkbox"></td>',
            '<td><a href="/service/detail?appkey=<%=item.appkey%>' + '#supplier" class="see-details">' + "<%=item.appkey%>" + '</a></td>',
            '<td><span class="t-ellipsis" title="owner">' + "<%=item.owners%>" + '</span></td>',
            '<td>',
            '<a id="subscribe_report" data-appkey="<%=item.appkey%>" data-option="report" data-subscribe="<%=item.isReportSubs%>" href="javascript:void(0);" class="config-panel-delete"><% if(item.isReportSubs == 0){ %>订阅<% }else{ %>取消订阅<% }%></a>',
            '</td>',
            '<td>',
            '<a id="subscribe_node" data-appkey="<%=item.appkey%>" data-option="node" data-subscribe="<%=item.isNodeTriggerSubs%>" href="javascript:void(0);" class="config-panel-delete"><% if(item.isNodeTriggerSubs == 0){ %>订阅<% }else{ %>取消订阅<% }%></a>',
            '</td>',
            '<td>',
            '<a id="subscribe_perf" data-appkey="<%=item.appkey%>" data-option="perf" data-subscribe="<%=item.isPerfTriggerSubs%>" href="javascript:void(0);" class="config-panel-delete"><% if(item.isPerfTriggerSubs == 0){ %>订阅<% }else{ %>取消订阅<% }%></a>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');


        function subs() {
            document.title = '服务治理平台-订阅中心';
            bindGetAgain();
            initSubs(1);
        }

        function initSubs(pageNo) {
            showContent('loading', '');
            var url = "subscribe/listsubs";
            Y.io(url, {
                method: 'get',
                data: {
                    pageNo: pageNo,
                    pageSize: pageSize,
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var pobj = ret.page;
                            if (Y.Lang.isArray(data) && data.length !== 0) {
                                fillSubs(data);
                                if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                    refreshPaginator(pbody, pobj);
                                }
                                everPaged = true;
                                totalPage = pobj.totalPageCount;
                                totalCount = pobj.totalCount;
                            }
                            // if (Y.Lang.isArray(data) && data.length !== 0) {
                            //     fillSupplier(data);
                            //     if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                            //         refreshPaginator(pbody, pobj);
                            //     }
                            // } else if (data.length === 0) {
                            //     emptyOrError();
                            // }
                            // everPaged = true;
                            // totalPage = pobj.totalPageCount;
                            // totalCount = pobj.totalCount;
                        } else {
                            emptyOrError(true);
                        }
                    },
                    failure: function () {
                        emptyOrError(false)
                    }
                }
            });
        }

        function changePage(params) {
            initSubs(params.page);
        }

        function fillSubs(arr) {
            var micro = new Y.Template();
            var html = micro.render(templateStr, {data: arr});
            wrapData(arr);
            showContent('data', html);
            bindClickCheckBox();
            bindAllClickButton();
        }

        function wrapData(arr) {
            tbody.delegate('click', function clickFun(parent) {
                return function () {
                    var self = this;
                    subsChange(parent, self);
                }
            }(this), '#subscribe_report');
            tbody.delegate('click', function clickFun(parent) {
                return function () {
                    var self = this;
                    subsChange(parent, self);
                }
            }(this), '#subscribe_node');
            tbody.delegate('click', function clickFun(parent) {
                return function () {
                    var self = this;
                    subsChange(parent, self);
                }
            }(this), '#subscribe_perf');
        }

        function bindClickCheckBox() {
            //全选
            wrapper.delegate('click', function () {
                var ifChecked = this.get("checked");
                //单选与全选保持一致
                wrapper.all('#one-checkbox').set("checked", ifChecked);
            }, '#all-check');
            //单选
            wrapper.delegate('click', function () {
                //全选与单选保持一致
                var allOneCheck = wrapper.all('#one-checkbox');
                var allOneChecked = wrapper.all('#one-checkbox:checked');
                if (allOneChecked.size() === 0) {
                    wrapper.one('#all-check').set("checked", false);
                } else {
                    if (allOneCheck.size() === allOneChecked.size()) {
                        wrapper.one('#all-check').set("checked", true);
                    }
                }
            }, '#one-checkbox');
        }

        function bindAllClickButton() {
            wrapper.delegate('click', function () {
                var isActive = this.hasClass('active');
                if (isActive) {
                } else {
                    var enabled = +this.getData('enabled');
                    //找到所有的checked,设置role，返回给后端
                    var allCheckedTr = wrapper.all("#one-checkbox:checked");
                    if (allCheckedTr.size() === 0) {
                        Y.msgp.config.popup.alert('请至少选择一项', 2000);
                    } else {
                        var enabledHlbNodesChecked = [];
                        allCheckedTr.each(function (item) {
                            var line = item.ancestor('tr');
                            var info = line.getData('info');
                            enabledHlbNodesChecked.push(info)
                        });
                        batchSubsChange(enabled, enabledHlbNodesChecked)
                    }
                }
            }, "#all-enabled .btn");
        }

        function successCallback() {
            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
            //清空全选
            wrapper.one('#all-check').set('checked', false);
            wrapper.all('#one-checkbox').set('checked', false);
        }

        function batchSubsChange(enabled, arr) {
            var len = arr.length;
            var i = 0;
            var jsonStr = "[";
            for (; i < len; i++) {
                jsonStr += arr[i];
                jsonStr += ","
            }
            jsonStr = jsonStr.substr(0, jsonStr.length - 1);
            jsonStr += "]";

            var data = "{\"enabled\":" + enabled + ",\"data\":" + jsonStr + "}";
            var url = "subscribe/changeList";
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                            successCallback();
                            location.reload();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                    }
                }
            })
        }

        function refreshPaginator(pbody, pobj) {
            new Y.mt.widget.Paginator({
                contentBox: pbody,
                index: pobj.pageNo || 1,
                max: pobj.totalPageCount || 1,
                pageSize: pobj.pageSize,
                totalCount: pobj.totalCount,
                callback: changePage
            });
        }

        function subsChange(parent, self) {
            // var line = self.ancestor('tr');
            var appkey = self.getData("appkey");
            var option = self.getData("option");
            var subscribe = self.getData("subscribe");
            var sub_status = Math.abs(subscribe - 1);
            var url = "subscribe/changeSingle";
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: JSON.stringify({
                    appkey: appkey,
                    option: option,
                    subStatus: sub_status
                }),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                            var $closetTd = $(self._node).closest('td');
                            var message = sub_status == 1 ? "取消订阅" : "订阅"
                            $closetTd.empty().append('<a id="subscribe_perf" data-appkey="' + appkey + '" data-option="' + option + '" data-subscribe="' + sub_status + '" href="javascript:void(0);" class="config-panel-delete">' + message + '</a>');
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                    }
                }
            });
        }

        function bindGetAgain() {
            tbody.delegate('click', function () {
                initSubs(1);
            }, '.get-again');
        }

        function emptyOrError(isError) {
            var html = '<tr class="subs_error"><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            showContent('error', html);
            pbody.empty();
        }

        function showContent(type, data) {
            var supplier_error = tbody.one('.subs_error');
            if (null != supplier_error) {
                supplier_error.remove();
            }
            tbody.one('#content_overlay').hide();
            pbody.show();
            switch (type) {
                case 'error':
                    tbody.append(data);
                    break;
                case 'loading':
                    tbody.all('.tr_machine_node').remove();
                    tbody.one('#content_overlay').show();
                    pbody.hide();
                    break;
                case 'data':
                    tbody.one('#content_overlay').hide();
                    tbody.append(data);
                    break;
            }
        }
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'mt-date',
            'w-base',
            'w-paginator',
            'template',
            'msgp-utils/msgpHeaderTip',
            'msgp-utils/check',
            'msgp-utils/localEdit',
            'msgp-service/commonMap',
            'msgp-utils/common',
            'msgp-config/popup'
        ]
    }
);

M.use('msgp-dashboard/subs', function (Y) {
});
