import mx.events.EventDispatcher;
class includes.seAS2.net.FSBDataLoader {
	
	private var dispatcher:EventDispatcher;
	private var xml:XML;
	
	private var modelsData:Array;
	private var filesData:Array;
	private var pubNum:String;
	private var pubDesc:String;
	private var partsReq:String;
	private var date:String;
	private var warranty:String;
	private var notes:String;
	
	
	private var noResult:Boolean;
	public function FSBDataLoader () {
		this.xml = new XML();
		this.dispatcher = new EventDispatcher();
	}
	
	public function load( targetURL:String , _params:Object ) : Void {
		trace("Loading: " + targetURL );
		var requestStr:String = "" ;
		requestStr = "<request><query>";
			 requestStr += "<dataSourceId>" + _params.dataSourceID + "</dataSourceId>";
			 requestStr += "<manufacturerId>" + _params.mfrID + "</manufacturerId>";
			 requestStr += "<safetyPubNum>" + _params.bulletinNumber + "</safetyPubNum>";
		requestStr += "</query></request>";
		
		this.xml = new XML();
		this.xml.ignoreWhite = true;
		this.xml.parseXML( requestStr );
		//trace( " request detailed xml: " + this.xml ) ;
		var me:FSBDataLoader = this;
		
		this.xml.onLoad = function (success:Boolean) {
			if( success ) {
				 
				me.parse ( me.xml );
				var _event:Object = new Object();
				_event.data = new Object();
				_event.data.xml  = me.xml;
				_event.type = "complete";
				_event.data.noResult = me.noResult;
				 
				_event.data.pubNum = me.pubNum;
				_event.data.pubDesc = me.pubDesc;
				_event.data.partsReq = me.partsReq;
				_event.data.warranty = me.warranty;
				_event.data.date = me.date;
				_event.data.notes = me.notes;
				_event.data.models = me.modelsData;
				_event.data.files = me.filesData;
				me.dispatcher.dispatchEvent( _event );
			}
		}
		this.xml.sendAndLoad(targetURL, this.xml, "POST");
		//this.xml.load( targetURL );
	}
	
	private function parse ( xml:XML  ) : Void {
		 
		if( xml.firstChild.attributes.returntype != "NoResult"   ) {
			 
			this.noResult = false;
			var publication:XMLNode = xml.firstChild.firstChild.firstChild;
			this.pubNum = publication.attributes.number;
			this.pubDesc = publication.attributes.description;
			this.partsReq = publication.attributes.partsReq;
			this.date  = publication.attributes.distributiondate
			this.warranty = publication.attributes.warranty;
		
		
			var child:XMLNode = publication.firstChild;
		
			while ( child != null ) {
				if( child.nodeName == "notes" ) {
					this.notes =  child.firstChild.nodeValue ;
				} else if (child.nodeName == "models" ) {
					var model:XMLNode = child.firstChild;
					this.modelsData = this.xmlNodeToArray( model );
				} else if( child.nodeName == "files" ) {
					var file:XMLNode = child.firstChild;
					this.filesData = this.xmlNodeToArray ( file );
				 
				}
				child =child.nextSibling;
			}
		
		} else {
			 
			this.noResult = true;
			 
		}
	}
	
	
	public function xmlNodeToArray(node:XMLNode ): Array {
			var itemNode:XMLNode = node;
			var items:Array = new Array();
			while ( itemNode != null ) {
				
				var item:Object = new Object()
				item = itemNode.attributes;
				 
				items.push( item );
				itemNode = itemNode.nextSibling;
			}
			return items;
	}
		
	public function deepParse(node:XMLNode, innerArrayName:String  ):Array {
			var itemArray:Array = new Array();
			while ( node != null ){
				var item:Object = node.attributes;
				if( node.hasChildNodes() == true ) {
					var childArray:Array = this.deepParse( node.firstChild);
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
	
	public function addEventListener( type:String , listener:Object ) : Void {
		this.dispatcher.addEventListener( type , listener );
	}
	
	public function removeEventListener ( type:String , listener:Object ) : Void {
		this.dispatcher.removeEventListener( type , listener );
	}
}