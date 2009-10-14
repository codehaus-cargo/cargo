#include "functions/DrawFunction.as"
#include "functions/MppAligner.as"

function MppButtonAsset(container , mcdepth){
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.aligner = new MppAligner();
	this.container_mc = container;
	this.depth = mcdepth;
	this.initClips();
	this.paint();
	this.addEvents();
	//trace("button created Name:" + this.button_mc);
}



MppButtonAsset.prototype.width  = 75;
MppButtonAsset.prototype.height = 25;
MppButtonAsset.prototype.x = 0;
MppButtonAsset.prototype.y = 0;
MppButtonAsset.prototype.alpha = 100;

//---------Physical Propeties -----------\\
MppButtonAsset.prototype.container_mc;
MppButtonAsset.prototype.depth;

MppButtonAsset.prototype.button_mc;
MppButtonAsset.prototype.label_txt;

//------ COLOR SCHEME & STYLES----------\\

MppButtonAsset.prototype.forecolor = 0xFFFFFF;
MppButtonAsset.prototype.bgcolor = 0xE1E1E1;
MppButtonAsset.prototype.bordercolor = 0xB3B3B3;

MppButtonAsset.prototype.glowcolor = 0xDFFEDE;
//MppButtonAsset.prototype.glowcolor = 0xFF9900;

MppButtonAsset.prototype.lightcolor = 0xFFFFFF;
MppButtonAsset.prototype.shadowcolor = 0x515151;
MppButtonAsset.prototype.disabledcolor  = 0xB7B7B7;



//--------TEXT FORMAT --------------\\
MppButtonAsset.prototype.label = "Button";
MppButtonAsset.prototype.align = "center";
MppButtonAsset.prototype.font = "Arial";
MppButtonAsset.prototype.color = 0x404040;
MppButtonAsset.prototype.presscolor = 0x000000;
MppButtonAsset.prototype.overcolor = 0xFFFFFF;
MppButtonAsset.prototype.disablecolor = 0xCCCCCC;
MppButtonAsset.prototype.size = 12;

MppButtonAsset.prototype.bold = true;
MppButtonAsset.prototype.underline = false;
MppButtonAsset.prototype.italic = false;

MppButtonAsset.prototype.disable_bold = true;
MppButtonAsset.prototype.disable_underline = false;
MppButtonAsset.prototype.disable_italic = false;


MppButtonAsset.prototype.enable = true;

MppButtonAsset.prototype.shape = "ellipse"; // rectangle
//---------TOOLS --------------\\
MppButtonAsset.prototype.aligner;

//--------------PUBLIC METHODS-----------------\\
MppButtonAsset.prototype.deleteMe = function (){
	// trace("IN Deleting: " + this.button_mc);
	this.button_mc.removeMovieClip();
	delete this;
}

MppButtonAsset.prototype.setCursor = function (bool) {
	this.button_mc.useHandCursor = bool;
}

//------------------DISPLAYS ------

MppButtonAsset.prototype.setVisibility = function ( visibility ) {
	this.button_mc._visible = visibility;
}

MppButtonAsset.prototype.setAlpha = function (newalpha) {
	this.button_mc._alpha = newalpha
}
MppButtonAsset.prototype.getDisplay = function (){
	return this.button_mc;
}

MppButtonAsset.prototype.foreStyle = function ( bool ){
	this.button_mc.fore_mc._visible = bool;
}
MppButtonAsset.prototype.borderStyle = function ( bool ) {
	this.button_mc.border_mc._visible = bool
}
MppButtonAsset.prototype.shadowStyle = function ( bool ){
	this.button_mc.shadow_mc._visible = bool;
}
MppButtonAsset.prototype.lightStyle = function ( bool ){
	this.button_mc.light_mc._visible = bool;
}
MppButtonAsset.prototype.bgStyle = function ( bool ){
	this.button_mc.bg_mc._visible = bool;
}
MppButtonAsset.prototype.glowStyle = function ( bool ){
	this.button_mc.glow_mc._visible = bool;
}

