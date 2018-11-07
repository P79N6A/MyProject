/**
 * 服务视图通用js
 */

'use strict';

$(function () {

  // 环境配置
  // 线上环境     production
  // 本地开发环境  development
  // 提交代码前需要修改 ENV 为 production
  var ENV = 'production';

  /**
   * 服务视图展示公共
   * 公共方法，全局变量全部挂载在 $.decorator 上
   */
  var decorator = {

    // API配置 production
    API: {
      // 获取idc列表，包括各个节点等
      idcList: '/graph/level/idc',

      // 服务描述
      serverDesc: '/graph/serverDesc',

      // 调用信息
      invokeMsg: '/graph/invokeDesc',

      // 修改小球坐标
      axes: '/graph/api/level/axes',

      // 服务信息修改
      updateServerDesc: '/graph/serverDesc',

      // 新加入服务
      newService: '/graph/new/apps',

      // 新加入API
      newAPI: '/graph/new/spannames',

      // 性能最差API
      perfWorstAPI: '/graph/perfWorst/spannames'
    },

    // APIDev配置 development
    APIDev: {
      // 获取idc列表，包括各个节点等
      idcList: '/static/_mockData/graph/level/idc.json',


      serverDesc: '/static/_mockData/graph/serverDesc.json',

      // 调用信息
      invokeMsg: '/static/_mockData/graph/invokeDesc.json',

      // 修改小球坐标
      axes: '/static/_mockData/graph/api/level/axes.json',

      // 服务信息修改
      updateServerDesc: '/static/_mockData/graph/serverDescUpdate.json',

      // 新加入服务
      newService: '/static/_mockData/graph/new/apps.json',

      // 新加入API
      newAPI: '/static/_mockData/graph/new/spannames.json',

      // 性能最差API
      perfWorstAPI: '/static/_mockData/graph/perfWorst/spannames.json'
    },

    // 节点视图显示配置
    showConfig: {

    },

    // 权限控制
    auth: null,

    /**
     * 获取url参数
     * @param name 参数名
     * @returns {null}
     */
    getUrlParam: function (name) {
      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
      var r = window.location.search.substr(1).match(reg);

      if (r != null) {
        return unescape(r[2]);
      }

      return null;
    }

  };


  // 赋给$对象
  $.decorator = decorator;

  if (ENV === 'development') {
    $.decorator.API = decorator.APIDev;
  }

});
