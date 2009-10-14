#include "functions/DrawFunction.as"

function MppVScrollBar (refcon, depth){
	this.sdepth = depth;
	this.content_mc = refcon;
	trace(this.content_mc._parent);
	this.container_mc  = this.content_mc._parent;

	this.bodywidth = this.content_mc._width;
	
	AsBroadcaster.initialize(this);
	this.addListener(this);

	var mppscroller = this;
	this.onDraggerPressed = function(){
		//trace("LEFT: " +  mppscroller.Left + " RIGHT: " +  mppscroller.Right );
		startDrag(mppscroller.dragger_mc, false, mppscroller.Left,mppscroller.Top,mppscroller.Right , mppscroller.Bottom);
		mppscroller.dragger_mc.onMouseMove = function() {
			var sDirY = Math.abs(mppscroller.draggerInitPosition - mppscroller.dragger_mc._y);
			mppscroller.content_mc._y = Math.round(sDirY * -1 * mppscroller.movement + mppscroller.contentInitPosition);
		}
	}
	this.onDraggerReleased = function() {
		stopDrag();
		delete mppscroller.dragger_mc.onMouseMove;
		
	}
	this.onUp = function(){
		
		mppscroller.content_mc.onEnterFrame = function(){
			if (mppscroller.content_mc._y + mppscroller.speed  < mppscroller.mask_mc._y) {
				if (mppscroller.dragger_mc._y <= mppscroller.Top) {
					mppscroller.dragger_mc._y = mppscroller.Top;
					mppscroller.content_mc._y += mppscroller.speed;
				} else {
					mppscroller.content_mc._y += mppscroller.speed;
					mppscroller.dragger_mc._y -= mppscroller.speed / mppscroller.movement;
				}
			} else {
				mppscroller.dragger_mc._y = mppscroller.Top;
				mppscroller.content_mc._y  = mppscroller.mask_mc._y;
				delete mppscroller.content_mc.onEnterFrame;
			}
		}
	}
	this.onReleased = function () {
		delete mppscroller.content_mc.onEnterFrame;
	}
	this.onDown = function() {
		mppscroller.content_mc.onEnterFrame = function(){
			if (mppscroller.content_mc._y - mppscroller.speed > mppscroller.contentFinalPos) {
				if (mppscroller.dragger_mc._y >= mppscroller.Bottom) {
					mppscroller.content_mc._y -= mppscroller.speed;
					mppscroller.dragger_mc._y = mppscroller.Bottom;
				} else {
					mppscroller.content_mc._y -= mppscroller.speed;
					mppscroller.dragger_mc._y += mppscroller.speed / mppscroller.movement;
				}
			} else {
				mppscroller.dragger_mc._y = mppscroller.Bottom;
				mppscroller.content_mc._y = mppscroller.contentFinalPos;
				delete mppscroller.content_mc.onEnterFrame;
			}
		}
	}
}
//---------------------------COLORS AND SKIN---------------------------\\

//--------BG

MppVScrollBar.prototype.sdepth;
MppVScrollBar.prototype.bgcolor = 0xF6F5F0;
MppVScrollBar.prototype.trackcolor = 0xF3F1EC;

MppVScrollBar.prototype.overcolor =  0xD6DBC6;
MppVScrollBar.prototype.presscolor = 0x798E5C;
MppVScrollBar.prototype.upcolor = 0xA4B48B;

MppVScrollBar.prototype.linecolor = 0xCBCAC5;
MppVScrollBar.prototype.lineshadowcolor = 0x859966;
MppVScrollBar.prototype.linelightcolor = 0xFFFFFF;

MppVScrollBar.prototype.arrowcolor = 0xFFFFFF;

//------------------------------PROPERTIES----------------\\
MppVScrollBar.prototype.sheight = 200;
MppVScrollBar.prototype.swidth = 15;

MppVScrollBar.prototype.scrollerHeight;

MppVScrollBar.prototype.bodywidth = 0;

MppVScrollBar.prototype.bg_mc;
MppVScrollBar.prototype.up_mc;
MppVScrollBar.prototype.down_mc;

MppVScrollBar.prototype.dragger_mc;
MppVScrollBar.prototype.track_mc;

MppVScrollBar.prototype.icon_mc;

MppVScrollBar.prototype.scrollbar_mc;

