/*THIS ACTIONSCRIPT 1 CLASS WAS WRITEN BY JOPIRUIZEN
 *CLASS TYPE - COMPONENT
 *CUSTOM TEXT BOX
 * TO USE IT, JUST INCLUDE IT using the #include
 */

function JopiText(ref, Depth) {
	AsBroadcaster.initialize(this);
	this.container = ref;
	this.container.createEmptyMovieClip("text"+Depth, Depth);
	this.mc = eval(this.container+".text"+Depth);
}
JopiText.prototype.container;
JopiText.prototype.mc;
JopiText.prototype.txt;

//----------------------------------------PUBLIC PROPERTIES --------------------------------------\\

// SIZE AND DIMENSION PROPERTY
JopiText.prototype.Width = 100;
JopiText.prototype.Height = 20;
JopiText.prototype.X = 50;
JopiText.prototype.Y = 50;

//BOX PROEPERTY
JopiText.prototype.bgdisabled = 0xEEEEEE;
JopiText.prototype.bgcolor = 0xFFFFFF;
JopiText.prototype.boxshadow = 0x6F7777;
JopiText.prototype.boxlight = 0xEEEEEE;
JopiText.prototype.glowcolor = 0xDFFEDE;

//FOnt PROPERTIES DEFAULT
JopiText.prototype.textcolor = 0x000000;
JopiText.prototype.textdisabled = 0xAEAEAE;

JopiText.prototype.textfont = "Arial";
JopiText.prototype.textsize = 12;
JopiText.prototype.framecolor = 0xD5DDDD;

// TEXT INPUT PROPERTIES
JopiText.prototype.embedfont = false;
JopiText.prototype.passwordtype = false;
JopiText.prototype.maxchar = 50;

// TRAPPER

JopiText.prototype.firstTouch = false;
//---------------------------------------- METHODS --------------------------------------\\

//----------------------------------------PUBLIC METHODS --------------------------------------\\
// DRAW THE BOX OF THE TEXT BOX (PRIVATE)
JopiText.prototype.drawFrame = function(target_mc, color, W, H) {
	target_mc.lineStyle(1, color);
	target_mc.moveTo(0, 0);
	//target_mc.beginFill(color);
	target_mc.lineTo(W, 0);
	target_mc.lineTo(W, H);
	target_mc.lineTo(0, H);
	target_mc.lineTo(0, 0);
	//target_mc.endFill();
};

JopiText.prototype.drawBox = function(target_mc, color, W, H){
	target_mc.lineStyle(0);
	target_mc.beginFill(color);
	target_mc.lineTo(W, 0);
	target_mc.lineTo(W, H);
	target_mc.lineTo(0, H);
	target_mc.lineTo(0, 0);
	target_mc.endFill();
}

JopiText.prototype.drawGlow = function(target_mc, color, W, H){
	W += 1;
	H += 1;
	target_mc.lineStyle(5, color,50);
	target_mc.moveTo(-1, -1);
	target_mc.lineTo(W, -1);
	target_mc.lineTo(W, H);
	target_mc.lineTo(-1, H);
	target_mc.lineTo(-1, -1);
}

JopiText.prototype.drawMask = function(target_mc, color, W, H){
	W += 2;
	H += 2;
	target_mc.lineStyle(3, color);
	target_mc.beginFill(color);
	target_mc.moveTo(-2, -2);
	target_mc.lineTo(W, -2);
	target_mc.lineTo(W, H);
	target_mc.lineTo(-2, H);
	target_mc.lineTo(-2, -2);
	target_mc.endFill();
}
// DRAW THE SHADOW OF THE BOX (PRIVATE)
JopiText.prototype.drawShadow = function(target_mc, shadowcolor, lightcolor, W, H) {
	W -= 1;
	H -= 1;
	target_mc.lineStyle(1, shadowcolor);
	target_mc.moveTo(1, 1);
	target_mc.lineTo(W, 1);
	target_mc.lineTo(W, H);
	target_mc.lineStyle(1, lightcolor);
	target_mc.lineTo(1, H);
	target_mc.lineTo(1, 1);
};



