#include "functions/DrawFunction.as"
#include "functions/vo/MppMenuItems.as"
#include "functions/MppAligner.as"
#include "MppButtonAsset.as"

function MppPopUp(container , newdepth){
	this.menuCollection = new ItemCollection();
	this.clipList = new ItemCollection();
	this.aligner = new MppAligner();
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.container_mc = container;
	this.depth = newdepth;
	this.initClips();
	
	var mpppop = this;
	this.onMouseDown = function () {
		
		if( mpppop.popup_mc._visible == false &&  mpppop.autopop == true && mpppop.overme == false) {
			mpppop.popup_mc._x = _xmouse;
			mpppop.popup_mc._y = _ymouse;
			 mpppop.popIn();
		} else if( mpppop.popup_mc._visible == true &&  mpppop.autopop == true && mpppop.overme == false) {
			 mpppop.fadeOut();
		}
	}
	Mouse.addListener(this);
	this.setAutoPop(false);
}
//-------TOOLS---------\\
MppPopUp.prototype.aligner;

//--------------------PHysical ENtities------------\\
MppPopUp.prototype.container_mc;
MppPopUp.prototype.popup_mc;
MppPopUp.prototype.list_mc;
MppPopUp.prototype.items_mc;
MppPopUp.prototype.depth;

MppPopUp.prototype.autopop = true;
MppPopUp.prototype.overme = false;

//----------Display ----------------------\\

//----------------FOnts

MppPopUp.prototype.font = "Arial";
MppPopUp.prototype.color = 0x404040;
MppPopUp.prototype.presscolor = 0x444444;
MppPopUp.prototype.overcolor = 0x706D65;
MppPopUp.prototype.disablecolor = 0xCCCCCC;
MppPopUp.prototype.size = 11;

MppPopUp.prototype.align = "center";

MppPopUp.prototype.bold =  false;
MppPopUp.prototype.underline = false;
MppPopUp.prototype.italic = false;
MppPopUp.prototype.item_alignment = "center";
//-----------Dimensions

MppPopUp.prototype.item_width = 75;
MppPopUp.prototype.item_height = 20;


//------ COLOR SCHEME & STYLES----------\\

MppPopUp.prototype.item_forecolor = 0xFFFFFF;
MppPopUp.prototype.item_bgcolor = 0xE1E1E1;
MppPopUp.prototype.item_bordercolor = 0x000000;
MppPopUp.prototype.item_glowcolor = 0x35BBF4;
MppPopUp.prototype.item_lightcolor = 0xFFFFFF;
MppPopUp.prototype.item_shadowcolor = 0x515151;
MppPopUp.prototype.item_disabledcolor  = 0xF2F2F2;

MppPopUp.prototype.item_shape = "rectangle";
//Styles--

MppPopUp.prototype.boolshadow = false;
MppPopUp.prototype.boolfore = false;
MppPopUp.prototype.boolbg = false;
MppPopUp.prototype.boolglow = true;
MppPopUp.prototype.boolborder = false;
MppPopUp.prototype.boollight = false;

//--
MppPopUp.prototype.spacing = 0;
MppPopUp.prototype.padding = 10;
MppPopUp.prototype.panelcolor = 0xFFFFFF;
MppPopUp.prototype.bordercolor = 0xD7D6D2;


MppPopUp.prototype.panelshape = "rectangle"; //ellipse


MppPopUp.prototype.hasborder = true;


//-------------Public Method -----------------\\


//------Data -----//
MppPopUp.prototype.menuCollection;
MppPopUp.prototype.clipList;
//------------------------------Public Methods ----------------------//

MppPopUp.prototype.addNewMenu = function (newname , newvalue , reference){
	var mppitem = new MppMenuItem ( newname , newvalue, reference);
	this.menuCollection.addNewItem(mppitem);
	this.paint();
}

MppPopUp.prototype.deleteMenuAt = function (index){
 	this.menuCollection.deleteItem(index);
}
MppPopUp.prototype.deleteMenu = function (menuname){
	for(var ctr = 0; ctr < this.menuCollection.getCollectionLength(); ctr++){
		var mppitem = new MppMenuItem ();
		mppitem = this.menuCollection.getItem(ctr);
		if(menuname == mppitem.getName()){
			this.menuCollection.deleteItem(ctr);
			break;
		}
	}
	this.paint();
}

