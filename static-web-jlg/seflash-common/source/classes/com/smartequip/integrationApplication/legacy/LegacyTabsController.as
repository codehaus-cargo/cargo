/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
import com.smartequip.integrationApplication.event.LegacyTreeEvent;
import com.smartequip.integrationApplication.event.LegacyWebSearchEvent;
import com.smartequip.integrationApplication.legacy.LegacySecurityController;
import flash.external.ExternalInterface;


class com.smartequip.integrationApplication.legacy.LegacyTabsController extends EventDispatcher{
	
	private var _isDelegationSet:Boolean = false;
	
	public function LegacyTabsController(){
	}
	
	public function init(curMCFlag, curMfrName, curSpecLevel, curSchematic, partsdiagramAppear, curComponent, partsListURL, partsListRequestXML /*partsListArray*/, curModel, curModelNumbers, 
						 curComponentID, useMasterDesc, /*topPartsListArray,*/ goToPartsListTab, customTabs, curSerialNum, curDataSourceID, 
						 colorCH, sysURL, curModelDisplay){
		trace("LegacyTabsController.init()  $")
		//trace("LegacyTabsController.init()   _level0.searchResultsOrderPartsClicked="+_level0.searchResultsOrderPartsClicked)
		
		_level0.curMCFlag = curMCFlag
		_level0.curMfrName=curMfrName
		_level0.curSpecLevel=curSpecLevel
		_level0.curSchematic=curSchematic
		_level0.partsdiagramAppear=partsdiagramAppear
		_level0.curComponent=curComponent
		//_level213.partsListArray=partsListArray
		_level0.curComponentID = curComponentID
		_level0.curModel = curModel
		_level0.curModelNumbers = curModelNumbers
		_level0.useMasterDesc = useMasterDesc
		//_level213.clickedArray = new Array(partsListArray.length)
		//_level213.topPartsListArray=topPartsListArray
		_level0.goToPartsListTab = goToPartsListTab;
		_level0.customTabs = customTabs;
		_level0.curSerialNum = curSerialNum;
		_level0.curDataSourceID = curDataSourceID;
		_level0.colorCH = colorCH;
		_level0.sysURL = sysURL;
		_level0.curModelDisplay = curModelDisplay;
		//trace("tab event topPartsList"+_level213.topPartsListArray+" len "+_level213.topPartsListArray.length)
		
		
		
		/* 
		_level0.curMfrName="JLG";
        _level0.curSpecLevel="3126025_266-307";
        _level0.curSchematic="266_2588_11184";
        _level0.partsdiagramAppear=0;
        _level0.curComponent=" ";
		*/
		
		trace("LegacyTabsController.init()  partsListURL="+partsListURL)
		trace("LegacyTabsController.init()  partsListRequestXML="+partsListRequestXML)
		trace("LegacyTabsController.init()  _level0.curMCFlag="+_level0.curMCFlag)
		trace("LegacyTabsController.init()  _level0.curMfrName="+_level0.curMfrName)
		trace("LegacyTabsController.init()  _level0.curSpecLevel="+_level0.curSpecLevel)
		trace("LegacyTabsController.init()  _level0.curSchematic="+_level0.curSchematic)
		trace("LegacyTabsController.init()  _level0.partsdiagramAppear="+_level0.partsdiagramAppear)
		trace("LegacyTabsController.init()  _level0.curComponent="+_level0.curComponent)
		//trace("LegacyTabsController.init()  _level213.partsListArray="+_level213.partsListArray);
		trace("LegacyTabsController.init()  _level0.curComponentID="+_level0.curComponentID);
		trace("LegacyTabsController.init()  _level0.curModel="+_level0.curModel);
		trace("LegacyTabsController.init()  _level0.curModelNumbers="+_level0.curModelNumbers);
		trace("LegacyTabsController.init()  _level0.useMasterDesc="+_level0.useMasterDesc);
		trace("LegacyTabsController.init()  _level0.goToPartsListTab="+_level0.goToPartsListTab);
		trace("LegacyTabsController.init()  _level0.colorCH="+_level0.colorCH);
		
		
		//if(_level0.curMfrName == 
		if(_level0.curSpecLevel == undefined) _level0.curSpecLevel="3120637_100-110HX";
		
		
		_level0.loadPartsList = Delegate.create(this,delegateLoadPartsList);
		//_level0.callJavaScriptAddParts = Delegate.create(this,delegateCallJavaScriptAddParts);
		//_level0.callJavaScriptAddToCart = Delegate.create(this,delegateCallJavaScriptAddToCart);
		
		
		//level100.addToLists(indx2, mc._name);
		
		_level100.lloaded = true;
		_level0.firstTimeSugParts = 0;
		trace("LegacyTabsController.init()  _level100.lloaded="+_level100.lloaded);
		trace("LegacyTabsController.init()  _level0.firstTimeSugParts="+_level0.firstTimeSugParts);
		
		if(_isDelegationSet==false){
			_level100.addToLists = Delegate.create(this,delegateAddToLists);
			_level100.addTopPartsToLists = Delegate.create(this,delegateTopPartsAddToLists);
			_level100.delFromLists = Delegate.create(this,delegateDelFromLists);
			_level100.upDateTListDisplay = Delegate.create(this,delegateUpDateTListDisplay);
			_level100.clearAll = Delegate.create(this,delegateClearAll);
			_level100.loadTop100Parts = Delegate.create(this,loadTop100Parts);
			_level0.websearch = Delegate.create(this, loadLegacySearchResults2);
			_level0.callJavaScriptAddParts = Delegate.create(this,delegateCallJavaScriptAddParts);
			_level0.callJavaScriptAddToCart = Delegate.create(this,delegateCallJavaScriptAddToCart);
			
			_isDelegationSet = true;
		}
				
		
		System.security.allowDomain( LegacySecurityController.getDomain(_level0.manufacturerURL) );
		System.security.allowDomain( LegacySecurityController.getDomain(_level0.customerURL) );
		
		loadParts(partsListURL, partsListRequestXML);
		//trace("url="+partsListURL+" requestxml="+partsListRequestXML);

		//loadMovieNum (_level0.staticWebURL + "tabs.swf", 200);
		//trace("LegacyTabsController.init()  after  loadMovieNum (_level0.staticWebURL + tabs.swf, 200);");
	}
	
