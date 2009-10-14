/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.LegacyTabsEvent extends BaseCommunicationEvent {
	
	public static var LOAD:String = "legacyTabs_load";
	public static var GLOBAL_SEARCH_LOAD:String = "legacyTabs_globalSearchLoad";
	
	private var _curMCFlag:String;
	private var _curMfrName:String;
	private var _curSpecLevel:String;
	private var _curSchematic:String;
	private var _partsdiagramAppear:String;
	private var _curComponent:String;
	//private var _partsListArray:Array;
	private var _partsListURL:String;
	private var _partsListRequestXML:String;
	private var _curComponentID:String;
	private var _curModel:String;
	private var _curModelNumbers:Array;
	private var _useMasterDesc:String;
	//private var _topPartsListArray:Array;
	private var _goToPartsListTab:String;
	private var _customTabs:String;
	private var _curSerialNum:String;
	private var _curDataSourceID:String;
	private var _colorCH:String;
	private var _sysURL:String;
	private var _curModelDisplay:String;
	private var _curMfrID:String;


	public function LegacyTabsEvent(eventType:String){
		super(eventType);
	}
	
	public function get curMCFlag():String{
		return _curMCFlag;
	}
	
	public function set curMCFlag(value:String){
		_curMCFlag = value;
	}
	
	public function get curMfrName():String{
		return _curMfrName;
	}
	
	public function set curMfrName(value:String){
		_curMfrName = value;
	}
	
	public function get curSpecLevel():String{
		return _curSpecLevel;
	}
	
	public function set curSpecLevel(value:String){
		_curSpecLevel = value;
	}
	
	public function get curSchematic():String{
		return _curSchematic;
	}
	
	public function set curSchematic(value:String){
		_curSchematic = value;
	}
	
	public function get partsdiagramAppear():String{
		return _partsdiagramAppear;
	}
	
	public function set partsdiagramAppear(value:String){
		_partsdiagramAppear = value;
	}
	
	public function get curComponent():String{
		return _curComponent;
	}
	
	public function set curComponent(value:String){
		_curComponent = value;
	}
	
	/*public function get partsListArray():Array{
		return _partsListArray;
	}
	
	public function set partsListArray(value:Array){
		_partsListArray = value;
	}*/
	
	public function get curComponentID():String{
		return _curComponentID;
	}
	
	public function set curComponentID(value:String){
		_curComponentID = value;
	}
	
	public function get curModel():String{
		return _curModel;
	}
	
	public function set curModel(value:String){
		_curModel = value;
	}
	
	public function get curModelNumbers():Array{
		return _curModelNumbers;
	}
	
	public function set curModelNumbers(value:Array){
		_curModelNumbers = value;
	}
		
	public function get useMasterDesc():String{
		return _useMasterDesc;
	}
	
	public function set useMasterDesc(value:String){
		_useMasterDesc = value;
	}
	
	/*public function get topPartsListArray():Array{
		return _topPartsListArray;
	}
	
	public function set topPartsListArray(value:Array){
		_topPartsListArray = value;
		//trace("top parts in event: "+_topPartsListArray+" len "+_topPartsListArray.length)
	}*/
	
	public function get partsListURL():String{
		return _partsListURL;
	}
	
	public function set partsListURL(value:String){
		_partsListURL = value;
	}
	
	public function get partsListRequestXML():String{
		return _partsListRequestXML;
	}
	
	public function set partsListRequestXML(value:String){
		_partsListRequestXML = value;
	}
	
	public function get goToPartsListTab():String{
		return _goToPartsListTab;
	}
	
	public function set goToPartsListTab(value:String){
		_goToPartsListTab = value;
	}
	
	public function set curSerialNum(value:String){
		_curSerialNum = value;
	}
	
	public function get curSerialNum():String{
		return _curSerialNum;
	}
	
	public function set curDataSourceID(value:String){
		_curDataSourceID = value;
	}
	
	public function get curDataSourceID():String{
		return _curDataSourceID;
	}
	
	public function set customTabs(value:String){
		_customTabs = value;
	}
	
	public function get customTabs():String{
		return _customTabs;
	}
	
	public function get colorCH():String{
		return _colorCH;
	}
	
	public function set colorCH(value:String){
		_colorCH = value;
	}
	
	public function get curModelDisplay():String{
		return _curModelDisplay;
	}
	
	public function set curModelDisplay(value:String){
		_curModelDisplay = value;
	}
	
	public function get sysURL():String{
		return _sysURL;
	}
	
	public function set sysURL(value:String){
		_sysURL = value;
	}
	
	public function get curMfrID():String{
		return _curMfrID;
	}
	
	public function set curMfrID(value:String){
		_curMfrID = value;
	}
	
}