<!doctype html>
<html>
<head>
	<meta charset="utf-8" />
	<title>服务视图外卖</title>
    <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon"/>
    <link type="text/css" rel="stylesheet" href="/static/css/graph-level.css" media="all">
    <link rel="stylesheet" type="text/css" href="https://cs0.meituan.net/cf/twitter-bootstrap/3.2.0/css/bootstrap.min.css">
    <link href="/static/template/css/font-awesome.min.css" rel="stylesheet">
    ${body}
</head>
<body>
	<div class="header">
		<span class="logo"><a href="/dashboard">美团点评</a><span class="logo-sep"></span></span>
		<h1>服务治理平台</h1>
	</div>
	<div id="toolbar" class="toolbar">
		<div class="toolbar-row">
			<!--div class="toolbar-cell apps">
				<a href="javascript:void(0);" class="toolbar-btn" id="apps">所有应用</a>
				<div class="toolbar-list">
					<a href="javascript:void(0);">接入层</a>
					<a href="javascript:void(0);">业务逻辑层</a>
					<a href="javascript:void(0);">数据层</a>
					<a href="javascript:void(0);">外部应用</a>
					<a href="javascript:void(0);">未注册应用</a>
				</div>
			</div-->
			<div class="toolbar-cell legend">
				<a href="javascript:void(0);" class="toolbar-btn" id="legend">筛选</a>
				<div class="toolbar-list" id="legendList">
				</div>
			</div>
			<div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn reset" id="reset">重置</a></div>
			<div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn refresh" id="refresh">刷新</a></div>
			<div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn co-refresh" id="coRefresh">关闭自动刷新</a></div>
			<div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn zoom-in" id="zoomIn">放大</a></div>
			<div class="toolbar-cell"><a href="javascript:void(0);" class="toolbar-btn zoom-out" id="zoomOut">缩小</a></div>
		</div>
	</div>
	<div id="vis"></div>
	<div class="infos" id="infos">
		<h2>加载中...</h2>
		<div class="wrap">
			<div class="desc"><h3>服务信息</h3><p>{{name}}</p></div>
			<div class="desc"><h3>服务负责人</h3><p>{{owners}}</p></div>
			<div class="desc"><h3>机器信息</h3><p>{{load}}</p></div>
			<div class="desc"><h3>创建时间</h3><p>{{createTime}}</p></div>
		</div>
	</div>
	<div id="nodelist">
		<h3>loading...</h3>
		<table>
			<thead>
				<tr>
					<th class="first">接口</th>
					<th title="Desc">描述</th>
					<th title="Query Per Second">QPS</th>
					<th title="50%耗时">tp50(ms)</th>
					<th title="90%耗时">tp90(ms)</th>
					<th title="95%耗时">tp95(ms)</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>{{name}}</td>
					<td>{{nameDesc}}</td>
					<td>{{qps}}</td>
					<td>{{upper50}}</td>
					<td>{{upper90}}</td>
					<td>{{upper95}}</td>
				</tr>
			</tbody>
		</table>
	</div>
	<div id="loading"><div></div><span>加载中...</span></div>
	<div id="error"><div></div><span>无数据</span></div>
	<div class="copyright">powered by 基础架构团队</div>
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
</body>
</html>
