var MILEvent_Instance = 0;

function MILEvent () {
	MILEvent_Instance++;
	if( MILevent_Instance > 1 ) {
	}else {
		this.LOAD_PICKLIST = "onLoadPickList"; 
		this.LOAD_FSBFSC = "onLoadFsbFsc";
		this.LOAD_COMPONENT_INDEX = "onLoadComponentIndex";
		this.LOAD_ADD_TO_SELECTION= "onLoadAddToSelection";
	}
}

 
MILEvent.prototype.LOAD_PICKLIST;
MILEvent.prototype.LOAD_FSBFSC;
MILEvent.prototype.LOAD_COMPONENT_INDEX;
MILEvent.prototype.LOAD_ADD_TO_SELECTION;

var MILoaderEvent = new MILEvent();



///LOADER


function MachineInfoLoader () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
}
MachineInfoLoader.prototype = new SEDataLoader(); //EXTEND SEDataLoader
MachineInfoLoader.prototype.eventType;
MachineInfoLoader.prototype.createRequestString = function ( param  , type ) {
	trace("MachineInfoLoader.prototype.createRequestString()   _level0.curDataSourceID="+_level0.curDataSourceID);
	var requestString;
	requestString  = "<request>";
	requestString += "<query>";
	//requestString += "<dataSourceId>"+ param.curDataSourceID +"</dataSourceId>";
	requestString += "<dataSourceId>"+ _level0.curDataSourceID +"</dataSourceId>";
	requestString += "<manufacturerId>"+ param.curMfrID +"</manufacturerId>";
	requestString += "<type>" +  type + "</type>" ; //Valid type  FSB_FSC PICK LIST COMPONENT INDEX
	requestString += "<serialnumber>" + param.serialNumber + "</serialnumber>"
	requestString += "</query>";
	requestString += "</request>";
	return requestString;
}

MachineInfoLoader.prototype.loadPickList = function ( targetURL , param) {
	this.loaderXML = new XML();	
	var reqStr = this.createRequestString ( param , "PICK LIST" );
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( reqStr );
	this.eventType = MILoaderEvent.LOAD_PICKLIST;
	this.load( targetURL );
}


MachineInfoLoader.prototype.loadFSB = function ( targetURL , param) {
	this.loaderXML = new XML();	
	var reqStr = this.createRequestString ( param , "FSB_FSC" );
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( reqStr );
	this.eventType = MILoaderEvent.LOAD_FSBFSC;
	this.load( targetURL );
	
}

MachineInfoLoader.prototype.loadComponentIndex = function ( targetURL , param) {
	this.loaderXML = new XML();	
	var reqStr = this.createRequestString ( param , "COMPONENT INDEX" );
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( reqStr );
	this.eventType = MILoaderEvent.LOAD_COMPONENT_INDEX;
	this.load( targetURL );
	
}

MachineInfoLoader.prototype.addToSelection = function ( targetURL  , param  ) {
	this.loaderXML = new XML ();
	var reqStr = "";
	reqStr = "<part-avail mfrgrp=\"\" >";
	reqStr += "<Cust User=\"\" Password=\"\"  AcctNum=\"\" />";
	reqStr += "<part pattern=\"" +  param.partnumber +  "\"   qty=\"" +  param.quantity +  "\"   />";
	reqStr += "</part-avail>";
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( reqStr );
	trace("Add to Selection: " + this.loaderXML );
	this.eventType = MILoaderEvent.LOAD_ADD_TO_SELECTION;
	this.load( targetURL );
}

MachineInfoLoader.prototype.resultLoaded = function () {
	var _event = new Object();
	_event.type = this.eventType;
	_event.resultXML = this.resultXML;
	 
	this.broadcastMessage( this.eventType , _event );
}
 