MppButtonAsset.prototype.buttonShape = function( newshape) {
	this.shape = newshape;
	this.paint();
}

//------------------ICON---------------------------\\

MppButtonAsset.prototype.getIconDisplay = function () {
	return this.button_mc.icon_mc;
}

MppButtonAsset.prototype.getIconDepth = function () {
	return 9;
}
MppButtonAsset.prototype.attachIcon = function ( newicon  ){
	this.button_mc.attachMovie(newicon, "icon_mc", 9);
	this.alignIcon("left",5);
}
MppButtonAsset.prototype.alignIcon = function(alignment , distance) {
	if(alignment == "right"){
		aligner.rightOf(this.label_txt, this.button_mc.icon_mc);
		this.button_mc.icon_mc._x += distance;
	}else if(alignment == "left"){
		aligner.leftOf(this.label_txt, this.button_mc.icon_mc);
		this.button_mc.icon_mc._x -= distance;
	} else if (alignment == "center"){
		aligner.horizontalCenter(this.button_mc.bg_mc, this.button_mc.icon_mc);
	}
	aligner.verticalCenter(this.button_mc.bg_mc, this.button_mc.icon_mc);
}
//--------------------LABELS ---------------------------\\

MppButtonAsset.prototype.getLabel = function (){
	return this.label;
}
MppButtonAsset.prototype.setLabelWeight = function ( newbold, newitalic , newunderline) {
	this.bold = newbold;
	this.italic = newitalic;
	this.underline = newunderline;
	this.label_txt.setTextFormat(this.getFormat());
}

MppButtonAsset.prototype.setDisableWeight = function ( newbold, newitalic , newunderline) {
	this.disable_bold = newbold;
	this.disable_italic = newitalic;
	this.disable_underline = newunderline;
}

MppButtonAsset.prototype.setLabelStyle = function ( newcolor, newpresscolor, newovercolor, newdiscolor, newfont ,newsize ){
	this.disablecolor = newdiscolor;
	this.size = newsize
	this.color = newcolor;
	if( this.embedFont != true ){
		this.font = newfont;
	}
	this.presscolor = newpresscolor;
	this.overcolor = newovercolor;
	this.label_txt.setNewTextFormat(this.getFormat());
	 
}

MppButtonAsset.prototype.alignLabel = function ( newalign ){
	
	this.align = newalign;
	this.label_txt.setTextFormat(this.getFormat());
	if(this.align == "left") {
		
		this.aligner.alignLeft(this.button_mc.bg_mc, this.label_txt);
		this.label_txt._x += 5;
	} else if (this.align == "right") {
		this.aligner.alignRight(this.button_mc.bg_mc, this.label_txt);
		this.label_txt._x -= 5;
	} else {
		this.aligner.center(this.button_mc.bg_mc, this.label_txt);
	}
}

MppButtonAsset.prototype.embedFont;
MppButtonAsset.prototype.embedFonts = function ( fontName ) {
	if( fontName != null ) {
		this.embedFont = true;
		this.label_txt.embedFonts = true;
		var tf = new TextFormat();
		tf.font  = fontName;
		this.label_txt.setTextFormat( tf );
		this.label_txt.setNewTextFormat( tf );
	}
}
MppButtonAsset.prototype.setLabel = function ( value) {
	this.label = value;
	this.label_txt.text = value;
	this.configureLabel();
}


//------------------------DISPLAY Color------------------\\
MppButtonAsset.prototype.setAllColor = function ( newcolor) {
	this.forecolor = newcolor;
	this.shadowcolor = newcolor;
	this.bgcolor = newcolor;
	this.disabledcolor = newcolor;
	this.lightcolor = newcolor;
	this.glowcolor = newcolor;
	this.bordercolor = newcolor;
	this.paint();
}

MppButtonAsset.prototype.setForeColor = function(newcolor){
	this.forecolor = newcolor;
	this.drawFore();
}
MppButtonAsset.prototype.setShadowColor = function(newcolor){
	this.shadowcolor = newcolor;
	this.drawShadow();
}

