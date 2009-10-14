#include "ValuePair.as"

function Table(){
	this.columns = new Array();
	this.inventory = new Array();
}


Table.prototype.columns;
Table.prototype.inventory;
Table.prototype.addNewColumn = function (colname){
	trace("COL " + this.columns.length);
	this.inventory[this.columns.length] = new Array();
	this.columns[this.columns.length] = colname;
}

Table.prototype.createColumns = function (COLUMNS) {
	this.columns = COLUMNS.split(",");
	var ctr;
	for(ctr = 0; ctr < this.columns.length; ctr++){
		this.inventory[ctr] = new Array();
	}
}

Table.prototype.insert = function ( colName ,  Value, index){
	var ctr;
	var colNumber;
	for(ctr = 0; ctr < this.columns.length; ctr++){
		if(this.columns[ctr] == colName){
			colNumber = ctr;
			break;
		}
	}
	this.inventory[colNumber][index] = Value;
}


Table.prototype.getValue = function(colName, index){
	var ctr;
	var colNumber;
	for(ctr = 0; ctr < this.columns.length; ctr++){
		if(this.columns[ctr] == colName){
			colNumber = ctr;
			trace("Column:" + this.columns[ctr]);
			break;
		}
	}
	return  this.inventory[colNumber][index];
}


Table.prototype.getColumnNumber = function(colName, index){
	var ctr;
	var colNumber;
	for(ctr = 0; ctr < this.columns.length; ctr++){
		if(this.columns[ctr] == colName){
			colNumber = ctr;
			trace("Column:" + this.columns[ctr]);
			break;
		}
	}
	return  colNumber;
}
Table.prototype.getTotalColumn = function (){
		return this.columns.length - 1;
}

Table.prototype.getTotalRow = function (){
	return this.inventory[0].length;
}

Table.prototype.insertLikeSQL = function (COLNAMES, COLVALUES) {
	var colarr = new Array();
	var colval = new Array();
	var ctr;
	var indexctr;
	var nomatch = false;
	colarr = COLNAMES.split(",");
	colval = COLVALUES.split(",");
	//verify existance of column
	if( colarr.length == this.columns.length){
		for(ctr = 0; ctr < colarr.length ; ctr++){
			for(indexctr = 0; indexctr < this.columns.length; indexctr++){
				//trace("entering:  "  + nomatch +  "  inqueary: "  +  colarr[ctr] +  " exisiting: " + this.columns[indexctr]);
				if(colarr[ctr] == this.columns[indexctr]){
					//this.inventory[indexctr][this.inventory[indexctr].length] = colval[ctr];
					nomatch = false;
					break;
				}else {
					
					nomatch = true;
				}
			}
		}
	} else {
		trace("INSERT ERROR: The length of the column query is either to short or to long for the actual column");
	}
	
	if(nomatch == true){
		trace("ERROR: Column Query did not match the actual existing columns");
	} else {
		for(ctr = 0; ctr < colarr.length ; ctr++){
			for(indexctr = 0; indexctr < this.columns.length; indexctr++){
				if(colarr[ctr] == this.columns[indexctr]){
					this.inventory[indexctr][this.inventory[indexctr].length] = colval[ctr];
				}
			}
		}
	}
}