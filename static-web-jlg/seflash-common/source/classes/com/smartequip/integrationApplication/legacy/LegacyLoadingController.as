/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
import com.smartequip.integrationApplication.event.*;
import flash.external.ExternalInterface;

class com.smartequip.integrationApplication.legacy.LegacyLoadingController extends EventDispatcher{

	private var modal:MovieClip;

	public function LegacyLoadingController(){
		preparePositionAndScale()
	}

	public function init(){
		trace("LegacyLoadingController.init()")

		loadMovieNum (_level0.staticWebURL + "indeterminateLoader.swf", 300);

		_level0.showErrorMessage = Delegate.create(this, delegateShowErrorMessage);
		_level0.loadGlobalSearchSearchResults = Delegate.create(this,delegateLoadGlobalSearchSearchResults);
		_level0.paginationSearch = Delegate.create(this,delegatePaginationSearch);

		_level0.globalSearchAddToSelection = Delegate.create(this,delegateGlobalSearchAddToSelection);

		_level0.checkForError = Delegate.create(this,delegateCheckForError);
		//_level0.callJavaScriptAddParts = Delegate.create(this,delegateCallJavaScriptAddParts);
		//_level0.callJavaScriptAddToCart = Delegate.create(this,delegateCallJavaScriptAddToCart);
	}

	private function delegateCheckForError (reply ) {
		//trace("LegacyLoadingController.delegateCheckForError()")
		/*
		//actual legacy code:

		 var e = reply.firstChild;

			if (e.attributes.m.substr( start, 16 ) == "Session Time Out")  {

		loadMovie("timeOutPopUp.swf", 310);
		//trace("error exists!");

			}

		  */

		//<message rcode="0" msg="Session Time Out."/>
		var responseNode = reply.firstChild;

		//trace("LegacyLoadingController.delegateCheckForError()    responseNode.attributes.rcode="+responseNode.attributes.rcode)
		trace("LegacyLoadingController.delegateCheckForError()    reply="+reply)
		//trace("LegacyLoadingController.delegateCheckForError()    reply.firstChild.attributes['msg']="+reply.firstChild.attributes['msg'])
		//trace("LegacyLoadingController.delegateCheckForError()    reply.firstChild.attributes.msg="+reply.firstChild.attributes.msg)
		//trace("LegacyLoadingController.delegateCheckForError()    reply.firstChild.firstChild.attributes.msg="+reply.firstChild.firstChild.attributes.msg)
		trace("LegacyLoadingController.delegateCheckForError()    responseNode.attributes.msg="+responseNode.attributes.msg)

		//legacy code for checking session time out
		if (responseNode.attributes.msg == "Session Time Out.")  {

			trace("LegacyLoadingController.delegateCheckForError()  session time out registered. calling js sessionTimeOut()")

			var sessionMsg = "flash got backend timeout.";

			ExternalInterface.call("sessionTimeOut",sessionMsg)

		}



	}

	private function delegateGlobalSearchAddToSelection ( ) {
		trace("LegacyLoadingController.delegateGlobalSearchAddToSelection()    _level209.addExternalParts()")

		_level209.addExternalParts();

		var event:LegacyTreeEvent = new LegacyTreeEvent(LegacyTreeEvent.ADD_TO_SELECTION);
		event.receiverName = LegacyComponentData.TREE;

		dispatchEvent(event);

	}

