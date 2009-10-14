/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseEvent;
 
class com.smartequip.integrationApplication.event.BaseCommunicationEvent extends BaseEvent {
	
	private var _receiverName:String;
	
	public function BaseCommunicationEvent(eventType:String){
		super(eventType);
	}
	
	public function get receiverName():String{
		return _receiverName;
	}
	
	public function set receiverName(value:String){
		_receiverName = value;
	}
	
	
}