var GlobalSearchCoreUIUtil_Instance = 0;
function GlobalSearchCoreUIUtil() {
	SmartEquipCoreUtil_Instance++;
	if( SmartEquipCoreUtil_Instance > 1 ) {
	} else {
		SmartEquipCoreUtil.prototype.NAME = "SmartEquipCoreUtil";
	}
}
GlobalSearchCoreUIUtil.prototype.NAME;

GlobalSearchCoreUIUtil.prototype.buttonConfig = function (mppbtn){
	mppbtn.embedFonts ( "MyFont");
	mppbtn.setSize(80,20);
	mppbtn.buttonShape("rectangle");
	mppbtn.foreStyle(false);
	mppbtn.setLabelStyle (0x000000,  0x000000,  0x000000,  0xEEEEEE ,"Arial" , 12 );
	mppbtn.setGlowColor( 0xFF9900);
	
	mppbtn.onRollOver = function () {
		mppbtn.setBgColor( 0xFF9900 );
	}
	mppbtn.onReleaseOutside = function () {
		mppbtn.setBgColor ( 0xE1E1E1 );
	}
	mppbtn.onRollOut = function () {
		mppbtn.setBgColor ( 0xE1E1E1 );
	}
	mppbtn.onDragOut = function () {
		mppbtn.setBgColor ( 0xE1E1E1 );
	}
	 
}

GlobalSearchCoreUIUtil.prototype.radioConfig = function ( radio ) {
	radio.setInteractiveColor  ( 0xFF9900 , 0xDDDDDD , 0xCCCCCC )
	radio.embedFonts("MyFont");
	
}

GlobalSearchCoreUIUtil.prototype.textConfig = function ( mtext) {
	mtext.setWidth(170);
	mtext.visualize();
	 
	//mtext.setSkin (boxcolor, boxdisable, framec, shadowcolor, lightcolor, glowc) 
	mtext.setSkin (0xFFFFFF, 0xCCCCCC, 0x999999, 0xEEEEEE, 0xFFFFFF, 0xFF9900) ;
	//mtext.characterRestriction("0-9");
}

GlobalSearchCoreUIUtil.prototype.showToolTip = function ( texttip ) {
	 
	_level0.machineInfoLevel.globalTooltip.setText (texttip );
	_level0.machineInfoLevel.globalTooltip.showTip (texttip );
}

GlobalSearchCoreUIUtil.prototype.hideToolTip = function () {
	_level0.machineInfoLevel.globalTooltip.hideTip();
}

GlobalSearchCoreUIUtil.prototype.setLgSLabelProperties = function ( target_lbl ) {
	target_lbl.setWeight( true, false, false);
	target_lbl.setSize(100, 20);
	target_lbl.embedFonts ( "MyFont" );
	
}
GlobalSearchCoreUIUtil.prototype.setStaticLabelStyles = function ( target_lbl ) {
	target_lbl.embedFonts ( "MyFont" );
	target_lbl.setWeight( true, false, false);
}
GlobalSearchCoreUIUtil.prototype.setDynamicLabelStyles = function  ( target_lbl ) {
	target_lbl.embedFonts ( "MyFont" );
	target_lbl.setWeight( true, false, false);
	
	target_lbl.onDragOut = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onRollOut = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onPress = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onRollOver = function () {
		 SEGSWindowCoreUIUtil.showToolTip ( target_lbl.getLabel() );
	}
}
 

GlobalSearchCoreUIUtil.prototype.setLgDLabelProperties = function ( target_lbl) {
	target_lbl.setWeight(true, false, false);
	target_lbl.setSize(100, 20);
	target_lbl.embedFonts ( "MyFont" );

	target_lbl.onDragOut = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onRollOut = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onPress = function () {
		SEGSWindowCoreUIUtil.hideToolTip();
	}
	target_lbl.onRollOver = function () {
		 SEGSWindowCoreUIUtil.showToolTip ( target_lbl.getLabel() );
	}
}


GlobalSearchCoreUIUtil.prototype.productComboConfig  = function (combo) {
	combo.setWidth(100);
	combo.setHeight(20);
	combo.visualize();
	combo.addItem("None");
	combo.addItem("Product");
	combo.addItem("Manual");
	combo.addItem("Model");
	combo.addItem("Component");
	combo.addItem("Part Number");
	combo.addItem("Part Description");
	combo.addItem("Mfr Serial #");
}

GlobalSearchCoreUIUtil.prototype.conditionComboConfig = function(combo) {
	//trace("GlobalSearchCoreUIUtil.prototype.conditionComboConfig()")
	
	combo.setWidth(100);
	combo.setHeight(20);
	combo.visualize();
	combo.addItem("Contains");
	combo.addItem("Equals");
	combo.addItem("Not Equals");
	combo.addItem("Greater Than");
	combo.addItem("Less Than");
	combo.addItem("Begins With");
	combo.setSelectedItem(combo.getItem(0));
}

GlobalSearchCoreUIUtil.prototype.tabButtonConfig = function ( tab ) {
	 
	tab.tabButtons.onLoop = function ( target , index ) {
		target.setLabelStyle (0x000000,  0x000000,  0x000000,  0x000000 ,"Arial" , 10 );
		target.embedFonts ("MyFont");
		target.setDisableColor ( 0xFF9900);
	}
	tab.tabButtons.loopIn();
}

GlobalSearchCoreUIUtil.prototype.logicComboConfig = function (combo) {
	combo.setWidth(50);
	combo.setHeight(20);
	combo.visualize();
	combo.addItem("And");
	combo.addItem("Or");
	combo.setSelectedItem(combo.getItem(0));
}

GlobalSearchCoreUIUtil.prototype.gridStyles = function ( grid ) {
	grid.setGlowSkin ( 0xC47809, 0xC47809);
}
GlobalSearchCoreUIUtil.prototype.searchView;
GlobalSearchCoreUIUtil.prototype.windowHandler;
GlobalSearchCoreUIUtil.prototype.machineView;
GlobalSearchCoreUIUtil.prototype.structView;

var SEGSWindowCoreUIUtil = new GlobalSearchCoreUIUtil(); //SINGLETON INSTANCE

//GlobalSearchEvent 

var GSCoreEvent_Instance = 0;
function GSCoreEvent () {
	 
	GSCoreEvent_Instance++;
	if( GSCoreEvent_Instance > 1 ){
	} else {
		this.SEARCH_VIEW_CANCEL = "onCancelSearchView";
		this.SEARCH_VIEW_DATA_LOADED = "onSearchViewDataLoaded";
		this.CLOSE_WINDOW = "onCloseWindow";
	}
}
GSCoreEvent.prototype.CLOSE_WINDOW;
GSCoreEvent.prototype.SEARCH_VIEW_CANCEL;
GSCoreEvent.prototype.SEARCH_VIEW_DATA_LOADED;
var GlobalSearchCoreEvent = new GSCoreEvent ();
 