	private function delegatePaginationSearch ( offset, srchlimit, type, cond) {
		trace("LegacyLoadingController.delegatePaginationSearch()  offset="+offset)
		trace("LegacyLoadingController.delegatePaginationSearch()  srchlimit="+srchlimit)
		trace("LegacyLoadingController.delegatePaginationSearch()  type="+type)
		trace("LegacyLoadingController.delegatePaginationSearch()  cond="+cond)
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
		if ( !(param.length > 0) ){
			param = _level0.searchParams[1][2];

		}

		trace("LegacyLoadingController.delegateSearch()  param="+param)
		/*
		if (_parent.companyCB._visible == true) {
			mfrgroup = _parent.companyCB.dropdown.getSelectedItem().label;
		}
		_level300.mouseWait();
		*/
		_level110.searchXML = new XML();
		if(mfrgroup==undefined) mfrgroup="";

		if(_level0.globalSearchParam == undefined){
			_level110.theXML = "<global-search mfrgrp='"+mfrgroup+"' " + "offset='" + offset + "' " + "srchlimit='" + srchlimit + "'>"+"<region>"+_level0.region+"</region>"+"<Option>All</Option>";
			_level110.theXML = _level110.theXML+ "<Qual"+i+">"+type+"</Qual"+i+">" +
							"<Cond"+i+">"+cond+"</Cond"+i+">" +
							"<Param"+i+">"+param+"</Param"+i+">";
			_level110.theXML = _level110.theXML+"</global-search>";
		}else{
			var param = _level0.globalSearchParam;


			var requestString = "<global-search ";

			param.offset = offset;
			param.srchlimit = srchlimit;

			if(param.mfrgroup==undefined) param.mfrgroup="";
			if(param.offset==undefined) param.offset="";
			if(param.srchlimit==undefined) param.srchlimit="";


			requestString += "mfrgrp=\"" + param.mfrgroup + "\" ";
			requestString += "offset=\"" + param.offset + "\" ";
			requestString += "srchlimit=\"" + param.srchlimit + "\" >";
			requestString += "<region>" + param.region + "</region>";
			requestString += "<Option>" + param.Option + "</Option>";
			//DO THE LAZY PARSE
			 for ( var ctr = 1 ; ctr <= 6 ; ctr++ ) {
				requestString += "<Qual" + ctr + ">" + param["Qual" + ctr] +  "</Qual" + ctr + ">";
				requestString += "<Cond" + ctr + ">" + param["Cond" + ctr] +  "</Cond" + ctr + ">";
				requestString += "<Param" + ctr + ">" + param["Param" + ctr] +  "</Param" + ctr + ">";
				requestString += "<Bool" + ctr + ">" + param["Bool" + ctr] +  "</Bool" + ctr + ">";
			}

			requestString += "</global-search>";

			_level110.theXML = requestString;
		}
		/*
		_level110.theXML = "<global-search mfrgrp='"+mfrgroup+"' " + "offset='" + offset + "' " + "srchlimit='" + srchlimit + "'>"+"<region>"+_level0.region+"</region>"+"<Option>All</Option>";
		_level110.theXML = _level110.theXML+ "<Qual"+i+">"+type+"</Qual"+i+">" +
						"<Cond"+i+">"+cond+"</Cond"+i+">" +
						"<Param"+i+">"+param+"</Param"+i+">";
		_level110.theXML = _level110.theXML+"</global-search>";
		*/

		_level110.searchXML.parseXML(_level110.theXML);
		_level0.searchResponseXML = new XML();

		//trace("LegacyWebSearchController.delegateSearch()  setting onLoad")

		_level0.searchResponseXML.onLoad = Delegate.create(this, delegatePaginationSearch_response)
		//_level0.searchResponseXML.onLoad = function(){trace("_level0.searchResponseXML.onLoad")};

		//_level0.searchResponseXML.onLoad = function(){dispatchEvent({type:"loadLegacySearchResults2"});};

		_level300.mouseWait();


		_level110.searchXML.sendAndLoad(_level0.baseURL + "servlet/RequestDirector?helper=GlobalSearch", _level0.searchResponseXML);

		//_level110.searchXML.sendAndLoad(_level0.SEServer + "servlet/RequestDirector?helper=GlobalSearch", _level0.searchResponseXML);
	}