	private function loadParts(url, reqXML) {
		_level100.RequestXML = new XML();
		_level100.RequestXML.parseXML(reqXML);
		_level100.partsListResponseXML = new XML();
		_level100.partsListResponseXML.onLoad = function(success) {
			if (success) {
				_level0.checkForError(_level100.partsListResponseXML);
				if ( _level100.partsListResponseXML.firstChild.firstChild.nodeName == "header" ) {
					_level213.partsListHeader = _level100.partsListResponseXML.firstChild.firstChild;
					_level213.partsListArray = _level100.partsListResponseXML.firstChild.childNodes;
					_level213.partsListArray.splice(0,1); // remove header node
				} else {
					_level213.partsListArray = _level100.partsListResponseXML.childNodes;
					_level213.partsListArray.pop(); // remove extra node
				}
				_level213.partsListArrayLength = _level213.partsListArray.length;
				_level213.clickedArray = new Array(_level213.partsListArrayLength);
				_level0.messageBox = _level0.curComponent;
			} else {
				_level0.messageBox = "parts list loading failed";
				_level0.showErrorMessage(_level0.messageBox)
			}
			delete _level100.RequestXML;
			//delete _level100.partsListResponseXML;
			trace("array length = "+_level213.partsListArray.length);
			//for(var i=0; i<_level213.partsListArray.length; i++)
			//	trace("_level213.partsListArray["+i+"]="+_level213.partsListArray[i]);
			//trace("tree.loadParts()  //_level300.mouseNormal();");
			_level300.mouseNormal();
			
			//_level0.goToPartsListTab = false;			
			_level213.clickedArray = new Array(_level213.partsListArray);
			loadMovieNum (_level0.staticWebURL + "tabs.swf", 200);
			
		};
		_level300.mouseWait ();
		_level0.messageBox = "loading parts list...";
		_level100.RequestXML.sendAndLoad(url, _level100.partsListResponseXML);
	}
	
