// 箭头marker
var p1 = svgWrapper.path("M2,2 L2,15 L12,8 L2,2").attr({
  fill: "#000"   
});
var p2 = svgWrapper.path("M2,2 L2,15 L12,8 L2,2").attr({
  // fill: linkColor 
  fill: "#666"   
});
// var m2 = p1.marker(0, 0, 13, 13, 2, 6)
// 黑色箭头
var m1 = p1.marker(0, 0, 15, 15, 4, 8).attr({
            markerUnits:"userSpaceOnUse",
         });
// 灰色箭头
var m2 = p2.marker(0, 0, 15, 15, 4, 8).attr({
            markerUnits:"userSpaceOnUse",
         });

// 直线
function path_line(x1, y1, x2, y2) {
  var path = "M" + x1 + "," + y1 + "L" + x2 + "," + y2;
  return path;
};

// 同级曲线
function curveArrow(x1, y1, x2, y2){
  var path = "M"+x1+","+y1+" Q"+(x1+x2)/2+","+(y1+50)+ " "+x2+","+y2;
  return path
}

// 自引圆环
function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
  var angleInRadians = (angleInDegrees-90) * Math.PI / 180.0;

  return {
    x: centerX + (radius * Math.cos(angleInRadians)),
    y: centerY + (radius * Math.sin(angleInRadians))
  };
}

function describeArc(x, y, radius, startAngle, endAngle){

    var start = polarToCartesian(x, y, radius, endAngle);
    var end = polarToCartesian(x, y, radius, startAngle);

    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";

    var d = [
        "M", start.x, start.y, 
        "A", radius, radius, 0, arcSweep, 0, end.x, end.y
    ].join(" ");

    return d;       
}