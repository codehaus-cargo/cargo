var MachineInfoViewEvent = new Object();

MachineInfoViewEvent.FSB_GRID_PRESS = "onClickFsbGrid";
MachineInfoViewEvent.PLIST_GRID_PRESS = "onClickPListGrid";
MachineInfoViewEvent.CINDEX_GRID_PRESS = "onClickCIndexGrid";


function SEMachineInfoView ( parentCanvas ) {
	this.grid_spliter = "&jopiruizen&" ;
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.parent = parentCanvas;
	this.aligner = new MppAligner();
	this.buildComponents();
	SEGSWindowCoreUIUtil.machineView = this;
	this.addButtonListener();
	this.addLoaderListener();
	this.maxPerPage = 10;
	
	this.selectedItems = new Array();
	
	this.miInit();
}

SEMachineInfoView.prototype.gridSelectedItem;

SEMachineInfoView.prototype.grid_spliter;

SEMachineInfoView.prototype.parent;
SEMachineInfoView.prototype.aligner;
SEMachineInfoView.prototype.machine_grid ;
SEMachineInfoView.prototype.miResult_lbl ;

SEMachineInfoView.prototype.loader;
SEMachineInfoView.prototype.loaderType;

SEMachineInfoView.prototype.loadEventType;

//Data Related Vars
SEMachineInfoView.prototype.returnType;
SEMachineInfoView.prototype.commonData;
SEMahinceInfoView.prototype.gridDataProvider;

SEMachineInfoView.prototype.miDataGroup;
SEMachineInfoView.prototype.miPageLblCol;
SEMachineInfoView.prototype.pageCtr;
SEMachineInfoView.prototype.maxPerPage; 

SEMachineInfoView.prototype.fullGridDataProvider;
SEMachineInfoView.prototype.searchGridDataProvider;


//PRIMARY CANVAS
SEMachineInfoView.prototype.miSerialCvs  ;
SEMachineInfoView.prototype.miPartsCvs ;
SEMachineInfoView.prototype.miBtnCvs  ;
SEMachineInfoView.prototype.miGridCvs ;
SEMachineInfoView.prototype.miLgCvs  ;

//SECONDARY CANVAS

SEMachineInfoView.prototype.miGridNaviCvs;

SEMachineInfoView.prototype.miLgSerialCvs ;
SEMachineInfoView.prototype.miLgEngineCvs ;
SEMachineInfoView.prototype.miLgDateCvs ; // date Canvas


// COMPONENTS 

SEMachineInfoView.prototype.miSerial_txt ;
SEMachineInfoView.prototype.miSerial_lbl ;
	
SEMachineInfoView.prototype.miPList_rbtn  ;
SEMachineInfoView.prototype.miCIndex_rbtn ;
SEMachineInfoView.prototype.miFsb_rbtn  ;
SEMachineInfoView.prototype.miSearch_btn  ;


	
SEMachineInfoView.prototype.miPartDesc_txt ;
SEMachineInfoView.prototype.miPartNum_txt ;
	
SEMachineInfoView.prototype.miPartDesc_lbl  ;
SEMachineInfoView.prototype.miPartNum_lbl  ;

SEMachineInfoView.prototype.miPartDesc_rbtn  ;
SEMachineInfoView.prototype.miPartNum_rbtn ;
SEMachineInfoView.prototype.miSerial_rgroup 


SEMachineInfoView.prototype.miPart_rgroup ;
SEMachineInfoView.prototype.miPartSearch_btn  ;
SEMachineInfoView.prototype.miDescSearch_btn  ;

SEMachineInfoView.prototype.miAddSelect_btn ;
SEMachineInfoView.prototype.miCancel_btn ;
SEMachineInfoView.prototype.miPrint_btn ;
SEMachineInfoView.prototype.miShowAll_btn;

SEMachineInfoView.prototype.miNext_btn  ;
SEMachineInfoView.prototype.miPrev_btn ;
SEMachineInfoView.prototype.miPage_lbl ;
	
SEMachineInfoView.prototype.miLgSSerial_lbl ;
SEMachineInfoView.prototype.miLgDSerial_lbl  ;
SEMachineInfoView.prototype.miLgSModel_lbl ;
SEMachineInfoView.prototype.miLgDModel_lbl ;
SEMachineInfoView.prototype.miLgSOwner_lbl ;
SEMachineInfoView.prototype.miLgDOwner_lbl ;
	
SEMachineInfoView.prototype.miLgSEngine_lbl  ;
SEMachineInfoView.prototype.miLgDEngine_lbl  ;
SEMachineInfoView.prototype.miLgSDesc_lbl ;
SEMachineInfoView.prototype.miLgDDesc_lbl  ;
SEMachineInfoView.prototype.miLgSPart_lbl  ;
SEMachineInfoView.prototype.miLgDPart_lbl   ;
	
SEMachineInfoView.prototype.miLgSBuild_lbl  ;
SEMachineInfoView.prototype.miLgDBuild_lbl  ;
SEMachineInfoView.prototype.miLgSShip_lbl  ;
SEMachineInfoView.prototype.miLgDShip_lbl ;
SEMachineInfoView.prototype.miLgSServ_lbl  ;
SEMachineInfoView.prototype.miLgDServ_lbl   ;


//FUNCTIONALITY VARS
SEMachineInfoView.prototype.serialNum; 
SEMachineInfoView.prototype.selectedItems;

SEMachineInfoView.prototype.buttonsClicked = function ( _event ) {
	 
	switch ( _event.target.button_mc ){
			 case SEGSWindowCoreUIUtil.machineView.miDescSearch_btn.button_mc : 
			 	 
				SEGSWindowCoreUIUtil.machineView.descriptionSearch();
			 break;
		
			 case SEGSWindowCoreUIUtil.machineView.miPartSearch_btn.button_mc :
				 SEGSWindowCoreUIUtil.machineView.partsSearch();
			 break;
			 case SEGSWindowCoreUIUtil.machineView.miSearch_btn.button_mc :
			 	//trace("Search");
				
				SEGSWindowCoreUIUtil.machineView.searchQuery();
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miAddSelect_btn.button_mc:
			 	 
				SEGSWindowCoreUIUtil.machineView.miAddSelect_btn.setEnable( false );
				SEGSWindowCoreUIUtil.machineView.miAddSelect_btn.setBgColor ( 0xE1E1E1 );
				SEGSWindowCoreUIUtil.machineView.addToSelection();
				
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miCancel_btn.button_mc :
			 	//trace("Cancel " );
				var _event = new Object();
				_event.type = GlobalSearchCoreEvent.CLOSE_WINDOW ;
				_event.target = SEGSWindowCoreUIUtil.machineView;
				SEGSWindowCoreUIUtil.machineView.broadcastMessage( GlobalSearchCoreEvent.CLOSE_WINDOW , _event);
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miPrint_btn.button_mc :
				_level0.machineInfoLevel.miCurResultType = SEGSWindowCoreUIUtil.machineView.returnType;
				_level0.machineInfoLevel.miItemCol = SEGSWindowCoreUIUtil.machineView.gridDataProvider;
				_level0.machineInfoLevel.miCurObj = SEGSWindowCoreUIUtil.machineView.commonData ;
				// loadMovieNum("printMachineInfo.swf",1129);
				//print verstion 2
				loadMovieNum(_level0.staticWebURL+"printMachineInfo.swf",1129);
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miNext_btn.button_mc :
			  
				SEGSWindowCoreUIUtil.machineView.miDisplayNext();
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miPrev_btn.button_mc :
			 	 SEGSWindowCoreUIUtil.machineView.miDisplayPrev();
				
			 break;
			 
			 case SEGSWindowCoreUIUtil.machineView.miShowAll_btn.button_mc : 
				SEGSWindowCoreUIUtil.machineView.miShowAll_btn.setEnable( false );
				SEGSWindowCoreUIUtil.machineView.miShowAll_btn.setBgColor ( 0xE1E1E1 );
				SEGSWindowCoreUIUtil.machineView.showAll();

			 break;
	}
}



