function Level0Cache()
{
	if (!_level0.ppID)
	{ 
		_level0.ppID = new Array();
	}	

	if (!_level0.pmfrID) 
	{
		_level0.pmfrID = new Array();
	}
	
	if (!_level0.pDesc) 
	{
		_level0.pDesc = new Array();
	}
	
	if (!_level0.pQty) 
	{
		_level0.pQty = new Array();
	}
	
	if (!_level0.pdsrc)
	{
		_level0.pdsrc = new Array();
	}

	if (!_level0.partsList)
	{
		_level0.partsList = new Array();
	}

	if (!_level0.pdispList) 
	{
		_level0.pdispList = new Array();
	}

	if (!_level0.pMC)
	{
		_level0.pMC = new Array();
	}

	if (!_level0.pComponent)
	{
		_level0.pComponent = new Array();
	}
	
	if ((_level0.trolleyList == undefined) || (_level0.trolleyList == null))
	{
		_level0.trolleyList = "";
	}
}

Level0Cache.prototype.initLists = function() 
{
	_level0.ppID.splice(0);
	_level0.pmfrID.splice(0);
	_level0.pDesc.splice(0);
	_level0.pQty.splice(0);
	_level0.pdsrc.splice(0);
	_level0.partsList.splice(0);
	_level0.pdispList.splice(0);
	_level0.pMC.splice(0);
	_level0.pComponent.splice(0);
	delete _level0.trolleyList;
}

Level0Cache.prototype.getPnameArr = function() 

{
	return(_level0.ppID);

}

Level0Cache.prototype.getPnumArr = function() 
{
	return(_level0.pmfrID);

}

Level0Cache.prototype.getDescArr = function() 
{
	return(_level0.pDesc);

}

Level0Cache.prototype.getQtyArr = function() 
{
	return(_level0.pQty);

}

Level0Cache.prototype.getDsrcArr = function() 
{
	return(_level0.pdsrc);

}

Level0Cache.prototype.getPlArr = function() 
{
	return(_level0.partsList);

}


Level0Cache.prototype.getDispArr = function() 
{
	return(_level0.pdispList);

}

Level0Cache.prototype.getMcArr = function() 
{
	return(_level0.pMC);

}

Level0Cache.prototype.getCompArr = function() 
{
	return(_level0.pComponent);

}

Level0Cache.prototype.getTrolley = function() 
{
	return(_level0.trolleyList);
}

Level0Cache.prototype.add2Trolley = function(s) 
{
	_level0.trolleyList += (s + newline);
}

Level0Cache.prototype.addLineItem = function(an_obj) 
{	
	var disp = (an_obj.mc == "") ? "+ " : (SeUtil.removeBrackets(an_obj.mc) + "- ");
	disp += an_obj.pdesc;
	this.getPnumArr().push(an_obj.pnum);
	this.getPnameArr().push(an_obj.pname);
	this.getDescArr().push(an_obj.pdesc);
	this.getQtyArr().push(Number(an_obj.qty));
	this.getDsrcArr().push(an_obj.dsrc);
	this.getPlArr().push(an_obj.pl);
	this.getDispArr().push(disp);
	this.getMcArr().push(an_obj.mc);
	this.getCompArr().push(an_obj.comp);
	this.add2Trolley(disp);
	this.traceAll();
}


Level0Cache.prototype.updLineItem = function(indx, an_obj) 
{
	this.getPnumArr()[indx] = an_obj.pnum;
	this.getPnameArr()[indx] = an_obj.pname;
	this.getDescArr()[indx] = an_obj.pdesc;
	this.getQtyArr()[indx] = Number(an_obj.qty);
	this.getDsrcArr()[indx] = an_obj.dsrc;	
	this.getPlArr()[indx] = an_obj.pl;
	this.getDispArr()[indx] = an_obj.disp;
	this.getMcArr()[indx] = an_obj.mc;
	this.getCompArr()[indx] = an_obj.comp;
}

Level0Cache.prototype.delLineItem = function(indx) 
{
	this.getPnumArr().splice(indx, 1);
	this.getPnameArr().splice(indx, 1);
	this.getDescArr().splice(indx, 1);
	this.getQtyArr().splice(indx, 1);
	this.getDsrcArr().splice(indx, 1);	
	this.getPlArr().splice(indx, 1);
	this.getDispArr().splice(indx, 1);
	this.getMcArr().splice(indx, 1);
	this.getCompArr().splice(indx, 1);
	this.updTrolleyList();
	this.traceAll();
}

Level0Cache.prototype.insLineItem = function(indx, an_obj) 
{
	this.getPnumArr().splice(indx, 0, an_obj);
	this.getPnameArr().splice(indx, 0, an_obj);
	this.getDescArr().splice(indx, 0, an_obj);
	this.getQtyArr().splice(indx, 0, an_obj);
	this.getDsrcArr().splice(indx, 0, an_obj);	
	this.getPlArr().splice(indx, 0, an_obj);
	this.getDispArr().splice(indx, 0, an_obj);
	this.getMcArr().splice(indx, 0, an_obj);
	this.getCompArr().splice(indx, 0, an_obj);
}

Level0Cache.prototype.updTrolleyList = function() 
{
	delete _level0.trolleyList;
	var disp_arr = this.getDispArr();
	for (var i = 0; i < disp_arr.length; i++)
	{
		_level0.trolleyList += disp_arr[i] + newline;
	}
}