	private function delegateLoadPartsList(movieName, level){
		trace("LegacyTabsController.delegateLoadPartsList()  movieName="+movieName)
		//trace("LegacyTabsController.delegateLoadPartsList()  level="+level)
		
		if ( _level0.customPartsList & 1 ) {
			loadMovieNum ( _level0.baseURL + "manufacturers/" + _level0.curMfrName + "/custom/" + movieName, level);
			//loadMovieNum (_level0.SEServer + "manufacturers/" + _level0.curMfrName + "/custom/" + movieName, level);
			
		} else {
			loadMovieNum ( movieName , level);
		}
		
	}
	
	/* */
	//private function delegateCallJavaScriptFunction(functionName, stringArgument){
	private function delegateCallJavaScriptAddParts(){
		
		ExternalInterface.call("hide")
		
		var partsList:Array = new Array();
		
		for(var i:Number = 0; i<_level0.partsList.length; i++){
			//trace("LegacyTabsController.delegateCallJavaScriptAddParts()   i="+i+"   _level213.partsListArray[_level0.partsList[i]].attributes.f3="+_level213.partsListArray[_level0.partsList[i]].attributes.f3)
			//trace("LegacyTabsController.delegateCallJavaScriptAddParts()   i="+i+"   _level213.partsListArray[_level0.partsList[i]].attributes.f4="+_level213.partsListArray[_level0.partsList[i]].attributes.f4)
			
			//partsList.push(_level213.partsListArray[_level0.partsList[i]].attributes.f3+","+_level213.partsListArray[_level0.partsList[i]].attributes.f4);
			var quantity:String;
			if(isNaN(_level213.partsListArray[_level0.partsList[i]].attributes.f4)) quantity = "1";
			else quantity = _level213.partsListArray[_level0.partsList[i]].attributes.f4
			
			partsList.push(_level0.pmfrID[i]+","+_level0.pQty[i]);
		}
				
		for(var i:Number = 0; i<_level0.externalPartsList.length; i++){
			partsList.push(_level0.externalPartsList[i].partNumber+","+_level0.externalPartsList[i].quantity);
		}
		
		trace("LegacyTabsController.delegateCallJavaScriptAddParts()   partsList="+partsList)
		
		ExternalInterface.call("addParts",partsList)
		
		//trace("LegacyTabsController.delegateCallJavaScriptAddParts()   after  partsList="+partsList)
		
		_level210.lineMC.unhighlightAll()
		_level210.alertsMC.clearAll();
		delegateClearAll()
	}
	
	private function delegateCallJavaScriptAddToCart(partId, partDescription,partQuantity){
		//trace("LegacyTabsController.delegateCallJavaScriptAddToCart()")
		
		var partsList:Array = new Array();
		
		for(var i:Number = 0; i<_level0.partsList.length; i++){
			//trace("LegacyTabsController.delegateCallJavaScriptAddParts()   i="+i+"   _level213.partsListArray[_level0.partsList[i]].attributes.f3="+_level213.partsListArray[_level0.partsList[i]].attributes.f3)
			//trace("LegacyTabsController.delegateCallJavaScriptAddParts()   i="+i+"   _level213.partsListArray[_level0.partsList[i]].attributes.f4="+_level213.partsListArray[_level0.partsList[i]].attributes.f4)
			
			var quantity:String;
			if(isNaN(_level213.partsListArray[_level0.partsList[i]].attributes.f4)) quantity = "1";
			else quantity = _level213.partsListArray[_level0.partsList[i]].attributes.f4
			
			partsList.push(_level0.pmfrID[i]+"~$"
							+_level0.pDesc[i]+"~$"
							+_level0.pQty[i]);
		}
		
		for(var i:Number = 0; i<_level0.externalPartsList.length; i++){
			partsList.push(_level0.externalPartsList[i].partNumber+"~$"+ _level0.externalPartsList[i].partDescription+"~$"+_level0.externalPartsList[i].quantity);
		}
		
		trace("LegacyTabsController.delegateCallJavaScriptAddToCart()   partsList="+partsList)
		
		ExternalInterface.call("addToCart",partsList)
		
		_level210.lineMC.unhighlightAll()
		_level210.alertsMC.clearAll();
		delegateClearAll()
	}
	
	
	