MppButtonAsset.prototype.setBgColor = function(newcolor){
	this.bgcolor = newcolor;
	this.drawBg();
}
MppButtonAsset.prototype.setDisableColor = function(newcolor){
	this.disabledcolor = newcolor;
	this.drawDisabled();
}
MppButtonAsset.prototype.setLightColor = function(newcolor){
	this.lightcolor = newcolor;
	this.drawLight();
}
MppButtonAsset.prototype.setBorderColor = function(newcolor){
	this.bordercolor = newcolor;
	this.drawBorder();
}
MppButtonAsset.prototype.setGlowColor = function(newcolor){
	this.glowcolor = newcolor;
	this.drawGlow();
}



MppButtonAsset.prototype.setSize = function(Width , Height){
	
	this.width = Width;
	this.height = Height;
	this.paint();
}

MppButtonAsset.prototype.setDimension = function (X , Y) {
	this.button_mc._x = X;
	this.button_mc._y = Y;
}

MppButtonAsset.prototype.setGlow = function (value){
	this.button_mc.glow_mc._visible = value;
	var icolor = new Color( this.button_mc.icon_mc);
	if( value == true){
		icolor.setRGB(this.overcolor);
	} else {
		icolor.setRGB(this.color);
	}
}

MppButtonAsset.prototype.getWidth = function(){
	return this.button_mc._width;
}

MppButtonAsset.prototype.getHeight = function(){
	return this.button_mc._height;
}


//--------------------------------Techincals--------------------\\


MppButtonAsset.prototype.setEnable = function (value){
	this.enable = value;
	this.setCursor(value);
	this.button_mc.disable_mc._visible = !value;
	if(this.enable == true ) {
		this.label_txt.setTextFormat(this.getFormat());
		this.iconNormal();
	} else {
		this.label_txt.setTextFormat(this.getDisableFormat());
		var mccolor = new Color( this.button_mc.icon_mc);
		this.setGlow(false);
		mccolor.setRGB(this.disabledcolor);
	}
}

MppButtonAsset.prototype.showPress = function (value) {
	this.button_mc.disable_mc._visible = value;
	this.label_txt.setTextFormat(this.getPressFormat());
	var icolor = new Color (this.button_mc.icon_mc);
	if( value == true){
		icolor.setRGB(this.presscolor);
	} else {
		icolor.setRGB(this.overcolor);
	}
	
}


//--------------Private Methods ---------------------------\\



MppButtonAsset.prototype.initClips = function (){
	this.container_mc.createEmptyMovieClip("MppButtonAsset" + this.depth + "_mc", this.depth);
	this.button_mc = eval(this.container_mc + ".MppButtonAsset" + this.depth + "_mc");
	
	this.button_mc.createEmptyMovieClip("bg_mc", 1);
	this.button_mc.createEmptyMovieClip("fore_mc", 2);
	this.button_mc.createEmptyMovieClip("border_mc", 3);
	this.button_mc.createEmptyMovieClip("shadow_mc", 4);
	this.button_mc.createEmptyMovieClip("light_mc", 5);
	this.button_mc.createEmptyMovieClip("glow_mc", 8);
	this.button_mc.createEmptyMovieClip("disable_mc", 6);
	this.button_mc.createTextField("label_txt", 7 , this.x + 2, this.y + 2, this.width - 4, this.height - 2);
	this.label_txt = this.button_mc.label_txt;
}
MppButtonAsset.prototype.paint =function (){
	this.drawBorder();
	this.drawBg();
	this.drawFore();
	this.drawGlow();
	this.drawLight();
	this.drawShadow();
	this.drawDisabled();
	this.setGlow(false);
	this.setEnable(true);
	this.setLabel(this.label);
}



