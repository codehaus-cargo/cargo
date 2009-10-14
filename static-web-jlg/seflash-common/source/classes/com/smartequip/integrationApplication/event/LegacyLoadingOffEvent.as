/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.LegacyLoadingOffEvent extends BaseCommunicationEvent {
	
	public static var OFF:String = "com.smartequip.integrationApplication.event.LegacyLoadingOffEvent.OFF";
	
	public function LegacyLoadingOffEvent(eventType:String){
		super(eventType);
	}
}