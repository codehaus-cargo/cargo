class includes.seAS2.utils.StringClean {
	
	public static function hasSubstring  ( target:String , sub:String  ) {
		var strArr:Array = new Array();
		var boolReturn:Boolean = false;
		target = target.toString();
		target = target.toLowerCase();
		sub = sub.toLowerCase();
		strArr = target.split(sub);
		if( strArr.length > 1 ) {
			boolReturn = true;
		}
		return boolReturn;
	}
}