SEMachineInfoView.prototype.gridCellPress = function ( _event ) {
	var miview = SEGSWindowCoreUIUtil.machineView;
	var param  = new Object();
	 if( SEGSWindowCoreUIUtil.machineView.loadEventType == "onLoadFsbFsc" ) {
		
		param.bulletinType = _event.row[0];
		param.bulletinNumber = _event.row[1];
		
		param.serialNumber = SEGSWindowCoreUIUtil.machineView.miLgDSerial_lbl.getLabel();
		param.modelNumber = SEGSWindowCoreUIUtil.machineView.miLgDModel_lbl.getLabel();
		param.owner = SEGSWindowCoreUIUtil.machineView.miLgDOwner_lbl.getLabel();
		param.engineNumber = SEGSWindowCoreUIUtil.machineView.miLgDEngine_lbl.getLabel();
		param.description = SEGSWindowCoreUIUtil.machineView.miLgDDesc_lbl.getLabel();
		param.partNumber =  SEGSWindowCoreUIUtil.machineView.miLgDPart_lbl.getLabel();
		
		param.type = MachineInfoViewEvent.FSB_GRID_PRESS;
	 	SEGSWindowCoreUIUtil.machineView.broadcastMessage( MachineInfoViewEvent.FSB_GRID_PRESS  , param );
	 } else {
		 
		//trace("SEMachineInfoView.prototype.gridCellPress()   _event.row[0]="+_event.row[0])
		//trace("SEMachineInfoView.prototype.gridCellPress()   _event.row[1]="+_event.row[1])
		//trace("SEMachineInfoView.prototype.gridCellPress()   _event.row[2]="+_event.row[2])
		var item   =  new Object();
		item.pnum = _event.row[0];
		item.desc = _event.row[1];
		item.qty = _event.row[2];
		miview.gridSelectedItem = item ;
	 	miview.miAddSelect_btn.setEnable ( true );
		SEGSWindowCoreUIUtil.machineView.broadcastMessage ("onPicklistGridPress", param );
		 
	 }
	
}


SEMachineInfoView.prototype.loadHandler = function ( _event ) {
	this.loadEventType = _event.type;
	if ( _event.type == MILoaderEvent.LOAD_ADD_TO_SELECTION ) {
		_level300.mouseNormal();
	} else {
		SEGSWindowCoreUIUtil.machineView.parseLoadedData ( _event.resultXML , _event.type );
	}
}

SEMachineInfoView.prototype.parseLoadedData = function ( resultXML , eventType ){
	_level0.machineInfoLevel.mixml = resultXML;
	this.initCommonData( resultXML , eventType );
	this.gridDataProvider = new ItemCollection();
	this.gridDataProvider.emptyCollection();
	var itemXml = new XML(); 
	itemXml = resultXML.firstChild.firstChild.nextSibling;
	//trace("itemXML  "+itemXML.nodeName)
	while ( itemXml != null ) {
		var itemObj = new Object();
		//trace("itemXML  "+itemXML);
		if(  eventType == MILoaderEvent.LOAD_FSBFSC) {
			 /*
			 var child = itemXml.firstChild ;
			 while ( child != null ) {
				 trace("child: " + child ) ;
				 this.gridDataProvider.addNewItem(  this.fsbData(  child )  );
				child = child.nextSibling;
			 } */
			 while ( itemXml != null ) {
				//trace("item: " + itemXml ); 
				this.gridDataProvider.addNewItem( this.fsbData( itemXml ) )  
				itemXml  = itemXml.nextSibling;
			}
			 
			
		} else if(  eventType == MILoaderEvent.LOAD_PICKLIST ){
			itemObj = this.pickListData(itemXml);
			this.gridDataProvider.addNewItem(itemObj);
		} else if(  eventType == MILoaderEvent.LOAD_COMPONENT_INDEX ){
			itemObj = this.cindexData(itemXml);
			this.gridDataProvider.addNewItem(itemObj);
		}
		
		itemXml = itemXml.nextSibling;
	}
	
	this.fullGridDataProvider = this.gridDataProvider;
	
	this.miDisplayData( eventType);
	if ( this.fullGridDataProvider.getCollectionLength () == 0 ) {
		this.miResult_lbl.setVisibility( true );
	}
	_level300.mouseNormal();
}
 
SEMachineInfoView.prototype.addButtonListener = function () {
	this.miShowAll_btn.onRelease = this.buttonsClicked;
	this.miSearch_btn.onRelease = this.buttonsClicked ;
	this.miPartSearch_btn.onRelease = this.buttonsClicked;
	this.miDescSearch_btn.onRelease = this.buttonsClicked;
	
	this.miAddSelect_btn.onRelease = this.buttonsClicked ;
	this.miCancel_btn.onRelease = this.buttonsClicked ;
	this.miPrint_btn.onRelease = this.buttonsClicked;
	
	this.miNext_btn.onRelease = this.buttonsClicked;
	this.miPrev_btn.onRelease = this.buttonsClicked;
	
}

SEMachineInfoView.prototype.addLoaderListener = function () {
	this.loader = new MachineInfoLoader();
	this.loader.onLoadPickList = this.loadHandler;
	this.loader.onLoadFsbFsc = this.loadHandler;
	this.loader.onLoadComponentIndex = this.loadHandler;
	this.loader.onLoadAddToSelection = this.loadHandler;
	this.loader.onIOError = this.loaderIoError;
}

SEMachineInfoView.prototype.loaderIoError = function  ( _event ) {
	 SEGSWindowCoreUIUtil.machineView.ioErrorHandler ( _event );
}

SEMachineInfoView.prototype.ioErrorHandler = function ( _event ) {
	this.machine_grid.cleanLines();
	this.machine_grid.deleteAllRows();
	this.miDisplayNoResult();
	 this.miResult_lbl.setVisibility( true );
	 _level300.mouseNormal();
}

SEMachineInfoView.prototype.addToSelection = function () {
	trace("SEMachineInfoView.prototype.addToSelection()   this.gridSelectedItem.pnum="+this.gridSelectedItem.pnum)
	trace("SEMachineInfoView.prototype.addToSelection()   this.gridSelectedItem.qty="+this.gridSelectedItem.qty)
	trace("SEMachineInfoView.prototype.addToSelection()   this.gridSelectedItem.desc="+this.gridSelectedItem.desc)
	
	//trace("SEMachineInfoView.prototype.addToSelection()   _level0.partid="+  _level0.partid);                
	//trace("SEMachineInfoView.prototype.addToSelection()   _level0.mpid="+  _level0.mpid);  
	//trace("SEMachineInfoView.prototype.addToSelection()   _level0.partdesc="+  _level0.partdesc);  
	
	/* */
	//if(  this.gridSelectedItem.pnum != "null" && this.gridSelectedItem.pnum != null &&  this.gridSelectedItem.pnum != undefined) {
		var targetURL = SEVars.REQUEST_DIRECTOR  + "CheckParts";     
		//this.selectedItems.push( this.gridSelectedItem );
		_level0.machineInfoLevel.selectedEntries.push( this.gridSelectedItem );
		_level0.machineInfoLevel.entriesAdded = false;
		this.machine_grid.refreshGrid();
		 // SEGSWindowCoreUIUtil.machineView.miAddSelect_btn.setEnable( false );
		/*
		if ( _level0.selectedPartsLevel == undefined  ||  _level0.selectedPartsLevel == null) {
			_level0.loadTabs ("tabs2web.swf", 200); 
		} else {
			_level0.selectedPartsLevel.addSelectedParts(); 
		}
		*/
		
		//this.loader.addToSelection( targetURL , this.gridSelectedItem );
	//}
	
	this.gridSelectedItem.color_selected = "";
	SEGSWindowCoreUIUtil.machineView.miAddSelect_btn.enabled =  false ;
	
	_level0.searchResultsOrderPartsClicked=true;
	_level0.mpid = this.gridSelectedItem.pnum;
	_level0.partExternalQuantity = this.gridSelectedItem.qty;
	_level0.partdesc = this.gridSelectedItem.desc;
	
	_level0.globalSearchAddToSelection();
	

}

