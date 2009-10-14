/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseEvent;
 
class com.smartequip.integrationApplication.event.AuthorizeCompleteEvent extends BaseEvent {
	
	public static var COMPLETE:String = "com.smartequip.integrationApplication.event.AuthorizeCompleteEvent.COMPLETE";
	
	public function AuthorizeCompleteEvent(eventType:String){
		super(eventType);
	}
	
}