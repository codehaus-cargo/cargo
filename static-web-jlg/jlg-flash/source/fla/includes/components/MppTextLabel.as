#include "functions/DrawFunction.as"
#include "functions/MppAligner.as"

function MppTextLabel(container, assignedDepth) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.container_mc = container;
	this.depth = assignedDepth;
	this.aligner = MppAligner();
	this.createClips();
	this.paint();
	this.addEvents();
	this.setBgAlpha(0);
	this.setBorderAlpha(0);
}
MppTextLabel.prototype.aligner;


MppTextLabel.prototype.container_mc;
MppTextLabel.prototype.depth
MppTextLabel.prototype.label_txt;
MppTextLabel.prototype.label_mc;
MppTextLabel.prototype.mask_mc;
MppTextLabel.prototype.box_mc;


MppTextLabel.prototype.label_width = 75;
MppTextLabel.prototype.label_height = 20;


MppTextLabel.prototype.bool_selectable = false;


//--- Text Format Properties
MppTextLabel.prototype.format_size = 12;
MppTextLabel.prototype.format_font = "Arial";
MppTextLabel.prototype.format_color = 0x000000;
MppTextLabel.prototype.format_align = "left";

MppTextLabel.prototype.format_bold =  false;
MppTextLabel.prototype.format_underline = false;
MppTextLabel.prototype.format_italic = false;

MppTextLabel.prototype.color_box_bg = 0xFFFFFF;
MppTextLabel.prototype.color_box_border = 0xB7B7B7;



MppTextLabel.prototype.label_text = "Label";


MppTextLabel.prototype.pressCtr = 0;
MppTextLabel.prototype.releaseCtr = 0;
//--- Technical Groups--

MppTextLabel.prototype.deleteMe = function () {
	//trace("DELETING");
	label_mc.removeMovieClip();
	delete this;
}

MppTextLabel.prototype.setBorderColor = function ( borderColor) {
	this.color_box_border = borderColor
	this.paint();
}
MppTextLabel.prototype.setBgColor = function ( bgColor) {
	this.color_box_bg = bgColor
	this.paint();
}


MppTextLabel.prototype.getDisplay = function () {
	return this.label_mc;
}

MppTextLabel.prototype.setDimension = function (newx , newy) {
	this.label_mc._x = newx;
	this.label_mc._y = newy;
}
MppTextLabel.prototype.setSize = function ( newWidth , newHeight ) {
	this.label_width = newWidth;
	this.label_height = newHeight;
	this.paint();
}

MppTextLabel.prototype.setWeight = function ( newBold , newItalic, newUnderline ) {
	this.format_bold = newBold;
	this.format_italic = newItalic;
	this.format_underline = newUnderline;
	this.label_txt.setTextFormat(this.getFormat());
}

MppTextLabel.prototype.setTextSize = function ( newSize ) {
	this.format_size = newSize;
	this.label_txt.setTextFormat(this.getFormat());
}

MppTextLabel.prototype.setTextAlignment = function ( newAlign ){
	this.format_align = newAlign;
	this.label_txt.setTextFormat(this.getFormat());
}

MppTextLabel.prototype.setFormat = function ( newFont, newSize, newColor, newAlign) {
	if( this.label_txt.embedFonts != true ) {
		this.format_font = newFont;
	}
	this.format_size = newSize;
	this.format_color = newColor;
	this.format_align = newAlign;
	this.label_txt.setTextFormat(this.getFormat());
}


MppTextLabel.prototype.embedFonts = function (newFont ) {
	this.label_txt.embedFonts = true;
	var tf = new TextFormat();
	tf.font = newFont ;
	this.label_txt.setTextFormat( tf );
	this.label_txt.setNewTextFormat( tf );
}

MppTextLabel.prototype.setEditable = function ( bool ) {
	if( bool  == true ) {
		this.label_txt.type = "input";
	} else {
		this.label_txt.type = "dynamic";
	}
}

MppTextLabel.prototype.setSelectable = function ( bool ) {
	this.bool_selectable = bool;
	this.setProperties()
}

MppTextLabel.prototype.setVisibility = function ( bool ) {
	this.label_mc._visible = bool;
}


