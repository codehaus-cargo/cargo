var SELoader_Instance = 0;
function SmartEquipLoaderEvent () {
	SELoader_Instance++;
	if( SELoader_Instance > 1 ) {//SINGLETON
	} else {
		this.IOError = "onIOError";
		this.DATA_LOADED = "onLoadData";
	}
}
SmartEquipLoaderEvent.prototype.DATA_LOADED;
SmartEquipLoaderEvent.prototype.IOError;
var SELoaderEvent  = new SmartEquipLoaderEvent();

//SE DATALOADER -
function SEDataLoader () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.resultXML = new XML();
	this.resultXML.ignoreWhite = true;
	
	this.loaderXML = new XML();
	this.loaderXML.ignoreWhite = true;
	
	this._event = new Object();
	 
}
SEDataLoader.prototype._event;
SEDataLoader.prototype.loaderXML;
SEDataLoader.prototype.resultXML;
SEDataLoader.prototype.me;
SEDataLoader.prototype.load = function ( targetURL ) {
	var me = this;
	this.resultXML = new XML();
	this.resultXML.ignoreWhite = true;
	this.resultXML.onLoad = function ( success ){
		if( success ){ 
			trace("SEDataLoader.prototype.load()   _level0.checkForError(me.resultXML)")
			_level0.checkForError(me.resultXML)
			me.resultLoaded( );
		} else {
			 
			me.ioError( targetURL );
		}
	}
	this.loaderXML.sendAndLoad( targetURL , this.resultXML    );
}
SEDataLoader.prototype.parseResult = function ( xml  ){}
SEDataLoader.prototype.resultLoaded = function () {
		//trace("SEDataLoader.prototype.resultLoaded()")
		_event = new Object();
		_event.resultXML = this.resultXML;
		_event.type = SELoaderEvent.DATA_LOADED;
		this.parseResult(this.resultXML);
		this.broadcastMessage( SELoaderEvent.DATA_LOADED, _event );
}

SEDataLoader.prototype.ioError = function ( errorURL) {
	_event = new Object();
	_event.type = SELoaderEvent.IOError;
	_event.errorURL = errorURL;
	_event.message = "Cannot locate \"" + errorURL + "\".";
	trace( _event.message );
	this.broadcastMessage( SELoaderEvent.IOError, _event );
}

SEDataLoader.prototype.deepParse  = function ( node , innerArrayName ) { //Return Type /Array
	var itemArray  = new Array();
		while ( node != null ){
			var item  = node.attributes;
			if( node.hasChildNodes() == true ) {
				var childArray  = this.deepParse( node.firstChild);
				if (innerArrayName == null ){
					innerArrayName = node.firstChild.nodeName;
				}	
					item[innerArrayName] = childArray;
				}
			itemArray.push(item );
			node = node.nextSibling;
		}
	return itemArray;
}

SEDataLoader.prototype.xmlNodeToArray = function ( node )  { //RETURN TYPE Array 
	var itemNode  = node;
	var items  = new Array();
	while ( itemNode != null ) {
		var item:Object = itemNode.attributes;
		items.push( item );
		itemNode = itemNode.nextSibling;
	}
	return items;
}