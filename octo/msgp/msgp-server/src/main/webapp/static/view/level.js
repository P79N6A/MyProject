;(function(document, callback){
	var width = window.innerWidth - 1,
		height = window.innerHeight - 1;
    var isDragging = false;
    var legends = [],
    	nexus,
    	graph;


	// ajax请求url统一配置 -- production
	var paths = {
		graphList: '/graph/level/graph',
		serverMsg: '/graph/serverMsg',
		invokeMsg: '/graph/invokeMsg',
		axes: '/graph/api/level/axes'
	};

	// ajax请求url统一配置 -- development
	/*
	var paths = {
		graphList: '/static/_mockData/graphList.json',
		serverMsg: '/static/_mockData/serverMsg.json',
		invokeMsg: '/static/_mockData/invokeMsg.json',
		axes: '/static/_mockData/axes.json'
	};*/


	var colors = {
		"0": "#0f6",
		"1": "#ff0",
		"2": "#f90",
		"3": "#f00"
	};
	var config = {
		canvas: {
			width: width * 2,
			height: height * 2,
            marginTop: 85
		},
		vertex: {
			size: 48,
			color: "#fff"
		},
		edge: {

		}
	};
	function valueToAngle(r, start, end){
		return d3.svg.arc().innerRadius(r - 5).outerRadius(r).startAngle(start).endAngle(end);
	}
	function Nexus(graph){
		this.matrix = [];
		this.balls = [];
		this.edges = [];
        this.dragger = [];
		this.graph = graph;
        this.hasDragging = false;
        this.selectedNode = null;
	}
	Nexus.prototype = {
		nodes: function(data){
			var matrix = this.matrix,
                me = this,
				graph = this.graph,
				balls = this.balls,
				length = data.length,
				i = j = 0,
                isDrag = false;
            for(i = 0; i <= length; i++){
                var rows = [];
                for(j = 0; j < length; j++){
                    rows[j] = 0;
                }
                matrix[i] = rows;
            }
			for(i = 0; i < length; i++){
				var t = data[i];
				var x = t.x || 0,
                    y = t.y || 0;//Math.random() * config.canvas.height / 1 + 32;
				x = Math.min(config.canvas.width - config.vertex.size, Math.max(x, config.vertex.size / 2));
				y = Math.min(config.canvas.height - config.vertex.size, Math.max(y, config.vertex.size / 2));
				var ball = graph.addVertex(x, y, i),
					angle = 0;
                ball.type = t.type;
				ball.out = t.out;
                ball["in"] = t["in"];
				ball.name = t.name;
				balls.push(ball);
				//if(matrix[i] == null)
				//	matrix[i] = [];
				matrix[i][i] = 1;//ball;//current ball
				t.hostCount = 0;
				for(var p in t.hosts){
					if(t.hosts.hasOwnProperty(p)){
						t.hostCount += t.hosts[p];
					}
				}
				for(var p in t.hosts){
					if(t.hosts.hasOwnProperty(p)){
						ball.node.append("path").attr({
							d: valueToAngle(config.vertex.size / 2, angle, Math.min(angle += t.hosts[p] / t.hostCount * Math.PI * 2, 2 * Math.PI)),
							"class": "pie",
							title: t.hosts[p],
							fill: colors[p]
						});
					}
				}
                var text = data[i].name;
                if(!!~text.indexOf("com.sankuai")){
                    text = text.substr(text.lastIndexOf(".") + 1);
                }
                else if(!!~text.indexOf("com.meituan")){
                    text = text.substr(text.lastIndexOf(".") + 1);
                }
				ball.setSize({
					"unknownnode": 36,
					"outnode": 26
				}[t.type] || config.vertex.size);
				text = text.substr(0, ball.size / 8) + (text.length > ball.size / 8 ?  ".." : "");
				ball.setValue(text).setColor(config.vertex.color).redraw();
				ball.node.attr({
					"class": "vertex x-vertex-current",
					"data-name": data[i].name,
                    "title": data[i].name
				});
				(function(ball, data){
					ball.node.data([{x: ball.x, y: ball.y}]).on("mouseover", function(){
						d3.select(this).selectAll(".value").style("fill", "#2DCB70").text(data.name);
					}).on("mouseout", function(){
						d3.select(this).selectAll(".value").style("fill", "rgb(37, 23, 35)").text(ball.value);
					}).on("click", function(){
                        d3.event.stopPropagation();
                    }).call(d3.behavior.drag()
						//.origin(function(d){ return d; })
						.on("dragstart", function(){
                            var i = 0;
							d3.event.sourceEvent.stopPropagation();
							ball.node.node().parentNode.appendChild(ball.node.node());
							d3.selectAll("body").classed("x-dragging", true);
						})
						.on("drag", function(d){
                            if(me.hasDragging){
    							d.x += d3.event.dx;
    							d.y += d3.event.dy;
    							ball.move(d.x, Math.max(d.y, ball.size / 2)).redraw();
    							if(ball.edgeTo.length){//有些顶点没有边
    								for(var j = 0; j < ball.edgeTo.length; j++) ball.edgeTo[j].redraw();
    							}
                                hideInfo();
                                hideTable();
                                isDrag = true;
                            }
						})
						.on("dragend", function(){
							var e = d3.event.sourceEvent;
							d3.selectAll("body").classed("x-dragging", false);
                            for(var j = 0; j < (ball.edgeTo || []).length; j++){//所有入度与出度
                                ball.edgeTo[j].animator.selectAll(".animator").attr("path", ball.edgeTo[j].node.attr("d"));
                            }
                            if(isDrag){
                                isDrag = false;
                                me.dragger.push(ball);
                                return;
                            }
                            else{
                            	me.selectedNode = ball;
                            	resetEdge(graph.edges, false);//reset
                            	resetLegend(legends);
								var mv = matrix[ball.key];

								for(i = 0; i < ball.edgeTo.length; i++){
									var edgeTo = ball.edgeTo[i];
									edgeTo.node.classed("x-edge-current", true);
									edgeTo.animator.classed("x-edge-animate-current", true);
									edgeTo.node.node().parentNode.appendChild(edgeTo.node.node());
								}

								for(i = 0; i < mv.length; i++){
									balls[i].node.classed("x-vertex-current", mv[i] !== 0);
								}

                                for(var i = 0; i < me.dragger.length; i++){
                                    for(var j = i + 1; j < me.dragger.length; j++){
                                        if(me.dragger[i] === me.dragger[j])
                                            me.dragger.splice(i, 1);
                                    }
                                }
                                e.stopPropagation();
                                x = e.x;//ball.x - x;
                                y = e.y;//ball.y - y;
                                var info = showInfo(graph.canvas, ball, x, y);
                                if(ball.type === "unknownnode"){
                                    info.title("未注册应用不可查询").position(x, y);
                                }
                                else{
									var idc = window.showConfig.machineRooms.all ? 'all' : window.showConfig.machineRooms.machine;
                                    $.ajax({
                                        url: "/graph/serverDesc",
										//url: paths.serverMsg,
                                        type: "GET",
                                        data: $.param({appkey: ball.name, idc: idc}),
																				dataType: "JSON"
                                    }).done(function(res){
                                        if(res && res.isSuccess === true){
                                            var loads = {
                                                    "0": "0.0~0.2",
                                                    "1": "0.2~0.4",
                                                    "2": "0.4~0.6",
                                                    "3": "0.6以上"
                                                },
                                                data;
                                            if(data = res.data){
                                                var load = [];
                                                for(var p in data.machines){
                                                    load.push("<span class='symbol' style='background-color:" +
                                                        colors[p] + "'></span>" +
                                                        loads[p] + " &nbsp;&nbsp;<b>" + data.machines[p] +  "</b>台</span><br />");
                                                }
                                                load = load.join("");
                                                load.length || (load = "无");
                                                var time = new Date(res.data.createTime*1000);
                                                time = time.toLocaleString();
                                                info.title(ball.name).text({
                                                    name: res.data.appkey + "(" + res.data.introduction + ")" || "无",
                                                    count: res.data.machinesCount,
                                                    owners: res.data.owners.map(function(item){
                                                        return item.name;
                                                    }).join("、"),
                                                    createTime: time,
                                                    load: load
                                                }).position(x, y);
                                            }
                                        }
                                    }).fail(function(xhr){
                                        info.title("获取失败：" +  xhr.status);
                                    });
                                }
                            }
						})
					);
				})(balls[i], data[i]);
			}
			return this;
		},
		links: function(data){
			var matrix = this.matrix,
                me = this,
				length = data.length,
				graph = this.graph,
				selectedNode = me.selectedNode,
				j = i = 0;
			this.edges = [];
			window.count = 0;
            function Link(nodes, i, fn){
                for(j = 0; j < nodes.length; j++){//out OR in
                    for(var k = 0; k < length; k++){
                        if(data[k].name === nodes[j].name){
                            fn && fn.call(nodes[j], i, k);
                            break;
                        }
                    }
                }
            }
            function linkTo(akey, bkey){window.count++;
                matrix[bkey][akey] = 1;
                matrix[akey][bkey] = matrix[bkey][akey];
                var upper = filterUpper(this.upper90),
                    qps = filterQPS(this.qps),
                    scale = 1,
                    vertexs = me.graph.vertexs,
                    what = this;
                var edge = graph.addEdge(bkey, akey)
                        .setColor(colors[upper])
                        .redraw();
                edge.link = {from: vertexs[bkey], to: vertexs[akey]};
                edge.node.attr({"stroke-width": Math.max(qps * scale, scale), "stroke-opacity": Math.max(qps * .35, .25)});
                edge.node.node().medge = edge;
                edge.upper = upper;
                edge.qps = qps;
                edge.upper90 = this.upper90;
                edge.xqps = this.qps;
                me.edges.push(edge);
                return {
                    bind: function(){
                        this.filter(edge).on("mouseover", function(){
                            this.setAttribute("stroke-width", edge.node.attr("stroke-width") * 4);
                        }).on("mouseout", function(){
                            this.setAttribute("stroke-width", Math.max(scale, edge.qps * scale));
                        }).on("click", function(e){
                            e = d3.event;
                            if(e.stopPropagation)
                                e.stopPropagation();
                            if(!isDragging && !!~edge.node.attr("class").indexOf("x-edge-current")){
                            	hideLegend();
                                var tt = toTables(edge);
								var idc = window.showConfig.machineRooms.all ? 'all' : window.showConfig.machineRooms.machine;
                                $.ajax({
                                    url: "/graph/invokeDesc",
                                    type: "GET",
                                    data: $.param({from: graph.vertexs[bkey].name, to: graph.vertexs[akey].name, idc: idc})
                                }).done(function(res){
                                    if(res && res.isSuccess === true){
                                        if(res.data){
											tt.title(
												res.data.fromDesc + '(' + res.data.from + ') 调用 ' + 
												res.data.toDesc + '(' + res.data.to + ') 的详情'
                                            	).text(res.data.invokeDesc || []);                                        }
                                    }
                                }).fail(function(xhr){});
                            }
                        });
						if(selectedNode){
							for(var i = 0; i < selectedNode.edgeTo.length; i++){
								var edgeTo = selectedNode.edgeTo[i];
								edgeTo.node.classed("x-edge-current", true);
								edgeTo.node.node().parentNode.appendChild(edgeTo.node.node());
							}
						}
                    },
                    filter: function(edge){
                    	var node = edge.node;
                    	return node.classed("x-edge-current", !selectedNode);
                    }
                }
            }
			for(i = 0; i < length; i++){
				var t = data[i],
					indegree = t["in"],
					outdegree = t["out"];//this name | in name | out name
					// for(j = 0; j < indegree.length; j++){
					// 	if(indegree[j].name === selectedNode.name){
					// 		linkTo.call(indegree[j], i, selectedNode.key).bind();
					// 		break;
					// 	}
                Link(outdegree, i, function(akey, bkey){
                	linkTo.call(this, bkey, akey).bind();
                });
                Link(indegree, i, function(akey, bkey){
                    linkTo.call(this, akey, bkey).bind();
                });
			}
			return this;
		},
		fire: function(data){
			var graph = this.graph,
				length = data.length,
				i = 0,
				edge;
			for(var i = 0; i < data.length; i++){
				var edge = data[i];//.v.edgeTo || [];
				var group = graph.edge.append("g")
					.attr({"class": "x-edge-animate"})
					.classed("x-edge-animate-current", !!~edge.node.attr("class").indexOf("x-edge-current"));
				group.append("path").attr({
					d: "M0,0 q38,15 40,0 q-2,-15 -40,0",
					fill: "#fff",
					transform: "scale(.25)",
					"opacity": 0.5
				});
				group.append("animateMotion").attr({
					path: edge.node.attr("d"),
					begin: "0s",
					dur: "4s",
					rotate: "auto",
					repeatCount: "indefinite",
					"class": "animator"
				});
				edge.animator = group;
			}
		}
	};

	function Toolbar(graph){
		this.tools = [];
		var methods = {
			"apps": function(){
				d3.select(this.parentNode).classed("on", true);
			},
			"legend": function(){
				d3.select(this.parentNode).classed("on", true);
			},
			"reset": function(){

			},
            "refresh": function(){

            },
			"zoomIn": function(){
				var view = graph.canvas,
					area = (view.attr("viewBox") || "").split(/\s+/);
					x = +area[0] | 0,
					y = +area[1] | 0,
					width = +area[2] || config.canvas.width,
					height = +area[3] || config.canvas.height;
				if(!(width ^ 1 && height ^ 1))
                    return;
                width = Math.max(width -= 100, 1);
                height = Math.max(height -= 100, 1);
                view.transition().duration(500).attr("viewBox", [x, y, width, height].join(" "));
			},
			"zoomOut": function(){
				var view = graph.canvas,
					area = (view.attr("viewBox") || "").split(/\s+/),
					x = +area[0] | 0,
					y = +area[1] | 0,
					width = +area[2] || config.canvas.width,
					height = +area[3] || config.canvas.height;
                //if(!(width ^ 1 && height ^ 1))
                 //   return;
                width = Math.max(width += 100, 1);
                height = Math.max(height += 100, 1);
                view.transition().duration(500).attr("viewBox", [x, y, width, height].join(" "));
			},
			"resize": function(){
				var width = window.innerWidth * 2,
					height = window.innerHeight * 2,
					area = (graph.canvas.attr("viewBox") || "").split(/\s+/),
					x = +area[0] | 0,
					y = +area[1] | 0,
                    boxWidth = width,
                    boxHeight = height;
                hideInfo();
				graph.canvas.attr({
					width: config.canvas.width = width,
					height: config.canvas.height = height,
					viewBox: [x, y, boxWidth, boxHeight].join(" ")
				});
			},
			"click": function(e){
				var e = e || window.event,
					target = e.target || e.srcElement,
					fn = function(o, c){
						while((o = o.parentNode) && o.nodeType === 1){
							if(o.getAttribute("class") && !!~o.getAttribute("class").indexOf(c))
								return 1;
						}
						return 0;
					};

				if(!fn(target, "vertex")){
					//resetVertex(nexus.balls, true);
					//resetEdge(nexus.edges, true);
					hideInfo();
				}
                if(!fn(target, "edge")){
                    hideTable();
                }
				var legend = d3.select("#legend").node().parentNode;
				if(!~(target.getAttribute("id") || "").indexOf("legend")
					&& !fn(target, "toolbar-item")){
					d3.select(legend).classed("on", false);
				}
			}
		};
		this.add = function(type, fn){
			this.tools.push(d3.select("#" + type).on("click", function(){
				type in methods && methods[type].call(this);
				fn && fn.call(this);
			}));
			return this;
		};
		this.bind = function(element, type){
			var timer;
			this.tools.push(d3.select(element).on(type, function(){
				var e = d3.event;
				if(type === "resize"){
					clearTimeout(timer);
					timer = setTimeout(function(){
						methods[type].call(element, e);
					}, 100);
				}
				else
					methods[type].call(element, e);
			}));
			return this;
		};
		this.drag = function(element){
			var xx = yy = 0;
			this.tools.push(element.call(d3.behavior.drag()
				.on("dragstart", function(e){
                    isDragging = false;
					xx = d3.event.sourceEvent.x;
					yy = d3.event.sourceEvent.y;
				})
				.on("drag", function(e){
					var view = d3.select(this),
						area = (view.attr("viewBox") || "").split(/\s+/),
						x = +area[0] | 0,
						y = +area[1] | 0,
						width = +area[2] || config.canvas.width,
						height = +area[3] || config.canvas.height;
					var e = d3.event.sourceEvent,
                        ex = Math.max(e.clientX, e.pageX), ey = Math.max(e.clientY, e.pageY);
					isDragging = true;
					x = +view.style("left").replace(/px/g, "");
					y = +view.style("top").replace(/px/g, "");
                    x -= xx - ex
                    y -= yy - ey;
                    xx = ex;
                    yy = ey;
                    //y = Math.max(y, 0);
                    view.style({left: x + "px", top: y + "px"});
					//view.attr("viewBox", [x, y, width, height].join(" "));
					hideInfo();
                    hideTable();
				})
				.on("dragend", function(){
					var view = d3.select(this),
						x = +view.style("left").replace(/px/g, ""),
						y = +view.style("top").replace(/px/g, "");
					//if(x > 0 || y > config.canvas.marginTop)
					//view.transition().style({left: 0, top: config.canvas.marginTop + "px"});
				})
			));
			return this;
		};
		this.fire = function(){
			while(this.tools.pop());
		};
	}
	function Legend(element){
        this.status = [];
        this.element = element.append("div").attr("class", "toolbar-item");
    }
    Legend.prototype = {
    	setClass: function(value){
    		this.element.classed(value, true);
    		return this;
    	},
        add: function(options){
            var element = this.element,
                tt = element.append("div").attr("class", "legend-item");
            this.data = options.value || [];
            tt.append("div").attr("class", "label").text(options.label || "");
            this.element = tt;
            return this;
        },
        fire: function(fn){
            var data = this.data,
                tpl = ["<span class='symbol' style='height:{{outsize}}px'><i style='background-color:{{color}};height:{{size}}px'></i></span>",
                    "<span class='title'>{{text}}</span>"
                ].join(""),
                me = this,
                link;
            for(var i = 0; i < data.length; i++){
                var t = new String(tpl);
                for(var p in data[i]){
                    t = t.replace(new RegExp("{{" + p + "}}", "g"), data[i][p]);
                }
                link = this.element.append("a").attr({
                    "href": "javascript:void(0);",
                    "class": "legend-btn"
                }).html(t);
                (function(link, i){
                    link.on("click", function(){
                        me.select(link, i);
                        fn && fn.call(link, me.status, i);
                    });
                })(link, i);
            }
            return this;
        },
        select: function(link, index){
            var status = this.status,
                flag = true;
            for(var i = 0; i < status.length; i++){
                if(status[i].index === index){
                    status.splice(i, 1);
                    flag = false;
                    break;
                }
            }
            if(flag)
                status.push({link: link, index: index});
            link.classed("legend-on", flag);
        },
        reset: function(){
            var status;
            while(status = this.status.pop())
                status.link.classed("legend-on", false);
        }
    };
    function resetVertex(vertexs, flag){
        for(var i = 0; i < vertexs.length; i++){
            vertexs[i].node.classed("x-vertex-current", flag);
        }
    }
    function resetEdge(edges, flag){
        for(var i = 0; i < edges.length; i++){
            edges[i].node.classed("x-edge-current", flag);
            edges[i].animator.classed("x-edge-animate-current", flag);
        }
    }
    function resetLegend(legends){
        for(var i = 0; i < legends.length; i++)
            legends[i].reset();
    }
	function reset(nexus){
		resetVertex(nexus.balls, true);
		resetEdge(nexus.edges, true);
		// resetLegend(legends);
        nexus.graph.canvas.transition().duration(500).attr("viewBox", [0, 0, config.canvas.width, config.canvas.height].join(" "));
	}
    function showLoading(flag){
        d3.select("#loading").classed("show", flag);
    }
	function hideInfo(){
		d3.select("#infos").style("display", "none");
	}
	function showInfo(canvas, ball, x, y){
		var size = ball.size / Math.E,
			el = d3.select("#infos"),
            title = el.selectAll("h2"),
            content = el.selectAll(".wrap"),
            area = canvas.attr("viewBox").split(/\s+/) || [],
            ch = +canvas.attr("height"),
            cw = +canvas.attr("width"),
            bw = +area[2] || 0,
            bh = +area[3] || 0,
            pos;
		if(!el.node().tpl){
			el.node().tpl = content.html();
		}
        function position(el, x, y){
            var dh = el.node().offsetHeight,
                dw = el.node().offsetWidth,
                mw = Math.abs(bw - cw),
                mh = Math.abs(ch - bh);
            //console.log(area, bw, cw, mw, mw * (bw - cw >> 31 | 1))
            x += size;// - mw * (bw - cw >> 31 | 1) / 2;
            y += size;// - mh * (bh - ch >> 31 | 1) / 2;
            //x = Math.min(x, config.canvas.width);
            if(y + dh > config.canvas.height / 2){
                y = y - dh - size * 2;
            }
            if(x + dw > config.canvas.width / 2){
                x = x - dw - size * 2;
            }
            return {x: x, y: y};
        }
        pos = position(el, x, y);
        title.html("加载中....");
        el.classed("loading", true).style({
            "left": pos.x + "px",
            "top": pos.y + "px",
            "display": "block"
        });
		return {
            title: function(text){
                title.html(text);
                return this;
            },
			text: function(values){
				var t = new String(el.node().tpl);
				for(var p in values){
					t = t.replace(new RegExp("{{" + p + "}}", "g"), values[p]);
				}
                el.classed("loading", false);
				content.html(t);
                return this;
			},
            position: function(x, y){
                pos = position(el, x, y);
                el.transition().style({
                    "left": pos.x + "px",
                    "top": pos.y + "px"
                });
            }
		}
	}
	function toTables(edge){
        var el = d3.select("#nodelist"),
            title = el.selectAll("h3");
        if(!el.node().tpl){
            el.node().tpl = el.selectAll("tbody").html();
        }
        title.html("加载中....");
        el.classed("loading", true).style({"display": "block"});
        return {
            title: function(text){
                title.html(text);
                return this;
            },
            text: function(values){
                var s = "";
                for(var i = 0; i < values.length; i++){
                    var t = new String(el.node().tpl);
                    for(var p in values[i]){
                    	if(p == 'createTime') {
                    		var tempTime = values[i][p];
                    		tempTime = new Date(tempTime*1000);
                    		tempTime = tempTime.toLocaleString();
                    		t = t.replace(new RegExp("{{" + p + "}}", "g"), values[i][p]);

                    	}else {
                        	t = t.replace(new RegExp("{{" + p + "}}", "g"), values[i][p]);		
                    	}
                    }
                    s += t;
                }
                el.classed("loading", false);
                el.selectAll("tbody").html(s);
            }
        }
    }
    function hideTable(){
        d3.select("#nodelist").style("display", "none");
    }
    function hideLegend(){
        d3.select("#toolbar .legend").classed("on", false);
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
	function filters(astatus, bstatus, afn, bfn){
    	var data = graph.edges,
    		balls = nexus.balls,
            length = data.length,
            i = j = 0,
            t;
        if(nexus.selectedNode){
			var mv = nexus.matrix[nexus.selectedNode.key],
				newBalls = [];
			for(var i = 0; i < mv.length; i++){
				balls[i].node.classed("x-vertex-current", mv[i] !== 0 && !newBalls.push(balls[i]));
			}
			balls = newBalls;//节点集
			data = nexus.selectedNode.edgeTo;//边
        }
        astatus = astatus.map(function(item){ return item.index; });
        bstatus = bstatus.map(function(item){ return item.index});
        resetVertex(balls, !astatus.length && !bstatus.length);
        resetEdge(data, !astatus.length && !bstatus.length);
        length = data.length;
        function set(t){
        	t.link.from.node.classed("x-vertex-current", true);
            t.link.to.node.classed("x-vertex-current", true);
            t.node.classed("x-edge-current", true);
            //t.node.node().parentNode.appendChild(t.node.node());
            t.animator.classed("x-edge-animate-current", true);//组合关系
        }
        for(; i < length; i++){
            t = data[i];
            t.node.classed("x-edge-current", !astatus.length && !bstatus.length);
            t.animator.classed("x-edge-animate-current", !astatus.length && !!~t.node.attr("class").indexOf("x-edge-current"));
        }
        var ret = [];
        for(i = 0; i < length; i++){
        	t = data[i];
        	for(j = 0; j < astatus.length; j++){
        		if(afn(t, +astatus[j])){
        			ret.push(t);
        			break;
        		}
        	}
        	!astatus.length && ret.push(t);
        }
        for(i = 0; i < ret.length; i++){
        	t = ret[i];
        	for(j = 0; j < bstatus.length; j++){
        		if(bfn(t, +bstatus[j])){
        			set(t);
        			break;
        		}
        	}
        	!bstatus.length && set(t);
        }
    }
    var sideData = [null, null, null];
    var activeList = 0;
    function newApps(data) {
    	if(activeList != 0) return;
    	if(sideData[0] === null) {
    		setTimeout(function(){
    			newApps();
    		}, 100);
    		return;
    	}
    	data = data || sideData[0];
    	var th = ['<thead>',
			'<tr>',
				'<th>appkey(服务名)</th>',
				'<th>负责人</th>',
				'<th>服务描述</th>',
                '<th>业务组</th>',
				'<th>创建时间</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{owners}</td>',
				'<td>{intro}</td>',
				'<td>{group}</td>',
				'<td>{createTime}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb,value;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				value = data[i][key];
				if(key === 'owners') {
					value = value.map(function(item) {
						return item.name
					}).join(',')
				}
				if(key === 'createTime') {
					value = new Date(value*1000).toLocaleString();
				}
				tempTb = tempTb.replace('{'+key+'}', value);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);

    }
    function newSpannames(data) {
    	if(activeList != 1) return;
    	if(sideData[1] === null) {
    		setTimeout(function(){
    			newSpannames();
    		}, 100);
    		return;
    	}
    	data = data || sideData[1];
    	var th = ['<thead>',
			'<tr>',
				'<th>appkey(服务名)</th>',
				'<th>api(接口名)</th>',
				'<th>调用量</th>',
				'<th>qps</th>',
				'<th>tp50</th>',
				'<th>tp90</th>',
				'<th>tp95</th>',
				'<th>tp99</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{spanname}</td>',
				'<td>{count}</td>',
				'<td>{qps}</td>',
				'<td>{tp50}</td>',
				'<td>{tp90}</td>',
				'<td>{tp95}</td>',
				'<td>{tp99}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				tempTb = tempTb.replace('{'+key+'}', data[i][key]);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);
    }
    function apiTop20(data) {
    	if(activeList != 2) return;
    	if(sideData[2] === null) {
    		setTimeout(function(){
    			apiTop20();
    		}, 100);
    		return;
    	}
    	data = data || sideData[2];
    	var th = ['<thead>',
			'<tr>',
				'<th>appkey(服务名)</th>',
				'<th>api(接口名)</th>',
				'<th>调用量</th>',
				'<th>qps</th>',
				'<th>tp50</th>',
				'<th>tp90</th>',
				'<th>tp95</th>',
				'<th>tp99</th>',
			'</tr>',
		'</thead>'].join();
		var tb = ['<tbody>',
			'<tr>',
				'<td>{appkey}</td>',
				'<td>{spanname}</td>',
				'<td>{count}</td>',
				'<td>{qps}</td>',
				'<td>{tp50}</td>',
				'<td>{tp90}</td>',
				'<td>{tp95}</td>',
				'<td>{tp99}</td>',
			'</tr>',
		'</tbody>'].join();
		var tempTb,resTb;
		for(var i=0; i<data.length; i++) {
			tempTb = tb;
			for(var key in data[i]) {
				tempTb = tempTb.replace('{'+key+'}', data[i][key]);
			}
			resTb += tempTb;
		}
		$('#side-table').html(th+resTb);
    }
	$(document).ready(function(){
		graph = new Graph("#vis", {"width": config.canvas.width, "height": config.canvas.height });
		nexus = new Nexus(graph);
		graph.canvas.attr("viewBox", [0, 0, config.canvas.width, config.canvas.height].join(" "));

        legends = [new Legend(d3.select("#legendList")).add({
            label: "服务调用性能",
            value: ["0~20ms", "20~50ms", "50~100ms", "100ms以上"].map(function(item, i){
                return {text: item, color: colors[i]};
            })
        }).fire(function(status){
            filters(legends[1].status, status, function(t, v){ return t.qps === v; }, function(t, v){ return t.upper === v});
        }), 
        new Legend(d3.select("#legendList")).setClass("line").add({
            label: "调用量",
            value: ["0~10", "10~100", "100~1000", "1000以上"].map(function(item, i){
                return {text: item, color: "#fff", size: i + 1, outsize: i + 4};
            })
        }).fire(function(status){
        	filters(legends[0].status, status, function(t, v){ return t.upper === v; }, function(t, v){ return t.qps === v});
        }), 
        new Legend(d3.select("#legendList")).setClass("line").add({
            label: "机房选择",
            value: ["廊坊机房", "永丰机房", "次渠机房", "大兴机房"].map(function(item, i){
                return {text: item, color: "#fff", size: 20, outsize: 24};
            })
        }).fire(function(status,i){
        	var machineList = ['lf','yf','cq','dx'];
        	status.forEach(function(item) {
        		if(item.index === i) return;
        		legends[2].select(item.link, item.index);
        	});

        	if(status.length === 0) {
        		showConfig.machineRooms.all = true;
        	}else {
        		showConfig.machineRooms.all = false;
        		showConfig.machineRooms.machine = machineList[i];
        	}

        	response(function(data){
        		nexus.nodes(data).links(data).fire(nexus.edges); 
        		resetVertex(nexus.balls, true);
				resetEdge(nexus.edges, true);
        	});
        }),
        new Legend(d3.select("#legendList")).setClass("line").add({
            label: "自动排列",
            value: ["关闭"].map(function(item, i){
                return {text: item, color: "#fff", size: 20, outsize: 24};
            })
        }).fire(function(status,i){
        	if(status.length>0) {
        		showConfig.autoRange = false;
        	}else {
        		showConfig.autoRange = true;
        	}

        	nexus.selectedNode = null;
            resetVertex(nexus.balls, true);

        	response(function(data){
        		nexus.nodes(data).links(data).fire(nexus.edges);
        	});
        }),
        new Legend(d3.select("#legendList")).setClass("line").add({
            label: "无关联服务",
            value: ["显示"].map(function(item, i){
                return {text: item, color: "#fff", size: 20, outsize: 24};
            })
        }).fire(function(status,i){
        	if(status.length>0) {
        		showConfig.noRelationship = true;
        	}else {
        		showConfig.noRelationship = false;
        	}

        	nexus.selectedNode = null;
            resetVertex(nexus.balls, true);

        	response(function(data){ 
        		nexus.nodes(data).links(data).fire(nexus.edges);
        	});
        })];

        var graphId = +$('#graphId').text();
        window.showConfig = {
        	noRelationship: false,
        	autoRange: true,
        	machineRooms: {
        		all: true,
        		machine: ''
        	}
        }

        new Toolbar(graph)
            .add("apps")
            .add("legend")
            .add("reset", function(){ reset(nexus); })
            .add("refresh", function(){
            	response(function(data){
            		graph.canvas.selectAll(".vertex").remove();
                	nexus.nodes(data).links(data).fire(nexus.edges);
                	reset(nexus);
            	});
            })
            .add("coRefresh", function(){
                var me = d3.select(this),
                	p = d3.select(this.parentNode);
                p.classed("on", !~p.attr("class").indexOf("on") ? (me.text("打开自动刷新"), Timer.stop()) : (me.text("关闭自动刷新"), Timer.start()));
            })
            .add("zoomIn")
            .add("zoomOut")
            .bind(window, "resize")
            .bind(document, "click")
            .drag(graph.canvas)
            .fire();

        function showFilter(data) {
        	var nodesMap = {};
        	var rightMap = {};
        	rightMap.count = 0;
        	var tempNode;
        	// 过滤无关联
        	if(!showConfig.noRelationship) {
        		for(var i = 0; i<data.length; i++) {
        			if(0 === data[i].in.length && 0 === data[i].out.length) {
        				data.splice(i,1);
        			}
        		}
        	}

        	// 自动排列
        	if(showConfig.autoRange) {
        		for(var i=0; i<data.length; i++) {
        			nodesMap[data[i].name] = data[i];
        		}
        		var getRight = function(node, path) {

        			var rightValue = 1;
        			// 防止死循环
        			path = path || {};
        			if(path[node.name]) {
        				return 0;
        			}else {
        				path[node.name] = true;
        			}

        			if(0 === node.out.length) {
        				return rightValue;
        			}else {
        				for(var i=0; i<node.out.length; i++) {
        					if(nodesMap[node.out[i].name]) {
        						rightValue = Math.max(rightValue, 1 + getRight(nodesMap[node.out[i].name], path));
        					}
        				}
        				return rightValue;
        			}
        		}

        		// 整理权图
        		for(var i=0; i<data.length; i++) {
        			tempNode = data[i];
        			// 获取路径深度权值
        			var tempRight = getRight(tempNode);
        			if(rightMap[tempRight]) {
        				rightMap[tempRight].push(tempNode);
        			} else {
        				rightMap[tempRight] = [];
        				rightMap[tempRight].push(tempNode);
        				rightMap.count++;
        			}
        		}
        		

        		var dep = rightMap.count;
        		for(var key in rightMap){
        			if(key == 'conut') return;
        			var item = rightMap[key];

        			for(var k=0; k<item.length; k++) {
						// item[k].x = (k * 60) + 25;
						// item[k].y = (dep - 1) * 90 + 25;

        				if(item.length < 20) {
        					item[k].x = (window.innerWidth-1)/2 + (k%2?-k:k+1) * 30;
        				}else {
        					item[k].x = (k * 60) + 25;
        				}

        				if(rightMap.count < 7) {
        					item[k].y = (dep - 1) * (window.innerHeight-1)/rightMap.count + 25;
        				}else {
        					item[k].y = (dep - 1) * 90 + 25;
        				}	
        			}
        			dep--;
        		}
        	}

        	return data;
        }
		function wait(){ setTimeout(function(){}, 100); return $.Deferred().resolve(); }
        function setXY(options){
            $.ajax({
                type: "PUT",
                //url: "/graph/api/level/axes",
								url: paths.axes,
                data: options.params,
								dataType: "JSON"
            }).done(function(res){
                options.success && options.success(res);
            }).fail(function(xhr){
                options.failure && options.failure(xhr);
            });
        }
        function response(fn){
            var dfd = $.Deferred();
            function level(){
            	var url = '';
            	if(showConfig.machineRooms.all) {
					url = "/graph/level/idc?id=" + graphId + '&idc=all';
            	} else {
            		 url = "/graph/level/idc?id=" + graphId + '&idc=' + showConfig.machineRooms.machine
            	}
                $.ajax({
                    type: "GET",
                    url: url,
                    dataType: "JSON"
                }).done(function(res){
                    if(res.isSuccess === true && res.data){
                    	$('#error').hide();
                        var data = res.data.nodes;
                        // 过滤数据
                        data = showFilter(data);

                        var ret = [];
                        //复制
                        for(var i = 0; i < data.length; i++){
                            ret.push(data[i]);
                        }
                        for(i = 0; i < res.data.outNodes.length; i++){
                            res.data.outNodes[i].type = "outnode"
                            ret.push(res.data.outNodes[i]);
                        }
                        for(i = 0; i < res.data.unknownNodes.length; i++){
                            res.data.unknownNodes[i].type = "unknownnode"
                            ret.push(res.data.unknownNodes[i]);
                        }
                        // nexus.hasDragging = res.data.auth === "write";
                        nexus.hasDragging = true;
                        fn && fn(ret);

                        dfd.resolve();  
                    }
                    else{
                        dfd.reject();
                        $('#error').show();
                    }
                    showLoading(false);
                }).fail(function(xhr){
                    showLoading(false);
                    dfd.reject();
                });
            }
            showLoading(true);
            //remove
            // for(var i = 0; i < nexus.balls.length; i++){
            //     graph.removeEdge(nexus.balls[i].key);
            // }
            for(var i=0; i<nexus.balls.length; i++) {
        		graph.removeVertex(i);
        	}
        	nexus.balls = [];
            graph.edges.splice(0, graph.edges.length);
            graph.canvas.selectAll(".x-edge-animate").remove();
            graph.canvas.selectAll(".edge").remove();

            nexus.edges.splice(0, nexus.edges.length);
            for(i = 0; i < nexus.edges.length; i++){
            	nexus.edges[i].animator = null;
            	nexus.edges[i].link = null;
            }
            hideInfo();
            hideTable();
            //hideLegend();
            if(nexus.dragger.length){
                setXY({
                    params: JSON.stringify({
                        graphId: graphId,
                        list: nexus.dragger.map(function(item){
                            return {"appkey": item.name, "x": item.x, "y": item.y };
                        })
                    }),
                    success: function(res){
                        nexus.dragger.splice(0, nexus.dragger.length);
                        if(res.isSuccess === true){
                            level();
                        }
                    },
                    failure: function(){
                        showLoading(false);
                    }
                });
            }
            else{
                level();
            }
            return dfd;
        }
        var Timer = {
            running: null,
            duration: 60000,
            start: function(){
                function fn(data){
					graph.canvas.selectAll(".vertex").remove();
                	nexus.nodes(data).links(data).fire(nexus.edges);
                	reset(nexus);                	
                }
                var queue = [];
                Timer.running = setInterval(function(){
                	if(!queue.length){
                		queue.push(response(fn));
                	}
                	else{
                		queue.shift();
                	}
                }, Timer.duration);
                return !1;
            },
            stop: function(){
                clearInterval(Timer.running);
                return !0;
            }
        };
		$.Deferred().resolve().pipe(wait).pipe(function(){
			Timer.stop();
			response(function(data){
                nexus.nodes(data).links(data).fire(nexus.edges);
            });
		}).done(function(){
			setTimeout(function(){
				Timer.start();
			}, Timer.duration);
		});

		// 服务列表
		$('.side-nav').click(function(e) {
			$(this).children().removeAttr('class');
			var target = e.target.parentNode;
			var index = $(target).attr('data-index');
			target.className = 'active';
			$('#side-table').html('加载中...');
			activeList = index;
			switch(index){
				case '0':
					newApps(sideData[0]);
					break;
				case '1':
					newSpannames(sideData[1]);
					break;
				case '2':
					apiTop20(sideData[2]);
					break;
			}
		})
		// 新加入服务
		$('#side-table').html('加载中...');
		$.ajax({
			type: "GET",
            url: "/graph/new/apps?id=" + graphId,
            dataType: "JSON"
		}).done(function(res) {
			if(res.isSuccess === true && res.data){
                var data = res.data;
                sideData[0] = data;
                newApps(sideData[0]);
            }
            else{
                console.log('error')
            }
		});
		// 新加入api
		$.ajax({
			type: "GET",
            url: "/graph/new/spannames?id=" + graphId,
            dataType: "JSON"
		}).done(function(res) {
			if(res.isSuccess === true && res.data){
                var data = res.data;
                sideData[1] = data;
            }
            else{
                console.log('error')
            }
		});
		// 性能最差api20
		$.ajax({
			type: "GET",
            url: "/graph/perfWorst/spannames?id=" + graphId,
            dataType: "JSON"
		}).done(function(res) {
			if(res.isSuccess === true && res.data){
                var data = res.data;
                sideData[2] = data;
            }
            else{
                console.log('error')
            }
		});
        window.onbeforeunload = function(e){
        	Timer.stop();
            response(function(data){
                nexus.links(data).fire(nexus.edges);
            });
            return "是否退出";
        };
        $(document).bind("contextmenu", function(e){
        	return false;
    	});
		callback && callback();
	});
}).call(this, document);
