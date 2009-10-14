#include "functions/MppAligner.as"
#include "ShapePainter.as"
#include "functions/DrawFunction.as"
#include "functions/vo/MppMenuItems.as"

function MppRadioButton(container , assignedDepth) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	
	this.depth = assignedDepth;
	this.container_mc = container;
	
	this.painter = new ShapePainter();
	this.aligner = new MppAligner();
	
	this.createClips();
	this.paint();
	this.addEvents();
	this.setEnable(true);
	this.setHover(false);
	this.setSelected(false);
	this.setPress (false);
	
	this.setWidth(this.radio_width);
}

MppRadioButton.prototype.container_mc;
MppRadioButton.prototype.depth;
MppRadioButton.prototype.radio_mc;

MppRadioButton.prototype.frame_mc;
MppRadioButton.prototype.mask_mc;
MppRadioButton.prototype.label_txt;

MppRadioButton.prototype.press_mc;
MppRadioButton.prototype.round_mc;
MppRadioButton.prototype.bullet_mc;
MppRadioButton.prototype.glow_mc;
MppRadioButton.prototype.disable_mc;
MppRadioButton.prototype.bg_mc

MppRadioButton.prototype.color_glow = 0xD9FBEA;
MppRadioButton.prototype.color_bg = 0xF7F7F7;
MppRadioButton.prototype.color_fore = 0x2C0FA4;
MppRadioButton.prototype.color_press = 0xDDDDDD;
MppRadioButton.prototype.color_border = 0x5F5C5C;
MppRadioButton.prototype.color_disable = 0xCCCCCC;
MppRadioButton.prototype.color_bullet = 0x000000;

MppRadioButton.prototype.painter;
MppRadioButton.prototype.aligner;

MppRadioButton.prototype.radio_width = 100;
MppRadioButton.prototype.radio_height = 17;
MppRadioButton.prototype.radio_label = "Radio Button";

MppRadioButton.prototype.round_height;
MppRadioButton.prototype.round_width;
MppRadioButton.prototype.round_insertion_x;
MppRadioButton.prototype.round_insertion_y;

//--- Technical Propeerties

MppRadioButton.prototype.bool_enabled = true;
MppRadioButton.prototype.bool_selected = false;
MppRadioButton.prototype.bool_clicked = false;
MppRadioButton.prototype.bool_member = false;

//--- Text Format Properties
MppRadioButton.prototype.format_size = 12;
MppRadioButton.prototype.format_font = "Arial";
MppRadioButton.prototype.format_color = 0x000000;
MppRadioButton.prototype.format_align = "left";

MppRadioButton.prototype.format_bold =  false;
MppRadioButton.prototype.format_underline = false;
MppRadioButton.prototype.format_italic = false;


//--- Technical Groups--




MppRadioButton.prototype.myGroupCollection;
//PUblic Methods--

MppRadioButton.prototype.setRadioColor = function (ForeColor, BGColor, BorderColor, BulletColor) {
	this.color_fore = ForeColor;
	this.color_bg = BGColor;
	this.color_border = BorderColor;
	this.color_bullet = BulletColor;
	this.paintRound();
}

MppRadioButton.prototype.setInteractiveColor = function  (GlowColor , PressColor, DisableColor ) {
	this.color_glow = GlowColor;
	this.color_press = PressColor;
	this.color_disable = DisableColor;
	this.paintRound();
}

MppRadioButton.prototype.embedFonts = function ( newfont ) {
	this.label_txt.embedFonts = true;
	var tf = new TextFormat();
	tf.font = newfont;
	this.label_txt.setTextFormat( tf );
	this.label_txt.setNewTextFormat( tf) ;
}

MppRadioButton.prototype.setLabelFormat = function (newfont, newsize, newcolor, newalign) {
	if( this.label_txt.embedFonts != true ){
		this.format_font = newfont;
	}
	this.format_size = newsize;
	this.format_color = newcolor;
	this.format_align = newalign;
	this.label_txt.setTextFormat(this.getFormat());
}
MppRadioButton.prototype.setLabelWeight = function (newbold, newitalic, newunderline) {
	this.format_bold = newbold;
	this.format_italic = newitalic;
	this.format_underline = newunderline;
	this.label_txt.setTextFormat(this.getFormat());
}

