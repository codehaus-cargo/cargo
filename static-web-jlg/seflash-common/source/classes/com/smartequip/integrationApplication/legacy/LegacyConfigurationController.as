/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
import com.smartequip.integrationApplication.event.ConfigurationCompleteEvent;

class com.smartequip.integrationApplication.legacy.LegacyConfigurationController extends EventDispatcher{
	
	private var loadConfigurationIntervalID;
	
	public function LegacyConfigurationController(){
	}
	
	
	public function init(){
		trace("LegacyConfigurationController.init()")
		
		loadConfigurationIntervalID = setInterval(this, "loadConfigurationInterval", 200);
		
		loadVariablesNum(_level0.staticWebURL + "localconfig.txt", 0);
	}
	
	
	private function loadConfigurationInterval() {
		if (_level0.end == "true") {
			clearInterval(loadConfigurationIntervalID);
			
			trace("LegacyConfigurationController.loadConfigurationInterval()   dispatch ConfigurationCompleteEvent")
			
			//dispatchEvent({type:"configuration_complete"})
			var event:ConfigurationCompleteEvent = new ConfigurationCompleteEvent(ConfigurationCompleteEvent.COMPLETE);
			dispatchEvent(event)
		}
	}
	
	public function get legacyComponentType():String{
		if(_level0.legacyComponentType==undefined) _level0.legacyComponentType = LegacyComponentData.WEB_SEARCH
		
		return _level0.legacyComponentType;
	}
	
	public function get uniqueId():String{
		if(_level0.uniqueId==undefined) _level0.uniqueId = "uniqueId";
		
		return _level0.uniqueId;
	}
}
	