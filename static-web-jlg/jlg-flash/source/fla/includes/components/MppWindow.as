#include "functions/DrawFunction.as"
#include "functions/MppAligner.as"
#include "MppButtonAsset.as"
#include "IconDisplay.as"
#include "functions/NumericConverter.as"
function MppWindow(container , newdepth) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.depth = newdepth;
	this.container_mc = container;
	
	this.aligner = new MppAligner();
	this.iconer = new IconDisplay();
	this.numConverter = new NumericConverter();
	
	this.iconer.setColor(this.color_icons);
	
	this.createClip();
	this.paint();
	
	this.stage_width = Stage.width;
	this.stage_height = Stage.height;
}

MppWindow.prototype.aligner;
MppWindow.prototype.iconer;
MppWindow.prototype.numConverter;

MppWindow.prototype.container_mc;
MppWindow.prototype.window_mc;
MppWindow.prototype.depth;

MppWindow.prototype.canvas_mc;
MppWindow.prototype.content_mc;
MppWindow.prototype.maskcontent_mc;

MppWindow.prototype.child_mc = null;

MppWindow.prototype.titlebar_mc;
MppWindow.prototype.titlebar_txt;

MppWindow.prototype.dragger_mc;

MppWindow.prototype.mini_btn = null;
MppWindow.prototype.max_btn  = null;
MppWindow.prototype.close_btn = null;

MppWindow.prototype.icon_close_mc;
MppWindow.prototype.icon_min_mc;
MppWindow.prototype.icon_max_mc;




//-----------DISPLAY PROPETYIES-----------\\

//--- Sizes and Dimensions

MppWindow.prototype.stage_height;
MppWindow.prototype.stage_width;

MppWindow.prototype.window_width = 300;
MppWindow.prototype.window_height = 300;


MppWindow.prototype.window_old_width;
MppWindow.prototype.window_old_height;
MppWindow.prototype.window_old_x;
MppWindow.prototype.window_old_y;


MppWindow.prototype.window_normal_width;
MppWindow.prototype.window_normal_height;
MppWindow.prototype.window_normal_x;
MppWindow.prototype.window_normal_y;



MppWindow.prototype.titlebar_height = 25;

MppWindow.prototype.buttons_size = 17; //20by20
MppWindow.prototype.buttons_spacing = 2;

//-------Shapes - Colors - Styles - \\
MppWindow.prototype.color_window_border = 0xD8CFAF;
MppWindow.prototype.color_window_canvas = 0xECE9D8;

MppWindow.prototype.color_titlebar_body = 0xA2B38F;
MppWindow.prototype.color_titlebar_border = 0x99A189;
MppWindow.prototype.color_titlebar_light = 0xBECAB0;
MppWindow.prototype.color_titlebar_shadow = 0x7C8D61;

MppWindow.prototype.color_icons = 0xFFFFFF;

MppWindow.prototype.shape_window = "rectangle";


//----------TextFormat ---------------\\

MppWindow.prototype.format_title_align = "left";
MppWindow.prototype.format_title_font = "Arial";
MppWindow.prototype.format_title_size = 12;

MppWindow.prototype.format_title_bold = true;
MppWindow.prototype.format_title_underline = false;
MppWindow.prototype.format_title_italic = false;

MppWindow.prototype.window_title = "Window";

// WInodw States --


MppWindow.prototype.bool_maximized = false;
MppWindow.prototype.bool_minimized = false;
MppWindow.prototype.bool_fromNormalMode = true;

MppWindow.prototype.bool_childAlignCenter = true;
MppWindow.prototype.bool_maximizable = true;
MppWindow.prototype.bool_minimizable = true;
MppWindow.prototype.bool_closable = true;
MppWindow.prototype.bool_draggable = true;

MppWindow.prototype.bool_open = true;
//------------PUblic Methods----------\\

MppWindow.prototype.getContentWidthByPercent = function ( percentage) {
	return this.numConverter.percentOf(this.content_mc._width , percentage);
}
MppWindow.prototype.getContentHeightByPercent = function ( percentage) {
	return this.numConverter.percentOf(this.content_mc._height , percentage);
}

MppWindow.prototype.getContentContainer = function () {
	return this.content_mc;
}

MppWindow.prototype.setChild = function ( newchild) {
	//trace("WinChild:" + newchild);
	this.child_mc = newchild
	this.centerChild();
}