JopiText.prototype.fontSetUp = function(){
	this.txt.maxChars = this.maxchar;
	this.txt.password = this.passwordtype;
	this.txt.type = "input";
	this.txt.setTextFormat(this.getFormat());
	this.txt.setNewTextFormat(this.getFormat());
}

JopiText.prototype.getFormat = function() {
	this.txt.embedFonts = this.embedfont;
	//var txtformat:TextFormat = new TextFormat();
	var txtformat = new TextFormat();
	txtformat.font = this.textfont;
	txtformat.color = this.textcolor;
	txtformat.size = this.textsize;
	
	return txtformat;
};

//----------------------------------------PUBLIC METHODS --------------------------------------\\

// THIS WILL MAKE THE TEXTBOX APPEAR
JopiText.prototype.visualize = function() {
	
	this.mc.createEmptyMovieClip("mask_mc", 1);
	this.mc.createEmptyMovieClip("box_mc", 0);
	this.mc.createEmptyMovieClip("shadow_mc", 2);
	this.mc.createEmptyMovieClip("frame_mc", 3);
	this.mc.createEmptyMovieClip("glow_mc", 5);
	
	var target_mc = this.mc.shadow_mc;
	this.drawShadow(target_mc, this.boxshadow, this.boxlight, this.Width, this.Height);
	
	target_mc = this.mc.frame_mc;
	this.drawFrame(target_mc, this.framecolor, this.Width, this.Height);
	
	target_mc = this.mc.box_mc;
	this.drawBox(target_mc, this.bgcolor, this.Width, this.Height);
	
	target_mc = this.mc.glow_mc;
	this.drawGlow(target_mc, this.glowcolor, this.Width, this.Height);
	
	this.unglow();
	
	target_mc = this.mc.mask_mc;
	this.drawMask(target_mc, 0x000000, this.Width, this.Height);
	
	this.mc.createTextField("text_txt",4, this.mc._x+2, this.mc._y+2, this.mc._width-2, this.mc._height-2);
	this.txt = this.mc.text_txt;
	this.mc.setMask(this.mc.mask_mc);
	
	this.fontSetUp();
	this.setDimension(this.X, this.Y);
	this.addEvent();
};

JopiText.prototype.setTextValue = function(val) {
	trace(this.embedfont + " " + this.textfont + " " + this.textcolor);
	this.firstTouch = true;
	this.txt.text = val;
	this.txt.setTextFormat(this.getFormat());
};
JopiText.prototype.getTextValue = function() {
	return this.txt.text;
};
JopiText.prototype.setDimension = function(X, Y) {
	this.mc._x = X;
	this.mc._y = Y;
};

JopiText.prototype.setEnable = function (bool){
	var txtformat = new TextFormat();
	if(bool == false){
		this.txt.type = "dynamic";
		txtformat.color = this.textdisabled;
		this.txt.setTextFormat(txtformat);
		this.drawBox(this.mc.box_mc, this.bgdisabled, this.Width, this.Height);
	} else {
		this.txt.type = "input";
		this.txt.setTextFormat(this.getFormat());
		this.txt.setNewTextFormat(this.getFormat());
		this.drawBox(this.mc.box_mc, this.bgcolor, this.Width, this.Height);
	}
}

JopiText.prototype.glow = function(){
	this.mc.glow_mc._visible = true;
}

JopiText.prototype.unglow = function(){
	this.mc.glow_mc._visible = false;
}

JopiText.prototype.addEvent = function(){
	var jopitext = this;
	this.txt.onSetFocus = function(obj){
		jopitext.glow();
		jopitext.broadcastMessage("gotFocus");
		if(jopitext.firstTouch == false) {
			jopitext.firstTouch = true;
		}
	}
	this.txt.onKillFocus = function(obj){
		jopitext.unglow();
		jopitext.broadcastMessage("lostFocus");
	}
	
	this.txt.onChanged = function(obj){
		jopitext.broadcastMessage("onChange");
	}
}
