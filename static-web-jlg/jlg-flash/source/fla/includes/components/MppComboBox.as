/*ACTIONSCRIPT 1 CLASS BY JOPIRUIZEN
 *CLASS TYPE - COMPONENT
 *CUSTOM COMBOBOX
 * 
 */
#include "functions/DrawFunction.as"
function MppComboBox (refcon, depth){
	//trace("Creating combo");
	AsBroadcaster.initialize(this);
	this.container_mc = refcon
	this.container_mc.createEmptyMovieClip("combo" + depth + "_mc",depth);
	this.combo_mc = eval( this.container_mc + ".combo" + depth + "_mc");
	this.dataset = new Array();
	this.listcollection = new Array();
	
	Mouse.addListener(this);
	var mcb = this;
	this.onMouseDown = function(){
		if(mcb.isOpen == true && mcb.isOver == false){
			mcb.isOpen = false;
			mcb.undrawList();
		}
	}
}

MppComboBox.prototype.container_mc;
MppComboBox.prototype.combo_mc;

MppComboBox.prototype.button_mc;
MppComboBox.prototype.bglow_mc;

MppComboBox.prototype.textbox_mc;
MppComboBox.prototype.text_txt;

MppComboBox.prototype.list_mc;
MppComboBox.prototype.disable_mc;



MppComboBox.prototype.dataset;
MppComboBox.prototype.listcollection;

MppComboBox.prototype.isOpen = false;
MppComboBox.prototype.isOver = false;
//----------------SKin colors --------------------------\\


// SIZE AND DIMENSION PROPERTY
MppComboBox.prototype.Width = 100;
MppComboBox.prototype.Height = 20;
MppComboBox.prototype.X = 50;
MppComboBox.prototype.Y = 50;

//BOX PROEPERTY
MppComboBox.prototype.bgdisabled = 0xEEEEEE;
MppComboBox.prototype.bgcolor = 0xFFFFFF;
MppComboBox.prototype.boxshadow = 0x6F7777;
MppComboBox.prototype.boxlight = 0xEEEEEE;
MppComboBox.prototype.glowcolor = 0xFF9900; //0xDFFEDE;
MppComboBox.prototype.framecolor = 0x9C9C9C;

//FOnt PROPERTIES DEFAULT
MppComboBox.prototype.textcolor = 0x000000;
MppComboBox.prototype.textdisabled = 0xAEAEAE;

MppComboBox.prototype.textfont = "MyFont";
MppComboBox.prototype.textsize = 12;
MppComboBox.prototype.embedfont = true;

// TEXT INPUT PROPERTIES

MppComboBox.prototype.passwordtype = false;
MppComboBox.prototype.maxchar = 99999;

MppComboBox.prototype.itemHeight = 17;


//-----------------PRIVATE FUNCTIONS --------------------------------------------\\

MppComboBox.prototype.fontSetUp = function(target_txt){
	target_txt.maxChars = this.maxchar;
	target_txt.password = this.passwordtype;
	target_txt.embedFonts = this.embedfont;
	
	target_txt.type = "dynamic";
	target_txt.setTextFormat(this.getFormat());
	target_txt.setNewTextFormat(this.getFormat());
}


MppComboBox.prototype.getFormat = function() {
	
	//var txtformat:TextFormat = new TextFormat();
	var txtformat = new TextFormat();
	txtformat.font = this.textfont;
	txtformat.color = this.textcolor;
	txtformat.size = this.textsize;
	txtformat.bold = true;
	return txtformat;
};

