import mx.events.EventDispatcher;

import includes.seAS2.utils.StringClean;

class includes.seAS2.net.DataLoader {
	
	public var dispatcher:EventDispatcher;
	public var xml:XML;
	
	public var _parsedData:Object;
	public var _type:String;
	
	public function DataLoader () {
		this._parsedData = new Object();
		this.xml = new XML();
		this.dispatcher = new EventDispatcher();
		this._type = "complete";
	}
	//overridable
	public function load( targetURL:String , _source:String   ) : Void {
		var me:DataLoader = this;
		this.xml = new XML ();
		this.xml.ignoreWhite = true;
		if( _source != "" ) {
			this.xml.parseXML( _source );
		}
		
		this.xml.onLoad = function ( success:Boolean ) {
			me.parse( me.xml );
			var _event:Object = new Object();
			_event.type =  this._type;
			_event.data = me.xml ;
			me.dispatcher.dispatchEvent( _event);
			
		}
		if( StringClean.hasSubstring( targetURL , ".txt" ) || StringClean.hasSubstring("targetURL", ".xml" ) ) {
			 
			this.xml.load( targetURL  );
		} else {
			this.xml.sendAndLoad( targetURL, this.xml, "POST" );
		}
	}
	//overridable
	public function parse( _xml:XML ) : Void {
		//do some parsing;
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