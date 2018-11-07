/**
 * [梵高构造函数]
 * @param  {[DOM]}    cotainer  [容器元素]
 * @param  {[Number]} width     [画布宽度]
 * @param  {[Number]} height    [画布高度]
 * @param  {[Number]} sharpness [清晰度]
 * @param  {[Number]} color     [颜色]
 * @return {[Bool]}             [返回创建结果]
 */
var vanGogh = function(cotainer, width, height, sharpness, color) {
	if(!cotainer) {
		console.error('Param Error: a cotainer is needed');
		return false;
	}
	this.width = width*sharpness || 1000;
	this.height = height*sharpness || 600;
	this.color = color || 0xFFFFFF;
	this.cotainer = cotainer;
	// this.bezierMap = {};
	this.sharpness = sharpness || 1;

	this.init();
	return true
}

/**
 * [初始化方法，会不断刷新画布]
 * @return {[void]} [无返回]
 */
vanGogh.prototype.init = function() {
	var self = this;
	this.stage = new PIXI.Stage(this.color, true);
	this.stage.ordered = false;
	this.renderer = PIXI.autoDetectRenderer(this.width, this.height, {antialias: true}); 
	this.stage.scale.x = this.sharpness;
	this.stage.scale.y = this.sharpness;
	this.stage.y = 115*this.sharpness;
	this.stage.x = 20*this.sharpness;
	this.renderer.backgroundColor = 0x303749;
	this.cotainer.appendChild(this.renderer.view);
	requestAnimationFrame(animate);

	var time = 0;
    function animate(){
    	time++;
    	// 排序
    	if(!self.stage.ordered){
    		self.stage.ordered = true;
    		self.stage.updateLayersOrder();
    	}
    	// 移动火苗
  		self.fireall(time%200/200);
	    self.renderer.render(self.stage);
	    requestAnimationFrame(animate);
	}
	this.stage.updateLayersOrder = function () {
	    self.stage.children.sort(function(a,b) {
	        a.zIndex = a.zIndex || 0;
	        b.zIndex = b.zIndex || 0;
	        return a.zIndex - b.zIndex
	    });
	}
};

/**
 * [绘制服务点]
 * @param  {[Number]} x      [服务点横坐标]
 * @param  {[Number]} y      [服务点纵坐标]
 * @param  {[Object]} info   [服务点信息]
 * @param  {[Number]} radius [服务点半径]
 * @param  {[Number]} color  [服务点颜色]
 * @return {[Graphics]}      [返回服务点对象]
 */
vanGogh.prototype.drawBall = function(x, y, info, radius, color) {
	if(!this.balls) {
		this.balls = [];
	}
	var self = this;
	var graphics = new PIXI.Graphics(true);
	graphics.zIndex = 100;
	x = x || 0;
	y = y || 0;
	radius = radius || 5;

	color = color || 0xFFFFFF;
	graphics.lineStyle(0);
	graphics.beginFill(color, 1);
	graphics.drawCircle(0, 0, radius);

	Object.defineProperties(graphics,{
		radius: {
			get: function() {
				return this.graphicsData[0].shape.radius;
			},
			set: function(radius) {
				this.graphicsData[0].shape.radius = radius;
			}
		}
	});
	graphics.drawInfo = function(color) {
		color = color || 0xFFFFFF;

		var text = this.info.name;
		var basicText = new PIXI.Text(text,{font : '10em Arial', fill : color, align : 'center'});
		basicText.x = -20;
		basicText.y = -30;
		basicText.scale.x = 0.15;
		basicText.scale.y = 0.15;
		this.addChild(basicText);
	}
	graphics.info = info || null;
	graphics.position.x = x;
	graphics.position.y = y;
	this.stage.addChild(graphics);
	this.stage.ordered = false;
	graphics.buttonMode = true;
	graphics.interactive = true;

	this.balls.push(graphics);
	return graphics;
};

/**
 * [绘制关联线条]
 * @param  {[Graphics]} ballFrom [起始服务点对象]
 * @param  {[Graphics]} ballTo   [目的服务点对象]
 * @param  {[Number]}   width    [线条宽度]
 * @param  {[Number]}   color    [线条颜色]
 * @return {[Graphics]}          [返回线条对象]
 */
