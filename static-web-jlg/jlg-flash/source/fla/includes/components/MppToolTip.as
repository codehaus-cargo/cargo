#include "functions/DrawFunction.as"


function MppToolTip ( container, newDepth ) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.container_mc = container;
	this.depth = newDepth;
	this.createClips();
	
	this.setDefault();
	this.addEvents();
}

MppToolTip.prototype.container_mc;
MppToolTip.prototype.tooltip_mc;
MppToolTip.prototype.depth;

MppToolTip.prototype.box_mc;

MppToolTip.prototype.mask_mc;

MppToolTip.prototype.tool_txt;

//colors
MppToolTip.prototype.color_border = 0xE0D9C0;
MppToolTip.prototype.color_bg = 0xECE9D8;

MppToolTip.prototype.alpha_bg = 100;
//--Properties
MppToolTip.prototype.text_width = 150;
MppToolTip.prototype.text_height = 20;
MppToolTip.prototype.text_multiline = true;


//--- Text Format Properties
MppToolTip.prototype.format_size = 11;
MppToolTip.prototype.format_font = "Arial";
MppToolTip.prototype.format_color = 0x000000;
MppToolTip.prototype.format_align = "left";

MppToolTip.prototype.format_bold =  false;
MppToolTip.prototype.format_underline = false;
MppToolTip.prototype.format_italic = false;



MppToolTip.prototype.text_lbl = "Tool Tip";

MppToolTip.prototype.timer = 1; // tooltip will appear in 5 seconds
MppToolTip.prototype.current_interval;
//Public methods
MppToolTip.prototype.setTipTimer  = function ( seconds ) {
	if( seconds < 1) {
		seconds = 1;
	}
	this.timer = seconds;
}

MppToolTip.prototype.setSelectable = function ( bool ) {
	this.tool_txt.selectable = bool;
}
MppToolTip.prototype.setBackgroundAlpha = function ( alpha ) {
	this.alpha_bg = alpha;
	this.paint();
}

MppToolTip.prototype.setColor = function (TextColor , BGColor , BorderColor ) {
	this.format_color = TextColor;
	this.color_bg = BGColor;
	this.color_border = BorderColor;
	this.tool_txt.setTextFormat(this.getFormat());
	this.paint();
}

MppToolTip.prototype.setTextWeight = function ( Bold , Italic, Underline ) {
	this.format_bold = Bold;
	this.format_italic = Italic;
	this.format_underline = Underline;
	this.tool_txt.setTextFormat(this.getFormat());
	this.paint();
}
MppToolTip.prototype.setTextSize = function ( newSize ) {
	this.format_size = newSize;
	this.text_height = newSize + 5;
	this.tool_txt._height = this.text_height;
	this.tool_txt.setTextFormat(this.getFormat());
	this.paint();
}
MppToolTip.prototype.setText = function (newText) {
	this.text_lbl = newText;
	this.tool_txt.text = this.text_lbl;
	this.tool_txt.setTextFormat(this.getFormat());
	this.tool_txt._width = this.getTextWidth() + 5;
	this.paint();
}


MppToolTip.prototype.setDefault = function () {
	this.setSelectable(false);
}

MppToolTip.prototype.getTextValue = function () {
	return this.tool_txt.text;
}

MppToolTip.prototype.getTextLength = function () {
	return this.tool_txt.length;
}

MppToolTip.prototype.getTextWidth = function () {
	return this.tool_txt.textWidth;
}

MppToolTip.prototype.getTextHeight = function () {
	return this.tool_txt.textHeight;
}

MppToolTip.prototype.setTextVisibility = function ( bool ) {
	this.tool_txt._visible = bool;
}
MppToolTip.prototype.showTip = function () {
	//trace("MppToolTip.prototype.showTip()")
	var mtip = this;
	var ctr = 0;
	function trigTip() {
		ctr++;
		if(ctr == mtip.timer) {
			clearInterval(mtip.current_interval);
			mtip.showToolTip();
		}
		
	}
	clearInterval(this.current_interval);
	//this.current_interval = setInterval(trigTip, 1000)
}



