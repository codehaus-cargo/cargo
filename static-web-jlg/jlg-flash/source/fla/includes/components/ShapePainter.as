/**
 * Flash 6 | Action Script 1 Component 
 * 
 *
**/
#include "functions/MppAligner.as"
#include "functions/DrawFunction.as"
function ShapePainter() {
	this.aligner = new MppAligner();	
}

ShapePainter.prototype.aligner;
ShapePainter.prototype.drawCircle = function (target_mc,radius, color, alpha) {
	
	var x:Number = radius;
    var y:Number = radius;
   	target_mc.beginFill(color, alpha); 
    target_mc.moveTo(x + radius, y);
   	target_mc.curveTo(radius + x, Math.tan(Math.PI / 8) * radius + y, Math.sin(Math.PI / 4) * radius + x, Math.sin(Math.PI / 4) * radius + y);
  	target_mc.curveTo(Math.tan(Math.PI / 8) * radius + x, radius + y, x, radius + y);
    target_mc.curveTo(-Math.tan(Math.PI / 8) * radius + x, radius+ y, -Math.sin(Math.PI / 4) * radius + x, Math.sin(Math.PI / 4) * radius + y);
    target_mc.curveTo(-radius + x, Math.tan(Math.PI / 8) * radius + y, -radius + x, y);
   	target_mc.curveTo(-radius + x, -Math.tan(Math.PI / 8) * radius + y, -Math.sin(Math.PI / 4) * radius + x, -Math.sin(Math.PI / 4) * radius + y);
   	target_mc.curveTo(-Math.tan(Math.PI / 8) * radius + x, -radius + y, x, -radius + y);
   	target_mc.curveTo(Math.tan(Math.PI / 8) * radius + x, -radius + y, Math.sin(Math.PI / 4) * radius + x, -Math.sin(Math.PI / 4) * radius + y);
	target_mc.curveTo(radius + x, -Math.tan(Math.PI / 8) * radius + y, radius + x, y);
    target_mc.endFill();
}


ShapePainter.prototype.paintRoundBorder = function (target_mc,radius, bgcolor , bordercolor, alpha , thickness) {
	
	//drawFrameInPos(target_mc, 0,0,radius,radius, bgcolor, 100);
	target_mc.createEmptyMovieClip("graphic_mc" , 1);
	var graphic_mc = target_mc.graphic_mc;
	graphic_mc.createEmptyMovieClip("border_mc",1);
	graphic_mc.createEmptyMovieClip("bg_mc", 2);
	this.drawCircle(graphic_mc.border_mc, radius, bordercolor,100);
	this.drawCircle(graphic_mc.bg_mc, radius - (thickness/2), bgcolor,100);
  	//this.aligner.center(graphic_mc.border_mc, graphic_mc.bg_mc);
	graphic_mc._x += graphic_mc._width / 2;
	graphic_mc._y += graphic_mc._height / 2;
	
}

ShapePainter.prototype.paintCircle = function (target_mc, radius, color, alpha ) {
	//drawFrameInPos(target_mc, 0,0,radius,radius, color, 100);
	target_mc.createEmptyMovieClip("graphic_mc", 1);
	var graphic_mc = target_mc.graphic_mc;
	this.drawCircle(graphic_mc, radius, color, alpha);
	graphic_mc._x += graphic_mc._width / 2;
	graphic_mc._y += graphic_mc._height / 2;
}