MppPopUp.prototype.enableItemAt = function ( index, bool) {
	var mppbtn  = this.clipList.getItem(index);
	mppbtn.setEnable(bool);
}
MppPopUp.prototype.enableItem = function ( itemname, bool) {
	for(var ctr = 0; ctr < this.menuCollection.getCollectionLength(); ctr++){
		var mppitem = new MppMenuItem ();
		mppitem = this.menuCollection.getItem(ctr);
		if(itemname == mppitem.getName()){
			var mppbtn = this.clipList.getItem(ctr);
			mppbtn.setEnable(bool);
			break;
		}
	}
}

MppPopUp.prototype.getItemAsObjectAt = function ( index) {
	return this.clipList.getItem(index);
}
 
MppPopUp.prototype.getItemAsObject = function (itemname) {
	for(var ctr = 0; ctr < this.menuCollection.getCollectionLength(); ctr++){
		var mppitem = new MppMenuItem ();
		mppitem = this.menuCollection.getItem(ctr);
		if(itemname == mppitem.getName()){
			return this.clipList.getItem(ctr);
		}
	}
}

MppPopUp.prototype.setAutoPop = function ( popvalue) {
	this.autopop = popvalue;
	if(this.autopop == true) {
		this.fadeOut();
	}
}

MppPopUp.prototype.popIn = function () {
	var pop_mc = this.popup_mc;
	var mpppop = this;
	pop_mc._visible = true;
	mpppop.broadcastMessage("onPopIn");
	pop_mc.onEnterFrame = function () {
		if(pop_mc._alpha < 100){
			pop_mc._alpha += 10;
		} else {
			delete pop_mc.onEnterFrame;
		}
	}
	
}

MppPopUp.prototype.fadeOut = function () {
	var pop_mc = this.popup_mc;
	var mpppop = this;
	pop_mc.onEnterFrame = function () {
		if(pop_mc._alpha > 0){
			pop_mc._alpha -= 50;
		} else {
			mpppop.broadcastMessage("onFadeOut");
			pop_mc._visible = false;
			delete pop_mc.onEnterFrame;
		}
	}
}

//---------------------PANELS
MppPopUp.prototype.setDimension = function ( newx , newy) {
	this.popup_mc._x = newx;
	this.popup_mc._y = newy;
}

MppPopUp.prototype.setPadding = function ( newpadding ) {
	this.padding = newpadding;
	this.arrangeVertically();
}

MppPopUp.prototype.setSpacing = function ( newspacing ) {
	this.spacing = newspacing;
	this.arrangeVertically();
}

MppPopUp.prototype.setPanelColor = function ( newpanelcolor, newbordercolor ) {
	this.panelcolor = newpanelcolor;
	this.bordercolor = newbordercolor;
	this.paint();
}
MppPopUp.prototype.setShape = function ( newshape ) {
	this.panelshape = newshape;
	this.paint();
}

MppPopUp.prototype.setItemSize = function ( Width , Height){
	this.item_width = Width;
	this.item_height = Height;
	this.paint();
}


//--------------------LABELING
MppPopUp.prototype.setItemsLabelAlignment = function (newalign ) {
	this.item_alignment = newalign;
	this.resetMenuStyle();
}

MppPopUp.prototype.setItemsLabelWeight = function ( newbold, newitalic, newunderline) {
	this.bold = newbold;
	this.italic = newitalic;
	this.underline = newunderline;
	this.resetMenuStyle();
}
MppPopUp.prototype.setItemsLabelStyle = function ( newcolor, newpresscolor, newovercolor, newdiscolor, newfont ,newsize ){
	this.disablecolor = newdiscolor;
	this.size = newsize
	this.color = newcolor;
	this.font = newfont;
	this.presscolor = newpresscolor;
	this.overcolor = newovercolor;
	this.resetMenuStyle();
}

//-----------ITEM CONTAINER
MppPopUp.prototype.setItemAllColor = function ( newcolor) {
	this.item_forecolor = newcolor;
	this.item_shadowcolor = newcolor;
	this.item_bgcolor = newcolor;
	this.item_disabledcolor = newcolor;
	this.item_lightcolor = newcolor;
	this.item_glowcolor = newcolor;
	this.item_bordercolor = newcolor;
	this.resetMenuStyle();
}

