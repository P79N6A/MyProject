YUI.add('config-tree/validatable', function(Y/*, NAME*/) {

    var Tree = Y.namespace('mt.config.Tree');

    function NodeValidatable() {
    }

    NodeValidatable.prototype.initializer = function(/*config*/) {
    };

    NodeValidatable.prototype.validateNodeName = function(nodeName) {
        if (arguments.length === 0) {
            nodeName = this.nodeName;
        }
        if (this.reNodeName.test(nodeName)) {
            return true;
        }
        return false;
    };

    NodeValidatable.prototype.validateNodeDataKey = function(key) {
        return this.reNodeDataKey.test(key);
    };

    NodeValidatable.prototype.reNodeName = /^[a-zA-Z0-9_\-]+$/;
    NodeValidatable.prototype.reNodeDataKey = /^[a-zA-Z0-9_\-\.]+$/;


    Tree.NodeValidatable = NodeValidatable;

}, '', {
    requires: [
        'config-tree'
    ]
});
