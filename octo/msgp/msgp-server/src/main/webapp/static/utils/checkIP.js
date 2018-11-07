/**
 * Created by lhmily on 7/27/15.
 */
/* jshint indent: false */
M.add('msgp-utils/checkIP', function (Y) {
    Y.namespace('msgp.utils').checkIP = checkIP;
    var exp = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
    function checkIP(ip) {
        return ip.match(exp);
    }
}, '0.0.1', {
    requires: [
        'mt-base'
    ]
});

