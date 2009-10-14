#include "functions/DrawFunction.as"
#include "functions/MppAligner.as"
#include "IconDisplay.as"
#include "MppButtonAsset.as"
function MppScrollBar (container, newdepth){
	AsBroadcaster.initialize(this);
	this.addListener(this);
	
	this.container_mc = container;
	this.depth = newdepth;
	this.icons = new IconDisplay();
	this.aligner = new MppAligner();
	
	this.icons.setColor(this.color_icon);
	
	this.initClips();
	this.paint();
	
}
//----

MppScrollBar.prototype.icons;
MppScrollBar.prototype.aligner;
//----

MppScrollBar.prototype.container_mc;
MppScrollBar.prototype.depth;

MppScrollBar.prototype.scrollbar_mc;
MppScrollBar.prototype.scroller_mc;
MppScrollBar.prototype.path_mc;

MppScrollBar.prototype.canvas_mc = null;
MppScrollBar.prototype.mask_mc;

MppScrollBar.prototype.up_btn = null;
MppScrollBar.prototype.down_btn = null;
MppScrollBar.prototype.dragger_btn = null;

MppScrollBar.prototype.icon_up_mc;
MppScrollBar.prototype.icon_down_mc;
MppScrollBar.prototype.icon_drag_mc;


//Scrollbar Properties---

MppScrollBar.prototype.color_frame_bg = 0xF3F1EC;
MppScrollBar.prototype.color_frame_border = 0xE4E0D6;
MppScrollBar.prototype.color_btn_border = 0xD9D9D9;
MppScrollBar.prototype.color_btn_fore = 0xBBBBBB;
MppScrollBar.prototype.color_btn_bg = 0xEEEEEE;
MppScrollBar.prototype.color_btn_shadow =  0x999999;
MppScrollBar.prototype.color_btn_light = 0xFFFFFF;
MppScrollBar.prototype.color_btn_glow = 0xEEEEEE;
MppScrollBar.prototype.color_btn_disable = 0x6F6F6F;

MppScrollBar.prototype.scrollbar_height = 200;
MppScrollBar.prototype.scrollbar_width = 15;

MppScrollBar.prototype.dragger_height;

MppScrollBar.prototype.color_icon = 0x5E5E5E;

//MoveMent properties--

MppScrollBar.prototype.path_top;
MppScrollBar.prototype.path_bottom;
MppScrollBar.prototype.path_right;
MppScrollBar.prototype.path_left;

MppScrollBar.prototype.canvas_initial_y;
MppScrollBar.prototype.canvas_final_y;
MppScrollBar.prototype.dragger_initial_y;
MppScrollBar.prototype.canvas_movement;

MppScrollBar.prototype.scroll_speed = 10;
//Public Method--


MppScrollBar.prototype.deleteMe = function () {
	this.scrollbar_mc.removeMovieClip();
	delete this.scrollbar_mc;
	delete this;
}

MppScrollBar.prototype.getDisplay = function () {
	return this.scrollbar_mc;
}

MppScrollBar.prototype.setButtonStyle = function (newfore, newbg, newborder, newglow,newshadow, newlight, newbisable){
	this.color_btn_fore = newfore;
	this.color_btn_bg = newbg;
	this.color_btn_shadow = newshadow;
	this.color_btn_light = newlight;
	this.color_btn_glow = newglow;
	this.color_btn_disable = newdisable;
	this.color_btn_border = newborder;
	this.setButtonSkin(this.up_btn);
	this.setButtonSkin(this.down_btn);
	this.setButtonSkin(this.dragger_btn);
}

MppScrollBar.prototype.setFrameColor = function ( newbody, newborder) {
	this.color_frame_bg = newbody;
	this.color_frame_border = newborder;
	this.paint();
	this.redrawScroller();
}

MppScrollBar.prototype.attachCanvas = function ( canvas ) {
	this.canvas_mc = canvas;
	this.canvas_mc._x = this.scrollbar_mc._x;
	this.canvas_mc._y = this.scrollbar_mc._y;
	//trace("Canvas:" +  this.canvas_mc + " Height:" + this.canvas_mc._height + " C Width: " + this.canvas_mc._width);
	this.paintMask();
	this.redrawScroller();
	this.addEvents();
}