MppRadioButton.prototype.setEnable = function ( bool ) {
	this.bool_enabled = bool;
	this.disable_mc._visible =  !bool;
}

MppRadioButton.prototype.isSelected = function () {
	return this.bool_selected;
}

MppRadioButton.prototype.setHover = function ( bool ) {
	this.glow_mc._visible = bool;
}

MppRadioButton.prototype.setPress = function ( bool) {
	this.press_mc._visible = bool;
}

MppRadioButton.prototype.setSelected = function ( bool ) {
	this.bool_selected = bool;
	this.bullet_mc._visible = bool;
}

MppRadioButton.prototype.setDimension = function ( newx , newy) {
	this.radio_mc._x = newx;
	this.radio_mc._y = newy
}

MppRadioButton.prototype.setLabel = function ( newlabel) {
	this.radio_label = newlabel;
	this.label_txt.text = this.radio_label;
	this.label_txt.setTextFormat(this.getFormat());
}

MppRadioButton.prototype.setLabelProperties = function () {
	this.label_txt.selectable = false;
	this.label_txt.setTextFormat(this.getFormat());
}

MppRadioButton.prototype.setWidth = function (newwidth) {
	this.radio_width = newwidth;
	this.paintFrame();
	this.arrangeClip();
}


MppRadioButton.prototype.addToGroup = function  ( mygroup) {
	this.bool_member = true;
	this.myGroupCollection = mygroup;
}

MppRadioButton.prototype.removeToGroup = function  () {
	this.bool_member = false;
	this.myGroupCollection = null;
}


//---Private MEthods-
MppRadioButton.prototype.createClips = function () {
	//trace("     MppRadioButton.prototype.createClips()");
	
	this.container_mc.createEmptyMovieClip("MppRadioButton" + this.depth + "_mc" , this.depth);
	this.radio_mc = eval(this.container_mc + ".MppRadioButton" + this.depth + "_mc");
	
	this.radio_mc.createEmptyMovieClip("mask_mc", 0);
	this.mask_mc = this.radio_mc.mask_mc;
	
	this.radio_mc.createEmptyMovieClip("frame_mc", 1);
	this.frame_mc = this.radio_mc.frame_mc;
	
	
	//drawBoxInPos(this.radio_mc.mask_mc, 0,0, this.radio_width, this.radio_height, 0x000000, 10);
	this.radio_mc.createEmptyMovieClip("round_mc", 2);
	this.radio_mc.createEmptyMovieClip("text_mc", 3);
	
	this.round_mc = this.radio_mc.round_mc;
	
	this.round_mc.createEmptyMovieClip("bg_mc", 1);
	this.round_mc.createEmptyMovieClip("press_mc",2);
	this.round_mc.createEmptyMovieClip("bullet_mc", 3);
	this.round_mc.createEmptyMovieClip("glow_mc", 4);
	this.round_mc.createEmptyMovieClip("disable_mc", 5);
	
	this.press_mc = this.round_mc.press_mc;
	this.bg_mc = this.round_mc.bg_mc;
	this.bullet_mc = this.round_mc.bullet_mc;
	this.glow_mc = this.round_mc.glow_mc;
	this.disable_mc = this.round_mc.disable_mc;
}


MppRadioButton.prototype.paint = function () {
	this.paintRound();
	this.paintFrame();
	this.arrangeClip();
	
}

