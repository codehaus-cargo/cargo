/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
import com.smartequip.integrationApplication.event.LegacyTabsEvent;
import flash.external.ExternalInterface;

class com.smartequip.integrationApplication.legacy.LegacyTreeController extends EventDispatcher{
	
	
	public function LegacyTreeController(){
	
	}
	
	public function init(){
		
		trace("LegacyTreeController.init()")
		
		_level0.loadTabs = Delegate.create(this,delegateLoadTabs);
		
		_level0.curDataSourceID = _level0.mfrXMLArray[0].firstChild.firstChild.nodeValue;
		_level0.initDSID = _level0.mfrXMLArray[0].firstChild.firstChild.nodeValue;
		_level0.curDataSourceCount = _level0.mfrXMLArray.length ;
		_level0.curMfrCount = _level0.mfrXMLArray[0].firstChild.nextSibling.firstChild.nodeValue ;
		_level0.curMfrName = _level0.mfrXMLArray[0].firstChild.firstChild.nodeValue;
		_level0.curMfrGrp = "";
		_level0.curMfrID = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.firstChild.nodeValue;
		_level0.customTabs = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
		_level0.customPartsList = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
		_level0.showMG = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
		_level0.curProdGroupID = "%"; // wildcard char for default query 
		_level0.useMasterDesc = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
		_level0.curDropDown = "Manuals"
		
		loadMovieNum (_level0.staticWebURL + "tree.swf", 100);
	}
	
	public function loadLegacyGlobalSearch() {
		var event:LegacyTabsEvent = new LegacyTabsEvent(LegacyTabsEvent.GLOBAL_SEARCH_LOAD);
		event.receiverName = LegacyComponentData.TABS;
		event.curDataSourceID = _level0.curDataSourceID;
		event.curMfrID = _level0.curMfrID;
		
		dispatchEvent(event);
	}
	
	public function delegateLoadTabs(tabs, level){
		
		trace("LegacyTreeController.delegateLoadTabs()    calling javascript refresh()")
		
		ExternalInterface.call("refresh")
		
		trace("LegacyTreeController.delegateLoadTabs()  tabs="+tabs)
		
		//trace("LegacyTreeController.delegateLoadTabs()  _level213.partsListArray="+_level213.partsListArray);
		
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curMCFlag="+_level0.curMCFlag)
		
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curMfrName="+_level0.curMfrName)
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curSpecLevel="+_level0.curSpecLevel)
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curSchematic="+_level0.curSchematic)
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.partsdiagramAppear="+_level0.partsdiagramAppear)
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curComponent="+_level0.curComponent)
		if(_level0.curComponent==undefined) _level0.curComponent = " ";
		//trace("LegacyTreeController.delegateLoadTabs()  _level0.curComponent="+_level0.curComponent)
		
		if( _level0.curSerialNum == "undefined"  || _level0.curSerialNum == undefined || _level0.curSerialNum == "null" || _level0.curSerialNum == null) _level0.curSerialNum = "";		
		
		var event:LegacyTabsEvent = new LegacyTabsEvent(LegacyTabsEvent.LOAD);
		event.receiverName = LegacyComponentData.TABS;
		event.curMCFlag = _level0.curMCFlag
		event.curMfrName = _level0.curMfrName
		event.curSpecLevel = _level0.curSpecLevel
		event.curSchematic = _level0.curSchematic
		event.partsdiagramAppear = _level0.partsdiagramAppear
		event.curComponent = _level0.curComponent
		//event.partsListArray = _level213.partsListArray

		if ( _level0.customPartsList & 2 ) { 
			event.partsListURL = _level0.baseURL + "servlet/RequestDirector?helper=ShowPartsListExtended";
		} else {
			event.partsListURL = _level0.baseURL + "servlet/RequestDirector?helper=ShowPartsList";
		}
		event.partsListRequestXML = "<parts-list-request><dataSourceID>" + _level0.curDataSourceID + "</dataSourceID><product-id>" + _level0.curModel + "</product-id><component-id>" + _level0.curComponentID + "</component-id><prodids>" + _level0.curProdIDs + "</prodids><mpd>" + _level0.useMasterDesc + "</mpd><serial-number>" + _level0.curSerialNum + "</serial-number></parts-list-request>";


		event.curComponentID = _level0.curComponentID
		event.curModel = _level0.curModel
		event.curModelNumbers = _level0.curModelNumbers
		event.useMasterDesc = _level0.useMasterDesc
		//event.topPartsListArray = _level213.topPartsListArray
		event.goToPartsListTab = _level0.goToPartsListTab
		event.customTabs = _level0.customTabs
		event.colorCH = _level0.colorCH
		event.sysURL = _level0.sysURL
		event.curModelDisplay = _level0.curModelDisplay
		event.curSerialNum = _level0.curSerialNum
		event.curDataSourceID = _level0.curDataSourceID
		
		trace("LegacyTreeController -- curSerialNum = "+_level0.curSerialNum);
		
		trace("LegacyTreeController.delegateLoadTabs() _level0.nextView = "+_level0.nextView);
		//trace("LegacyTreeController.delegateLoadTabs()  event.partsListArray="+event.partsListArray);
		
		trace("LegacyTreeController.delegateLoadTabs()  Number(_level213.partsListArray[0].attributes.f7="+Number(_level213.partsListArray[0].attributes.f7))
		
		trace("LegacyTreeController.delegateLoadTabs()  event.curComponentID="+event.curComponentID);
		trace("LegacyTreeController.delegateLoadTabs()  event.curModel="+event.curModel);
		trace("LegacyTreeController.delegateLoadTabs()  event.curModelNumbers="+event.curModelNumbers);
		
		trace("LegacyTreeController.delegateLoadTabs()  event.useMasterDesc="+event.useMasterDesc);
		
		//trace("LegacyTreeController.delegateLoadTabs()  event.topPartsListArray213="+_level213.topPartsListArray);
		//trace("LegacyTreeController.delegateLoadTabs()  event.topPartsListArray="+event.topPartsListArray);
		dispatchEvent(event);
		/*
		dispatchEvent({	type:"legacyTabs_load", 
						//externalCall:false,
						receiverName:LegacyComponentData.TABS,
						curMfrName:_level0.curMfrName,
						curSpecLevel:_level0.curSpecLevel,
						curSchematic:_level0.curSchematic,
						partsdiagramAppear:_level0.partsdiagramAppear,
						curComponent:_level0.curComponent
						})
		*/
	}
	
