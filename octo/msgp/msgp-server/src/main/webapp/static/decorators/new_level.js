$(document).ready(function() {

  window.showConfig = {
    idc: 'all',

    // 线条清晰度
    sharpness: 2,

    // 线条颜色配置
    colors: {
      '0': '#0f6',
      '1': '#ff0',
      '2': '#f90',
      '3': '#f00'
    },

    // 服务调用性能
    upper: [0, 1, 2, 3],

    // 服务调用量
    qps: [0, 1, 2, 3],


    autoRange: true
  };


  // 服务视图页面模块
  var serviceView = {

    vanGogh: null,
    nexus: null,

    /**
     * 初始化
     */
    init: function () {
      var self = this;

      // 初始化画布
      // 获取画布
      window.painter = document.getElementById('painter');

      if(!window.painter) {
        console.error('Init Error: vanGogh init faild');
        return;
      }

      var width = window.innerWidth - 1;
      var height = window.innerHeight - 1;

      // 监测是否有本地配置
      if(window.localStorage.showConfig) {
        var tempConfig = window.localStorage.showConfig;
        window.showConfig = JSON.parse(tempConfig);
      }

      var van = new vanGogh(painter, width, height, window.showConfig.sharpness);
      //var nexus = new Nexus(van);

      //van.drawRect();

      self.nexus = new Nexus(van);


      // 通过配置关联筛选器的各个选项
      self.relateDom();

      // 画图
      self.getAndDrawIdcs();

      // 放大
      self.canvasZoomIn();

      // 缩小
      self.canvasZoomOut();

      // 拖动
      self.canvasDrag();

      // 画布重置为选中某个节点前的状态
      self.canvasReset();

      // 刷新
      self.canvasRefresh();

      // 服务视图筛选
      self.filterServiceView();

      // 点击画布时清除所有对话框
      self.clearDialog();

    },

    /**
     * 获取并绘制节点及线
     */
    getAndDrawIdcs: function () {
      var self = this;

      // 请求数据绘制nexus
      $.ajax({
        url: $.decorator.API.idcList,
        type: 'get',
        dataType: 'JSON',
        data: {
          id: $.decorator.getUrlParam('id'),
          idc: window.showConfig.idc
        }
      }).done(function(data) {

        console.log(data);

        if(!data.isSuccess) {
          return;
        }

        if(!data.data) {
          $('#error').show();
          return;
        }

        // 权限
        $.decorator.auth = data.data.auth;

        var nodes = data.data.nodes;

        nodes = self.idcFilter(nodes);

        nodes = self._relocation(nodes);

        serviceView.nexus.nodes(nodes).links().fire();
        //serviceView.nexus.nodes(nodes);
        //console.log(serviceView.nexus);
      });
    },

    /**
     * 画布放大
     */
    canvasZoomIn: function () {
      $('#zoomOut').click(function() {
        var van = serviceView.nexus.vanGogh;
        var stage = van.stage;
        stage.scale.x -= 0.2;
        stage.scale.y -= 0.2;
        van.sharpness = stage.scale.x;
        var van = null;
        var stage = null;
      });
    },

    /**
     * 画布缩小
     */
    canvasZoomOut: function () {
      $('#zoomIn').click(function() {
        var van = serviceView.nexus.vanGogh;
        var stage = van.stage;
        stage.scale.x += 0.2;
        stage.scale.y += 0.2;
        van.sharpness = stage.scale.x;
      });
    },

    /**
     * 画布拖动
     */
    canvasDrag: function () {
      painter.onmousedown = function(e1) {
        var x1 = e1.clientX;
        var y1 = e1.clientY;
        var x = serviceView.nexus.vanGogh.stage.x;
        var y = serviceView.nexus.vanGogh.stage.y;
        if(this.paopao) {
          this.paopao = false;
          return;
        }
        this.onmousemove = function(e2) {
          var x2 = e2.clientX;
          var y2 = e2.clientY;
          serviceView.nexus.vanGogh.stage.x = x2-x1+x;
          serviceView.nexus.vanGogh.stage.y = y2-y1+y;
        }
      };

      document.onmouseup = function(e) {
        painter.onmousemove = null;
      };
    },

    /**
     * 画布刷新
     */
    canvasRefresh: function () {
      $('#refresh').click(function() {
        window.localStorage.clear();
        window.location.reload()
      });
    },

    /**
     * 画布重绘
     */
    _canvasReDraw: function () {
      window.localStorage.clear();

      var showConfigJson = JSON.stringify(window.showConfig);
      window.localStorage.setItem('showConfig', showConfigJson);

      window.location.reload();
    },

    /**
     * 画布重置为选中某个节点前的状态
     */
    canvasReset: function () {
      $('#reset').click(function() {
        serviceView.nexus.reset();
        var van = serviceView.nexus.vanGogh;
        var stage = van.stage;
        stage.scale.x = window.showConfig.sharpness;
        stage.scale.y = window.showConfig.sharpness;
      });
    },

    /**
     * 画布被点击时清空页面对话框
     */
    clearDialog: function () {
      painter.onclick = function() {
        if(!painter.paopao){
          //$('#infos').hide();
          $('.j-ball-info').hide();
          $('#nodelist').hide();
          $('#legendList').hide();
        }
        painter.paopao = false;
      };
    },

    /**
     * 筛选服务视图
     */
    filterServiceView: function () {

      // 刷选下拉框
      $('#legend').click(function() {
        $('#legendList').toggle();
      });

      // 服务调用性能筛选
      $('#upper_switch').find('a').click(function(e) {
        var upper = this.getAttribute('data-index');
        var upper = parseInt(upper, 10);
        var index = window.showConfig.upper.indexOf(upper);
        if(index === -1) {
          window.showConfig.upper.push(upper);
        }else {
          window.showConfig.upper.splice(index, 1);
        }
        serviceView._canvasReDraw();
      });

      // 调用量筛选
      $('#qps_switch').find('a').click(function(e) {
        var qps = this.getAttribute('data-index');
        var qps = parseInt(qps, 10);
        var index = window.showConfig.qps.indexOf(qps);
        if(index === -1) {
          window.showConfig.qps.push(qps);
        }else {
          window.showConfig.qps.splice(index, 1);
        }
        serviceView._canvasReDraw();
      });

      // 机房筛选
      $('#room_switch').find('a').click(function(e) {
        var room = this.getAttribute('data-room');
        window.showConfig.idc = room;
        serviceView._canvasReDraw();
      });
    },

    /**
     * idc数据筛选
     * @param data
     * @returns {*}
     */
    idcFilter: function (data) {
      var self = this;
      data.forEach(function(ballData) {
        ballData.in = ballData.in.filter(function(inNode) {
          var upper = self._idcFilterUpper(inNode.upper90);
          var qps = self._idcFilterQPS(inNode.qps);

          // 过滤upper，若配置里不存在这个数据则舍弃不显示
          if (window.showConfig.upper.indexOf(upper) === -1) {
            return false;
          }

          // 过滤qps
          if (window.showConfig.qps.indexOf(qps) === -1) {
            return false;
          }

          return true;
        })
      });

      return data;
    },

    /**
     * 坐标(x,y)为0的点,重新生成坐标
     * @param data
     * @returns [object]
     * @private
     */
    _relocation: function (data) {
      if (data instanceof Array) {
        var len = data.length;
        if(len > 1) {
          var ratioW = (window.innerWidth-1)/len;
          var ratio = window.innerHeight-1;
          for (var i = 0; i < len; i++) {
            if(data[i].x == 0 && data[i].y == 0) {
              data[i].x = ratioW*(i+0.1);
              data[i].y = Math.abs(Math.sin(data[i].x))* ratio;
            }
          }
        }
      }
      return data;
    },

    /**
     * 调用性能分级
     * @param value
     * @returns {number}
     * @private
     */
    _idcFilterUpper: function (value) {
      switch(true){
        case value <= 20:
          return 0;
        case value > 20 && value <= 50:
          return 1;
        case value > 50 && value <= 100:
          return 2;
        default:
          return 3;
      }
    },

    /**
     * 调用量分级
     * @param x
     * @returns {number}
     * @private
     */
    _idcFilterQPS: function (x) {
      if (x <= 10) {
        return 0;
      } else if (x > 10 && x <= 100) {
        return 1;
      } else if (x > 100 && x <= 1000) {
        return 2;
      } else {
        return 3;
      }
    },

    /**
     * 通过配置关联筛选选项
     */
    relateDom: function () {
      var showConfig = window.showConfig;
      var $upper_switch = $('#upper_switch').find('a');
      var $qps_switch = $('#qps_switch').find('a');
      var $room_switch = $('#room_switch').find('a');

      // 渲染服务性能
      if(showConfig.upper) {
        $upper_switch.attr('class', 'legend-btn');
        showConfig.upper.forEach(function(item) {
          $upper_switch[item].className = 'legend-btn legend-on';
        })
      }
      // 渲染调用量
      if(showConfig.qps) {
        $qps_switch.attr('class', 'legend-btn');
        showConfig.qps.forEach(function(item) {
          $qps_switch[item].className = 'legend-btn legend-on';
        })
      }
      // 机房筛选
      if(showConfig.idc) {
        $room_switch.attr('class', 'legend-btn');
        switch(showConfig.idc) {
          case 'lf':
            $room_switch[0].className = 'legend-btn legend-on';
            break;
          case 'yf':
            $room_switch[1].className = 'legend-btn legend-on';
            break;
          case 'cq':
            $room_switch[2].className = 'legend-btn legend-on';
            break;
          case 'dx':
            $room_switch[3].className = 'legend-btn legend-on';
            break;
          default:
            $room_switch.attr('class', 'legend-btn legend-on');
            break;
        }
      }
    }

  };


  serviceView.init();

});
