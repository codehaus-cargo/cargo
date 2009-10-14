function SESQueryView ( parentCanvas ) {
	SEGSWindowCoreUIUtil.structView = this;
	this.parent = parentCanvas;
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.initData();
	
	this.buildComponents();
	this.init();
	this.setButtonsListeners();
	this.setLoaderListener();
}



SESQueryView.prototype.parent;

//DATA 
SESQueryView.prototype.partnumber;
SESQueryView.prototype.loader;

//COMPONENTS
SESQueryView.prototype.siSearchCvs  ;
SESQueryView.prototype.siLgCvs  ;
SESQueryView.prototype.siGridCvs ;
SESQueryView.prototype.siBtnCvs ;

SESQueryView.prototype.siPart_lbl ;
SESQueryView.prototype.siPart_txt ;
SESQueryView.prototype.siBill_rbtn;
SESQueryView.prototype.siUsed_rbtn ;
SESQueryView.prototype.siSearch_btn ;
SESQueryView.prototype.siRGroup ;

SESQueryView.prototype.structure_grid;
SESQueryView.prototype.siResult_lbl;

 
SESQueryView.prototype.siPrint_btn ;
SESQueryView.prototype.siAddSelect_btn ;
SESQueryView.prototype.siCancel_btn;
SESQueryView.prototype.siGridNaviCvs;

SESQueryView.prototype.siPrev_btn ;
SESQueryView.prototype.siNext_btn;
SESQueryView.prototype.siPage_lbl;

SESQueryView.prototype.gridSelectedItem;

//LEGEND S - Static  D - Dynamic
//

SESQueryView.prototype.siLgSPart_lbl ;
SESQueryView.prototype.siLgDPart_lbl;
SESQueryView.prototype.siLgSDesc_lbl ;
SESQueryView.prototype.siLgDDesc_lbl ;

SESQueryView.prototype.init = function () {
	this.siGridNaviCvs.setVisibility(false);
	this.siLgCvs.setVisibility(false);
	this.siResult_lbl.setVisibility(false);
	this.siPrint_btn.setEnable(false);
	this.siAddSelect_btn.setEnable(false);
	this.siBtnCvs.setVisibility(false);
}

SESQueryView.prototype.setLoaderListener = function () {
	 
	this.loader = new SESQueryLoader();
	this.loader.onLoadSingleLevelBill = this.loaderHandler;
	this.loader.onLoadSingleLevelWhereUsed  = this.loaderHandler;
}


SESQueryView.prototype.setButtonsListeners  = function () {
	 
	this.siPrint_btn.onRelease = this.buttonsClicked;
	this.siAddSelect_btn.onRelease = this.buttonsClicked;
	this.siCancel_btn.onRelease = this.buttonsClicked;
	this.siSearch_btn.onRelease = this.buttonsClicked;	
	
	this.siNext_btn.onRelease = this.buttonsClicked;	
	this.siPrev_btn.onRelease = this.buttonsClicked;	
}


//Helpers:  SingleLevelBill | SingleLevelWhereUsed
//EVENTS: SQueryLoaderEvent.LOAD_SINGLE_LEVEL_BILL | SQueryLoaderEvent.LOAD_SINGLE_LEVEL_WHERE_USED

SESQueryView.prototype.search = function () {
	 
	this.partnumber = this.siPart_txt.getTextValue();
	this.partnumber = StringUtil.trim( this.partnumber);
	var targetURL = "";
	
	var param = new Object();
	param.curDataSourceID = _level0.curDataSourceID;
	param.curMfrID = _level0.curMfrID;
	param.partnumber = this.partnumber;
	_level300.mouseWait();
	if( this.siBill_rbtn.isSelected() == true){
		// targetURL = "http://localhost/smartequipt/xml/sq/single_level_bill.php";
		targetURL = SEVars.REQUEST_DIRECTOR + "SingleLevelBill";
		this.loader.loadSingleLevelBill ( targetURL , param );
		//this.loader.loadSingleLevelBill("datatest/StructuredQuery/response_SQSingleLevelBill.txt", _param );	
	} else if( this.siUsed_rbtn.isSelected() == true){
		//targetURL = "http://localhost/smartequipt/xml/sq/single_level_where_used.php";
		targetURL = SEVars.REQUEST_DIRECTOR + "SingleLevelWhereUsed";
		this.loader.loadSingleLevelWhereUsed ( targetURL , param );
		//this.loader.loadSingleLevelWhereUsed("datatest/StructuredQuery/response_SQSingleLevelWhereUsed.txt", _param );	
	}   
	if( partnumber == ""){
		siUrl = siUrlNoResponse;
	}
}

