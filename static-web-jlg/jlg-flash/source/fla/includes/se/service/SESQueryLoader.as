var SQEvent_Instance = 0;


 
function SQEvent () {
	SQEvent_Instance++;
	if( SQEvent_Instance > 1 ){
	} else {
		this.LOAD_SINGLE_LEVEL_BILL = "onLoadSingleLevelBill";
		this.LOAD_SINGLE_LEVEL_WHERE_USED = "onLoadSingleLevelWhereUsed";
	}
}

SQEvent.prototype.LOAD_SINGLE_LEVEL_BILL;
SQEvent.prototype.LOAD_SINGLE_LEVEL_WHERE_USED;
var SQueryLoaderEvent = new SQEvent();

//LOADER

function SESQueryLoader () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	 
}

SESQueryLoader.prototype = new SEDataLoader();
SESQueryLoader.prototype.eventType;

SESQueryLoader.prototype.createRequestString = function ( param , type ) {
	var requestString = "";
	requestString  = "<request>\n";
	requestString += "<query>\n";
	requestString += "<dataSourceId>"+ param.curDataSourceID + "</dataSourceId>";
	requestString += "<manufacturerId>"+ param.curMfrID + "</manufacturerId>";
	requestString += "<type>" + type + "</type>\n"
	requestString += "<partnumber>" + param.partnumber + "</partnumber>\n"
	requestString += "</query>\n";
	requestString += "</request>\n";
	return requestString;
}

SESQueryLoader.prototype.loadSingleLevelBill  = function ( targetURL , param  ) {
	var requestString = this.createRequestString ( param  , "SINGLE LEVEL BILL" );
	this.loaderXML = new XML();
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( requestString );
	this.eventType =  SQueryLoaderEvent.LOAD_SINGLE_LEVEL_BILL;
	 
	this.load( targetURL );
}

SESQueryLoader.prototype.loadSingleLevelWhereUsed  = function ( targetURL , param  ) {
	var requestString = this.createRequestString ( param  ,"SINGLE LEVEL WHERE USED" );
	this.loaderXML = new XML();
	this.loaderXML.ignoreWhite = true;
	this.loaderXML.parseXML( requestString );
	this.eventType = SQueryLoaderEvent.LOAD_SINGLE_LEVEL_WHERE_USED;
	 
	this.load( targetURL );
}

SESQueryLoader.prototype.resultLoaded = function () {
	var _event = new Object();
	_event.type = this.eventType;
	_event.resultXML = this.resultXML;
	this.broadcastMessage( this.eventType , _event );
}