MppComboBox.prototype.paint = function(){
	this.combo_mc.createEmptyMovieClip("list_mc", 1);
	this.combo_mc.createEmptyMovieClip("textbox_mc", 2);
	this.combo_mc.createEmptyMovieClip("button_mc", 3);
	this.combo_mc.createEmptyMovieClip("bglow_mc", 4);
	this.combo_mc.createEmptyMovieClip("disable_mc", 5);
	
	
	this.button_mc = this.combo_mc.button_mc;
	this.bglow_mc = this.combo_mc.bglow_mc;
	this.textbox_mc = this.combo_mc.textbox_mc;
	
	this.list_mc = this.combo_mc.list_mc;
	this.disable_mc = this.combo_mc.disable_mc;
	drawBox(this.disable_mc , this.bgcolor, this.Width - 2, this.Height - 2, 50);
	this.disable_mc._visible = false;
	this.disable_mc._x = 1;
	this.disable_mc._y = 1;
	this.paintTextBox();
	this.paintButton();
}

//---------------------------------------TEXT BOX--------------------------------\\
MppComboBox.prototype.paintTextBox = function(){
	
	this.textbox_mc.createEmptyMovieClip("box_mc",1);
	this.textbox_mc.createEmptyMovieClip("frame_mc",2);
	
	this.textbox_mc.createEmptyMovieClip("text_mc",3);
	this.textbox_mc.text_mc.createTextField("text_txt",1, 2, 2, this.Width - 3, this.Height - 2);
	this.text_txt = this.textbox_mc.text_mc.text_txt;
	this.textbox_mc.createEmptyMovieClip("mask_mc", 4);
	drawBox(this.textbox_mc.box_mc, this.bgcolor, this.Width - 2, this.Height - 2, 100);
	drawBox(this.textbox_mc.mask_mc, 0x000000, this.Width - 2, this.Height - 2, 100);
	this.textbox_mc.box_mc._x = 1;
	this.textbox_mc.box_mc._y = 1;
	this.textbox_mc.mask_mc._x = 1;
	this.textbox_mc.mask_mc._y = 1;
	//---TextBox DRAW FRAMES --- \\
	drawPixelVLine(this.textbox_mc.frame_mc, this.framecolor, 0, 1, this.Height - 2, 100);
	drawPixelHLine(this.textbox_mc.frame_mc, this.framecolor, 1, 0, this.Width - 1, 100);
	drawPixelHLine(this.textbox_mc.frame_mc, this.framecolor, 1, this.Height - 1, this.Width - 1, 100);
	drawPixelVLine(this.textbox_mc.frame_mc, this.boxshadow, this.Width - 2, 1, this.Height - 2, 100);
	drawPixelVLine(this.textbox_mc.frame_mc, this.boxshadow, 1, 1, this.Height - 2, 100);
	drawPixelVLine(this.textbox_mc.frame_mc, this.framecolor, this.Width - 1, 0, this.Height, 100);
	drawPixelHLine(this.textbox_mc.frame_mc,this.boxshadow, 1, 1, this.Width - 2, 80);
	drawPixelHLine(this.textbox_mc.frame_mc,this.boxlight, 1, this.Height - 2, this.Width - 2, 80);
	//-----END OF TEXT BOX FRAMES -----\\
	//-------------TEXTFIELD---------------\\
	
	this.fontSetUp(this.text_txt);
	this.textbox_mc.text_mc.setMask(this.textbox_mc.mask_mc);
}