SEMachineInfoView.prototype.searchQuery  = function() {
	this.serialNum = this.miSerial_txt.getTextValue();
	this.serialNum = StringUtil.trim(this.serialNum);
	var param = new Object();
	//PARAM CONFIG
	param.curDataSourceId = _level0.curDataSourceID;
	param.curMfrID = _level0.curMfrID;
	param.serialNumber = this.serialNum;
	var targetURL;
	/*HELPER
		MachcineInfoFSB
		MachineInfoComponentIndex
		MachineInfoPickList
	*/
	_level300.mouseWait();
	if( this.miPList_rbtn.isSelected() == true){
		// targetURL = "http://localhost/smartequipt/xml/mi/picklist.php";
		//targetURL = "datatest/MachineInformation/picklist_response.xml";
		// targetURL = "datatest/MachineInformation/responsePickList.xml";
		 targetURL = SEVars.REQUEST_DIRECTOR + "MachineInfoPickList";
		 this.loader.loadPickList( targetURL , param );
		 //FOR LOCAL TESTING
		 
		//this.loader.loadPickList("datatest/MachineInformation/responsePickList.xml", _param );	
		//this.loader.loadPickList("picklist_response.xml", _param );	
	} else if( this.miCIndex_rbtn.isSelected() == true){ 
		// targetURL = "http://localhost/smartequipt/xml/mi/cindex.php";
	 	//targetURL = "datatest/MachineInformation/responseComponentIndex.txt";
		 targetURL = SEVars.REQUEST_DIRECTOR + "MachineInfoComponentIndex";
		 this.loader.loadComponentIndex(targetURL, param );
		//FOR LOCAL TESTING
		//this.loader.loadComponentIndex("datatest/MachineInformation/responseComponentIndex.xml", _param );	
		 
	} else if ( this.miFsb_rbtn.isSelected() == true) {
		//targetURL = "http://localhost/smartequipt/xml/mi/noresult.php"; 
		// targetURL = "http://localhost/smartequipt/xml/mi/fsbfsc.php"; 
		// targetURL = "NEW_FSB_DATA.txt";
		 targetURL = SEVars.REQUEST_DIRECTOR + "MachineInfoFSB";
		 this.loader.loadFSB(targetURL, param );
		 //FOR LOCAL TESTING
		//this.loader.loadFSB("fsb_response.xml");
		//this.loader.loadFSB("datatest/MachineInformation/responseFSB.xml", _param );	
		 
	}
	 
}

// DISPLAY ARRANGEMENT
SEMachineInfoView.prototype.miInit = function () {
	this.miDisplayNoResult();
	this.miResult_lbl.setVisibility( false );
}
SEMachineInfoView.prototype.miResizeGrid1 = function () {
	//trace("ResizeGrid1");
	/* DEFAULT */
	this.miGridCvs.setSize(570, 305);
	this.miGridCvs.setDimension(0, 50);
	this.machine_grid.setSize(550,300);
	this.machine_grid.setDimension(15,0);
	this.miResult_lbl.setDimension(0, 145);
}
SEMachineInfoView.prototype.miResizeGrid2 = function () {
	/*FieldService Bulletin - FieldService Component - FSB_FSC*/
	//trace("ResizeGrid2");
	this.miGridCvs.setSize(570, 255);
	this.miGridCvs.setDimension(0, 100);
	this.machine_grid.setSize(550,250);
	this.machine_grid.setDimension(15,0);
	this.miResult_lbl.setDimension(0, 120);
}
SEMachineInfoView.prototype.miResizeGrid3 = function () {
	//trace("ResizeGrid3");
	/* PICKLIST */
	this.miGridCvs.setSize(570, 235);
	//old dimension	this.miGridCvs.setDimension(0, 125);
	this.miGridCvs.setDimension(0, 140);
	this.machine_grid.setSize(550,230);
	this.machine_grid.setDimension(15,0);
	this.miResult_lbl.setDimension(0, 90);
	
}
SEMachineInfoView.prototype.miLgRepos1 = function () {
	/* DEFAULT */
	this.miLgCvs.setDimension(0,72);
}

SEMachineInfoView.prototype.miLgRepos2 = function (){
	/*FieldService Bulletin /FSB*/
	this.miLgCvs.setDimension(0,50);
}

SEMachineInfoView.prototype.miDisplayNoResult = function(){
	this.miResult_lbl.setVisibility(true);
	this.miBtnCvs.setVisibility(false);
	this.miGridNaviCvs.setVisibility(false);
	this.miPrint_btn.setEnable(false);
	this.miAddSelect_btn.setEnable(false);
	this.miShowAll_btn.setEnable( false );
	this.miLgCvs.setVisibility(false);
	this.miPartsCvs.setVisibility(false);
	this.miResizeGrid1();
}

SEMachineInfoView.prototype.descriptionSearch = function () {
	 
	var searchText = StringUtil.trim ( this.miPartNum_txt.getTextValue() );
	this.searchGridDataProvider = new ItemCollection();
	for (var i = 0 ; i <  this.fullGridDataProvider.getCollectionLength(); i++ ) {
		var item = this.fullGridDataProvider.getItem( i );
		var boolResult = this.searchItem ( item , searchText );
		if( boolResult == true ) {
			 this.searchGridDataProvider.addNewItem ( item );
		}
	}
	 
	this.gridDataProvider = this.searchGridDataProvider;
	this.miDisplayData(  this.loadEventType  );
	
	if( this.gridDataProvider.getCollectionLength () == 0 ){
		this.miResult_lbl.setVisibility( true );
	}
	this.miShowAll_btn.setEnable ( true );
}

SEMachineInfoView.prototype.partsSearch = function  () {
	var searchText = StringUtil.trim ( this.miPartNum_txt.getTextValue() );
	this.searchGridDataProvider = new ItemCollection();
	for (var i = 0 ; i <  this.fullGridDataProvider.getCollectionLength(); i++ ) {
		var item = this.fullGridDataProvider.getItem( i ); 
		if( StringUtil.equalAtStart( item.partNumber , searchText ) ) {
			 this.searchGridDataProvider.addNewItem ( item );
		}
	}
	
	this.gridDataProvider = this.searchGridDataProvider;
	this.miDisplayData(  this.loadEventType  );
	
	if( this.gridDataProvider.getCollectionLength () == 0 ){
		this.miResult_lbl.setVisibility( true );
	}
	this.miShowAll_btn.setEnable ( true );
}

SEMachineInfoView.prototype.showAll = function () {
	this.gridDataProvider = this.fullGridDataProvider;
	this.miShowAll_btn.setEnable ( false );
	this.miDisplayData(  this.loadEventType  );
}

SEMachineInfoView.prototype.searchItem  = function ( item , searchText ) {
	//trace("item: partnumber: " + item.partNumber + " desc: " + item.description );
	if( StringUtil.hasSubstring (item.description  , searchText ) ) {
		return true;
	} else {
		return false;
	}
	
}
//FUNCTIONALITY --------------------




