<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>服务治理价值报告 - 服务治理平台</title>
    <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" type="text/css" href="/static/css/chart.css">
</head>
<body>

<div class="control-group">
    <label style="padding-left:1em">组织部门</label>
    <select id="business" placeholder="组织部门">
        <option value="">所有</option>
        <option value="0">到店事业群</option>
        <option value="1">技术工程部</option>
        <option value="2">猫眼电影</option>
        <option value="3">创新业务部</option>
        <option value="4">酒店旅游事业群</option>
        <option value="5">外卖配送事业群</option>
        <option value="6">云计算部</option>
        <option value="7">金融发展部</option>
        <option value="8">支付平台部</option>
        <option value="9">智能餐厅部</option>
        <option value="10">IT工程部</option>
        <option value="100">其他</option>
    </select>

<div class="container-echart">
    <div class="chart-main" id="j-chart-main"></div>
    <div class="chart-footer" id="j-chart-cat-axis">
        <div class="chart-cat-axis">
            <div class="item">Q1</div>
            <div class="item">Q2</div>
            <div class="item">Q3</div>
            <div class="item">Q4</div>
        </div>
    </div>
</div>


<script type="text/javascript" src="https://cs0.meituan.net/cf/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript" src="/static/worth/report.js"></script>
<script>
    var project = "${project}";
    var business = "${business}";
    getData(project,business)
</script>
</body>
</html>
