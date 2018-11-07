/*
* new Graph();
* addVertex
* last modified: 2015/05/11
*/
;(function(d3){
	/****************/
	var linearTo = (function(){
		function line(projection){
			var x = function(d){ return d[0]; },
				y = function(d){ return d[1]; },
				fx = function(d){ return d.x; },
				fy = function(d){ return d.y; },
				interpolate = function(points){ return points.join("L"); },
				defined = function(){ return true; }
			function line(data){
				var segments = [],
					segment = function(){ return segments.push("M", interpolate(projection(points), .7));},
					points = [],
					length = data.length,
					i = -1,
					d;
				while(++i < length){
					d = data[i];
					if(defined.call(this, d, i)){
						points.push([+fx.call(this, d, i), +fy.call(this, d, i)]);
					}
					else if(points.length){
						segment();
						points = [];
					}
				}
				if(points.length)
					segment();
				return segments.length ? segments.join("") : null;
			}
			line.x = function(v){
				x = v;
				return line;
			};
			line.y = function(v){
				y = v;
				return line;
			};
			return line;
		}
		return function(){
			return line(function(d){
				return d;
			});
		};
	})();
	/*************创建两个节点间的链接*************/
	function linkTo(a, b){
		var x1 = a.x,
			y1 = a.y,
			x2 = b.x,
			y2 = b.y,
			size = 16,//Math.max(a.size, b.size),
			min = 5000,
			ci = cj = 0;
		var p1 = pointTo(x1, y1, x2, y2, size, x1, y1),
			p2 = pointTo(x1, y1, x2, y2, size, x2, y2);
		for(var i = 1; i <= 3; i += 2){
			for(var j = 1; j <= 3; j += 2){
				var x = p1[i - 1] - p2[j - 1],
					y = p1[i] - p2[j],
					distance = Math.sqrt(x * x + y * y);
				if(distance < min){
					min = distance;
					ci = i;
					cj = j;
				}
			}
		}
		return [
			{"x": p1[ci - 1], "y": p1[ci]},//beginPoint
			{"x": p2[cj - 1], "y": p2[cj]}//endPoint
		];
	}
	function pointTo(x1, y1, x2, y2, size, cx, cy){
		var pointX = x2 - x1,
			pointY = y2 - y1,
			centerX = cx - x1,
			centerY = cy - y1;
		var pointSqrt = pointX * pointX + pointY * pointY,//平方面积
			pcSqrt = pointX * centerX + pointY * centerY,
			centerSqrt = centerX * centerX + centerY * centerY - size * size;
		var pBy2 = pcSqrt / pointSqrt,
			distance = pBy2 * pBy2 - centerSqrt / pointSqrt;
		var absFactor1 = -pBy2 + Math.sqrt(distance),
			absFactor2 = -pBy2 - Math.sqrt(distance);
		//
		return [
			x1 - pointX * absFactor1,
			y1 - pointY * absFactor1,
			x1 - pointX * absFactor2,
			y1 - pointY * absFactor2
		];
	}
	function interpolateNumber(a, b){
		b -= a = +a;
		return function(n){
			return a + b * n;
		};
	}
	function bezierForLine(source, target, step){
		var sy = 0,
			ty = 0;
		var curvature = .5 + (step | 0);

		var startX = source.x + source.width,
			startY = source.y + sy + source.height / 2;
		var x1 = target.x,
			xi = interpolateNumber(startX, x1),
			x2 = xi(curvature),
			x3 = xi(1 - curvature),
			y1 = target.y + ty + source.height / 2;
		return "M" + startX + "," + startY
			+ "C" + x2 + "," + startY
			+ " " + x3 + "," + y1
			+ " " + x1 + "," + y1;
	}
	/*
	 * Node
	 */
	function Node(key, value){
		this.key = key;
		this.value = value;
	}
	function Vertex(cx, cy){
		this.x = cx;
		this.y = cy;
		this.size = 32;
		this.value = null;
		this.weight = 0;
		this.key = 0;
		this.node = null;
		this.edgeTo = [];//一个顶点可以有多个边
		this.color = "none";
	}
	Vertex.prototype = {
		"draw": function(){
			V.drawTo(this);
			return this;
		},
		"redraw": function(){
			this.draw();
			return this;
		},
		addEdge: function(a, b){
			V.appendEdge(a, b);
		},				
		getValue: function(){
			return this.value;
		},
		setColor: function(newColor){
			this.color = newColor;
			return this;
		},
		getColor: function(newColor){
			return this.color;
		},
		setSize: function(newSize){
			this.size = newSize;
			return this;
		},
		getSize: function(){
			return this.size;
		},
		setKey: function(newKey){
			this.key = newKey;
			return this;
		},
		getKey: function(){
			return this.key;
		},
		setValue: function(newValue){
			V.appendValue(this, this.value = newValue);
			return this;
		},
		setWeight: function(weight){
			this.weight = weight;
			V.appendWeight(this);
			return this;
		},
		getWeight: function(){
			return this.weight;
		},
		move: function(x, y){
			this.x = x;
			this.y = y;
			return this;
		},
		removeVertex: function(){
			V.removeVertex(this);//.removeEdge(this);
		},
		removeEdge: function(){
			V.removeEdge(this);
		}
	}
	function Edge(v, w){
		this.v = v;
		this.w = w;//边
		this.color = "#333";
	}
	Edge.prototype = {
		setColor: function(newColor){
			this.color = newColor;
			return this;
		},
		getColor: function(){
			return this.color;
		},
		draw: function(){
			this.redraw();
			return this;
		},
		redraw: function(){
			V.drawEdge(this);
			return this;
		}
	};
	function Canvas(canvas, width, height){
		var canvas = d3.select(canvas).append("svg").attr({
			"width": width,
			"height": height
		});
		canvas.append("g").attr("class", "x-arrow").append("marker").attr({
			"id": "arrow",
			"viewBox": "0 -5 10 10",
			"refX": 9,
			"markerWidth": 3,
			"markerHeight": 3,
			"orient": "auto"
		}).append("path").attr({
			"d": "M0,-5 L10,0 L0,5",
			"fill": "#333"
		});
		var v = new V(canvas);
		this.canvas = canvas;
		this.vertex = v;
		this.edge = v.edge;
	}
	function V(canvas){
		var edge = canvas.append("g").attr({"class": "x-edge" }),
			vertex = canvas.append("g").attr({"class": "x-vertex" }),
			weight = canvas.append("g").attr({"class": "x-weight" });
		var linear = linearTo().x(function(d){
				return d.x;
			}).y(function(d){
				return d.y;
			});
		this.edge = edge;
		this.vertex = vertex;
		this.appendTo = function(v){
			v.node = vertex.append("g");
			v.node.append("circle").attr({
				"class": "node",
				"cx": 0,//v.x,
				"cy": 0,//v.y,
				"x": 0,//v.x - v.getSize() / 2,
				"y": 0,//v.y - v.getSize() / 2,
				"r": v.getSize() / 2,
				"fill": v.getColor(),
				"stroke-width": 0
			});
		};
		V.drawTo = function(v){
			v.node./*transition().duration(500).*/attr({"transform": "translate(" + v.x + "," + v.y + ")"});
			v.node.selectAll(".node").attr({
				// "cx": v.x * 0,
				// "cy": v.y * 0,
				// "x": 0,//v.x - v.getSize() / 2,
				// "y": 0,//v.y - v.getSize() / 2,
				"r": v.getSize() / 2,
				"fill": v.getColor()
			});
			// v.node.selectAll(".value").transition().duration(500).attr({
			// 	"x": 0,//v.x - 5,
			// 	"y": 5//v.y + 5
			// });
		};
		V.appendEdge = function(a, b){
			var link = linkTo(a, b),
				w = a.size / 2;
			w *= a.x - b.x >> 31 | 1;
			b.edgeTo[0].node = edge.append("path");
			b.edgeTo[0].node.attr({
				"id": "e-" + a.key,
				"class": "edge",
				"stroke": "#333",
				"stroke-width": 1.5,
				"fill": "none",
				//"d": linear([link[0], link[0]])
			})/*.style("marker-end", "url(#arrow)")*/.transition().duration(500).attr({
				"d": bezierForLine({x: a.x - 0, y: a.y, width: 0, height: 0}, {x: b.x + 0, y: b.y, width: 0, height: 0})//linear([link[0], link[1]])
			});
		};
		V.drawEdge = function(e){
			var a = e.v,
				b = e.w,
				link = linkTo(a, b),//v link to w
				w = a.size / 2;
			w *= a.x - b.x >> 31 | 1;
			e.node/*.transition().duration(500)*/.attr({
				"stroke": e.getColor(),
				"d": bezierForLine({x: a.x - 0, y: a.y, width: 0, height: 0}, {x: b.x + 0, y: b.y - 0, width: 0, height: 0})
			});
		};
		V.appendValue = function(v, value){
			v.node.selectAll(".value").remove();
			v.node.append("text").attr({
				"class": "value",
				"x": -17,//v.x - 5,
				"y": 5,//v.y + 5,
				//"fill": v.edgeTo[0].getColor()
			}).text(value);
		};
		V.removeVertex = function(v){					
			//删除边完成后删除顶点
			v.node.remove();
		};
		V.removeEdge = function(v){
			var edgeTo = v.edgeTo;
			//删除被指向的所有边
			for(var i = 0; i < edgeTo.length; i++){
				var vedge = edgeTo[i];
				//console.log(edgeTo, v.key, vedge.w.key);
				edgeTo[i].node.remove();
				//被删除的顶点key等于指向的顶点指向的b key，就能找出父指向的a key
				/*if(v.key == vedge.w.key){//注意v和w的对称关系
					var wedge = vedge.w.edgeTo;//父指向的边
					//删除父指向的边
					//path.parentNode.removeChild(path);
					console.log(vedge.w.edgeTo)
					for(var j = 0; j < wedge.length; j++){
						console.log(wedge[j].node[0][0])
						var path = wedge[j].node[0][0]//.node();
						if(path && path.parentNode)
							path.parentNode.removeChild(path);
					}
				}*/
			}
		};
		V.appendWeight = function(v){
			weight.append("text").attr({
				"id": "w" + v.weight,
				"fill": "#333",
				"font-size": 16,
				"font-weight": "bold",
				"text-anchor": "middle",
				"text-decoration": "underline"
			}).append("textPath").attr({
				"xlink:href": "#e-" + v.key,
				"startOffset": "10%"
			}).append("tspan").attr("dy", -5).text(v.weight);
		};
	}
	function Graph(canvas, options){
		var options = options || {},
			width = options.width || 500,
			height = options.height || 450;
		var canvas = new Canvas(canvas, width, height);
		this.canvas = canvas.canvas;
		this.vertex = canvas.vertex;
		this.edge = canvas.edge;
		this.vertexs = {};
		this.edges = [];
	}
	Graph.prototype.addVertex = function(cx, cy, key){
		var v = new Vertex(cx, cy).setKey(key);			
		this.vertex.appendTo(v);
		this.vertexs[v.getKey()] = v;
		return v;
	};
	Graph.prototype.addEdge = function(akey, bkey){
		//console.log(this.vertexs, akey, bkey)
		var a = this.vertexs[akey],
			b = this.vertexs[bkey],
			e = new Edge(a, b);
		this.edges.push(e);
		a.edgeTo.unshift(e);//边栈
		b.edgeTo.unshift(e);//共同边，如果b的顶点被删除，a指向b的边也被删除，找出其中的共同边
		a.addEdge(a, b);
		return e;
	};
	//删除顶点和所有指向和被指向的边
	Graph.prototype.removeVertex = function(key){
		//this.vertexs[key].remove();
		var vertex = this.vertexs[key];
		vertex.removeVertex();
		//删除边
		this.removeEdge(key);
		delete this.vertexs[key];
		return vertex;
	};
	Graph.prototype.removeEdge = function(key){
		var vertex = this.vertexs[key],
			edgeTo = vertex.edgeTo;
		vertex.removeEdge();
		while(edgeTo.length){
			edgeTo.pop();
		}
	};
	window["Graph"] = window.Graph || Graph;
})(d3);