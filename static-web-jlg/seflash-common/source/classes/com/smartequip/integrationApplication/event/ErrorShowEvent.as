/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.ErrorShowEvent extends BaseCommunicationEvent {
	
	public static var SHOW:String = "com.smartequip.integrationApplication.event.ErrorShowEvent.SHOW";
	
	private var _message:String;
	
	public function ErrorShowEvent(eventType:String){
		super(eventType);
	}
	
	public function get message():String{
		return _message;
	}
	
	public function set message(value:String){
		_message = value;
	}
	
}