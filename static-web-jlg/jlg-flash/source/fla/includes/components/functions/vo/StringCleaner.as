function StringCleaner() {
	
}
StringCleaner.prototype.rightTrim = function(strValue) {
	//if (strValue instanceof String) {
	
		var index:Number;
		var textArray:Array = new Array();
		textArray = strValue.split("");
		strValue = "";
		for (index=textArray.length-1; index>=0; index--) {
			if (textArray[index] == " ") {
				textArray[index] = "";
			} else {
				break;
			}
		}
		for (index=0; index<textArray.length; index++) {
			strValue += textArray[index];
		}
	//}
	return strValue;
};
StringCleaner.prototype.leftTrim = function(strValue) {
	//if (strValue instanceof String) {
		var index = 0;
		var textArray = new Array();
		textArray = strValue.split("");
		strValue = "";
		for (index=0; index<textArray.length; index++) {
			if (textArray[index] == " ") {
				textArray[index] = "";
			} else {
				break;
			}
		}
		for (index=0; index<textArray.length; index++) {
			strValue += textArray[index];
		}
	//}
	return strValue;
};
StringCleaner.prototype.trim = function(strValue) {
	strValue = this.rightTrim(strValue);
	strValue = this.leftTrim(strValue);
	return strValue;
};

StringCleaner.prototype.hasSubstring = function ( target, sub ) {
	var strArr = new Array();
	var boolReturn = false;
	target = target.toString();
	target = target.toLowerCase();
	sub = sub.toLowerCase();
	strArr = target.split(sub);
	if( strArr.length > 1 ) {
		boolReturn = true;
	}
	return boolReturn;
}


StringCleaner.prototype.equalAtStart  = function ( target , sub ) {
	var targetArr = new Array();
	var boolReturn = false;
	var subArr  = new Array();
	subArr = sub.split("");
	target = target.toString();
	targetArr = target.split("");
	 
	var sublen = subArr.length;
	var newStr = "";
	for ( var i = 0; i < sublen ; i++ ) {
		 newStr +=  targetArr[i] ;
	}
	 
	
	return this.equalString ( newStr , sub );
}

StringCleaner.prototype.replaceSubstring = function ( strText, target ,  replacement ) {
		strText =  strText.toString();
		target = target.toString();
		replacement =  replacement.toString();
		var textArray  = strText.split( target );
		var index  = 0;
		strText = textArray[0];
		for(index = 1; index < textArray.length; index++){
			strText +=  replacement + textArray[index];
		}
		return strText;
}

StringCleaner.prototype.equalString = function  ( target, sub ) {
	var boolReturn = false;
	target = target.toString();
	target = target.toLowerCase();
	sub = sub.toLowerCase();
	sub = this.trim(sub);
	target = this.trim(target);
	if( target == sub ) {
		boolReturn = true;
	}
	return boolReturn;
}

StringCleaner.prototype.formalNameString = function ( target ) {
	var partsArr = new Array();
	var index = 0;
	target = this.trim(target.toString());
	target = target.toLowerCase();
	partsArr = target.split(" ");
	for(index = 0; index < partsArr.length; index++ ) {
		var charArr = new Array();
		partsArr[index] =  this.trim(partsArr[index]);
		charArr = partsArr[index].split("");
		charArr[0] = charArr[0].toUpperCase();
		var partString = "";
		for(var chCtr = 0; chCtr < charArr.length; chCtr++){
			partString += charArr[chCtr];
		}
		partsArr[index] = partString;
	}
	var newStr = "";
	for(index = 0; index < partsArr.length; index++) {
		if( partsArr[index] != "" ){
			newStr += partsArr[index] + " ";
		}
	}
	target = this.trim(newStr);
	return target;
}

StringCleaner.prototype.reverseString = function ( target ) {
	var strArr = new Array();
	strArr = target.split("");
	strArr.reverse();
	target = "";
	for(var index = 0; index < strArr.length; index++){
		target += strArr[index];
	}
	return target;
}



var StringUtil = new StringCleaner();