SEMachineInfoView.prototype.initCommonData   = function( resultXML , eventType ) {
	trace("SEMachineInfoView.prototype.initCommonData()   eventType="+eventType)
	//THE LIST OF COMMON DATA 
	//var container = 	_level291.MppWindow1_mc.canvas_mc.content_mc.MppTab1_mc.tab1_mc.MppCanvas2_mc.child_mc.MppCanvas5_mc.child_mc.MppCanvas2_mc.child_mc.createEmptyMovieClip("container", 999);
	var container = this.miLgEngineCvs.getContainer().createEmptyMovieClip( "container", 999);
	//trace("Container: " + container );
	this.commonData = new Object();
	this.returnType = resultXML.firstChild.attributes.returntype;
	this.miLgSEngine_lbl.setVisibility(true) ;
	this.miLgSPart_lbl.setVisibility(true) ;
	this.miLgSDesc_lbl.setVisibility(true);
	this.miLgDEngine_lbl.setVisibility(true) ;
	this.miLgDPart_lbl.setVisibility(true) ;
	this.miLgDDesc_lbl.setVisibility(true);
	
	//trace( " Serial:  "  + this.miLgDSerial_lbl.getDisplay()._visible );
	
	if(resultXML.firstChild.attributes.serialnum!=undefined) this.commonData.serialNum = resultXML.firstChild.attributes.serialnum;
	else this.commonData.serialNum = "";
	
	if(resultXML.firstChild.attributes.model!=undefined) this.commonData.model = resultXML.firstChild.attributes.model;
	else this.commonData.model = "";
	
	if(resultXML.firstChild.attributes.owner!=undefined) this.commonData.owner = resultXML.firstChild.attributes.owner;
	else this.commonData.owner = "";
	
	if(resultXML.firstChild.firstChild.firstChild.attributes.engineserialnum!=undefined) this.commonData.engineSNum =  resultXML.firstChild.firstChild.firstChild.attributes.engineserialnum;
	else this.commonData.engineSNum = "";
	
	if(resultXML.firstChild.firstChild.firstChild.attributes.description!=undefined) this.commonData.description = resultXML.firstChild.firstChild.firstChild.attributes.description;
	else this.commonData.description = "";
	
	if(resultXML.firstChild.firstChild.firstChild.attributes.partnum!=undefined) this.commonData.partNumber =  resultXML.firstChild.firstChild.firstChild.attributes.partnum;
   	else this.commonData.partNumber = "";
   	
   
   	//trace("DESCRIPTION "+resultXML.firstChild.firstChild.firstChild.nodeName);
	 
	if( eventType != MILoaderEvent.LOAD_FSBFSC ){   // Have DATEs ATTRIBUTE 
		if(resultXML.firstChild.attributes.builddate!=undefined) this.commonData.buildDate = resultXML.firstChild.attributes.builddate;
		else if(resultXML.firstChild.attributes.buildDate!=undefined) this.commonData.buildDate = resultXML.firstChild.attributes.buildDate;
		else this.commonData.buildDate = "";
		
		if(resultXML.firstChild.attributes.shipdate!=undefined) this.commonData.shipDate = resultXML.firstChild.attributes.shipdate;
		else if(resultXML.firstChild.attributes.shipDate!=undefined) this.commonData.shipDate = resultXML.firstChild.attributes.shipDate;
		else this.commonData.shipDate = "";
		
		if(resultXML.firstChild.attributes.inservdate!=undefined) this.commonData.inServDate = resultXML.firstChild.attributes.inservdate;
		else if(resultXML.firstChild.attributes.inservDate!=undefined) this.commonData.inServDate = resultXML.firstChild.attributes.inservDate;
		else this.commonData.inServDate = "";
		
	var engInfoNode = resultXML.firstChild.firstChild.firstChild;
	var lineCounter = 1;
	yPosition = 20;
	var boxHeight = 0;
	//IF ONLY MULIPLE  SETS OF DATA ARE RETURNED
	//trace("Engine: " + engInfoNode + " NodeName: " +  engInfoNode.nodeName );
	if ( engInfoNode.nextSibling !=null && engInfoNode.nodeName == "engineLine"  ){
		 
		container._visible = true;
		//HIDE LABELS 
		
        this.miLgSEngine_lbl.setVisibility(false) ;
		this.miLgSPart_lbl.setVisibility(false) ;
		this.miLgSDesc_lbl.setVisibility(false);
		
		this.miLgDEngine_lbl.setVisibility(false) ;
		this.miLgDPart_lbl.setVisibility(false) ;
		this.miLgDDesc_lbl.setVisibility(false);
		
		container.createTextField("multiple_txt",1,15,0,300,20);
		container.multiple_txt.html = true;
		container.multiple_txt.htmlText = "More than one result found. Click here to view >";
		container.useHandCursor = true;
		var txtStyle_fmt = new TextFormat();
		txtStyle_fmt.color = 0xFF9933;
		txtStyle_fmt.underline = true;
		txtStyle_fmt.font = "Arial";
		txtStyle_fmt.size = "11";
		txtStyle_fmt.bold = true;
		container.multiple_txt.setTextFormat(txtStyle_fmt);
		
		var popUptxtStyle_fmt = new TextFormat();
		popUptxtStyle_fmt.color = 0x333333;
		popUptxtStyle_fmt.font = "Arial";
		popUptxtStyle_fmt.size = "11";
	    
		var headersPopUptxtStyle_fmt = new TextFormat();
		headersPopUptxtStyle_fmt.color = 0x333333;
		headersPopUptxtStyle_fmt.font = "Arial";
		headersPopUptxtStyle_fmt.size = "12";
		headersPopUptxtStyle_fmt.bold = true;
		
	
		container.onRelease= function(){
			
			if(!multiLinePopUp_mc._visible){
				multiLinePopUp_mc._visible = true;
				multiLinePopUp_mc.swapDepths( 2 );
				//_level291.MppWindow1_mc.swapDepths(multiLinePopUp_mc);
			}
		}
		


		multiLinePopUp_mc.createTextField("serialnumLabel",10, 0  ,0 , 150, 20);
		multiLinePopUp_mc.createTextField("descriptionLabel",12, 130  ,0 , 150, 20);
		multiLinePopUp_mc.createTextField("partnumLabel",11, 350  ,0 , 150, 20);
		multiLinePopUp_mc.serialnumLabel.text = "Component Serial #";
		multiLinePopUp_mc.partnumLabel.text = "Part Number";
		multiLinePopUp_mc.descriptionLabel.text = "Description";
		multiLinePopUp_mc.serialnumLabel.setTextFormat(headersPopUptxtStyle_fmt);
		multiLinePopUp_mc.descriptionLabel.setTextFormat(headersPopUptxtStyle_fmt);
		multiLinePopUp_mc.partnumLabel.setTextFormat(headersPopUptxtStyle_fmt);
		
	for (var aNode = engInfoNode; aNode != null; aNode = aNode.nextSibling) {
		
		//RESIZE THE POPUP BACKGROUND
		if (lineCounter > 4){
			multiLinePopUp_mc.boxHolder_mc._height+=20;		
		}
		
		//CREATE NEW TEXT FIELDS FOR POPUP
		multiLinePopUp_mc.createTextField("engineserialnum"+lineCounter, lineCounter, multiLinePopUp_mc.serialnumLabel._x  ,yPosition , 100, 20);
		multiLinePopUp_mc.createTextField("description"+lineCounter, lineCounter+50, multiLinePopUp_mc.descriptionLabel._x  ,yPosition , 250, 20);
		multiLinePopUp_mc.createTextField("partnum"+lineCounter, lineCounter+100, multiLinePopUp_mc.partnumLabel._x  ,yPosition , 100, 20);
		
		//PUSH DATA
		multiLinePopUp_mc["engineserialnum"+lineCounter].text = aNode.attributes.engineserialnum ;
		multiLinePopUp_mc["partnum"+lineCounter].text = aNode.attributes.partnum ;
		multiLinePopUp_mc["description"+lineCounter].text = aNode.attributes.description ;
		multiLinePopUp_mc["description"+lineCounter].multiline = true;
		multiLinePopUp_mc["description"+lineCounter].wordWrap = true;
		multiLinePopUp_mc["description"+lineCounter].autoSize = "left";
		multiLinePopUp_mc["engineserialnum"+lineCounter].setTextFormat(popUptxtStyle_fmt);
		multiLinePopUp_mc["description"+lineCounter].setTextFormat(popUptxtStyle_fmt);
		multiLinePopUp_mc["partnum"+lineCounter].setTextFormat(popUptxtStyle_fmt);

		//trace("yposition "+yPosition +"  wraped text height " + multiLinePopUp_mc["description"+lineCounter]._height );
		yPosition +=   multiLinePopUp_mc["description"+lineCounter]._height ;
		lineCounter++;
	     }
	 } 
	} else { //Have Owner ATTRIBUTE
	trace("NOT HERE");
		container._visible = false;
		var module = resultXML.firstChild.firstChild;
		/*
		var module = resultXML.firstChild;
		
		this.commonData.serialNum =  module.attributes.serialnum;
		this.commonData.model = module.attributes.model;
		this.commonData.engineSNum =  module.attributes.engineserialnum;
		this.commonData.description = module.attributes.description;
		this.commonData.partNumber =  module.attributes.partnum;
	
		this.commonData.owner =  resultXML.firstChild.attributes.owner;
		*/
	}
}



