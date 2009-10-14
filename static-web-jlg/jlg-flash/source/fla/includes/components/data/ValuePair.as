/*
 *s
 */
function ValuePair() {
	trace("create new VP");
	this.pairname = new Array();
	this.pairvalue = new Array();
	this.pairvalue[0] = null;
	this.pairname[0] = null;
}
ValuePair.prototype.pairname;
ValuePair.prototype.pairvalue;
//METHODS-----//
ValuePair.prototype.addValuePair = function(Name) {
	//trace("In VP Len: " +this.pairname.length);
	this.pairvalue[this.pairname.length] = "";
	this.pairname[this.pairname.length] = Name;
};
ValuePair.prototype.setValue = function(Name, Value) {
	//trace("ADDING PAIR NAME: "+Name+" VALUE: "+Value+" CURRENT LENGHT: "+ this.pairname.length);
	for (var Ctr = 0; Ctr<this.pairname.length; Ctr++) {
		if (Name == this.pairname[Ctr]) {
			this.pairvalue[Ctr] = Value;
			//trace("ADDING: " + this.pairvalue[Ctr] );
		}
	}
};
ValuePair.prototype.getValue = function(Name) {
	for (var Ctr:Number = 0; Ctr<this.pairname.length; Ctr++) {
		//trace("Current: " + this.pairname[Ctr] + " LEN: " + this.pairname.length  );
		if (Name == this.pairname[Ctr]) {
			return this.pairvalue[Ctr];
			///trace("FOUND: " + this.pairvalue[Ctr] );
		}
	}
};
ValuePair.prototype.isExist = function(Name) {
	var exist:Boolean = false;
	for (var Ctr:Number = 0; Ctr<this.pairname.length; Ctr++) {
		if (Name == this.pairname[Ctr]) {
			exist = true;
			break;
		}
	}
	return exist;
};
ValuePair.prototype.getVPLength = function() {
	return this.pairname.length-1;
};
ValuePair.prototype.getPairname = function(index:Number) {
	return this.pairname[index];
};