MppToolTip.prototype.hideTip = function () {
	var mtip = this;
	var curAlpha = 100;
	var maskMove =  this.mask_mc._width / 2;
	clearInterval(this.current_interval);
	this.current_interval = setInterval(trigTip, 1000)
	this.box_mc.onEnterFrame = function () {
		if(curAlpha > 0) {
			curAlpha-=10;
			mtip.mask_mc._x -= maskMove;
			mtip.tool_txt._alpha = curAlpha;
			mtip.tooltip_mc._alpha = curAlpha;
		}  else if( curAlpha <= 0){
			delete mtip.box_mc.onEnterFrame;
		}
	}
	
}

//Private Methods

MppToolTip.prototype.manageXY = function () {
	var local = new Object();
	local.x = _xmouse;
	local.y = _ymouse;
	this.container_mc.globalToLocal(local);
	this.tooltip_mc._visible = true;
	this.tooltip_mc._x = local.x + 10;
	this.tooltip_mc._y = local.y  + 5;
}

MppToolTip.prototype.showToolTip = function () {
	this.manageXY();
	var mtip = this;
	var curAlpha = 0;
	var maskMove =  this.mask_mc._width / 5;
	this.mask_mc._x = -this.mask_mc._width;
	this.box_mc.onEnterFrame = function () {
		if(curAlpha < 100) {
			curAlpha += 10;
			if( mtip.mask_mc._x < 0 ) {
				mtip.mask_mc._x += maskMove;
			}
			mtip.tooltip_mc._alpha = curAlpha;
		}  else if( curAlpha >= 100){
			mtip.mask_mc._x = -1;
			delete mtip.box_mc.onEnterFrame;
		}
	}
}

MppToolTip.prototype.createClips = function () {
	this.tooltip_mc = this.container_mc.createEmptyMovieClip("tooltip"+ this.depth + "_mc", this.depth);
	//trace("TOOL TIP: " + this.tooltip_mc);
	this.box_mc = this.tooltip_mc.createEmptyMovieClip("box_mc", 1);
	this.box_mc.createTextField("tool_txt", 3, 0,0, this.text_width, this.text_height);
	this.tool_txt = this.box_mc.tool_txt;
	
	this.box_mc.createEmptyMovieClip("bg_mc", 1);
	this.box_mc.createEmptyMovieClip("border_mc", 2);
	this.mask_mc = this.box_mc.createEmptyMovieClip("mask_mc", 4);
	
	this.box_mc.setMask(this.mask_mc);
	
	this.setText(this.text_lbl);
	
}

MppToolTip.prototype.paint = function () {
	
	this.box_mc.bg_mc.clear();
	this.mask_mc.clear();
	this.box_mc.border_mc.clear();
		drawBoxInPos(this.box_mc.bg_mc, 0, 0, this.getTextWidth() + 2, this.getTextHeight()+ 2, this.color_bg,this.alpha_bg);
		drawFrameInPos(this.box_mc.border_mc, 0, 0, this.getTextWidth() + 2, this.getTextHeight() + 2, this.color_border, this.alpha_bg);
		drawBoxInPos(this.mask_mc, 0, 0, this.getTextWidth() + 3, this.getTextHeight()+ 3, 0x000000, 100);
		drawFrameInPos(this.mask_mc, 0, 0, this.getTextWidth() + 3, this.getTextHeight() + 3, 0x000000, 100);
	this.mask_mc._x = -this.mask_mc._width;
}


MppToolTip.prototype.getFormat = function (){
	var format = new TextFormat();
	format.size = this.format_size;
	format.align = this.format_align;
	format.font = this.format_font;
	format.color = this.format_color;
	format.bold =  this.format_bold;
	format.underline = this.format_underline;
	format.italic = this.format_italic;
	return format;
}

MppToolTip.prototype.addEvents = function () {
	var ttip = this;
}