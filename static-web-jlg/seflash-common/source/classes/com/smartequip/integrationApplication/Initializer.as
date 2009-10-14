/**
 *	The Initializer class performs initialization tasks and delegates messages between the legacy manager and the swf bridge
 *
 */
import mx.utils.Delegate;
import com.smartequip.integrationApplication.legacy.LegacyMainController;
import com.smartequip.integrationApplication.bridge.SwfBridge;
import com.smartequip.integrationApplication.event.*;
//import com.smartequip.integrationApplication.legacy.citrine.event.*;
import flash.external.ExternalInterface;

class com.smartequip.integrationApplication.Initializer extends MovieClip{
	
	private var _legacyController:LegacyMainController;
	private var _swfBridge:SwfBridge;
	
	private var mouseLastClicked:Number = undefined;
	private var startTime:Number = undefined;
	private var callKeepAliveOnClick:Boolean = false;
	
	/**
	 * Constructor initializes event listeners, default variables and loading of configuration file.
	 */
	public function Initializer() {		
		trace("Initializer()")
		
		Stage.scaleMode ="noScale";
		
		_legacyController = new LegacyMainController();
		_legacyController.addEventListener(AuthenticateCompleteEvent.COMPLETE, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(AuthorizeCompleteEvent.COMPLETE, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(ConfigurationCompleteEvent.COMPLETE, Delegate.create(this, routeEvent))
		
		_legacyController.addEventListener(LegacyTreeEvent.MODIFY, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyTabsEvent.LOAD, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyTabsEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyLoadingOnEvent.ON, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyLoadingOffEvent.OFF, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(ErrorShowEvent.SHOW, Delegate.create(this,routeEvent))
		_legacyController.addEventListener(LegacyTreeEvent.ADD_TO_SELECTION, Delegate.create(this,routeEvent))
		
		_swfBridge = new SwfBridge();
		_swfBridge.addEventListener(LegacyTreeEvent.MODIFY, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyTabsEvent.LOAD, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyTabsEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyLoadingOnEvent.ON, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyLoadingOffEvent.OFF, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(ErrorShowEvent.SHOW, Delegate.create(this,routeEvent))
		_swfBridge.addEventListener(LegacyTreeEvent.ADD_TO_SELECTION, Delegate.create(this,routeEvent))
		
		_legacyController.initConfiguration();
		
		var mouseListener:Object = new Object();
		mouseListener.onMouseDown = Delegate.create(this, checkMouse);
		Mouse.addListener(mouseListener);
		
		startTime = getTimer();
	}
	
	private function checkMouse(){
		trace("mouse clicked");
		if(callKeepAliveOnClick)
		{
			trace("Initializer-- calling sessionKeepAlive");
			ExternalInterface.call("sessionKeepAlive");
		}
		mouseLastClicked = getTimer();
	}
	
	private function onEnterFrame(){
		var WAIT_TIME = Number(_level0.sessionActivityCheckInterval);
		if(WAIT_TIME > 0 && ((getTimer() - startTime) > WAIT_TIME))
		{
			if(mouseLastClicked > startTime)
			{
				callKeepAliveOnClick = false;
				trace("Initializer-- calling sessionKeepAlive");
				ExternalInterface.call("sessionKeepAlive");
			}
			else
				callKeepAliveOnClick = true;
			startTime = getTimer();
		}
	}
	/*
	Called from the .fla, which was called from the browser to close the LocalConnection receiver
	
	public function closeConnection()
	{
		_swfBridge.closeConnection();
	}
	*/
	private function routeEvent(event:BaseEvent){
		trace("Initializer.routeEvent()   event.type="+event.type)
		switch(event.type){
			case ConfigurationCompleteEvent.COMPLETE:
				_legacyController.initSecurity();	
				_swfBridge.init(_legacyController.legacyComponentType, _legacyController.uniqueId);
				_legacyController.authenticate();
				break;
			case AuthenticateCompleteEvent.COMPLETE:
				_legacyController.authorize();
				break;
			case AuthorizeCompleteEvent.COMPLETE:
				_legacyController.loadLegacyComponent();
				break;
			case LegacyTabsEvent.LOAD:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.loadLegacyTabs(LegacyTabsEvent(event));
				break;
			case LegacyTreeEvent.MODIFY:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.modifyLegacyTree(LegacyTreeEvent(event));
				break;
			case LegacyTabsEvent.GLOBAL_SEARCH_LOAD:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.loadLegacyGlobalSearch(LegacyTabsEvent(event));
				break;
			case LegacyWebSearchEvent.SEARCH_RESULTS_LOAD:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.loadLegacySearchResults2(LegacyWebSearchEvent(event));
				break;
			case LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.loadLegacyGlobalSearchToTree();
				break;
			case LegacyLoadingOnEvent.ON:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.startLoading();
				break;
			case LegacyLoadingOffEvent.OFF:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.stopLoading();
				break;
			case ErrorShowEvent.SHOW:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.showError(event);
				break;
			case LegacyTreeEvent.ADD_TO_SELECTION:
				if(!isExternalCall(BaseCommunicationEvent(event)))	_legacyController.globalSearchAddToSelection();
				break;
			default:
				trace("EventSerializer.routeEvent()   error. unhandled event.type="+event.type);
		}
	}
	
	private function isExternalCall(event:BaseCommunicationEvent):Boolean{
		if(event.receiverName!=undefined && event.receiverName!=_legacyController.legacyComponentType){
			_swfBridge.sendEvent(event);
			return true;
		}
		
		return false;
	}
}