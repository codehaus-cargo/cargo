/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.LegacyWebSearchEvent extends BaseCommunicationEvent {
	
	public static var SEARCH_RESULTS_LOAD:String = "legacySearchResults2_load";
	public static var GLOBAL_SEARCH_LOAD:String = "globalSearch_load";
	
	private var _searchResponseXML:String;
	private var _searchParams:Array;
	
	public function LegacyWebSearchEvent(eventType:String){
		super(eventType);
	}
	
	public function get searchResponseXML():String{
		return _searchResponseXML;
	}
	
	public function set searchResponseXML(value:String){
		_searchResponseXML = value;
	}
	
	public function get searchParams():Array{
		return _searchParams;
	}
	
	public function set searchParams(value:Array){
		_searchParams = value;
	}
	
}