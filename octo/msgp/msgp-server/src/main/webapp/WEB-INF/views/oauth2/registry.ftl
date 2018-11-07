<title>注册服务并授权</title>
<div id="registryForm" class="form-horizontal">
    <div class="control-group mb5"><label class="control-label"></label>

        <div class="controls"><span id="result-tip" class="text-error"></span></div>
    </div>
    <div class="control-group"><label class="control-label">唯一标识：</label>

        <div class="controls">
            <input id="appkey" type="text" class="span6" placeholder="建议格式：com.公司(内部sankuai、外部meituan).业务线.具体服务"/>
            <span class="tips"></span>
        </div>
        <div id="selectName"></div>
    </div>
    <div class="control-group" id="business_wrap"><label class="control-label">所属业务：</label>

        <div class="controls">
            <select id="business_name">
            <#list businessMap as item>
                <option value="${item.value}"
                        <#if (businessVal?exists && businessVal==item.value) || (!businessVal?exists && item_index == 0) >selected</#if> >${item.name}</option>
            </#list>
            </select>
            <span class="ml10 mr10"></span>
            <input type="text" id="business_group" placeholder="请输入子业务名称" value="${group!''}"/>
        </div>
    </div>
    <div class="control-group"><label class="control-label">服务描述：</label>

        <div class="controls">
            <input id="intro" class="span6" type="text" placeholder="简单服务描述信息">${intro!''}</input>
            <span class="tips"></span>
        </div>
    </div>
    <div class="control-group" id="category_wrap" style="display: none;"><label class="control-label">类型：</label>

        <div class="controls">
            <label class="radio inline">
                <input type="radio" name="category" value="http">http
            </label>
            <label class="radio inline">
                <input type="radio" name="category" value="thrift" checked>thrift
            </label>
            <label class="checkbox inline pl0"><span class="tips"></span></label>
        </div>
    </div>
    <div class="control-group" id="level_wrap" style="display: none;"><label class="control-label">层级：</label>

        <div class="controls">
        <#list levelMap as item>
            <label class="radio inline">
                <input type="radio" name="level" value="${item.value}"
                       <#if 1 ==item.value>checked</#if> />${item.name}
            </label>
        </#list>
        </div>
    </div>
    <div class="form-actions"><input id="submit-btn" class="btn btn-primary" type="submit" value="注册并授权"/></div>
</div>
<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script type="text/javascript" src="/static/js/lodash.js"></script>
<script>
    var response_type = "${response_type}";
    var client_id = "${client_id}";
    var redirect_uri = "${redirect_uri}";
</script>
<script type="text/javascript" src="/static/oauth2/registry.js"></script>
<script>
    $(function () {
        //自定义带加载loading的ajax请求
        function defultAjax(url, success) {
            $.ajax({
                type: "GET",
                url: url,
                data: {},
                timeout: 30000,
                dataType: "json",
                beforeSend: function () {
                    $('#spinner').css('display', 'block');
                },
                success: function (data) {
                    if (data.isSuccess) {
                        success(data)
                    } else {
                        alert('服务器错误')
                    }
                },
                complete: function (XHR, TS) {
                    $('#spinner').css('display', 'none');
                },
                error: function () {
                    alert('请求出错请查看网络连接')
                }
            });
        }

        defultAjax('/service/getTags', function (data) {
            var data = data.data, timeOut;
            var owt = data.owt, srv = data.srv, com = ['sankuai', 'meituan'];
            $('#appkey').bind('input propertychange', function (e) {
                var $this = $(this);
                clearTimeout(timeOut);
                timeOut = setTimeout(function () {
                    var v = $(e.target).val(), reso = [], vs = v.split('.');
                    if (vs.length == 2) {
                        reso = _.filter(com, function (o) {
                            if (o.indexOf(vs[1]) > -1) {
                                return true;
                            }
                            return false;
                        });
                    } else if (vs.length == 3) {
                        reso = _.filter(owt, function (o) {
                            if (o.indexOf(vs[2]) > -1) {
                                return true;
                            }
                            return false;
                        });
                    } else if (vs.length == 4) {
                        reso = _.filter(srv, function (o) {
                            if (o.indexOf(vs[3]) > -1) {
                                return true;
                            }
                            return false;
                        });
                    } else if (vs.length == 5) {
                        reso = _.filter(srv, function (o) {
                            if (o.indexOf(vs[4]) > -1) {
                                return true;
                            }
                            return false;
                        });
                    }

                    var compiled = _.template('<div style="position: absolute;top:<%=top%>px;left:<%=left%>px;background-color: #fff;border: 1px solid #eeeeee"><%= ul %></div>');
                    var nameList = '<ul id="nameBox" style="width:190px;max-height: 350px;overflow-y: scroll;overflow-x:hidden ">';
                    for (var i = 0, l = reso.length; i < l; i++) {
                        nameList += '<li style="width:190px;display: block">' + reso[i] + '</li>';
                    }
                    nameList += '</ul>';
                    var html = compiled({ul: nameList, top: e.target.offsetTop + 30, left: e.target.offsetLeft});
                    $('#selectName').html(html);
                    //添加鼠标事件
                    $('#nameBox li').hover(function () {
                        $('#nameBox li').removeClass('cmail');
                        $(this).addClass('cmail');
                    },function () {
                        $(this).removeClass('cmail');
                    }).click(function () {
                                input.val($(this).html());
                                $mailBox.hide(0);
                            });
                    var $nameBox = $("#nameBox"), eindex = -1;
                    $this.focus(function () {
                        if ($this.val().split('.').length == 3) {
                            $nameBox.hide(0);
                        }
                    }).blur(function () {
                                setTimeout(function () {
                                    $nameBox.hide(0);
                                }, 10000);//
                            }).keyup(function (event) {
                                //下
                                if (event.keyCode == 40) {
                                    eindex++;
                                    if (eindex >= $('#nameBox li').length) {
                                        eindex = 0;
                                    }
                                    setUlTable(eindex);
                                } else if (event.keyCode == 38) {
                                    eindex--;
                                    if (eindex < 0) {
                                        eindex = $('#nameBox li').length - 1;
                                    }
                                    setUlTable(eindex);
                                } else if (event.keyCode == 13) {
                                    if (eindex >= 0) {
                                        vs[vs.length - 1] = $('#nameBox li').eq(eindex).html();
                                        $this.val(vs.join('.'));
                                        $nameBox.hide(0);
                                    }
                                } else {
                                    eindex = -1;
                                }
                                //如果在表单中，防止回车提交
                            }).keydown(function (event) {
                                if (event.keyCode == 13) {
                                    return false;
                                }
                            });
                    $("#nameBox").click(function (e) {
                        vs[vs.length - 1] = e.target.innerHTML;
                        $this.val(vs.join('.'));
                        $nameBox.hide(0);
                    });
                    $('body').click(function () {
                        $nameBox.hide(0);
                    });
                    function setUlTable(eindex) {
                        $("#nameBox>li").css('background-color', "#fff");
                        $($("#nameBox>li")[eindex]).css('background-color', "#ff0")
                    }
                }, 400);
            });
        })
    })
</script>
</div>