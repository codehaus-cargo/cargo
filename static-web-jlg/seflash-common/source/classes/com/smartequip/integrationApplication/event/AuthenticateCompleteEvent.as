/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseEvent;
 
class com.smartequip.integrationApplication.event.AuthenticateCompleteEvent extends BaseEvent {
	
	public static var COMPLETE:String = "com.smartequip.integrationApplication.event.AuthenticateCompleteEvent.COMPLETE";
	
	public function AuthenticateCompleteEvent(eventType:String){
		super(eventType);
	}
	
}