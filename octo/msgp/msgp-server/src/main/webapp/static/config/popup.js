M.add('msgp-config/popup', function (Y, NAME) {
    var config = Y.msgp.config;

    var popupTypes = [ 'alert', 'confirm', 'prompt' ];

    var gConf = {
        width: 400,
        centered: true,
        visible: false,
        modal: false,
        render: true,
        zIndex: 9
    };
    var popup = new Y.Panel(gConf);
    popup.get('boundingBox').addClass('yui3-skin-cos');

    popup.addAttr('popupType', {
        value: 'alert',
        validator: function(newVal) {
            return Y.Array.indexOf(popupTypes, newVal) !== -1;
        }
    });

    popup.after('popupTypeChange', function(e) {
        if (this.closeTimer) {
            this.closeTimer.cancel();
        }
        if (e.newVal !== 'alert') {
            this.uiSetAlert();
        }
        this.uiSetPopUp(e.newVal, e.prevVal);
    });

    popup.uiSetPopUp = function(popupType, lastPopupType) {
        if (typeof popupType === 'undefined') {
            popupType = this.get('popupType');
        }

        var boundingBox = this.get('boundingBox');
        if (typeof lastPopupType !== 'undefined') {
            boundingBox.removeClass(this.getClassName(lastPopupType));
        }
        boundingBox.addClass(this.getClassName(popupType));

        this.set('headerContent', popupType);
        var headerNode = this.getStdModNode(Y.WidgetStdMod.HEADER);
        if (popupType === 'alert') {
            headerNode.hide();
        } else {
            headerNode.show();
        }
    };

    function returnTrue() { return true; }

    popup.callback = returnTrue;

    var alertButtons = {
        footer: [
            {
                name: 'close',
                label: '关闭',
                action: 'hide',
                classNames: 'yui3-button-close'
            }
        ]
    };

    popup.alert = function(msg, timer) {
        this.set('popupType', 'alert');
        if (timer) {
            this.closeTimer = Y.later(timer, Y, Y.bind(this.hide, this));
            this.set('buttons', []);
        } else {
            this.set('buttons', alertButtons);
        }
        this.uiSetAlert(msg);
        this.show();
    };

    popup.uiSetAlert = function(msg) {
        var boundingBox = this.get('boundingBox');
        var errorClass = this.getClassName('error');
        if (arguments.length === 0) {
            if (boundingBox.hasClass(errorClass)) {
                boundingBox.removeClass(errorClass);
            }
        } else {
            this.get('boundingBox').addClass(errorClass);
            this.set('bodyContent', msg);
        }
    };

    var buttons = {
        footer: [
            {
                name: 'proceed',
                label: '确定',
                action: 'onOK',
                classNames: 'btn btn-primary'
            },
            {
                name: 'cancel',
                label: '取消',
                action: 'onCancel',
                classNames: 'btn'
            }
        ]
    };

    popup.confirm = function(msg, callback) {
        this.set('popupType', 'confirm');
        this.set('buttons', buttons);
        this.callback = callback;
        this.uiSetConfirm(msg);
        this.show();
    };

    popup.uiSetConfirm = function(msg) {
        this.set('bodyContent',
                '<div class="' + this.getClassName('popup-confirm') + '">' +
                msg +
                '</div>');
    };

    popup.prompt = function(label, callback) {
        this.set('popupType', 'prompt');
        this.set('buttons', buttons);
        this.callback = function() {
            var result = Y.Lang.trim(this.get('contentBox')
                .one('.' + this.getClassName('popup-prompt') + ' input')
                .get('value'));
            return callback(result);
        };
        this.uiSetPrompt(label);
        this.show();
    };

    popup.uiSetPrompt = function(label) {
        var guid = Y.guid();
        this.set('bodyContent',
                '<p class="' + this.getClassName('popup-prompt')+ '">' +
                '  <label for="' + guid + '">' +
                '  ' + label +
                '  </label>' +
                '<input id="' + guid + '" type="text" value=""><br>' +
                '<div><input id ="swimlaneGroup" type="checkbox" value="swimlaneGroup">&nbsp;是否为泳道分组' +
                '  &nbsp;&nbsp;<input id ="cellGroup" type="checkbox" value="cellGroup">&nbsp;是否为set分组' +
                '</div></p>');
    };

    popup.onOK = function(e) {
        e.preventDefault();
        var flag = this.callback();
        if (flag !== false) {
            this.hide();
            this.callback = returnTrue;
        }
    };

    popup.onCancel = function(e) {
        e.preventDefault();
        this.hide();
        this.callback = returnTrue;
    };

    popup.error = function(msg) {
        var contentBoxNode;
        var parentNode;
        var errorNode;
        var errorClass;
        var popupType = this.get('popupType');
        if (popupType === 'prompt') {
            contentBoxNode = this.get('contentBox');
            parentNode = contentBoxNode.one('.' + this.getClassName('popup-prompt'));
            errorClass = this.getClassName('popup-error');
            errorNode = parentNode.next('.' + errorClass);
            if (!errorNode) {
                errorNode = Y.Node.create('<div class="alert alert-error ' + errorClass + '"></div>');
                parentNode.insert(errorNode, 'after');
            }
            if (msg) {
                errorNode.set('text', msg);
                errorNode.show();
            } else {
                errorNode.hide();
            }
        }
    };

    Y.namespace('msgp.config').popup = popup;

}, '', {
    skinnable: false,
    requires: [
        'panel',
        'array-extras'
    ]
});
