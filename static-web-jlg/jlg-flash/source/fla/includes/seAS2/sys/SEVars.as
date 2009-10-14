class includes.seAS2.sys.SEVars{
	public static var baseURL:String;
	public static var requestDirector:String;
	public static var pdfURL:String;
	
	public static var domainURL:String;
	
	public static var dataSourceID:String;
	public static var mfrID:String;
	// _level0.curDataSourceID
	//_level0.curMfrID
	public static function setVars() : Void {
		trace("BaseURL: " + _level0.baseURL );
		SEVars.baseURL = _level0.baseURL;
		SEVars.dataSourceID = _level0.curDataSourceID;
		SEVars.mfrID = _level0.curMfrID;
		SEVars.domainURL = SEVars.generateDomainURL( SEVars.baseURL);
		SEVars.requestDirector = _level0.baseURL + "servlet/RequestDirector?helper=";
		 
	}
	
	//http://192.168.201.17/jlg-se/
	public static function generateDomainURL( _targetString:String ):String {
		 
		var target:String =   _targetString;
		var URLARR:Array = new Array();
		 
		URLARR = target.split("//");
		 
		var full:String = URLARR[1];
		 
		URLARR = new Array();
		URLARR = full.split("/");
		//return "http://" + URLARR[0] ;
		return "http://" + URLARR[0] + "/"; 
	}
	//http://lead.jlg.com/jlgr4/
}