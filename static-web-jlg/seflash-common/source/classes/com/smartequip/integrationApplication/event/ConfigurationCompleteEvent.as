/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseEvent;
 
class com.smartequip.integrationApplication.event.ConfigurationCompleteEvent extends BaseEvent {
	
	public static var COMPLETE:String = "com.smartequip.integrationApplication.event.ConfigurationCompleteEvent.COMPLETE";
	
	public function ConfigurationCompleteEvent(eventType:String){
		super(eventType);
	}
	
}