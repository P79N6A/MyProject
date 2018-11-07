<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>服务视图 - 服务治理平台</title>
  <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon">

  <!-- css library -->
  <link rel="stylesheet" type="text/css" href="https://cs0.meituan.net/cf/twitter-bootstrap/3.2.0/css/bootstrap.min.css">
  <link rel="stylesheet" type="text/css" href="/static/template/css/font-awesome.min.css">

  <!-- decorators css -->
  <link rel="stylesheet" type="text/css" href="/static/decorators/graph-level.css">
  <link rel="stylesheet" type="text/css" href="/static/decorators/newgraph.css">
  <link rel="stylesheet" type="text/css" href="/static/decorators/main.css">
</head>
<body>

  <div class="header">
    <span class="logo"><a href="/dashboard">美团点评</a><span class="logo-sep"></span></span>
    <h1>服务治理平台</h1>
  </div>

  <div id="toolbar" class="toolbar">
    <div class="toolbar-row">
      <div class="toolbar-cell legend">
        <a href="javascript:void(0);" class="toolbar-btn" id="legend">筛选</a>
        <div class="toolbar-list" id="legendList">
          <div class="toolbar-item" id="upper_switch">
            <div class="legend-item">
              <div class="label">服务调用性能</div>
              <a href="javascript:void(0);" class="legend-btn" data-index="0">
                  <span class="symbol">
                  <i style="background-color:#0f6;"></i>
                  </span><span class="title">0~20ms</span>
              </a>
              <a href="javascript:void(0);" class="legend-btn" data-index="1">
                  <span class="symbol">
                  <i style="background-color:#ff0;"></i></span>
                <span class="title">20~50ms</span>
              </a>
              <a href="javascript:void(0);" class="legend-btn" data-index="2">
                  <span class="symbol">
                  <i style="background-color:#f90;"></i>
                  </span>
                <span class="title">50~100ms</span>
              </a>
              <a href="javascript:void(0);" class="legend-btn" data-index="3">
                <span class="symbol"><i style="background-color:#f00;"></i></span><span class="title">100ms以上</span>
              </a>
            </div>
          </div>
          <div class="toolbar-item line" id="qps_switch">
            <div class="legend-item">
              <div class="label">调用量</div>
              <a href="javascript:void(0);" class="legend-btn" data-index="0">
                <span class="symbol" style="height:4px"><i style="background-color:#fff;height:1px"></i></span><span class="title">0~10</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-index="1">
                <span class="symbol" style="height:5px"><i style="background-color:#fff;height:2px"></i></span><span class="title">10~100</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-index="2">
                <span class="symbol" style="height:6px"><i style="background-color:#fff;height:3px"></i></span><span class="title">100~1000</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-index="3">
                <span class="symbol" style="height:7px"><i style="background-color:#fff;height:4px"></i></span><span class="title">1000以上</span></a>
            </div>
          </div>
          <div class="toolbar-item line" id="room_switch">
            <div class="legend-item">
              <div class="label">机房选择</div>
              <a href="javascript:void(0);" class="legend-btn" data-room="lf"><span class="symbol" style="height:24px"><i style="background-color:#fff;height:20px"></i></span><span class="title">廊坊机房</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-room="yf"><span class="symbol" style="height:24px"><i style="background-color:#fff;height:20px"></i></span><span class="title">永丰机房</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-room="cq"><span class="symbol" style="height:24px"><i style="background-color:#fff;height:20px"></i></span><span class="title">次渠机房</span></a>
              <a href="javascript:void(0);" class="legend-btn" data-room="dx"><span class="symbol" style="height:24px"><i style="background-color:#fff;height:20px"></i></span><span class="title">大兴机房</span></a>
            </div>
          </div>
        </div>
      </div>
      <div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn reset" id="reset">重置</a></div>
      <div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn refresh" id="refresh">刷新</a></div>
      <div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn zoom-in" id="zoomIn">放大</a></div>
      <div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn zoom-out" id="zoomOut">缩小</a></div>
    </div>
  </div>

  <!-- 节点信息弹窗 -->
  <div class="ball-info j-ball-info">
    <div class="msgp-loading">Loading...</div>
  </div>

  <!-- 节点信息弹窗模板 -->
  <script type="text/x-dot-template" class="j-tpl-ball-info">
    {{
      var data = it.data;
    }}
    <div class="wrap">
      <div class="desc">
        <h3>服务信息</h3>
        <p class="intro">{{= data.introduction }}</p>
        {{
          if (data.auth === 'write') {
        }}
        <div class="intro-update">
          <a class="btn btn-success btn-xs j-btn-update-toggle" data-toggle="close" href="javascript:;">修改服务信息</a>
          <div class="intro-update-form">
            <input type="text" class="input-sm" placeholder="请输入新的描述信息">
            <a class="btn btn-success btn-sm j-btn-ok" href="javascript:;">提交</a>
          </div>
        </div>
        {{
          }
        }}
      </div>
      <div class="desc">
        <h3>服务负责人</h3>
        <p>
          {{
            var ownerStr = '';
            data.owners.forEach(function (owner) {
              ownerStr += owner.name;
            });
          }}
          {{= ownerStr }}
        </p>
      </div>
      <div class="desc">
        <h3>机器信息</h3>
        <p>
          {{= data.load }}
        </p>
      </div>
      <div class="desc">
        <h3>创建时间</h3>
        <p>{{= new Date(data.createTime*1000).toLocaleString() }}</p>
      </div>
    </div>
  </script>

  <!--
  <div class="infos" id="infos">
    <h2>加载中...</h2>
  </div>
  -->

  <div id="nodelist">
    <h3>loading...</h3>
  </div>


  <div id="loading"><div></div><span>加载中...</span></div>
  <div id="error"><div></div><span>无数据</span></div>

  <!--<div class="copyright">powered by 基础架构团队</div>-->

  <div class="side-list">
    <div class="side-switch glyphicon glyphicon-menu-hamburger">
      接口列表
    </div>
    <div class="side-wrap">
      <ul class="nav nav-tabs side-nav">
        <li role="presentation" data-index="0" class="active"><a href="#">新加入服务</a></li>
        <li role="presentation" data-index="1"><a href="#">新加入方法</a></li>
        <li role="presentation" data-index="2"><a href="#">性能最差api-Top20</a></li>
      </ul>
      <div class="side-content">
        <table class="table" id="side-table">
        </table>
      </div>
    </div>
  </div>

  <div id="painter">
    <div></div>
  </div>

  <!-- js librarys -->
  <script type="text/javascript" src="/static/js/jquery.min.js"></script>
  <script type="text/javascript" src="/static/decorators/pixi.js"></script>
  <script type="text/javascript" src="/static/decorators/doT.min.js"></script>

  <!-- decorators js -->
  <script type="text/javascript" src="/static/decorators/decorator.js"></script>
  <script type="text/javascript" src="/static/decorators/graph.js"></script>
  <script type="text/javascript" src="/static/decorators/nexus.js"></script>
  <script type="text/javascript" src="/static/decorators/new_level.js"></script>
  <script type="text/javascript" src="/static/decorators/side_list.js"></script>

</body>
</html>
