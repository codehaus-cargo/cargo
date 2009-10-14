 
function SESearchView ( parentCanvas ) {
	AsBroadCaster.initialize(this);
	this.addListener( this );
	this.parent = parentCanvas;
	this.drawComponents();
	SEGSWindowCoreUIUtil.searchView = this;
	this.searchStack = new Array();
}
SESearchView.prototype.parent;
//---------------------Search Interface ---------------------\\
 
SESearchView.prototype.ySpacing = 25;


SESearchView.prototype.proComboCollection ; 

SESearchView.prototype.proCombo1; 
SESearchView.prototype.proCombo2; 
SESearchView.prototype.proCombo3; 
SESearchView.prototype.proCombo4;  
SESearchView.prototype.proCombo5; 

 

///------condtion 
SESearchView.prototype.conditionComboCollection; 
SESearchView.prototype.conditionCmb1;
SESearchView.prototype.conditionCmb2;
SESearchView.prototype.conditionCmb3;
SESearchView.prototype.conditionCmb4;
SESearchView.prototype.conditionCmb5;
 
//-----logic


SESearchView.prototype.logicComboCollection;

SESearchView.prototype.logicCmb1;
SESearchView.prototype.logicCmb2;
SESearchView.prototype.logicCmb3;
SESearchView.prototype.logicCmb4;

SESearchView.prototype.searchTextCollection;
SESearchView.prototype.searchText1;
SESearchView.prototype.searchText2;
SESearchView.prototype.searchText3;
SESearchView.prototype.searchText4;
SESearchView.prototype.searchText5;
 

//Radion buttons
SESearchView.prototype.searchGroup;
SESearchView.prototype.uniqueRadio;
SESearchView.prototype.usedRadio;
 

SESearchView.prototype.btnCollection;
SESearchView.prototype.search_btn;
SESearchView.prototype.restore_btn;
SESearchView.prototype.clear_btn;
SESearchView.prototype.cancel_btn;
 
SESearchView.prototype.searchStack;
//RELATED SERVICES

SESearchView.prototype.xmlService;
 