//CLOSE MULTIPLE ENGINE LINE ITEMS POPUP
multiLinePopUp_mc.closeButton_mc.onRelease = function(){
	multiLinePopUp_mc._visible = false;
	_level291.MppWindow1_mc.swapDepths(multiLinePopUp_mc);
}



SEMachineInfoView.prototype.pickListData  = function ( itemXml ) {
	 
	var itemObj = new Object();
	itemObj.partNumber = itemXml.firstChild.firstChild;
	
	itemObj.description = itemXml.firstChild.nextSibling.firstChild.nodeValue;
	//trace(itemObj.description)
	itemObj.quantity = itemXml.firstChild.nextSibling.nextSibling.firstChild;
	itemObj.UM = itemXml.firstChild.nextSibling.nextSibling.nextSibling.firstChild;
	 
	return itemObj;
}
SEMachineInfoView.prototype.cindexData  = function ( itemXml){
	var itemObj = new Object();
	itemObj.partNumber = itemXml.firstChild.firstChild;
	itemObj.description = itemXml.firstChild.nextSibling.firstChild;
	return itemObj;
}
SEMachineInfoView.prototype.fsbData = function( itemXml  ) {
	
	var itemObj = new Object();
	 var child = itemXml.firstChild;
	 while ( child != null ) {
		
		if( child.nodeName == "bulletintype" ) {
			itemObj.bulletinType = child.attributes.xmlns;
		} else if ( child.nodeName == "bulletinnumber" ) {
			itemObj.bulletinNumber = child.attributes.xmlns;
		} else if ( child.nodeName == "description" ) {
			itemObj.description = child.attributes.xmlns;
		} else if ( child.nodeName == "completed" ) {
			itemObj.completeStatus =child.attributes.xmlns;
		}
		 child = child.nextSibling
	} 
	/*
	itemObj.bulletinType = child.attributes.type;
	itemObj.bulletinNumber = child.attributes.bulletinnumber 
	itemObj.description = child.attributes.bulletindescription;
	itemObj.completeStatus = child.attributes.completed;
	*/
	return itemObj;
}


SEMachineInfoView.prototype.miDisplayData = function( eventType ) {
		
			
	this.miCommonDisplay(eventType);
	if(this.returnType != "NoResult" || this.gridDataProvider.length.getCollectionLength() !=  0 ){
		if(   eventType == MILoaderEvent.LOAD_FSBFSC ) {
			this.miDisplayFsbFsc();
		} else if(  eventType == MILoaderEvent.LOAD_PICKLIST  ){
			this.miDisplayPList();
		} else if( eventType == MILoaderEvent.LOAD_COMPONENT_INDEX  ){
			this.miDisplayCIndex();
		}  
		this.miGroupByMax ();
		this.miDisplayFirstMax( eventType );
	} else {
		
		this.miDisplayNoResult();
	}
} 
SEMachineInfoView.prototype.miCommonDisplay = function( eventType) {
	
	this.miPrint_btn.setEnable(true);
	this.miAddSelect_btn.setEnable(false);
	this.machine_grid.deleteAllColumns();
	
	this.miResult_lbl.setVisibility(false);
	this.miLgCvs.setVisibility(true);
	this.miBtnCvs.setVisibility(true);
	this.miPartsCvs.setVisibility(true);
	this.miLgDateCvs.setVisibility(true);
	this.miAddSelect_btn.setVisibility(true);
	this.miPrint_btn.setVisibility(true);
	this.miShowAll_btn.setVisibility ( true );
	this.miLgSOwner_lbl.setVisibility(false);
	this.miLgDOwner_lbl.setVisibility(false);
	this.miResizeGrid3();
	this.miLgRepos1();
	
	 
	trace("serial "+this.commonData.serialNum)
	trace("model "+this.commonData.model)
	trace("engserial "+this.commonData.engineSNum)
	trace("description "+this.commonData.description)
	trace("partnum "+this.commonData.partNumber)
	trace("owner "+this.commonData.owner)
	trace("builddate "+this.commonData.buildDate)
	trace("shipdate "+this.commonData.shipDate)
	trace("servdate "+this.commonData.inServDate)
	 
	 
	this.miLgDSerial_lbl.setLabel( this.commonData.serialNum);
	//trace("D Label "+this.miLgDSerial_lbl.getLabel())
	this.miLgDModel_lbl.setLabel(this.commonData.model);
	this.miLgDEngine_lbl.setLabel(this.commonData.engineSNum );	
	this.miLgDDesc_lbl.setLabel(this.commonData.description );
	this.miLgDPart_lbl.setLabel(this.commonData.partNumber );

	if(  eventType == MILoaderEvent.LOAD_FSBFSC   ) {
		 
		this.miLgDOwner_lbl.setLabel(this.commonData.owner);
	} else {
		this.miLgDBuild_lbl.setLabel(this.commonData.buildDate);
		this.miLgDShip_lbl.setLabel(this.commonData.shipDate);
		this.miLgDServ_lbl.setLabel(this.commonData.inServDate);
	}
}
SEMachineInfoView.prototype.miDisplayCIndex = function (){
	this.machine_grid.addColumn("Part/Component", 120);
	this.machine_grid.addColumn("Description", 250);
	
}
SEMachineInfoView.prototype.miDisplayPList = function (){
	this.machine_grid.addColumn("Part Number", 120);
	this.machine_grid.addColumn("Description", 250);
	this.machine_grid.addColumn("Quantity", 100);
	this.machine_grid.addColumn("UM", 10);
}
SEMachineInfoView.prototype.miDisplayFsbFsc = function (){
	this.miAddSelect_btn.setVisibility(false);
	this.miPrint_btn.setVisibility(false);
	this.miShowAll_btn.setVisibility ( false );
	this.miLgSOwner_lbl.setVisibility(true);
	this.miLgDOwner_lbl.setVisibility(true);
	this.miPartsCvs.setVisibility(false);
	this.miLgDateCvs.setVisibility(false);
	this.miResizeGrid2();
	this.miLgRepos2();
	//this.machine_grid.addColumn("", 20);
	
	this.machine_grid.addColumn("Bulletin Type", 80);
	this.machine_grid.addColumn("Bulletin Number", 100);
	this.machine_grid.addColumn("Description", 250);
	this.machine_grid.addColumn("Completed", 10);
}


SEMachineInfoView.prototype.miDisplayItemObj = function ( itemObj ,  eventType) {
	this.loadEventType = eventType;
	if(  eventType == MILoaderEvent.LOAD_FSBFSC  ) {
		this.machine_grid.sqlInsert("Bulletin Type ,  Bulletin Number, Description , Completed" , itemObj.bulletinType + this.grid_spliter + itemObj.bulletinNumber + this.grid_spliter + itemObj.description + this.grid_spliter + itemObj.completeStatus );
	} else if( eventType == MILoaderEvent.LOAD_PICKLIST  ){
		this.machine_grid.sqlInsert("Part Number,Description,Quantity,UM" ,  itemObj.partNumber + this.grid_spliter + itemObj.description + this.grid_spliter + itemObj.quantity + this.grid_spliter + itemObj.UM );
	} else if( eventType == MILoaderEvent.LOAD_COMPONENT_INDEX ){
		this.machine_grid.sqlInsert( " Part/Component , Description " ,  itemObj.partNumber + this.grid_spliter +itemObj.description );
	}
}

SEMachineInfoView.prototype.renderGrid = function () {
	if( SEGSWindowCoreUIUtil.machineView.loadEventType == "onLoadFsbFsc" ) {
		this.renderFSBGrid()
	}
}