MppVScrollBar.prototype.uparrow_mc;
MppVScrollBar.prototype.downarrow_mc;

MppVScrollBar.prototype.container_mc;
MppVScrollBar.prototype.content_mc;
MppVScrollBar.prototype.mask_mc;


//--------------Important Variables

MppVScrollBar.prototype.Top;
MppVScrollBar.prototype.Bottom;
MppVScrollBar.prototype.Left;
MppVScrollBar.prototype.Right;


MppVScrollBar.prototype.speed;
MppVScrollBar.prototype.contentInitPosition;
MppVScrollBar.prototype.draggerInitPosition;
MppVScrollBar.prototype.contentFinalPos;

MppVScrollBar.prototype.movement;
//-------------------------------------METHODS------------------------------\\

//--------------------PRIVATE METHOD METHODS---------------------------\\

MppVScrollBar.prototype.setScrollerHeight = function(){
	this.scrollerHeight = (this.sheight / this.content_mc._height);
	this.scrollerHeight = (this.sheight - (this.swidth * 2) )  * this.scrollerHeight;
	
	trace(this.content_mc + " " + this.scrollerHeight);
}

MppVScrollBar.prototype.setTrack = function(){
	this.Top = this.track_mc._y;
	this.Left = 0; //this.track_mc._x - 1;
	this.Bottom = this.track_mc._y - this.scrollerHeight + this.track_mc._height;
	this.Right = 0;//this.track_mc._x + this.track_mc._width  - this.swidth + 1;
	
}


MppVScrollBar.prototype.initVars = function(){
	this.speed  = 10;
	this.contentInitPosition = this.content_mc._y;
	this.draggerInitPosition = this.dragger_mc._y;
	this.contentFinalPos = this.sheight - this.content_mc._height + this.contentInitPosition;
	this.movement = ((this.container_mc._height - this.sheight)/( this.track_mc._height - this.scrollerHeight)) 
}


MppVScrollBar.prototype.paint = function (){
	trace("DePTH " + this.sdepth);
	this.container_mc.createEmptyMovieClip("scrollbar" + this.sdepth +  "_mc", this.sdepth);
	this.scrollbar_mc = eval(this.container_mc + ".scrollbar"+ this.sdepth +"_mc");
	
	this.container_mc.createEmptyMovieClip("mask_mc",1);
	
	this.mask_mc = this.container_mc.mask_mc;
	drawBox(this.mask_mc, 0x000000, this.bodywidth + this.swidth, this.sheight,100);
	this.container_mc.setMask(this.mask_mc);
	
	
	if(this.container_mc != _root){
		this.content_mc._x = localToGlobal(this.container_mc._x);
		this.content_mc._y =  localToGlobal(this.container_mc._y);
	}else {
		this.content_mc._x = this.container_mc._x;
		this.content_mc._y =  this.container_mc._y;
	}
	//trace("Container" + this.container_mc + " darrow" + this.downarrow_mc);
	/*
	this.scrollbar_mc.createEmptyMovieClip("downarrow_mc",10);
	this.downarrow_mc = this.scrollbar_mc.downarrow_mc;
	drawUpArrow(this.downarrow_mc, 0x000000, 10, 5);
	*/
	this.scrollbar_mc.createEmptyMovieClip("bg_mc",1);
	this.bg_mc = this.scrollbar_mc.bg_mc;
	drawBox(this.bg_mc, this.bgcolor, this.swidth, this.sheight, 100);
	
	this.scrollbar_mc.createEmptyMovieClip("up_mc",2);
	this.up_mc = this.scrollbar_mc.up_mc;
	this.paintUpComponent();
	this.scrollbar_mc.createEmptyMovieClip("down_mc",3);
	this.down_mc = this.scrollbar_mc.down_mc;
	this.paintDownComponent();
	
	this.scrollbar_mc.createEmptyMovieClip("track_mc",4);
	this.track_mc = this.scrollbar_mc.track_mc;
	drawBox(this.track_mc, this.trackcolor , this.swidth - 2, (this.sheight - (this.swidth * 2)) - 2, 100);
	
	this.track_mc._y = this.swidth + 1;
	this.track_mc._x += 1;
	this.setTrack();
	this.scrollbar_mc.createEmptyMovieClip("dragger_mc",5);
	this.dragger_mc = this.scrollbar_mc.dragger_mc;
	this.paintDraggerComponent();
}


