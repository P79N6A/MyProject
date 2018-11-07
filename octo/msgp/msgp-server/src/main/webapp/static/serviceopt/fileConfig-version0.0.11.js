M.add('msgp-serviceopt/fileConfig-version0.0.11', function (Y) {
        Y.namespace('msgp.serviceopt').fileConfig = fileConfig;
        Y.namespace('msgp.serviceopt').fileConfig_setCurrentFile = setCurrentFile;
        Y.namespace('msgp.serviceopt.fileConfig').clearLog = clearLog;

        var logSpan = '<span>[<font color="{color}">{msgType}</font>][{time}]:{log_content}<br/></span>';

        var openFileDialog;
        var appkey;
        var wrapper = Y.one('#file_config_detail_wrap');
        var currentEnv;

        var upload_body = wrapper.one('#upload_body');
        var file_config_machine = wrapper.one('#file-config-machine');
        var file_config_content = wrapper.one('#file-config-content');
        var file_config_enable = wrapper.one('#file-config-enable');
        var file_config_cancel = wrapper.one('#file-config-cancel');
        var file_config_redo = wrapper.one('#file-config-redo');
        var file_config_idc = wrapper.one('#fileconfig_idc');

        var addItemEventHandler,
            downFileEventHandler,
            searchConfigIdcEventHandler,
            searchConfigIPEventHandler,
            fileCancelEventHandler,
            redoEventHandler,
            editFileEventHandler,
            allCheckEventHandler,
            oneCheckEventHandler,
            uploadFileEventHandler,
            addFileEventHandler,
            deleteFileEventHandler,
            inputFileEventHandler,
            oneRadioEventHandler;

        var idcList = [
            {key: "大兴", value: "DX"}, {key: "永丰", value: "YF"} ,{key: "光环", value: "GH"},
            {key: "次渠", value: "CQ"}, {key: "桂桥", value: "GQ"}, {key: "月浦", value: "YP"},
            {key: "廊坊", value: "LF"}, {key: "润泽", value: "RZ"}, {key: "南汇", value: "NH"},
            {key: "其他", value: "OTHER"}];

        var ipArray;
        var groupID;
        var ipCheckUrl = document.location.href;
        var jumpUrl;

        /**
         * 在新上传和删除文件后，需要跟新此属性
         * @type {Array}
         */
        var current_fileNames = [];
        var redo_data = {
                flag: false,
                IPs: [],
                fileName: '',
                appkey: ''
            },
            interval = 15;

        /**
         * 在环境切换时，保存当前环境中数据库中的文件
         * @param fileList
         */
        function setCurrentFile(fileList) {
            current_fileNames.length = 0;
            fileList.forEach(function (item) {
                current_fileNames.push(item.filename);
            });
        }

        function getFileString(appkey) {
            var filePath = '/opt/meituan/apps/mcc/' + appkey + '/';
            return '<tr data-tag="2" id="file-tr">' +
                '<td><input id="one_radio" type="radio" style="margin-bottom: 50%;" />' +
                '</td>' +
                '<td>' +
                '<form id="form" enctype="multipart/form-data">' +
                ' <input id="input_file" name="file" type="file" style="display:none;">' +
                '<a id="add_file" href="javascript:void(0);" class="config-panel-select"> <i class="fa fa-file-text"></i> <span>选择文件</span>&nbsp; </a>' +
                '</form>' +
                '</td>' +
                '<td><input id="filePath" class="span5" style="width: 90%;" type="text" readonly value="' + filePath + '"/>' +
                '</td>' +
                '<td><input id="fileComment" class="span5" style="width: 90%;" readonly/>' +
                '</td>' +
                '<td> <a id="upload_file" href="javascript:void(0);" class="config-panel-upload" style="display: none;"> &nbsp;&nbsp;<i class="fa fa-upload"></i> <span>上传</span> </a>' +
                '<a id="edit_file" href="javascript:void(0);" class="config-panel-edit" style="display:none;"> &nbsp;&nbsp;<i class="fa fa-edit"></i> <span>编辑</span> </a>' +
                '<a id="delete_file" href="javascript:void(0);" class="config-panel-delete" style="display: none;"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>' +
                '</td>' +
                '</tr>';

        }

        /**
         * 本模块的主函数，主要是注册按钮的响应函数和初始化相关的内部变量
         * @param key
         */
        function fileConfig(key, env, gID, envtext) {
            init(key, gID, env, envtext)
            // bindDelete();
            bind();
            displayRedo(false);
        }

        function undelegate(handler) {
            if ('undefined' != typeof handler) {
                handler.detach();
            }
        }

        function unbind() {
            undelegate(addItemEventHandler);
            undelegate(downFileEventHandler);
            undelegate(searchConfigIdcEventHandler);
            undelegate(searchConfigIPEventHandler);
            undelegate(fileCancelEventHandler);
            undelegate(redoEventHandler);
            undelegate(editFileEventHandler);
            undelegate(allCheckEventHandler);
            undelegate(oneCheckEventHandler);
            undelegate(uploadFileEventHandler);
            undelegate(addFileEventHandler);
            undelegate(deleteFileEventHandler);
            undelegate(inputFileEventHandler);
            undelegate(oneRadioEventHandler);
        }

        function bind() {
            unbind();
            addItemEventHandler = wrapper.delegate('click', clickAddItem, '#add-item');
            downFileEventHandler = wrapper.delegate('click', clickDownFile, '#down-file-item');
            searchConfigIdcEventHandler = wrapper.delegate('change', clickSearchIDC, '#fileconfig_idc');
            searchConfigIPEventHandler = wrapper.delegate('click', clickSearchIP, '#search-config-ip');
            fileCancelEventHandler = wrapper.delegate('click', fileConfigCancel, '#file-config-cancel');
            redoEventHandler = wrapper.delegate('click', redo, '#file-config-enable');
            editFileEventHandler = wrapper.delegate('click', editFile, '#edit_file');
            allCheckEventHandler = wrapper.delegate('click', allCheck, '#all-check');
            oneCheckEventHandler = wrapper.delegate('click', oneCheck, '#one-checkbox');
            uploadFileEventHandler = wrapper.delegate('click', uploadFile, '#upload_file');
            addFileEventHandler = wrapper.delegate('click', addFile, '#add_file');
            deleteFileEventHandler = wrapper.delegate('click', deleteFile, '#delete_file');
            inputFileEventHandler = wrapper.delegate('change', inputFile, '#input_file');
            oneRadioEventHandler = wrapper.delegate('click', oneRadio, '#one_radio');
        }


        function init(key, gID, env, envtext) {
            appkey = key;
            groupID = gID;
            currentEnv = env;
            wrapper.one('.form-inline span label').set('text', envtext);

            fillIdcList(file_config_idc, idcList);
        }

        function fillIdcList(idc,data) {
            idc.empty();
            idc.append("<option value='all' selected='selected'>全部</option>");
            Y.Array.each(data, function (item) {
                idc.append('<option value=' + item.value + '>'+ item.key +'</option>');
            });
        }

        function clickSearchIDC() {
            var idcType = wrapper.one("#fileconfig_idc").get('value');
            var allList = Y.msgp.serviceopt.getIpArrayList();
            if (!idcType || 'all' == idcType) {
                ipArray = allList;
            } else {
                ipArray = getIdcArray(allList, idcType);
            }
            Y.msgp.serviceopt.fillIPs(ipArray);
        }

        function clickSearchIP() {
            var input = Y.Lang.trim(wrapper.one('#searchConfigIPInput').get('value'));
            var idcType = wrapper.one("#fileconfig_idc").get('value');
            var allList = Y.msgp.serviceopt.getIpArrayList();
            if ('all' == idcType) {
                ipArray = allList;
            } else {
                ipArray = getIdcArray(allList, idcType);
            }
            if ('' != input) {
                allList = ipArray;
                ipArray = getFilledIpArray(allList, input);
            }
            Y.msgp.serviceopt.fillIPs(ipArray);
        }

        function getIdcArray(allList, keyWord) {
            ipArray = new Array();
            if ('OTHER' == keyWord) {
                allList.forEach(function(item, index) {
                    var isOther = false;
                    for(var i = 0;i < idcList.length - 1; i++) {
                        console.log(idcList[i].value);
                        if (idcList[i] && idcList[i].value == item.idc) {
                            isOther = true;
                            continue;
                        }
                    }
                    if (isOther == false) {
                        ipArray.push(item);
                    }
                })
            } else {
                allList.forEach(function(item, index) {
                    if (item && item.idc == keyWord) {
                        ipArray.push(item);
                    }
                })
            }
            return ipArray
        }


        function getFilledIpArray(allList, keyWord) {
            ipArray = new Array();
            allList.forEach(function(item, index) {
                if (item && (item.IP.indexOf(keyWord) >= 0 || item.domain.indexOf(keyWord) >= 0)) {
                    ipArray.push(item);
                }
            })
            return ipArray
        }
        /**
         * 响应重试按钮
         */
        function redo() {
            var tag_value = file_config_redo.getData('tag');
            var downButton = wrapper.one('#down-file-item');
            switch (tag_value) {
                case '-1':
                    if (!redo_data.flag) {
                        printLog("ERROR", "数据已经失效");
                        displayRedo(false);
                        return;
                    }
                    var url = '/serverOpt/' + appkey + '/config/file/downFile2IPs';
                    file_config_enable.setAttribute("disabled")
                    downButton.setAttribute("disabled");
                    var iplist = getIPByDomainIPs(redo_data.IPs);
                    redo_data.flag = false;
                    file_config_redo.setData('tag', '0');
                    hasAppkeyAuth(url, redo_data.fileName, iplist, 0);
                    redo_data.IPs = [];
                    break;
                case '0':
                    displayRedo(false);
                    break;
            }
        }

        /**
         * 获取IP列表
         * @param domainIPs
         * @returns {Array}
         */
        function getIPByDomainIPs(domainIPs) {
            var ret = [];
            domainIPs.forEach(function (item) {
                ret.push(item.IP);
            });
            return ret;
        }

        function domainIPsToString(domainIPs) {
            var ret = [];
            if (ipCheckUrl.split("/").length > 2) {
                jumpUrl = ipCheckUrl.split("/")[2]
            }
            domainIPs.forEach(function (item) {
                if (undefined != jumpUrl && (undefined == item.msg || '' == item.msg || "未知错误" == item.msg)) {
                    ret.push(item.domain + ' (IP：' + item.IP + '，Message：' + "<a href=\'http://" + jumpUrl + "/checker/userCheck?hostname=" + item.IP + "#checkerHostInfo\' target=\"_blank\">主机诊断</a>" + ')');
                } else if("未知错误" != item.msg){
                    ret.push(item.domain + ' (IP：' + item.IP + '，Message：' + item.msg + ')' );
                } else {
                    ret.push(item.domain + ' (' + item.IP + ')' );
                }
            });
            return ret;
        }

        function displayRedo(flag) {
            if (flag) {
                file_config_enable.show();
                file_config_cancel.show();
            } else {
                file_config_enable.hide();
                file_config_cancel.hide();
            }
        }

        function fileConfigCancel() {
            displayRedo(false);
            clearLog();
            redo_data.flag = false;
        }

        function clearLog() {
            var log_childs = file_config_content.all('span');
            log_childs.each(function (item) {
                file_config_content.removeChild(item);
            });
        }

        function printListLog(msgType, prefix, list) {
            if (list.length > 0) {
                var logMsg = prefix + domainIPsToString(list).join(', ');
                printLog(msgType, logMsg);
            }
        }

        /**
         * 文件下发函数
         * @param url
         * @param fileName
         * @param IPs
         */
        function doDownFile(url, fileName, IPs, i) {
            if (i >= IPs.length) {
                var downButton = wrapper.one('#down-file-item');
                if (downButton) {
                    downButton.removeAttribute("disabled");
                }
                file_config_enable.removeAttribute("disabled");
                return;
            }

            var curIps = IPs.slice(i, i + interval > IPs.length ? IPs.length : i + interval);
            var data = {
                env: currentEnv,
                fileName: fileName,
                IPs: curIps,
                groupID: groupID
            };
            Y.io(url, {
                method: 'POST',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                sync: false,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var fileLogPrefix = ' 文件"' + fileName;
                        if (ret.isSuccess) {
                            displayRedo(redo_data.flag);
                            //file_config_redo.setData('tag', '0');
                            //redo_data.flag = false;
                        } else {
                            var ret_data = ret.data;
                            if (ret.msg) {
                                local_error_alert(ret.msg);
                                return;
                            }
                            redo_data.flag = true;
                            displayRedo(redo_data.flag);
                            //printListLog("INFO", fileLogPrefix + '"在以下主机中已下发且生效成功：', ret_data.successList);
                            printListLog("ERROR", fileLogPrefix + '"在以下主机中下发失败：', ret_data.distributeErr);
                            printListLog("ERROR", fileLogPrefix + '"在以下主机中生效失败：', ret_data.enableErr);
                            redo_data.appkey = appkey;
                            redo_data.fileName = fileName;
                            redo_data.IPs = redo_data.IPs.concat(ret_data.distributeErr).concat(ret_data.enableErr);
                            file_config_redo.setData('tag', '-1');
                        }

                        if(i + interval >= IPs.length){
                            DownFileFinish(fileName);
                        }

                    },
                    failure: function () {
                        if(i + interval >= IPs.length){
                            DownFileFinish(fileName);
                        }else{
                            local_error_alert("下发失败");
                        }
                    }
                }
            });

            var nextIndex = i + interval;
            doDownFile(url, fileName, IPs, nextIndex);
        }

        function DownFileFinish(fileName){
            var logMsg, msgType;
            msgType = "INFO";
            logMsg = ' 文件"' + fileName + '"下发结束。';
            printLog(msgType, logMsg);
        }

        /**
         * 响应点击文件下发按钮
         */
        function clickDownFile() {

            //获取需要下发的文件
            var checked_radio = wrapper.one('#one_radio:checked');
            if (null == checked_radio) {
                local_error_alert('请选择勾选所需要下发的文件');
                return;
            }
            var tr = checked_radio.ancestor('tr');
            var file_name = tr.one('#show_file_name');
            if (null == file_name || '' == file_name.get('text')) {
                local_error_alert('下发失败，所需要下发的文件不能为空');
                return;
            }

            //获取将要下发到的IPs
            var selected_Ips = file_config_machine.all('#one-ipcheck:checked');
            if (selected_Ips.isEmpty()) {
                local_error_alert('请勾选IP地址');
                return;
            }
            var ip_array = new Array();
            selected_Ips.each(function (item, index) {
                ip_array.push(item.getAttribute('value'));
            });
            //重置文件下发日志的对象
            redo_data.IPs = [];
            //文件下发
            var url = '/serverOpt/' + appkey + '/config/file/downFile2IPs';
            var downButton = wrapper.one('#down-file-item');
            downButton.setAttribute("disabled");
            redo_data.flag = false;
            file_config_redo.setData('tag', '0');
            //处理中
            hasAppkeyAuth(url, file_name.get('text'), ip_array, 0);
        }

        function hasAppkeyAuth(url, file_name, ip_array, type){
            //判断是否是服务负责人
            var authurl = '/serverOpt/' + appkey + '/config/auth';
            Y.io(authurl, {
                method: 'get',
                sync: true,
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (!res.isSuccess || false == res.data) {
                            Y.msgp.utils.msgpHeaderTip('error', "您对此服务没有操作权限", 3);
                            return;
                        } else {
                            //处理中
                            showDownFile();
                            doDownFile(url, file_name, ip_array, type);
                        }
                    }
                }
            });
        }


        /**
         * 日志打印函数
         * @param msgType
         * @param logMsg
         */
        function printLog(msgType, logMsg) {
            var dataTime = new Date();
            var log_content = [];
            var params = {
                color: ("error" == msgType.toLowerCase().toString()) ? "red" : "blue",
                msgType: msgType.toUpperCase(),
                time: dataTime.toString(),
                log_content: logMsg
            }
            log_content.push(Y.Lang.sub(logSpan, params));
            var template = log_content.join('');
            file_config_content.append(template);
        }

        /**
         * 点击“添加一项”按钮。此处仅仅添加前端的页面的html，并没有涉及到后台的操作。
         */
        function clickAddItem() {
            upload_body.append(getFileString(appkey));
            var none_file_content = wrapper.all('#none-file-content');
            if (null != none_file_content) {
                none_file_content.remove();
            }
        }


        function allCheck() {
            var ifChecked = this.get("checked");
            //单选与全选保持一致
            wrapper.all('#one-checkbox').set("checked", ifChecked);
        }

        function oneCheck() {
            var allOneCheck = wrapper.all('#one-checkbox');
            var allOneChecked = wrapper.all('#one-checkbox:checked');
            if (allOneChecked.size() === 0) {
                wrapper.one('#all-check').set("checked", false);
            } else {
                if (allOneCheck.size() === allOneChecked.size()) {
                    wrapper.one('#all-check').set("checked", true);
                }
            }
        }

        function uploadFile() {
            var p_temp = this.ancestor('tr');
            var uploader = p_temp.one('#input_file');
            var tag = p_temp.getData('tag');

            var fileList = uploader._node.files;
            if (fileList.length <= 0) {
                local_error_alert("上传失败，请选择文件");
                return;
            }
            var file = fileList[0];
            if (file.size > 1024 * 200) {
                local_error_alert("文件不能超过200KB");
                return;
            }

            var fileName = p_temp.one('#add_file').one('#show_file_name').get('text');
            if(fileName.indexOf(" ") != -1){
                local_error_alert("文件名有空格，请重新选择并上传");
                return;
            }

            if ('2' == tag) {
                var isExist = false;
                current_fileNames.forEach(function (item) {
                    if (item == fileName) {
                        isExist = true;
                        local_error_alert("文件名冲突，请重新选择并上传");
                        //经过测试，直接在此处使用return返回，并不能真正返回；因此添加isExist来作返回标志。
                    }
                });
                if (isExist)return;
            }
            function submitForm() {

                var myData = {
                    file: file,
                    appkey: appkey,
                    env: currentEnv,
                    filepath: p_temp.one('#filePath').getAttribute('value'),
                    filename: fileName.trim(),
                    groupID: groupID
                }
                //ev.preventDefault();
                Y.io('/serverOpt/' + appkey + '/config/file/upload', {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'enctype': 'multipart/form-data',
                        'contentType': false, //必须
                        'processData': false //必须
                    },
                    method: 'POST',
                    form: {
                        id: p_temp.one('#form'),
                        upload: true
                    },
                    data: myData,
                    on: {
                        complete: function (id, response) {
                            var ret = Y.JSON.parse(response.responseText);
                            if (ret.isSuccess) {
                                //var file = ret.data;
                                var file_size_Byte = file.size;
                                var isKB = file_size_Byte >= 1024 ? true : false;
                                var unit = isKB ? "KB" : "Byte";
                                var show_size = (isKB ? file_size_Byte / 1024 : file_size_Byte).toFixed(2);
                                var msg = '"' + file.name + '"已成功上传，大小为' + show_size + unit;
                                var downFile = wrapper.one('#down-file-item');
                                p_temp.setData('tag', '1');
                                downFile.removeAttribute('disabled');
                                Y.msgp.utils.msgpHeaderTip('info', msg, 5);
                                if ('2' == tag) {
                                    current_fileNames.push(fileName);
                                }
                            } else {
                                local_error_alert(ret.msg);
                            }
                            p_temp.one('#one_radio')._node.click();
                        },
                        failure: function () {
                            local_error_alert("文件上传失败");
                        }
                    }
                });
            }

            submitForm();
        }

        function addFile() {
            var p_temp = this.ancestor('tr');
            var uploader = p_temp.one('#input_file');
            uploader._node.click();
        }


        function inputFile() {
            var flname = Y.msgp.utils.getFileNameByPath(this._node.value);
            var p_temp = this.ancestor('tr');
            var add_file = p_temp.one('#add_file');
            var show_file_name = p_temp.one('#show_file_name');
            switch (p_temp.getData('tag')) {
                case '0':
                //有更新，需上传，才能下发
                case '1':
                    //有更新且已上传（或未更改），可下发
                    if (null != flname) {
                        p_temp.setData('tag', '0');
                    }
                    break;
                case '2':
                    //新增
                    var flname_temp = (null != flname) ? flname : "";
                    var showFileNameSpan = '<span id="show_file_name" style="color:black;">' + flname_temp + '</span>'
                    if (null != show_file_name) {
                        show_file_name.set('text', flname_temp);
                    } else {
                        add_file.append(showFileNameSpan);
                    }
                    break;
            }
            p_temp.one('#one_radio')._node.click();
            if (null == flname) {
                local_error_alert("未选择文件，请重新选择文件，并上传");
            }
        }
        var deleteFileDialog;
        function deleteFile(){
            var tr = this.ancestor('tr');
            var file_name = tr.one('#show_file_name');
            if (null == file_name || '' == file_name.get('text')) {
                return;
            }
            var fileName = file_name.get('text');
            deleteFileDialog = deleteFileDialog ? deleteFileDialog : new Y.mt.widget.CommonDialog({
                title: '删除配置文件',
                width: 400,
                content: '你确定要删除这个配置文件吗？',
                btn: {
                    pass: doDeleteFile
                }
            });
            deleteFileDialog.tr = tr;
            deleteFileDialog.show();

            function doDeleteFile() {
                var tr = deleteFileDialog.tr;
                var fileName = tr.one('#show_file_name').get('text');
                var url = '/serverOpt/' + appkey + '/config/file/deleteFile';
                var data = {
                    env: currentEnv,
                    groupID: groupID,
                    appkey: appkey,
                    fileName: fileName
                };
                myIO(url, "DELETE", data, suc, fail);
                function suc(ret) {
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', "配置文件\"" + fileName + "\"删除成功", 3);
                        tr.remove();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }
                }
                function fail() {
                    Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                }
            }
        }



        function myIO(url, method, data, suc, fail) {
            if ('POST' == method)data = Y.JSON.stringify(data);
            Y.io(url, {
                method: method,
                data: data,
                on: {
                    success: function (id, o) {
                        suc(Y.JSON.parse(o.responseText));
                    },
                    failure: function () {
                        fail();
                    }
                }
            });
        }

        var saveFilePath;

        /**
         *编辑文件
         */
        function editFile() {
            var tr = this.ancestor('tr');

            var file_name = tr.one('#show_file_name');
            if (null == file_name || '' == file_name.get('text')) {
                local_error_alert('查看失败，请重试');
                return;
            }

            var file_path = tr.one('#filePath');
            saveFilePath = file_path.getAttribute('value');
            var fileName = file_name.get('text');
            var url = '/serverOpt/' + appkey + '/config/file/filecontent';
            Y.io(url, {
                method: 'GET',
                data: {
                    appkey: appkey,
                    env: currentEnv,
                    fileName: fileName,
                    groupID: groupID
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var data = ret.data;
                        if (ret.isSuccess) {
                            openFileDialog = new Y.mt.widget.CommonDialog({
                                id: 'openFileDialog',
                                title: fileName,
                                top: '40%',
                                height: '50%',
                                btn: {
                                    enabled: true,
                                    passName: '保存并上传',
                                    pass: saveFileAfterEdit
                                }
                            });
                            openFileDialog.setContent('<textarea id="dialog-pre" contenteditable="true" style="overflow: scroll;height: 600px;width: 580px;">' + data.filecontent + '</textarea>');
                            openFileDialog.show();

                        } else {
                            local_error_alert(ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        local_error_alert("服务器异常：获取文件内容失败");
                    }
                }
            });
        }


        function saveFileAfterEdit(saveButton, container) {
            var pre = container.one('#dialog-pre');
            var fileContent = pre.get("value");
            var fileName = container.one('h3').get('text');
            var url = '/serverOpt/' + appkey + '/config/file/saveFileContent';
            var post_data = {
                env: currentEnv,
                fileName: fileName,
                filePath: saveFilePath,
                fileContent: fileContent,
                groupID: groupID
            };
            Y.io(url, {
                method: 'POST',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(post_data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', "保存成功", 3);
                        } else {
                            local_error_alert(ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        local_error_alert("保存失败");
                    }
                }
            });
            return false;
        }

        function oneRadio() {
            wrapper.all('#one_radio').set('checked', false);
            this.set('checked', true);
            var tr = this.ancestor('tr');
            var uploadTag = tr.getData('tag');
            var uploadButton = wrapper.one('#down-file-item');


            switch (uploadTag) {
                case '0':
                //有更新，需上传，才能下发
                case '2':
                    //新增
                    var flname = Y.msgp.utils.getFileNameByPath(tr.one('#input_file')._node.value);
                    tr.one('#upload_file').setStyle("display", (null != flname) ? "inline" : "none");
                    tr.one('#edit_file').setStyle("display", "none");
                    uploadButton.setAttribute("disabled");
                    break;
                case '1':
                    //有更新且已上传（或未更改），可下发
                    uploadButton.removeAttribute("disabled");
                    tr.one('#upload_file').setStyle("display", "none");
                    tr.one('#edit_file').setStyle("display", "inline");
                    tr.one('#delete_file').setStyle("display", "inline");
                    break;

            }
        }

        function local_error_alert(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg, 5);
        }

        function showDownFile() {
            printLog("INFO", "&nbsp;正在执行文件下发...");
        }
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base',
            'msgp-utils/msgpHeaderTip',
            'template',
            'msgp-utils/getFileNameByPath',
            'msgp-service/commonMap',
            'io-upload-iframe'
        ]
    }
)
;


