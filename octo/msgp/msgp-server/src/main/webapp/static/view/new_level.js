$(document).ready(function() {
    $.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
    function reDraw() {
		window.localStorage.clear();

		var showConfigJson = JSON.stringify(window.showConfig);
		window.localStorage.setItem('showConfig', showConfigJson);

		window.location.reload();
	}
	function relateDom() {
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
	function filter(data) {
		// 过滤upper
		// 过滤qps
		data.forEach(function(ballData) {
			ballData.in = ballData.in.filter(function(inNode) {
				var qps = filterQPS(inNode.qps);
				var upper = filterUpper(inNode.upper90);
				if(window.showConfig.upper.indexOf(upper) === -1) return false;
				if(window.showConfig.qps.indexOf(qps) === -1) return false;
				return true;
			})
		})
		return data;
	}
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
				return 0;
			case value > 20 && value <= 50:
				return 1;
			case value > 50 && value <= 100:
				return 2;
			default:
				return 3;
		}
	}

	// 初始化配置
	window.apiConfig = {
		graphList: '/graph/level/idc',
		serverDesc: '/graph/serverDesc',
		invokeMsg: '/graph/invokeDesc',
		axes: '/graph/api/level/axes'
	};
	window.showConfig = {
		idc: 'all',
		sharpness: 2,
		colors: {
			"0": "#0f6",
			"1": "#ff0",
			"2": "#f90",
			"3": "#f00"
		},
		upper:[0,1,2,3],
		qps:[0,1,2,3],
		autoRange: true
	}
	if(window.localStorage.showConfig) {
		var tempConfig = window.localStorage.showConfig;
		window.showConfig = JSON.parse(tempConfig);
	}
	relateDom();

	// 初始化画布
	window.painter = document.getElementById('painter');
	if(!window.painter) {
		console.error('Init Error: vanGogh init faild');
		return;
	}
	var width = window.innerWidth - 1,
		height = window.innerHeight - 1;
	var van = new vanGogh(painter, width, height, window.showConfig.sharpness);
	var nexus = new Nexus(van);

	// 请求数据绘制nexus
	$.ajax({
		url: apiConfig.graphList,
		type: 'get',
		dataType: "JSON",
		data: {id: $.getUrlParam('id'), idc: window.showConfig.idc}
	}).done(function(data) {
		if(!data.isSuccess) return;
		if(!data.data) {
			$('#error').show();
			return;
		}
		var nodes = data.data.nodes;
		nodes = filter(nodes);
		nexus.nodes(nodes).links().fire();
	});
		

	// toolBar
	$('#legend').click(function() {
		$('#legendList').toggle();
	})

	$('#upper_switch').find('a').click(function(e) {
		var upper = this.getAttribute('data-index');
		var upper = parseInt(upper, 10);
		var index = window.showConfig.upper.indexOf(upper);
		if(index === -1) {
			window.showConfig.upper.push(upper);
		}else {
			window.showConfig.upper.splice(index, 1);
		}
		reDraw();
	})
	$('#qps_switch').find('a').click(function(e) {
		var qps = this.getAttribute('data-index');
		var qps = parseInt(qps, 10);
		var index = window.showConfig.qps.indexOf(qps);
		if(index === -1) {
			window.showConfig.qps.push(qps);
		}else {
			window.showConfig.qps.splice(index, 1);
		}
		reDraw();
	})
	$('#room_switch').find('a').click(function(e) {
		var room = this.getAttribute('data-room');
		window.showConfig.idc = room;
		reDraw();
	})
	$('#reset').click(function() {
		nexus.reset();
		var van = nexus.vanGogh;
		var stage = van.stage;
		stage.scale.x = window.showConfig.sharpness;
		stage.scale.y = window.showConfig.sharpness;
	})
	$('#refresh').click(function() {
		window.localStorage.clear();
		window.location.reload()
	})
	$('#zoomIn').click(function() {
		var van = nexus.vanGogh;
		var stage = van.stage;
		stage.scale.x += 0.2;
		stage.scale.y += 0.2;
		van.sharpness = stage.scale.x;
	})
	$('#zoomOut').click(function() {
		var van = nexus.vanGogh;
		var stage = van.stage;
		stage.scale.x -= 0.2;
		stage.scale.y -= 0.2;
		van.sharpness = stage.scale.x;
		var van = null;
		var stage = null;
	})
	
	// 画布被点击清空所有弹出框
	painter.onclick = function() {
		if(!painter.paopao){
			$('#infos').hide();
			$('#nodelist').hide();
			$('#legendList').hide();
		}
		painter.paopao = false;
	}
	// 画布拖动
	painter.onmousedown = function(e1) {
		var x1 = e1.clientX;
		var y1 = e1.clientY;
		var x = nexus.vanGogh.stage.x;
		var y = nexus.vanGogh.stage.y;
		if(this.paopao) {
			this.paopao = false;
			return;
		}
		this.onmousemove = function(e2) {
			var x2 = e2.clientX;
			var y2 = e2.clientY;
			nexus.vanGogh.stage.x = x2-x1+x;
			nexus.vanGogh.stage.y = y2-y1+y;
		}
	}
	document.onmouseup = function(e) {
		painter.onmousemove = null;
	}


});