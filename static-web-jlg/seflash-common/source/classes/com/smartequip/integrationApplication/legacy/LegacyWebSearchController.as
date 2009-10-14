/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
import com.smartequip.integrationApplication.event.LegacyWebSearchEvent;
import flash.external.ExternalInterface;

class com.smartequip.integrationApplication.legacy.LegacyWebSearchController extends EventDispatcher{
	
	
	public function LegacyWebSearchController(){
	}
	
	
	
	
	
	public function init(region){
		trace("LegacyWebSearchController.init()")
		
		loadMovieNum (_level0.staticWebURL + "websearch.swf", 110);
		
		_level0.loadGlobalSearch = Delegate.create(this, delegateLoadGlobalSearch);
		_level0.websearchSearch = Delegate.create(this, delegateWebsearchSearch);
		_level0.searchInputFieldClicked = Delegate.create(this, delegateSearchInputFieldClicked);
		_level0.region = region;
		
		/*var selListener = new Object();
		selListener.onSetFocus = function(oldF, newF){
			trace("LegacyWebSearchController.selListener.onSetFocus()   oldF="+oldF)
			trace("LegacyWebSearchController.selListener.onSetFocus()   newF="+newF)
		}
		
		Selection.addListener(selListener);
				
		ExternalInterface.addCallback("removeSearchInputFocus", this, removeSearchInputFocus);*/
	}
	
	/*private function removeSearchInputFocus(){
		
		trace("LegacyWebSearchController.removeSearchInputFocus()  before  Selection.getFocus('_level0.searchPattern')="+Selection.getFocus("_level0.searchPattern"))
		
		trace("LegacyWebSearchController.removeSearchInputFocus()  Selection.setFocus(null)="+Selection.setFocus(null))
		
		trace("LegacyWebSearchController.removeSearchInputFocus()  after  Selection.getFocus('_level0.searchPattern')="+Selection.getFocus("_level0.searchPattern"))
		
		//trace("LegacyWebSearchController.removeSearchInputFocus()  Selection.setFocus(_level110)="+Selection.setFocus(_level110))
	}*/
	