MppWindow.prototype.maximizeWindow = function () {
	
	
	if(this.bool_maximized == false ) {
		this.bool_maximized = true;
		this.window_normal_width = this.window_width;
		this.window_normal_height = this.window_height;
		this.window_normal_x = this.getX();
		this.window_normal_y = this.getY();
		this.aligner.alignLeft(this.content_mc, this.child_mc);
		this.setDimension(0, 0);
		this.setSize(Stage.width,Stage.height);
		this.broadcastMessage("onMaximize");
		this.bool_draggable = false;
	}  else {
		this.aligner.alignLeft(this.content_mc, this.child_mc);
		if(this.bool_minimized == true ) {
			
			
			this.bool_maximized = true;
			this.bool_minimized = false;
			this.window_normal_width = this.window_old_width;
			this.window_normal_height = this.window_old_height;
			this.window_normal_x = this.window_old_x;
			this.window_normal_y = this.window_old_y;
			
			if(this.bool_fromNormalMode == true){
				this.setDimension(0, 0);
				this.broadcastMessage("onMaximize");
				this.setSize(Stage.width,Stage.height);
				this.bool_fromNormalMode = false;
				this.bool_draggable = false;
			} else {
				this.setDimension(this.window_normal_x, this.window_normal_y);
				this.setSize(this.window_normal_width,this.window_normal_height);
				this.bool_fromNormalMode == true;
				this.bool_maximized = false;
				this.bool_draggable = true;
			}
			
		} else {
			this.bool_draggable = true;
			this.bool_fromNormalMode = true;
			this.setDimension(this.window_normal_x, this.window_normal_y);
			this.setSize(this.window_normal_width,this.window_normal_height);
			this.bool_maximized = false;
		}
		
		this.bool_minimized = false;
		
	}
	
	this.content_mc._visible = true;
	
}

MppWindow.prototype.minimizeWindow = function () {
	this.bool_draggable = true;
	
	if(this.bool_minimized == false ) {
		
		if(this.bool_maximized == true) {
			this.bool_fromNormalMode = true;
			this.window_old_width = this.window_normal_width;
			this.window_old_height = this.window_normal_height;
			this.window_old_x = this.window_normal_x;
			this.window_old_y = this.window_normal_y;
		} else {
			this.window_old_width = this.window_width;
			this.window_old_height = this.window_height;
			this.window_old_x = this.getX();
			this.window_old_y = this.getY();
			this.bool_fromNormalMode = false;
		}
		
		this.broadcastMessage("onMinimize");
		this.aligner.alignLeft(this.content_mc, this.child_mc);
		this.setSize(150,25);
		this.setDimension(0, Stage.height - this.window_height);
		this.content_mc._visible = false;
		this.bool_minimized = true;
	}  else {
		this.bool_fromNormalMode = false;
		this.aligner.alignLeft(this.content_mc, this.child_mc);
		if(this.bool_maximized == true) {
			this.setDimension(0, 0);
			this.broadcastMessage("onMaximize");
			this.setSize(Stage.width,Stage.height);
		} else {
			
			this.bool_fromNormalMode = true;
			this.setDimension(this.window_old_x, this.window_old_y);
			this.setSize(this.window_old_width,this.window_old_height);
		}
		this.content_mc._visible = true;
		this.bool_minimized = false;
	}
//	trace("onMiniMized| Minimized:" + this.bool_minimized + " Maximized:" + this.bool_maximized);
}


MppWindow.prototype.minimizeOn = function ( bool) {
	this.bool_minimizable = bool;
	this.alignButton();
}

MppWindow.prototype.maximizeOn = function ( bool ) {
	this.bool_maximizable = bool;
	this.alignButton();
}
MppWindow.prototype.closeOn = function ( bool ) {
	this.bool_closable = bool;
	this.alignButton();
}

MppWindow.prototype.dragOn = function (bool ) {
	this.bool_draggable = bool;
}



MppWindow.prototype.childAtCenter = function ( bool) {
	this.bool_childAlignCenter = bool;
}

MppWindow.prototype.closeWindow = function () {
	this.window_mc._visible = false;
	this.bool_open = false;
	this.broadcastMessage("onCloseWindow");
}

