var SmartEquipVars_Instance = 0;
function SmartEquipVars () {
	SmartEquipVars_Instance++;
	if( SmartEquipVars_Instance > 1 ){
	}else {
		this.BASE_URL = _level0.baseURL;
		this.REQUEST_DIRECTOR = _level0.baseURL + "servlet/RequestDirector?helper=";
		this.DOMAIN_URL = this.generateDomainURL( this.BASE_URL );
	}
	//_level0.baseURL add "servlet/RequestDirector?helper=";
}

SmartEquipVars.prototype.setVars = function () {
	
}

SmartEquipVars.prototype.generateDomainURL = function ( _targetString ) {
		var targetString  =   _targetString;
		var URLARR  = new Array();
		 
		URLARR = targetString.split("//");
		 
		var full  = URLARR[1];
		 
		URLARR = new Array();
		URLARR = full.split("/");
		return "http://" + URLARR[0] + "/" ;
}

SmartEquipVars.prototype.BASE_URL;
SmartEquipVars.prototype.REQUEST_DIRECTOR;
SmartEquipVars.prototype.DOMAIN_URL;
var SEVars = new SmartEquipVars();