	private function delegateAddToLists(index, mc){
		trace("LegacyTabsController.delegateAddToLists()  index="+index)
		trace("LegacyTabsController.delegateAddToLists()  mc="+mc)
		//trace("LegacyTabsController.delegateAddToLists()  before push  _level0.partsList="+_level0.partsList)
		
		//var line =  (_level213.partsListArray[index].attributes.f2  +  " - "  +  _level213.partsListArray[index].attributes.f1  +  newline);
		
		var partNumber = ""
		var partDescription = ""
		if(_level213.partsListArray[index].attributes.f2!=undefined) partNumber = _level213.partsListArray[index].attributes.f2;
		if(_level213.partsListArray[index].attributes.f1!=undefined) partDescription = _level213.partsListArray[index].attributes.f1;
		
		var line =  (partNumber  +  " - "  +  partDescription  +  newline);
		_level0.partsList.push(index);
		
		//trace("LegacyTabsController.delegateAddToLists()  after push  _level0.partsList="+_level0.partsList)
		
	//trace("_level0.partsList="+_level0.partsList);
		//eval("_level209.schematicClip."  +  mc).clicked = (_level0.partsList.length-1);
		var item = eval("_level209.schematicClip."  +  mc);
		item.clicked = (_level0.partsList.length-1);
		
		trace("LegacyTabsController.delegateAddToLists()  _level209.schematicClip="+_level209.schematicClip)
		//trace("LegacyTabsController.delegateAddToLists()  _level209.schematicClip['{9}']="+_level209.schematicClip['{9}'])
		trace("LegacyTabsController.delegateAddToLists()  _level209.schematicClip[mc]="+_level209.schematicClip[mc])
		
		trace("LegacyTabsController.delegateAddToLists()  item.clicked="+item.clicked)
		
	//trace("mc.clicked="+eval("_level209.schematicClip."  +  mc).clicked);
		_level0.pdispList.push( line );
		_level0.pmfrID.push(_level213.partsListArray[index].attributes.f3);
		_level0.pdsrc.push(_level0.curDataSourceID);
		_level0.ppID.push( _level213.partsListArray[index].attributes.f6 );
		_level0.pDesc.push( _level213.partsListArray[index].attributes.f1 );
		
		//fix the non-numberical qty issue for now
		var quantity:String;
		if(isNaN(_level213.partsListArray[index].attributes.f4)) quantity = "1";
		else quantity = _level213.partsListArray[index].attributes.f4
		
		_level0.pQty.push( quantity );
		_level0.pMC.push(mc);
		_level0.pgDesc.push( _level0.curPPGdesc );
		
		if(_level0.curMfrID==undefined) _level0.curMfrID="";
		if(_level0.curManualType==undefined) _level0.curManualType="";
		if(_level0.curModel==undefined) _level0.curModel="";
		if(_level0.curComponentID==undefined) _level0.curComponentID="";
		
		_level0.pComponent.push( (_level0.curMfrID  +  _level0.curManualType  +  _level0.curModel  +  _level0.curComponentID) );
		_level0.pVendNo.push(_level0.exVendNo);
		_level0.trolleyList = _level0.trolleyList  +  line;
	}