MppWindow.prototype.openWindow = function () {
	//trace("onOpenWindow");
	this.bool_open = true;
	this.window_mc._visible = true;
	this.broadcastMessage("onOpenWindow");
}

MppWindow.prototype.isOepn = function () {
	return this.bool_open;
}

//-------------------PHYSICAL PROPS
MppWindow.prototype.setIconColor = function (newcolor) {
	this.color_icons = newcolor ;
	this.titlebar_txt.setTextFormat(this.getTitleFormat());
	this.paint();
}


MppWindow.prototype.setTitleFormat = function (newfont, newsize, newalign) {
	this.format_title_align = newalign;
	this.format_title_font = newfont;
	this.format_title_size = newsize;
	this.titlebar_txt.setTextFormat(this.getTitleFormat());
}

MppWindow.prototype.setTitleWeight = function ( newbold , newitalic, newunderline ) {
	 this.format_title_bold = newbold;
	 this.format_title_underline = newunderline;
	this.format_title_italic = newitalic;
	this.titlebar_txt.setTextFormat(this.getTitleFormat());
}

MppWindow.prototype.setTitlebarColor = function  (BodyColor , ShadowColor, LightColor) {
	this.color_titlebar_body = BodyColor;
	this.color_titlebar_light = LightColor;
	this.color_titlebar_shadow = ShadowColor;
	this.paint();
}

MppWindow.prototype.setWindowColor = function (CanvasColor , BorderColor) {
	this.color_window_canvas = CanvasColor;
	this.color_window_border = BorderColor;
	this.paint();
}


MppWindow.prototype.setSize = function (newwidth, newheight ) {
	this.window_width = newwidth;
	this.window_height = newheight;
	this.paint();
	if(this.child_mc != null){
		this.centerChild();
	}
}

MppWindow.prototype.setDimension = function (newX, newY) {
	this.window_mc._x = newX;
	this.window_mc._y = newY;
}

MppWindow.prototype.setTitle = function ( newtitle ) {
	this.window_title = newtitle;
	this.titlebar_txt.text = this.window_title;
	this.titlebar_txt.setTextFormat(this.getTitleFormat());
}

MppWindow.prototype.getX = function () {
	return this.window_mc._x;	
}
MppWindow.prototype.getY = function () {
	return this.window_mc._y;
}

//------------Private Methods ---------\\
MppWindow.prototype.createClip = function() {
	this.container_mc.createEmptyMovieClip("MppWindow" + this.depth + "_mc", this.depth );
	this.window_mc = eval(this.container_mc + ".MppWindow" + this.depth + "_mc");
	this.window_mc.createEmptyMovieClip("canvas_mc", 1);
	
	this.canvas_mc  = this.window_mc.canvas_mc;
	this.canvas_mc.createEmptyMovieClip("box_mc", 1);
	this.canvas_mc.createEmptyMovieClip("content_mc",2);
	this.canvas_mc.createEmptyMovieClip("maskcontent_mc", 3);
	
	this.canvas_mc.createEmptyMovieClip("border_mc",4 );
	this.canvas_mc.createEmptyMovieClip("titlebar_mc",5);
	
	this.maskcontent_mc = this.canvas_mc.maskcontent_mc;
	this.content_mc = this.canvas_mc.content_mc;
	
	this.titlebar_mc = this.canvas_mc.titlebar_mc;
}

MppWindow.prototype.paint = function () {
	this.paintWindow();
	this.paintTitlebar();
	this.paintDragger();
	
	this.titlebar_txt.text = this.window_title;
	this.titlebar_txt.setTextFormat(this.getTitleFormat());
	
	this.paintContent();
	this.addEvents();
}


MppWindow.prototype.paintWindow = function() {
	this.canvas_mc.box_mc.clear();
	this.canvas_mc.border_mc.clear();
	
	
	if(this.shape_window == "rectangle") {
		drawBoxInPos(this.canvas_mc.box_mc, 0 , 0, this.window_width, this.window_height, this.color_window_canvas, 100);
		drawFrameInPos(this.canvas_mc.border_mc, 0 , 0, this.window_width, this.window_height, this.color_window_border, 100);
		drawFrameFade(this.canvas_mc.border_mc, 0 , 0, this.window_width, this.window_height, this.color_window_border, 100,2, "IN", 20);
	} else if (this.shape_window == "ellipse") {
		drawEllipseFill(this.canvas_mc.box_mc, 0 , 0, this.window_width, this.window_height, this.color_window_canvas, 100);
		drawEllipseBorder(this.canvas_mc.border_mc, 0 , 0, this.window_width, this.window_height, this.color_window_border, 100);
	}
}