MppButtonAsset.prototype.drawBg = function (){
	this.button_mc.bg_mc.clear();
	if(this.shape == "ellipse"){
		drawEllipseFill(this.button_mc.bg_mc ,  0,  0 , this.width,this.height, this.bgcolor, 100);
	} else if (this.shape == "rectangle"){
		drawBoxInPos(this.button_mc.bg_mc ,  0,  0 , this.width,this.height, this.bgcolor, 100);
	}
}
MppButtonAsset.prototype.drawFore = function (){
	this.button_mc.fore_mc.clear();
	
	if(this.height < 9 || this.width < 9 ) {
		//trace("still h:" +  this.height + " w:" + this.width);
		if(this.shape == "ellipse"){
			drawEllipseFade(this.button_mc.fore_mc , 0,  0, this.width,this.height, this.forecolor, 100, 1, "IN", 20);
		} else if (this.shape == "rectangle"){
			drawFrameFade(this.button_mc.fore_mc , 0,  0, this.width,this.height, this.forecolor, 100, 1, "IN", 20);
		}
	} else {
		if(this.shape == "ellipse"){
			drawEllipseFade(this.button_mc.fore_mc , 0,  0, this.width,this.height, this.forecolor, 100, 3, "IN", 20);
		} else if (this.shape == "rectangle"){
			drawFrameFade(this.button_mc.fore_mc , 0,  0, this.width,this.height, this.forecolor, 100, 3, "IN", 20);
		}
	}
}
MppButtonAsset.prototype.drawBorder = function (){
	this.button_mc.border_mc.clear();
	
	if(this.shape == "ellipse"){
		drawEllipseBorder(this.button_mc.border_mc , 0,  0, this.width,  this.height, this.bordercolor, this.alpha);
	}else if (this.shape == "rectangle"){
		drawFrameInPos(this.button_mc.border_mc , 0,  0, this.width,  this.height, this.bordercolor, this.alpha);
	}
}
MppButtonAsset.prototype.drawGlow = function (){
	this.button_mc.glow_mc.clear();
	if(this.height < 9 || this.width < 9 ) {
		if(this.shape == "ellipse"){
			drawEllipseFade(this.button_mc.glow_mc , -1,  -1, this.width + 2,this.height + 2, this.glowcolor, 50, 1, "IN", 5);
		}else if (this.shape == "rectangle"){
			drawFrameFade(this.button_mc.glow_mc , -1,  -1, this.width + 2,this.height + 2, this.glowcolor, 50, 1, "IN", 5);
		}
	} else {
		if(this.shape == "ellipse"){
			drawEllipseFade(this.button_mc.glow_mc , -1,  -1, this.width + 2,this.height + 2, this.glowcolor, 50, 3, "IN", 5);
		}else if (this.shape == "rectangle"){
			drawFrameFade(this.button_mc.glow_mc , -1,  -1, this.width + 2,this.height + 2, this.glowcolor, 50, 3, "IN", 5);
		}
	}
}
MppButtonAsset.prototype.drawLight = function (){
	this.button_mc.light_mc.clear();
	if(this.shape == "ellipse"){
		drawHFade(this.button_mc.light_mc, 2, 1, 1 , this.lightcolor, 100, this.width - 4, "RIGHT", 1);
	}else if (this.shape == "rectangle"){
		drawHFade(this.button_mc.light_mc, 2, 1, 1 , this.lightcolor, 100, this.width - 4, "RIGHT", 1);
	}
}
MppButtonAsset.prototype.drawShadow = function (){
	this.button_mc.shadow_mc.clear();
	if(this.shape == "ellipse"){
		drawPixelHLine(this.button_mc.shadow_mc, this.shadowcolor,  2,this.height - 2 ,  this.width - 2, 60);
		this.shadowStyle(false);
	}else if (this.shape == "rectangle"){
		drawPixelHLine(this.button_mc.shadow_mc, this.shadowcolor,  1,this.height - 1 ,  this.width - 1, 60);
	}
}