SESearchView.prototype.onSearchViewDataLoaded;
 
 
//DRAW SEARCH VIEW COMPONENTS
SESearchView.prototype.drawComponents  = function () {
	
	//trace("SESearchView.prototype.drawComponents()")
	
	var me = this;
	//PRODUCT COMBO BOX
	this.proComboCollection = new ItemCollection ();
	this.proCombo1 = new MppComboBox ( this.parent.getContainer() , 5);
	this.proCombo2 = new MppComboBox ( this.parent.getContainer() , 4);
	this.proCombo3 = new MppComboBox ( this.parent.getContainer() , 3);
	this.proCombo4 = new MppComboBox ( this.parent.getContainer() , 2);
	this.proCombo5 = new MppComboBox ( this.parent.getContainer() , 1);
	
	this.proComboCollection.addNewItem(this.proCombo1);
	this.proComboCollection.addNewItem(this.proCombo2);
	this.proComboCollection.addNewItem(this.proCombo3);
	this.proComboCollection.addNewItem(this.proCombo4);
	this.proComboCollection.addNewItem(this.proCombo5);
	
	
	this.proComboCollection.onLoop = function (target, index) {
		SEGSWindowCoreUIUtil.productComboConfig(target);
		target.setDimension(110,  index *( me.ySpacing * 2) + me.ySpacing);
		if(index == 0 ) {
			target.setSelectedItem(target.getItem(1));
		} else {
			target.setSelectedItem(target.getItem(0));
		}
	}
	this.proComboCollection.loopIn();
	
	//CONDITION COMBOBOX
	this.conditionComboCollection = new ItemCollection ();

	this.conditionCmb1 = new MppComboBox ( this.parent.getContainer() , 10);
	this.conditionCmb2 = new MppComboBox ( this.parent.getContainer() , 9);
	this.conditionCmb3 = new MppComboBox ( this.parent.getContainer() , 8);
	this.conditionCmb4 = new MppComboBox ( this.parent.getContainer() , 7);
	this.conditionCmb5 = new MppComboBox ( this.parent.getContainer() , 6);

	this.conditionComboCollection.addNewItem(this.conditionCmb1);
	this.conditionComboCollection.addNewItem(this.conditionCmb2);
	this.conditionComboCollection.addNewItem(this.conditionCmb3);
	this.conditionComboCollection.addNewItem(this.conditionCmb4);
	this.conditionComboCollection.addNewItem(this.conditionCmb5);
	
	this.conditionComboCollection.onLoop = function (target, index) {
		SEGSWindowCoreUIUtil.conditionComboConfig(target);
		target.setDimension(250,  index *(me.ySpacing * 2) + me.ySpacing);
	}
	this.conditionComboCollection.loopIn();
	
	//LOGIC COMBO BOX
	this.logicComboCollection = new ItemCollection ();
	this.logicCmb1 = new MppComboBox ( this.parent.getContainer() , 14);
	this.logicCmb2 = new MppComboBox ( this.parent.getContainer() , 13);
	this.logicCmb3 = new MppComboBox ( this.parent.getContainer() , 12);
	this.logicCmb4 = new MppComboBox ( this.parent.getContainer() , 11);
	
	this.logicComboCollection.addNewItem(this.logicCmb1);
	this.logicComboCollection.addNewItem(this.logicCmb2);
	this.logicComboCollection.addNewItem(this.logicCmb3);
	this.logicComboCollection.addNewItem(this.logicCmb4);
	this.logicComboCollection.addNewItem(this.logicCmb5);
	
	this.logicComboCollection.onLoop = function (target, index) {
		SEGSWindowCoreUIUtil.logicComboConfig(target);
		target.setDimension(20,  (index + 1) * (me.ySpacing * 2) + me.ySpacing);
	}
	this.logicComboCollection.loopIn();
	
	//DRAW TEXT INPUT
	
	this.searchTextCollection = new ItemCollection();
	this.searchText1 = new MppText( this.parent.getContainer(), 15);
	this.searchText2 = new MppText( this.parent.getContainer(), 16);
	this.searchText3 = new MppText( this.parent.getContainer(), 17);
	this.searchText4 = new MppText( this.parent.getContainer(), 19);
	this.searchText5 = new MppText( this.parent.getContainer(), 20);
	
	this.searchTextCollection.addNewItem(this.searchText1);
	this.searchTextCollection.addNewItem(this.searchText2);
	this.searchTextCollection.addNewItem(this.searchText3);
	this.searchTextCollection.addNewItem(this.searchText4);
	this.searchTextCollection.addNewItem(this.searchText5);
	this.searchTextCollection.onLoop =  function (target, index) {
		SEGSWindowCoreUIUtil.textConfig(target);
		target.setDimension(390,  (index) *(me.ySpacing * 2) + me.ySpacing);
	}
	this.searchTextCollection.loopIn();
	
	//RADIO BUTTONS
	
	this.searchGroup = new MppRadioGroup();
	this.uniqueRadio = new MppRadioButton (this.parent.getContainer(), 0);
	this.usedRadio = new MppRadioButton (this.parent.getContainer(), -1);
	
	this.uniqueRadio.setDimension(465, 280);
	this.usedRadio.setDimension(345, 280);
	this.usedRadio.setLabel("Show Where Used");
	this.uniqueRadio.setLabel("Show Unique");
	this.usedRadio.setWidth(150);
	this.uniqueRadio.setWidth(150);
	this.searchGroup.addRadioButton(this.usedRadio);
	this.searchGroup.addRadioButton(this.uniqueRadio);
	this.usedRadio.setSelected(true);

	//PUSH BUTTONS
	
	
	//Buttons

	this.btnCollection = new ItemCollection();
	this.search_btn = new MppButtonAsset (this.parent.getContainer(), -2);
	this.restore_btn = new MppButtonAsset (this.parent.getContainer(), -3);
	this.clear_btn = new MppButtonAsset (this.parent.getContainer(), -4);
 	this.cancel_btn = new MppButtonAsset (this.parent.getContainer(), -5);

	this.btnCollection.addNewItem(this.search_btn);
	this.btnCollection.addNewItem(this.restore_btn);
	this.btnCollection.addNewItem(this.clear_btn);
	this.btnCollection.addNewItem(this.cancel_btn);

	this.search_btn.setLabel("Search");
	this.restore_btn.setLabel("Restore");
	this.clear_btn.setLabel( "Clear");
	this.cancel_btn.setLabel("Cancel");


	this.btnCollection.onLoop = function (target, index) {
		SEGSWindowCoreUIUtil.buttonConfig(target);
	}
	this.btnCollection.loopIn();
	this.search_btn.setDimension( 160, 320);
	this.restore_btn.setDimension(265, 320);
	this.clear_btn.setDimension(370, 320);
	this.cancel_btn.setDimension(475, 320);
	//ADD EVENT LISTENERs
	this.search_btn.onRelease = this.buttonsClick ;
	this.clear_btn.onRelease = this.buttonsClick;
	this.restore_btn.onRelease = this.buttonsClick;
	this.cancel_btn.onRelease = this.buttonsClick;
	
	SEGSWindowCoreUIUtil.radioConfig ( this.uniqueRadio );
	SEGSWindowCoreUIUtil.radioConfig ( this.usedRadio  );
}