MppRadioButton.prototype.paintRound = function () {
	//trace("MppRadioButton.prototype.paintRound()");
	
	this.bg_mc.clear();
	this.glow_mc.clear();
	this.disable_mc.clear();
	this.bullet_mc.clear();
	this.press_mc.clear();
	
	this.painter.paintRoundBorder(this.bg_mc, 6 , this.color_bg, this.color_border, 100 , 1);
	this.painter.paintCircle(this.glow_mc, 6 , this.color_glow,50);
	this.painter.paintCircle(this.disable_mc, 6 , this.color_disable, 40);
	this.painter.paintCircle(this.press_mc, 5, this.color_press, 100);
	this.painter.paintCircle(this.bullet_mc, 2, this.color_bullet, 100);
	
	this.aligner.center(this.bg_mc, this.bullet_mc);
	this.aligner.center(this.bg_mc, this.glow_mc);
	this.aligner.center(this.bg_mc, this.press_mc);
	this.aligner.center(this.bg_mc, this.disable_mc);
	
	this.bullet_mc._x += 4;
	this.bullet_mc._y += 3;
}

MppRadioButton.prototype.paintFrame = function () {
	//trace("MppRadioButton.prototype.paintFrame()");
	this.frame_mc.clear();
	this.mask_mc.clear();
	drawBoxInPos(this.frame_mc, 0,0, this.radio_width, this.radio_height, 0x000000, 0);
	drawBoxInPos(this.mask_mc, 0,0, this.radio_width, this.radio_height, 0x000000, 100);
	this.radio_mc.text_mc.createTextField("label_txt", 1, 0,0, this.radio_width, this.radio_height);
	this.label_txt = this.radio_mc.text_mc.label_txt;
	this.setLabelProperties();
	this.setLabel(this.radio_label);
}

MppRadioButton.prototype.arrangeClip = function () {
	//trace("MppRadioButton.prototype.arrangeClip()");
	
	this.roundInsertionPoint();
	this.aligner.verticalCenter(this.frame_mc, this.round_mc);
	//this.round_mc._y += this.round_insertion_y;
	//this.round_mc._x += this.round_insertion_x;
	this.aligner.verticalCenter(this.frame_mc, this.radio_mc.text_mc);
	//this.aligner.horizontalCenter(this.frame_mc, this.radio_mc.text_mc);
	this.aligner.rightOf(this.round_mc, this.radio_mc.text_mc);
	this.radio_mc.text_mc.setMask(this.mask_mc);
	
	this.radio_mc.text_mc._x += 6;
	this.radio_mc.text_mc._y += 6;
	
	this.mask_mc._y += 2;
	
}

MppRadioButton.prototype.roundInsertionPoint = function() {
	this.round_height = this.round_mc._height;
	this.round_width = this.round_mc._width;
	this.round_insertion_x =  (this.round_width / 2) ;
	this.round_insertion_y =  (this.round_height / 2);
}

MppRadioButton.prototype.getFormat = function (){
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

MppRadioButton.prototype.addEvents = function () {
	var mppradio = this;
	this.frame_mc.useHandCursor = false;
	this.frame_mc.onRollOver = function () {
		if(  mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onRollOver");
			mppradio.setHover(true);
		}
	}
	this.frame_mc.onDragOut = function () {
		if(mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onDragOut");
			mppradio.setHover(false);
			mppradio.setPress(false);
		}
	}
	this.frame_mc.onRollOut = function () {
		if(mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onRollOut");
			mppradio.setHover(false);
			mppradio.setPress(false);
		}
	}
	this.frame_mc.onReleaseOutSide = function () {
		if(mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onReleaseOutSide");
			mppradio.setHover(false);
			mppradio.setPress(false);
		}
	}
	
	this.frame_mc.onPress = function () {
		if(mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onPress");
			mppradio.setHover(true);
			mppradio.setPress(true);
		}
	}
	
	this.frame_mc.onRelease = function () {
		if(mppradio.bool_enabled == true) {
			mppradio.broadcastMessage("onRelease");
			mppradio.setHover(true);
			mppradio.setPress(false);
			if(mppradio.bool_member == true){
				mppradio.myGroupCollection.onLoop = function (target) {
					target.setSelected(false);
				}	
				mppradio.myGroupCollection.loopIn();
			}
			if(mppradio.bool_selected == true) {
				mppradio.setSelected(false);
			} else {
				mppradio.setSelected(true);
			}
		}
	}
}