SESQueryView.prototype.loaderHandler = function ( _event ) {
	_level300.mouseNormal();
	SEGSWindowCoreUIUtil.structView.loadEventType = _event.type;
	trace("Type: " + SEGSWindowCoreUIUtil.structView.loadEventType);
	SEGSWindowCoreUIUtil.structView.siGenerateLoadedData ( _event.resultXML );
	
}

SESQueryView.prototype.gridCellPress = function ( _event ) {
	//SEGSWindowCoreUIUtil.structView.loadEventType
	SEGSWindowCoreUIUtil.structView.gridCellPressHandler ( _event );
}

SESQueryView.prototype.gridCellPressHandler = function ( _event ) {
	this.gridSelectedItem = new Object();
	this.gridSelectedItem.pnum = _event.row[0];
	this.gridSelectedItem.desc = _event.row[1];
	this.gridSelectedItem.qty = _event.row[2];
	trace(" PNUM: " +  this.gridSelectedItem.pnum + " " + this.gridSelectedItem.qty );
	this.siAddSelect_btn.setEnable ( true );
}

SESQueryView.prototype.addSelectedParts = function () {
	trace("SESQueryView.prototype.addToSelection()   this.gridSelectedItem.pnum="+this.gridSelectedItem.pnum)
	trace("SESQueryView.prototype.addToSelection()   this.gridSelectedItem.qty="+this.gridSelectedItem.qty)
	trace("SESQueryView.prototype.addToSelection()   this.gridSelectedItem.desc="+this.gridSelectedItem.desc)
	
	//trace("add SELECTED PARTS");
	this.siAddSelect_btn.setEnable ( false );
	this.siAddSelect_btn.setBgColor ( 0xE1E1E1 );
	this.structure_grid.refreshGrid();
	_level0.machineInfoLevel.selectedEntries.push( this.gridSelectedItem );
	
	
	/*
	if ( _level0.selectedPartsLevel == undefined  ||  _level0.selectedPartsLevel == null) {
		_level0.loadTabs ("tabs2web.swf", 200); 
	} else {
		_level0.selectedPartsLevel.addSelectedParts(); 
	}
	*/
	
	_level0.searchResultsOrderPartsClicked=true;
	_level0.mpid = this.gridSelectedItem.pnum;
	_level0.partExternalQuantity = this.gridSelectedItem.qty;
	_level0.partdesc = this.gridSelectedItem.desc;
	
	_level0.globalSearchAddToSelection();
}

SESQueryView.prototype.buttonsClicked = function  ( _event ) {
	 var sqview = SEGSWindowCoreUIUtil.structView;
	switch ( _event.target.button_mc ) {
		 case SEGSWindowCoreUIUtil.structView.siPrint_btn.button_mc:
		 //PRINT
		 	_level0.machineInfoLevel.siItemCol = SEGSWindowCoreUIUtil.structView.gridDataProvider;
			_level0.machineInfoLevel.siCurResultType = SEGSWindowCoreUIUtil.structView.returnType;
			_level0.machineInfoLevel.siCurPartNum = SEGSWindowCoreUIUtil.structView.curPartNum;
			_level0.machineInfoLevel.siCurPartDesc = SEGSWindowCoreUIUtil.structView.curPartDesc;
			trace("CUR: " +  SEGSWindowCoreUIUtil.structView.curPartNum );
			//loadMovieNum("printStructuredQuery.swf",1129);
		 	//print version 2
			loadMovieNum(_level0.staticWebURL+"printStructuredQuery.swf",1129);
		 break;
		 case SEGSWindowCoreUIUtil.structView.siAddSelect_btn.button_mc:
		 //ADD TO SELECTION
		 trace("ADD TO SELECTED:");
		 	SEGSWindowCoreUIUtil.structView.addSelectedParts ();
		 	//SEGSWindowCoreUIUtil.structView.siAddSelect_btn.setEnable ( true );
		 	//SEGSWindowCoreUIUtil.structView.addSelectedParts();
		 break;
		 case SEGSWindowCoreUIUtil.structView.siCancel_btn.button_mc:
		 //Cancel
		 	var _event = new Object();
			_event.type = GlobalSearchCoreEvent.CLOSE_WINDOW ;
			_event.target = SEGSWindowCoreUIUtil.structView;
		 	SEGSWindowCoreUIUtil.structView.broadcastMessage( GlobalSearchCoreEvent.CLOSE_WINDOW , _event );
		 break;
		 case SEGSWindowCoreUIUtil.structView.siSearch_btn.button_mc:
		 //SEARCH
		 	 SEGSWindowCoreUIUtil.structView.search();
		 break;
		 
		 case SEGSWindowCoreUIUtil.structView.siNext_btn.button_mc:
		 //Next
		 
		 	  SEGSWindowCoreUIUtil.structView.siDisplayNext();
		 break;
		 
		  case SEGSWindowCoreUIUtil.structView.siPrev_btn.button_mc:
		 //Prev
		 	 SEGSWindowCoreUIUtil.structView.siDisplayPrev();
		 break;
	}
}




