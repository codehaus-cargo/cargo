#include "functions/DrawFunction.as"
#include "functions/vo/MppMenuItems.as"
#include "MppButtonAsset.as"


function MppVerticalMenu( container , depth){
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.container_mc = container;
	this.menuDepth = depth;
	this.createEmptyClips();
	
	this.menuCollection = new ItemCollection();
	
	this.paint();
}



//------------------PROPERTIES
MppVerticalMenu.prototype.container_mc;
MppVerticalMenu.prototype.menuDepth;
MppVerticalMenu.prototype.menu_mc;
MppVerticalMenu.prototype.main_btn;
MppVerticalMenu.prototype.popup_mc;
MppVerticalMenu.prototype.popup;

//Dimension and Properties
MppVerticalMenu.prototype.main_width = 40;
MppVerticalMenu.prototype.main_height = 20;
MppVerticalMenu.prototype.x = 100;
MppVerticalMenu.prototype.y = 100;
MppVerticalMenu.prototype.alpha = 100;

//Main properties and Style --

MppVerticalMenu.prototype.main_label = "Menu";


//----------------FOnts

MppVerticalMenu.prototype.font = "Arial";
MppVerticalMenu.prototype.color = 0x404040;
MppVerticalMenu.prototype.presscolor = 0x444444;
MppVerticalMenu.prototype.overcolor = 0x706D65;
MppVerticalMenu.prototype.disablecolor = 0xCCCCCC;
MppVerticalMenu.prototype.size = 11;


MppVerticalMenu.prototype.bold =  false;
MppVerticalMenu.prototype.underline = false;
MppVerticalMenu.prototype.italic = false;
MppVerticalMenu.prototype.main_alignment = "left";
//-----------Dimensions


//------ COLOR SCHEME & STYLES----------\\

MppVerticalMenu.prototype.main_forecolor = 0x12E4A5;
MppVerticalMenu.prototype.main_bgcolor = 0xF2F2F2;
MppVerticalMenu.prototype.main_bordercolor = 0xBBBBBB;
MppVerticalMenu.prototype.main_glowcolor = 0x35BBF4;
MppVerticalMenu.prototype.main_lightcolor = 0xFFFFFF;
MppVerticalMenu.prototype.main_shadowcolor = 0x515151;
MppVerticalMenu.prototype.main_disabledcolor  = 0xF2F2F2;

MppVerticalMenu.prototype.main_shape = "rectangle";


//Styles--

MppVerticalMenu.prototype.boolshadow = false;
MppVerticalMenu.prototype.boolfore = false;
MppVerticalMenu.prototype.boolbg = true;
MppVerticalMenu.prototype.boolglow = true;
MppVerticalMenu.prototype.boolborder = true;
MppVerticalMenu.prototype.boollight = false;

//--

//--------------------------

//BOX PROEPERTY


//FOnt PROPERTIES DEFAULT
MppVerticalMenu.prototype.textcolor = 0x000000;
MppVerticalMenu.prototype.textdisabled = 0xAEAEAE;

MppVerticalMenu.prototype.textfont = "Arial";
MppVerticalMenu.prototype.textsize = 12;
MppVerticalMenu.prototype.embedfont = false;

//------Data -----//
MppVerticalMenu.prototype.menuCollection;

//------------------------------Public Methods ----------------------//



//-----------------------------private methods -----------------------//
MppVerticalMenu.prototype.createEmptyClips = function (){
	this.container_mc.createEmptyMovieClip("menu" + this.menuDepth + "_mc", this.menuDepth);
	this.menu_mc = eval(this.container_mc + ".menu" + this.menuDepth + "_mc");
	this.menu_mc.createEmptyMovieClip("mainmenu_mc", 1);
	this.menu_mc.createEmptyMovieClip("list_mc", 2);
	ths.menu_mc.createEmptyMovieClip("masklist_mc", 3);
}


MppVerticalMenu.prototype.paint = function (){
	this.drawMain();
}

MppVerticalMenu.prototype.drawMain = function(){
	main_btn =  new MppButtonAsset(this.menu_mc.mainmenu_mc, 1);
	main_btn.setLabel(this.main_label);
	
	this.setDefaultMenuStyle();
	this.setDefaultMenuFontStyle();
	this.setDefaultMenuColor();
}

MppVerticalMenu.prototype.setDefaultMenuStyle = function () {
	main_btn.foreStyle(this.boolfore);
	main_btn.borderStyle(this.boolborder);
	main_btn.shadowStyle(this.boolshadow);
	main_btn.lightStyle(this.boollight);
	main_btn.glowStyle(this.boolglow);
	main_btn.bgStyle(this.boolbg);
	
	main_btn.buttonShape(this.main_shape)
	
	main_btn.setSize(this.main_width, this.main_height);
}

MppVerticalMenu.prototype.setDefaultMenuColor = function () {
	main_btn.setForeColor(this.main_forecolor);
	main_btn.setBorderColor(this.main_bordercolor);
	main_btn.setGlowColor(this.main_glowcolor);
	main_btn.setShadowColor(this.main_shadowcolor);
	main_btn.setLightColor(this.main_lightcolor);
	main_btn.setBgColor(this.main_bgcolor);
	main_btn.setDisableColor(this.main_disabledcolor);
}

MppVerticalMenu.prototype.setDefaultMenuFontStyle = function () {
	main_btn.setLabelStyle( this.color, this.presscolor, this.overcolor, this.disablecolor , this.font ,this.size );
	main_btn.alignLabel(this.main_alignment);
	main_btn.setLabelWeight( this.bold, this.italic, this.underline);
	
}

MppVerticalMenu.prototype.drawPopUp = function (){
	
}