MppWindow.prototype.paintTitlebar = function () {
	this.titlebar_mc.clear();
	if( this.close_btn != null ) {
		this.close_btn.deleteMe();
		this.min_btn.deleteMe();
		this.max_btn.deleteMe();
	}
	drawBoxInPos(this.titlebar_mc, 1 , 1, this.window_width - 1, this.titlebar_height, this.color_titlebar_body, 100);
	//-lights
	drawPixelHLine(this.titlebar_mc, this.color_titlebar_light,  1 , 0 , this.window_width - 2,  100);
	drawPixelHLine(this.titlebar_mc, this.color_titlebar_light,  1 , 1 , this.window_width - 2,  100);
	//shadow
	drawPixelHLine(this.titlebar_mc, this.color_titlebar_shadow,  1 , this.titlebar_height - 1 , this.window_width - 2,  100);
	drawPixelHLine(this.titlebar_mc, this.color_titlebar_shadow,  1 , this.titlebar_height - 2 , this.window_width - 2,  100);
	
	this.titlebar_mc.createEmptyMovieClip("dragger_mc", 1);
	this.dragger_mc = this.titlebar_mc.dragger_mc;
	
	this.close_btn = new MppButtonAsset(this.titlebar_mc, 2);
	this.max_btn = new MppButtonAsset(this.titlebar_mc, 3);
	this.min_btn = new MppButtonAsset(this.titlebar_mc, 4);
	
	this.setButton(this.close_btn);
	this.setButton(this.max_btn);
	this.setButton(this.min_btn);
	this.alignButton();
	this.createIcon();
}

MppWindow.prototype.setButton = function (mppbtn) {
	mppbtn.setSize(this.buttons_size, this.buttons_size);
	mppbtn.setLabel("");
	mppbtn.buttonShape("rectangle");
	mppbtn.setAlpha(80);
	mppbtn.foreStyle(false);
	mppbtn.setBorderColor(this.color_titlebar_shadow);
	mppbtn.setBgColor(this.color_titlebar_light);
	mppbtn.setDisableColor(0x000000);
	this.aligner.verticalCenter(this.titlebar_mc, mppbtn.getDisplay());
}

MppWindow.prototype.alignButton = function () {
	if ( this.bool_closable == true ) {
		this.aligner.alignRight(this.titlebar_mc, this.close_btn.getDisplay());
		this.close_btn.getDisplay()._x -= this.buttons_spacing;
		
		if( this.bool_maximizable == true) {
			this.aligner.leftOf(this.close_btn.getDisplay(), this.max_btn.getDisplay());
			this.max_btn.getDisplay()._x -= this.buttons_spacing;
			this.aligner.leftOf(this.max_btn.getDisplay(), this.min_btn.getDisplay());
			this.min_btn.getDisplay()._x -= this.buttons_spacing;
		} else  if  ( this.bool_maximizable == false) {
			this.aligner.leftOf(this.close_btn.getDisplay(), this.min_btn.getDisplay());
			this.min_btn.getDisplay()._x -= this.buttons_spacing;
		}
		
	} else {
		if( this.bool_maximizable == true) {
			this.aligner.alignRight(this.titlebar_mc, this.max_btn.getDisplay());
			this.max_btn.getDisplay()._x -= this.buttons_spacing;
			this.aligner.leftOf(this.max_btn.getDisplay(), this.min_btn.getDisplay());
			this.min_btn.getDisplay()._x -= this.buttons_spacing;
		} else  if  ( this.bool_maximizable == false) {
			this.aligner.alignRight(this.titlebar_mc, this.min_btn.getDisplay());
			this.min_btn.getDisplay()._x -= this.buttons_spacing;
		}
		
	}
	this.close_btn.setVisibility( this.bool_closable );
	this.max_btn.setVisibility( this.bool_maximizable );
	this.min_btn.setVisibility( this.bool_minimizable );
}