MppPopUp.prototype.setItemColors = function (newfore, newbg, newborder,newglow, newshadow, newlight , newdisabled ) { 
	this.item_forecolor = newfore;
	this.item_bgcolor = newbg;
	this.item_bordercolor = newborder;
	this.item_glowcolor = newglow;
	
	this.item_disabledcolor = newdisabled;
	this.item_shadowcolor = newshadow;
	this.item_lightcolor = newlight;
	
	this.resetMenuStyle();
}

MppPopUp.prototype.setItemForeColor = function(newcolor){
	this.item_forecolor = newcolor;
	this.resetMenuStyle();
}
MppPopUp.prototype.setItemShadowColor = function(newcolor){
	this.item_shadowcolor = newcolor;
	this.resetMenuStyle();
}

MppPopUp.prototype.setItemBgColor = function(newcolor){
	this.item_bgcolor = newcolor;
	this.resetMenuStyle();
}
MppPopUp.prototype.setDisableColor = function(newcolor){
	this.item_disabledcolor = newcolor;
	
}
MppPopUp.prototype.setItemLightColor = function(newcolor){
	this.item_lightcolor = newcolor;
	this.resetMenuStyle();
}
MppPopUp.prototype.setItemBorderColor = function(newcolor){
	this.item_bordercolor = newcolor;
	this.resetMenuStyle();
}
MppPopUp.prototype.setItemGlowColor = function(newcolor){
	this.item_glowcolor = newcolor;
	this.resetMenuStyle();
}
MppPopUp.prototype.setItemsShape = function(newshape){ 
	trace("Call From You");
	this.item_shape = newshape;
	this.resetMenuStyle();
}

MppPopUp.prototype.setItemStyleOn = function (newfore, newbg, newborder,newglow, newshadow, newlight ) { 
	this.boolshadow = newshadow;
	this.boolfore = newfore;
	this.boolbg = newbg;
	this.boolglow = newglow;
	this.boolborder = newborder;
	this.boollight = newlight;
	trace("hellO");
	this.resetMenuStyle();
}

//-------------Private Method ------------------\\

MppPopUp.prototype.resetMenuStyle = function (){
	var ctr = 0;
	for(ctr = 0; ctr < this.clipList.getCollectionLength(); ctr++) {
		var mppbtn = this.clipList.getItem(ctr);
		this.manageItemAttachment(mppbtn);
	}
}
MppPopUp.prototype.manageItemAttachment = function (mppbtn) {
	//trace(mppbtn.getDisplay());
	this.addStyle(mppbtn);
	this.setButtonProperties(mppbtn);
	this.attachLabelStyle(mppbtn);
	this.attachLabelWeight(mppbtn);
	this.attachLabelAlignment(mppbtn);
	this.redrawItemContainer(mppbtn);
}

MppPopUp.prototype.redrawItemContainer = function (mppbtn) {
	mppbtn.setForeColor(this.item_forecolor);
	mppbtn.setBorderColor(this.item_bordercolor);
	mppbtn.setGlowColor(this.item_glowcolor);
	mppbtn.setShadowColor(this.item_shadowcolor);
	mppbtn.setLightColor(this.item_lightcolor);
	mppbtn.setBgColor(this.item_bgcolor);
	mppbtn.setDisableColor(this.item_disabledcolor);
}

MppPopUp.prototype.initClips = function (){
	this.container_mc.createEmptyMovieClip("mpppopup" + this.depth + "_mc", this.depth);
	this.popup_mc = eval(this.container_mc + ".mpppopup" + this.depth + "_mc");
	this.popup_mc.createEmptyMovieClip("shadow_mc", 1);
	this.popup_mc.createEmptyMovieClip("bg_mc", 2);
	this.popup_mc.createEmptyMovieClip("list_mc", 3);
	this.list_mc = this.popup_mc.list_mc;
}
MppPopUp.prototype.paint = function (){
	this.drawList();
}
MppPopUp.prototype.drawList = function(){
	this.removeList();
	
	for(var ctr = 0; ctr < this.menuCollection.getCollectionLength(); ctr++){
		var mppbtn = new MppButtonAsset(this.list_mc, ctr);
		var menuitem = this.menuCollection.getItem(ctr);
		
		this.manageItemAttachment(mppbtn);
		
		this.clipList.addNewItemAt(mppbtn, ctr);
		mppbtn.setLabel(menuitem.getValue());
		this.addEvents(mppbtn, menuitem);
	}
	this.clipList.traceList();
	this.arrangeVertically();
}
MppPopUp.prototype.arrangeVertically = function (){
	var mpp = this.clipList.getItem(0);
	mpp.setDimension(0,0);
	
	for(var ctr = 1; ctr < this.clipList.getCollectionLength(); ctr++){
		var mppbtn = this.clipList.getItem(ctr);
		if( ctr >= 1 ) {
			var prebtn = this.clipList.getItem(ctr - 1);	
			this.aligner.bottomOf(prebtn.getDisplay(), mppbtn.getDisplay());
			var mc = mppbtn.getDisplay();
			 mc._y += this.spacing;
		}
	}
	this.drawBg();
}


