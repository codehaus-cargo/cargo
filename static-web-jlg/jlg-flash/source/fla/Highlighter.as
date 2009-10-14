function Highlighter(t)
{
	this.trgt = t;
	this.initialized = false;
	this.toggle = 0;
	this.searchPart_arr = new Array();
	this.backColor_arr = null;
	this.mcColor_arr = null;
	this.cache = new Level0Cache();
	this.initColors();
}

Highlighter.prototype.getTarget = function()
{
	return(this.trgt);	
}

Highlighter.prototype.setTarget = function(t)
{
	this.trgt = t;	
}

Highlighter.prototype.getPartsArr = function()
{
	return(_level213.partsListArray);
}

Highlighter.prototype.getCurComp = function()
{
	return(_level0.curMfrID + _level0.curManualType + _level0.curModel  + _level0.curComponentID);
}

Highlighter.prototype.isInitialized = function()
{
	return(this.initialized);
}

Highlighter.prototype.getSearchPart = function()
{
	return(_level213.globalSearchPart);	
}

Highlighter.prototype.setSearchPart = function(p)
{
	_level213.globalSearchPart = p;
}

Highlighter.prototype.delSearchPart = function(p)
{
	delete _level213.globalSearchPart;
}

Highlighter.prototype.getSearchState = function()
{
	return(_level213.globalSearchState);
}

Highlighter.prototype.setSearchState = function(n)
{
	_level213.globalSearchState = n;
}

Highlighter.prototype.initColors = function(p)
{
	if ( _level0.colorCH == "1" ) 
	{
		this.backColor_arr = new Array( 0xFFFFFF,0x636563);
		this.mcColor_arr = new Array ( new Array( 0x000000,0xFF0000), new Array(0xFFFFFF,0xFFFF00) );
	} 
	else 
	{
		this.backColor_arr = new Array( 0x636563, 0xFFFFFF );
		this.mcColor_arr = new Array ( new Array( 0xFFFFFF,0xFFFF00), new Array(0x000000,0xFF0000) );
	}
}

Highlighter.prototype.highlightPart = function(theClicked)
{
	theClicked.textMC.gotoAndStop(2);
	theClicked.textMC2.gotoAndStop(2);
	theClicked.textMC3.gotoAndStop(2);
	theClicked.textMC4.gotoAndStop(2);
	theClicked.textMC5.gotoAndStop(2);
}

Highlighter.prototype.highlightParts = function()
{
	var comp = this.getCurComp();	
	for (var i = 0; i < _level0.partsList.length ; i++) 
	{
		if (_level0.pComponent[i] == comp) 
		{
			var dup_mc = eval(this.trgt + ".dupMC" + _level0.partsList[i]);
			this.highlightPart(dup_mc);	
		}
	}
}

Highlighter.prototype.isAggregateMC = function(s) 
{
	return((s.indexOf("{+") != -1)? true: false);

}

Highlighter.prototype.markClicked = function(indx) 
{
	_level213.clickedArray[indx] = 1;
}

Highlighter.prototype.addToLists = function(info_obj) 
{
	for (var i = 0; i < info_obj.mc_arr.length; i++)
	{
		_level100.addToLists(info_obj.indx, info_obj.mc_arr[i]);
	}
	this.markClicked(info_obj.indx);
}

Highlighter.prototype.handleParts = function() 
{
	if (this.searchPart_arr.length < 1)
	{
		return(false);
	}
	for (var i = 0; i < this.searchPart_arr.length; i++)
	{
		this.addToLists(this.searchPart_arr[i]);		
	}
	return(true);
}

Highlighter.prototype.findParts = function() 
{
	this.searchPart_arr.splice(0);
	var prt = this.getSearchPart();	
	for (var i = 0; i < _level213.partsListArray.length ; i++) 
	{
		if (_level213.partsListArray[i].attributes.f3 == prt)
		{
			var info_obj = new Object();
			info_obj.indx = i;

			var isAgg = this.isAggregateMC(_level213.partsListArray[i].attributes.mcn);
			if (isAgg)
			{
				info_obj.mc_arr = new Array();
				info_obj.mc_arr.push(_level213.partsListArray[i].attributes.mcn);
			}
			else
			{
				info_obj.mc_arr = _level213.partsListArray[i].attributes.mcn.split(",");				
			}

			this.searchPart_arr.push(info_obj);
		}
	}
}

Highlighter.prototype.mcClicked = function(mc_arr) 
{
	for (var i = 0; i < mc_arr.length; i++)
	{
		var mc_str = "_level209.schematicClip." + mc_arr[i];

		var mc = eval(mc_str);
		if (! mc)
		{
			trace("ERROR: Highlighter.prototype.mcClicked(): mc = " + mc);	
		}
		mc.clicked = 1;
	}
}

Highlighter.prototype.handleMCs = function() 
{
	if (this.searchPart_arr.length < 1)
	{
		trace("ERROR: handleMCs() : There are no parts to handle");
		return(false);
	}
	for (var i = 0; i < this.searchPart_arr.length; i++)
	{
		this.mcClicked(this.searchPart_arr[i].mc_arr);		
	}
	this.initialized = true;
	return(true);
}


Highlighter.prototype.highlightMC = function(mc, state, len) 
{
	var theMC = eval("_level209.schematicClip." + mc);
	var c = new Color (theMC);
	if ( state == 0 ) 
	{
		eval(theMC).clicked = null;
	} 
	else if ( state == 1 ) 
	{
		eval(theMC).clicked = len;
	}
	c.setRGB(this.mcColor_arr[this.toggle][state]); // Reset mc color
}

Highlighter.prototype.highlightMCs = function() 
{
	for (var i = 0; i < this.searchPart_arr.length; i++)
	{
		for (var j = 0; j < this.searchPart_arr[i].mc_arr.length; j++)
		{
			var mc = this.searchPart_arr[i].mc_arr[j];
			this.highlightMC(mc, 1);
		}		
	}
}

Highlighter.prototype.getPartInfo = function(indx)
{
	var info_obj = new Object();
	var indent = null;
	var the2Chars = null;
	info_obj.ref_str = _level213.partsListArray[indx].attributes.f2;

	indent = _level213.partsListArray[indx].attributes.idnt ;
	for ( the2Chars = "";  indent-- >= 1;   the2Chars = the2Chars + " " ); 
	if ( _level213.partsListArray[indx].attributes.bold == "Y" ) 
	{

		info_obj.desc_str = the2Chars + "<b>" + _level213.partsListArray[indx].attributes.f1 + "</b>";
	} 
	else 
	{
		info_obj.desc_str = the2Chars + _level213.partsListArray[indx].attributes.f1;
	}
	info_obj.partNum_str = _level213.partsListArray[indx].attributes.f3;
	info_obj.qty_str = _level213.partsListArray[indx].attributes.f4;
	info_obj.rev_str = _level213.partsListArray[indx].attributes.f5;
	info_obj.partID_str = _level213.partsListArray[indx].attributes.f6;
	info_obj.pl_index_str = _level213.partsListArray[indx].attributes.f7;
	return(info_obj);		
}