//ADDING OF MAGNIFYING GLASS ICON
SEMachineInfoView.prototype.renderFSBGrid = function () {
	 
 	var bultypeColumn = this.machine_grid.columns[0];
	trace("CELL LENGTH "+bultypeColumn.cells.length);
	for( var i = 0 ; i < bultypeColumn.cells.length ; i++ ){
		
		var cell = bultypeColumn.cells[i];
		var cell_mc = cell.getDisplay();
		 
		cell_mc.attachMovie( "GlassIcon" , "icon" , 5 );
		cell_mc.icon._x = 10 ;
		cell_mc.label_txt._x = 27;
		trace(cell_mc.icon)
	}
	 /*
	for ( var i = 0 ; i < this.machine_grid.cells.length; i++ ) {
		var cell = this.machine_grid.cells[i];
		var cell_mc = cell.getDisplay();
		//trace( "cell: " + cell.columnIndex + " label: " + cell.getLabel() + " mc: " + cell_mc );
		if( cell.columnIndex == 1 ) { //BulletinType COlumn
			cell_mc.attachMovie( "GlassIcon" , "icon" , 5 );
			cell_mc.icon._x = 10 ;
			cell_mc.label_txt._x = 27;
		}
		
		//trace("Label: " + cell.getLabel () + " columnIndex: " + cell.columnIndex );
	} */
}

SEMachineInfoView.prototype.miGroupByMax = function() {
	this.miDataGroup = new ItemCollection();
	this.miPageLblCol = new ItemCollection();
	var ctr = 0;
	var newGroup  = new ItemCollection();
	var lastMax;
	for( ctr = 0; ctr < this.gridDataProvider.getCollectionLength(); ctr++){
		newGroup.addNewItem( this.gridDataProvider.getItem(ctr));
		if((ctr + 1 ) % this.maxPerPage == 0){
			this.miDataGroup.addNewItem(newGroup);
			newGroup = new ItemCollection();
			var pageObj = new Object();
			pageObj.firstIndex = (ctr - this.maxPerPage ) + 2;
			pageObj.lastIndex = ctr + 1;
			this.miPageLblCol.addNewItem(pageObj);
			lastMax = ctr + 2;
		}  else if ( (ctr + 1) == this.gridDataProvider.getCollectionLength()){
			this.miDataGroup.addNewItem(newGroup);
			var pageObj = new Object();
			pageObj.firstIndex = lastMax;
			pageObj.lastIndex = this.gridDataProvider.getCollectionLength();
			this.miPageLblCol.addNewItem(pageObj);
		}
	}
	
}

SEMachineInfoView.prototype.miDisplayFirstMax = function( eventType) {
	var ctr = 0;
 	this.pageCtr = 0;
	this.miNext_btn.setEnable(true);
	this.miPrev_btn.setEnable(false);
	this.machine_grid.deleteAllRows();
	if( this.gridDataProvider.getCollectionLength() > (this.maxPerPage - 1) ){
		this.miGridNaviCvs.setVisibility(true);
		this.miDisplayByMax( this.pageCtr , eventType );
	} else {
		this.miGridNaviCvs.setVisibility(false);
		for( ctr = 0; ctr < this.gridDataProvider.getCollectionLength(); ctr++){
			var itemObj = this.gridDataProvider.getItem(ctr);
			this.miDisplayItemObj( itemObj ,  eventType );
		}
		this.machine_grid.refreshGrid();
		this.renderGrid();
	}
}

SEMachineInfoView.prototype.miDisplayByMax = function ( current , eventType ) {
	//trace("CURRENT: " + current);
	this.machine_grid.deleteAllRows();
	var pageObj = this.miPageLblCol.getItem( current );
	this.miPage_lbl.setLabel( pageObj.firstIndex + " - " + pageObj.lastIndex + " of " + this.gridDataProvider.getCollectionLength());
	var icol =  this.miDataGroup.getItem( current );
	var ctr = 0;
	for( ctr = 0; ctr < icol.getCollectionLength(); ctr++){
		var itemObj= icol.getItem(ctr);
		this.miDisplayItemObj( itemObj , eventType );
	}
	this.machine_grid.refreshGrid();
	this.renderGrid();
}
SEMachineInfoView.prototype.miDisplayNext = function() {
	this.pageCtr++;
	this.miPrev_btn.setEnable(true);
	if( this.pageCtr < this.miDataGroup.getCollectionLength()){
		this.miDisplayByMax(this.pageCtr , this.loadEventType);
		if( this.pageCtr >=  this.miDataGroup.getCollectionLength() - 1) {
			this.miNext_btn.setEnable(false);
			this.miNext_btn.setBgColor ( 0xE1E1E1 );
		}
	} else { 
		this.miNext_btn.setEnable(false);
		this.miNext_btn.setBgColor ( 0xE1E1E1 );
		this.pageCtr = this.miDataGroup.getCollectionLength();
	}
}
SEMachineInfoView.prototype.miDisplayPrev = function() {
	this.pageCtr--;
	this.miNext_btn.setEnable(true);
	if( this.pageCtr < 0 ) {
		this.pageCtr = 0;
		this.miPrev_btn.setEnable(false);
		this.miPrev_btn.setBgColor ( 0xE1E1E1 );
	} else {
		if(this.pageCtr <= 0 ) {
			this.miPrev_btn.setEnable(false);
			this.miPrev_btn.setBgColor ( 0xE1E1E1 );
		}
		this.miDisplayByMax(this.pageCtr, this.loadEventType);
	}
}

//COMPONENTS BUILDER 
//**********************************************************************************************************\\
//**********************************************************************************************************\\
//**********************************************************************************************************\\
//**********************************************************************************************************\\
//COMPONENTS BUILDER 