//DATA RALATED VARS
SESQueryView.prototype.gridDataProvider;
SESQueryView.prototype.returnType;
SESQueryView.prototype.curPartNum;
SESQueryView.prototype.curPartDesc;
SESQueryView.prototype.loadEventType;
SESQueryView.prototype.siDataGroup  ;
SESQueryView.prototype.siPageLblCol ;

SESQueryView.prototype.maxperpage;
SESQueryView.prototype.pageCtr;
SESQueryView.prototype.grid_spliter;


SESQueryView.prototype.initData = function () {
	this.grid_spliter = "&jopiruizen&";
	this.maxperpage = 10;
	this.pageCtr = 0;
}


SESQueryView.prototype.siGenerateLoadedData = function ( resultXML ){
	resultXML.ignoreWhite = true;
	_level0.machineInfoLevel.sqxml = resultXML;
	
	this.gridDataProvider = new ItemCollection();
	this.gridDataProvider.emptyCollection();
	this.returnType = resultXML.firstChild.attributes.returntype;
	this.curPartNum =  resultXML.firstChild.attributes.partnumber;
	this.curPartDesc =  resultXML.firstChild.attributes.partdesc;
	//trace(this.curPartNum)
	var itemXml = new XML();
	itemXml = resultXML.firstChild.firstChild;
	while( itemXml != null){
		var itemObj = new Object();
		itemObj.itemNumber = itemXml.firstChild.firstChild.nodeValue;
		itemObj.desccription = itemXml.firstChild.nextSibling.firstChild.nodeValue;
		if( this.loadEventType == SQueryLoaderEvent.LOAD_SINGLE_LEVEL_BILL){
			itemObj.UM = itemXml.firstChild.nextSibling.nextSibling.firstChild.nodeValue;
			itemObj.quantity= itemXml.firstChild.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
		}
		this.gridDataProvider.addNewItem(itemObj);
		itemXml = itemXml.nextSibling;
		//trace("Item#:" + itemObj.itemNumber + " Desc: " + itemObj.desccription + " quantity: " + itemObj.quantity + " UM: " + itemObj.UM);
	}
	 
	this.siDisplayData();
}

