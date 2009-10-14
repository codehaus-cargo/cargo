#include "MppCanvas.as"


function MppTextArea( container, newDepth ){
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.aligner = new MppAligner();
	this.container_mc = container;
	this.depth = newDepth;
	this.initClip();
	this.addEventHandler();
}

MppTextArea.prototype.aligner;
MppTextArea.prototype.container_mc;
MppTextArea.prototype.textarea_mc;
MppTextArea.prototype.canvas;
MppTextArea.prototype.scrollpane_mc;

MppTextArea.prototype.depth;

MppTextArea.prototype.color_bg = 0xFFFFFF;
MppTextArea.prototype.color_border = 0xDED8CB;
MppTextArea.prototype.color_scroll_bg = 0xF3F1EC;

MppTextArea.prototype.height = 100;
MppTextArea.prototype.width = 300;
MppTextArea.prototype.actualWidth;
MppTextArea.prototype.scrollbarWidth = 15;


MppTextArea.prototype.textarea_txt;


//--------FORMAT

MppTextArea.prototype.format_size = 12;
MppTextArea.prototype.format_font = "Arial";
MppTextArea.prototype.format_color = 0x000000;
MppTextArea.prototype.format_align = "left";

MppTextArea.prototype.format_bold =  false;
MppTextArea.prototype.format_underline = false;
MppTextArea.prototype.format_italic = false;



MppTextArea.prototype.prop_type = "input";

//-----------PUBLIC METHOD
MppTextArea.prototype.scrollUp = function () {
	this.canvas.getScrollBar().scrollUp();
	this.broadcastMessage("onScrollUp");
}

MppTextArea.prototype.scrollDown = function () {
	this.canvas.getScrollBar().scrollDown();
	this.broadcastMessage("onScrollDown");
}


MppTextArea.prototype.getScrollBar = function () {
	return this.canvas.getScrollBar();
}

MppTextArea.prototype.setVisibility = function ( bool ) {
	this.textarea_mc._visible = bool;
}

MppTextArea.prototype.setOpacity = function ( value ) {
	this.textarea_mc._alpha = value;
}

MppTextArea.prototype.getDisplay = function () {
	return this.textarea_mc;
}

MppTextArea.prototype.setStyleColor = function ( bgColor , borderColor , scrollColor ) {
	this.color_bg = bgColor;
	this.color_border = borderColor;
	this.color_scroll_bg = scrollColor;
	this.paintCanvas();
	this.paintScrollpane();
}

MppTextArea.prototype.setTextStyle = function ( font , size ,  color ) {
	this.format_font = font;
	this.format_size = size;
	this.format_color = color;
	this.textarea_txt.setTextFormat( this.getFormat());
	this.textarea_txt.setNewTextFormat( this.getFormat());
}



MppTextArea.prototype.setAlignment =  function ( alignment ) {
	this.format_align = alignment;
	this.textarea_txt.setTextFormat( this.getFormat());
	this.textarea_txt.setNewTextFormat( this.getFormat());
}

MppTextArea.prototype.setWeight = function ( bold , italic, underline ) {
	this.format_bold = bold;
	this.format_italic = italic;
	this.format_underline = underline;
	this.textarea_txt.setTextFormat( this.getFormat());
	this.textarea_txt.setNewTextFormat( this.getFormat());
}

MppTextArea.prototype.setSize = function( newWidth , newHeight){
	this.width = newWidth;
	this.height = newHeight;
	this.paint();
}

MppTextArea.prototype.setDimension = function ( newX , newY ) {
	this.textarea_mc._x = newX;
	this.textarea_mc._y = newY;
}

MppTextArea.prototype.setText = function ( strText ) {
	this.textarea_txt.text = strText;
	this.checkTextHeight();
}

MppTextArea.prototype.getText = function () {
	return this.textarea_txt.text;
}
MppTextArea.prototype.setAsHtml = function ( bool ) {
	this.textarea_txt.html = bool;
}
MppTextArea.prototype.setEditable = function ( bool ){
	if( bool == true){
		this.prop_type = "input";
		this.textarea_txt.type = "input";
	}else {
		this.prop_type = "dynamic";
		this.textarea_txt.type = "dynamic";
	}
}
//-----------PRIVATE METHOD--------\\

MppTextArea.prototype.initClip = function(){
	this.textarea_mc = this.container_mc.createEmptyMovieClip("MppTextArea" + this.depth ,this.depth);
	this.canvas = new MppCanvas(this.textarea_mc, 2);
	this.scrollpane_mc = this.textarea_mc.createEmptyMovieClip("scrollpane", 1);
	this.actualWidth = this.width - this.scrollbarWidth;
	this.canvas.getContainer().createTextField("textarea", 1 , 0, 0, this.actualWidth, this.height);
	this.textarea_txt = this.canvas.getContainer().textarea;
	this.paint();
}


MppTextArea.prototype.paint = function (){
	this.paintCanvas();
	this.paintText();
	this.paintScrollpane();
}

MppTextArea.prototype.paintCanvas = function () {
	this.actualWidth = this.width - this.scrollbarWidth;
	this.canvas.setSize(this.actualWidth, this.height);
	this.canvas.setCanvasColor( this.color_bg, this.color_border);
	this.canvas.setBgAlpha(100);
	this.canvas.setBorderAlpha(100);
}

MppTextArea.prototype.paintText = function () {
	this.textarea_txt._width = this.actualWidth;
	this.textarea_txt._height = this.height;
	this.setInitProperties();
}

MppTextArea.prototype.paintScrollpane = function (){
	this.scrollpane_mc.clear();
	drawBoxInPos(this.scrollpane_mc, 0,0 , this.scrollbarWidth, this.height, this.color_scroll_bg, 100);
	drawFrameInPos(this.scrollpane_mc, 0,0 , this.scrollbarWidth, this.height, this.color_border, 100);
	this.aligner.alignRight(this.canvas.getDisplay(), this.scrollpane_mc);
	
}

MppTextArea.prototype.setInitProperties = function (){
	this.textarea_txt.multiline = true;
	this.textarea_txt.wordWrap = true;
	this.textarea_txt.autoSize = true;
	this.textarea_txt.type = this.prop_type;
	this.textarea_txt.setTextFormat( this.getFormat() );
	this.textarea_txt.setNewTextFormat(this.getFormat());
}


MppTextArea.prototype.getFormat = function (){
	
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


MppTextArea.prototype.checkTextHeight = function () {
		this.canvas.refreshCanvas();
		this.canvas.getScrollBar().scrollDown();
}

MppTextArea.prototype.addEventHandler = function () {
	var mppta = this;
	this.textarea_txt.onChanged = function (){
		mppta.checkTextHeight();
		mppta.broadcastMessage("onChanged");
	}
	
	this.textarea_txt.onKillFocus = function () {
		mppta.broadcastMessage("onLostFocus");
	}
}

