function getQueryValue() {
    getHosts();
    var hosts = selectedHosts;
    filePath = $("#filePath").val().trim();
    filter = $("#filter").val().trim().replace(', ',',');
    if(filter.charAt(filter.length - 1) == ','){
        filter = filter.slice(0, -1);
    }
    return {
        userName: userName,
        appkey: appkey,
        hosts: hosts,
        filePath: filePath,
        filter: filter
    }
}
function getHosts() {
    selectedHosts = [];
    var hosts = $('#hosts option:selected').map(function (a, item) {
        selectedHosts.push(item.value);
    });
}

function registerLogEvent(socket) {
    if (socket != null) {
        socket.on('LogEvent', function (data) {
            output('<span class="msg"><span style="color: limegreen">' + data.host + '-></span>' + data.content + '</span>');
        });
    }
}
function connectToServer() {
    if (socket == null) {
        var logServer = logMonitorServer;
        socket = io(logServer);
        socket.on('disconnect', function () {
            connected = false;
            output('<span class="msg">The client has disconnected!</span>');
        });
        return socket;
    } else {
        return socket;
    }
}
function sendDisconnect() {
    if (socket != null) {
        socket.disconnect();
        socket = null;
    }
}

function isConnected() {
    return connected;
}

function output(message) {
    //  替换所有的换行符为<br/>
    message = message.replace(/(?:\r\n|\r|\n)/g, '<br/>');
    var element = $("<div class='msg'>" + " " + message + "</div><br/>");
    var msgElement = $('.messages');
    msgElement.append(element);
    msgElement.animate({scrollTop: msgElement.get(0).scrollHeight}, 10);
}

function outputParam(message){
    message = message.replace(/(?:\r\n|\r|\n)/g, '<br/>');
    var element = $("<div class='param' style='color: green; font-weight: bold;'>" + " " + message + "</div><br/>");
    var msgElement = $('.params');
    msgElement.append(element);
}

function clearParamScreen() {
    var msgElement = $('.params');
    msgElement.empty();
}

function startWatch(param) {
    clearParamScreen();
    outputParam('Hosts: ' + param.hosts.join(' | '));
    outputParam('Log Path: ' + param.filePath);
    if(!param.filter == ""){
        outputParam('Filter: ' + param.filter.replace(',',', '));
    }
    if (!isConnected()) {
        socket = connectToServer();

        socket.on('connect', function () {
            connected = true;
            output('<span class="msg">The client has connected with remote RealTimeLog server!</span>');
            output('<span class="msg">Fetching data from remote provider...</span>');
            registerLogEvent(socket);
            emitStartWatchSignal(socket, param);
        });
        //  注册重连接事件
        registerReconnectEvent(socket, param);
        output('<span class="msg">Connecting, please wait...(if this state lasts more than a minute, please <a href="https://123.sankuai.com/km/page/28210916" target="_blank">click here</a> to fix it.)</span>');
    } else {
        //  先停止之前的查询事件
        emitStopWatchSignal(socket);
        emitStartWatchSignal(socket, param);
        //  注册重连接事件
        registerReconnectEvent(socket, param);
    }
}
function stopWatch() {
    emitStopWatchSignal(socket);
}
function emitStartWatchSignal(socket, param) {
    if (socket != null) {
        socket.emit("BROWSER_START_WATCH", param);
    }

}
function emitStopWatchSignal(socket) {
    if (socket != null) {
        socket.emit("BROWSER_STOP_WATCH", null);
    }
}
function registerReconnectEvent(socket, param) {
    if (socket != null) {
        socket.on('reconnect', function () {
            connected = true;
            emitStartWatchSignal(socket, param);
        });

    }
}
function initHosts() {
    $('#hosts').multiselect({
        selectAllText: "选择全部",
        allSelectedText: "已选择全部主机",
        nonSelectedText: "未选择主机",
        placeholder: "请选择主机",
        buttonWidth: '325px',
        includeSelectAllOption: true,
        selectAllNumber: true,
        buttonText: function (options, select) {
            var total = $('#hosts option').length;
            if (options.length === 0) {
                return '主机列表为空 请至反馈页面查看解决方法';
            }
            else if (options.length < total && options.length > 1) {
                return '已选择' + options.length + '台主机';
            } else if (options.length == total) {
                return '已选择全部主机(' + total + '台)';
            } else {
                var labels = [];
                options.each(function () {
                    if ($(this).attr('label') !== undefined) {
                        labels.push($(this).attr('label'));
                    }
                    else {
                        labels.push($(this).html());
                    }
                });
                return labels.join(', ') + '';
            }
        }
    });
    if($('#hosts option').length >= 1){
        $("#hosts option:first").prop("selected", 'selected');
        $('#hosts').multiselect('refresh');
    }
    //.multiselect('updateButtonText')
}