	private function delegateTopPartsAddToLists(index, mc){
		trace("LegacyTabsController.delegateTopPartsAddToLists()  index="+index)
		trace("LegacyTabsController.delegateTopPartsAddToLists()  mc="+mc)
		
		var line =  (_level213.topPartsListArray[index].attributes.f2  +  " - "  +  _level213.topPartsListArray[index].attributes.f1  +  newline);
		_level0.partsList.push(index);
	//trace("_level0.partsList="+_level0.partsList);
		//eval("_level209.schematicClip."  +  mc).clicked = (_level0.partsList.length-1);
		var item = eval("_level209.schematicClip."  +  mc);
		item.clicked = (_level0.partsList.length-1);
		
		trace("LegacyTabsController.delegateTopPartsAddToLists()  item="+item)
		
		trace("LegacyTabsController.delegateTopPartsAddToLists()  _level209.schematicClip="+_level209.schematicClip)
		
		trace("LegacyTabsController.delegateTopPartsAddToLists()  item.clicked="+item.clicked)
		
	//trace("mc.clicked="+eval("_level209.schematicClip."  +  mc).clicked);
		_level0.pdispList.push( line );
		_level0.pmfrID.push(_level213.topPartsListArray[index].attributes.f3);
		_level0.pdsrc.push(_level0.curDataSourceID);
		_level0.ppID.push( _level213.topPartsListArray[index].attributes.f6 );
		_level0.pDesc.push( _level213.topPartsListArray[index].attributes.f1 );
		_level0.pQty.push( _level213.topPartsListArray[index].attributes.f4 );
		_level0.pMC.push(mc);
		_level0.pgDesc.push( _level0.curPPGdesc );
		
		if(_level0.curMfrID==undefined) _level0.curMfrID="";
		if(_level0.curManualType==undefined) _level0.curManualType="";
		if(_level0.curModel==undefined) _level0.curModel="";
		if(_level0.curComponentID==undefined) _level0.curComponentID="";
		
		_level0.pComponent.push( (_level0.curMfrID  +  _level0.curManualType  +  _level0.curModel  +  _level0.curComponentID) );
		_level0.pVendNo.push(_level0.exVendNo);
		_level0.trolleyList = _level0.trolleyList  +  line;
	}
	
	
	private function delegateDelFromLists(index, count){
		trace("LegacyTabsController.delegateDelFromLists()  index="+index+"   count="+count)
		
		var i;
		var thisMC;
		var j;
		if ( index < 0 ) index = 0;
		
		for( i = 0; i < _level213.xmlArrayLength; i++ ) {
			
			if ( _level213.manufacturerArray[i].attributes.pname == _level0.ppID[index] ) {
				for ( j = i;  _level213.manufacturerArray[j].attributes.pname == _level0.ppID[index]; j++ ) { 
					_level213.qtyrequired.splice(i,1);
					_level213.qtavl.splice(i,1);
					_level213.pDesc.splice(i,1);
				}
				break;
			}
		}
	
		_level0.partsList.splice(index, count);
		_level0.pdispList.splice(index, count);
		_level0.pmfrID.splice(index, count);
		_level0.pdsrc.splice(index, count);
		_level0.ppID.splice(index, count);
		_level0.pDesc.splice(index, count);
		_level0.pQty.splice(index, count);
		_level0.pMC.splice(index, count);
		_level0.pgDesc.splice(index, count);
		_level0.pComponent.splice(index, count);
	
		for ( i = index ; i < _level0.partsList.length ; i++ ) {
	
			if ( _level0.pMC[i].length > 0){
				if ( _level0.pComponent[i] == (_level0.curMfrID + _level0.curManualType +  _level0.curModel + _level0.curComponentID) ) {
					thisMC = eval("_level209.schematicClip." + _level0.pMC[i]);
					thisMC.clicked = thisMC.clicked - count;
			   		while ( thisMC._name == _level0.pMC[i+1] ) i++;
				}
			}
		}
	}
	
	private function delegateUpDateTListDisplay(){
		trace("LegacyTabsController.delegateUpDateTListDisplay()  ")
		
		var i;
		var thisDesc;
		var thisRef;
		_level0.trolleyList = "";
	
		for (i=0; i<_level0.partsList.length; i++) {
			_level213.clickedArray[_level0.partsList[i]] = 1;
			_level0.trolleyList = _level0.trolleyList + _level0.pdispList[i];
		}
		
		for (i=0; i<_level0.externalPartsList.length; i++){
			_level0.trolleyList = _level0.trolleyList + _level0.externalPartsList[i].partNumber+" - "  +  _level0.externalPartsList[i].partDescription  +  newline;
		}
	}
	