SEMachineInfoView.prototype.buildComponents = function () {
	this.miSerialCvs = new MppCanvas(this.parent.getContainer(), 1);
	this.miPartsCvs = new MppCanvas(this.parent.getContainer(), 2);
	this.miBtnCvs = new MppCanvas ( this.parent.getContainer(), 3);
	this.miGridCvs = new MppCanvas( this.parent.getContainer(), 4);
	this.miLgCvs = new MppCanvas( this.parent.getContainer(), 5);
	
	
	
	this.miSerialCvs.setSize(570, 30);
	
	this.miPartsCvs.setSize(550 , 25);
	this.miPartsCvs.setCanvasColor ( 0xFFEEFF, 0xB3B3B3);
	this.miPartsCvs.setBorderAlpha( 100 );
	this.miPartsCvs.setDimension( 15  , 45);
	
	this.miBtnCvs.setSize(570, 25);
	this.miGridCvs.setSize(570, 195);
	this.miLgCvs.setSize(570, 70);
	 
	this.miSerialCvs.setDimension(0, 20);
	
	
	this.miBtnCvs.setDimension(0, 385);
	/* old position
	this.miBtnCvs.setDimension(0, 360);
	*/
	this.miGridCvs.setDimension(0, 155);
	this.miLgCvs.setDimension( 0, 72 );

	this.miSerial_txt = new MppText (this.miSerialCvs.getContainer(), 1);
	this.miSerial_lbl = new MppTextLabel( this.miSerialCvs.getContainer(), 2);

	this.miSerial_txt.visualize();
	this.miSerial_lbl.setSize(90, 20);
	this.miSerial_lbl.setLabel("Serial Number:");

	this.miSerial_lbl.setDimension( 15, 0);
	this.aligner.rightOf(this.miSerial_lbl.getDisplay() , this.miSerial_txt.getDisplay());
	this.miSerial_txt.setDimension(this.miSerial_txt.getDisplay()._x + 10, 0);
	this.miPList_rbtn = new MppRadioButton(this.miSerialCvs.getContainer(), 3);
	this.miCIndex_rbtn = new MppRadioButton(this.miSerialCvs.getContainer(), 4);
	this.miFsb_rbtn = new MppRadioButton(this.miSerialCvs.getContainer(), 5);
	this.miSearch_btn = new MppButtonAsset(this.miSerialCvs.getContainer(), 6);
	
	this.miSerial_rgroup = new MppRadioGroup();

	this.miSerial_rgroup.addRadioButton(this.miPList_rbtn);
	this.miSerial_rgroup.addRadioButton(this.miCIndex_rbtn);
	this.miSerial_rgroup.addRadioButton(this.miFsb_rbtn);

	this.miPList_rbtn.setDimension(220, 0);
	this.miCIndex_rbtn.setDimension(290, 0);
	this.miFsb_rbtn.setDimension(410, 0);
	this.miCIndex_rbtn.setWidth(120);
	this.miPList_rbtn.setLabel("Pick List");
	this.miCIndex_rbtn.setLabel("Component Index");
	this.miFsb_rbtn.setLabel("FSB/FSC");
	SEGSWindowCoreUIUtil.buttonConfig( this.miSearch_btn);

	this.miPList_rbtn.setSelected(true);
	this.miSearch_btn.setDimension(485, 0);
	this.miSearch_btn.setLabel("Search");

 
//SEARCH --
	
	//this.miPartDesc_txt = new MppText( this.miPartsCvs.getContainer(), 1);
	this.miPartNum_txt = new MppText ( this.miPartsCvs.getContainer(), 2);

	this.miPartDesc_txt.setWidth(170);
	this.miPartNum_txt.setWidth(200);
	this.miPartDesc_txt.visualize();
	this.miPartNum_txt.visualize();

	//this.miPartDesc_lbl = new MppTextLabel( this.miPartsCvs.getContainer(), 3);
	this.miPartNum_lbl  = new MppTextLabel(this.miPartsCvs.getContainer(), 4);

	//this.miPartDesc_rbtn = new MppRadioButton(this.miPartsCvs.getContainer(), 5);
	//this.miPartNum_rbtn = new MppRadioButton(this.miPartsCvs.getContainer(), 6);
	this.miDescSearch_btn = new MppButtonAsset(this.miPartsCvs.getContainer(), 6);
	this.miPartSearch_btn = new MppButtonAsset(this.miPartsCvs.getContainer(), 7);
	//this.miPart_rgroup = new MppRadioGroup();


	this.miPartDesc_lbl.setDimension(15,25);
	this.miPartNum_lbl.setDimension(15, 2);
	this.miPartDesc_lbl.setLabel("Part Description:");
	this.miPartNum_lbl.setLabel("Search for:");
	
	this.miPartDesc_rbtn.setLabel("Part Description");
	this.miPartNum_rbtn.setLabel("Part Number");
	
	this.miPartDesc_rbtn.setWidth(120);
	this.miPartDesc_lbl.setSize(100,20);
	
	this.miPartNum_lbl.setSize(100,20);
 
	
	this.miPartDesc_txt.setDimension(0, 20);
	
	this.aligner.rightOf(this.miPartDesc_lbl.getDisplay(), this.miPartDesc_txt.getDisplay());
	
	this.miPartNum_txt.setDimension (20, 2 ) ;
	
	this.aligner.rightOf(this.miPartNum_lbl.getDisplay(), this.miPartNum_txt.getDisplay());
	this.miPartNum_txt.getDisplay()._x -= 15;
	
	this.miPartDesc_rbtn.setDimension(290,25);
	this.miPartNum_rbtn.setDimension(290,0);

	SEGSWindowCoreUIUtil.buttonConfig(this.miPartSearch_btn);
	SEGSWindowCoreUIUtil.buttonConfig(this.miDescSearch_btn);
	
	
	this.miPartSearch_btn.setLabel("Part Number");
	this.miPartSearch_btn.setDimension( 320 , 2);
	
	this.miDescSearch_btn.setLabel("Description");
	this.miDescSearch_btn.setDimension(420 , 2 );
	
	this.miPart_rgroup.addRadioButton(this.miPartDesc_rbtn);
	this.miPart_rgroup.addRadioButton(this.miPartNum_rbtn);
	
	this.miPartDesc_rbtn.onRelease = function (){
		SEGSWindowCoreUIUtil.machineView.miPartDesc_txt.setEnable(true);
		SEGSWindowCoreUIUtil.machineView.miPartNum_txt.setEnable(false);
		SEGSWindowCoreUIUtil.machineView.miPartNum_txt.setTextValue("");
	}
	this.miPartNum_rbtn.onRelease = function (){
		SEGSWindowCoreUIUtil.machineView.miPartDesc_txt.setEnable(false);
		SEGSWindowCoreUIUtil.machineView.miPartNum_txt.setEnable(true);
		SEGSWindowCoreUIUtil.machineView.miPartDesc_txt.setTextValue("");
	}


//--------BTNS

	this.miAddSelect_btn = new MppButtonAsset( this.miBtnCvs.getContainer(), 1);
	this.miCancel_btn = new MppButtonAsset ( this.miBtnCvs.getContainer(), 2 );
	this.miPrint_btn = new MppButtonAsset ( this.miBtnCvs.getContainer(), 3 );
	this.miShowAll_btn = new MppButtonAsset ( this.miBtnCvs.getContainer() , 5 );
	
	this.miGridNaviCvs = new MppCanvas( this.miBtnCvs.getContainer(), 4);
	this.miNext_btn = new MppButtonAsset( this.miGridNaviCvs.getContainer(),1);
	this.miPrev_btn = new MppButtonAsset( this.miGridNaviCvs.getContainer(),2);
	this.miPage_lbl = new MppTextLabel(  this.miGridNaviCvs.getContainer(),3);

	this.miGridNaviCvs.setSize(300, 25);

	SEGSWindowCoreUIUtil.buttonConfig ( this.miShowAll_btn );
	SEGSWindowCoreUIUtil.buttonConfig( this.miAddSelect_btn);
	SEGSWindowCoreUIUtil.buttonConfig(  this.miCancel_btn);
	SEGSWindowCoreUIUtil.buttonConfig( this.miPrint_btn );
	SEGSWindowCoreUIUtil.buttonConfig(this.miNext_btn);
	SEGSWindowCoreUIUtil.buttonConfig(this.miPrev_btn);


	this.miAddSelect_btn.setLabel("Add to Selection");
	this.miCancel_btn.setLabel("Cancel");
	this.miPrint_btn.setLabel("Printer Friendly");
	
	this.miShowAll_btn.setLabel("Show All");
	this.miShowAll_btn.setSize( 80 , 20 );
	 
	
	this.miNext_btn.setLabel("");
	this.miPrev_btn.setLabel("");

	this.miAddSelect_btn.setSize(120, 20);
	this.miPrint_btn.setSize(120, 20);
	this.miCancel_btn.setDimension(485,0);
	this.aligner.leftOf(this.miCancel_btn.getDisplay(), this.miAddSelect_btn.getDisplay());
	this.miAddSelect_btn.setDimension(this.miAddSelect_btn.getDisplay()._x - 2.5,0);
	this.aligner.leftOf( this.miAddSelect_btn.getDisplay(), this.miPrint_btn.getDisplay());
	this.miPrint_btn.setDimension(this.miPrint_btn.getDisplay()._x - 2.5 , 0);
	
	this.aligner.leftOf( this.miPrint_btn.getDisplay(), this.miShowAll_btn.getDisplay() );
	this.miShowAll_btn.setDimension( this.miShowAll_btn.getDisplay()._x - 2.5 , 0 );
	
	this.miNext_btn.setSize(20,20);
	this.miPrev_btn.setSize(20,20);

	this.miPrev_btn.setDimension(15,0);
	this.miNext_btn.setDimension(40,0);
    
	this.miNext_btn.attachIcon("righticon");
	this.miPrev_btn.attachIcon("lefticon");
	 
	var  right_icon = this.miNext_btn.getIconDisplay ();
	var left_icon = this.miPrev_btn.getIconDisplay();
	
	right_icon._x = 7;
	right_icon._y = 7;
	left_icon._x = 7;
	left_icon._y = 7;
	this.miPrev_btn.alignIcon("center",0);
	this.miNext_btn.alignIcon("center",0);
	
		
	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.miPage_lbl );
	SEGSWindowCoreUIUtil.setLgSLabelProperties ( this.miSerial_lbl );
	this.miPage_lbl.setLabel("1 - 10 of 100");
	//this.miPage_lbl.setDimension( 70, 0);
	this.miPage_lbl.setDimension( 61, 0);


