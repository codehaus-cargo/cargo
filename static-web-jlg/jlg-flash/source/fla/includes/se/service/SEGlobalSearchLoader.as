
//GLOBAL SEARCH SERVICES AND RELATED EVENTS
var GSEvents_ID = 0;
function GSEvents () {
	GSEvents++;
	if( GSEvents_ID > 1 ){
	} else {
		this.SEARCH_LOADED = "onSearchLoaded";
	}
}


GSEvents.prototype.SEARCH_LOADED;
var GlobalSearchLoaderEvent = new GSEvents();

///---------------------------------LOADER

function GlobalSearchLoader (){
	AsBroadcaster.initialize(this);
	this.addListener(this);
}
GlobalSearchLoader.prototype = new SEDataLoader(); //EXTEND SEDataLoader

GlobalSearchLoader.prototype.loadSearch = function (targetURL , param ) {
	trace("GlobalSearchLoader.prototype.loadSearch()")
	
	_level0.globalSearchParam = param;
	
	this.loaderXML = new XML();
	this.loader.ignoreWhite = true;
	//GENERATE THE XML REQUEST FOR GLOBAL SEARCH
	var requestString = "<global-search ";
	
	if(param.mfrgroup==undefined) param.mfrgroup="";
	if(param.offset==undefined) param.offset="";
	if(param.srchlimit==undefined) param.srchlimit="";
		
	
	requestString += "mfrgrp=\"" + param.mfrgroup + "\" ";
	requestString += "offset=\"" + param.offset + "\" ";
	requestString += "srchlimit=\"" + param.srchlimit + "\" >";
	requestString += "<region>" + param.region + "</region>";
	requestString += "<Option>" + param.Option + "</Option>";
	//DO THE LAZY PARSE
	 for ( var ctr = 1 ; ctr <= 6 ; ctr++ ) {
		requestString += "<Qual" + ctr + ">" + param["Qual" + ctr] +  "</Qual" + ctr + ">";
		requestString += "<Cond" + ctr + ">" + param["Cond" + ctr] +  "</Cond" + ctr + ">";
		requestString += "<Param" + ctr + ">" + param["Param" + ctr] +  "</Param" + ctr + ">";
		requestString += "<Bool" + ctr + ">" + param["Bool" + ctr] +  "</Bool" + ctr + ">";
	}
	
	requestString += "</global-search>";
	this.loaderXML.parseXML( requestString );
	trace("loaderXML: " + this.loaderXML );
	this.load(targetURL);
}

GlobalSearchLoader.prototype.resultLoaded = function () { //OVERRIDE resultLoaded of SEDataLoader
	trace("GlobalSearchLoader.prototype.resultLoaded()")
	this._event = new Object();
	this._event.resultXML = this.resultXML;
	this._event.type = GlobalSearchLoaderEvent.SEARCH_LOADED;
	this.parseResult(this.resultXML);
	this.broadcastMessage( GlobalSearchLoaderEvent.SEARCH_LOADED ,this._event );
}