	private function delegateClearAll(){
		trace("LegacyTabsController.delegateClearAll()  ")
		
		_level0.externalPartsList = new Array();
		
		var myColor;
		var i;
		var j;
		var indx;
		var mcname;
		var theMC;
		var thisline;
	//trace("_level0.pMC="+_level0.pMC);
		_level0.trolleyList = "";
		_level0.pdispList.splice(0);
		_level0.pmfrID.splice(0);
		_level0.pdsrc.splice(0);
		_level0.ppID.splice(0);
		_level0.pDesc.splice(0);
		_level0.pQty.splice(0);
		_level0.pMC.splice(0);
		_level0.pgDesc.splice(0);
		_level0.pComponent.splice(0);
		_level0.pVendNo.splice(0);
		_level213.clickedArray.splice(0);
		_level213.qtyrequired.splice(0);
		_level213.qtavl.splice(0);
		_level213.xmlArrayLength = null;
		_level213.manufacturerArray.splice(0);
		_level213.pDesc.splice(0);
		_level0.curMCFlag = null;
		_level0.curSchematic = null;
		if ( _level213.noParts > 0 ) {
			for ( i = _level213.noParts-1 ; i >= 0 ; i-- ) {
				eval("_level210.partMC.dupMC" + i).removeMovieClip();
			}
		}
		_level213.noParts = 0;
		_level0.purchaseOrderAppear = 0;
		_level0.curEquipNum = "";
		delete _level0.stockOrder;
		delete _level0.curLine;
		delete _level0.curMfrGrp;
		delete _level0.curTab;
		
		
		//loadMovieNum ("empty.swf", 213);
	//trace("_level0.partsList="+_level0.partsList);
		
		trace("LegacyTabsController.delegateClearAll()   clearing diagram _level0.partsList.length="+_level0.partsList.length)
		for ( i = _level0.partsList.length - 1 ; i >= 0  ; i-- ) {
			indx = _level0.partsList.pop();
	//trace("indx="+indx);
			thisline =  _level213.partsListArray[indx];
	//trace("thisline="+thisline.attributes);
	
			for ( j = 10; (mcname = eval("thisline.attributes.f" + j)) != undefined; j++) {
				theMC = ("_level209.schematicClip." + mcname);
				if (eval(theMC) == undefined) {
					theMC = "_level209.partsSelectMC.refsMC." + mcname;
				}
				myColor = new Color( theMC );
				eval(theMC).clicked = null; // Clear mc state 
				myColor.setRGB(_level209.mcColor[_level209.toggle][0]); // Reset mc color
			}
	
			/*theMC = ("_level209.schematicClip." + thisline.attributes.f10);
	//trace("theMC="+theMC);
			trace("LegacyTabsController.delegateClearAll()   clearing diagram, item  theMC="+theMC)
	
			myColor = new Color( theMC );
			
			eval(theMC).clicked = null; // clear mc state 
			myColor.setRGB(_level209.mcColor[_level209.toggle][0]); // reset mc color*/
		}
		
	}
	
	private function loadTop100Parts(modelNumber:String, cbObj:Object, cbFcn:String)
	{
		var Top100RequestXML = new XML();
		
		if(modelNumber == undefined)
			modelNumber = _level0.curModelNumbers[0];		
	
		var theXML = "<topparts-list-request><dataSourceID>" + _level0.curDataSourceID + "</dataSourceID><manufacturer-id>" + _level0.curMfrID + "</manufacturer-id><model> " + modelNumber + " </model></topparts-list-request>";
		Top100RequestXML.parseXML(theXML);
		_level100.topPartsListResponseXML = new XML();
		_level100.topPartsListResponseXML.onLoad = function(success) {
			if (success) {
				_level0.checkForError(_level100.topPartsListResponseXML);
				if ( _level100.topPartsListResponseXML.firstChild.firstChild.nodeName == "header" ) {
					_level213.topPartsListHeader = _level100.topPartsListResponseXML.firstChild.firstChild;
					_level213.topPartsListArray = _level100.topPartsListResponseXML.firstChild.childNodes;
					_level213.topPartsListArray.splice(0,1); // remove header node
				} else {
					_level213.topPartsListArray = _level100.topPartsListResponseXML.childNodes;
					_level213.topPartsListArray.pop(); // remove extra node
				}
				_level213.topPartsListArrayLength = _level213.topPartsListArray.length;
				
				//top parts list delete the clicked and load tabs swf
			} else {
				_level0.messageBox = "top 100 parts list loading failed";
				_level0.showErrorMessage(_level0.messageBox)
			}
			delete _level100.Top100RequestXML;
			delete _level100.topPartsListResponseXML;
			_level300.mouseNormal();
			
			if(cbObj != undefined && cbFcn != undefined)
				cbObj[cbFcn]();				
		};
		_level300.mouseWait ();
	
		//Top100RequestXML.sendAndLoad("http://agorcawsdev01.ca.smartequip.net:9080/CitrineSeWeb/sewebservice/servlet/RequestDirector?helper=ShowPopularPartsList", _level100.topPartsListResponseXML);
		Top100RequestXML.sendAndLoad(_level0.SEWebService + "parts/popularPartsList", _level100.topPartsListResponseXML);		
		//Top100RequestXML.sendAndLoad(_level0.baseURL + "servlet/RequestDirector?helper=ShowPopularPartsList", _level100.topPartsListResponseXML);			
	}	
	
	
	public function loadLegacySearchResults2 (searchResponseXML, searchParams) {
		//trace("LegacyTabsController.loadLegacySearchResults2()   searchResponseXML="+searchResponseXML)
		trace("LegacyTabsController.loadLegacySearchResults2()   searchParams="+searchParams)
		
		if(searchParams!=undefined) _level0.globalSearchParam = undefined;
		
		_level0.searchResponseXML = searchResponseXML;
		_level0.searchParams = searchParams;
		_level110.onReleaseFunction = Delegate.create(this, delegateOnReleaseFunction);
		
		//loadMovieNum("searchResultsWeb.swf", 221);
		loadMovieNum(_level0.staticWebURL + "searchResults2.swf",292);
		
		//trace("LegacyTabsController.loadLegacySearchResults2()   _level0.searchResponseXML="+_level0.searchResponseXML)
		
	}
	
