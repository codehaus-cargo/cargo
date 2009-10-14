function search(offset, srchlimit) {
	var e;
	var i;
	var j;
	var cond;
	 
	var mfrgroup;
	if (popupMC.companyCB._visible == true) {
		mfrgroup = popupMC.companyCB.dropdown.getSelectedItem().label;
	}
	_level300.mouseWait();
	searchXML = new XML();
 	//**************************************************
	//INSERT NEW VENDOR ADDITION WITHIN THIS CONSTRUCTION
	//**************************************************
	 var requestString = "<global-search ";
	requestString += "mfrgrp=\"" + _level0.machineInfoLevel.searchParam.mfrgroup + "\" ";
	requestString += "offset=\"" +  offset + "\" ";
	requestString += "srchlimit=\"" +  srchlimit + "\" >";
	requestString += "<region>" + _level0.machineInfoLevel.searchParam.region + "</region>";
	requestString += "<Option>" + _level0.machineInfoLevel.searchParam.Option + "</Option>";
	//DO THE LAZY PARSE
	 for ( var ctr = 1 ; ctr <= 6 ; ctr++ ) {
		requestString += "<Qual" + ctr + ">" + _level0.machineInfoLevel.searchParam["Qual" + ctr] +  "</Qual" + ctr + ">";
		requestString += "<Cond" + ctr + ">" + _level0.machineInfoLevel.searchParam["Cond" + ctr] +  "</Cond" + ctr + ">";
		requestString += "<Param" + ctr + ">" + _level0.machineInfoLevel.searchParam["Param" + ctr] +  "</Param" + ctr + ">";
		requestString += "<Bool" + ctr + ">" + _level0.machineInfoLevel.searchParam["Bool" + ctr] +  "</Bool" + ctr + ">";
	}
	
	requestString += "</global-search>"; 
	 
	
	
	searchXML.parseXML( requestString );
	_level0.searchResponseXML = new XML();
	_level0.searchResponseXML.onLoad = function(success) {
		if (success) {
			_level0.checkForError(_level0.searchResponseXML);
			_level0.lastResultSelected = undefined;
			_level0.lastResultSelectedIndex = undefined;
			if (_level0.searchResponseXML.firstChild.firstChild.attributes.error.length > 1) {
				_level300.popup(_level0.searchResponseXML.firstChild.firstChild.attributes.error, "Search Warning");
			} else {
				if (_level292._width > 0) {
					_level292.searchResultsMC.searchResultsMC.functionDupMC(_level0.searchResponseXML.firstChild.childNodes);
				} else {
					loadMovieNum("searchResults2.swf", 292);
				}
			}
		} else {
			_level0.messageBox = "Loading failed";
		}
		_level300.mouseNormal();
	};
	searchXML.sendAndLoad(_level0.baseURL add "servlet/RequestDirector?helper=GlobalSearch", _level0.searchResponseXML);
}