MppVScrollBar.prototype.construct = function(){
	this.scrollbar_mc._x = this.bodywidth;
	trace(this.content_mc._height);
	if(this.sheight >= this.content_mc._height) {
		this.scrollbar_mc._visible = false;
	} else {
		this.scrollbar_mc._visible = true;
	}
}



MppVScrollBar.prototype.paintUpComponent = function(){
	this.up_mc.createEmptyMovieClip("boxdown_mc",1);
	this.up_mc.createEmptyMovieClip("boxover_mc",2);
	this.up_mc.createEmptyMovieClip("boxup_mc",3);
	this.up_mc.createEmptyMovieClip("frame_mc",4);
	this.up_mc.createEmptyMovieClip("shadow_mc",5);
	this.up_mc.createEmptyMovieClip("light_mc",6);
	this.up_mc.createEmptyMovieClip("icon_mc",7);
	
	drawPixelFrame(this.up_mc.frame_mc, this.linecolor,this.swidth, this.swidth, 100);
	drawPixelFrame(this.up_mc.shadow_mc, this.lineshadowcolor,this.swidth - 1, this.swidth - 1, 100);
	drawPixelFrame(this.up_mc.shadow_mc, this.linelightcolor,this.swidth - 2, this.swidth - 2, 100);
	this.up_mc.shadow_mc._x = 1;
	this.up_mc.shadow_mc._y = 1;
	drawBox(this.up_mc.boxup_mc,this.upcolor,this.swidth, this.swidth, 100);
	drawBox(this.up_mc.boxdown_mc,this.presscolor,this.swidth, this.swidth, 100);
	drawBox(this.up_mc.boxover_mc,this.overcolor,this.swidth, this.swidth, 100);
	drawUpArrow(this.up_mc.icon_mc,this.arrowcolor, 8, 4 );
	this.centerThis(this.up_mc, this.up_mc.icon_mc);
	this.addPhysicalEvent(this.up_mc);
}

MppVScrollBar.prototype.paintDownComponent = function(){
	this.down_mc.createEmptyMovieClip("boxdown_mc",1);
	this.down_mc.createEmptyMovieClip("boxover_mc",2);
	this.down_mc.createEmptyMovieClip("boxup_mc",3);
	this.down_mc.createEmptyMovieClip("frame_mc",4);
	this.down_mc.createEmptyMovieClip("shadow_mc",5);
	this.down_mc.createEmptyMovieClip("light_mc",6);
	this.down_mc.createEmptyMovieClip("icon_mc",7);
	
	drawPixelFrame(this.down_mc.frame_mc, this.linecolor,this.swidth, this.swidth, 100);
	drawPixelFrame(this.down_mc.shadow_mc, this.lineshadowcolor,this.swidth - 1, this.swidth - 1, 100);
	drawPixelFrame(this.down_mc.shadow_mc, this.linelightcolor,this.swidth - 2, this.swidth - 2, 100);
	this.down_mc.shadow_mc._x = 1;
	this.down_mc.shadow_mc._y = 1;
	drawBox(this.down_mc.boxup_mc,this.upcolor,this.swidth, this.swidth, 100);
	drawBox(this.down_mc.boxdown_mc,this.presscolor,this.swidth, this.swidth, 100);
	drawBox(this.down_mc.boxover_mc,this.overcolor,this.swidth, this.swidth, 100);
	drawDownArrow(this.down_mc.icon_mc,this.arrowcolor, 8, 4 );
	this.centerThis(this.down_mc, this.down_mc.icon_mc);
	this.addPhysicalEvent(this.down_mc);
	this.down_mc._y = this.sheight - this.swidth;
}