//------------------------------------END TEXT BOX --------------------------------\\
//-----------------------------------------Painting SIDE BUTTON --------------------------\\
MppComboBox.prototype.paintButton = function(){
	this.button_mc.createEmptyMovieClip("box_mc",1);
	this.button_mc.createEmptyMovieClip("frame_mc", 2);
	this.button_mc.createEmptyMovieClip("icon_mc", 3);
	this.button_mc.createEmptyMovieClip("glow_mc", 4);
	this.button_mc.createEmptyMovieClip("glowbg_mc", 5);
	drawDownArrow(this.button_mc.icon_mc, this.framecolor, 8 ,4);
	this.drawButtonFrame();
	drawBox(this.button_mc.box_mc, this.boxlight, this.Height - 2, this.Height - 2, 100);
	this.button_mc.box_mc._x  = 1;
	this.button_mc.box_mc._y = 1;
	centerMC(this.button_mc, this.button_mc.icon_mc);
	this.button_mc._x = this.textbox_mc._x + this.textbox_mc._width;
	this.drawButtonGlow();
	this.addButtonEvents();
	this.unGlowButton();
}
MppComboBox.prototype.drawButtonGlow = function(){
	drawPixelHLine(this.button_mc.glow_mc, this.glowcolor, 0, 0, this.Height - 1, 80);
	drawPixelHLine(this.button_mc.glow_mc, this.glowcolor, 0, this.Height - 1, this.Height - 1, 80);
	drawPixelVLine(this.button_mc.glow_mc, this.glowcolor, 0, 1, this.Height - 2, 80);
	drawPixelVLine(this.button_mc.glow_mc, this.glowcolor, this.Height - 1, 1, this.Height - 2, 80);
	//--
	drawPixelHLine(this.button_mc.glow_mc, this.glowcolor, 1, 1, this.Height - 2, 50);
	drawPixelVLine(this.button_mc.glow_mc, this.glowcolor, this.Height - 2, 2, this.Height - 3, 50);
	drawPixelHLine(this.button_mc.glow_mc, this.glowcolor, 1, this.Height - 2, this.Height - 2, 50);
	drawPixelVLine(this.button_mc.glow_mc, this.glowcolor, 1, 2, this.Height - 3, 50);
	
	drawBox(this.button_mc.glowbg_mc, this.glowcolor, this.Height - 2, this.Height - 2, 50);
	this.button_mc.glowbg_mc._x  = 1;
	this.button_mc.glowbg_mc._y = 1;
}

MppComboBox.prototype.drawButtonFrame = function(){
	drawPixelHLine(this.button_mc.frame_mc, this.framecolor, 0, 0, this.Height - 1, 100);
	drawPixelHLine(this.button_mc.frame_mc, this.framecolor, 0, this.Height - 1, this.Height - 1, 100);
	drawPixelVLine(this.button_mc.frame_mc, this.framecolor, 0, 1, this.Height - 2, 100);
	drawPixelVLine(this.button_mc.frame_mc, this.framecolor, this.Height - 1, 1, this.Height - 2, 100);
	//--
	drawPixelHLine(this.button_mc.frame_mc, this.bgcolor, 1, 1, this.Height - 2, 50);
	drawPixelVLine(this.button_mc.frame_mc, this.bgcolor, this.Height - 2, 2, this.Height - 3, 50);
	drawPixelHLine(this.button_mc.frame_mc, this.framecolor, 1, this.Height - 2, this.Height - 2, 80);
	drawPixelVLine(this.button_mc.frame_mc, this.framecolor, 1, 2, this.Height - 3, 80);
	//--
	drawPixelHLine(this.button_mc.frame_mc, this.bgcolor, 2, 2, this.Height - 3, 70);
	drawPixelVLine(this.button_mc.frame_mc, this.bgcolor, this.Height - 3, 3, this.Height - 4, 70);
	drawPixelHLine(this.button_mc.frame_mc, this.framecolor, 2, this.Height - 3, this.Height - 3, 60);
	drawPixelVLine(this.button_mc.frame_mc, this.framecolor, 2, 3, this.Height - 4, 60);
	
}
MppComboBox.prototype.glowButton = function(){
	this.button_mc.glowbg_mc._visible = true;
	this.button_mc.glow_mc._visible = true;
}
MppComboBox.prototype.unGlowButton = function(){
	this.button_mc.glowbg_mc._visible = false;
	this.button_mc.glow_mc._visible = false;
}


MppComboBox.prototype.addButtonEvents = function(){
	var mcb = this;
	this.button_mc.onRollOver = function(){
		mcb.isOver = true;
		mcb.glowButton();
		mcb.button_mc.glowbg_mc._visible = false;
	}
	this.button_mc.onRelease = function(){
		mcb.glowButton();
		mcb.button_mc.glowbg_mc._visible = false;
	}
	this.button_mc.onDragOut = function(){
		mcb.isOver = false;
		mcb.unGlowButton();
	}
	this.button_mc.onRollOut = function(){
		mcb.isOver = false;
		mcb.unGlowButton();
	}
	this.button_mc.onPress = function(){
		mcb.glowButton();
		if(mcb.isOpen == false){
			mcb.isOpen = true;
			mcb.broadcastMessage("onOpen");
			mcb.redrawList();
		} else {
			mcb.isOpen = false;
			mcb.broadcastMessage("onClose");
			mcb.undrawList();
		}
		
	}
	
}