SESQueryView.prototype.siDisplayData = function( ){
	this.siLgCvs.setVisibility(true);
	this.siResult_lbl.setVisibility(false);
	this.structure_grid.deleteAllColumns();
	this.siPrint_btn.setEnable(true);
	this.siAddSelect_btn.setEnable(false);
	if( this.returnType != "NoResult"){
		if( this.loadEventType ==   SQueryLoaderEvent.LOAD_SINGLE_LEVEL_BILL ){
			this.siDisplayBill();
		}else if( this.loadEventType  ==   SQueryLoaderEvent.LOAD_SINGLE_LEVEL_WHERE_USED){
			this.siDisplayWhereUsed();
		}  
		this.siGroupByMax ();
		this.siDisplayFirstMax();
	} else {
		this.siDisplayNoResult();
	}
}
SESQueryView.prototype.siDisplayBill = function () {
	this.siResult_lbl.setVisibility(false);
	this.siLgDPart_lbl.setLabel(this.curPartNum);
	this.siLgDDesc_lbl.setLabel(this.curPartDesc);
	this.structure_grid.addColumn("Component Item", 150);
	this.structure_grid.addColumn("Component Description", 200);
	this.structure_grid.addColumn("Quantity", 50);
	this.structure_grid.addColumn("U/M", 10);
}
SESQueryView.prototype.siDisplayWhereUsed = function (){
	this.siResult_lbl.setVisibility(false);
	this.siLgDPart_lbl.setLabel(this.curPartNum);
	this.siLgDDesc_lbl.setLabel(this.curPartDesc);
	this.structure_grid.addColumn("Component Item", 150);
	this.structure_grid.addColumn("Component Description", 200);
}
SESQueryView.prototype.siDisplayNoResult = function () {
	this.siBtnCvs.setVisibility(false);
	this.siGridNaviCvs.setVisibility(false);
	this.siResult_lbl.setVisibility(true);
	this.siPrint_btn.setEnable(false);
	this.siAddSelect_btn.setEnable(false);
	this.siLgCvs.setVisibility(false);
}
SESQueryView.prototype.siGroupByMax  = function () {
	this.siDataGroup =  new ItemCollection();
	this.siPageLblCol = new ItemCollection();
	var ctr = 0;
	var newGroup  = new ItemCollection();
	var lastMax;
	for( ctr = 0; ctr < this.gridDataProvider.getCollectionLength(); ctr++){
		newGroup.addNewItem(this.gridDataProvider.getItem(ctr));
		if((ctr + 1 ) % this.maxPerPage == 0){
			this.siDataGroup.addNewItem(newGroup);
			newGroup = new ItemCollection();
			var pageObj = new Object();
			pageObj.firstIndex = (ctr - this.maxPerPage ) + 2;
			pageObj.lastIndex = ctr + 1;
			this.siPageLblCol.addNewItem(pageObj);
			lastMax = ctr + 2;
		}  else if ( (ctr + 1) == this.gridDataProvider.getCollectionLength()){
			this.siDataGroup.addNewItem(newGroup);
			var pageObj = new Object();
			pageObj.firstIndex = lastMax;
			pageObj.lastIndex = this.gridDataProvider.getCollectionLength();
			this.siPageLblCol.addNewItem(pageObj);
		}
	}
}
SESQueryView.prototype.siDisplayFirstMax = function  () {
	//trace("DISPLAY FIRST TEN:");
	this.siBtnCvs.setVisibility(true);
	var ctr = 0;
	this.pageCtr = 0;
	this.siNext_btn.setEnable(true);
	this.siPrev_btn.setEnable(false);
	if( this.gridDataProvider.getCollectionLength() > (this.maxPerPage - 1) ){
		this.siGridNaviCvs.setVisibility(true);
		this.siDisplayByMax( this.pageCtr );
	} else {
		this.siGridNaviCvs.setVisibility(false);
		for( ctr = 0; ctr < this.gridDataProvider.getCollectionLength(); ctr++){
			var itemObj = this.gridDataProvider.getItem(ctr);
			//trace(ctr + ": Item#:" + itemObj.itemNumber + " Desc: " + itemObj.desccription + " quantity: " + itemObj.quantity + " UM: " + itemObj.UM);
			this.siDisplayItemObj( itemObj);
		}
		this.structure_grid.refreshGrid();
	}
}
SESQueryView.prototype.siDisplayByMax = function( current  ) {
	//trace("CURRENT: " + current);
	this.structure_grid.deleteAllRows();
	var pageObj = this.siPageLblCol.getItem( current );
	this.siPage_lbl.setLabel( pageObj.firstIndex + " - " + pageObj.lastIndex + " of " + this.gridDataProvider.getCollectionLength() );
	var icol = this.siDataGroup.getItem( current );
	var ctr = 0;
	for( ctr = 0; ctr < icol.getCollectionLength(); ctr++){
		var itemObj= icol.getItem(ctr);
		this.siDisplayItemObj( itemObj);
	}
	this.structure_grid.refreshGrid();
}
SESQueryView.prototype.siDisplayItemObj  = function ( itemObj ) {
	//trace("SESQueryView.prototype.siDisplayItemObj()")
	if( this.loadEventType == SQueryLoaderEvent.LOAD_SINGLE_LEVEL_BILL ){
		this.structure_grid.sqlInsert("Component Item , Component Description, Quantity, U/M", itemObj.itemNumber + this.grid_spliter +  itemObj.desccription + this.grid_spliter +  itemObj.quantity + this.grid_spliter + itemObj.UM);
	} else {
		this.structure_grid.sqlInsert("Component Item , Component Description", itemObj.itemNumber + this.grid_spliter +  itemObj.desccription);
	}
}
SESQueryView.prototype.siDisplayNext = function () {
	//trace("Page Ctr: " + siPageCtr + " GroupLen: " + siDataGroup.getCollectionLength());
	this.pageCtr++;
	this.siPrev_btn.setEnable(true);
	if( this.pageCtr < this.siDataGroup.getCollectionLength()){
		this.siDisplayByMax(this.pageCtr);
		if( this.pageCtr >=  this.siDataGroup.getCollectionLength() - 1) {
			this.siNext_btn.setEnable(false);
		}
	} else { 
		this.siNext_btn.setEnable(false);
		this.pageCtr = this.siDataGroup.getCollectionLength();
	}
}
SESQueryView.prototype.siDisplayPrev = function () {
	this.pageCtr--;
	this.siNext_btn.setEnable(true);
	if( this.pageCtr < 0 ) {
		this.pageCtr = 0;
		this.siPrev_btn.setEnable(false);
	} else {
		if(this.pageCtr <= 0 ) {
			this.siPrev_btn.setEnable(false);
		}
		this.siDisplayByMax(this.pageCtr);
	}
}




