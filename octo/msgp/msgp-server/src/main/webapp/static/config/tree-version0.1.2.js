M.add('msgp-config/tree-version0.1.2', function (Y, NAME) {
    var env = "prod";
    var config = Y.msgp.config;

    var rest = Y.msgp.config.rest;
    var each = Y.Object.each;
    var map = Y.Array.map;
    var reduce = Y.Array.reduce;
    var create = Y.Node.create;

    var tpl = Y.msgp.config.tpl;
    var popup = Y.msgp.config.popup;

    var sub = Y.Lang.sub;
    var curEnv = "prod";
    var wrapper = Y.one('#wrap_config');
    var currentAccessAjax;
    isOnline = false;
    var envList = ["prod", "stage", "test"];

    var TEXT = {
        nodeComfirmDelete: '确定删除节点 {nodeName} 吗，此操作不可逆转',
        nodeTipDeleteSuccess: '删除节点 {nodeName} 成功',
        spacesTipAddSuccess: '添加空间 {spaceName} 成功',
        spacesPromptAdd: '请输入新空间名：'
    };

    var tokenTmp, originToken;

    function Tree(rootNode, container, appkey, configContainer) {
        var self = this;
        self.isOnlineInit();
        self.getSettingAuth();
        configContainer.delegate('click', function () {
            //if (this.hasClass("btn-primary")) {
            //    return;
            //} else {
            configContainer.all('.dyBtn').removeClass('btn-primary');
            this.addClass("btn-primary");
            //}
            var tag = this.getAttribute('value');
            var node;
            if ("all" == tag) {
                node = {
                    spaceName: appkey,
                    nodeName: appkey,
                    isLeaf: true,
                    enableAdd: true,
                    isCell: false
                };
            } else {
                node = {
                    spaceName: appkey,
                    nodeName: appkey + "." + tag,
                    isLeaf: true,
                    enableAdd: true,
                    isCell: false
                };
            }
            env = tag;
            // self.toggleNodeHandler(node);


            var tree = new Y.Tree({
                rootNode: node,
                nodeExtensions: [config.tree.NodeValidatable]
            });
            self.tree = tree;
            self.container = container;
            rootNode = tree.rootNode;
            self.renderRootNode(rootNode);
            self.bindNode();

        }, '.dyBtn');

        configContainer.delegate('click', function () {
            Y.msgp.service.setEnvText('history_env');
            self.showPage('history');
            self.initDatePicker();
            var aArr = wrapper.one('#history_env').all('a');
            aArr.removeClass('btn-primary');
            aArr.each(function (item) {
                if (env === item.getAttribute('value')) {
                    item.simulate('click');
                    item.addClass('btn-primary');
                    return;
                }
            });
        }, '#history_btn');

        configContainer.delegate('click', function () {
            self.showPage('review');
            wrapper.one('#review_type a').simulate('click');
        }, "#pr_reveiw_manage");

        configContainer.delegate('click', function () {
            self.showPage('configset');
            self.setInitReminder();
        }, "#pr_config_settings");

        self.showPage('home');
        //执行一次all
        //self.toggleNodeHandler(rootNode);
        //执行一次prod_btn
        configContainer.one("#prod_btn").simulate('click');


    }

    Tree.prototype = {
        constructor: Tree,
        renderRootNode: function (rootNode) {
            var domNode = create(tpl.rootNode({
                nodeLastName: this.getLastName(rootNode.nodeName),
                isLeaf: rootNode.isLeaf,
                enableAdd: rootNode.enableAdd,
                isCell: rootNode.isCell
            }));
            //绑定一些节点信息
            this.tieNode(rootNode, domNode);
            this.container.setHTML(domNode);
            this.toggleNodeHandler(rootNode);
            rootNode.isFold = true;
            wrapper.one('#dynamic_env_loading').hide();
        },
        //点击节点时获取子节点
        toggleNodeHandler: function (node, e) {
            var self = this;
            wrapper.one('tbody').hide();
            wrapper.one('#dynamic_env_loading').show();

            var $syncProdEle = wrapper.one('#sync-prod-btn');
            if (isOnline && envList.indexOf(env) == 1) {
                $syncProdEle.show();
            } else {
                $syncProdEle.hide();
            }
            var parent = self.getParentNode(e);
            this.getNodeData(node, function () {
                if (node.children) {
                    node.isLeaf = node.children.length === 0;
                }
                if (!node.isLeaf) {
                    if (node.isFold) {
                        self.unfoldNode(node);
                    } else {
                        self.foldNode(node);
                    }
                }
                self.showPanel(node, parent);
            }, parent);
        },
        editNodeHandler: function (node, e) {
            var self = this;
            var parent = this.getParentNode(e);
            this.getNodeData(node, function () {
                self.showPanel(node, parent);
            }, parent);
        },
        showPage: function (type) {
            wrapper.one("#wrapper-sgconfig-migration-body").hide();
            wrapper.one("#wrapper-review").hide();
            wrapper.one("#wrapper-mcc-body").hide();
            wrapper.one("#wrapper-configset").hide();
            wrapper.one("#wrapper-history").hide();
            wrapper.one("#wrapper-filelog").hide();///
            switch (type) {
                case 'review':
                    wrapper.one("#wrapper-review").show();
                    break;
                case 'configset':
                    wrapper.one("#wrapper-configset").show();
                    break;
                case 'history':
                    wrapper.one("#wrapper-history").show();
                    break;
                ///
                case 'filelog':
                    wrapper.one("#wrapper-filelog").show();
                    break;
                case 'home':
                default:
                    wrapper.one("#wrapper-mcc-body").show();
                    break;
            }
        },
        /**
         * /config/space/:spaceName/node/clientsynclog
         */
        synclogHandler: function (node, e) {
            var self = this;
            var pending = 2;
            var parent = this.getParentNode(e);
            this.getNodeData(node, testEnd, parent);

            var successCallback = function (data) {
                node.synclog = data;

                node.synclog.logs = node.synclog.operatorLog;
                testEnd();
            };
            var failureCallback = function (msg) {
                //Y.msgp.config.popup.alert(msg);
                wrapper.one('tbody').show();
                wrapper.one('tbody').setHTML(msg + '<a href="#">重新获取</a>');
                wrapper.one('tbody').setHTML(msg + '&nbsp;<a href="javascript:;" class="get-again">重新获取</a>');
                wrapper.one('tbody').delegate('click', function () { //绑定重新获取事件
                    wrapper.one('#dynamic_env .btn-primary').simulate('click');
                }, '.get-again');
            };
            var url = 'config/space/' + node.spaceName + '/node/clientsynclog';
            var params = {
                nodeName: node.nodeName
            };
            rest.newGet(url, params, successCallback, failureCallback);
            function testEnd() {
                pending--;
                if (pending === 0) {
                    self.showPanel(node, null, 'synclog');
                    //Y.config.win.location.href = Y.config.win.location.href.replace(/#[\s\S]+$/, '') + '#' + 'config-node-synclog-anchor';
                }
            }
        },
        addNodeHandler: function (node, nodeName, nodeTrueName, isSwimlaneGroup, isCellGroup) {
            var successCallback = function () {
                var parent = node.domNode.one('.J-config-tree-toggle');
                self.getNodeData(node, function () {
                    self.unfoldNode(node);
                    self.showPanel(Y.Array.find(node.children, function (item) {
                        return item.nodeName === nodeName;
                    }), parent);
                    popup.alert('添加节点成功', 1000);
                }, null);
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert(msg);
            };
            var self = this;
            var spaceName = node.spaceName;
            var url = 'config/space/' + spaceName + '/node/add';
            var params = Y.JSON.stringify({
                nodeName: nodeTrueName,
                spaceName: spaceName,
                swimlaneGroup: isSwimlaneGroup,
                cellGroup: isCellGroup
            });
            rest.newPost(url, params, successCallback, failureCallback);
        },

        deleteNodeHandler: function (node, parent) {
            var self = this;
            var nodeName = node.nodeName;
            var nodeTrueName = parent && parent._node.nextElementSibling.innerText == "set" ? this.getParentName(nodeName) + "[cell]." + this.getLastName(nodeName) : nodeName;
            var url = 'config/space/' + node.spaceName + '/node/delete';
            var successCallback = function () {
                popup.alert(sub(TEXT.nodeTipDeleteSuccess, {
                    nodeName: self.getClearDeleteNodeName(node.spaceName, nodeName)
                }), 1000);
                node.tree.removeNode(node);
                node.domNode.remove();
                parent.simulate("click");
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert(msg);
            };
            var params = Y.JSON.stringify({
                nodeName: nodeTrueName,
                spaceName: node.spaceName,
                cellGroup: node.isCell
            });
            rest.newDelete(url, params, successCallback, failureCallback);
        },
        //deleteNodeHandler: function(node) {
        //    var nodeName = node.nodeName;
        //    rest.delete('/node/' + nodeName, function() {
        //        popup.alert(sub(TEXT.nodeTipDeleteSuccess, {
        //            nodeName: nodeName
        //        }), 1000);
        //        node.tree.removeNode(node);
        //        node.domNode.remove();
        //    });
        //},
        bindNode: function () {
            var self = this;
            // toggle click
            this.delegateContainer('.J-config-tree-toggle', function(node, e) {
                self.toggleNodeHandler(node, e);
            }, true);
            // add click
            var nodeValidator = new config.tree.NodeValidatable();
            this.delegateContainer('.J-config-tree-controller-add', function (node, e) {
                popup.prompt('添加的节点名称：', function (result) {
                    var isSwimlaneGroup = Y.one("#swimlaneGroup").get("checked");
                    var isCellGroup = Y.one("#cellGroup").get("checked");
                    if (isSwimlaneGroup && isCellGroup) {
                        popup.error('不能同时选定泳道分组和set分组！');
                        return false;
                    }
                    if (!nodeValidator.validateNodeName(result)) {
                        popup.error('添加的节点名称不符合规范，请修改');
                        return false;
                    }
                    var name = node.isCell ? node.nodeName + "[cell]" : node.nodeName;
                    var nodeTrueName = name + '.' + result;
                    var nodeName = node.nodeName + '.' + result;
                    self.addNodeHandler(node, nodeName, nodeTrueName, isSwimlaneGroup, isCellGroup);
                });
            }, true);
            // delete click
            this.delegateContainer('.J-config-tree-controller-delete', function (node, e) {
                var parent = self.getParentNode(e);
                var nodeName = node.nodeName;
                if (node.isRoot()) {
                    popup.alert('根节点不可删除');
                }
                if (node.children.length > 0) {
                    popup.alert('存在子节点，不可删除');
                } else {
                    popup.confirm(sub(TEXT.nodeComfirmDelete, {
                        nodeName: this.getClearDeleteNodeName(node.spaceName, nodeName)
                    }), function () {
                        self.deleteNodeHandler(node, parent);
                    });
                }
            }, true);
            // edit click
            this.delegateContainer('.J-config-tree-controller-update', function(node, e) {
                self.editNodeHandler(node, e);
            }, true);
            this.delegateContainer('#history_btn', function(node, e) {
                self.synclogHandler(node, e);
            }, true);
            //this.delegateContainer('#filelog_btn', this.synclogHandler);///
            this.delegateContainer('.J-config-tree-node', function(node, e) {
                self.editNodeHandler(node, e);
            }, true);
        },
        getClearDeleteNodeName: function (spaceName, nodeName) {
            var self = this;
            var nodeNameSlice = nodeName.slice(spaceName.length + 1, nodeName.length);
            var data = nodeNameSlice.split('.');
            var clearNodeName = spaceName;
            Y.Array.forEach(data, function (item) {
                if ('prod' == item || 'stage' == item) {
                    item = self.getEnvDesc(item);
                }
                clearNodeName += "/" + item;
            });
            return clearNodeName;
        },
        unfoldNode: function (node) {
            var self = this;
            var domNode = node.domNode;
            var domNodeUl = domNode.one('ul');
            var domNodeToggleFa = domNode.one('.J-config-tree-toggle-fa');
            var newDomNodeUl = reduce(node.children, create('<ul></ul>'), function (prev, item) {
                var node = create(tpl.node({
                    nodeLastName: self.getLastName(item.nodeName),
                    isLeaf: item.isLeaf,
                    enableAdd: item.enableAdd,
                    isCell: item.isCell
                }));
                self.tieNode(item, node.one('.J-config-tree-wrapper'));
                prev.append(node);
                item.isFold = true;
                return prev;
            });
            domNodeToggleFa.empty();
            domNodeToggleFa.addClass('fa');
            domNodeToggleFa.replaceClass('fa-caret-right', 'fa-caret-down');
            domNodeUl.replace(newDomNodeUl);
            node.isFold = false;
        },
        foldNode: function (node) {
            var domNode = node.domNode;
            var domNodeUl = domNode.one('ul');
            var domNodeToggleFa = domNode.one('.J-config-tree-toggle-fa');
            domNodeToggleFa.replaceClass('fa-caret-down', 'fa-caret-right');
            domNodeUl.replace(create('<ul></ul>'));
            node.isFold = true;
        },
        tieNode: function (treeNode, domNode) {
            this.setNodeData(treeNode, {
                domNode: domNode
            });
            domNode.setData('treeNode', treeNode);
        },
        /**
         * @param {Tree.Node} node
         * @param {String} plugin
         */
        showPanel: function (node, parentNode, plugin) {
            Y.all('.config-tree-node-current').removeClass('config-tree-node-current');
            node.domNode.one('.J-config-tree-node').addClass('config-tree-node-current');
            var opts = {node: node, parentNode: parentNode};
            if (!this.panel) {
                this.panel = new config.Panel(opts);
                this.panel.render();
            } else {
                this.panel.node = node;
                this.panel.parentNode = parentNode;
                this.panel.initData();
                this.panel.syncUI();
            }
            this.panel.resetVersion(node.version);
            if (!plugin) {
                Y.Array.forEach(Y.Object.keys(this.panel.plugins), function (key) {
                    this.panel[key].hide();
                }, this);
                return;
            }
            if (!this.panel[plugin]) {
                this.panel.plug(config.panel[plugin]);
                this.panel[plugin].render();
            } else {
                this.panel[plugin].syncUI();
            }
        },
        getNodeData: function (node, callback, parent) {
            var self = this;
            var spaceName = node.spaceName;
            var nodeName = node.nodeName;
            var successCallback = function (data) {
                var Node = Y.Tree.Node;
                var children = map(data.childrenNodes, function (item) {
                    item.name = item.cell ? item.name.slice(0, item.name.length-6) : item.name;
                    return new Node(node.tree, {
                        spaceName: spaceName,
                        //path: path + '/' + item.name,
                        nodeName: nodeName + '.' + item.name,
                        isLeaf: item.leaf,
                        enableAdd: item.enableAdd && self.getNodeNameLength(spaceName, nodeName + '.' + item.name) < 6,
                        isCell: item.cell,
                        version: item.version
                    });
                });
                self.setNodeData(node, {
                    data: data.data,
                    children: children,
                    version: data.version
                });
                wrapper.one('#dynamic_env_loading').hide();
                wrapper.one('tbody').show();
                callback();
            };
            var failureCallback = function (msg) {
                wrapper.one('#dynamic_env_loading').hide();
                wrapper.one('tbody').show();
                wrapper.one('tbody').setHTML('<tr><td colspan="4">' + msg + '&nbsp;<a href="javascript:;" class="get-again">重新获取</a></td></tr>');
                wrapper.one('tbody').delegate('click', function () { //绑定重新获取事件
                    wrapper.one('#dynamic_env .btn-primary').simulate('click');

                }, '.get-again');
            };
            //if (typeof(currentAccessAjax) != "undefined") {
            //    currentAccessAjax.abort();
            //}
            var nodeTrueName = this.getNodeTrueName(node, parent);
            rest.newGet('config/space/node/get', {
                appkey: spaceName,
                nodeName: nodeTrueName,
            }, successCallback, failureCallback);
        },
        setNodeData: function (node, data) {
            each(data, function (value, key) {
                node[key] = value;
            });
        },
        getLastName: function (name) {
            var currentEnv = name.slice(name.lastIndexOf('.') + 1);
            return this.getEnvDesc(currentEnv);
        },
        getNodeNameLength: function (spaceName, nodeName) {
            var nodeNameSlice = nodeName.slice(spaceName.length + 1, nodeName.length);
            return nodeNameSlice.split('.').length;
        },
        getParentName: function (nodeName) {
            return nodeName.slice(0, nodeName.lastIndexOf('.'));
        },
        getParentNode: function (e) {
            if (undefined == e) return null;
            var wrapperNode = e.target.ancestor(".J-config-tree-wrapper").ancestor(".J-config-tree-wrapper");
            return wrapperNode ? wrapperNode.one(".J-config-tree-toggle") : null;
        },
        getNodeTrueName: function (node, parent) {
            var nodeName = node.nodeName;
            var nodeTrueName;
            if (node.isCell) {
                nodeTrueName = nodeName + "[cell]"
            }
            else {
                nodeTrueName = parent && parent._node.nextElementSibling.innerText == "set" ? this.getParentName(nodeName) + "[cell]." + nodeName.slice(nodeName.lastIndexOf('.') + 1) : nodeName;
            }
            return nodeTrueName;
        },
        delegateContainer: function (selector, handler, isPassEvent) {
            var self = this;
            this.container.delegate('click', function (e) {
                var node = e.target.ancestor('.J-config-tree-wrapper').getData('treeNode');
                if (undefined === isPassEvent) {
                    handler.call(self, node);
                } else {
                    handler.call(self, node, e);
                }

                e.stopImmediatePropagation();
            }, selector);
        },
        destroy: function () {
            this.panel.destroy();
            this.container.empty();
            this.container.detachAll('click');
        },
        setInitReminder: function () {
            var self = this;
            wrapper.delegate('click', function () {
                wrapper.all('#reminder_one_radio').set("checked", false);
                this.set("checked", true);
            }, '#reminder_one_radio');
            wrapper.delegate('click', function () {
                wrapper.all('#version_one_radio').set("checked", false);
                this.set("checked", true);
            }, '#version_one_radio');
            wrapper.delegate('click', function () {
                wrapper.all('#setvalue_auth_one_radio').set("checked", false);
                this.set("checked", true);
                if ('true' == this.get('value')) {
                    if (undefined != tokenTmp) {
                        wrapper.all('#setvalue_auth_one_text').set('value', tokenTmp);
                    }
                    wrapper.all('#setvalue_auth_menu').show();

                } else {
                    wrapper.all('#setvalue_auth_menu').hide();
                }

            }, '#setvalue_auth_one_radio');
            wrapper.delegate('click', function () {
                self.showPage('home');
                wrapper.one('#dynamic_env .btn-primary').simulate('click');
            }, '#reminder_return');
            wrapper.delegate('click', function () {
                self.setReminderSave();
            }, '#btn_config_save');
            wrapper.delegate('click', function () {
                self.makeConfigToken();
            }, '#make_config_token');

            var reminder = wrapper.all('#reminder_one_radio');
            var version = wrapper.all('#version_one_radio');
            var enableAuth = wrapper.all('#setvalue_auth_one_radio');
            var authToken = wrapper.all('#setvalue_auth_one_text');
            var appkey = Y.one('#apps_select').get('value');
            var url = '/serverOpt/config/settings/' + appkey + '/settings/data';
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.success) {
                            var data = res.data;
                            var rflag = true;
                            var vflag = true;
                            var aflag = true;
                            var bflag = true;
                            Y.Array.forEach(data, function (item) {
                                if ('enableXMAlert' === item.key) {
                                    if ('true' === item.value) {
                                        reminder.item(0).set("checked", false);
                                        reminder.item(1).set("checked", true);
                                        rflag = false;
                                    } else {
                                        reminder.item(0).set("checked", true);
                                        reminder.item(1).set("checked", false);
                                        rflag = false;
                                    }
                                }
                                if ('enableCheckVersion' === item.key) {
                                    if ('true' === item.value) {
                                        version.item(0).set("checked", false);
                                        version.item(1).set("checked", true);
                                        vflag = false;
                                    } else {
                                        version.item(0).set("checked", true);
                                        version.item(1).set("checked", false);
                                        vflag = false;
                                    }
                                }
                                if ('enableAuth' === item.key) {
                                    if ('true' === item.value) {
                                        enableAuth.item(0).set("checked", false);
                                        enableAuth.item(1).set("checked", true);
                                        aflag = false;
                                    } else {
                                        enableAuth.item(0).set("checked", true);
                                        enableAuth.item(1).set("checked", false);
                                        aflag = false;
                                        bflag = false;
                                    }
                                }
                                if ('authToken' === item.key) {
                                    tokenTmp = item.value;
                                }
                                if ('originToken' === item.key) {
                                    originToken = item.value;
                                }
                            });
                            if (rflag) {
                                reminder.item(0).set("checked", false);
                                reminder.item(1).set("checked", true);
                            }
                            if (vflag) {
                                version.item(0).set("checked", false);
                                version.item(1).set("checked", true);
                            }
                            if (aflag) {
                                //zk没有对应的enableAuth
                                enableAuth.item(0).set("checked", true);
                                enableAuth.item(1).set("checked", false);
                                wrapper.all('#setvalue_auth_menu').hide();
                            } else {
                                if (bflag) {
                                    //开权限了
                                    authToken.set("value", tokenTmp);
                                    wrapper.all('#setvalue_auth_menu').show();
                                } else {
                                    //确实没开权限
                                    wrapper.all('#setvalue_auth_menu').hide();
                                }

                            }
                        }
                    }
                }
            });
        },

        getSettingAuth: function () {
            var appkey = Y.one('#apps_select').get('value');
            var url = '/serverOpt/' + appkey + '/config/auth';
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (!res.isSuccess || false == res.data) {
                            wrapper.all('#config_auth_div').hide();
                        } else {
                            wrapper.all('#config_auth_div').show();
                        }
                    }
                }
            });
        },

        makeConfigToken: function () {
            var appkey = Y.one('#apps_select').get('value');
            var setvalueAuthVal = wrapper.one('#setvalue_auth_one_radio:checked').get('value');
            var setvalueTokenVal = ('true' == setvalueAuthVal) ? wrapper.one('#setvalue_auth_one_text').get('value') : "";
            if ('' == setvalueTokenVal) {
                Y.msgp.utils.msgpHeaderTip('error', "请填写token，token不能为空", 3);
            } else {
                var url = '/serverOpt/' + appkey + '/config/token';
                Y.io(url, {
                    method: 'get',
                    data: {
                        token: setvalueTokenVal
                    },
                    on: {
                        success: function (id, o) {
                            var res = Y.JSON.parse(o.responseText);
                            if (res.success && null != res.data) {
                                originToken = setvalueTokenVal;
                                wrapper.one('#setvalue_auth_one_text').set('value', res.data);
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', "生成密文失败，请重试", 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                        }
                    }
                });
            }
        },

        setReminderSave: function () {
            var appkey = Y.one('#apps_select').get('value');
            var reminderVal = wrapper.one('#reminder_one_radio:checked').get('value');
            var versionVal = wrapper.one('#version_one_radio:checked').get('value');
            var setvalueAuthVal = wrapper.one('#setvalue_auth_one_radio:checked').get('value');
            var setvalueTokenVal = wrapper.one('#setvalue_auth_one_text').get('value');
            setvalueTokenVal = setvalueTokenVal.replace(/\s+/g, "")
            var url = '/serverOpt/config/settings/' + appkey + '/settings/update/';
            //console.log(originToken);
            //console.log(tokenTmp);
            if ('true' === setvalueAuthVal && (undefined == setvalueTokenVal || '' == setvalueTokenVal)) {
                Y.msgp.utils.msgpHeaderTip('error', "未生成密文，请点击生成密文", 3);
                return;
            }
            originToken = (undefined == originToken) ? "" : originToken;

            Y.io(url, {
                method: 'POST',
                data: Y.JSON.stringify({
                    enableXMAlert: reminderVal,
                    enableCheckVersion: versionVal,
                    enableAuth: setvalueAuthVal,
                    authToken: setvalueTokenVal,
                    originToken: originToken
                }),
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.success) {
                            Y.msgp.config.popup.alert(('true' === reminderVal ? '启用' : '禁用') + '大象消息&nbsp;' +
                                ('true' === versionVal ? '启用' : '禁用') + '覆盖校验&nbsp;' +
                                ('true' === setvalueAuthVal ? '启用' : '禁用') + 'API修改配置鉴权设置成功', 1000);
                        }
                    }
                }
            });
        },
        initDatePicker: function () {
            var self = this;
            var startInput = wrapper.one('#log_start_time');
            var endInput = wrapper.one('#log_end_time');
            var now = new Date();
            var yesterday = new Date(now - 7 * 24 * 60 * 60 * 1000);
            var sdate = new Y.mt.widget.Datepicker({
                node: startInput,
                showSetTime: true
            });
            sdate.on('Datepicker.select', function () {
                self.dateSelect();
            });
            startInput.set('value', Y.mt.date.formatDateByString(yesterday, 'yyyy-MM-dd hh:mm:ss'));
            var edate = new Y.mt.widget.Datepicker({
                node: endInput,
                showSetTime: true
            });
            edate.on('Datepicker.select', function () {
                self.dateSelect();
            });
            endInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
        },
        dateSelect: function () {
            var aArr = wrapper.one('#history_env').all('a'),
                curEnv = wrapper.one('#history_env a.btn-primary').getAttribute('value');
            aArr.each(function (item) {
                if (curEnv === item.getAttribute('value')) {
                    item.removeClass('btn-primary');
                    item.simulate('click');
                    return;
                }
            });
        },

        isOnlineInit: function () {
            var url = '/common/online';
            $.ajax({
                type : "get",
                url : url,
                async : false,//取消异步
                success: function (res) {
                    if (res.isSuccess) {
                        isOnline = res.data;
                    }
                }
            });
        },

        getEnvDesc: function (currentEnv) {
            var envDesc;
            if ('prod' == currentEnv) {
                envDesc = (true == isOnline)? 'prod' : 'dev';
            } else if ('stage' == currentEnv) {
                envDesc = (true == isOnline) ? 'stage' : 'ppe';
            } else {
                envDesc = currentEnv;
            }
            ;
            return envDesc;
        },
    };

    Y.mix(Tree, config.tree);
    Y.namespace('msgp.config').tree = Tree

}, '', {
    requires: [
        'tree',
        'collection',
        'node',
        'msgp-config/rest-version0.0.2',
        'msgp-config/panel-version0.1.6',
        'msgp-config/tpl-version0.0.8',
        'msgp-config/popup',
        'msgp-config/validatable-version0.1.0',
        'msgp-service/commonMap'
    ]
});