MppScrollBar.prototype.setScrollerSpeed = function ( newspeed) {
	this.scroll_speed = newspeed;
}

MppScrollBar.prototype.setHeight = function ( newheight ) {
	
	//trace("Canvas:" + this.canvas_mc +" Height:" + newheight);
	this.scrollbar_height = newheight;
	this.paint();
	this.paintMask();
	if(this.canvas_mc != null) {
		this.redrawScroller();
	}
	
	
}

MppScrollBar.prototype.setDimension = function ( newx, newy) {
	this.scrollbar_mc._x = newx;
	this.scrollbar_mc._y = newy;
	this.canvas_mc._x = this.scrollbar_mc._x;
	this.canvas_mc._y = this.scrollbar_mc._y;
	this.setDraggerPath();
}


//Private Method--

MppScrollBar.prototype.setDraggerPath = function () {
	this.path_top = this.path_mc._y;
	this.path_bottom = this.path_mc._y  + this.path_mc._height  - this.dragger_btn.getDisplay()._height;
	this.path_right = 0;
	this.path_left = 0;
	
	this.dragger_initial_y = this.dragger_btn.getDisplay()._y;
	this.canvas_initial_y = this.canvas_mc._y;// + this.scrollbar_mc._y;
	this.canvas_final_y = (this.scrollbar_height  + this.scrollbar_mc._y) - ( this.canvas_mc._height + this.canvas_mc._y) ;
	this.canvas_movement = ((this.canvas_mc._height + this.canvas_mc._y ) - (this.scrollbar_height +this.scrollbar_mc._y )) / (this.path_mc._height - this.dragger_height);
}

MppScrollBar.prototype.redrawScroller = function () {
	this.dragger_height = this.scrollbar_height / this.canvas_mc._height;
	this.dragger_height = (this.scrollbar_height - (this.scrollbar_width * 2)) * this.dragger_height;
	if(this.dragger_height < 5) {
		this.dragger_height = 5;
	}
	this.dragger_btn.setSize(this.scrollbar_width, this.dragger_height);
	this.paintDragIcon();
	this.setDraggerPath();
	
	if(this.scrollbar_height >= this.canvas_mc._height) {
		this.scroller_mc._visible = false;
	} else {
		this.scroller_mc._visible = true;
	}
}

MppScrollBar.prototype.setIconColor = function ( IconColor ) {
	this.color_icon = IconColor;
	this.icons.setColor(this.color_icon);
	this.paintUpIcon();
	this.paintDownIcon();
	this.paintDragIcon();
}

MppScrollBar.prototype.initClips = function () {
	this.container_mc.createEmptyMovieClip("MppScrollBar" + this.depth + "_mc" , this.depth);
	this.scrollbar_mc = eval(this.container_mc + ".MppScrollBar" + this.depth + "_mc");
	//trace("SB" + this.scrollbar_mc + " depth" + this.depth);
	
	this.scrollbar_mc.createEmptyMovieClip("mask_mc", 1);
	this.mask_mc = this.scrollbar_mc.mask_mc;
	
	this.scrollbar_mc.createEmptyMovieClip("scroller_mc", 2);
	this.scroller_mc = this.scrollbar_mc.scroller_mc;
	this.scroller_mc.createEmptyMovieClip("framework_mc",1);
	this.scroller_mc.createEmptyMovieClip("path_mc",2);
	
	this.path_mc = this.scroller_mc.path_mc;
}

MppScrollBar.prototype.paint = function () {
	if(this.up_btn != null) {
		this.up_btn.deleteMe();
		this.down_btn.deleteMe();
		this.dragger_btn.deleteMe();
	}
	this.paintBody();
	this.paintUp();
	this.paintDown();
	this.paintDragger();
	this.setSkinToAll();
	if( this.canvas_mc !=  null ) {
		this.addEvents();
	}
}


MppScrollBar.prototype.paintMask = function () {
	this.mask_mc.clear();
	drawBoxInPos(this.mask_mc, 0,0, this.canvas_mc._width, this.scrollbar_height, 0x000000, 100);
	this.canvas_mc.setMask(this.mask_mc);
	this.aligner.rightOf(this.canvas_mc, this.scroller_mc);
}

