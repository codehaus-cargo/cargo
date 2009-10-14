#include "functions/DrawFunction.as"
#include "functions/MppAligner.as"
#include "functions/vo/MppMenuItems.as"
#include "MppScrollBar.as"
function MppCanvas (container, newdepth) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	children = new ItemCollection();
	
	this.container_mc = container;
	this.depth = newdepth;
	
	this.initClips();
	this.scrollbar = new MppScrollBar(this.canvas_mc, 0);
	this.paint();
	
	this.setBgAlpha (0);
	this.setBorderAlpha(0);
}




MppCanvas.prototype.container_mc;
MppCanvas.prototype.depth;

MppCanvas.prototype.canvas_mc;
MppCanvas.prototype.child_mc;
MppCanvas.prototype.child_width = 300;
MppCanvas.prototype.child_height = 300;
MppCanvas.prototype.frame_mc;
MppCanvas.prototype.bg_mc;
MppCanvas.prototype.border_mc;

MppCanvas.prototype.children = null;
MppCanvas.prototype.scrollbar;

MppCanvas.prototype.color_bg = 0xFFFFFF;
MppCanvas.prototype.color_border = 0x000000;


MppCanvas.prototype.getScrollBar = function () {
	return this.scrollbar;
}

MppCanvas.prototype.deleteMe = function () {
	this.canvas_mc.removeMovieClip();
	delete this;
}

MppCanvas.prototype.setCanvasColor = function ( newBg, newBorder ) {
	this.color_bg = newBg;
	this.color_border = newBorder;
	this.paintBg();
	
}

MppCanvas.prototype.setBgAlpha  = function (newAlpha ) {
	this.bg_mc._alpha = newAlpha;
	//trace("Canvas ALPHA: "  + newAlpha);
}
MppCanvas.prototype.setBorderAlpha  = function (newAlpha ) {
	this.border_mc._alpha = newAlpha;
}


//-------private Method-------\\


MppCanvas.prototype.initClips = function () {
	this.container_mc.createEmptyMovieClip("MppCanvas" + this.depth + "_mc", this.depth);
	this.canvas_mc = eval(this.container_mc + ".MppCanvas" + this.depth + "_mc");
	
	this.frame_mc = this.canvas_mc.createEmptyMovieClip("frame_mc", 1);
	this.child_mc = this.canvas_mc.createEmptyMovieClip("child_mc", 2);
	
	
	this.bg_mc = this.frame_mc.createEmptyMovieClip("bg_mc",1);
	this.border_mc = this.frame_mc.createEmptyMovieClip("border_mc",2);
	//trace("Canvas CHild: " + this.child_mc);
}

MppCanvas.prototype.paint = function () {
	this.child_mc.clear();
	drawBoxInPos(this.child_mc, 0 ,0 , this.child_width, this.child_height, 0x000000, 0);
	this.scrollbar.attachCanvas(this.child_mc);
	//trace("Setting Child Height to " +  this.child_height);
	this.scrollbar.setHeight(this.child_height);
	this.paintBg();
}

MppCanvas.prototype.paintBg = function () {
	this.bg_mc.clear();
	this.border_mc.clear();
	drawBoxInPos(this.bg_mc, 0 ,0 , this.child_width, this.child_height, this.color_bg, 100);
	drawFrameInPos(this.border_mc, 0 ,0 , this.child_width, this.child_height, this.color_border, 100);
}

MppCanvas.prototype.refreshCanvas = function () {
	this.scrollbar.attachCanvas(this.child_mc);
	this.scrollbar.setHeight(this.child_height);
}

MppCanvas.prototype.getContainer = function () {
	return this.child_mc;
}

MppCanvas.prototype.getDisplay = function () {
	return this.canvas_mc;
}


MppCanvas.prototype.setVisibility = function ( bool ) {
	//trace("Canvas: " + this.canvas_mc + " Visible: " + bool);
	this.canvas_mc._visible = bool;
}

MppCanvas.prototype.setDimension  = function ( newx, newy) {
	this.canvas_mc._x = newx;
	this.canvas_mc._y = newy;
}

MppCanvas.prototype.setSize = function ( newwidth, newheight) {
	this.child_width = newwidth;
	this.child_height = newheight;
	this.paint();
}