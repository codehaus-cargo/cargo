#include "../SECoreService.as"
var FsbEvent = new Object();
FsbEvent.LOAD_DETAIL = "onLoadDetail";

function FSBService () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
}

FSBService.prototype = new SEDataLoader();

FSBService.prototype.attributesToObject = function ( node ) {
	var obj = new Object();
	for ( var prop in node.attributes ){
		obj[prop] = node.attributes[prop];
	}
	return obj;
}

FSBService.prototype.loadDetailedInfo = function( targetURL , params ) {
	this.loaderXML = new XML();
	this.loaderXML.ignoreWhite = true;
	this.eventType = FsbEvent.LOAD_DETAIL ;
	this.load( targetURL);	
}

FSBService.prototype.resultLoaded = function () {
	var _param = new Object();
	var attr = this.attributesToObject ( this.resultXML.firstChild.firstChild);
	_param.data = attr;
	_param.resultXML = this.resultXML;
	_param.type = this.eventType;
	this.broadcastMessage( this.eventType , _param);	
}