MppScrollBar.prototype.paintBody = function  () {
	this.scroller_mc.framework_mc.clear();
	this.path_mc.clear();
	
	drawBoxInPos(this.scroller_mc.framework_mc, 0,0,this.scrollbar_width, this.scrollbar_height, this.color_frame_bg, 100);
	drawFrameInPos(this.scroller_mc.framework_mc, 0,0,this.scrollbar_width, this.scrollbar_height, this.color_frame_border, 100);
	
	drawBoxInPos(this.path_mc, 0, 0, this.scrollbar_width - 1, this.scrollbar_height - ( 2 * this.scrollbar_width), this.color_frame_bg, 100); //this.color_frame_bg
	this.path_mc._y = this.scrollbar_width;
	this.path_mc._x = 1;
	this.setDraggerPath();
}

MppScrollBar.prototype.paintUp = function() {
	this.up_btn = new MppButtonAsset( this.scroller_mc, 3);
	this.up_btn.setSize(this.scrollbar_width, this.scrollbar_width);
	this.setButtonDefaultProperty(this.up_btn);
	this.paintUpIcon();
}



MppScrollBar.prototype.paintDown = function() {
	this.down_btn = new MppButtonAsset( this.scroller_mc, 4);
	this.down_btn.setSize(this.scrollbar_width, this.scrollbar_width);
	this.setButtonDefaultProperty(this.down_btn);
	this.aligner.alignBottom(this.scroller_mc.framework_mc, this.down_btn.getDisplay());
	this.paintDownIcon();
}
MppScrollBar.prototype.paintDragger = function() {
	this.dragger_btn = new MppButtonAsset(this.scroller_mc, 5);
	this.setButtonDefaultProperty(this.dragger_btn);
	this.dragger_btn.setSize(this.scrollbar_width, this.scrollbar_height - ( 2 * this.scrollbar_width));
	this.aligner.bottomOf(this.up_btn.getDisplay(),this.dragger_btn.getDisplay());
	this.paintDragIcon();
}


MppScrollBar.prototype.paintUpIcon = function () {
	this.icon_up_mc.clear();
	this.up_btn.getDisplay().createEmptyMovieClip("icon_mc", this.up_btn.getIconDepth());
	this.icon_up_mc = this.up_btn.getDisplay().icon_mc;
	this.icons.upArrowheadIcon(this.icon_up_mc);
	this.aligner.center(this.up_btn.getDisplay(), this.icon_up_mc);
}

MppScrollBar.prototype.paintDownIcon = function  () {
	this.icon_down_mc.clear();
	this.down_btn.getDisplay().createEmptyMovieClip("icon_mc", this.down_btn.getIconDepth());
	this.icon_down_mc = this.down_btn.getDisplay().icon_mc;
	this.icons.downArrowheadIcon(this.icon_down_mc);
	this.aligner.center(this.down_btn.getDisplay(), this.icon_down_mc);
}
MppScrollBar.prototype.paintDragIcon = function () {
	this.drag_icon_mc.clear();
	this.dragger_btn.getDisplay().createEmptyMovieClip("icon_mc", this.dragger_btn.getIconDepth());
	this.icon_drag_mc = this.dragger_btn.getDisplay().icon_mc;
	this.icons.stripeIcon(this.icon_drag_mc, 3);
	this.aligner.center(this.dragger_btn.getDisplay(), this.icon_drag_mc);
}

MppScrollBar.prototype.setButtonDefaultProperty = function (mppbtn) {
	mppbtn.buttonShape("rectangle");
	mppbtn.setLabel("");
	mppbtn.setCursor(false);
}

MppScrollBar.prototype.setSkinToAll = function () {
	this.setButtonSkin(this.up_btn);
	this.setButtonSkin(this.down_btn);
	this.setButtonSkin(this.dragger_btn);
}

MppScrollBar.prototype.setButtonSkin = function (mppbtn) {
	
	mppbtn.setForeColor(this.color_btn_fore);
	mppbtn.setBgColor(this.color_btn_bg);
	mppbtn.setShadowColor(this.color_btn_shadow);
	mppbtn.setLightColor(this.color_btn_light);
	mppbtn.setGlowColor(this.color_btn_glow);
	mppbtn.setDisableColor(this.color_btn_disable);
	mppbtn.setBorderColor(this.color_btn_border);
}