	private function delegateLoadGlobalSearch ( ) {
		trace("LegacyWebSearchController.delegateLoadGlobalSearch()    calling javascript refresh()")
		
		ExternalInterface.call("refresh")
		
		//trace("LegacyWebSearchController.delegateLoadGlobalSearch()")
		
		var event:LegacyWebSearchEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD)
		event.receiverName = LegacyComponentData.TREE;
		dispatchEvent(event);
	}
	
	private function delegateWebsearchSearch ( offset, srchlimit, type, cond) {
		trace("LegacyWebSearchController.delegateWebsearchSearch()    calling javascript refresh()")
		
		ExternalInterface.call("refresh")
		
		//trace("LegacyWebSearchController.delegateWebsearchSearch()   offset="+offset)
		//trace("LegacyWebSearchController.delegateWebsearchSearch()   srchlimit="+srchlimit)
		
		doSearch( offset, srchlimit, type, cond)
	}
	
	private function delegateSearchInputFieldClicked ( ) {
		trace("LegacyWebSearchController.delegateSearchInputFieldClicked()    calling javascript searchInputFieldClicked()")
		
		ExternalInterface.call("searchInputFieldClicked")
		
		
	}
	
	private function doSearch ( offset, srchlimit, type, cond) {
		trace("LegacyWebSearchController.delegateSearch()  offset="+offset)
		trace("LegacyWebSearchController.delegateSearch()  srchlimit="+srchlimit)
		trace("LegacyWebSearchController.delegateSearch()  type="+type)
		trace("LegacyWebSearchController.delegateSearch()  cond="+cond)
		var e;
		var i = 1;
		var j;
		_level110.storeWebSearchForNewGlobalSearch ( type , cond );
		
		var param = _level0.searchPattern;
		var mfrgroup = "";
		var tval;	
		
		if ( !(type.length > 0) ) {
			tval = _level0.searchParams[1][0].split(":");
			type = tval[1];
		}
		if ( !(cond.length > 0) ){
			tval = _level0.searchParams[1][1].split(":");
			cond = tval[1];
		}	
		/* 
		if (_parent.companyCB._visible == true) {
			mfrgroup = _parent.companyCB.dropdown.getSelectedItem().label;
		}
		_level300.mouseWait();
		*/
		_level110.searchXML = new XML();
		if(mfrgroup==undefined) mfrgroup="";
		_level110.theXML = "<global-search mfrgrp='"+mfrgroup+"' " + "offset='" + offset + "' " + "srchlimit='" + srchlimit + "'>"+"<region>"+_level0.region+"</region>"+"<Option>All</Option>";
		_level110.theXML = _level110.theXML+ "<Qual"+i+">"+type+"</Qual"+i+">" +
						"<Cond"+i+">"+cond+"</Cond"+i+">" +
						"<Param"+i+">"+param+"</Param"+i+">";	
		_level110.theXML = _level110.theXML+"</global-search>";
		_level110.searchXML.parseXML(_level110.theXML);
		_level0.searchResponseXML = new XML();
		
		//trace("LegacyWebSearchController.delegateSearch()  setting onLoad")
		
		_level0.searchResponseXML.onLoad = Delegate.create(this, doSearch_response)
		//_level0.searchResponseXML.onLoad = function(){trace("_level0.searchResponseXML.onLoad")};
		
		//_level0.searchResponseXML.onLoad = function(){dispatchEvent({type:"loadLegacySearchResults2"});};
		
		_level300.mouseWait();
		
		_level110.searchXML.sendAndLoad(_level0.baseURL + "servlet/RequestDirector?helper=GlobalSearch", _level0.searchResponseXML);
		//_level110.searchXML.sendAndLoad(_level0.SEServer + "servlet/RequestDirector?helper=GlobalSearch", _level0.searchResponseXML);
	}
	
	private function doSearch_response(success) { 
		trace("LegacyWebSearchController.doSearch_response()  success="+success)
		if (success) {
			_level0.checkForError(_level0.searchResponseXML);
			_level0.lastResultSelected = undefined;
			_level0.lastResultSelectedIndex = undefined;
			if (_level0.searchResponseXML.firstChild.firstChild.attributes.error.length>1) {
				_level300.popup(_level0.searchResponseXML.firstChild.firstChild.attributes.error, "Search Warning");
				_level0.showErrorMessage(_level0.searchResponseXML.firstChild.firstChild.attributes.error);
			} else {
				if (_level292._width > 0) {
					_level292.searchResultsMC.searchResultsMC.functionDupMC(_level0.searchResponseXML.firstChild.childNodes);
				} else {
					//loadMovieNum("searchResults2.swf",292);
					
					//trace("LegacyWebSearchController.doSearch_response()  _level0.searchResponseXML="+_level0.searchResponseXML);
					
					var event:LegacyWebSearchEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD)
					event.receiverName = LegacyComponentData.TABS;
					event.searchResponseXML = _level0.searchResponseXML;
					event.searchParams = _level0.searchParams;
					dispatchEvent(event);
					/*
					dispatchEvent({type:"legacySearchResults2_load",
									externalCall:false,
									receiverName:LegacyComponentData.TABS,
									searchResponseXML:_level0.searchResponseXML});
					*/
				}
			}
		} else {
			_level0.messageBox = "Loading failed";
			_level0.showErrorMessage(_level0.messageBox);
			
		}
		_level300.mouseNormal();
	}
	
	public function preparePositionAndScale(){
		trace("LegacyWebSearchController.preparePositionAndScale()   Stage.width="+Stage.width)
		trace("LegacyWebSearchController.preparePositionAndScale()   Stage.height="+Stage.height)
		
		_level0.legacyComponent_x = 190;
		_level0.legacyComponent_y = 115;
		_level0.legacyComponent_xscale = 106;
		_level0.legacyComponent_yscale = 106;
	}
	
	public function get legacyView():MovieClip{
		return _level110;
	}
}
	