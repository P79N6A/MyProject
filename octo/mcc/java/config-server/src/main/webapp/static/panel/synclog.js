/*jshint camelcase:false*/
YUI.add('config-panel/synclog', function(Y/*, NAME*/) {

    var synclog = {};
    Y.mix(synclog, Y.namespace('mt.config.panel.synclog'));

    synclog.ns = 'synclog';

    synclog.create = function() {
        var newsynclog = Y.Object(this);
        return newsynclog;
    };

    synclog.initializer = function() {
    };

    synclog.render = function() {
        this.renderUI();
        this.bindUI();
        this.syncUI();
    };

    synclog.renderUI = function() {
    };

    synclog.bindUI = function() {
    };

    synclog.syncUI = function() {
        this.synclog = this.host.node.synclog;
        if (Y.Lang.isArray((this.synclog.operatorLog))) {
            this.synclog = {
                logs: this.synclog.operatorLog,
                version: this.synclog.currentVersion
            };
        }
        this.synclog.css_prefix = 'config-node-synclog';
        var keys;
        if (this.synclog.logs.length > 0) {
            //this.synclog.logs.sort(function(a, b) {
            //    return b.syncTime - a.syncTime;
            //});
            //this.synclog.keys = Y.Object.keys(this.synclog.logs[0]);
            this.synclog.keys = ["time", "operator", "type", "detail"];
            keys = this.synclog.keys;
            this.synclog.logs = Y.Array.reduce(this.synclog.logs, [], function(prev, log) {
                prev.push(Y.Array.map(keys, function(key) {
                    if (key === 'time') {
                        return Y.Date.format(new Date(log[key]), { format: '%F %T' });
                    }
                    return log[key];
                }));
                return prev;
            });
        }
        Y.log(this.synclog);

        this.html = this.template(this.synclog);
        var oldContainer = this.host.container.one('.J-' + this.synclog.css_prefix);
        if (oldContainer) {
            oldContainer.remove();
        }
        this.host.container.append(this.html);
        this.container = this.host.container.one('.J-' + this.synclog.css_prefix);
        Y.log(this);
    };

    var keys = 'id syncTime version host ip pid node'.split(' ');

    function keySort(a, b) {
        var ai = keys.indexOf(a);
        var bi = keys.indexOf(b);
        if (ai === -1 ) return 1;
        if (bi === -1) return -1;
        return ai - bi;
    }

    synclog.show = function() {
        this.container.show();
    };

    synclog.hide = function() {
        this.container.hide();
    };

    synclog.destructor = function() {
        this.container.remove(true);
    };

    Y.namespace('mt.config.panel').synclog = synclog;

}, '', {
    requires: [
        'datatype-date-format',
        'config-rest',
        'config-panel/synclog-tpl'
    ]
});
