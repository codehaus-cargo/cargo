#include "functions/vo/MppMenuItems.as"
#include "MppRadioButton.as"
function MppRadioGroup  () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	
	this.radioList = new ItemCollection();
}

MppRadioGroup.prototype.radioList;

MppRadioGroup.prototype.addRadioButton = function ( radioButton ) {
	this.radioList.addNewItem(radioButton);
	var radgroup = this;
	this.radioList.onLoop = function ( target ) {
		target.addToGroup(radgroup.radioList);
	}
	this.radioList.loopIn();
}
MppRadioGroup.prototype.removeRadioButton = function ( index ) {
	var radgroup = this;
	this.radioList.onLoop = function ( target, counter ) {
		if(index == counter ) {
			target.removeToGroup();
		}
	}
	
	this.radioList.loopIn();
	this.radioList.deleteItem(index);
	this.radioList.onLoop = function ( target ) {
		target.addToGroup(radgroup.radioList);
	}
	this.radioList.loopIn();
}