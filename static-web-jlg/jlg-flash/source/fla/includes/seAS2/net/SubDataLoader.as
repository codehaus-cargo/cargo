
import includes.seAS2.net.DataLoader;

class includes.seAS2.net.SubDataLoader extends DataLoader{
	
	public function SubDataLoader () {
		 
		super();
	}
	
	 
	//AS2 has no override key word
	public function parse(_xml:XML ) : Void {
		 this._parsedData = new Object();
		 this._parsedData.type = "parseComplete";
		this.dispatcher.dispatchEvent( this._parsedData );
		 
	}
}