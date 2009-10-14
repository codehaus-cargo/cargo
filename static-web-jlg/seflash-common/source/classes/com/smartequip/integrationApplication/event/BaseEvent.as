/**
 *
 */

/**
 *
 */
 
class com.smartequip.integrationApplication.event.BaseEvent {
	
	private var _type:String;
	
	public function BaseEvent(eventType:String){
		_type = eventType;
	}
	
	public function get type():String{
		return _type;
	}
	
	public function set type(value:String){
		_type = value;
	}
}