	public function modifyLegacyTree(mfrgrp, mfr, prodGroupID, modelGroupID, model, equipno, serialno, componentID, componentGroupID, manualTitle, manualtype, goToPartsListTab )  { 
		trace("LegacyTreeController.modifyLegacyTree()   mfrgrp="+mfrgrp)
		trace("LegacyTreeController.modifyLegacyTree()   mfr="+mfr)
		trace("LegacyTreeController.modifyLegacyTree()   prodGroupID="+prodGroupID)
		trace("LegacyTreeController.modifyLegacyTree()   modelGroupID="+modelGroupID)
		trace("LegacyTreeController.modifyLegacyTree()   model="+model)
		trace("LegacyTreeController.modifyLegacyTree()   equipno="+equipno)
		trace("LegacyTreeController.modifyLegacyTree()   serialno="+serialno)
		trace("LegacyTreeController.modifyLegacyTree()   componentID="+componentID)
		trace("LegacyTreeController.modifyLegacyTree()   componentGroupID="+componentGroupID)
		trace("LegacyTreeController.modifyLegacyTree()   manualTitle="+manualTitle)
		trace("LegacyTreeController.modifyLegacyTree()   manualtype="+manualtype)
		trace("LegacyTreeController.modifyLegacyTree()   goToPartsListTab="+goToPartsListTab)
		
		_level0.curMfrName = mfr;
		_level0.goToPartsListTab = goToPartsListTab;
		
		_level100.tree.collapseTree ( );
		// open tree to the selected item
		if ( (_level0.curDropDown == "Manufacturers") or (_level0.curDropDown == "Manuals") ) {
			_level100.tree.expandMfrTree( mfrgrp, _level0.curMfrName, prodGroupID, modelGroupID, model, equipno, serialno, componentID, componentGroupID, manualTitle, manualtype  );
		} else if ( _level0.curDropDown == "Products"  ) {
			if ( _level0.searchType == "modelNumber" ) {
				_level100.tree.expandProductTree( mfr, prodGroupID, modelGroupID, model, equipno, serialno, null, null, manualTitle, manualtype );
			} else {
				_level100.tree.expandProductTree( mfr, prodGroupID, modelGroupID, model, equipno, serialno, componentID, componentGroupID, manualTitle, manualtype );
			}
		}
	}
	
	public function preparePositionAndScale(){
		trace("LegacyTreeController.preparePositionAndScale()   Stage.width="+Stage.width)
		trace("LegacyTreeController.preparePositionAndScale()   Stage.height="+Stage.height)
		
		_level0.legacyComponent_x = 195;
		//_level0.legacyComponent_y = 15;
		_level0.legacyComponent_y = -5;
		_level0.legacyComponent_xscale = 96;
		_level0.legacyComponent_yscale = 96;
	}
	
	
	public function get legacyView():MovieClip{
		return _level100;
	}
}
	