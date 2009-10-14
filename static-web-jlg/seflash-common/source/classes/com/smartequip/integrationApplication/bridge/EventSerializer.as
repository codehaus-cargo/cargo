/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
import com.smartequip.integrationApplication.event.*;

class com.smartequip.integrationApplication.bridge.EventSerializer{
	
	private static var ARRAY_DELIMITER:String = "#delimiter#"
	
	public function serializeEvent(event:Object):Object{
		var anonymousEventObject:Object;
		
		/* */
		switch(event.type){
			case LegacyTabsEvent.LOAD:
				/*var tempPartsListArrayString:String = "";
				for(var i:Number = 0; i<event.partsListArray.length; i++){
					if((tempPartsListArrayString + event.partsListArray[i]).length < 38000) { //only allow less then 40k to pass through
						tempPartsListArrayString = tempPartsListArrayString + event.partsListArray[i];
					}
					else { //add message and break out of loop
						//trace("node = "+event.partsListArray[i]);
						tempPartsListArrayString += "<parts aflag='' mcn='' idnt='0' bold='' f1='*** List Truncated ***' f2='' f3='' f4='' f5='' f6='' f7='' f8='' f9='' f10='' />";
						break;
					}
				}*/
				
				/*var tempTopPartsListArrayString:String = "";
				for(var i:Number = 0; i<event.topPartsListArray.length; i++){
					tempTopPartsListArrayString = tempTopPartsListArrayString + event.topPartsListArray[i];
				}
				if(event.topPartsListArray.length == undefined)
					tempTopPartsListArrayString = event.topPartsListArray; //why do i have to do this??!!*/
				//trace("serializing, str="+tempTopPartsListArrayString);
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName,
										curMCFlag:event.curMCFlag, 
										curMfrName:event.curMfrName, 
										curSpecLevel:event.curSpecLevel, 
										curSchematic:event.curSchematic, 
										partsdiagramAppear:event.partsdiagramAppear, 
										curComponent:event.curComponent,
										//partsListArray:tempPartsListArrayString,
										partsListURL:event.partsListURL,
										partsListRequestXML:event.partsListRequestXML,
										curModel:event.curModel,
										curComponentID:event.curComponentID,
										useMasterDesc:event.useMasterDesc ,
										//topPartsListArray:tempTopPartsListArrayString,
										goToPartsListTab:event.goToPartsListTab,
										curModelNumbers:event.curModelNumbers,
										customTabs:event.customTabs,
										curSerialNum:event.curSerialNum,
										curDataSourceID:event.curDataSourceID,
										colorCH:event.colorCH,
										sysURL:event.sysURL,
										curModelDisplay:event.curModelDisplay}
				
				
				//trace("EventSerializer.serializeEvent()   anonymousEventObject.partsListArray="+anonymousEventObject.partsListArray);
				
				break;
			case LegacyTabsEvent.GLOBAL_SEARCH_LOAD:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName,
										curDataSourceID:event.curDataSourceID,
										curMfrID:event.curMfrID
										}
				break;
			case LegacyTreeEvent.MODIFY:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName,
										mfrgrp:event.mfrgrp, 
										mfr:event.mfr, 
										prodGroupID:event.prodGroupID, 
										modelGroupID:event.modelGroupID, 
										model:event.model, 
										equipno:event.equipno, 
										serialno:event.serialno, 
										componentID:event.componentID, 
										componentGroupID:event.componentGroupID, 
										manualTitle:event.manualTitle, 
										manualtype:event.manualtype,
										goToPartsListTab:event.goToPartsListTab
										}
				break;
			case LegacyWebSearchEvent.SEARCH_RESULTS_LOAD:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName,
										searchResponseXML:event.searchResponseXML,
										searchParams:event.searchParams
										}
				break;	
			case LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName,
										region:event.region
										}
				break;	
			case LegacyLoadingOnEvent.ON:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName
										}
				break;
			case LegacyLoadingOffEvent.OFF:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName
										}
				break;
			case ErrorShowEvent.SHOW:
				anonymousEventObject = {
										type:event.type,
										receiverName:event.receiverName,
										message:event.message
										}
				break;
				
			case LegacyTreeEvent.ADD_TO_SELECTION:
				anonymousEventObject = {type:event.type,
										receiverName:event.receiverName
										
										}
				break;
			default:
				trace("EventSerializer.serializeEvent()   error. unhandled event.type="+event.type);
		}
		
		//trace("EventSerializer.serializeEvent()   anonymousEventObject.type="+anonymousEventObject.type);
		//trace("EventSerializer.serializeEvent()   anonymousEventObject.receiverName="+anonymousEventObject.receiverName);
		return anonymousEventObject;
	}
	
	public function deserializeEvent(anonymousEventObject:Object):BaseCommunicationEvent{
		trace("EventSerializer.deserializeEvent()   anonymousEventObject.type="+anonymousEventObject.type);
		
		var event:BaseCommunicationEvent;
		
		switch(anonymousEventObject.type){
			case LegacyTabsEvent.LOAD:
				//trace("EventSerializer.deserializeEvent()   anonymousEventObject.partsListArray="+anonymousEventObject.partsListArray);
				
				//var tempXML:XML = new XML();
				//tempXML.parseXML(anonymousEventObject.partsListArray);
				
				//var tempTopPartXML:XML = new XML();
				//tempTopPartXML.parseXML(anonymousEventObject.topPartsListArray);
				
				var tempEvent = new LegacyTabsEvent(LegacyTabsEvent.LOAD)
				tempEvent.curMCFlag = anonymousEventObject.curMCFlag;
				tempEvent.curMfrName = anonymousEventObject.curMfrName;
				tempEvent.curSpecLevel = anonymousEventObject.curSpecLevel;
				tempEvent.curSchematic = anonymousEventObject.curSchematic;
				tempEvent.partsdiagramAppear = anonymousEventObject.partsdiagramAppear;
				tempEvent.curComponent = anonymousEventObject.curComponent;
				//tempEvent.partsListArray = tempXML.childNodes;
				tempEvent.partsListURL = anonymousEventObject.partsListURL;
				tempEvent.partsListRequestXML = anonymousEventObject.partsListRequestXML;
				tempEvent.curModel = anonymousEventObject.curModel;
				tempEvent.curComponentID = anonymousEventObject.curComponentID
				tempEvent.useMasterDesc = anonymousEventObject.useMasterDesc
				//tempEvent.topPartsListArray = tempTopPartXML.childNodes;
				tempEvent.goToPartsListTab = anonymousEventObject.goToPartsListTab;
				tempEvent.curModelNumbers = anonymousEventObject.curModelNumbers;
				tempEvent.customTabs = anonymousEventObject.customTabs;
				tempEvent.curSerialNum = anonymousEventObject.curSerialNum;
				tempEvent.curDataSourceID = anonymousEventObject.curDataSourceID;
				tempEvent.colorCH = anonymousEventObject.colorCH;
				tempEvent.sysURL = anonymousEventObject.sysURL;
				tempEvent.curModelDisplay = anonymousEventObject.curModelDisplay;
				
				event = tempEvent;
				break;
			case LegacyTabsEvent.GLOBAL_SEARCH_LOAD:
				var tempEvent = new LegacyTabsEvent(LegacyTabsEvent.GLOBAL_SEARCH_LOAD)
				tempEvent.curDataSourceID = anonymousEventObject.curDataSourceID;
				tempEvent.curMfrID = anonymousEventObject.curMfrID;
				
				event = tempEvent;
				break;
			case LegacyTreeEvent.MODIFY:
				var tempEvent = new LegacyTreeEvent(LegacyTreeEvent.MODIFY)
				tempEvent.mfrgrp = anonymousEventObject.mfrgrp;
				tempEvent.mfr = anonymousEventObject.mfr;
				tempEvent.prodGroupID = anonymousEventObject.prodGroupID; 
				tempEvent.modelGroupID = anonymousEventObject.modelGroupID; 
				tempEvent.model = anonymousEventObject.model; 
				tempEvent.equipno = anonymousEventObject.equipno; 
				tempEvent.serialno = anonymousEventObject.serialno; 
				tempEvent.componentID = anonymousEventObject.componentID; 
				tempEvent.componentGroupID = anonymousEventObject.componentGroupID; 
				tempEvent.manualTitle = anonymousEventObject.manualTitle; 
				tempEvent.manualtype = anonymousEventObject.manualtype;
				tempEvent.goToPartsListTab = anonymousEventObject.goToPartsListTab;
				
				event = tempEvent;
				break;
			case LegacyWebSearchEvent.SEARCH_RESULTS_LOAD:
				var tempEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD)
				tempEvent.searchResponseXML = anonymousEventObject.searchResponseXML;
				tempEvent.searchParams = anonymousEventObject.searchParams;
				
				event = tempEvent;
				break;
			case LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD:
				var tempEvent = new LegacyWebSearchEvent(LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD)
				
				event = tempEvent;
				break;
			case LegacyLoadingOnEvent.ON:
				var tempEvent = new LegacyLoadingOnEvent(LegacyLoadingOnEvent.ON)
				
				event = tempEvent;
				break;
			case LegacyLoadingOffEvent.OFF:
				var tempEvent = new LegacyLoadingOffEvent(LegacyLoadingOffEvent.OFF)
				
				event = tempEvent;
				break;
			case ErrorShowEvent.SHOW:
				var tempEvent = new ErrorShowEvent(ErrorShowEvent.SHOW)
				tempEvent.message = anonymousEventObject.message;
				trace("EventSerializer.deserializeEvent()   anonymousEventObject.message="+anonymousEventObject.message);
				trace("EventSerializer.deserializeEvent()   anonymousEventObject['message']="+anonymousEventObject['message']);
				trace("EventSerializer.deserializeEvent()   tempEvent.message="+tempEvent.message);
				trace("EventSerializer.deserializeEvent()   anonymousEventObject.dummyProp="+anonymousEventObject.dummyProp);
				event = tempEvent;
				break;
			case LegacyTreeEvent.ADD_TO_SELECTION:
				var tempEvent = new LegacyTreeEvent(LegacyTreeEvent.ADD_TO_SELECTION)
				
				event = tempEvent;
				break;
			default:
				trace("EventSerializer.deserializeEvent()   error. unhandled anonymousEventObject.type="+anonymousEventObject.type);
		}
		
		event.receiverName = anonymousEventObject.receiverName;
		
		return event;
	}
	
}