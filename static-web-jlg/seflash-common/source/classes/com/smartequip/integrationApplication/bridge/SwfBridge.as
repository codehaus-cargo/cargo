/**
 *
 */

/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
import flash.external.ExternalInterface;

import com.smartequip.integrationApplication.bridge.EventSerializer;


class com.smartequip.integrationApplication.bridge.SwfBridge  extends EventDispatcher{
	
	private static var SWF_COMMUNICATION_RETRIES:Number = 10;
	private var swfCommunicationRetriesCount:Number = 0;
	
	private var receiverLocalConnection:LocalConnection;
	private var receiverLocalConnection2:LocalConnection;
	private var senderLocalConnection:LocalConnection;
	
	private var _legacyComponentType:String;
	private var _uniqueId:String;
	private var _eventSerializer:EventSerializer;
	
	private var _currentEvent:BaseCommunicationEvent;
	
	private var _swfCommunicationRetriesIntervalID:Number = -1;
	
	
	public function SwfBridge(){
		
	}
	
	public function init(legacyComponentType:String, uniqueId:String){
		trace("SwfBridge.init()   legacyComponentType="+legacyComponentType);
		trace("SwfBridge.init()   uniqueId="+uniqueId);
		
		_legacyComponentType = legacyComponentType;
		_uniqueId = uniqueId;
		
		_eventSerializer = new EventSerializer();
		
		senderLocalConnection = new LocalConnection();
		senderLocalConnection.onStatus = Delegate.create(this,senderLocalConnection_onStatus);
		
		ExternalInterface.addCallback("closeConnection", this, closeReceiverLocalConnection);
				
		receiverLocalConnection = new LocalConnection();
		receiverLocalConnection.receiveEvent = Delegate.create(this,receiveEvent);
		receiverLocalConnection.connect(_legacyComponentType+"@"+_uniqueId);
		
		receiverLocalConnection2 = new LocalConnection();
		receiverLocalConnection2.receiveEvent = Delegate.create(this,receiveEvent);
		receiverLocalConnection2.connect(_legacyComponentType+"2@"+_uniqueId);
	}
	
	/*
	Method which was called from the browser to close the LocalConnection receiver
	*/
	private function closeReceiverLocalConnection(){
		trace("SwfBridge.closeReceiverLocalConnection()");
		receiverLocalConnection.close();
		receiverLocalConnection2.close();
	}
	
	/*
	Called from the Initializer, which was called from the .fla, which was called from the browser
	to close the LocalConnection receiver
	
	public function closeConnection()
	{
		receiverLocalConnection.close();
	}
	*/
	private function senderLocalConnection_onStatus(infoObject:Object) {
		//trace("SwfBridge.senderLocalConnection_onStatus()   infoObject.level="+infoObject.level);
		
		switch (infoObject.level) {
	        case 'status' :
	            trace("SwfBridge.senderLocalConnection_onStatus()   success");
	            
				if(_swfCommunicationRetriesIntervalID >= 0)
		            clearInterval(_swfCommunicationRetriesIntervalID);
	            _swfCommunicationRetriesIntervalID = -1;
	            swfCommunicationRetriesCount = 0;
	            
                break;
	        case 'error' :
	            trace("SwfBridge.senderLocalConnection_onStatus()   failure: " + infoObject.code);
	            
	           if(swfCommunicationRetriesCount==0 && _swfCommunicationRetriesIntervalID==-1) {
	           		_swfCommunicationRetriesIntervalID = setInterval(this, "swfCommunicationRetry", 500);
	           		//trace("SwfBridge.senderLocalConnection_onStatus()   interval started _swfCommunicationRetriesIntervalID="+_swfCommunicationRetriesIntervalID);
	           }
	            
	            break;
	        default:
	        	trace("SwfBridge.senderLocalConnection_onStatus()   unhandled status");
	        	break;
        }
	}
	
	private function swfCommunicationRetry(){
		trace("SwfBridge.swfCommunicationRetry()");
		
		if(swfCommunicationRetriesCount<SWF_COMMUNICATION_RETRIES) {
        	swfCommunicationRetriesCount++;
        	sendEvent(_currentEvent, true);
        }else{
        	trace("SwfBridge.swfCommunicationRetry()   failure to connect after "+SWF_COMMUNICATION_RETRIES+" tries");
        	//trace("SwfBridge.swfCommunicationRetry()   should clear _swfCommunicationRetriesIntervalID="+_swfCommunicationRetriesIntervalID);
        	clearInterval(_swfCommunicationRetriesIntervalID);
        	_swfCommunicationRetriesIntervalID = -1;
        	swfCommunicationRetriesCount = 0;
        }
       
	}
	
	
	private function receiveEvent(anonymousEventObject:Object){
		trace("SwfBridge.receiveEvent()   anonymousEventObject.type="+anonymousEventObject.type);
		trace("SwfBridge.receiveEvent()   anonymousEventObject.receiverName="+anonymousEventObject.receiverName);
		trace("SwfBridge.receiveEvent()   _legacyComponentType="+_legacyComponentType);
		
		var event:BaseCommunicationEvent = _eventSerializer.deserializeEvent(anonymousEventObject);
		
		if(event.receiverName == _legacyComponentType) dispatchEvent(event);
		else trace("SwfBridge.receiveEvent()   error   event.receiverName != _legacyComponentType")
	}
	
	public function sendEvent(event:BaseCommunicationEvent, extraId:Boolean){
		//trace("SwfBridge.sendEvent()   _currentEvent.type="+_currentEvent.type);
		//trace("SwfBridge.sendEvent()   _currentEvent.receiverName="+_currentEvent.receiverName);
		
		_currentEvent = event;
		
		trace("SwfBridge.sendEvent()   event.type="+event.type);
		trace("SwfBridge.sendEvent()   event.receiverName="+event.receiverName);
		trace("SwfBridge.sendEvent()   this._legacyComponentType="+this._legacyComponentType);
		trace("SwfBridge.sendEvent()   destination="+event.receiverName+"@"+_uniqueId);
		trace("SwfBridge.sendEvent()   extraId="+extraId);
		
		senderLocalConnection.send(event.receiverName+(extraId ? "2" : "")+"@"+_uniqueId, "receiveEvent", _eventSerializer.serializeEvent(event));
		/*
		var anonymousEventObject:Object = _eventSerializer.serializeEvent(event)
		trace("SwfBridge.sendEvent()   anonymousEventObject.type="+anonymousEventObject.type);
		trace("SwfBridge.sendEvent()   anonymousEventObject.message="+anonymousEventObject.message);
		senderLocalConnection.send(event.receiverName+"@"+_uniqueId, "receiveEvent", anonymousEventObject);
		*/
		
	}
		
}