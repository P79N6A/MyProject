M.add('msgp-config/validatable-version0.1.0', function(Y) {

    var Tree = Y.namespace('msgp.config.tree');

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
        'msgp-config/tree-version0.1.2'
    ]
});