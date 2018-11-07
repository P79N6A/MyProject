/**
 * Created by lhmily on 08/05/2015.
 */
M.add('msgp-utils/getFileNameByPath',function(Y){
    Y.namespace('msgp.utils').getFileNameByPath=getFileNameByPath;
    function getFileNameByPath(str){
        var ch = '/';
        var s = str.lastIndexOf(ch);
        if (s == -1) {
            ch = '\\';
            var s = str.lastIndexOf(ch);
        }
        if (s == -1) {
            return (null);
        }
        else {
            return (str.substring(s + 1, str.length));
        }
        return (' ');
    }
},'0.0.1',{
    requires:[
        'mt-base'
    ]
});