MppVScrollBar.prototype.paintDraggerComponent = function() {
	this.dragger_mc.createEmptyMovieClip("boxdown_mc",1);
	this.dragger_mc.createEmptyMovieClip("boxover_mc",2);
	this.dragger_mc.createEmptyMovieClip("boxup_mc",3);
	this.dragger_mc.createEmptyMovieClip("frame_mc",4);
	this.dragger_mc.createEmptyMovieClip("shadow_mc",5);
	this.dragger_mc.createEmptyMovieClip("light_mc",6);
	this.dragger_mc.createEmptyMovieClip("icon_mc",7);
	
	drawPixelFrame(this.dragger_mc.frame_mc, this.linecolor,this.swidth, this.scrollerHeight, 100);
	drawPixelFrame(this.dragger_mc.shadow_mc, this.lineshadowcolor,this.swidth - 1, this.scrollerHeight - 1, 100);
	drawPixelFrame(this.dragger_mc.shadow_mc, this.linelightcolor,this.swidth - 2, this.scrollerHeight - 2, 100);
	this.dragger_mc.shadow_mc._x = 1;
	this.dragger_mc.shadow_mc._y = 1;
	drawBox(this.dragger_mc.boxup_mc,this.upcolor,this.swidth, this.scrollerHeight , 100);
	drawBox(this.dragger_mc.boxdown_mc,this.presscolor,this.swidth, this.scrollerHeight , 100);
	drawBox(this.dragger_mc.boxover_mc,this.overcolor,this.swidth, this.scrollerHeight , 100);
	this.dragger_mc._y = this.swidth;
	this.addScrollerEvent(this.dragger_mc);
}

MppVScrollBar.prototype.centerThis = function (parent_mc, child_mc) {
	child_mc._x = (parent_mc._x + parent_mc._width / 2) - (child_mc._width / 2);
	child_mc._y =  (parent_mc._y + parent_mc._height / 2) - (child_mc._height / 2);
}

MppVScrollBar.prototype.addPhysicalEvent = function(target_mc){
	var jscroller = this;
	target_mc.onRollOver = function(){
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = true;
		target_mc.boxdown_mc._visible = false;
	}
	target_mc.onRelease = function(){
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = true;
		target_mc.boxdown_mc._visible = false;
		jscroller.broadcastMessage("onReleased");
	}
	target_mc.onDragOut = function(){
		target_mc.boxup_mc._visible = true;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = false;
		jscroller.broadcastMessage("onReleased");
	}
	target_mc.onRollOut = function(){
		target_mc.boxup_mc._visible = true;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = false;
	}
	
	target_mc.onPress = function() {
		//trace(target_mc  + " EQ "  + jscroller.up_mc);
		if(target_mc == jscroller.up_mc) {
			jscroller.broadcastMessage("onUp");
		}else {
			jscroller.broadcastMessage("onDown");
		}
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = true;
	}
}

MppVScrollBar.prototype.addScrollerEvent = function(target_mc){
	var jscroller = this;
	target_mc.onRollOver = function(){
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = true;
		target_mc.boxdown_mc._visible = false;
	}
	target_mc.onRelease = function(){
		jscroller.broadcastMessage("onDraggerReleased");
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = true;
		target_mc.boxdown_mc._visible = false;
	}
	target_mc.onDragOut = function(){
		target_mc.boxup_mc._visible = true;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = false;
	}
	target_mc.onRollOut = function(){
		target_mc.boxup_mc._visible = true;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = false;
	}
	
	target_mc.onPress = function() {
		jscroller.broadcastMessage("onDraggerPressed");
		target_mc.boxup_mc._visible = false;
		target_mc.boxover_mc._visible = false;
		target_mc.boxdown_mc._visible = true;
	}
}

//--------------------PUBLIC METHODS---------------------------\\


MppVScrollBar.prototype.visualize = function(){
	this.setScrollerHeight();
	this.paint();
	this.construct();
	this.initVars();
	trace(this.content_mc._x);
}

MppVScrollBar.prototype.setHeight = function (val) {
	this.sheight = val;
}
MppVScrollBar.prototype.setDimension = function( X , Y){ 
	this.container_mc._x = X;
	this.container_mc._y = Y;
}

MppVScrollBar.prototype.setSkinColor = function (bg, track){
	this.bgcolor = bg;
	this.trackcolor = track;
}

MppVScrollBar.prototype.setButtonColor = function(up, over , hit){
	this.overcolor =  over;
	this.presscolor = hit;
	this.upcolor = up;
}

MppVScrollBar.prototype.setLineColor = function(line, shadw, light){
	this.linecolor = line ;
	this.lineshadowcolor = shadw;
	this.linelightcolor =  light;
}

MppVScrollBar.prototype.setIconColor = function(color){
	this.arrowcolor = color;
}

