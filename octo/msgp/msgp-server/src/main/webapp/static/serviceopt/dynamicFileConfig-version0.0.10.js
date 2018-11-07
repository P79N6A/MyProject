M.add('msgp-serviceopt/dynamicFileConfig-version0.0.10', function (Y) {
        Y.namespace('msgp.serviceopt').dynamicFileConfig = dynamicFileConfig;
        Y.namespace('msgp.serviceopt').getConfigMachine = getConfigMachine;
        Y.namespace('msgp.serviceopt').fillIPs = fillIPs;
        Y.namespace('msgp.serviceopt').getIpArrayList = getIpArrayList;

        var appkey, groupID, currentEnv;
        var wrapper;
        var wrapper_machines, wrapper_files, wrapper_log;
        var ipArrayList;

        var returnEventHandler, addIpsEventHandler;

        var SHOW_IP_TEMPLATE = [
            '<tr id="ip-tr">',
            '<%var nullItemNum=0; Y.Array.each(this.data, function(item, index){ %>',
            '<%if(null==item||""==item){++nullItemNum;}else{%>',
            '<td style="width: 20%;">',
            '<input type="checkbox" id="one-ipcheck" style="margin-bottom: 3px;" value="<%= item.IP%>"/> &nbsp;<%= item.domain%>(<%= item.IP%>)',
            '</td>',
            '<% }}); %>',
            '<% for(var i=0;i < 5 - this.data.length-nullItemNum; ++i){%>',
            '<td></td>',
            '<% } %>',
            '</tr>'
        ].join('');

        /**
         * data-tag:
         *        ==2: 新增；
         *        ==1: 有更新且已上传（或未更改），可下发；
         *        ==0: 有更新，需上传，才能下发。
         *
         * @type {string}
         */
        var add_item_template = [
            '<% Y.Array.each(this.data, function(item, index){ %>',
            '<tr data-tag="1" id="file-tr">',
            //'<td><input id="one-checkbox" type="checkbox"/>',
            '<td><input id="one_radio" type="radio" style="margin-bottom: 50%;" />',
            '</td>',
            '<td>',
            '<form id="form" enctype="multipart/form-data">',
            ' <input id="input_file" name="file" type="file" style="display:none;">',
            '<a id="add_file" href="javascript:void(0);" class="config-panel-select"> <i class="fa fa-file-text"></i> <span>选择文件</span>&nbsp;',
            '<span id="show_file_name" style="color:black;">',
            '<%= item.filename%>',
            '</span></a>',
            '</form>',
            '</td>',
            //'<td><input id="input_file" style="display:none;" type="file" class=""/>',
            //<input type="hidden" name="MAX_FILE_SIZE" value="30000">File:
            //'</td>',
            '<td><input id="filePath" class="span5" style="width: 90%;" type="text" readonly value="<%= item.filepath%>"/>',
            '</td>',
            '<td><input id="fileComment" class="span5" style="width: 90%;" readonly/>',
            '</td>',
            '<td> <a id="upload_file" href="javascript:void(0);" class="config-panel-upload" style="display: none;"> &nbsp;&nbsp;<i class="fa fa-upload"></i> <span>上传</span> </a>',
            '<a id="edit_file" href="javascript:void(0);" class="config-panel-edit">&nbsp;&nbsp; <i class="fa fa-edit"></i> <span>编辑</span> </a>',
            '<a id="delete_file" href="javascript:void(0);" class="config-panel-delete"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span>删除</span> </a>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');

        var upload_body = Y.one('#upload_body');

        function dynamicFileConfig(key, env, wrap, gID, envtext) {
            init(key, env, wrap, gID);
            bindClickCheckBox();
            Y.msgp.serviceopt.fileConfig(appkey, currentEnv, groupID, envtext);
            if ('undefined' != typeof returnEventHandler) {
                returnEventHandler.detach();
            }
            returnEventHandler = wrapper.one('#detail_file_return').on('click', clickReturn);
            addIpsEventHandler = wrapper.one('#file-config-add-ip').on('click', addIps);
        }


        function clickReturn() {
            Y.msgp.serviceopt.optFileConfig.returnGroup();
        }

        function addIps() {
            Y.msgp.serviceopt.optFileConfig.addIps(this, groupID);
        }
        function init(key, env, wrap, gID) {
            appkey = key;
            wrapper = wrap;
            groupID = gID;
            currentEnv = env;
            wrapper_files = wrapper.one('#file-config-container-panel');
            wrapper_machines = wrapper.one('#file-config-machine');
            wrapper_log = wrapper.one('#file-config-log');
            Y.msgp.serviceopt.fileConfig.clearLog();

            getConfigFile();

            getConfigMachine();

            showCotent(true, 2);
            showCotent(false, 2);

        }

        function getConfigFile() {
            var url = '/serverOpt/' + appkey + '/config/file/filenames';
            var data = {
                env: currentEnv,
                groupID: groupID
            };
            Y.io(url, {
                method: 'get', data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            fillFileList(ret.data);
                        } else {
                            showError(true, ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        showError(true, "服务器异常");
                    }
                }
            });
        }

        function getConfigMachine() {
            var url = '/serverOpt/' + appkey + '/config/file/domainIPByGroupID';
            var data = {
                env: currentEnv,
                groupID: groupID
            };
            Y.io(url, {
                method: 'get', data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            ipArrayList = ret.data;
                            fillIPs(ret.data);
                        } else {
                            showError(false, ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        showError(false, "服务器异常");
                    }
                }
            });
        }

        function fillFileList(fileList) {
            var upload_body = wrapper_files.one('#upload_body');
            if (undefined != fileList && (fileList.length > 0)) {
                showCotent(true, 1000);
                var macor = new Y.Template();
                var html = macor.render(add_item_template,
                    {data: fileList});
                upload_body.append(html);
                Y.msgp.serviceopt.fileConfig_setCurrentFile(fileList);
                return;
            }else{
                Y.msgp.serviceopt.fileConfig_setCurrentFile(new Array());
            }

            showError(true, "没有文件");
        }

        function showError(isFile, msg) {
            if (isFile) {
                wrapper_files.all('#file-tr').remove();
                wrapper_files.one('#file-content-overlay').hide();
                var upload_body = wrapper_files.one('#upload_body');
                wrapper_files.all('#none-file-content').remove();
                var html = '<tr id="none-file-content"><td colspan="8" class = "mb5 span4">' + msg + '</td></tr>';
                upload_body.append(html);
            } else {
                wrapper_machines.all('#ip-tr').remove();
                wrapper_machines.one('#ip-content-overlay').hide();
                wrapper_machines.one('#all-ipcheck').hide();
                wrapper_machines.all('#none-ip-content').remove();
                var down_body = wrapper_machines.one('#down_body');
                var html = '<tr id="none-ip-content"><td  class = "mb5 span4">' + msg + '</td></tr>';
                down_body.append(html);
            }
        }

        function showCotent(isFile, flag) {
            if (isFile) {
                wrapper_files.all('#none-file-content').remove();
                wrapper_files.all('#file-tr').remove();
                wrapper_files.one('#file-content-overlay').hide();
                switch (flag) {
                    case 1:
                        //展示刷新状态
                        wrapper_files.one('#file-content-overlay').show();
                        break;
                    case 2:
                        //隐藏刷新状态
                        wrapper_files.one('#file-content-overlay').hide();
                        break;
                    default:
                        break;
                }
            } else {
                wrapper_machines.all('#none-ip-content').remove();
                wrapper_machines.all('#ip-tr').remove();
                wrapper_machines.one('#ip-content-overlay').hide();
                wrapper.one('#all-ipcheck').hide();
                switch (flag) {
                    case 1:
                        //展示刷新状态
                        wrapper_machines.one('#ip-content-overlay').show();
                        break;
                    case 2:
                        //隐藏刷新状态
                        wrapper_machines.one('#ip-content-overlay').hide();
                        break;
                    default:
                        break;
                }
            }
        }

        function getIpArrayList() {
            return ipArrayList;
        }

        function fillIPs(ipList) {
            var down_file = wrapper_machines.one('#down-file-item');
            if (0 >= ipList.length) {
                if (!down_file.hasClass('displayNone')) {
                    down_file.addClass('displayNone');
                }
                showError(false, "没有主机");
            } else {
                showCotent(false, 1000);
                down_file.removeClass('displayNone');
                wrapper_machines.all('#none-ip-content').remove();
                wrapper_machines.one('#all-ipcheck').show();
                wrapper_machines.one('#all-ipcheck').set("checked", false);
                var colspan = 5;
                var row_num = ipList.length / colspan;
                var ipListTBody = wrapper.one('#down_body');
                var macor = new Y.Template();
                ipListTBody.all('#ip-tr').remove();
                for (var i = 0; i < row_num; ++i) {
                    var sub_IPs = ipList.slice(colspan * i, colspan * i + colspan);
                    var html = macor.render(SHOW_IP_TEMPLATE,
                        {data: sub_IPs});
                    ipListTBody.append(html);
                }
            }
        }

        /**
         * 点击IP的复选框
         */
        function bindClickCheckBox() {
            //全选
            wrapper_machines.delegate('click', function () {
                var ifChecked = this.get("checked");
                //单选与全选保持一致
                wrapper_machines.all('#one-ipcheck').set("checked", ifChecked);
            }, '#all-ipcheck');
            //单选
            wrapper_machines.delegate('click', function () {
                //全选与单选保持一致
                var allOneCheck = wrapper_machines.all('#one-ipcheck');
                var allOneChecked = wrapper_machines.all('#one-ipcheck:checked');
                if (allOneChecked.size() === 0) {
                    wrapper_machines.one('#all-ipcheck').set("checked", false);
                } else {
                    if (allOneCheck.size() === allOneChecked.size()) {
                        wrapper_machines.one('#all-ipcheck').set("checked", true);
                    }
                }
            }, '#one-ipcheck');
        }
    }, '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base',
            'template',
            'msgp-serviceopt/fileConfig-version0.0.11',
            'msgp-utils/msgpHeaderTip',
            'msgp-serviceopt/optFileConfig-version0.0.14'
        ]
    }
)
;
