/**
 * 供各个app或页面使用的小组件
 * @module w-base
 */
M.add('w-base', function(Y){

    var $N = Y.Node;
    var L = Y.Lang;
    var isFunction = L.isFunction;
    var $Util = Y.mt.util;

    Y.namespace('mt.widget');

    /**
     * 异步POST按钮
     * @class ButtonToPost
     * @constructor
     * @param {Node} ndBtn
     * @param {Object} params
     * @namespace mt.widget
     */
    Y.mt.widget.ButtonToPost = function(ndBtn, config) {
        var _this = this;
        //default参数, 不需要发送到后台的数据
        var defaultConf = {
            success: 'replace',
            action: '',
            method: 'post',
            successHandler: function (res) {
                _this._handleSuccess(res);
            },
            confirm: '',
            url: '',
            query: {}
        };

        this.EVENT_NAME = {
            success: 'ButtonToPost.success',
            failure: 'ButtonToPost.failure',
            start: 'ButtonToPost.start',
            postAll: 'ButtonToPost.postAll'
        };

        //能通过该方式改变实例的配置, 而不用每次都新建一个实例
        this.config = config;
        this.defaultConf = defaultConf;
        this._handle = {};
        this.async = null;
        this.ndBtn = ndBtn;

        Y.mix(config, defaultConf);

        if (!config.action) return;

        if (!ndBtn.get('id')) {
            ndBtn.set('id', Y.guid());
        }

        this._handle['click'] = ndBtn.on('click', function() {
            if (_this.async && _this.async.isInProgress()) return;
            if (config['confirm']) {
                if (window.confirm(config['confirm'])) {
                    _this._sendRequest();
                }
            } else {
                _this._sendRequest();
            }
        });

        Y.Global.on(this.EVENT_NAME['postAll'], function(data) {
            if (Y.mt.lang.inArray(ndBtn.get('id'), data)) {
                _this._sendRequest();
            }
        });
    };
    var btnProto = Y.mt.widget.ButtonToPost.prototype;
    /**
     * 处理成功回调事件
     * @private
     * @method _handleSuccess
     * @param {res} ajax的返回资源
     * @return
     */
    btnProto._handleSuccess = function (res) {
        var config = this.config;
        var success = config.success;
        if (success === 'replace') {
            var ndSpan = $N.create('<span>' + res.msg + '</span>');
            this.ndBtn.replace(ndSpan);
        } else if (success === 'reload') {
            // 重新加载页面
            this._reloadPage();
        } else if (success === 'redirect' && config.url){
            // redirect页面
            this._redirectPage();
        }
    };
    /**
     * 重新加载页面
     * @private
     * @method _reloadPage
     * @return
     */
    btnProto._reloadPage = function () {
        var win = Y.config.win;
        if (win.parent.main) {
            win.parent.main.location.reload() ;
        } else {
            win.location.reload();
        }
    };
    /**
     * redirect页面
     * @private
     * @method _redirectPage
     * @return
     */
    btnProto._redirectPage = function () {
        var win = Y.config.win;
        win.location.href = this.config.url;
    };
    /**
     * 获取提交的数据
     * 由于config中没有明确区分哪些参数是需要post到后台的
     * 要对参数去重，将不需要发送到后台的部分区别出来
     * @private
     * @method _getPostData
     * @return
     */
    btnProto._getPostData = function () {
        var config = this.config;
        var defaultConf = this.defaultConf;
        var postData = {};
        //去掉不需要发送到后台的值
        for(var p in config) {
            if(defaultConf[p] === undefined || defaultConf[p] === null)  {
                postData[p] = config[p];
            }
        }
        //不为空的情况和postData合并
        if(config.query && !Y.Object.isEmpty(config.query)) {
            Y.mix(postData, config.query, true);
        }
        return postData;
    };
    /**
     * 响应ajax response
     * @private
     * @method _ajaxSuccessHandle
     * @param {id}
     * @param {o}
     * @param {msg}
     * @return
     */
    btnProto._ajaxSuccessHandle = function (id, o, msg) {
        var ndBtn = this.ndBtn;
        var EVENT_NAME = this.EVENT_NAME;
        var config = this.config;
        var res = $Util.getEvalRes(o, msg);
        if (res.status) {
            if (config.successHandler) {
                config.successHandler(res);
            }
            Y.Global.fire(EVENT_NAME['success'], {res: res});
        } else {
            Y.Global.fire(EVENT_NAME['failure'], {id: ndBtn.get('id'), res: res});
            Y.config.win.alert(res.msg);
        }
    };
    /**
     * 发送请求
     * @private
     * @method _sendRequest
     * @return
     */
    btnProto._sendRequest = function () {
        var EVENT_NAME = this.EVENT_NAME;
        var config = this.config;
        var msg = '权限不够或者网络有问题，请稍后重试。';
        Y.Global.fire(EVENT_NAME['start'], {config: config});
        if (this.async && this.async.isInProgress()) {
            this.async.abort();
        }
        // 发送ajax请求
        this._sendAjaxRequest(msg);
    };
    /**
     * 发送ajax请求
     * @private
     * @method _sendAjaxRequest
     * @param {msg} 提示消息
     * @return
     */
    btnProto._sendAjaxRequest = function (msg) {
        var _this = this;
        var EVENT_NAME = this.EVENT_NAME;
        var config = this.config;
        var ndBtn = this.ndBtn;
        this.async = Y.io(config.action, {
            method: config.method,
            data: this._getPostData(),
            on: {
                success: function(id, o) {
                    _this._ajaxSuccessHandle(id, o, msg);
                },
                failure: function(id, o) {
                    if (o.statusText && o.statusText === 'abort') return;
                    Y.Global.fire(EVENT_NAME['failure'], {id: ndBtn.get('id'), res: {status:0, msg: msg}});
                    Y.config.win.alert(msg);
                }
            }
        });
    };
    /**
     * 验证信息提示条: 在可见区域最上面显示提示信息
     * @class HeaderTip
     * @constructor
     * @namespace mt.widget
     * @param {String} type: 显示信息的类型：success|error
     * @param {String} msg: 显示信息的类型：msg
     * @param {Selector|String} focus： 是否显示完信息后让某元素进入focus的状态
    */
    var HeaderTip = function(type, msg, focus, fixed) {
        var tipContainer;

        if(!msg) return null;
        if (!type) type = 'default';
        if(typeof fixed === 'undefined') {
            fixed = true;
        }

        tipContainer = Y.one('.widget-headertip');
        if (tipContainer) {
            tipContainer.remove();
        }
        HeaderTip.CSS_NAME['type'] = HeaderTip.CSS_NAME['container'] + '-' + type;
        if (type === 'success') {
            HeaderTip.CSS_NAME['iconType'] = 'check';
        } else if (type=== 'error') {
            HeaderTip.CSS_NAME['iconType'] = 'times';
        } else if (type=== 'warn') {
            HeaderTip.CSS_NAME['iconType'] = 'exclamation';
        } else {
            HeaderTip.CSS_NAME['iconType'] = 'info';
        }
        HeaderTip.CSS_NAME['msgContent'] = msg;
        tipContainer = Y.Node.create(Y.Lang.sub(HeaderTip.TEMPlATE['container'], HeaderTip.CSS_NAME));

        tipContainer.on('click', function() {
            tipContainer.remove();
        });

        if(focus) {
            var ndFocus = focus instanceof Y.NodeList ? focus.item(0) : Y.one(focus);
            if(ndFocus) {
                window.scrollTo(0, ndFocus.getXY()[1] - 80);
                ndFocus.focus();
            }
        }
        Y.one(document.body).prepend(tipContainer);
        if( ! fixed) {
            tipContainer.setStyles({
                position: 'relative'
            });
        }
        return tipContainer;
    };
    HeaderTip.CSS_NAME = {
        container: 'widget-headertip',
        close: 'widget-headertip-close',
        sysmsg: 'widget-headertip-sysmsg'
    };
    HeaderTip.TEMPlATE = {
        container: '<div class="{container}">'+
                        '<div class="{sysmsg} {type}">'+
                            '<i class="fa fa-{iconType}-circle"></i>'+
                            '<span class="{close}">×</span>'+
                            '<p>{msgContent}</p>'+
                        '</div>'+
                    '</div>'
    };

    Y.mt.widget.HeaderTip = HeaderTip;

    /**
    * commonDialog
    * @class CommonDialog
    * @constructor
    * @namespace mt.widget
    * @param {String} width:弹出层的宽度
    * @param {String} title:弹出层的title
    * @param {Node|String} content:弹出层的内容
    * @param {Node} target 在那个目标下面弹出层
    * @param {String} theme 使用的颜色方案 'default/blue/sea/orange/green/red/black..'
    */
    Y.mt.widget.CommonDialog = function(conf) {
        if(!conf) return;
        this.config = {
            content: '请定制信息',
            title: '',
            width: '600',
            id: Y.guid(),
            method:'post',
            target: 'center',
            canDrag: true,
            closeMask: true,
            mask: true,
            theme: 'default'
        };
        this.ndDoc = conf.doc || Y.one(document.body);
        this.ndContainer = null;

        this.setConfig(conf);
        this.init();
    };
    Y.mt.widget.CommonDialog.prototype = {
        zIndex: 10,
        setConfig:function(conf){
            Y.mix(this.config, conf, true);
            if(conf.target instanceof Y.Node && Y.Lang.isString(conf.target)) {
                this.config.target = Y.one(conf.target);
            }
        },
        init: function(){
            //存在则返回
            if(this.ndContainer) {
                this.zIndex++;
                this.ndContainer.setStyle('zIndex', this.config.zIndex || this.zIndex);
                return this.ndContainer;
            } else {
                this.ndContainer = this.createDialog();
            }
            //dialog弹出来之前的处理,如事件绑定
            this.ndDoc.appendChild(this.ndContainer);
            this.bindUI();
            return false;
        },
        bindUI: function() {
            //mask插件
            this.addMaskModule();
            //drag插件
            this.addDragPlugin();
            //显示前处理
            if (this.config.beforeShow) {
                this.config.beforeShow(this);
            }
            //增加closehandler
            this.addCloseHandler();
            this.addESCCloseHandler();
            //绑定默认确认取消按钮事件
            var config = this.config;
            if (config.btn && config.btn.enabled !== false) {
                this._addBtnHandle(config.btn);
            }
        },
        /**
         * 增加默认确认取消按钮时间
         * @private
         * @method _addBtnHandle
         * @param {}
         * @return {}
         */
        _addBtnHandle: function (btn) {
            var _this = this;
            var ndBtnBox = this.ndContainer.one(".commonDialog-btn-box");
            ndBtnBox.delegate("click", function () {
                var result = {keepOpening: false};
                if (this.test(".commonDialog-btn-pass")) {
                    if (typeof btn.pass === "function") {
                        result.keepOpening = btn.pass(this, _this.ndContainer);
                    }
                    _this.fire("pass", {btn: this, content: _this.ndContainer, result: result});
                } else {
                    if (typeof btn.unpass === "function") {
                        result.keepOpening = btn.unpass(this, _this.ndContainer);
                    }
                    _this.fire("unpass", {btn: this, content: _this.ndContainer, result: result});
                }
                if (!result.keepOpening) {
                    _this.close();
                }
            }, ".commonDialog-btn-pass, .commonDialog-btn-unpass");
        },
        /**
         * 注册ESC关闭事件
         * @method addESCCloseHandler
         */
        addESCCloseHandler: function() {
            var _this = this;
            Y.one(document).on('keyup',function(event){
                if( event.keyCode === 27 ) {
                    event.halt();
                    if( _this.ndContainer.getStyle("display") === "block" ) {
                        _this.close();
                    }
                }
            });
        },
        /**
         * 绑定拖拽功能
         * @method addDragPlugin
         */
        addDragPlugin: function() {
            var _this = this;
            if(this.config.canDrag) {
                Y.use("dd-plugin", function() {
                    _this.plugDrag();
                });
            }
        },
        /**
         * 添加遮罩层模块
         * @method addMaskModule
         * @param {Function} callbackFn
         */
        addMaskModule: function(callbackFn) {
            var _this = this;
            if(this.config.mask) {
                Y.use("p-mask", function(Y) {
                    _this.pluginMask = Y.mt.plugin.Mask;
                    if (callbackFn) {
                        callbackFn();
                    }
                });
            }
        },
        /**
         * 获取关闭Icon
         * @method getCloseIcon
         * @return {Node}
         */
        getCloseIcon: function() {
            return this.ndContainer.one('.close');
        },
        /**
         * 获取body
         * @method getBody
         * @return {Node}
         */
        getBody: function() {
            return this.ndContainer.one('.body');
        },
        /**
         * 获取Header
         * @method getHeader
         * @return {Node}
         */
        getHeader: function() {
            return this.ndContainer.one('h3');
        },
        /**
         * 获取boundingBox
         * @method getBoundingBox
         * @return {Node}
         */
        getBoundingBox: function() {
            return this.ndContainer;
        },
        /**
         * 绑定DragPlugin
         * @method plugDrag
         * @return {Node}
         */
        plugDrag: function() {
            var DDM = Y.DD.DDM;
            DDM.on('ddm:start', function(e) {
                if(e.target._pg === null) {
                    e.target._createPG();
                }
            });
            this.dialogDrag = this.ndContainer.plug(Y.Plugin.Drag);
            if(this.config.title !== "none") {
                this.ndContainer.dd.addHandle("h3");
            } else {
                this.ndContainer.dd.addHandle('.body');
            }
        },
        /**
         * 取消绑定DragPlugin
         * @method unPlugDrag
         * @return {Node}
         */
        unPlugDrag: function() {
            if (this.ndContainer.dd) {
                this.ndContainer.dd.removeHandle("h3");
            }
            this.ndContainer.unplug(this.dialogDrag);
        },
        /**
         * 显示Dialog
         * @method show
         */
        show: function() {
            var _this = this;
            if(!this.pluginMask && this.config.mask) {
                this.addMaskModule(function() {
                    _this._showBase();
                });
            } else {
                this._showBase();
            }
        },
        /**
         * showBase
         * @method unPlugDrag
         */
        _showBase: function(cb) {
            var conf = this.config,
                content = conf.content,
                ndContainerBody = this.ndContainer.one('.body'),
                innerIframe = null,
                height,
                callback;
            this.ndDoc.plug(this.pluginMask);
            this.ndContainer.show();
            Y.mt.io.loading(ndContainerBody,"", false);
            if(conf.scroll) {
                height = conf.height ? (parseInt(conf.height, 10) - 40) + 'px' : '500px';
                ndContainerBody.setStyles({ overflow:"hidden", maxHeight: "400px", overflowY:"auto"});
            }
            if(content && !Y.Lang.isString(content)) {
                content.show();
            } else if(conf.url && !ndContainerBody.one('iframe')) {
                innerIframe = this.createIframe(conf.url, conf.height);
                ndContainerBody.appendChild(innerIframe);
            }

            Y.mt.widget.util.setPosition(this.ndContainer, conf.target, null, this.ndDoc);

            callback = Y.bind(function() {
                if (cb && isFunction(cb)) {
                    cb.call(this);
                }
            }, this);

            if(conf.url) {
                Y.on('load', function() {
                    callback();
                }, this.ndContainer.one('iframe'));
            } else {
                callback();
            }

            Y.mt.io.clearLoading(ndContainerBody);
        },
        /**
         * 设置title
         * @method setTitle
         * @param {String} title
         */
        setTitle: function(title) {
            this.ndContainer.one('.title').setHTML(title);
        },
        /**
         * 设置嵌套iframe的Url
         * @method setUrl
         * @param {String} url
         */
        setUrl: function(url) {
            var ndIframe = this.ndContainer.one('iframe');
            if (ndIframe) {
                ndIframe.remove();
            }
            this.ndContainer.one('.body').appendChild(this.createIframe(url, this.config.height));
        },
        /**
         * 设置Diag的Content
         * @method setContent
         * @param {Node|String} content
         * @param {Function} cb 回调函数
         */
        setContent: function(content, cb) {
            this.ndContainer.one('.body').setHTML("");
            this.ndContainer.one('.body').appendChild(content);
            if (cb) {
                cb.call(this);
            }
        },
        /**
         * 设置Diag的width
         * @method setWidth
         * @param {Number} width
         */
        setWidth: function(width) {
            this.ndContainer.setStyle("width", width+'px') ;
        },
        /**
         * 设置Diag的height
         * @method setHeight
         * @param {Number} height
         */
        setHeight: function(height) {
            var ndContainer = this.ndContainer;
            var ndIframe = ndContainer.one('iframe');
            ndContainer.setStyle("height", height+'px');
            if (ndIframe) {
                ndIframe.setStyle("height", (height - 50) + "px");
            }
        },
        /**
         * iframe 自适应高度
         * @method syncHeight
         * @param {Number} extraHeight 额外添加的高度
         */
        syncHeight: function(extraHeight) {
            var ndIframe = this.ndContainer.one('iframe'),
                style, doc, elIframe,
                resizeFn = function() {
                    style = elIframe.style;
                    doc = elIframe.contentWindow.document.body;
                    extraHeight = parseInt(extraHeight, 10);
                    if (!extraHeight) {
                        extraHeight = 0;
                    }
                    style.height = (extraHeight + doc.scrollHeight) + 'px';
                };
            if (!ndIframe) {
                return;
            }
            elIframe = ndIframe._node;
            //ie 的onload 事件有问题 需要用 attachEvent绑定
            if (elIframe.attachEvent) {
                elIframe.attachEvent("onload", function(){
                    resizeFn();
                });
            } else {
                elIframe.onload = function() {
                    resizeFn();
                };
            }
        },
        /**
         * 构建head && body模板
         * @method buildTemplate
         * @return {String}
         */
        buildTemplate: function() {
            var config = this.config;
            var template = '';
            var bodyTemp = '<div class="body"></div>';
            var footerTemp = '<div class="footer"></div>';

            if(config.title !== "none") {
                template =  '<div class="head"><h3 class="title">'+config.title +'</h3><span class="close">×</span></div>' + bodyTemp + footerTemp;
            } else {
                template = '<div class="head"><span class="close">×</span></div>' + bodyTemp + footerTemp;
            }
            return template;
        },
        /**
         * 初始化容器
         * @method createDialog
         * @return {Node}
         */
        createDialog : function(){
            var config = this.config,
                dyncForm = null,
                tempContent = this.config.url ? "" : config.content,
                tpl = '<div class="common-popdialog theme-' + config.theme +
                    (config.title === 'none' ? ' no-title' : '') +
                    (config.btn ? '': ' no-btn') + '"></div>',
                ndContainer = $N.create(tpl);

            ndContainer.hide();
            if (config.id) {
                ndContainer.set("id", config.id);
            }
            var paddingLeftRight = 15;
            var width = parseInt(config.width, 10) + 2 * paddingLeftRight;
            ndContainer.setStyles({width: width + 'px', zIndex:this.config.zIndex || this.zIndex});
            if (config.height) {
                ndContainer.setStyle('height', config.height+'px');
            }

            //create header and body
            var template = this.buildTemplate();
            ndContainer.appendChild(template);
            if(config.actionUrl) {
                dyncForm = Y.Node.create('<form action="'+ config.actionUrl +'" method="'+ config.method +'">');
                dyncForm.appendChild(config.content);
                tempContent = dyncForm;
            }
            ndContainer.one('.body').appendChild(tempContent);
            if (config.btn && config.btn.enabled !== false) {
                // 生成确认、取消按钮
                var ndBtnBox = this._buildButton(config.btn);
                ndContainer.one('.footer').append(ndBtnBox);
            }

            return ndContainer;
        },
        /**
         * 生成确认、取消按钮
         * @private
         * @method _buildButton
         * @param {Object} btn
         * @return
         */
        _buildButton: function (btn) {
            var passNode = btn.passNode || "button";
            var unpassNode = btn.unpassNode || "button";
            var passName = btn.passName || "确定";
            var unpassName = btn.unpassName || "取消";
            var passClass = btn.passClass || "btn-primary";
            var unpassClass = btn.unpassClass || "";
            var align = btn.align || "center";
            var passTemplate;
            var unpassTemplate;
            var href;
            var target = "";
            if (passNode === "button") {
                passTemplate = "<input class='btn "+passClass+" commonDialog-btn-pass' type='button' value='"+passName+"'>";
            } else {
                href = btn.passHref || 'javascript:void(0);';
                if (btn.passTarget) {
                    target = " target='"+btn.passTarget+"'";
                }
                passTemplate = "<a href='"+ href +"' class='commonDialog-btn-pass "+passClass+"' "+target+">"+passName+"</a>";
            }
            if (unpassNode === "button") {
                unpassTemplate = "<input class='btn "+unpassClass+" commonDialog-btn-unpass' type='button' value='"+unpassName+"'>";
            } else {
                href = btn.unpassHref || 'javascript:void(0);';
                if (btn.unpassTarget) {
                    target = " target='"+btn.unpassTarget+"'";
                }
                unpassTemplate = "<a href='"+ href +"' class='commonDialog-btn-unpass "+unpassClass+"' "+target+">"+unpassName+"</a>";
            }
            var ndBtnBox = $N.create('<div class="commonDialog-btn-box" style="text-align:' + align + '"></div>');
            ndBtnBox.appendChild(passTemplate+unpassTemplate);
            return ndBtnBox;
        },
        /**
         * 创建嵌入页面的iframe
         * @method createIframe
         * @return {String}
         */
        createIframe: function(url, height) {
            if(!height) height = 400;
            return "<iframe name='_inner_dlg_frame' style='overflow-x:hidden; width:100%;height:" + (height - 50) + "px;margin:0;padding:0' frameborder='0' src='" + url + "' />";
        },
        /**
         * 关闭按钮注册事件
         * @method addCloseHandler
         */
        addCloseHandler: function(){
            var _this = this;
            Y.on('click', function(e) {
                e.halt();
                _this.close();
            }, _this.ndContainer.one('.close'));
        },
        /**
         * 关闭Dialog
         * @method close
         * @param {String} selector
         */
        close: function(selector){
            //关闭回调和事件
            if (this.config.closeCallback) {
                this.config.closeCallback(this);
            }
            Y.Global.fire("Dialog.close", this);
            if(selector && Y.one(selector)) {
                Y.one(selector).hide();
            } else {
                this.ndContainer.hide();
            }
            //关闭回调和事件
            if (this.config.closeMask) {
                this.ndDoc.unplug(this.pluginMask);
            }
            //清除掉弹出层上的其它控件
            if (this.ndDoc.simulate) {
                this.ndDoc.simulate('click');
            }
        },
        destroy: function () {
            this.getBoundingBox().detach();
            this.getBoundingBox().remove();
        }
    };
    Y.augment(Y.mt.widget.CommonDialog, Y.EventTarget);
}, '1.0.0', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-form',
        'datatype-number',
        'w-core',
        'w-select-cascade',
        'io',
        'node',
        'p-mask',
        'dd',
        'oop',
        'event-custom'
    ],
    skinnable: true
});
 