function split( val ) {
    return val.split( /,\s*/ );
}
function extractLast( term ) {
    return split( term ).pop();
}

function initFilterNew() {
    $("#filter")
        .on( "keydown", function( event ) {
            if ( event.keyCode === $.ui.keyCode.TAB &&
                $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
        })
        .autocomplete({
            minLength: 0,
            autoFill: true,
            mustMatch: false,
            source: function( request, response ) {
                response( $.ui.autocomplete.filter(
                    defaultFilter, extractLast( request.term ) ) );
            },
            focus: function() {
                return false;
            },
            select: function( event, ui ) {
                var terms = split( this.value );
                terms.pop();
                terms.push( ui.item.value );
                terms.push( "" );
                this.value = terms.join( ", " );
                return false;
            }
        });
}

function validationCheck(){
    tips.html('');
    var filePath = $("#filePath").val().trim();
    var pathReg = /^[\.a-zA-Z_\-0-9/]+$/;
    var filterReg = /^[a-zA-Z0-9,]+$/;
    if(selectedHosts.length == 0){
        tips.html("请选择主机");
        return false
    }else if(filePath.length == 0){
        tips.html("请输入日志路径");
        return false
    }else if(!pathReg.test(filePath)) {
        tips.html("日志路径含有非法字符(只能由字母 数字 .  - _ \\ 组成)");
        return false;
    } else if(!(filePath.startsWith("/var") || filePath.startsWith("/opt"))){
        tips.html('路径必须以"/var"或"/opt"开头');
        return false
    }else if(!(filePath.indexOf("/.") < 0 || filePath.indexOf("./") < 0 )){
        tips.html('路径不能包含"."或".."');
        return false
    }else if(filter != "" && !filterReg.test(filter)) {
        tips.html("关键字含有非法字符(只能由字母 数字 , 组成)");
        return false;
    } else {
        return true
    }
}

function initSubmitButton() {
    $('#submitBtn').click(function () {
        var param = getQueryValue();
        if(validationCheck()){
            startWatch(param);
        }
    });
}

function initStopButton() {
    $('#stopBtn').click(function () {
        stopWatch();
    });
}

function bindTypeSelect() {
    $('#env_select a').click(function () {
        $('#env_select a').removeClass('btn-primary');
        var elem = $(this);
        elem.addClass("btn-primary");
        var elemClass = elem.attr('class');
        if (elemClass.indexOf("btn-primary") >= 0) {
            var env = elem.attr('value');
            var url = "/realtime/entry?appkey=" + appkey + "&env=" + env;
            $(location).attr('href', url);
        }
    })
}

function initValue() {
    tips = $("#tips");
    userName = $('#userName').text();
    appkey = $('#appkey').text();
    logMonitorServer = $('#logMonitorServer').text();
}

function setEnvTextByType(type) {
    var url = '/common/online';
    $.ajax({
        url: url,
        method: 'get',
        success: function(res) {
            if( res.isSuccess ){
                if(res.data) {
                    $($('#'+type+' a')[0]).text('prod');
                    $($('#'+type+' a')[1]).text('staging');
                    $($('#'+type+' a')[2]).text('test');
                }else{
                    $($('#'+type+' a')[0]).text('dev');
                    $($('#'+type+' a')[1]).text('ppe');
                    $($('#'+type+' a')[2]).text('test');
                }
            }
        }
    });
};


var connected = false;
var socket = null;

var selectedHosts = [];

var userName;
var appkey;
var logMonitorServer;
var tips;
var filter;
var filePath;

var defaultFilter = ["INFO","WARN","DEBUG","ERROR","FATAL"];


$(document).ready(function () {
    initValue();
    initHosts();
    initFilterNew();
    //initFilter();
    initStopButton();
    initSubmitButton();
    bindTypeSelect();
    tips.html('');
    setEnvTextByType('env_select');
});

$(window).unload(sendDisconnect());