vanGogh.prototype.line = function(ballFrom, ballTo, width, color) {
	if(!this.lines) {
		this.lines = [];
	}
	var self = this;
	var graphics = new PIXI.Graphics();
	graphics.zIndex = 10;


	graphics.ballFrom = ballFrom;
	graphics.ballTo = ballTo;
	graphics.qpsWidth = width || 1;
	graphics.upperColor = color || 0x000000;

	


	graphics.draw = function(width, color) {
		color = color || this.upperColor;
		this.clear();
		var p0 = graphics.p0 = {
			x: ballFrom.x,
			y: ballFrom.y
		}
		var p1 = graphics.p1 = {
			x: (ballFrom.x + ballTo.x) / 2,
			y: ballFrom.y
		}
		var p2 = graphics.p2 = {
			x: p1.x,
			y: ballTo.y
		}
		var p3 = graphics.p3 = {
			x: ballTo.x,
			y: ballTo.y
		}

		// 法向量
		var tempVar = (p3.y-p0.y) / (p3.x-p0.x);
		// debugger;
		var ny = Math.sqrt(1 / (1 + (tempVar*tempVar) ));
		var nx = -ny*tempVar;

		graphics.beginFill(color);
		graphics.lineStyle(0, 0xEEEEEE, 1);
		graphics.moveTo(p0.x, p0.y);
		graphics.bezierCurveTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
		graphics.lineTo(p3.x+nx*width, p3.y+ny*width);
		graphics.bezierCurveTo(p2.x+nx*width, p2.y+ny*width, p1.x+nx*width, p1.y+ny*width, p0.x+nx*width, p0.y+ny*width);
		graphics.endFill();
		graphics.alpha = Math.max(width * .25, .35);
	}
	graphics.draw(width, color);

	// graphics.p1 = {x:res_x, y:res_y};
	this.stage.addChild(graphics);
	this.stage.ordered = false;
	graphics.buttonMode = true;
	graphics.interactive = true;
	this.lines.push(graphics);

	return graphics;
};

/**
 * [绘制火苗]
 * @param  {[Graphics]} line [线条对象]
 * @return {[Graphics]}      [火苗对象]
 */
vanGogh.prototype.fire = function(line) {
	var self = this;
	if(!this.fires) {
		this.fires = [];
	}
	var sprite = PIXI.Sprite.fromImage('/static/img/graph_fire.png');
	
	sprite.zIndex = 1000;
	sprite.bezierMap = {};
	sprite.getBezier = function(t) {
		// debugger;
		sprite.line = line;
		sprite.p0 = line.p0;
		sprite.p1 = line.p1;
		sprite.p2 = line.p2;
		sprite.p3 = line.p3;
		// if(this.bezierMap[t]) return this.bezierMap[t];
		var x = self.bezier2(t,this.p0.x, this.p1.x, this.p2.x, this.p3.x);
		var y = self.bezier2(t,this.p0.y, this.p1.y, this.p2.y, this.p3.y);
		this.bezierMap[t] = [x,y];
		return this.bezierMap[t];
	}
	this.fires.push(sprite);
	line.addChild(sprite);
	this.stage.ordered = false;
};

/**
 * [根据贝塞尔公式计算火苗位置]
 * @param  {[Number]} t [时刻（0-1）]
 * @return {[void]}     [无返回]
 */

vanGogh.prototype.fireall = function(t) {
	var self = this;
	if(!this.fires) this.fires = [];
	this.fires.forEach(function(item) {

		var bezierPoint = item.getBezier(t);
		var bezierPointN = item.getBezier(t+0.01);
		var x = bezierPoint[0];
		var y = bezierPoint[1];
		var dx = bezierPointN[0];
		var dy = bezierPointN[1];

		var rotation = Math.atan((dy-y)/(dx-x));
		
		item.pivot.x = 180;
		item.pivot.y = 2;
		if(dx>x) {
			item.rotation = rotation;
		}else {
			item.rotation = rotation+Math.PI;
		}
		item.x = x;
		item.y = y;
		item.scale.x = 0.1;
	})
};
/**
 * [3阶贝塞尔函数]
 * @param  {[Number]} t  [时刻]
 * @param  {[Number]} p0 [坐标分量1]
 * @param  {[Number]} p1 [坐标分量2]
 * @param  {[Number]} p2 [坐标分量3]
 * @param  {[Number]} p3 [坐标分量4]
 * @return {[Number]}    [贝塞尔值]
 */

vanGogh.prototype.bezier2 = function(t, p0, p1, p2, p3) {
	var T = 1-t;
	var pow = Math.pow;
	var a = p0*pow(T,3) + 3*p1*t*pow(T,2) + 3*p2*pow(t,2)*T + p3*pow(t,3);
	return a;
}