//-------------------------------------End of Buttons ------------------------------------\\


//--------------------------------Panels-------------------------\\
MppComboBox.prototype.redrawList = function(){
	var listheight;
	var mcb = this;
	listheight = this.dataset.length * this.itemHeight + 5;
	
	if(this.dataset.length == 0 ) {
		listheight = 1 * this.Height + 5;
	}
	this.list_mc.createEmptyMovieClip("mask_mc",1);
	this.list_mc.createEmptyMovieClip("box_mc",2);
	this.list_mc.box_mc.createEmptyMovieClip("bg_mc",-1);
	this.list_mc.box_mc.createEmptyMovieClip("frame_mc",-2);
	//----------------------DRAW FRAMES ------------------------\\
	drawPixelVLine(this.list_mc.box_mc.frame_mc, this.framecolor, 0, 1, listheight  - 2, 80); //Left LIne
	drawPixelVLine(this.list_mc.box_mc.frame_mc, this.framecolor, this.Width - 1, 1, listheight - 2, 80); //Right Line
	drawPixelHLine(this.list_mc.box_mc.frame_mc, this.framecolor, 1, 0, this.Width - 2, 80); //UPPER LINE
	drawPixelHLine(this.list_mc.box_mc.frame_mc, this.framecolor, 1, listheight  - 1, this.Width - 2, 80); //BOTTOM LINE
	//--
	drawPixelVLine(this.list_mc.box_mc.frame_mc, this.boxlight, 1, 2, listheight  - 3, 90); //Left LIne
	drawPixelVLine(this.list_mc.box_mc.frame_mc, this.boxshadow, this.Width - 2, 2, listheight - 3, 50); //Right Line
	drawPixelHLine(this.list_mc.box_mc.frame_mc, this.boxshadow, 2, 1, this.Width - 3, 50); //UPPER LINE
	drawPixelHLine(this.list_mc.box_mc.frame_mc, this.boxlight, 2, listheight  - 2, this.Width - 3, 90); //BOTTOM LINE
	//------------------------END DRAW FRAMES ----------------------\\
	drawBox(this.list_mc.box_mc.bg_mc, this.bgcolor, this.Width - 2, listheight - 2,100);
	drawBox(this.list_mc.mask_mc, 0x000000, this.Width, listheight,100);
	
	this.list_mc.box_mc.bg_mc._x = 1;
	this.list_mc.box_mc.bg_mc._y = 1;
	this.list_mc.box_mc.setMask(this.list_mc.mask_mc);
	this.list_mc._x = this.textbox_mc._x;
	this.list_mc._y = this.textbox_mc._y + this.textbox_mc._height;
	this.list_mc.box_mc._y = this.list_mc.mask_mc._y - (this.list_mc.box_mc._y + this.list_mc.box_mc._height);
	
	//create list 
	var listctr;
	var target_mc;
	for(listctr = 0; listctr < this.dataset.length; listctr++){
		this.list_mc.box_mc.createEmptyMovieClip("item" + listctr + "_mc", listctr);
		target_mc = eval(this.list_mc.box_mc + ".item" + listctr + "_mc");
		
		target_mc.createEmptyMovieClip("bg_mc",1);
		target_mc.createEmptyMovieClip("glow_mc",2);
		drawBox(target_mc.glow_mc, this.glowcolor, this.Width - 2, this.itemHeight - 2 ,80)
		
		drawBox(target_mc.bg_mc, this.glowcolor, this.Width - 2, this.itemHeight - 2,30)
		
		target_mc.createTextField("item_txt",3, 0, 0, this.Width - 4, this.itemHeight - 2);
		
		this.fontSetUp(target_mc.item_txt);
		target_mc.item_txt.text = this.dataset[listctr];
		target_mc.item_txt.selectable = false;
		//target_mc.item_txt.border = true;
		target_mc.glow_mc._visible = false;
		target_mc._x = 1;
		target_mc._y = (listctr * this.itemHeight) + 5;
		target_mc.count = listctr;
		target_mc.itemvalue = this.dataset[listctr];
		this.addListItemEvent(target_mc);
		//trace(target_mc);
	}
	
	this.list_mc.box_mc.onEnterFrame = function(){
		mcb.list_mc.box_mc._y += mcb.Height;
		if(mcb.list_mc.box_mc._y >= mcb.list_mc.mask_mc._y){
			mcb.list_mc.box_mc._y = mcb.list_mc.mask_mc._y;
			delete mcb.list_mc.box_mc.onEnterFrame;
		}
	}
	
}