	public function loadLegacyGlobalSearch (curDataSourceID, curMfrID) {
		trace("LegacyTabsController.loadLegacyGlobalSearch(), datasource="+curDataSourceID+" mfrid="+curMfrID);
		
		_level0.curDataSourceID = curDataSourceID;
		_level0.curMfrID = curMfrID;
		
		unloadMovieNum(292);
		_level0.refineFlag = false;
		_level0.region = "";
		
		loadMovieNum(_level0.staticWebURL + "globalSearch.swf",291);
		
	}
	
	private function delegateOnReleaseFunction(mfrgrp, mfr, prodGroupID, modelGroupID, model, equipno, serialno, componentID, componentGroupID, manualTitle, manualtype, goToPartsListTab )  { 
		//trace("delegateOnReleaseFunction()   mfrgrp="+mfrgrp)
		trace("LegacyTabsController.delegateOnReleaseFunction()   mfr="+mfr+" gotoparts="+goToPartsListTab);
		trace("schematic = "+_level0.curSchematic);
		trace("curComponent = "+_level0.curComponent);
		trace("curComponentID = "+_level0.curComponentID);
		trace("model = "+_level0.curModel);
		
		_level0.curSchematic = undefined;
		_level0.curComponent = undefined;
		_level0.curComponentID = undefined;
		_level0.curModel = undefined;
		
		_level0.curMfrName = mfr;
		_level0.goToPartsListTab = goToPartsListTab;

		//unloadMovieNum(292);
		
		var event:LegacyTreeEvent = new LegacyTreeEvent(LegacyTreeEvent.MODIFY)
		event.receiverName = LegacyComponentData.TREE;
		event.mfrgrp = mfrgrp;
		event.mfr = mfr;
		event.prodGroupID = prodGroupID; 
		event.modelGroupID = modelGroupID; 
		event.model = model; 
		event.equipno = equipno; 
		event.serialno = serialno; 
		event.componentID = componentID; 
		event.componentGroupID = componentGroupID; 
		event.manualTitle = manualTitle; 
		event.manualtype = manualtype;
		event.goToPartsListTab = goToPartsListTab;
		dispatchEvent(event);
		/*
		dispatchEvent({type:"legacyTree_modify",
						externalCall:false,
						receiverName:LegacyComponentData.TREE,
						mfrgrp:mfrgrp,
						mfr:mfr,
						prodGroupID:prodGroupID, 
						modelGroupID:modelGroupID, 
						model:model, 
						equipno:equipno, 
						serialno:serialno, 
						componentID:componentID, 
						componentGroupID:componentGroupID, 
						manualTitle:manualTitle, 
						manualtype:manualtype
		})
		*/
		
	}
	
	
	public function preparePositionAndScale(){
		/*
		_level0.legacyComponent_x = - 430;
		_level0.legacyComponent_y = - 185;
		_level0.legacyComponent_xscale = 130;
		_level0.legacyComponent_yscale = 130;
		*/
	}
	
	public function get legacyView():MovieClip{
		return _level200;
	}
	
}
	