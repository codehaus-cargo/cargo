/**
 *
 */

/**
 *
 */
import com.smartequip.integrationApplication.event.BaseCommunicationEvent;
 
class com.smartequip.integrationApplication.event.LegacyTreeEvent extends BaseCommunicationEvent {
	
	public static var MODIFY:String = "legacyTree_modify";
	
	public static var ADD_TO_SELECTION:String = "ADD_TO_SELECTION";
	
	private var _mfrgrp:String;
	private var _mfr:String;
	private var _prodGroupID:String;
	private var _modelGroupID:String;
	private var _model:String;
	private var _equipno:String;
	private var _serialno:String;
	private var _componentID:String;
	private var _componentGroupID:String;
	private var _manualTitle:String;
	private var _manualtype:String;
	private var _goToPartsListTab:String;
	
	public function LegacyTreeEvent(eventType:String){
		super(eventType);
	}
	
	public function get mfrgrp():String{
		return _mfrgrp;
	}
	
	public function set mfrgrp(value:String){
		_mfrgrp = value;
	}
	
	public function get mfr():String{
		return _mfr;
	}
	
	public function set mfr(value:String){
		_mfr = value;
	}
	
	public function get prodGroupID():String{
		return _prodGroupID;
	}
	
	public function set prodGroupID(value:String){
		_prodGroupID = value;
	}
	
	public function get modelGroupID():String{
		return _modelGroupID;
	}
	
	public function set modelGroupID(value:String){
		_modelGroupID = value;
	}
	
	public function get model():String{
		return _model;
	}
	
	public function set model(value:String){
		_model = value;
	}
	
	public function get equipno():String{
		return _equipno;
	}
	
	public function set equipno(value:String){
		_equipno = value;
	}
	
	public function get serialno():String{
		return _serialno;
	}
	
	public function set serialno(value:String){
		_serialno = value;
	}
	
	public function get componentID():String{
		return _componentID;
	}
	
	public function set componentID(value:String){
		_componentID = value;
	}
	
	public function get componentGroupID():String{
		return _componentGroupID;
	}
	
	public function set componentGroupID(value:String){
		_componentGroupID = value;
	}
	
	public function get manualTitle():String{
		return _manualTitle;
	}
	
	public function set manualTitle(value:String){
		_manualTitle = value;
	}
	
	public function get manualtype():String{
		return _manualtype;
	}
	
	public function set manualtype(value:String){
		_manualtype = value;
	}
	
	public function get goToPartsListTab():String{
		return _goToPartsListTab;
	}
	
	public function set goToPartsListTab(value:String){
		_goToPartsListTab = value;
	}
}