MppTextLabel.prototype.setBgAlpha = function ( newAlpha){
	this.box_mc.bg_mc._alpha = newAlpha;
}

MppTextLabel.prototype.setBorderAlpha = function ( newAlpha){
	this.box_mc.border_mc._alpha = newAlpha;
}

//-----PRivate Functions-----\\



MppTextLabel.prototype.createClips = function () {
	this.label_mc =	this.container_mc.createEmptyMovieClip("label" + this.depth + "_mc", this.depth);
	this.box_mc = this.label_mc.createEmptyMovieClip("box_mc", 1);
	this.mask_mc = this.label_mc.createEmptyMovieClip("mask_mc", 3);
	
	this.box_mc.createEmptyMovieClip("bg_mc",1);
	this.box_mc.createEmptyMovieClip("border_mc",2);
}

MppTextLabel.prototype.paint  = function () {
	this.box_mc.bg_mc.clear();
	this.box_mc.border_mc.clear();
	this.mask_mc.clear();
	
	drawBoxInPos(this.box_mc.bg_mc, 0,0, this.label_width, this.label_height, this.color_box_bg, 100);
	drawFrameInPos(this.box_mc.border_mc, 0,0, this.label_width, this.label_height , this.color_box_border, 100);
	drawBoxInPos(this.mask_mc, 0,0, this.label_width , this.label_height , this.color_box_bg, 100);
	drawFrameInPos(this.mask_mc, 0,0, this.label_width, this.label_height , this.color_box_border, 100);
	
	this.label_mc.createTextField("label_txt",2,0,0,this.label_width, this.label_height);
	this.label_txt = this.label_mc.label_txt;
	
	this.label_mc.setMask(this.mask_mc);
	
	this.setProperties();
	this.setLabel(this.label_text);

	
}

MppTextLabel.prototype.setProperties = function () {
	this.label_txt.selectable = this.bool_selectable;
	this.label_txt.setTextFormat(this.getFormat());
}


MppTextLabel.prototype.setLabel = function (newlabel) {
	this.label_text = newlabel;
	this.label_txt.text = this.label_text;
	this.label_txt.setTextFormat(this.getFormat());
}

MppTextLabel.prototype.getLabel = function () {
	return  this.label_text;
}

MppTextLabel.prototype.getFormat = function (){
	var form:TextFormat = new TextFormat();
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

MppTextLabel.prototype.setHandCursor  = function ( value ) {
	this.label_mc.useHandCursor = value;
}


MppTextLabel.prototype.addEvents = function () {
	var mlbl = this;
	this.label_mc._focusrect = false;
	this.label_mc.useHandCursor = false;
	this.label_mc.onRollOut = function () {
		mlbl.broadcastMessage("onRollOut");
	}
	this.label_mc.onPress = function () {
		mlbl.pressCtr++;
		//if(mlbl.pressCtr >= 2) {
			//mlbl.deleteButtonEvents();
		//}
		mlbl.broadcastMessage("onPress");
	}
	this.label_mc.onRelease = function () {
		mlbl.releaseCtr++;
		//if(mlbl.releaseCtr >= 2) {
			//mlbl.deleteButtonEvents();
		//}
		mlbl.broadcastMessage("onRelease");
	}
	this.label_mc.onReleaseOutside = function () {
		mlbl.broadcastMessage("onReleaseOutside");
	}
	this.label_mc.onDragOut = function () {
		mlbl.broadcastMessage("onDragOut");
	}
	this.label_mc.onRollOver = function () {
		mlbl.broadcastMessage("onRollOver");
	}
	
	this.label_txt.onSetFocus = function ( oldFocus ){
		trace("GOT FOCUS");
	}
	this.label_txt.onKillFocus = function ( newFocus) {
		trace("FOCUS");
		mlbl.addEvents();
		mlbl.pressCtr = 0;
		mlbl.releaseCtr = 0;
	}
}

MppTextLabel.prototype.deleteButtonEvents = function () {
	delete this.label_mc.onPress;
	delete this.label_mc.onRollOver;
	delete this.label_mc.onRelease;
	delete this.label_mc.onDragOut;
	delete this.label_mc.onReleaseOutside;
	delete this.label_mc.onRollOut;
}