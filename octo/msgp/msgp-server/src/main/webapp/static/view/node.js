// 节点类
function Node(option){
  // var o = {};
  this.name = option.name || "waimai_e";
  this.id = option.id || 0;
  this.in = option.in || [];
  this.out = option.out || [];
  this.type =  option.type || 0;
  this.x = option.x || 0;
  this.y = option.y || 0;
  //prototype
  this.svg = function(width, height){
    var s = svgWrapper;
    var w = width || 50, h = height || 40; 
    //draw
    var rect = s.rect(this.x, this.y, w, h);
    rect.attr({
      fill: colorMap[this.type],
      stroke: "#000",
      strokeWidth: strokeW,
    })
    // var name = this.name.replace("com.sankuai.","");
    var name = this.name;
    var text = s.text(this.x+rectW/2, this.y+rectH/2+3, name).attr({
      fontSize:12,
      textAnchor: "middle",
      width: rectW
    }); 
    var nodeText = svgWrapper.group(rect, text);
    nodeText.attr({
       
      // width: 112
    })      
    .data("id", this.name)
    .click(function(evt){
      drawLinks(evt, this.data("id"));
    });
    return rect;
  }
  // return o;
};
function UnknownNode(option){
  Node.call(this, option);
  //this.op
}
UnknownNode.prototype = new Node({});
UnknownNode.prototype.constructor = Node;
UnknownNode.prototype.svgCircle = function(r){
  var s = svgWrapper;
  var r=r || 20;
  var circle = s.circle(this.x, this.y, r);
  circle.attr({
    fill: colorMap[this.type],
    stroke: "#000",
    strokeWidth: strokeW,
  });
  return circle;
}
UnknownNode.prototype.svgText = function(){
  var s = svgWrapper;
  // var name = this.name.replace("com.sankuai.","");
  var name = this.name;
  var text = s.text(this.x, this.y+rectH/2+17, name).attr({
    fontSize:12,
    textAnchor: "middle",
    width: unknownIndent
  }); 
  // var nodeText = svgWrapper.group(circle, text);
  // nodeText.attr({}); 
}