SESearchView.prototype.buttonsClick = function ( _event  ) {
	
	switch ( _event.text ) {
			case "Search" :
				SEGSWindowCoreUIUtil.searchView.search(); 
			break;
			case "Restore" : 
				SEGSWindowCoreUIUtil.searchView.restoreOldSearch();
			break;
			case "Clear":
				SEGSWindowCoreUIUtil.searchView.clearTextField();
			break;
			case "Cancel":
				SEGSWindowCoreUIUtil.searchView.closeGSWindow();
			break;
	}
}

SESearchView.prototype.searchLoaded = function ( _event ) {
	//trace("SESearchView.prototype.searchLoaded()  _event.resultXML="+_event.resultXML)
	var _param = new Object();
	_param.type =  GlobalSearchCoreEvent.SEARCH_VIEW_DATA_LOADED;
	_param.resultXML = _event.resultXML;
	this.broadcastMessage( GlobalSearchCoreEvent.SEARCH_VIEW_DATA_LOADED , _param );
	 _level300.mouseNormal();
	 
	 this.onSearchViewDataLoaded(_param)
}
SESearchView.prototype.search = function () {
	trace("SESearchView.prototype.search()")
	 xmlService = new GlobalSearchLoader();
	 xmlService.onSearchLoaded = function ( _event ) {
		
		SEGSWindowCoreUIUtil.searchView.searchLoaded ( _event );
	 }
	 var param = new Object();
	 //this.searchText1.getTextValue().length 
	 if ( StringUtil.trim(  this.searchText1.getTextValue() ) != "" ){
 		param.Param1 =    StringUtil.trim(  this.searchText1.getTextValue() ) ;
	 }
	 if ( StringUtil.trim(  this.searchText2.getTextValue() ) != "" ){
		 param.Param2 =   StringUtil.trim(  this.searchText2.getTextValue() ) ;
	 }
	 if ( StringUtil.trim(  this.searchText3.getTextValue() ) != "" ){
		 param.Param3 =  StringUtil.trim(  this.searchText3.getTextValue() ) ;
	 }
	 if ( StringUtil.trim(  this.searchText4.getTextValue() ) != "" ){
		 param.Param4 =   StringUtil.trim(  this.searchText4.getTextValue() ) ;
	 }
	 if ( StringUtil.trim(  this.searchText5.getTextValue() ) != "" ){
		 param.Param5 =   StringUtil.trim(  this.searchText5.getTextValue() ) ;
	 }
	 
	 param.Qual1 =  this.qualEquivalent ( this.proCombo1.getSelectedData() );
	 param.Cond1 = this.condEquivalent ( this.conditionCmb1.getSelectedData() );
	 param.Bool1 = this.logicCmb1.getSelectedData();
	 param.Param1 = this.addPercent ( param.Param1,  this.conditionCmb1.getSelectedData() );
	 
	 param.Qual2 = this.qualEquivalent (  this.proCombo2.getSelectedData() );
	 param.Cond2 = this.condEquivalent ( this.conditionCmb2.getSelectedData());
	 param.Bool2 = this.logicCmb2.getSelectedData();
	 param.Param2 = this.addPercent ( param.Param2,  this.conditionCmb2.getSelectedData() );
	 
	 param.Qual3 = this.qualEquivalent (  this.proCombo3.getSelectedData() );
	 param.Cond3 = this.condEquivalent ( this.conditionCmb3.getSelectedData() );
	 param.Bool3 = this.logicCmb3.getSelectedData();
	 param.Param3 = this.addPercent ( param.Param3,  this.conditionCmb3.getSelectedData() );
	 
	 param.Qual4 = this.qualEquivalent (  this.proCombo4.getSelectedData() );
	 param.Cond4 = this.condEquivalent ( this.conditionCmb4.getSelectedData() );
	 param.Bool4 = this.logicCmb4.getSelectedData();
	 param.Param4 = this.addPercent ( param.Param4,  this.conditionCmb4.getSelectedData() );
	
	 param.Qual5 = this.qualEquivalent (  this.proCombo5.getSelectedData() );
	 param.Cond5 = this.condEquivalent ( this.conditionCmb5.getSelectedData());
	 param.Param5 = this.addPercent ( param.Param5 ,  this.conditionCmb5.getSelectedData() );
	
	if( this.uniqueRadio.isSelected() == true ) {
		 param.Option = "Unique";
	 } else if ( this.usedRadio.isSelected() == true ) {
		 param.Option = "All";
	 }
	 //COMMON CONFIGURATION
	// param.mfrgroup = _level0.mfrgroups;
	 param.region = _level0.region;
	 
	 this.store();
	_level0.machineInfoLevel.searchParam = param;
	 var targetURL =  _level0.baseURL + "servlet/RequestDirector?helper=GlobalSearch";
	 trace("URL: " + targetURL );
	 _level300.mouseWait();
	 xmlService.loadSearch( targetURL,  param );
	 //xmlService.loadSearch( "http://localhost/smartequipt/xml/global_search_response.php",  param );
}

