#include "functions/DrawFunction.as"

function IconDisplay() {}

IconDisplay.prototype.icon_color = 0xFFFFFF;
IconDisplay.prototype.setColor = function (newcolor) {
	this.icon_color = newcolor;
}

IconDisplay.prototype.maximizeIcon = function ( target) {	
	drawFrameInPos(target, 0,0, 10,10,this.icon_color, 100);
	drawFrameFade(target, 0,0, 10,10, this.icon_color, 100, 1, "IN", 20);
	drawPixelHLine(target, this.icon_color, 2, 2, 7, 100);
	drawPixelHLine(target, this.icon_color, 2, 3, 7, 100);
}

IconDisplay.prototype.crossIcon = function ( target ) {
	target.createEmptyMovieClip("cross_mc", 1);
	drawPixelHLine(target.cross_mc, this.icon_color, 0, 5, 12, 100);
	drawPixelHLine(target.cross_mc, this.icon_color, 0, 6, 12, 100);
	drawPixelVLine(target.cross_mc, this.icon_color, 5, 0, 12, 100);
	drawPixelVLine(target.cross_mc, this.icon_color, 6, 0, 12, 100);
}

IconDisplay.prototype.rotatedCrossIcon = function ( target) {
	target.createEmptyMovieClip("cross_mc", 1);
	drawPixelHLine(target.cross_mc, this.icon_color, -6, -1, 12, 100);
	drawPixelHLine(target.cross_mc,  this.icon_color, -6, 0, 12, 100);
	
	drawPixelVLine(target.cross_mc,  this.icon_color, -1, -6, 12, 100);
	drawPixelVLine(target.cross_mc, this.icon_color , 0, -6, 12, 100);
	/*Mark
	drawPixelPoint(target.cross_mc, 0x000000, -6,-1, 100);
	drawPixelPoint(target.cross_mc, 0x000000, -6,0, 100);
	drawPixelPoint(target.cross_mc, 0xCC0000, 0,-6, 100);
	drawPixelPoint(target.cross_mc, 0xCC0000, -1,-6, 100);
	*/
	target.cross_mc._x += target.cross_mc._width / 2;
	target.cross_mc._y += target.cross_mc._width / 2;
	target.cross_mc._rotation = 45;
}

IconDisplay.prototype.slopedIcon = function ( target_mc,  startPoint , endPoint, alpha, thickness){
	target_mc.moveTo(startPoint.getX(), startPoint.getY());
	target_mc.beginFill(this.icon_color,alpha);
	if(Math.abs(startPoint.getX() - endPoint.getX()) < Math.abs(startPoint.getY() - endPoint.getY()) ){
		target_mc.lineTo(startPoint.getX() + thickness ,startPoint.getY());
		target_mc.lineTo(endPoint.getX() + thickness ,endPoint.getY());
		target_mc.lineTo(endPoint.getX()  ,endPoint.getY());
		target_mc.lineTo(startPoint.getX(),startPoint.getY());
	} else {
		target_mc.lineTo(startPoint.getX() ,startPoint.getY() + thickness);
		target_mc.lineTo(endPoint.getX() ,endPoint.getY() + thickness);
		target_mc.lineTo(endPoint.getX() ,endPoint.getY());
		target_mc.lineTo(startPoint.getX(),startPoint.getY());
	}
	target_mc.endFill();
}

IconDisplay.prototype.minimizeIcon = function (target) {
	drawPixelHLine(target, this.icon_color, 0,0, 8, 100);
	drawPixelHLine(target, this.icon_color,0, 1, 8, 100);
}

IconDisplay.prototype.upArrowheadIcon = function (target) {
	target.clear();
	drawUpArrow(target,this.icon_color, 8, 4 );
}


IconDisplay.prototype.downArrowheadIcon = function (target) {
	target.clear();
	drawDownArrow(target,this.icon_color, 8, 4 );
}

IconDisplay.prototype.stripeIcon = function ( target, stripecount) {
	var ctr = 0;
	target.clear;
	for(ctr = 0; ctr < stripecount; ctr++ ) {
		drawPixelHLine(target, this.icon_color, 0,ctr * 2, 7, 100);
	}
}