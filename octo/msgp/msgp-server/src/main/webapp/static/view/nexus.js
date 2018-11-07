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
	 * [展示节点信息]
	 * @param  {[String]} appkey [节点appkey]
	 * @return {[void]}          [无返回]
	 */
	Nexus.prototype.showInfo = function(appkey) {
		var tpl = [
		'<div class="wrap">',
			'<div class="desc"><h3>服务信息</h3><p>{{introduction}}</p></div>',
			'<div class="desc"><h3>服务负责人</h3><p>{{owners}}</p></div>',
			'<div class="desc"><h3>机器信息</h3><p>{{load}}</p></div>',
			'<div class="desc"><h3>创建时间</h3><p>{{createTime}}</p></div>',
		'<div>'].join('');
		var data;
		$('#infos').html('<h2>加载中...</h2>');
		$('#infos').show();
		$('#nodelist').hide();
		if(this.nodeInfo[appkey]) {
			data = this.nodeInfo[appkey];
			var tempTpl = tpl;
			// debugger;
			for (var key in data) {
				var value = data[key];
				if(key === 'owners') {
					value = value.map(function(item) {
						return item.name;
					}).join(',');
				}
				if(key === 'createTime') {
					value = new Date(value*1000)
					value = value.toLocaleString();
					// tempTpl = tempTpl.replace('{{}}')
				}
				tempTpl = tempTpl.replace('{{'+key+'}}', value);

			}
			$('#infos').html(tempTpl);
		}else {
			$.ajax({
				url: apiConfig.serverDesc,
				type: 'get',
				dataType: 'json',
				data: {appkey: appkey, idc: window.showConfig.idc}
			}).done(function(data) {
				if(!data.isSuccess) {
					$('#infos').html('加载失败');
					return;
				}
				if(!data.data) {
					$('#infos').html('<h2>无数据</h2>');
					return;
				}
				var data = data.data;
				var loads = {
                    "0": "0.0~0.2",
                    "1": "0.2~0.4",
                    "2": "0.4~0.6",
                    "3": "0.6以上"
                }
				var load = [];
                for(var p in data.machines){
                    load.push("<span class='symbol' style='background-color:" +
                        window.showConfig.colors[p] + "'></span>" +
                        loads[p] + " &nbsp;&nbsp;<b>" + data.machines[p] +  "</b>台</span><br />");
                }
                load = load.join("");
                load.length || (load = "无");
                data.load = load;
				var tempTpl = tpl;
				// debugger;
				for (var key in data) {
					var value = data[key];
					if(key === 'owners') {
						value = value.map(function(item) {
							return item.name;
						}).join(',');
					}
					if(key === 'createTime') {
						value = new Date(value*1000)
						value = value.toLocaleString();
						// tempTpl = tempTpl.replace('{{}}')
					}
					tempTpl = tempTpl.replace('{{'+key+'}}', value);

				}
				$('#infos').html(tempTpl);
			})
		}
	}

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
            url: window.apiConfig.invokeMsg,
            type: "GET",
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
	}

	/**
	 * [根据数据绘制所有服务点]
	 * @param  {[Array]} data [服务点数据]
	 * @return {[Nexus]}      [返回自身]
	 */
	Nexus.prototype.nodes = function(data) {
		var nexus = this;
		data.forEach(function(item) {
			var ball = nexus.vanGogh.drawBall(item.x, item.y, item);
			ball.drawInfo();
			$.ajax({
				url: apiConfig.serverDesc,
				dataType: 'json',
				type: 'get',
				data: {appkey: item.name, idc: window.showConfig.idc}
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
                }
				var load = [];
                for(var p in data.machines){
                    load.push("<span class='symbol' style='background-color:" +
                        window.showConfig.colors[p] + "'></span>" +
                        loads[p] + " &nbsp;&nbsp;<b>" + data.machines[p] +  "</b>台</span><br />");
                }
                load = load.join("");
                load.length || (load = "无");
                data.load = load;
				
				ball.children[0].text = data.introduction;
				nexus.nodeInfo[item.name] = data;
			});
			var clickHandler = function(info) {
				if(this.drag) {
					return ;
				}
				var self = this;
				var appkey = this.info.name;
				painter.paopao = true;
				nexus.showInfo(appkey);
				nexus.focus(this);
			}
			ball.click = clickHandler;
			ball.mouseover = function(info) {
				this.scale.x = 1.2;
				this.scale.y = 1.2;
				this.children[0].tint = 0x2DCB70;
				this.children[0].text = this.info.name;
			}
			ball.mouseout = function(info) {
				this.scale.x = 1;
				this.scale.y = 1;
				var text = nexus.nodeInfo[this.info.name];
				if(text) text = text.introduction;
				this.children[0].tint = 0xFFFFFF;
				this.children[0].text = text;
			}
			ball.mousedown = function() {
				painter.paopao = true;
				ball.drag = false;
				ball.mousemove = function(info) {
					ball.drag = true;
					var x_global = info.data.global.x;
					var y_global = info.data.global.y;
					var x_stage = nexus.vanGogh.stage.x;
					var y_stage = nexus.vanGogh.stage.y;
					this.x = (x_global-x_stage)/nexus.vanGogh.sharpness;
					this.y = (y_global-y_stage)/nexus.vanGogh.sharpness;

					// 线条重绘
					var name = ball.info.name;
					nexus.lineMap[name].forEach(function(line) {
						line.draw(line.qpsWidth);
					})

				}
				ball.mouseup = function() {
					ball.mousemove = null;
					if(!ball.drag) return;
					setXY({
	                    params: JSON.stringify({
	                        graphId: +$.getUrlParam('id'),
	                        list: {"appkey": ball.info.name, "x": ball.x, "y": ball.y }
	                    })
	                });
				}
			}
			nexus.nodeMap[item.name] = ball;
			nexus.balls.push(ball);
		})
		return this;
	}

	/**
	 * [根据数据绘制所有调用线条]
	 * @return {[Nexus]} [返回自身]
	 */
	Nexus.prototype.links = function() {
		var self = this;
		this.balls.forEach(function(ball) {
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
					}
					line.mouseover = function(info) {
						this.draw(width+2);
						// this.draw
					}
					line.mouseout = function(info) {
						this.draw(width);
					}
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
			}) 
		})
		return this;
	}

	/**
	 * [根据数据绘制所有火苗]
	 * @return {[Nexus]} [返回自身]
	 */
	Nexus.prototype.fire = function() {
		var self = this;
		this.lines.forEach(function(line) {
			var fire = self.vanGogh.fire(line);
			self.fires.push(fire);
		})
		return this;
	}

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
	}

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
	}

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
	function setXY(options){
        $.ajax({
            type: "PUT",
			url: window.apiConfig.axes,
            data: options.params,
			dataType: "JSON"
        }).done(function(res){
            options.success && options.success(res);
        }).fail(function(xhr){
            options.failure && options.failure(xhr);
        });
    }
})();