MppWindow.prototype.createIcon = function () {
	this.icon_max_mc.clear();
	this.icon_close_mc.clear();
	this.icon_min_mc.clear();
	
	this.close_btn.getDisplay().createEmptyMovieClip("icon_mc", this.close_btn.getIconDepth());
	this.icon_close_mc = this.close_btn.getDisplay().icon_mc;
	this.iconer.rotatedCrossIcon(this.icon_close_mc);
	this.aligner.center(this.close_btn.getDisplay(), this.icon_close_mc);
	this.icon_close_mc._x += 1;
	this.icon_close_mc._y += 1;
	
	
	this.max_btn.getDisplay().createEmptyMovieClip("icon_mc", this.max_btn.getIconDepth());
	this.icon_max_mc = this.max_btn.getDisplay().icon_mc;
	this.iconer.maximizeIcon(this.icon_max_mc);
	this.aligner.center(this.max_btn.getDisplay(), this.icon_max_mc);
	
	this.min_btn.getDisplay().createEmptyMovieClip("icon_mc", this.min_btn.getIconDepth());
	this.icon_min_mc = this.min_btn.getDisplay().icon_mc;
	//trace(this.icon_min_mc);
	this.iconer.minimizeIcon(this.icon_min_mc);
	this.aligner.center(this.min_btn.getDisplay(), this.icon_min_mc);
}

MppWindow.prototype.paintDragger = function (){
	this.dragger_mc.clear();
	drawBoxInPos(this.dragger_mc , this.titlebar_mc._x + 1, this.titlebar_mc._y + 3, this.titlebar_mc._width - (this.buttons_size * 3 + this.buttons_spacing * 3),this.buttons_size + 3 ,this.color_titlebar_body, 100);
	//this.color_titlebar_body
	this.dragger_mc.createTextField("title_txt",1,0,0, this.dragger_mc._width - this.buttons_spacing, this.dragger_mc._height);
	this.titlebar_txt = this.dragger_mc.title_txt;
	this.aligner.center(this.dragger_mc, this.titlebar_txt);
	this.setTitleProperty();
}

MppWindow.prototype.paintContent = function () {
	this.content_mc.clear();
	this.maskcontent_mc.clear();
	
	drawBoxInPos(this.content_mc, 0 , 0, this.window_width, this.window_height - this.titlebar_height,this.color_window_canvas, 100);//this.color_window_canvas
	drawBoxInPos(this.maskcontent_mc, 0 , 0, this.window_width, this.window_height - this.titlebar_height, 0x000000, 100);
	
	this.aligner.bottomOf(this.titlebar_mc, this.content_mc);
	this.aligner.bottomOf(this.titlebar_mc, this.maskcontent_mc);
	
	this.content_mc.setMask(this.maskcontent_mc);
}
MppWindow.prototype.setTitleProperty = function () {
	this.titlebar_txt.selectable = false;
}

MppWindow.prototype.getTitleFormat = function () {
	var format =  new TextFormat();
	format.align = this.format_title_align;
	format.font = this.format_title_font;
	
	format.size = this.format_title_size;
	format.color = this.color_icons;
	
	format.bold =  this.format_title_bold;
	format.underline = this.format_title_underline;
	format.italic = this.format_title_italic;
	return format;
}


MppWindow.prototype.centerChild = function () {
	this.aligner.center(this.content_mc, this.child_mc);
}

MppWindow.prototype.addEvents = function () {
	var mppwin = this;
	this.dragger_mc.useHandCursor = false;
	this.close_btn.onRelease = function () {
		if(mppwin.bool_closable == true){
			mppwin.closeWindow();
		}
	}
	
	this.min_btn.onRelease = function () {
		if(mppwin.bool_minimizable == true) {
			mppwin.minimizeWindow();
		}
	}
	
	this.max_btn.onRelease = function () {
		if(mppwin.bool_maximizable == true ) {
			mppwin.maximizeWindow();
		}
	}
	this.dragger_mc.onPress = function () {
		if(mppwin.bool_draggable == true ) {
			mppwin.broadcastMessage("onDragWindow");
			mppwin.window_mc.startDrag();
		}
	}
	this.dragger_mc.onRelease = function () {
		if(mppwin.bool_draggable == true ) {
			mppwin.broadcastMessage("onDropWindow");
			mppwin.window_mc.stopDrag();
		}
	}
	
	
}