SESearchView.prototype.addPercent = function ( param , cond ) {
	param = StringUtil.trim ( param );
	if( cond == "Contains" && param != "" ) {
		param = "%" + param;
	}
	return param;
}

SESearchView.prototype.closeGSWindow = function () {
	 unloadMovieNum(291);
	  var _event = new Object();
	 _event.target = this;
	 _event.type = GlobalSearchCoreEvent.SEARCH_VIEW_CANCEL;
	 this.broadcastMessage(  GlobalSearchCoreEvent.SEARCH_VIEW_CANCEL  , _event );
}

SESearchView.prototype.clearTextField = function () {
	 //this.store();
	 this.defaultData();
	 this.searchText1.setTextValue("");
	 this.searchText2.setTextValue("");
	 this.searchText3.setTextValue("");
	 this.searchText4.setTextValue("");
	 this.searchText5.setTextValue("");
}

SESearchView.prototype.store = function () {
	 var searchItem = new Object();
	 
	 searchItem.Param1 =  this.searchText1.getTextValue();
	 searchItem.Qual1 =  this.proCombo1.getSelectedData();
	 searchItem.Cond1 = this.conditionCmb1.getSelectedData();
	 searchItem.Bool1 = this.logicCmb1.getSelectedData();
	 
	 searchItem.Param2 =  this.searchText2.getTextValue();
	 searchItem.Qual2 =  this.proCombo2.getSelectedData();
	 searchItem.Cond2 = this.conditionCmb2.getSelectedData();
	 searchItem.Bool2 = this.logicCmb2.getSelectedData();
	 
	 searchItem.Param3 =  this.searchText3.getTextValue();
	 searchItem.Qual3 =  this.proCombo3.getSelectedData();
	 searchItem.Cond3 = this.conditionCmb3.getSelectedData();
	 searchItem.Bool3 = this.logicCmb3.getSelectedData();
	
	 searchItem.Param4 =  this.searchText4.getTextValue();
	 searchItem.Qual4 =  this.proCombo4.getSelectedData();
	 searchItem.Cond4 = this.conditionCmb4.getSelectedData();
	 searchItem.Bool4 = this.logicCmb4.getSelectedData();
	 
	 searchItem.Param5 =  this.searchText2.getTextValue();
	 searchItem.Qual5 =  this.proCombo5.getSelectedData();
	 searchItem.Cond5 = this.conditionCmb5.getSelectedData();
	 
	 
	 this.searchStack.push( searchItem );
	 _level0.searchStack = this.searchStack;
}

SESearchView.prototype.defaultData = function () {
	 
	this.proCombo1.setSelectedItem( "Product" );
	this.conditionCmb1.setSelectedItem ( "Contains");
	this.logicCmb1.setSelectedItem ( "And" );
	
	this.proCombo2.setSelectedItem( "None" );
	this.conditionCmb2.setSelectedItem ( "Contains");
	this.logicCmb2.setSelectedItem ( "And" );

	this.proCombo3.setSelectedItem( "None" );
	this.conditionCmb3.setSelectedItem ( "Contains");
	this.logicCmb3.setSelectedItem ( "And" );

	this.proCombo4.setSelectedItem( "None" );
	this.conditionCmb4.setSelectedItem ( "Contains");
	this.logicCmb4.setSelectedItem ( "And" );

	this.proCombo5.setSelectedItem( "None" );
	this.conditionCmb5.setSelectedItem ( "Contains");
}