//*************************BUILD COMPONENTS*************************************************\\
//*************************BUILD COMPONENTS*************************************************\\
//*************************BUILD COMPONENTS*************************************************\\
//*************************BUILD COMPONENTS*************************************************\\
//*************************BUILD COMPONENTS*************************************************\\
//*************************BUILD COMPONENTS*************************************************\\

SESQueryView.prototype.buildComponents  = function () {
	
	this.siSearchCvs = new MppCanvas( this.parent.getContainer(), 1);
	this.siLgCvs = new MppCanvas( this.parent.getContainer() ,2 );
	this.siGridCvs = new MppCanvas( this.parent.getContainer(), 3);
	this.siBtnCvs = new MppCanvas ( this.parent.getContainer() , 4);

	this.siSearchCvs.setDimension(0, 20);
	this.siLgCvs.setDimension(0, 50 );
	this.siGridCvs.setDimension(0, 75);
	this.siBtnCvs.setDimension( 0, 360);

	this.siSearchCvs.setSize(570, 30);
	this.siLgCvs.setSize(570, 20);
	this.siGridCvs.setSize(570, 300);
	this.siBtnCvs.setSize(570, 30);

//---------SEARcH COMPONENTS

	this.siPart_lbl = new MppTextLabel (this.siSearchCvs.getContainer(), 1);
	this.siPart_txt = new MppText ( this.siSearchCvs.getContainer(), 2);
	this.siBill_rbtn = new MppRadioButton (this.siSearchCvs.getContainer(), 3);
	this.siUsed_rbtn = new MppRadioButton (this.siSearchCvs.getContainer(), 4);
	this.siSearch_btn = new MppButtonAsset( this.siSearchCvs.getContainer(), 5);
	this.siRGroup = new MppRadioGroup();

	
	 
	this.siRGroup.addRadioButton(this.siBill_rbtn);
	this.siRGroup.addRadioButton(this.siUsed_rbtn);

	this.siPart_txt.visualize();
	this.siPart_txt.setDimension(100,0)
	this.siPart_lbl.setSize(100,20);
	this.siPart_lbl.setDimension(15,0);
	this.siPart_lbl.setLabel("Part Number:");
	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.siPart_lbl );
	
	this.siBill_rbtn.setLabel("Single Level Bill");
	this.siUsed_rbtn.setLabel("Single Level Where Used");
	this.siBill_rbtn.setWidth(150);
	this.siUsed_rbtn.setWidth(160);
	this.siBill_rbtn.setDimension(205,0);
	this.siUsed_rbtn.setDimension(315,0);

	this.siBill_rbtn.setSelected( true );
	this.siSearch_btn.setDimension(485, 0);
	this.siSearch_btn.setLabel("Search");
	SEGSWindowCoreUIUtil.buttonConfig(this.siSearch_btn);
	
	SEGSWindowCoreUIUtil.radioConfig ( this.siBill_rbtn );
	SEGSWindowCoreUIUtil.radioConfig ( this.siUsed_rbtn  );

//LABEL COMPONENTS

