M.add('msgp-config/panel-version0.1.6', function (Y, NAME) {
    var modifiedItem = {}
    var config = Y.namespace("msgp.config");
    var popup = config.popup;
    var tpl = config.tpl;

    var getCN = function (name) {
        return "J-config-panel-" + name
    };

    var deletedCls = getCN('item-deleted');
    var modifiedCls = getCN('item-modified');
    var addedCls = getCN('item-added');
    var createPRDialog = null;
    var deleteAllDialog = null;

    var trim = Y.Lang.trim;
    var file;
    var timer = null;
    var isTop = true;

    var beginTime = null;
    var confirmSaveDialog, confirmAllSaveDialog, configRollbackDialog, comfirmConfigRollbackDialog;

    var templateStr = [
        '<textarea style="width:620px;height: 300px;" placeholder="PR备注,必须输入...">',
        '</textarea>'
    ].join('');

    var resultTemplate = [
        '<% var type = this.data.type; var color = ("新增" == type) ? "#20b2aa" : "red" ; Y.Array.each(this.data.data,function(item){%>',
        '<tr> <td>'
        + '<%= type%>'
        + '</td><td>'
        + '<font color=\"' + '<%= color%>' + '\"><%= item.key%></font>'
        + '</td><td>'
        + '<font color=\"' + '<%= color%>' + '\"><%= item.value%></font>'
        + '</td><td>'
        + '<font color=\"' + '<%= color%>' + '\"><%= item.comment%></font>'
        + '</td></tr>'
        ,
        '<%});%>'].join('');

    var resultUpdateTemplate = [
        '<% var type = this.data.type; Y.Array.each(this.data.data,function(item){%>',
        '<tr><td>'
        + '<%= type%>'
        + '</td><td>'
        + '<font color="#ff8c00"><%= item.key%></font>'
        + '</td><td>'
        + '<% if(item.newValue != item.value){ %>'
        + '<span class="label">回滚前</span>&nbsp;'
        + '<font color="#ff8c00"><%= (item.newValue && item.newValue.length > 150) ? (item.newValue.slice(0, item.newValue.length)) : item.newValue%></font>'
        + '<br/>'
        + '<span class="label">回滚后</span>&nbsp;'
        + '<% } %>'
        + '<font color="#ff8c00"><%= (item.value && item.value.length > 150) ? (item.value.slice(0, item.value.length)) : item.value%></font>'
        + '<br/></td><td>'
        + '<% if(item.newComment != item.comment && \'\' != item.newComment && \'\' != item.comment){ %>'
        + '<span class="label">回滚前</span>&nbsp;'
        + '<font color="#ff8c00"><%= (item.newComment && item.newComment.length > 20) ? (item.newComment.slice(0, item.newComment.length) ) : item.newComment%></font>'
        + '<br/>'
        + '<span class="label">回滚后</span>&nbsp;'
        + '<% } %>'
        + '<font color="#ff8c00"><%= (item.comment && item.comment.length > 20) ? (item.comment.slice(0, item.comment.length) ) : item.comment%></font>'
        + '<br/></td></tr>'
        ,
        '<%});%>'].join('');

    var tableHead = '<div id="dialog-pre" style="overflow: scroll;height: 500px;width: 980px;"><table class="table  table-striped table-hover table-condensed table-page ", id="rollbackTable">'
        + ' <colgroup>'
        + '   <col width="10%"/>'
        + '   <col width="20%"/>'
        + '   <col width="50%"/>'
        + '   <col width="20%"/>'
        + '   </colgroup>'
        + '   <thead>'
        + '   <tr>'
        + '   <th>操作</th> '
        + '   <th>Key</th> '
        + '   <th>Value</th>'
        + '   <th>Comment</th>'
        + '   </tr>'
        + '   </thead>'
        + '   <tbody class="J-config-panel-tbody">';

    var tableTail = '  </tbody>'
        + '   </table></div>';

    var isOnline;

    var today;

    function nodeDatas() {
        var isNodeDataValid = true;
        var nodeData;
    }

    function MCCEntry() {
        var key;
        var value;
        var comment;
        var isDeleted = false;
    }

    function initOnline() {
        Y.io('/common/online', {
            method: 'get',
            on: {
                success: function (id, obj) {
                    var res = Y.JSON.parse(obj.responseText);
                    if (res.isSuccess) {
                        isOnline = res.data;
                    }
                }
            }
        });
    }

    /**
     * @param {Tree.Node} node
     */
    function Panel(userConfig) {
        this.parentNode = userConfig.parentNode;
        this.container = Y.one(userConfig.container || '#J-config-container-panel');
        this.contentNode = this.container.one(userConfig.contentNode || '.' + getCN('tbody'));
        this.configrollback = this.container.one('.' + getCN('configrollback'));
        this.addNode = this.container.one(userConfig.addNode || '.' + getCN('add'));
        this.exportBtn = this.container.one('.' + getCN('export'));
        this.uploadBtn = this.container.one('.' + getCN('upload'));
        this.saveBtn = this.container.one('.' + getCN('submit'));
        this.uploadFileBtn = this.container.one("#mcc_dynamic_file");
        this.syncDynamicCfg2ProdBtn = this.container.one("#sync-prod-btn button");
        this.nodeValidator = new config.tree.NodeValidatable();
        this.nodeCurrentClassName = '.config-tree-node-current';

        this.createPRBtn = this.container.one('.' + getCN('create-pr'));

        this.deleteAllBtn = this.container.one('.' + getCN('delete-all'));

        this._backToTopBtn = this.container.get('parentNode').one("#backToTop");

        this.version = userConfig.node.version;

        Y.mix(this, userConfig, true, ['node']);
        this.plugins = {};
        this.initData();
        initOnline();

        Y.msgp.service.setEnvText('dynamic_env');
        Y.msgp.service.setEnvText('file_env');
        this._backToTopBtn.hide();
    }

    Panel.prototype = {
        constractor: Panel,
        resetVersion: function (version) {
            this.version = version;
        },
        initData: function (data) {

            if (arguments.length === 0) {
                data = this.node.data || [];
            }
            data = Y.Array.map(data, function (item) {
                if (item.deleted) {
                    return null;
                }
                if (item.comment === null) {
                    if (!(item.oriComment === null)) {
                        item.comment = item.oriComment
                    } else {
                        item.comment = '';
                    }
                }
                if (item.value === null) {
                    if (!(item.oriValue === null)) {
                        item.value = item.oriValue
                    } else {
                        item.value = '';
                    }
                }
                var newItem = {
                    key: item.key,
                    value: item.value,
                    comment: item.comment
                };
                return newItem;
            });
            this.data = Y.Array.filter(data, function (v) {
                return !!v;
            });
            this.originData = Y.clone(this.data);
        },
        plug: function (plugin, cfg) {
            if (!plugin.ns) {
                return this;
            }
            if (this.plugins[plugin.ns]) {
                return this;
            }
            this.plugins[plugin.ns] = true;
            this[plugin.ns] = plugin.create();
            this[plugin.ns].host = this;
            if (typeof plugin.initializer === 'function') {
                plugin.initializer(cfg);
            }
            return this;
        },
        unplug: function (ns) {
            if (arguments.length === 0) {
                Y.Object.each(this.plugins, function (flag, key) {
                    this.unplug(key);
                }, this);
                return this;
            }
            var plugin = this[ns];
            if (typeof plugin.destructor === 'function') {
                plugin.destructor();
            }
            delete this.plugins[ns];
            delete this[ns];
            return this;
        },
        render: function () {
            this.contentNode.setHTML(tpl.panelTrList(this.node));
            this.bindUI();
            this.syncUI();
        },

        bindUI: function () {
            this.addNode.on('click', this._afterAddNodeClick, this);
            this.exportBtn.on('click', this._afterExport, this);
            this.uploadBtn.on('click', this._afterClickUpload, this);
            this.contentNode.delegate('click', this._afterDeleteClick, '.' + getCN('delete'), this);
            this.contentNode.delegate('click', this._afterSingleSaveClick, '.' + getCN('singlesave'), this);
            this.contentNode.delegate('valuechange', this._afterValuechange, 'input, textarea', this);
            this.saveBtn.on('click', this._afterSubmit, this);
            this.uploadFileBtn.on('change', this._afterUpload, this);
            this.createPRBtn.detachAll('click');
            this.createPRBtn.on('click', this._afterClickCreatePR, this);
            this.deleteAllBtn.on('click', this._afterClickDeleteAll, this);
            this._backToTopBtn.on('click', this._backToTop, this);
            this.contentNode.delegate('click', this._expandBtnClick, '.' + getCN('value') + ' i', this);
            this.syncDynamicCfg2ProdBtn.on('click', this._afterClickSyncDynamicCfg2Prod, this);
            this.configrollback.on('click', this._configRollbackClick, this)
        },

        syncUI: function () {
            this.clean();
            this.contentNode.setHTML(tpl.panelTrList(this.node));
            this.AdjustHeight(this);
            this.scrollWin();
            resetBeginTime();
        },

        clean: function () {
            // first delete
            this.contentNode.all('.' + deletedCls).each(function (itemNode) {
                this.uiSetDelete(itemNode, true);
            }, this);
            // then modify
            //this.contentNode.all('.' + getCN('item')).each(function (itemNode, index) {
            //    var originItem = this.originData[index];
            //    this.uiSetModify(itemNode, originItem);
            //}, this);
            this.contentNode.all('.' + modifiedCls).removeClass(modifiedCls);
            this.contentNode.all('.' + addedCls).removeClass(addedCls);
        },

        getItemIndex: function (itemNode) {
            var itemNodeList = this.contentNode.all('.' + getCN('item'));
            var index = itemNodeList.indexOf(itemNode);
            return index;
        },

        getEnvDesc: function (currentEnv) {
            var currentEnvTmp =(-1 == currentEnv.indexOf(".")) ? currentEnv : currentEnv.slice(0, currentEnv.indexOf('.'));
            var envDesc;
            if ('prod' == currentEnvTmp) {
                envDesc = isOnline ? 'prod' : 'dev';
            } else if ('stage' == currentEnvTmp) {
                envDesc = isOnline ? 'stage' : 'ppe';
            } else {
                envDesc = currentEnvTmp;
            };
            return envDesc;
        },

        _afterAddNodeClick: function (e) {
            if (!judgeIntervalTime()) {
                return;
            }
            e.halt();
            this.data.unshift({
                key: '',
                value: '',
                comment: ''
            });
            this.uiSetAdd();
            resetBeginTime();
        },
        _afterSingleSaveClick: function (e) {
            //this.configrollback.removeAttribute('disabled');
            if (!judgeIntervalTime()) {
                return;
            }
            var itemNode = getItemNode(e.target);
            if (!itemNode) {
                return;
            }
            e.halt();

            var saveData = [];
            var index = this.getItemIndex(itemNode);
            var isDeteted = itemNode.hasClass(deletedCls);
            var isAdded = itemNode.hasClass(addedCls);
            var isModified = itemNode.hasClass(modifiedCls);

            if (!isDeteted && !isAdded && !isModified) {
                Y.msgp.utils.msgpHeaderTip('info', "配置内容无任何更改", 3);
                return;
            }

            if (isDeteted) {
                //删除操作
                this.originData.forEach(function (item, i) {
                    if (i != index) {
                        saveData.push(item);
                    }
                });
            } else {
                //更新操作
                var changedKeyInput = itemNode.one('.' + getCN('key') + ' textarea');
                var changedValueInput = itemNode.one('.' + getCN('value') + ' textarea');
                var changedCommentInput = itemNode.one('.' + getCN('comment') + ' input');

                var changedKey = '' + changedKeyInput.get('value');
                var changedValue = changedValueInput.get('value');
                var changedComment = changedCommentInput.get('value');

                var newItem1 = {
                    key: changedKey,
                    value: changedValue,
                    comment: changedComment
                };
                //勿动，用于断掉引用才这么写
                var modifiedItem = {
                    key: newItem1.key,
                    value: newItem1.value,
                    comment: newItem1.comment
                };

                var invalids = [];
                if (!this.nodeValidator.validateNodeDataKey(changedKey)) {
                    var keyNode = itemNode.one('.' + getCN('key') + ' textarea');
                    keyNode.setData('errormsg', '错误 key：' + changedKey + '，key 只能是以下字符的组合：a-zA-Z0-9_-.');
                    invalids.push(keyNode);
                }
                var origin = this.originData.length;
                var cur = this.data.length;
                var isModifiedOverOrigin = isModified && cur > origin;
                if (isModifiedOverOrigin) {
                    index -= (cur - origin);
                }

                this.originData.forEach(function (item, i) {
                    var keyDupNode = itemNode.one('.' + getCN('key') + ' textarea');
                    if (isAdded && (item.key === changedKey)) {
                        keyDupNode.setData('errormsg', '有重复的 key：' + changedKey);
                        invalids.push(keyDupNode);
                    }
                    if (isModified && item.key === changedKey) {//解决添加两次以上修改的的时候顺序乱的问题
                        index = i;
                    }
                    saveData.push(item);
                });


                if (isAdded) {
                    saveData.unshift(modifiedItem);
                } else if (isModified) {
                    saveData[index] = modifiedItem;
                }

                if (invalids.length > 0) {
                    popup.alert(Y.Array.map(invalids, function (nd) {
                        return nd.getData('errormsg');
                    }).join('<br>'));
                    this.uiSetInvalids(invalids);
                    return;
                }
            }
            this.judgeEmptyBeforeSubmit(saveData);

            var self = this;

            var nodeData = Y.JSON.stringify(saveData);
            var nodeName = self.node.nodeName;
            var spaceName = self.node.spaceName;
            var nodeTrueName = this.getNodeTrueName(self.node);
            var url = 'config/space/' + spaceName + '/node/update';
            var params = {
                nodeName: nodeTrueName,
                appkey: spaceName,
                nodeData: nodeData,
                spaceName: spaceName,
                version: self.version ? self.version : 0,
                rollback: false,
            };

            var successCallback = function () {
                self.uiSetValids();
                if (isDeteted) {
                    self.data.splice(index, 1);
                    self.originData.splice(index, 1);
                    itemNode.remove();
                } else if (isAdded) {
                    self.originData.unshift(modifiedItem);
                    itemNode.one("." + getCN('key') + " textarea")._node.setAttribute("readOnly", 'true');
                    itemNode.removeClass(addedCls);
                    itemNode.removeClass(modifiedCls);
                } else {
                    self.originData[index] = modifiedItem;
                    itemNode.removeClass(modifiedCls);
                }
                var msg = isDeteted ? "修改节点删除!" : "修改节点数据成功！";
                popup.alert(msg, 1000);
                //tree.showPanel(params);
                resetBeginTime();
                //location.reload(true);
                self.getLatestVersion(false);
            };
            var failureCallback = function (msg) {
                //Y.msgp.config.popup.alert(msg);
                var savedialog = new Y.mt.widget.CommonDialog({
                    id: 'alert_singlesave_dialog',
                    title: '提示',
                    width: 300,
                    content: msg + "<a target='_blank' href='https://123.sankuai.com/km/page/28354863'>解决方案</a>",
                    btn: {
                        pass: doUpdateVersion,
                        passName: '确定并刷新'
                    }
                });
                resetBeginTime();
                function doUpdateVersion() {
                    self.getLatestVersion(true);
                }

                savedialog.show();
            };
            var currentEnv = nodeName.substring(spaceName.length + 1, nodeName.length);
            confirmSaveDialog = new Y.mt.widget.CommonDialog({
                title: '保存配置',
                width: 250,
                btn: {
                    pass: doSaveConfig
                }
            });
            confirmSaveDialog.setContent('目前处于业务高峰期，请谨慎修改！\n 当前环境是' + this.getEnvDesc(currentEnv) + '，确定保存吗？');
            confirmSaveDialog.text = Y.JSON.stringify(params);
            function doSaveConfig() {
                config.rest.newPut(url, confirmSaveDialog.text, successCallback, failureCallback);
            }
            today = new Date();
            var hour = today.getHours();
            if ((hour >= 11 && hour < 13) || (hour >= 17 && hour < 19)) {
                confirmSaveDialog.show();
            } else {
                config.rest.newPut(url, Y.JSON.stringify(params), successCallback, failureCallback);
            }
        },
        _afterDeleteClick: function (e) {
            if (!judgeIntervalTime()) {
                return;
            }
            var itemNode = getItemNode(e.target);
            if (!itemNode) {
                return;
            }
            e.halt();

            var index = this.getItemIndex(itemNode);
            if (itemNode.hasClass(addedCls)) {
                itemNode.remove();
                this.data.shift(0);
                return;
            }

            var originItem = this.originData[index];
            var item;
            if (originItem) {
                item = this.data[index];
                // toggle
                item.deleted = !item.deleted;

                this.uiSetDelete(itemNode);
                var deleted = itemNode.hasClass(deletedCls);
                if (!deleted) {
                    this.uiSetModify(itemNode, originItem);
                }
            } else {
                this.data.splice(index, 1);
                this.uiSetDelete(itemNode, true);
            }
            resetBeginTime();
        },

        _afterValuechange: function (e) {
            if (!judgeIntervalTime()) {
                return;
            }
            var itemNode = getItemNode(e.target);
            var index = this.getItemIndex(itemNode);

            var role = e.target.ancestor('.J-control-group').get('role');
            this.data[index][role] = trim(e.newVal);

            if (itemNode.hasClass(getCN('item-deleted'))) {
                return;
            }
            var curVal = itemNode.one('.' + getCN('key') + ' textarea').get('value');
            if (!itemNode.hasClass(addedCls)) {
                this.originData.forEach(function (item, i) {
                    if (item.key === curVal) {
                        index = i;
                        return;
                    }
                });
            }
            var originItem = this.originData[index];
            if (!originItem) {
                return;
            }
            if (!itemNode.hasClass(addedCls)) {
                this.uiSetModify(itemNode, originItem);
            }
            resetBeginTime();
        },

        uiSetAdd: function () {
            var itemNode = Y.Node.create(tpl.panelTr());
            this.contentNode.insertBefore(itemNode, this.contentNode.one('tr'));
            itemNode.addClass(addedCls);
        },

        /**
         * @param {Node} itemNode
         */
        uiSetDelete: function (itemNode, remove) {
            if (remove) {
                itemNode.remove();
                return;
            }
            var modified = itemNode.hasClass(modifyCls);
            itemNode.toggleClass(deletedCls);

            var modifyCls = getCN('item-modified');
            if (itemNode.hasClass(modifyCls)) {
                itemNode.removeClass(modifyCls);
            }

            if (!modified) {
                uiSetRestore(itemNode);
            }
        },

        uiSetModify: function (itemNode, originItem) {
            if (!originItem) {
                return;
            }
            var modified = itemNode.all('input, textarea').some(function (valueNode) {
                var role = valueNode.ancestor('.J-control-group').get('role');
                return valueNode.get('value') !== originItem[role];
            });
            if (modified) {
                itemNode.addClass(modifiedCls);
            } else {
                itemNode.removeClass(modifiedCls);
            }
        },

        uiSetInvalids: function (invalids) {
            Y.Array.each(invalids, function (nd) {
                var ndGroup = nd.ancestor('.J-control-group');
                if (ndGroup) {
                    ndGroup.addClass('error');
                }
            });
        },

        uiSetValids: function () {
            var allkeyinputs = this.contentNode.all('.' + getCN('key') + ' textarea');
            allkeyinputs.each(function (nd) {
                var ndGroup = nd.ancestor('.J-control-group');
                if (ndGroup.hasClass('error')) {
                    ndGroup.removeClass('error');
                }
            });
        },

        _afterClickUpload: function (e) {
            if (!judgeIntervalTime()) {
                return false;
            }
            this.container.one("#mcc_dynamic_file").simulate("click");
        },
        _handleChangedEntry: function (nodes, isDeleted) {
            var ret = [];
            nodes.each(function (nd) {
                var keyNd = nd.one("." + getCN('key') + " textarea");
                var valueNd = nd.one("." + getCN('value') + " textarea");
                var commentNd = nd.one("." + getCN('comment') + " input");
                var item = new MCCEntry();
                item.key = keyNd.get('value');
                item.value = valueNd.get('value');
                item.comment = commentNd.get('value');
                item.isDeleted = isDeleted;
                ret.push(item);
            }, this);
            return ret;
        },
        _afterClickCreatePR: function (e) {
            if (!judgeIntervalTime()) {
                return false;
            }
            var tableNode = this.container.one('.table');
            var addNodes = tableNode.all("." + addedCls);
            var modifiedNodes = tableNode.all("." + modifiedCls);
            var deletedNodes = tableNode.all("." + deletedCls);

            if (addNodes.isEmpty() && modifiedNodes.isEmpty() && deletedNodes.isEmpty()) {
                Y.msgp.utils.msgpHeaderTip('error', "配置内容无任何更改,不能提交PR", 3);
                return;
            }

            var dataRet = this._getNodeData(e);
            if (!dataRet.isNodeDataValid)return;

            var addEntries = this._handleChangedEntry(addNodes, false);
            var modifiedEntries = this._handleChangedEntry(modifiedNodes, false);
            var deletedEntries = this._handleChangedEntry(deletedNodes, true);
            var dataList = addEntries.concat(modifiedEntries);
            dataList = dataList.concat(deletedEntries);
            var nodeName = this.node.nodeName;
            var spaceName = this.node.spaceName;

            var envStr = nodeName.slice(spaceName.length + 1, nodeName.length).split('.')[0];
            var envs = ["test", "stage", "prod"];
            var envInt;
            var isExist = false;
            for (envInt = 0; envInt < envs.length; ++envInt) {
                if (envs[envInt] === envStr) {
                    isExist = true;
                    break;
                }
            }
            ++envInt;
            // if (!isExist) {
            //     Y.msgp.utils.msgpHeaderTip('error', "暂不支持分组和泳道配置的Review管理", 3);
            //     //createPRDialog.hide();
            //     //Y.msgp.utils.msgpHeaderTip('error', "环境识别出错,env = " + envStr, 3);
            //     resetBeginTime();
            //     return;
            // }

            var params1 = {
                appkey: this.getPathFromNodeName(this.node),
                env: envInt,
                data: dataList
            };
            createPRDialog = new Y.mt.widget.CommonDialog({
                id: 'create_pr_dialog',
                title: '创建PR',
                width: 640,
                btn: {
                    pass: doCreatePR,
                    arguments: params1,
                    passName: '提交'
                }
            });
            var micro = new Y.Template();
            var str = micro.render(templateStr);
            createPRDialog.setContent(str);
            createPRDialog.text = nodeName;
            createPRDialog.show();

            function doCreatePR(saveButton, container) {
                params1 = createPRDialog.config.btn.arguments;
                var prNote = trim(container.one('textarea').get('value'));
                if ("" === prNote) {
                    Y.msgp.utils.msgpHeaderTip('error', "PR备注不能为空", 3);
                    return true;
                }
                var appkey = params1.appkey.split("/")[0];
                var url = '/serverOpt/'+ appkey + '/config/pullrequest';
                var params = {
                    appkey: appkey,
                    nodename: params1.appkey,
                    env: params1.env,
                    note: prNote,
                    data: params1.data
                };
                var successCallback = function (ret) {
                    if (ret.isSuccess) {
                        Y.one("#wrap_config").one('#pr_reveiw_manage').simulate('click');
                        Y.msgp.utils.msgpHeaderTip('success', 'PR提交成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('success', ret.data || '系统出错', 3);
                    }
                };

                var failureCallback = function (msg) {
                    var failuredialog = new Y.mt.widget.CommonDialog({
                        id: 'alert_singlefresh_dialog',
                        title: '提示',
                        width: 300,
                        content: msg,
                        btn: {
                            pass: doUpdateVersion,
                            passName: '确定并刷新',
                            unpass: doUpdateVersion
                        }
                    });
                    function doUpdateVersion() {
                        Y.one("#wrap_config").one('.dyBtn').simulate('click');
                    }

                    failuredialog.show();
                };
                Y.io(url, {
                    method: 'POST',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(params),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                successCallback(ret);
                            } else {
                                failureCallback(ret.msg);
                            }
                        },
                        failure: function (id, o) {
                            failureCallback("系统出错");
                        }
                    }
                });
            }

            resetBeginTime();
        },

        _afterUpload: function (e) {
            var self = this;
            var uploader = this.container.one("#mcc_dynamic_file");
            var fileList = uploader._node.files;
            if (fileList.length <= 0) {
                Y.msgp.utils.msgpHeaderTip('error', "请选择文件", 5);
                return;
            }
            var file = fileList[0];
            if (file.size > 204800) {
                Y.msgp.utils.msgpHeaderTip('error', "文件不能超过200KB", 5);
                return;
            }

            var reader = new FileReader();
            reader.onload = (function (file) {
                return function (e) {
                    self._parseMCCDynamicFile(this.result);
                    var new_node = document.createElement("textarea");
                    new_node.setAttribute('id', 'mcc_dynamic_file');
                    new_node.setAttribute('type', 'file');
                    //new_node.setStyle('display', 'none');
                    self.container.one('#mcc_dynamic_file').setStyle('display', 'none');
                    self.container.one('#mcc_dynamic_file').set('value', '');
                    self.container.one('#mcc_dynamic_file').on('change', self._afterUpload, self);
                };
            })(file);
            reader.readAsText(file);
            resetBeginTime();
        },

        _parseMCCDynamicFile: function (dataStr) {
            var self = this;
            var lineStr = dataStr.split(/\n/);
            var curComment = "";
            var curKey = "";
            var curValue = "";
            var ret = [];
            lineStr.forEach(function (item) {
                var curItem = trim(item);
                if (curItem.length > 0) {
                    var eqIndex = curItem.indexOf("=");
                    if (eqIndex > 0) {
                        var key = curItem.substring(0, eqIndex);
                        curKey = trim(key);
                        var value = curItem.substring(eqIndex + 1, curItem.length);
                        curValue = trim(value);
                        var newItem = {
                            key: curKey,
                            value: curValue,
                            comment: curComment
                        };
                        ret.push(newItem);
                        curComment = "";
                        curKey = "";
                        curValue = "";
                    } else if (-1 !== curItem.indexOf("#")) {
                        var sharpIndex = curItem.indexOf("#");
                        if (sharpIndex >= 0) {
                            var comment = curItem.substring(sharpIndex + 1, curItem.length);
                            curComment += trim(comment);
                        }
                    } else {
                        ret[ret.length - 1].value += '\r\n' + item;
                    }
                }
            });
            //console.log("ret: " + Y.JSON.stringify(ret));
            ret.forEach(function (item) {
                var uiKeyNode = self.container.one("#" + item.key) || Y.one(document.getElementById(item.key));
                if (uiKeyNode) {
                    //change value
                    var tr = uiKeyNode.ancestor("tr");
                    var uiValueNode = tr.one(".J-config-panel-value textarea");
                    var uiCommentNode = tr.one(".J-config-panel-comment input");


                    var oldValue = uiValueNode.get('value');
                    var oldComment = uiCommentNode.get('value');
                    var isChanged = false;
                    if (oldValue != item.value) {
                        isChanged = true;
                        uiValueNode.set('value', item.value);
                    }
                    var checkComment = "# " + item.comment;

                    if ((("" != trim(oldComment)) && (oldComment != checkComment) || (("" == trim(oldComment)) && ("" != item.comment)))) {
                        isChanged = true;
                        uiCommentNode.set('value', item.comment);
                    }

                    self.data.forEach(function (y) {
                        if (y.key == item.key) {

                            oldValue = y.value;
                            oldComment = y.comment;
                            y.value = item.value;
                            y.comment = item.comment;
                        }
                    });
                    if (isChanged) {
                        uiValueNode.ancestor("tr").addClass(modifiedCls);
                    } else {
                        uiValueNode.ancestor("tr").removeClass(modifiedCls);
                    }
                    self.AdjustHeightNode(tr);
                } else {
                    //new key = value
                    self.data.unshift({
                        key: item.key,
                        value: item.value,
                        comment: item.comment
                    });

                    var newNodeStr = tpl.panelTrList({data: [item]});
                    var itemNode = Y.Node.create(newNodeStr);

                    self.contentNode.insertBefore(itemNode, 0);
                    itemNode.addClass(addedCls);
                    self.AdjustHeightNode(itemNode);
                }
            });
            popup.alert('导入成功，请保存!', 1000);
        },

        _afterExport: function (e) {
            if (!judgeIntervalTime()) {
                return;
            }
            var spaceName = this.node.spaceName;
            var nodeName = this.node.nodeName;
            var nodeTrueName = this.getNodeTrueName(this.node);
            var url = 'config/space/node/get';
            var clearNodeName = this.getClearNodeName(nodeTrueName);
            var successCallback = function (data) {
                var dataStr = "\ufeff";
                data.data.forEach(function (item) {
                    dataStr += (item.comment) ? item.comment : "";
                    dataStr += "\r\n";
                    dataStr += item.key + "=" + item.value;
                    dataStr += "\r\n";
                });
                save2File(clearNodeName, dataStr);
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert(msg);
            };
            config.rest.newGet(url, {appkey: spaceName, nodeName: nodeName}, successCallback, failureCallback);
            resetBeginTime();
        },

        getPathFromNodeName: function (node) {
            var self = this;
            var nodeTrueName = this.getNodeTrueName(node);
            var nodeNameSlice = nodeTrueName.slice(node.spaceName.length + 1, nodeTrueName.length);
            var data = nodeNameSlice.split('.');
            var clearNodeName = node.spaceName;
            Y.Array.forEach(data, function (item) {
                if ('prod' == item || 'stage' == item) {
                    item = self.getEnvDesc(item);
                }
                clearNodeName += "/" + item;
            });
            return clearNodeName;
        },

        getClearNodeName: function (nodeName) {
            if (isOnline) return nodeName;
            var self = this;
            var data = nodeName.split('.');
            var clearNodeName = "";
            Y.Array.forEach(data, function (item) {
                if ('prod' == item || 'stage' == item) {
                    item = self.getEnvDesc(item);
                }
                clearNodeName += item + ".";
            });
            clearNodeName = clearNodeName.slice(0, clearNodeName.length-1);
            return clearNodeName;
        },

        _configRollbackClick: function (e) {
            var self = this;
            var self_e = e;
            var appkey = self.node.spaceName;
            var nodeData = Y.JSON.stringify(self.data);
            //console.log("页面：" + nodeData);
            var nodeName = self.node.nodeName;
            var url = '/serverOpt/config/space/' + appkey + '/node/configrollback';
            var params = {
                nodeName: nodeName,
                content: nodeData,
            };
            Y.io(url, {
                method: "POST",
                data: Y.JSON.stringify(params),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var retData = ret.data;
                        //console.log("回滚：" + Y.JSON.stringify(retData));
                        if (undefined === ret || !ret.success || 500 == retData.ret) {
                            //获取版本记录失败，且与当前页面的配置不一样
                            Y.msgp.utils.msgpHeaderTip('error', "暂无可回滚的配置", 3);
                        } else if (304 == retData.ret) {
                            //获取版本记录成功，且与当前页面的配置一样
                            Y.msgp.utils.msgpHeaderTip('error', "暂无可回滚的配置，当前配置没有修改", 3);
                        } else if (200 == retData.ret) {
                            //获取版本记录成功，且与当前页面的配置不一样
                            comfirmConfigRollbackDialog = comfirmConfigRollbackDialog ? comfirmConfigRollbackDialog : new Y.mt.widget.CommonDialog({
                                id: 'configRollbackDiv',
                                title: '回滚动态配置',
                                width: 1000,
                                btn: {
                                    pass: confirmRollbackConfig
                                }
                            });
                            var result = self._getConfigRollbackResult(retData);
                            comfirmConfigRollbackDialog.setContent(result);
                            comfirmConfigRollbackDialog.text = retData;

                            comfirmConfigRollbackDialog.show();
                            function confirmRollbackConfig() {
                                var retData = comfirmConfigRollbackDialog.text;
                                configRollbackDialog = configRollbackDialog ? configRollbackDialog : new Y.mt.widget.CommonDialog({
                                    title: '回滚动态配置',
                                    width: 300,
                                    content: '确定回滚到上一次的修改吗，回滚后配置会被覆盖',
                                    btn: {
                                        pass: doRollbackConfig
                                    }
                                });
                                configRollbackDialog.text = retData;
                                configRollbackDialog.show();
                                function doRollbackConfig() {
                                    var retData1 = configRollbackDialog.text;
                                    self._doConfigRollback(self_e, retData1, self)
                                }
                            }
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                    }
                }
            });


        },

        _doConfigRollback: function (e, retData, self) {
            //console.log(Y.JSON.stringify(retData));
            if (!retData.enableRollback) {
                this.configrollback.setAttribute('disabled', 'disabled');
            }

            if (retData.updateData.length > 0) {
                retData.updateData.forEach(function (itemUpdate) {
                    var uiKeyNode = self.container.one("#" + itemUpdate.key) || Y.one(document.getElementById(itemUpdate.key));
                    if (!uiKeyNode) {
                        var ndKeys = self.contentNode.all('.' + getCN('key') + ' textarea');
                        ndKeys.each(function (nd) {
                            var val = trim(nd.get('value'));
                            if (val == itemUpdate.key) {
                                uiKeyNode = nd.ancestor("td");
                            }
                        });
                    }
                    //change value
                    var tr = uiKeyNode.ancestor("tr");
                    var uiValueNode = tr.one(".J-config-panel-value textarea");
                    var uiCommentNode = tr.one(".J-config-panel-comment input");

                    var oldValue = uiValueNode.get('value');
                    var oldComment = uiCommentNode.get('value');
                    var isChanged = false;
                    if (oldValue != itemUpdate.value) {
                        isChanged = true;
                        uiValueNode.set('value', itemUpdate.value);
                    }
                    var checkComment = "# " + itemUpdate.comment;
                    if ((("" != trim(oldComment)) && (oldComment != checkComment) || (("" == trim(oldComment)) && ("" != itemUpdate.comment)))) {
                        isChanged = true;
                        //item.comment = item.comment ? item.comment : 'null';
                        uiCommentNode.set('value', itemUpdate.comment);
                    }

                    self.data.forEach(function (y) {
                        if (y.key == itemUpdate.key) {
                            oldValue = y.value;
                            oldComment = y.comment;
                            y.value = itemUpdate.value;
                            y.comment = itemUpdate.comment;
                        }
                    });
                    if (isChanged) {
                        uiValueNode.ancestor("tr").addClass(modifiedCls);
                    } else {
                        uiValueNode.ancestor("tr").removeClass(modifiedCls);
                    }
                    //self.AdjustHeightNode(tr);
                });
                //retData.updateData.length = 0;
            }
            if (retData.deleteData.length > 0) {
                retData.deleteData.forEach(function (itemDelete) {
                    var uiKeyNode = self.container.one("#" + itemDelete.key) || Y.one(document.getElementById(itemDelete.key));
                    //var targetNode;
                    if (!uiKeyNode) {
                        var ndKeys = self.contentNode.all('.' + getCN('key') + ' textarea');
                        ndKeys.each(function (nd) {
                            var val = trim(nd.get('value'));
                            if (val == itemDelete.key) {
                                uiKeyNode = nd.ancestor("td");
                            }
                        });
                    }

                    var itemNode = uiKeyNode.ancestor("tr");
                    var index = self.getItemIndex(itemNode);
                    var originItem = self.originData[index];

                    var item;
                    if (originItem) {
                        item = self.data[index];
                        // toggle
                        item.deleted = !item.deleted;

                        self.uiSetDelete(itemNode);
                        itemNode.addClass(deletedCls);
                    } else {
                        self.data.splice(index, 1);
                        self.uiSetDelete(itemNode, true);
                    }
                });
                //retData.deleteData.length = 0;
            }
            if (retData.addData.length > 0) {
                retData.addData.forEach(function (itemAdd) {
                    self.data.unshift({
                        key: itemAdd.key,
                        value: itemAdd.value,
                        comment: itemAdd.comment
                    });

                    var newNodeStr = tpl.panelTrList({data: [itemAdd]});
                    var itemNode = Y.Node.create(newNodeStr);

                    self.contentNode.insertBefore(itemNode, 0);
                    itemNode.addClass(addedCls);
                    self.AdjustHeightNode(itemNode);
                })
                //retData.addData.length = 0;
            }
            //retData.data.length = 0;
            //console.log("结束originData：" + Y.JSON.stringify(self.originData));
            //console.log("结束data：" + Y.JSON.stringify(self.data));
            self._updateNodeData(self, e, retData.data, '配置回滚成功！', true);
        },

        _getConfigRollbackResult:function (retData) {
            var result = '';
            result += tableHead;
            if ( '' != retData.updateData) {
                result += new Y.Template().render(resultUpdateTemplate, {
                    data: {
                        type: "修改",
                        data: retData.updateData
                    }
                });

            }
            if ( '' != retData.addData) {
                result += new Y.Template().render(resultTemplate, {
                    data: {
                        type: "新增",
                        data: retData.addData
                    }
                });
            }
            if ( '' != retData.deleteData) {
                result += new Y.Template().render(resultTemplate, {
                    data: {
                        type: "删除",
                        data: retData.deleteData
                    }
                });
            }
            result += tableTail ;
            return result;
        },

        _getNodeData: function (e) {
            var ret = new nodeDatas();
            e.preventDefault();
            var invalids = [];
            var ndKeys = this.contentNode.all('.' + getCN('key') + ' textarea');
            var vals = [];
            ndKeys.each(function (nd) {
                var val = trim(nd.get('value'));
                if (!this.nodeValidator.validateNodeDataKey(val)) {
                    nd.setData('errormsg', '错误 key：' + val + '，key 只能是以下字符的组合：a-zA-Z0-9_-.');
                    invalids.unshift(nd);
                } else if (vals.indexOf(val) !== -1) {
                    nd.setData('errormsg', '有重复的 key：' + val);
                    invalids.unshift(nd);
                } else if (!nd._node.hasAttribute('readOnly')) {
                    nd._node.setAttribute("readOnly", 'true');
                }
                vals.push(val);

            }, this);

            if (invalids.length > 0) {
                popup.alert(Y.Array.map(invalids, function (nd) {
                    return nd.getData('errormsg');
                }).join('<br>'));
                this.uiSetInvalids(invalids);
                ret.isNodeDataValid = false;
                ret.nodeData = [];
                return ret;
            }
            //console.log("this.data：" + Y.JSON.stringify(this.data));
            var nodeData = Y.Array.reduce(this.data, [], function (prev, item) {
                if (item.deleted) {
                    return prev;
                }
                var newItem = {key: item.key, value: item.value, comment: item.comment};
                prev.unshift(newItem);
                return prev;
            });
            ret.nodeData = nodeData;
            ret.isNodeDataValid = true;
            return ret;
        },

        _afterSubmit: function (e) {
            //this.configrollback.removeAttribute('disabled');
            if (!judgeIntervalTime()) {
                return;
            }
            var ndKeys = this.contentNode.all('.' + getCN('key') + ' textarea');
            var operateFlag = false;
            ndKeys.each(function (item) {
                var node = getItemNode(item);
                if (node.hasClass(deletedCls) || node.hasClass(addedCls) || node.hasClass(modifiedCls)) {
                    operateFlag = true;
                    return;
                }
            });
            if (!operateFlag) {
                Y.msgp.utils.msgpHeaderTip('info', "配置内容无任何更改", 3);
                return;
            }
            var nodeDataRet = this._getNodeData(e);
            if (!nodeDataRet.isNodeDataValid)return;

            var self = this;
            var nodeName = self.node.nodeName;
            var spaceName = self.node.spaceName;
            var currentEnv = nodeName.substring(spaceName.length + 1, nodeName.length);
            confirmAllSaveDialog = confirmAllSaveDialog ? confirmAllSaveDialog : new Y.mt.widget.CommonDialog({
                title: '保存配置',
                width: 250,
                btn: {
                    pass: doSaveConfig,
                }
            });
            confirmAllSaveDialog.setContent('当前环境是' + this.getEnvDesc(currentEnv) + '，确定保存吗？');
            confirmAllSaveDialog.text = nodeDataRet.nodeData;
            confirmAllSaveDialog.show();
            function doSaveConfig() {
                self._updateNodeData(self, e, confirmAllSaveDialog.text, '修改节点数据成功！', false);
            }
        },

        _updateNodeData: function (self, e, nodedata, sucMsg, rollback) {
            this.judgeEmptyBeforeSubmit(nodedata);
            var nodeData = Y.JSON.stringify(nodedata);
            var nodeName = this.getNodeTrueName(this.node);
            var spaceName = this.node.spaceName;
            var url = 'config/space/' + spaceName + '/node/update';
            //console.log("nodeData：" + nodeData);

            //update时需要对nodeData转义
            var params = {
                appkey: spaceName,
                nodeName: nodeName,
                nodeData: nodeData,
                spaceName: spaceName,
                version: this.version ? this.version : 0,
                rollback: rollback,
            };
            var successCallback = function () {
                popup.alert(sucMsg, 1000);
                self.initData(self.data);
                self.clean();
                self.uiSetValids();
                resetBeginTime();
                self.getLatestVersion(false);
            };
            var failureCallback = function (msg) {
                var savedialog = new Y.mt.widget.CommonDialog({
                    id: 'alert_singlesave_dialog',
                    title: '提示',
                    width: 300,
                    content: msg + "<a target='_blank' href='https://123.sankuai.com/km/page/28354863'>解决方案</a>",
                    btn: {
                        pass: doUpdateVersion,
                        passName: '确定并刷新'
                    }
                });
                resetBeginTime();
                function doUpdateVersion() {
                    self.getLatestVersion(true);
                }

                savedialog.show();
            };
            //successCallback();
            //console.log("originData：" + Y.JSON.stringify(self.originData));
            //console.log("data：" + Y.JSON.stringify(self.data));
            config.rest.newPut(url, Y.JSON.stringify(params), successCallback, failureCallback);
        },

        getParentName: function (nodeName) {
            return nodeName.slice(0, nodeName.lastIndexOf('.'));
        },

        getNodeTrueName: function (node) {
            var self = this;
            var nodeName = node.nodeName;
            var nodeTrueName;
            if (node.isCell) {
                nodeTrueName = nodeName + "[cell]"
            }
            else {
                var parent = self.parentNode;
                nodeTrueName = parent && parent._node.nextElementSibling.innerText == "set" ? this.getParentName(nodeName) + "[cell]." + nodeName.slice(nodeName.lastIndexOf('.') + 1) : nodeName;
            }
            return nodeTrueName;
        },

        destroy: function () {
            this.contentNode.empty();
            // detachAll
            this.addNode.detachAll('click');
            this.exportBtn.detachAll('click');
            this.uploadBtn.detachAll('click');
            this.configrollback.detachAll('click');
            this.saveBtn.detachAll('click');
            this.contentNode.detachAll('click');
            this.uploadFileBtn.detachAll('change');
            this.contentNode.detachAll('valuechange');
            this.createPRBtn.detachAll('click');
            //this.container.one('form').detachAll('submit');
            this.deleteAllBtn.detachAll('click');
            this.unplug();
        },
        _afterClickDeleteAll: function (e) {
            if (!judgeIntervalTime()) {
                return;
            }
            var nodes = this.contentNode.all('.' + getCN('delete'));
            nodes.each(function (node) {
                var textNode = node.one('.' + getCN('delete-text'));
                if (("删除" === textNode.get('text'))) {
                    node.simulate('click');
                }
            });
            deleteAllDialog = deleteAllDialog ? deleteAllDialog : new Y.mt.widget.CommonDialog({
                id: 'delete_all_dialog',
                title: '删除全部',
                width: 500,
                btn: {
                    pass: doDeleteAll,
                    unpass: unDoDeleteAll
                },
                content: '<font color="red">确定要删除全部吗？删除后不可恢复。</font>'
            });
            deleteAllDialog.show();
            var self = this;

            function doDeleteAll() {
                self._afterSubmit(e);
                resetBeginTime();
            }

            function unDoDeleteAll() {
                nodes.each(function (node) {
                    var textNode = node.one('.' + getCN('delete-text'));
                    if (("恢复" === textNode.get('text'))) {
                        node.simulate('click');
                    }
                });
                resetBeginTime();
            }
        },
        scrollWin: function () {
            var self = this;
            //获取页面可视区的高度
            var clientHeight = document.documentElement.clientHeight;
            window.onscroll = function () {
                var osTop = document.documentElement.scrollTop || document.body.scrollTop;
                if (osTop >= clientHeight) {
                    self._backToTopBtn.show();
                } else {
                    self._backToTopBtn.hide();
                }
                if (!self.isTop) {
                    clearInterval(self.timer);
                }
                self.isTop = false;
            }
        },
        //返回顶部
        _backToTop: function (e) {
            var self = this;
            //设置定时器
            self.timer = setInterval(function () {
                //获取滚动条距离顶部的高度
                var osTop = document.documentElement.scrollTop || document.body.scrollTop;
                var isSpeed = Math.floor(-osTop / 6);
                document.documentElement.scrollTop = document.body.scrollTop = osTop + isSpeed;
                self.isTop = true;
                if (osTop === 0) {
                    clearInterval(self.timer);
                }
            }, 30);
        },

        AdjustHeight: function (panel) {
            var alltextarea = this.container.all('.' + getCN('key') + ' textarea').concat(this.container.all('.' + getCN('value') + ' textarea'));
            var ielement = '<i class="fa fa-expand"></i>';
            alltextarea.each(function (node) {
                var valuetext = node._node,
                    scrollHeight = valuetext.scrollHeight;
                if (scrollHeight > 100) {
                    scrollHeight = 100;
                    var tr = node.ancestor('tr');
                    if (!tr.hasClass(addedCls) && !tr.hasClass(modifiedCls)) {
                        node.ancestor('td').append(ielement);
                    }
                }
                requestAnimationFrame(function () {
                    valuetext.style.height = scrollHeight + 'px';
                });
            });
            initJsonEditor(panel);
        },

        AdjustHeightNode: function (node) {
            var ielement = '<i class="fa fa-expand"></i>';
            var valuetext = node.one(('.' + getCN('value') + ' textarea'))._node;
            if (valuetext.scrollHeight > 100) {
                valuetext.style.height = '100px';
                if (!node.one('i')) {
                    node.append(ielement);
                }
            } else {
                valuetext.style.overflow = 'hidden';
                valuetext.style.height = valuetext.scrollHeight + 'px';
            }

        },

        _expandBtnClick: function (e) {
            var expand = e.target;
            var valuetext = expand.ancestor('td').one('textarea')._node;
            if (expand.hasClass('fa-expand')) {
                valuetext.style.overflow = 'hidden';
                valuetext.style.height = valuetext.scrollHeight + 'px';
                expand.removeClass('fa-expand').addClass('fa-compress');
            } else {
                valuetext.style.overflow = 'auto';
                valuetext.style.height = '100px';
                expand.removeClass('fa-compress').addClass('fa-expand');
            }
        },

        judgeEmptyBeforeSubmit: function (saveData) {
            saveData.forEach(function (item) {
                if ('' || null || undefined === item.key) {
                    return false;
                }
            });
            return true;
        },

        getLatestVersion: function (reload) {
            var spaceName = this.node.spaceName;
            var nodeTrueName = this.getNodeTrueName(this.node);
            var url = 'config/space/node/get';

            var self = this;
            var successCallback = function (data) {
                //self.configrollback.removeAttribute('disabled');
                self.resetVersion(data.version);
                if (reload) {
                    self.node.data = data.data;
                    self.initData();
                    self.syncUI();
                }
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert(msg);
            };
            config.rest.newGet(url, {appkey: spaceName, nodeName: nodeTrueName}, successCallback, failureCallback);
        },

        _afterClickSyncDynamicCfg2Prod: function () {
            var spaceName = this.node.spaceName;
            var nodeName = this.node.nodeName;
            var url = 'config/space/' + spaceName + '/node/syncprod';
            var delSupplierDialog = new Y.mt.widget.CommonDialog({
                id: 'sync_dynamic_cfg_dialog',
                title: '操作确认',
                content: '确认添加staging现有动态配置到prod环境?<br/>提示: 同步后prod中同Key的Value将被覆盖!(其他Key不受影响)',
                width: 300,
                btn: {
                    pass: doSyncDynamicCfg
                }
            });
            delSupplierDialog.show();
            function doSyncDynamicCfg() {
                var successCallback = function (data) {
                    Y.one("#wrapper-config-dynamic-file #prod_btn")._node.click();
                };
                var failureCallback = function (msg) {
                    var syncDialog = new Y.mt.widget.CommonDialog({
                        id: 'alert_sync_dialog',
                        title: '提示',
                        width: 300,
                        content: msg + "<a target='_blank' href='https://123.sankuai.com/km/page/28354863'>解决方案</a>",
                        btn: {
                            passName:'确认'
                        }
                    });
                    syncDialog.show();
                };
                config.rest.newPost(url, {nodeName: nodeName}, successCallback, failureCallback);
            }
        }
    };

    config.Panel = Panel;

    function getItemNode(itemNode) {
        var itemCls = '.' + getCN('item');
        if (!itemNode.test(itemCls)) {
            itemNode = itemNode.ancestor(itemCls);
        }
        if (itemNode) {
            return itemNode;
        }
        return null;
    }


    function uiSetRestore(itemNode) {
        var deleted = itemNode.hasClass(deletedCls);
        var modified = itemNode.hasClass(modifiedCls);
        var text;
        var previous;
        var current;
        if (deleted || modified) {
            previous = 'fa-trash-o';
            current = 'fa-undo';
            text = '恢复';
        } else {
            current = 'fa-trash-o';
            previous = 'fa-undo';
            text = '删除';
        }
        var iconNode = itemNode.one('.' + getCN('delete') + ' > .fa');
        iconNode.removeClass(previous).addClass(current);
        var textNode = itemNode.one('.' + getCN('delete-text'));
        textNode.set('text', text);
    }

    function judgeIntervalTime() {
        var endTime = new Date();
        var diff = (endTime.getTime() - beginTime.getTime()) / 1000;
        if (diff > 600) {
            //alert("请刷新页面后再操作!");
            var dialog = new Y.mt.widget.CommonDialog({
                id: 'alert_overtime_dialog',
                title: '提示',
                width: 300,
                content: '停留时间超过10分钟,请刷新页面后再操作'
            });
            dialog.show();
            return false;
        }
        return true;
    }

    function resetBeginTime() {
        beginTime = new Date();
    }

    /* -- json editor start -- */
    function initJsonEditor(panel) {
        var valueAreas = document.querySelectorAll('.J-config-panel-value');

        var onMouseEnter = function (e) {
            var target = e.target;
            var textarea = target.querySelector('textarea');
            try {
                var value = textarea.value;
                JSON.parse(textarea.value);
                if (value.indexOf('{') !== -1 && value.indexOf('}') !== -1) {
                    target.insertAdjacentHTML('beforeend', '<div class="json-edit-btn"></i>在 JSON 编辑器中打开</div>');
                    var btn = target.querySelector('.json-edit-btn');
                    btn.addEventListener('click', function () {
                        window.jsonEditorStore.showModal(textarea, panel);
                    });
                }
            } catch (e) {

            }
        };

        var onMouseLeave = function (e) {
            var target = e.target;
            var btn = target.querySelector('.json-edit-btn');
            if (btn) {
                target.removeChild(btn);
            }
        };

        valueAreas.forEach(function (valueArea) {
            valueArea.addEventListener('mouseenter', onMouseEnter);
            valueArea.addEventListener('mouseleave', onMouseLeave);
        });

        var jsonEditorStore = {
            panel: null,
            modal: void(0),
            showModal: function (textarea, parent) {
                this.currentTextarea = textarea;
                if (!this.modal) {
                    this.createModal();
                } else {
                    this.setJson();
                }
                this.panel = panel;
                this.modal.style.display = 'block';
            },
            hideModal: function () {
                if (this.modal) {
                    this.modal.style.display = 'none';
                }
            },
            createModal: function () {
                const self = this;
                this.modal = document.createElement('div');
                this.modal.className = 'json-edit-modal';
                this.modal.innerHTML = '<iframe frameBorder="0" src="/static/json-editor/json-editor.html">';
                this.iframe = this.modal.querySelector('iframe');
                this.iframe.onload = function () {
                    self.setJson();
                }
                document.body.appendChild(this.modal);
                return this.modal;
            },
            setJson: function () {
                var value = this.currentTextarea.value;
                this.iframe.contentWindow.setJson(value);
            },
            saveJson: function (value) {
                var node = Y.one("#" + this.currentTextarea.id);
                node.set("value", value);
                var itemNode = Y.one("#" + this.currentTextarea.id).ancestor("tr");
                var index = panel.getItemIndex(itemNode);
                var role = node.ancestor('.J-control-group').get('role');
                panel.data[index][role] = value;
                var originItem = panel.originData[index];
                panel.uiSetModify(itemNode, originItem);
            }
        }
        window['jsonEditorStore'] = jsonEditorStore;
    }

    /* -- json editor end -- */

}, '', {
    //skinnable: true,
    requires: [
        'msgp-config/tpl-version0.0.8',
        'msgp-config/rest-version0.0.2',
        'msgp-config/popup',
        'json',
        'cos-make-getcn',
        'event-valuechange',
        'msgp-service/commonMap',
        'template',
        'msgp-utils/common',
        'msgp-utils/localEdit'
    ]
});