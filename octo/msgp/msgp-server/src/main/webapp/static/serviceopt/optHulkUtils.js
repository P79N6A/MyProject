M.add('msgp-serviceopt/optHulkUtils', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkIdcList = getIdcList;
    Y.namespace('msgp.serviceopt').optHulkRichIdcList = getRichIdcList;
    Y.namespace('msgp.serviceopt').optHulkCellList= getCellList;
    Y.namespace('msgp.serviceopt').optHulkIdcName = getIdcName;
    Y.namespace('msgp.serviceopt').optHulkRegionName = getRegionName;
    Y.namespace('msgp.serviceopt').optHulkAppkey = getAppkey;
    Y.namespace('msgp.serviceopt').optHulkEnv = getEnv;
    Y.namespace('msgp.serviceopt').optHulkEnv2 = getEnv2;
    Y.namespace('msgp.serviceopt').optHulkIsOnline = isOnline;
    Y.namespace('msgp.serviceopt').optHulkUser = getUser;
    Y.namespace('msgp.serviceopt').optHulkCheckImagineExist = checkImagineExist;
    Y.namespace('msgp.serviceopt').optHulkCheckIsAccessIn = checkIsAccessIn;
    Y.namespace('msgp.serviceopt').optHulkOperationEntityType = getOperationEntityType;
    Y.namespace('msgp.serviceopt').optHulkCheckIsConf = checkIsConf;

    //环境相关
    var onlineEnvMap = {3: "prod", 2: "staging"};
    var offlineEnvMap = {3: "dev", 2: "ppe", 1: "test"};
    var idcList;//机房列表
    var cellList;//SET标识列表
    var isImagineExist;

    function getAppkey(){
        var appkey = Y.one('#apps_select').getAttribute('value');
        return appkey;
    }

    function getEnv(){
        var envNumber = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        var curUrl = window.location.href;
        if(curUrl.indexOf('octo.sankuai.com') >= 0 || curUrl.indexOf('octo.st.sankuai.com') >= 0){
            return onlineEnvMap[envNumber];
        }else{
            return offlineEnvMap[envNumber];
        }
    }

    function getEnv2(){
        var envNumber = Y.one('#manScale_env_select a.btn-primary').getAttribute('value');
        var curUrl = window.location.href;
        if(curUrl.indexOf('octo.sankuai.com') >= 0 || curUrl.indexOf('octo.st.sankuai.com') >= 0){
            return onlineEnvMap[envNumber];
        }else{
            return offlineEnvMap[envNumber];
        }
    }

    function isOnline(){
        var curUrl = window.location.href;
        if(curUrl.indexOf('octo.sankuai.com') >= 0 || curUrl.indexOf('octo.st.sankuai.com') >= 0){
            return true;
        }else{
            return false;
        }
    }

    function getUser(){
        var user = $("#logined_user_name_").attr("value");
        return user;
    }

    function checkImagineExist(){
        $.ajax({
            type: "get",
            url: '/hulk/checkIsImagineExist/' + getAppkey() + '/' + getEnv() + '/get',
            async: false,
            success: function (ret) {
                isImagineExist = ret.data;
            }
        });
        return isImagineExist;
    }

    function getOperationEntityType(){
        var operationEntityType = "unified_policy";
        if($("#addNewScalingPolicy").hasClass("btn-primary")){
            operationEntityType = "unified_policy";
        }else if($("#addNewPeriodicPolicy").hasClass("btn-primary")){
            operationEntityType = "periodic_policy";
        }else if($("#addModifySgGroup").hasClass("btn-primary")){
            operationEntityType = "tag_info";
        }
        return operationEntityType;
    }

    //同步获取所有机房列表,region(北京'bj',上海'sh',所有'')
    function getIdcList(region){
        if(!idcList){
            var idcListUrl = '/hulk/getIdcList?env=' + getEnv() + '&region=' + region;
            $.ajax({
                type: "get",
                url: idcListUrl,
                async: false,
                success: function (ret) {
                    if(ret && ret.idcList){
                        idcList = ret.idcList;
                    }
                }
            });
        }
        if(idcList){
            if(region){
                var regionIdcList = [];
                for(var index=0;index<idcList.length;index++){
                    if(idcList[index].region == region){
                        regionIdcList.push(idcList[index]);
                    }
                }
                return regionIdcList;
            }else{
                return idcList;
            }
        }
        return [];
    }

    //同步获取资源富余机房列表,region(北京'bj',上海'sh',所有''),origin(弹性'policy',一键扩容'manual_scale')
    function getRichIdcList(region, origin){
        var richIdcList = [];
        var richIdcListUrl = '/hulk/getRichIdcList?env=' + getEnv() + '&region=' + region + "&origin=" + origin;
        $.ajax({
            type: "get",
            url: richIdcListUrl,
            async: false,
            success: function (ret) {
                if(ret && ret.idcList){
                    if(region){
                        for(var index = 0;index < ret.idcList.length; index++){
                            if(ret.idcList[index].region == region){
                                richIdcList.push(ret.idcList[index]);
                            }
                        }
                    }else{
                        richIdcList = ret.idcList;
                    }
                }
            }
        });
        return richIdcList;
    }

    //同步获取SET标识列表
    function getCellList(){
        if(!cellList){
            var cellListUrl = '/hulk/getCellList?env=' + Y.msgp.serviceopt.optHulkEnv();
            $.ajax({
                type: "get",
                url: cellListUrl,
                async: false,
                success: function (ret) {
                    if(ret && ret.cellList){
                        cellList = ret.cellList;
                    }
                }
            });
        }
        if(cellList){
            return cellList;
        }else{
            return [];
        }
    }

    //获取idc的中文名称
    function getIdcName(idc){
        if(!idcList){
            idcList = getIdcList("");
        }
        if(idcList){
            for(var index=0;index<idcList.length;index++){
                if(idcList[index].idc == idc){
                    return idcList[index].name;
                }
            }
        }
        return idc;
    }

    //获取region中文名称
    function getRegionName(region) {
        if (region == "bj") {
            return "北京";
        }
        if (region == "sh") {
            return "上海";
        }
        if (!region) {
            return "";
        }
    }

    //检查权限
    function checkIsAccessIn() {
        var owtAllow = false;
        var ownerAllow = false;
        $.ajax({
            type: "get",
            url: "/hulk2-auth/check/hulk-allow?appkey=" + getAppkey(),
            async: false,
            success: function (ret) {
                if (ret.code == 200) {
                    owtAllow = true;
                } else {
                    owtAllow = false;
                }
            },
            failure: function (ret) {
                alert("error owt");
            }
        });
        $.ajax({
            type: "get",
            url: "/hulk2-auth/check/isOwner?appkey=" + getAppkey(),
            async: false,
            success: function (ret) {
                if (ret.code == 200) {
                    ownerAllow = true;
                } else {
                    ownerAllow = false;
                }
            },
            failure: function (ret) {
                alert("error owner");
            }
        });
        //3 all;2 not owner; 1 not owt;0 all not
        if (owtAllow && ownerAllow) {
            return 3;
        }
        if (owtAllow && !ownerAllow) {
            return 2;
        }
        if (!owtAllow && ownerAllow) {
            return 1;
        }
        if (!owtAllow && !ownerAllow) {
            return 0;
        }
    }

    //检查机器配置
    function checkIsConf() {
        var result = 0;
        $.ajax({
            type: "get",
            url: "/hulk2-auth/check/isConf?appkey=" + getAppkey() + "&env=" + getEnv(),
            async: false,
            success: function (ret) {
                if (ret.code == 200) {
                    result = 1;
                } else {
                   //
                }
            },
            failure: function (ret) {
                alert("error in");
            }
        });
        return result;
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'template',
        'w-base',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap'
    ]
});