MppScrollBar.prototype.addEvents = function () {
	var mppsb = this;
	this.dragger_btn.onPress = function () {
		mppsb.broadcastMessage("onScroll");
		mppsb.dragger_btn.getDisplay().startDrag(false,mppsb.path_left, mppsb.path_top, mppsb.path_right, mppsb.path_bottom);
		
		mppsb.dragger_btn.getDisplay().onMouseMove = function () {
			var directiony = Math.abs(mppsb.dragger_initial_y - mppsb.dragger_btn.getDisplay()._y);
			var newy = Math.round(directiony * -1 * mppsb.canvas_movement + mppsb.canvas_initial_y);
			mppsb.canvas_mc._y = newy;// + mppsb.scrollbar_mc._y;
			//trace("Direy: " + directiony + " DragInitY: " + mppsb.dragger_initial_y + " DragCurrentY:" + mppsb.dragger_btn.getDisplay()._y+ " NewY:" + newy);
		}
	}
	this.dragger_btn.onRelease = function () {
		mppsb.broadcastMessage("onStopScroll");
		mppsb.dragger_btn.getDisplay().stopDrag();
		delete mppsb.dragger_btn.getDisplay().onMouseMove;
	}
	
	this.dragger_btn.onReleaseOutside = function () {
		mppsb.broadcastMessage("onStopScroll");
		mppsb.dragger_btn.getDisplay().stopDrag();
		delete mppsb.dragger_btn.getDisplay().onMouseMove;
	}
	
	this.up_btn.onRelease = function () {
		delete mppsb.up_btn.getDisplay().onEnterFrame;
	}
	
	this.up_btn.onPress = function () {
		mppsb.broadcastMessage("onUpScroll");
		mppsb.up_btn.getDisplay().onEnterFrame = function () {
			if ((mppsb.canvas_mc._y  - mppsb.scrollbar_mc._y) + mppsb.scroll_speed  < mppsb.mask_mc._y) {
				if (mppsb.dragger_btn.getDisplay()._y <= mppsb.path_top) {
					mppsb.dragger_btn.getDisplay()._y = mppsb.path_top;
					mppsb.canvas_mc._y += mppsb.scroll_speed;
				} else {
					mppsb.canvas_mc._y += mppsb.scroll_speed;
					mppsb.dragger_btn.getDisplay()._y -= (mppsb.scroll_speed / mppsb.canvas_movement);
				}
			} else {
				mppsb.dragger_btn.getDisplay()._y = mppsb.path_top;
				mppsb.canvas_mc._y  = mppsb.mask_mc._y + mppsb.scrollbar_mc._y ;
				delete mppsb.up_btn.getDisplay().onEnterFrame;
			}
		}
	}
	
	this.down_btn.onPress = function () {
		mppsb.broadcastMessage("onDownScroll");
		mppsb.down_btn.getDisplay().onEnterFrame = function(){
			if (mppsb.canvas_mc._y - mppsb.scroll_speed - mppsb.scrollbar_mc._y > mppsb.canvas_final_y) {
				
				if (mppsb.dragger_btn.getDisplay()._y >= mppsb.path_bottom) {
					mppsb.canvas_mc._y -= mppsb.scroll_speed;
					mppsb.dragger_btn.getDisplay()._y = mppsb.path_bottom;
				} else {
					mppsb.canvas_mc._y -= mppsb.scroll_speed;
					mppsb.dragger_btn.getDisplay()._y += mppsb.scroll_speed / mppsb.canvas_movement;
				}
			} else {
				mppsb.dragger_btn.getDisplay()._y = mppsb.path_bottom;
				mppsb.canvas_mc._y = mppsb.canvas_final_y +  mppsb.scrollbar_mc._y ;
				delete mppsb.down_btn.getDisplay().onEnterFrame;
			}
		}
	}
	
	this.down_btn.onRelease = function () {
		delete mppsb.down_btn.getDisplay().onEnterFrame;
	}
}

MppScrollBar.prototype.scrollDown = function (){
	this.dragger_btn.getDisplay()._y = this.path_bottom;
	this.canvas_mc._y = this.canvas_final_y + this.scrollbar_mc._y;
}

MppScrollBar.prototype.scrollUp = function () {
	this.dragger_btn.getDisplay()._y = this.path_top;
	this.canvas_mc._y = this.mask_mc._y + this.scrollbar_mc._y;
}