//LEGEND S - Static  D - Dynamic
//

	this.siLgSPart_lbl = new MppTextLabel ( this.siLgCvs.getContainer(), 1);
	this.siLgDPart_lbl = new MppTextLabel ( this.siLgCvs.getContainer(), 2);
	this.siLgSDesc_lbl = new MppTextLabel ( this.siLgCvs.getContainer(), 3);
	this.siLgDDesc_lbl = new MppTextLabel ( this.siLgCvs.getContainer(), 4);

	this.siLgSPart_lbl.setLabel("Part Number:");
	this.siLgDPart_lbl.setLabel("11228281");
	this.siLgSDesc_lbl.setLabel("Description:");
	this.siLgDDesc_lbl.setLabel("Lorem Ipsum Dolor");

	this.siLgSPart_lbl.setDimension(15,0);
	this.siLgDPart_lbl.setDimension(115,0);
	this.siLgSDesc_lbl.setDimension(205,0);
	this.siLgDDesc_lbl.setDimension(280,0);

	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.siLgSPart_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.siLgSDesc_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties(this.siLgDPart_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties(this.siLgDDesc_lbl);
	
	this.siLgDDesc_lbl.setSize(200,20);

//GRIDS 

	this.structure_grid = new MppDataGrid( this.siGridCvs.getContainer(), 1);
	this.siResult_lbl = new MppTextLabel( this.siGridCvs.getContainer(), 2);

	this.structure_grid.setSize(550,275);
	this.structure_grid.setDimension(15,0);
	this.structure_grid.setSpliter(this.grid_spliter);
	SEGSWindowCoreUIUtil.gridStyles ( this.structure_grid );
	
	this.structure_grid.onCellPress = this.gridCellPress ;
	this.structure_grid.toolTipOn( false );
	
	this.siResult_lbl.setSize(570, 20);
	this.siResult_lbl.setTextAlignment("center");
	this.siResult_lbl.setDimension(0, 135);
	this.siResult_lbl.setWeight(true, false, false);
	this.siResult_lbl.setLabel("No Records Found");
//BTNS

	this.siPrint_btn = new MppButtonAsset( this.siBtnCvs.getContainer(), 1);
	this.siAddSelect_btn = new MppButtonAsset( this.siBtnCvs.getContainer(), 2);
	this.siCancel_btn = new MppButtonAsset( this.siBtnCvs.getContainer(), 3);
	this.siGridNaviCvs = new MppCanvas( this.siBtnCvs.getContainer(), 4);

	this.siPrev_btn = new MppButtonAsset( this.siGridNaviCvs.getContainer(),1);
	this.siNext_btn = new MppButtonAsset( this.siGridNaviCvs.getContainer(),2);
	this.siPage_lbl = new MppTextLabel( this.siGridNaviCvs.getContainer(), 3);

	this.siGridNaviCvs.setSize(300,25);

	
	SEGSWindowCoreUIUtil.buttonConfig( this.siPrint_btn);
	SEGSWindowCoreUIUtil.buttonConfig( this.siAddSelect_btn);
	SEGSWindowCoreUIUtil.buttonConfig( this.siCancel_btn);
	SEGSWindowCoreUIUtil.buttonConfig( this.siPrev_btn);
	SEGSWindowCoreUIUtil.buttonConfig( this.siNext_btn);


	this.siCancel_btn.setLabel("Cancel");
	this.siPrint_btn.setLabel("Printer Friendly");
	this.siAddSelect_btn.setLabel("Add to Selection");
	this.siNext_btn.setLabel("");
	this.siPrev_btn.setLabel("");

	this.siCancel_btn.setDimension(485,0);
	this.siAddSelect_btn.setDimension(359,0);
	this.siPrint_btn.setDimension(233,0);

	this.siAddSelect_btn.setSize(120, 20);
	this.siPrint_btn.setSize(120, 20);

	this.siNext_btn.setSize(20,20);
	this.siPrev_btn.setSize(20,20);

	this.siPrev_btn.setDimension(15,0);
	this.siNext_btn.setDimension(40,0);
	
	this.siNext_btn.attachIcon("righticon");
	this.siPrev_btn.attachIcon("lefticon");
	
	
	this.siPrev_btn.alignIcon("center",0);
	this.siNext_btn.alignIcon("center",0);
	
	this.siPrev_btn.getIconDisplay()._x = 7;
	this.siPrev_btn.getIconDisplay()._y = 7;
	this.siNext_btn.getIconDisplay()._x = 7;
	this.siNext_btn.getIconDisplay()._y = 7;
	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.siPage_lbl);
	this.siPage_lbl.setLabel("1 - 10 of 100");
	this.siPage_lbl.setDimension( 70, 0);
}
