/*jshint unused:false*/
YUI.add('config-user/apis', function(Y) {

    var rest = Y.namespace('mt.config.rest');

    function wrapError(cb) {
        return function(err) {
            if (typeof err === 'string') {
                err = new Error(err);
            }
            cb(err);
        };
    }

    var type2method = {
        add: 'post',
        'delete': 'delete'
    };

    var admins = {};
    admins.add = makeApi('add');
    admins.delete = makeApi('delete');


    function makeApi(type) {
        return function(id, cb) {
            return rest[type2method[type]]('/config/user/admin/' + type + '?id=' + id, function() {
                cb(null);
            }, wrapError(cb));
        };
    }

    var spaceAdmins = {};
    spaceAdmins.add = makeSpaceApi('add');
    spaceAdmins.delete = makeSpaceApi('delete');

    function makeSpaceApi(type) {
        return function(id, cb) {
            var spaceName = this.req.params.spaceName;
            return rest[type2method[type]]('/config/space/' + spaceName + '/user/' + type + '?id=' + id, function() {
                cb(null);
            }, wrapError(cb));
        };
    }

    var apis = {};
    apis.admins = admins;
    apis.spaceAdmins = spaceAdmins;

    Y.namespace('mt.config.user').apis = apis;

}, '', {
    requires: [
        'json',
        'config-rest'
    ]
});