//trace("Add: " +  miAddSelect_btn.getDisplay()._x + " Print: "  + miPrint_btn.getDisplay()._x)

//
//Code Legend:  lg = Label Group | D = Dynamic | S = Static  | Cvs = Canvas
//--------LABELS-----
	this.miLgSerialCvs = new MppCanvas(  this.miLgCvs.getContainer(), 1);
	this.miLgEngineCvs = new MppCanvas(  this.miLgCvs.getContainer(), 2);
	this.miLgDateCvs = new MppCanvas(  this.miLgCvs.getContainer(), 3); // date Canvas

	this.miLgSerialCvs.setSize(570,25);
	/* old size and position
	this.miLgEngineCvs.setSize(570,25);
	this.miLgEngineCvs.setDimension(0, 15);
	*/
	this.miLgEngineCvs.setSize(570,50);
	this.miLgEngineCvs.setDimension(0, 15);
	
	/* old size and position
	this.miLgDateCvs.setSize(570,25);
	this.miLgDateCvs.setDimension(0,30);
    */
	this.miLgDateCvs.setSize(570,25);
	this.miLgDateCvs.setDimension(0,45);
	
	this.miLgSSerial_lbl = new MppTextLabel( this.miLgSerialCvs.getContainer(), 1);
	this.miLgDSerial_lbl = new MppTextLabel(this.miLgSerialCvs.getContainer(), 2);
	this.miLgSModel_lbl = new MppTextLabel( this.miLgSerialCvs.getContainer(), 3);
	this.miLgDModel_lbl = new MppTextLabel( this.miLgSerialCvs.getContainer(), 4);
	this.miLgSOwner_lbl = new MppTextLabel( this.miLgSerialCvs.getContainer(), 5);
	this.miLgDOwner_lbl = new MppTextLabel( this.miLgSerialCvs.getContainer(), 6);

	 
	
	this.miLgSSerial_lbl.setLabel("Serial Number:");
	this.miLgDSerial_lbl.setLabel("000000");
	this.miLgSModel_lbl.setLabel("Model:");
	this.miLgDModel_lbl.setLabel("4DM000");
	this.miLgSOwner_lbl.setLabel("Current Owner: " );
	this.miLgDOwner_lbl.setLabel("N/A" );
	
	this.miLgSSerial_lbl.setDimension(15,0);
	this.miLgDSerial_lbl.setDimension(115,0);
	 
	this.miLgSModel_lbl.setDimension(220,0);
	this.miLgDModel_lbl.setDimension(260,0);
	this.miLgSOwner_lbl.setDimension(400,0);
	this.miLgDOwner_lbl.setDimension(490,0);

	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSSerial_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSOwner_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSModel_lbl);
	
	SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDSerial_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDModel_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDOwner_lbl);

	this.miLgSEngine_lbl = new MppTextLabel( this.miLgEngineCvs.getContainer(), 1);
	this.miLgDEngine_lbl = new MppTextLabel( this.miLgEngineCvs.getContainer(), 2);
	
	this.miLgSDesc_lbl = new MppTextLabel( this.miLgEngineCvs.getContainer(), 3);
	this.miLgDDesc_lbl = new MppTextLabel( this.miLgEngineCvs.getContainer(), 4);
	
	this.miLgSPart_lbl  = new MppTextLabel( this.miLgEngineCvs.getContainer(), 5);
	this.miLgDPart_lbl  = new MppTextLabel( this.miLgEngineCvs.getContainer(), 6);

	this.miLgSEngine_lbl.setLabel("Component Serial #:");
	this.miLgDEngine_lbl.setLabel("000000");
	this.miLgSDesc_lbl.setLabel("Description:");
	this.miLgDDesc_lbl.setLabel("Lorem Ipsum Dolor");
	this.miLgSPart_lbl.setLabel("Part Number:");
	this.miLgDPart_lbl.setLabel("000000");

	this.miLgSEngine_lbl.setDimension(15,0);
	
	this.miLgDEngine_lbl.setDimension(135,0);
	this.miLgSPart_lbl.setDimension(220,0);
	this.miLgDPart_lbl.setDimension(300,0);
	/* old position
	this.miLgSDesc_lbl.setDimension(400,0);
	this.miLgDDesc_lbl.setDimension(470,0);
	*/
	this.miLgSDesc_lbl.setDimension(15,15);
	this.miLgDDesc_lbl.setDimension(95,15);
	
	//SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSEngine_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSDesc_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties( this.miLgSPart_lbl);
	
	SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDEngine_lbl);
	//SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDDesc_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties( this.miLgDPart_lbl);
	
	this.miLgSEngine_lbl.setSize(200,20);
	SEGSWindowCoreUIUtil.setStaticLabelStyles ( this.miLgSEngine_lbl );
	
	this.miLgDDesc_lbl.setSize(500,20);
	SEGSWindowCoreUIUtil.setDynamicLabelStyles ( this.miLgDDesc_lbl );
	
	
	this.miLgSBuild_lbl = new MppTextLabel( this.miLgDateCvs.getContainer(), 1);
	this.miLgDBuild_lbl = new MppTextLabel( this.miLgDateCvs.getContainer(), 2);
	this.miLgSShip_lbl = new MppTextLabel( this.miLgDateCvs.getContainer(), 3);
	this.miLgDShip_lbl = new MppTextLabel( this.miLgDateCvs.getContainer(), 4);
	this.miLgSServ_lbl  = new MppTextLabel( this.miLgDateCvs.getContainer(), 5);
	this.miLgDServ_lbl  = new MppTextLabel( this.miLgDateCvs.getContainer(), 6);

	this.miLgSBuild_lbl.setLabel("Build Date:");
	
	this.miLgDBuild_lbl.setLabel("11/28/81");
	this.miLgSShip_lbl.setLabel("Ship Date:");
	this.miLgDShip_lbl.setLabel("11/28/81");
	this.miLgSServ_lbl.setLabel("In Service Date:");
	this.miLgDServ_lbl.setLabel("11/28/81");


	this.miLgSBuild_lbl.setDimension(15,0);
	this.miLgDBuild_lbl.setDimension(115,0);
	this.miLgSShip_lbl.setDimension(220,0);
	this.miLgDShip_lbl.setDimension(300,0);
	this.miLgSServ_lbl.setDimension(400,0);
	this.miLgDServ_lbl.setDimension(490,0);

	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.miLgSBuild_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.miLgSShip_lbl);
	SEGSWindowCoreUIUtil.setLgSLabelProperties(this.miLgSServ_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties(this.miLgDBuild_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties(this.miLgDShip_lbl);
	SEGSWindowCoreUIUtil.setLgDLabelProperties(this.miLgDServ_lbl);

//-----GRID GROUPS

	this.machine_grid = new MppDataGrid ( this.miGridCvs.getContainer(), 1 );
	this.miResult_lbl = new MppTextLabel( this.miGridCvs.getContainer(), 2);
	this.machine_grid.toolTipOn( false );
	this.machine_grid.setSize(550,190);
	this.machine_grid.setDimension(15,0);
	this.machine_grid.setSpliter( this.grid_spliter);
    SEGSWindowCoreUIUtil.gridStyles ( this.machine_grid );
     
	this.miResult_lbl.setSize(570, 20);
	this.miResult_lbl.setTextAlignment("center");
	this.miResult_lbl.setDimension(0, 90);
	this.miResult_lbl.setWeight(true, false, false);
	this.miResult_lbl.setLabel("No Results Found");
	
	this.machine_grid.onCellPress = this.gridCellPress ;
	
	SEGSWindowCoreUIUtil.radioConfig ( this.miPartDesc_rbtn );
	SEGSWindowCoreUIUtil.radioConfig ( this.miPartNum_rbtn );

	SEGSWindowCoreUIUtil.radioConfig(this.miCIndex_rbtn);
	SEGSWindowCoreUIUtil.radioConfig (this.miPList_rbtn  );
	SEGSWindowCoreUIUtil.radioConfig ( this.miFsb_rbtn  );

	
}