MppComboBox.prototype.addListItemEvent = function(target_mc){
	var mcb = this;
	target_mc.onRollOver = function(){
		mcb.broadcastMessage("onItemOver" , target_mc);
		target_mc.glow_mc._visible  = true;
	}
	target_mc.onRollOut = function(){
		target_mc.glow_mc._visible  = false;
	}
	target_mc.onDragOut = function(){
		target_mc.glow_mc._visible  = false;
	}
	target_mc.onPress = function () {
		mcb.setSelectedItem(target_mc.itemvalue);
		var _event = new Object() ;
		_event.target = target_mc;
		_event.type = "onItemPress";
		_event.data =  target_mc.itemvalue;
		mcb.broadcastMessage("onItemPress" , target_mc);
	}
}

MppComboBox.prototype.undrawList = function(){
	var mcb = this;
	this.list_mc.box_mc.onEnterFrame = function() {
		mcb.list_mc.box_mc._y -= mcb.Height;
		if(mcb.list_mc.box_mc._y + mcb.list_mc.box_mc._height <= mcb.list_mc.mask_mc._y ){
			mcb.list_mc.box_mc.removeMovieClip();
			mcb.list_mc.mask_mc.removeMovieClip();
			delete mcb.list_mc.box_mc;
			delete mcb.list_mc.mask_mc;
			delete mcb.list_mc.box_mc;
			delete mcb.list_mc.box_mc.onEnterFrame;
		}
	}
}
//-------------------------END OF LIST-----------------------------------------\\

//-----------PUBLIC METHODS -------------------\\
MppComboBox.prototype.visualize = function(){
	this.paint();
}

MppComboBox.prototype.setSelectedItem = function(val) {
	
	this.text_txt.text = val;
}

MppComboBox.prototype.setDimension = function(X, Y){
	this.X = X;
	this.Y = Y;
	this.combo_mc._x = this.X;
	this.combo_mc._y = this.Y;
}

MppComboBox.prototype.setWidth = function(val) {
	this.Width = val;
}

MppComboBox.prototype.setHeight = function(val) {
	this.Height = val;
}

//---------------------

MppComboBox.prototype.setEnable = function(bool){
	//trace("Enable " + bool  + " Combo " + this.combo_mc);
	this.combo_mc.enabled = bool;
	this.button_mc.enabled = bool;
	this.text_txt.selectable = bool;
	this.disable_mc._visible = !bool;
}	

MppComboBox.prototype.getSelectedData = function () {
	return this.text_txt.text;
}

MppComboBox.prototype.setEditable = function(bool){
	if(bool == true){
		this.text_txt.type = "input";
	} else {
		this.text_txt.type = "dynamic";
	}
}
MppComboBox.prototype.deleteItem = function(index){
	this.dataset.splice(index,1);
}
MppComboBox.prototype.getItem = function(index){
	return this.dataset[index];
}

MppComboBox.prototype.addItem = function(val){
	this.dataset[this.dataset.length] = val;
}