/*Begin
 * Additional code added by PHillip John Ruiz Ardona ( Jopi )
 * this will store web search for the new globalsearch component
 * Modified Date: Aug 24 2007
 */

SESearchView.prototype.restoreWebSearch = function () {
	var searchItem =  new Object ();
 	if( _level0.WebSearchQuery.type == "Part" ) {
		searchItem.Qual1 = "Part Number";
	} else {
 		searchItem.Qual1 =_level0.WebSearchQuery.type ;
	}
	searchItem.Cond1 =_level0.WebSearchQuery.cond  ;
	searchItem.Param1 = _level0.WebSearchQuery.param ;
	searchItem.Bool1 = "And";
	this.searchStack.push( searchItem ); 
	_level0.searchStack = this.searchStack;
	//	_level0.WebSearchQuery.flag  
	this.restoreOldSearch();
	
}

/*END
 * Additional code added by PHillip John Ruiz Ardona ( Jopi )
 * this will store web search for the new globalsearch component
 * Modified Date: Aug 24 2007
 */

SESearchView.prototype.restoreOldSearch = function () {
	this.searchStack = _level0.searchStack ;
	if( this.searchStack.length  > 0 ){
		var searchItem = this.searchStack[ this.searchStack.length - 1 ];
		this.searchText1.setTextValue( searchItem.Param1 );
		this.proCombo1.setSelectedItem(  searchItem.Qual1 );
		this.conditionCmb1.setSelectedItem ( searchItem.Cond1 );
		this.logicCmb1.setSelectedItem ( searchItem.Bool1 );
		
		if( searchItem.Param2 != undefined ) {
			this.searchText2.setTextValue( searchItem.Param2 );
			this.proCombo2.setSelectedItem(   searchItem.Qual2 );
			this.conditionCmb2.setSelectedItem ( searchItem.Cond2 );
			this.logicCmb2.setSelectedItem ( searchItem.Bool2 );
		}
		
		
		if( searchItem.Param3 != undefined ) {
			this.searchText3.setTextValue(  searchItem.Param3  );
			this.proCombo3.setSelectedItem(   searchItem.Qual3  );
			this.conditionCmb3.setSelectedItem ( searchItem.Cond3 );
			this.logicCmb3.setSelectedItem ( searchItem.Bool3 );
		}
		if( searchItem.Param4 != undefined ) {
			this.searchText4.setTextValue( searchItem.Param4 );
			this.proCombo4.setSelectedItem( searchItem.Qual4 );
			this.conditionCmb4.setSelectedItem ( searchItem.Cond4 );
			this.logicCmb4.setSelectedItem ( searchItem.Bool4 );
		}
		if( searchItem.Param5 != undefined ) {
			this.searchText5.setTextValue( searchItem.Param5 );
			this.proCombo5.setSelectedItem(   searchItem.Qual5   );
			this.conditionCmb5.setSelectedItem ( searchItem.Cond5 );
		}
	}
}

SESearchView.prototype.qualEquivalent  = function ( string ) {
	var resultString = "";
	if( string == "Product" ){
		resultString = "pg"
	} else if ( string == "None" ) {
		resultString = "None"
	} else if ( string == "Manual" ) {
		resultString = "Manual"
	}  else if ( string == "Model" ) {
		resultString = "Model"
	}  else if ( string == "Component" ) {
		resultString = "Component"
	}  else if ( string == "Part Number" ) {
		resultString = "Part"
	}  else if ( string == "Part Description" ) {
		resultString = "Description"
	}  else if ( string == "Mfr Serial #" ) {
		resultString = "Serial"
	} 
	return resultString;
}

SESearchView.prototype.condEquivalent = function ( string ) {
	var resultString = "";
	if( string == "Begins With" ) {
		resultString = "Begins With";
	} else if( string == "Equals" ) {
		resultString = "Equals";
	} else if( string == "Not Equals" ) {
		resultString = "NotEquals";
	} else if( string == "Greater Than" ) {
		resultString = "Greater";
	} else if( string == "Less Than" ) {
		resultString = "Less";
	} else if( string == "Contains" ) {
		resultString = "Begins With";
	}
	return resultString;
}
