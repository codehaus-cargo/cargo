#include "functions/DrawFunction.as"
#include "MppButtonAsset.as"
#include "MppCanvas.as"
#include "functions/vo/MppMenuItems.as"

function MppTab (container, assignedDepth) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.container_mc = container;
	this.depth = assignedDepth;
	
	this.aligner = new MppAligner();
	this.tabButtons = new ItemCollection();
	this.tabContainers = new ItemCollection();
	this.tabNames = new ItemCollection();
	this.tabCanvas = new ItemCollection();
	
	this.createClips();
	this.paint();
	
}


MppTab.prototype.container_mc;
MppTab.prototype.fakecover_mc;
MppTab.prototype.depth;
MppTab.prototype.aligner;


MppTab.prototype.tab_mc;
MppTab.prototype.tabButtons;
MppTab.prototype.tabContainers;
MppTab.prototype.tabCanvas;
MppTab.prototype.tabNames;

MppTab.prototype.tab_width = 400;
MppTab.prototype.tab_height = 300;
MppTab.prototype.tab_spacing = 2;

MppTab.prototype.tab_button_width = 75;
MppTab.prototype.tab_button_height = 20;



MppTab.prototype.color_bg = 0xAF9F61;
MppTab.prototype.color_border = 0xAF9F61;
MppTab.prototype.color_selected = 0xE2DCC7;
MppTab.prototype.color_glow = 0xFF9900;

MppTab.prototype.color_label_disable = 0xFFFFFF;
MppTab.prototype.color_label_over = 0xFFFFFF;
MppTab.prototype.color_label_press = 0xFFFFFF;
MppTab.prototype.color_label_up = 0x000000;

MppTab.prototype.label_font = "Arial";
MppTab.prototype.label_size = 12;
//Public Methods--



MppTab.prototype.setTabColor = function  ( bgColor , borderColor, selectedColor, glowColor) {
	this.color_bg = bgColor;
	this.color_glow = glowColor;
	this.color_border = borderColor;
	this.color_selected =  selectedColor;
	var mtab = this;
	this.tabButtons.onLoop = function (target, index) {
		mtab.setCanvasSize(mtab.tabCanvas.getItem(index));
		mtab.setCanvasAppearance(mtab.tabCanvas.getItem(index));
		mtab.setButtonProperties(target);
	}
	this.tabButtons.loopIn();
	this.selectTab(0);
}

MppTab.prototype.setLabelColor = function ( upColor, overColor, pressColor, disableColor){
	this.color_label_up = upColor;
	this.color_label_over = overColor;
	this.color_label_press = pressColor;
	this.color_label_disable = disableColor;
	var mtab = this;
	this.tabButtons.onLoop = function (target, index) {
		mtab.setButtonProperties(target);
	}
	this.tabButtons.loopIn();
}

MppTab.prototype.setSize = function ( newWidth, newHeight) {
	this.tab_width = newWidth;
	this.tab_height = newHeight;
	var mtab = this;
	this.tabCanvas.onLoop = function (target, index) {
		mtab.setCanvasSize(target);
	}
	this.tabCanvas.loopIn();
}


MppTab.prototype.setTabSpacing = function ( spacing) {
	this.tab_spacing = spacing;
	this.alignTabButtons();
}
MppTab.prototype.getCanvas = function ( index ){
	return this.tabCanvas.getItem(index);
}

MppTab.prototype.setDimension = function (newx , newy) {
	this.tab_mc._x = newx;
	this.tab_mc._y = newy;
	
}

MppTab.prototype.setTabButtonSize = function (index, newWidth, newHeight  ) {
	var btn  = this.tabButtons.getItem(index);
	btn.setSize(newWidth,newHeight);
	this.alignTabButtons();
}


MppTab.prototype.addTab = function (  tabname , tabdepth ) {
	var mc = this.tab_mc.createEmptyMovieClip("tab" + tabdepth  + "_mc", tabdepth)
	this.fakecover_mc.removeMovieClip();
	this.fakecover_mc = this.tab_mc.createEmptyMovieClip("fake_mc" , tabdepth + 1); 
	this.tabNames.addNewItem(tabname);
	this.tabContainers.addNewItem(mc);
	this.paint();
	this.selectTab(0);
}