MppPopUp.prototype.drawBg = function (){
	var firstbtn = this.clipList.getItem(0);
	var lastbtn = this.clipList.getItem(this.clipList.getCollectionLength() - 1);
	var first_mc = firstbtn.getDisplay();
	var last_mc = lastbtn.getDisplay();
	
	var bgheight = first_mc._y + last_mc._y + last_mc._height + this.padding;
	var bgwidth = first_mc._width + this.padding;
	this.popup_mc.bg_mc.clear();
	this.popup_mc.shadow_mc.clear();
	if(this.panelshape == "rectangle") {
		drawBoxInPos(this.popup_mc.bg_mc, 0, 0 , bgwidth, bgheight, this.panelcolor, 100);
	} else  if(this.panelshape == "ellipse"){
		drawEllipseFill(this.popup_mc.bg_mc, 0, 0 , bgwidth, bgheight,  this.panelcolor, 100);
	}
	if(this.hasborder == true) {
		if(this.panelshape == "rectangle") {
			drawFrameInPos(this.popup_mc.bg_mc, 0, 0 , bgwidth, bgheight, this.bordercolor, 100);
		} else  if(this.panelshape == "ellipse"){
			drawEllipseBorder(this.popup_mc.bg_mc, 0, 0 , bgwidth, bgheight,  this.bordercolor, 100);
		}
	}
	
	this.aligner.center(this.popup_mc.bg_mc, this.list_mc);
}
MppPopUp.prototype.removeList = function (){
	this.clipList.onEmpty = function (target) {
		target.deleteMe();
	}
	this.clipList.emptyCollection();
}

MppPopUp.prototype.addEvents = function ( mppbutton , menuitem ){
	var popup = this;
	var Caller = menuitem.getFunction();
	mppbutton.onRelease = function () {
		popup.broadcastMessage("onReleaseItem" , menuitem );
		if(Caller != null) {
			Caller();
		} else {
			trace("No Function to Execute! This menu item is not functional after all");
		}
	}
	
	
	mppbutton.onRollOver = function () {
		popup.overme = true;
	}
	
	mppbutton.onRollOut = function () {
		popup.overme = false;
	}
	mppbutton.onReleaseOutside = function () {
		popup.overme = true;
	}
	mppbutton.onDragOut = function () {
		popup.overme = true;
	}
}


//-------------------------Item Label Weight Style Properties ------//

MppPopUp.prototype.addStyle = function ( mppbutton ){
	mppbutton.foreStyle(this.boolfore);
	mppbutton.borderStyle(this.boolborder);
	mppbutton.shadowStyle(this.boolshadow);
	mppbutton.lightStyle(this.boollight);
	mppbutton.glowStyle(this.boolglow);
	mppbutton.bgStyle(this.boolbg);
}
MppPopUp.prototype.attachLabelStyle = function(mppbutton) {
	mppbutton.setLabelStyle( this.color, this.presscolor, this.overcolor, this.disablecolor , this.font ,this.size );
}
MppPopUp.prototype.attachLabelAlignment = function ( mppbtn ) {
	mppbtn.alignLabel(this.item_alignment);
}

MppPopUp.prototype.attachLabelWeight = function (mppbtn) {
	mppbtn.setLabelWeight( this.bold, this.italic, this.underline);
}


MppPopUp.prototype.setButtonProperties = function ( mppbutton ) {
	mppbutton.buttonShape(this.item_shape);
	mppbutton.setSize( this.item_width , this.item_height);
}