Level0Cache.prototype.addLineItems = function(li_arr) 
{
	for ( var i = 0; i < li_arr.length ; i++ ) 
	{
		this.addLineItem(i, li_arr[i]);
	}
}



Level0Cache.prototype.updLineItems = function(li_arr) 
{
	for ( var i = 0; i < li_arr.length ; i++ ) 
	{
		this.updLineItem(i, li_arr[i]);
	}
}

Level0Cache.prototype.upDateTListDisplay = function() 
{
	var i;
	var thisDesc;
	var thisRef;
	_level0.trolleyList = "";
	for (i=0; i < _level0.partsList.length; i++) {
		_level213.clickedArray[_level0.partsList[i]] = 1;
		_level0.trolleyList = _level0.trolleyList + _level0.pdispList[i];
	}
}

Level0Cache.prototype.clearAll = function()
{
	this.getPnumArr().splice(0);
	this.getPnameArr().splice(0);
	this.getDescArr().splice(0);
	this.getQtyArr().splice(0);
	this.getDsrcArr().splice(0);	
	this.getPlArr().splice(0);
	this.getDispArr().splice(0);
	this.getMcArr().splice(0);
	this.getCompArr().splice(0);
	_level0.trolleyList = "";
}

Level0Cache.prototype.flushAll = function(li_arr)
{
	this.addLineItems(li_arr);
}

Level0Cache.prototype.addToAllLists = function(info_obj) 
{	
	this.getPlArr().push(info_obj.indx);
	_level0.pdispList.push(info_obj.line);
	_level0.pDesc.push(info_obj.pDesc);
	_level0.pmfrID.push(info_obj.pmfrID);
	_level0.pQty.push(info_obj.pQty);
	_level0.pdsrc.push(info_obj.pdsrc);
	_level0.ppID.push(ifo_obj.ppID);
	_level0.pMC.push(info_obj.pMC);
	_level0.pgDesc.push(info_obj.pgDesc);
	_level0.pComponent.push(info_obj.pComponent);
	_level0.pVendNo.push(info_obj.pVendNo);
	_level0.trolleyList += info_obj.line;
	eval("_level209.schematicClip." + mc).clicked = (_level0.partsList.length-1);
}

Level0Cache.prototype.addToPartLists = function(info_obj) 
{
	_level0.partsList.push(info_obj.indx);
	_level0.pMC.push(info_obj.pMC);
	_level0.pdispList.push(info_obj.line);
	_level0.pDesc.push(info_obj.pDesc);
	_level0.pmfrID.push(info_obj.pmfrID);
	_level0.pQty.push(info_obj.pQty);
	_level0.pdsrc.push(info_obj.pdsrc);
	_level0.ppID.push(ifo_obj.ppID);
	_level0.pgDesc.push(info_obj.pgDesc);
	_level0.pComponent.push(info_obj.pComponent);
	_level0.pVendNo.push(info_obj.pVendNo);
	_level0.trolleyList += info_obj.line;
}

Level0Cache.prototype.addToMCList = function(mc) 
{
	_level0.pMC.push(mc);
}

Level0Cache.prototype.delFromLists = function( indx, count) 
{
	var i;
	var thisMC;
	var j;
	if ( indx < 0 ) indx = 0;	
	for( i = 0; i < _level213.xmlArrayLength; i++ ) 
	{		
		if ( _level213.manufacturerArray[i].attributes.pname == _level0.ppID[indx] ) 
		{
			for ( j = i;  _level213.manufacturerArray[j].attributes.pname == _level0.ppID[indx]; j++ ) { 
				_level213.qtyrequired.splice(i,1);
				_level213.qtavl.splice(i,1);
			}
			break;
		}
	}

	_level0.partsList.splice(indx, count);
	_level0.pdispList.splice(indx, count);
	_level0.pmfrID.splice(indx, count);
	_level0.pdsrc.splice(indx, count);
	_level0.ppID.splice(indx, count);
	_level0.pDesc.splice(indx, count);
	_level0.pQty.splice(indx, count);
	_level0.pMC.splice(indx, count);
	_level0.pgDesc.splice(indx, count);
	_level0.pComponent.splice(indx, count);

	for ( i = indx ; i < _level0.partsList.length ; i++ ) 
	{
		if ( _level0.pMC[i].length > 0)
		{
			if ( _level0.pComponent[i] == (_level0.curMfrID + 
				_level0.curManualType + 
				_level0.curModel + 
				_level0.curComponentID) ) 
			{
				thisMC = eval("_level209.schematicClip." + _level0.pMC[i]);
				thisMC.clicked = thisMC.clicked - count;
		   		while ( thisMC._name == _level0.pMC[i+1] ) 
					i++;
			}
		}
	}
}

Level0Cache.prototype.traceAll = function()
{
	traceArr(_level0.pmfrID, "_level0.pmfrID");
	traceArr(_level0.ppID, "_level0.ppID");
	traceArr(_level0.pDesc, "_level0.pDesc");
	traceArr(_level0.pQty, "_level0.pQty");
	traceArr(_level0.pdsrc, "_level0.pdsrc");
	traceArr(_level0.partsList, "_level0.partsList");
	traceArr(_level0.pdispList, "_level0.pdispList");
	traceArr(_level0.pMC, "_level0.pMC");
	traceArr(_level0.pComponent, "_level0.pComponent");
}