	private function delegatePaginationSearch_response(success) {
		trace("LegacyLoadingController.delegatePaginationSearch_response()  success="+success)
		if (success) {
			//_level0.checkForError(_level0.searchResponseXML);
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

					trace("LegacyLoadingController.delegatePaginationSearch_response()  _level0.searchResponseXML="+_level0.searchResponseXML);

					var event:LegacyWebSearchEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD)
					event.receiverName = LegacyComponentData.TABS;
					event.searchResponseXML = _level0.searchResponseXML;
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


	public function delegateMouseWait ( ) {
		trace("LegacyLoadingController.delegateMouseWait()")

		var event:LegacyLoadingOnEvent = new LegacyLoadingOnEvent(LegacyLoadingOnEvent.ON);
		event.receiverName = LegacyComponentData.TABS;
		dispatchEvent(event);

	}

	public function delegateMouseNormal ( ) {
		trace("LegacyLoadingController.delegateMouseNormal()")

		var event:LegacyLoadingOffEvent = new LegacyLoadingOffEvent(LegacyLoadingOffEvent.OFF);
		event.receiverName = LegacyComponentData.TABS;
		dispatchEvent(event);

	}

	public function start ( ) {
		trace("LegacyLoadingController.start()")

		legacyView._visible = true;
		legacyView.loader._visible = true;
		/*
		modal = _level300.createEmptyMovieClip("modal",0);
		modal.beginFill(0xcccccc, 60)
		modal.moveTo(0, 0);
		modal.lineTo(Stage.width+500, 0);
		modal.lineTo(Stage.width+500, Stage.height+500);
		modal.lineTo(0, Stage.height+500);
		modal.lineTo(0, 0);
		modal.endFill();

		modal._x = -500;
		modal._y = -500;

		trace("LegacyLoadingController.start()    Stage.width="+Stage.width)
		trace("LegacyLoadingController.start()    Stage.height="+Stage.height)
		trace("LegacyLoadingController.start()    _root._width="+_root._width)
		trace("LegacyLoadingController.start()    _root._height="+_root._height)
		trace("LegacyLoadingController.start()    legacyView._width="+legacyView._width)
		trace("LegacyLoadingController.start()    legacyView._height="+legacyView._height)
		*/
	}

	public function stop ( ) {
		trace("LegacyLoadingController.stop()")
		//modal.removeMovieClip();

		if(!legacyView.errorPopupMC._visible)  legacyView._visible = false;
		legacyView.loader._visible = false;
	}

	private function delegateShowErrorMessage (message) {
		trace("LegacyLoadingController.delegateShowErrorMessage()   message="+message)

		var event:ErrorShowEvent = new ErrorShowEvent(ErrorShowEvent.SHOW);
		event.receiverName = LegacyComponentData.TABS;
		event.message = message;
		dispatchEvent(event);
	}

	public function showError (message) {
		if(message==undefined) message = "An internal error has occured."

		trace("LegacyLoadingController.showError()   message="+message)

		legacyView.errorPopupMC.errorMessage = message;

		legacyView._visible = true;
		legacyView.errorPopupMC._visible = true;

	}

	private function delegateLoadGlobalSearchSearchResults () {
		trace("LegacyLoadingController.delegateLoadGlobalSearchSearchResults()")

		var event:LegacyWebSearchEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD)
		event.receiverName = LegacyComponentData.TABS;
		event.searchResponseXML = _level0.searchResponseXML;
		dispatchEvent(event);

		//loadLegacySearchResults2(_level0.searchResponseXML);
	}


	public function preparePositionAndScale(){
		trace("LegacyLoadingController.preparePositionAndScale()   Stage.width="+Stage.width)
		trace("LegacyLoadingController.preparePositionAndScale()   Stage.height="+Stage.height)
		//without scale
		//_level0.legacyComponent_x = - 360;
		//_level0.legacyComponent_y = - 380;

		_level0.legacyComponent_x = - 405;
		_level0.legacyComponent_y = - 400;
		_level0.legacyComponent_xscale = 141;
		_level0.legacyComponent_yscale = 141;
	}


	public function get legacyView():MovieClip{
		return _level300;
	}
}