MppButtonAsset.prototype.drawDisabled = function (){
	this.button_mc.disable_mc.clear();
	if(this.shape == "ellipse"){
		drawEllipseFill(this.button_mc.disable_mc, 0, 0, this.width, this.height, this.disabledcolor, 30 );
	}else if (this.shape == "rectangle"){
		drawBoxInPos(this.button_mc.disable_mc, 0, 0, this.width, this.height, this.disabledcolor, 30 );
	}
}



MppButtonAsset.prototype.configureLabel = function(){
	this.label_txt._width = this.label_txt.textWidth;
	this.label_txt._height = this.label_txt.textHeight;
	this.label_txt.selectable = false;
	this.label_txt.autoSize = true;
	this.label_txt.setTextFormat(this.getFormat());
	this.aligner.center(this.button_mc.bg_mc, this.label_txt);
}

MppButtonAsset.prototype.getDisableFormat = function (){
	var form:TextFormat = new TextFormat();
	var format = new TextFormat();
	format.size = this.size;
	format.align = this.align;
	format.font = this.font;
	format.color = this.disablecolor;
	format.bold =  this.disable_bold;
	format.underline = this.underline;
	format.italic = this.disable_italic;
	return format;
}


MppButtonAsset.prototype.getFormat = function (){
	var form:TextFormat = new TextFormat();
	var format = new TextFormat();
	format.size = this.size;
	format.align = this.align;
	format.font = this.font;
	format.color = this.color;
	format.bold =  this.bold;
	format.underline = this.underline;
	format.italic = this.italic;
	return format;
}

MppButtonAsset.prototype.getOverFormat = function (){
	var format =  new TextFormat();
	format.align = this.align;
	format.font = this.font;
	format.size = this.size;
	format.color = this.overcolor;
	format.bold =  this.bold;
	format.underline = this.underline;
	format.italic = this.italic;
	return format;
	
}

MppButtonAsset.prototype.getPressFormat = function (){
	var format =  new TextFormat();
	format.align = this.align;
	format.font = this.font;
	
	format.size = this.size;
	format.color = this.presscolor;
	format.bold =  this.bold;
	format.underline = this.underline;
	format.italic = this.italic;
	return format;
	
}


MppButtonAsset.prototype.addEvents = function (){
	//trace("ADDING EVENT");
	var mpp = this;
	this.button_mc.onRollOver = function (){
		if(mpp.enable == true){
			mpp.broadcastMessage("onRollOver");
			mpp.setGlow(true);
			mpp.label_txt.setTextFormat(mpp.getOverFormat());
		}
	}
	
	this.button_mc.onPress = function (){
		if(mpp.enable == true){
			mpp.broadcastMessage("onPress");
			mpp.showPress(true);
		}
	}
	
	this.button_mc.onRollOut = function (){
		if(mpp.enable == true){
			mpp.broadcastMessage("onRollOut");
			mpp.setGlow(false);
			mpp.label_txt.setTextFormat(mpp.getFormat());
		}
	}
	this.button_mc.onRelease = function (){
		if(mpp.enable == true){
			
			mpp.showPress(false);
			mpp.label_txt.setTextFormat(mpp.getOverFormat());
			var _event  = new Object();
			_event.target = mpp;
			_event.text = mpp.label_txt.text;
			_event.type = "onRelease";
			mpp.broadcastMessage("onRelease" , _event  );
		}
	}
	this.button_mc.onReleaseOutside = function (){
		if(mpp.enable == true){
			mpp.broadcastMessage("onReleaseOutside");
			mpp.setGlow(false);
			mpp.showPress(false);
			mpp.label_txt.setTextFormat(mpp.getFormat());
			mpp.iconNormal();
		}
	}
	this.button_mc.onDragOut = function (){
		if(mpp.enable == true){
			mpp.broadcastMessage("onDragOut");
			mpp.setGlow(false);
			mpp.showPress(false);
			mpp.label_txt.setTextFormat(mpp.getFormat());
			mpp.iconNormal();
		}
	}
}

MppButtonAsset.prototype.iconNormal = function () {
	var icolor = new Color( this.button_mc.icon_mc);
	icolor.setRGB(this.color);
}