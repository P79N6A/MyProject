(function(){
  /**
   * [关联图构造函数]
   * @param {[vanGogh]} vanGogh [梵高对象]
   */
  function Nexus(vanGogh){
    // this.matrix = [];
    this.balls = [];
    this.lines = [];
    this.fires = [];
    this.vanGogh = vanGogh;
    this.nodeMap = {};
    this.lineMap = {};
    this.fireMap = {};
    this.nodeInfo= {};
  }

  /**
   * 展示节点信息
   * @param ball 小球
   * @param appkey 节点appkey
   */
  Nexus.prototype.showInfo = function(ball, appkey) {

    var self = this;
    var $ballInfo = $('.j-ball-info');

    if (self.nodeInfo.hasOwnProperty(appkey)) {
      var ballInfoData = self.nodeInfo[appkey];

      $.extend(ballInfoData, {
        auth: $.decorator.auth
      });

      var fnTpl = doT.template( $('.j-tpl-ball-info').html() );
      $ballInfo.html(fnTpl({
        data: ballInfoData
      }));
      console.log(ballInfoData);
    }

    // 显示小球弹层
    $ballInfo.show();

    // 服务信息修改开始
    var $intro = $ballInfo.find('.intro');
    var $btnIntroUpdate = $ballInfo.find('.j-btn-update-toggle');
    var $introForm = $ballInfo.find('.intro-update-form');
    var $iptNewIntro = $introForm.find('input');
    var $btnOk = $introForm.find('.j-btn-ok');

    // 修改框显示隐藏
    $btnIntroUpdate.click(function () {
      var self = $(this);
      if (self.data('toggle') === 'close') {
        self.text('取消修改');
        self.data('toggle', 'open');
        $introForm.show();
      } else {
        self.text('修改服务信息');
        self.data('toggle', 'close');
        $introForm.hide();
      }
    });

    // 提交修改
    $btnOk.click(function () {
      if ($iptNewIntro.val() === '') {
        alert('描述信息不能为空');
      } else {
        $btnOk.text('处理中');
        $.ajax({
          type: 'PUT',
          url: $.decorator.API.updateServerDesc,
          data: {
            appkey: appkey,
            idc: window.showConfig.idc,
            introduction: $iptNewIntro.val()
          }
        }).done(function (res) {

          if (res.isSuccess) {
            // 修改显示为新的描述
            $intro.text(res.data.intro);
            ball.children[0].text = appkey + "(" + res.data.intro + ")";

            // 修改self.nodeInfo
            for (var name in self.nodeInfo) {
              if (name === appkey) {
                self.nodeInfo[name].introduction = res.data.intro;
              }
            }

            // 请求完成后的一些处理
            $btnOk.text('提交');
            $btnIntroUpdate.text('修改服务信息');
            $iptNewIntro.val('');
            $btnIntroUpdate.data('toggle', 'close');
            $introForm.hide();

            console.log(ball);
            console.log(self.nodeInfo);

          } else {
            alert('error!');
          }

          console.log(res);

        }).fail(function (err) {
          console.log('Update ballInfo error!');
        });
      }
    });

  };

  /**
   * [展示调用信息]
   * @param  {[String]} from [起始点appkey]
   * @param  {[String]} to   [目标点appkey]
   * @return {[void]}        [无返回]
   */
  Nexus.prototype.showLineInfo = function(from, to) {
    painter.paopao = true;
    var tpl = ['<table>',
      '<caption><span>{{fromDesc}}</span>调用<span>{{toDesc}}</span>的详情</caption>',
      '<thead>',
      '<tr>',
      '<th class="first">接口</th>',
      '<th title="Desc">描述</th>',
      '<th title="Query Per Second">QPS</th>',
      '<th title="50%耗时">tp50(ms)</th>',
      '<th title="90%耗时">tp90(ms)</th>',
      '<th title="95%耗时">tp95(ms)</th>',
      '</tr>',
      '</thead>',
      '<tbody>',
      '{{list}}',
      '</tbody>',
      '</table>'].join('');
    var tplList = ['<tr>',
      '<td>{{name}}</td>',
      '<td>{{nameDesc}}</td>',
      '<td>{{qps}}</td>',
      '<td>{{upper50}}</td>',
      '<td>{{upper90}}</td>',
      '<td>{{upper95}}</td>',
      '</tr>'].join('');
    var $nodeList = $('#nodelist');
    $('#infos').hide();
    $nodeList.html('<caption><span>加载调用信息...</span></caption>');
    $nodeList.show();
    $.ajax({
      url: $.decorator.API.invokeMsg,
      type: 'GET',
      dataType: 'json',
      data: $.param({from: from, to: to, idc: window.showConfig.idc})
    }).done(function(res){
      // debugger;
      if(!res.isSuccess) return;
      var data = res.data;
      for (var key in data) {
        tpl = tpl.replace('{{'+key+'}}', data[key]);
      }
      var body = '';
      body = data.invokeDesc.map(function(item) {
        var li = tplList;
        for(var key in item) {
          li = li.replace('{{'+key+'}}', item[key]);
        }
        return li;
      });
      tpl = tpl.replace('{{list}}', body.join(''));
      $nodeList.html(tpl);
      $nodeList.show();
    })
  };

  /**
   * [根据数据绘制所有服务点]
   * @param  {[Array]} data [服务点数据]
   * @return {[Nexus]}      [返回自身]
   */
  Nexus.prototype.nodes = function(data) {
    var nexus = this;

    data.forEach(function(item) {

      // 绘制小球, 并把graphic对象赋给ball
      var ball = nexus.vanGogh.drawBall(item.x, item.y, item);

      // 绘制小球描述
      ball.drawInfo();

      //
      $.ajax({
        type: 'GET',
        url: $.decorator.API.serverDesc,
        dataType: 'JSON',
        data: {
          appkey: item.name,
          idc: window.showConfig.idc
        }
      }).done(function(data) {

        if(!data.isSuccess) {
          return;
        }

        var data = data.data;

        var loads = {
          "0": "0.0~0.2",
          "1": "0.2~0.4",
          "2": "0.4~0.6",
          "3": "0.6以上"
        };

        var load = [];

        for(var p in data.machines){
          load.push("<span class='symbol' style='background-color:" +
            window.showConfig.colors[p] + "'></span>" +
            loads[p] + " &nbsp;&nbsp;<b>" + data.machines[p] +  "</b>台</span><br />");
        }

        load = load.join('');
        load.length || (load = '无');
        data.load = load;

        ball.children[0].text = item.name + "(" + data.introduction + ")";
        nexus.nodeInfo[item.name] = data;
      });

      ball.on('click', function () {
        var self = this;
        if (self.dragging) {
          return;
        }

        var appkey = self.info.name;
        painter.paopao = true;
        nexus.showInfo(ball, appkey);
        nexus.focus(self);
      });

      ball.on('mouseover', function () {
        var self = this;
        self.scale.x = 1.2;
        self.scale.y = 1.2;
        self.children[0].tint = 0x2DCB70;
        self.children[0].text = self.info.name;
      });

      ball.on('mouseout', function () {
        var self = this;
        self.scale.x = 1;
        self.scale.y = 1;
        var text = nexus.nodeInfo[self.info.name];
        if(text) text = item.name + "(" + text.introduction + ")";
        self.children[0].tint = 0xFFFFFF;
        self.children[0].text = text;
      });

      // 拖动需要有权限
      if ($.decorator.auth === 'write') {
        ball.on('mousedown', function () {
          console.log('down');
          painter.paopao = true;
          //ball.drag = false;
          ball.dragging = false;

          ball.on('mousemove', function (info) {
            ball.dragging = true;
            var x_global = info.data.global.x;
            var y_global = info.data.global.y;
            var x_stage = nexus.vanGogh.stage.x;
            var y_stage = nexus.vanGogh.stage.y;
            this.x = (x_global-x_stage)/nexus.vanGogh.sharpness;
            this.y = (y_global-y_stage)/nexus.vanGogh.sharpness;

            // 线条重绘
            var name = ball.info.name;
            console.log(name);
            nexus.lineMap[name].forEach(function(line) {
              line.draw(line.qpsWidth);
            })
          });

          ball.on('mouseup', function () {
            ball.off('mousemove');

            if(!ball.dragging) {
              return;
            }

            // setBallPosition参数
            var _reqParams = {
              graphId: parseInt($.decorator.getUrlParam('id')),
              list: [
                {
                  appkey: ball.info.name,
                  x: ball.x,
                  y: ball.y
                }
              ]
            };

            setBallPositon({
              params: JSON.stringify(_reqParams)
            });
          });

        });
      }

      nexus.nodeMap[item.name] = ball;
      nexus.balls.push(ball);
    });

    return this;
  };

  /**
   * [根据数据绘制所有调用线条]
   * @return {[Nexus]} [返回自身]
   */
  Nexus.prototype.links = function() {
    var self = this;
    self.balls.forEach(function(ball) {

      ball.info.in.forEach(function(inNode) {
        var ballfrom = self.nodeMap[inNode.name];

        if(ballfrom) {
          var upper = filterUpper(inNode.upper90);
          var qps = filterQPS(inNode.qps);
          var width = Math.max(qps, 1)

          var line = self.vanGogh.line(ballfrom, ball, width, upper);

          line.click = function(info) {
            var from  = this.ballFrom.info.name;
            var to = this.ballTo.info.name;
            self.showLineInfo(from, to);
          };

          line.mouseover = function(info) {
            this.draw(width+2);
            // this.draw
          };

          line.mouseout = function(info) {
            this.draw(width);
          };

          self.lines.push(line);

          if(!self.lineMap[inNode.name]) {
            self.lineMap[inNode.name] = [];
          }

          if(!self.lineMap[ball.info.name]) {
            self.lineMap[ball.info.name] = [];
          }

          self.lineMap[inNode.name].push(line);
          self.lineMap[ball.info.name].push(line);
        }
      });

    });

    return this;
  };

  /**
   * [根据数据绘制所有火苗]
   * @return {[Nexus]} [返回自身]
   */
  Nexus.prototype.fire = function() {
    var self = this;
    this.lines.forEach(function(line) {
      var fire = self.vanGogh.fire(line);
      self.fires.push(fire);
    });
    return this;
  };

  /**
   * [聚焦某个服务点]
   * @param  {[Graphics]} ball [服务点对象]
   * @return {[void]}          [无返回]
   */
  Nexus.prototype.focus = function(ball) {
    this.balls.forEach(function(item) {
      item.alpha = 0.1;
      item.children[0].alpha = 0;
    });

    this.lines.forEach(function(item) {
      // item.alpha = 0;
      item.visible = false;
    });

    var relationLines = this.lineMap[ball.info.name];

    if(relationLines) {
      relationLines.forEach(function(item) {
        // item.draw(item.qpsWidth);
        item.visible = true;
        item.ballFrom.alpha = 1;
        item.ballFrom.children[0].alpha = 1;
        item.ballTo.alpha = 1;
        item.ballTo.children[0].alpha = 1;
      });
    }

    ball.alpha = 1;
    ball.children[0].alpha = 1;
  };

  /**
   * [重置页面页面]
   * @return {[void]} [无返回]
   */
  Nexus.prototype.reset = function() {
    this.balls.forEach(function(ball) {
      ball.alpha = 1;
      ball.children[0].alpha = 1;
    });
    this.lines.forEach(function(line) {
      // line.draw(line.qpsWidth);
      line.visible = true;
    });
  };

  window.Nexus = Nexus;

  function filterQPS(x) {
    if (x <= 10) {
      return 0;
    } else if (x > 10 && x <= 100) {
      return 1;
    } else if (x > 100 && x <= 1000) {
      return 2;
    } else
      return 3;
  }

  function filterUpper(value){
    switch(true){
      case value <= 20:
        return 0x00ff66;
      case value > 20 && value <= 50:
        return 0xffff00;
      case value > 50 && value <= 100:
        return 0xff9900;
      default:
        return 0xff0000;
    }
  }


  /**
   * 设置小球坐标，拖动后触发
   * @param options [params,success,failure]
   */
  function setBallPositon(options){
    $.ajax({
      type: 'PUT',
      url: $.decorator.API.axes,
      data: options.params,
      dataType: 'JSON'
    }).done(function(res){
      options.success && options.success(res);
    }).fail(function(xhr){
      options.failure && options.failure(xhr);
    });
  }

})();
