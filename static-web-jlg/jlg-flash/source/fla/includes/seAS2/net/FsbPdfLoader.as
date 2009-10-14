
import includes.seAS2.net.DataLoader;

class includes.seAS2.net.FsbPdfLoader extends DataLoader{
	
	public function FsbPdfLoader () {
		super();
	}
	
	 
	public function loadPDFPath ( targetURL:String , _params:Object ) : Void {
		var requestStr:String = "" ;
		requestStr = "<request><query>";
			 requestStr += "<dataSourceId>" + _params.dataSourceID + "</dataSourceId>";
			 requestStr += "<manufacturerId>" + _params.mfrID + "</manufacturerId>";
			 requestStr += "<filename>" + _params.filename + "</filename>";
		requestStr += "</query></request>";
		//trace( "XML: " + this.xml );
		this.load(targetURL , requestStr );
		
	}
	 
	//override
	public function parse(_xml:XML ) : Void {
		//trace("parsing" + _xml );
		this._parsedData = new Object();
		var node:XMLNode = _xml.firstChild;
		this._parsedData.filepath = node.attributes.filepath ;
		this._parsedData.type = "parseComplete";
		this.dispatcher.dispatchEvent( this._parsedData );
	}
}