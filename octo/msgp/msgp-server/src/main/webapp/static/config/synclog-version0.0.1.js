/*jshint camelcase:false*/
M.add('msgp-config/synclog-version0.0.1', function (Y, NAME) {

    var synclog = {};
    Y.mix(synclog, Y.namespace('msgp.config.panel').synclog);

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
        if (Y.Lang.isArray((this.synclog))) {
            this.synclog = {
                logs: this.synclog
            };
        }
        this.synclog.css_prefix = 'config-node-synclog';
        var keys;
        if (this.synclog.logs.length > 0) {
            this.synclog.logs.sort(function(a, b) {
                return b.time - a.time;
            });
            this.synclog.keys = Y.Object.keys(this.synclog.logs[0]).sort(keySort);
            keys = this.synclog.keys;
            //this.synclog.logs.sort(function(a,b){return a[0]<b[0]});
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

    var keys = 'id time type operator detail'.split(' ');

    function keySort(a, b) {
        var ai = keys.indexOf(a);
        var bi = keys.indexOf(b);
        if (ai === -1) return 1;
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

    Y.namespace('msgp.config.panel').synclog = synclog;

}, '', {
    requires: [
        'datatype-date-format',
        'msgp-config/rest-version0.0.2',
        'msgp-config/synclog-tplversion0.0.1'
    ]
});
