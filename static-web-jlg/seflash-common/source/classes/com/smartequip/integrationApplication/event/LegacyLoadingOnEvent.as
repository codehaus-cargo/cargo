/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.LegacyLoadingOnEvent extends BaseCommunicationEvent {
	
	public static var ON:String = "com.smartequip.integrationApplication.event.LegacyLoadingOnEvent.ON";
	
	public function LegacyLoadingOnEvent(eventType:String){
		super(eventType);
	}
}