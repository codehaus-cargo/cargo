function MppMenuItem (newname, newvalue, reference){
	this.name = newname;
	this.value = newvalue;
	this.referenceFunction = reference;
}

MppMenuItem.prototype.value = "menu";
MppMenuItem.prototype.name = "name";
MppMenuItem.prototype.referenceFunctions;

MppMenuItem.prototype.setValue =  function(newvalue){
	this.value = newvalue;
}
MppMenuItem.prototype.setName = function (newname) {
	this.name = newname;
}

MppMenuItem.prototype.getValue = function (){
	return this.value;
}

MppMenuItem.prototype.getName = function () {
	return this.name;
}


MppMenuItem.prototype.setFunction  = function( reference) {
	this.referenceFunction = reference;
}

MppMenuItem.prototype.getFunction = function () {
	return this.referenceFunction;
}

///--------------------------Menu Collections ----------------------\\


function ItemCollection (){
	//trace("Item Collection");
	this.collection = new Array();
	AsBroadcaster.initialize(this);
	this.addListener(this);
}

ItemCollection.prototype.collection;

ItemCollection.prototype.addNewItem = function ( newitem ) {
	//trace("Add to Collection: " + newitem);
	this.collection[this.collection.length] = newitem;
}

ItemCollection.prototype.addNewItemAt = function ( newitem , index) {
	
	this.collection[index] = newitem;
}

ItemCollection.prototype.getItem = function (index) {
	
	return this.collection[index];
}

ItemCollection.prototype.deleteItem = function (index) {
	var newarray = new Array ();
	var ctr = 0;
	for(ctr = 0; ctr < this.collection.length ; ctr++) {
		if(ctr < index ) {
			newarray[ctr] = this.collection[ctr];
		} else if ( ctr > index ) {
			newarray[ctr - 1] = this.collection[ctr];
		} else if (ctr == index) {
		}
	}
	this.collection = newarray;
}
ItemCollection.prototype.loopIn = function () {
	var ctr = 0;
	for(ctr = 0 ; ctr < this.collection.length; ctr++ ) {
		this.broadcastMessage("onLoop", this.getItem(ctr), ctr);
	}
}

ItemCollection.prototype.emptyCollection = function (){
	var index = this.collection.length;
	
	for(index--; index >= 0; index--){
		this.broadcastMessage("onEmpty", this.getItem(index));
		this.deleteItem(index);
	}
}

ItemCollection.prototype.traceList = function () {
	for(var ctr = 0; ctr < this.collection.length ; ctr++) {
		//trace("List Len: " + this.collection.length + "Trace if Mc: " + this.collection[ctr].getDisplay());
	}
}

ItemCollection.prototype.getCollectionLength = function (){
	//trace("LEN: " + this.collection.length);
	return this.collection.length;
}