//Private Methods--
MppTab.prototype.createClips = function () {
	this.tab_mc = this.container_mc.createEmptyMovieClip("MppTab" + this.depth + "_mc", this.depth);
	this.tab_mc;
}

MppTab.prototype.paint = function () {
	var mtab = this;
	this.tabContainers.onLoop = function (target, index) {
		var btn = new MppButtonAsset(target , 1);
		var canvas = new MppCanvas(target, 2);
		mtab.setCanvasProperties(canvas);
		
		btn.setLabel(mtab.tabNames.getItem(index));
		mtab.setButtonProperties(btn);
		mtab.setButtonSize(btn);
		mtab.tabCanvas.addNewItemAt(canvas, index);
		mtab.tabButtons.addNewItemAt(btn , index);
	}
	this.tabContainers.loopIn();
	this.alignTabButtons();
	this.paintFake();
	this.addEvents();
}


MppTab.prototype.paintFake = function () {
	this.fakecover_mc.clear();
	drawBoxInPos(this.fakecover_mc,0,0,this.tab_width + 1, 3,this.color_border,100);
	this.fakecover_mc._y = this.tab_button_height - 2;
}

MppTab.prototype.setButtonProperties = function (mppbtn) {
	mppbtn.setCursor(false);
	mppbtn.foreStyle(false);
	mppbtn.borderStyle(false);
	mppbtn.setBgColor(this.color_bg);
	mppbtn.setGlowColor( this.color_glow);
	mppbtn.setDisableColor(this.color_selected);
	mppbtn.setLabelStyle(this.color_label_up, this.color_label_press, this.color_label_over, this.color_label_disable, this.label_font, this.label_size);
}


MppTab.prototype.setButtonSize = function  (mppbtn) {
	mppbtn.setSize(this.tab_button_width, this.tab_button_height);
}

MppTab.prototype.setCanvasProperties = function( canvas ) {
	this.setCanvasSize(canvas);
	canvas.setDimension(0, this.tab_button_height - 2);
	this.setCanvasAppearance(canvas);
	canvas.setVisibility(false);
}

MppTab.prototype.setCanvasAppearance = function (canvas){
	canvas.setCanvasColor(this.color_selected, this.color_border);
	canvas.setBgAlpha(100);
	canvas.setBorderAlpha(100);
}

MppTab.prototype.setCanvasSize = function ( canvas ) {
	canvas.setSize(this.tab_width, this.tab_height);
	this.paintFake();
}


MppTab.prototype.alignTabButtons = function () {
	var mtab = this;
	this.tabButtons.onLoop = function (target, index) {
		if(index != 0 ){
			var btn = mtab.tabButtons.getItem(index - 1);
			mtab.aligner.rightOf(btn.getDisplay(), target.getDisplay());
			target.getDisplay()._x += mtab.tab_spacing;												
		} else {
			target.setDimension(0,0);
		}
	}
	this.tabButtons.loopIn();
}


MppTab.prototype.addEvents = function (){
	var mtab = this;
	this.tabButtons.onLoop = function (target, index) {
		target.onRelease = function () {
		
			mtab.tabButtons.onLoop = function (newtarget, newindex) {
				newtarget.setEnable(true);
				mtab.tabCanvas.getItem(newindex).setVisibility(false);
			}
			mtab.tabButtons.loopIn();
			
			mtab.tabCanvas.getItem(index).setVisibility(true);
			target.setEnable(false);
			
			var _event = new Object();
			_event.target = target;
			_event.display = target.button_mc;
			_event.text = target.getLabel();
			_event.canvas = mtab.tabCanvas.getItem(index);
			_event.type = "change";
			mtab.broadcastMessage("change" , _event );
		}
	}
	this.tabButtons.loopIn();
	
}

MppTab.prototype.selectTab = function (cIndex){
	var mtab = this;
	this.tabButtons.onLoop = function (target, index) {
		target.setEnable(true);
		mtab.tabCanvas.getItem(index).setVisibility(false);
	}
	this.tabButtons.loopIn();
	this.tabButtons.getItem(cIndex).setEnable(false);
	this.tabCanvas.getItem(cIndex).setVisibility(true);
}