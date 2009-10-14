#include "MppMenuItems.as"

function ObjectTable () {
	//trace("CREATING OBJECT TABLE");
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.column = new ItemCollection();
	this.row = new ItemCollection();
	this.item = new ItemCollection()
	
}


ObjectTable.prototype.column;
ObjectTable.prototype.row;
ObjectTable.prototype.item;

ObjectTable.prototype.insertAt = function ( colNum, rowNum, itemObject ) {
	//trace("insertAt STATUS: " + this.row.getItem(rowNum) +  " Col: " + colNum + " Row " + rowNum);
	if( this.row.getItem(rowNum) == undefined || this.row.getItem(rowNum) == null){
		var icol = new ItemCollection();
	} else {
	 	var icol = this.row.getItem(rowNum);
	}
	//trace("ICol: " + icol.getCollectionLength());
	icol.addNewItemAt(itemObject, colNum);
	this.row.addNewItemAt(icol, rowNum);
	//trace(" NEW: " + this.getItemAtColumn( colNum, rowNum));
}

ObjectTable.prototype.sqlCreateTable = function ( tableColumns ) {
	if(this.column.getCollectionLength() == 0 ) {
		var tableCol = new Array();
		var ctr = 0;
		tableCol = tableColumns.split(",");
		for(ctr = 0; ctr < tableCol.length; ctr++) {
			this.addColumn(tableCol[ctr]);
		}
	} else {
		trace("Table Error: Can not create new table");
	}
}


ObjectTable.prototype.sqlEdit = function ( fieldName , fieldValue, fieldString, newItems) {
	
	var colCtr = 0;
	var rowCtr = 0;
	for(colCtr = 0; colCtr < this.getTotalColumn(); colCtr++) {
		if( fieldName == this.column.getItem(colCtr)) {
			break;
		}
	}
	for(rowCtr = 0; rowCtr < this.getTotalRow(); rowCtr++) {
		var icol = this.row.getItem(rowCtr);
		if(icol.getItem(colCtr) == fieldValue){
			var fields = new Array();
			var values = new Array();
			var fldCtr = 0;
			var colCtr2 = 0;
			fields = fieldString.split(",");
			values = newItems.split(",");
			
				for(fldCtr = 0; fldCtr < fields.length; fldCtr++) {
					for(colCtr2 = 0; colCtr2 < this.column.getCollectionLength();colCtr2++) {
						if(fields[fldCtr] == this.column.getItem(colCtr2)){
							icol.addNewItemAt(values[fldCtr], colCtr2);
							break;
						}
					}
				}
			this.row.addNewItemAt(icol, rowCtr);
		}
	}
}

ObjectTable.prototype.sqlDelete = function (fieldName , fieldValue) {
	var colCtr = 0;
	var rowCtr = 0;
	for(colCtr = 0; colCtr < this.getTotalColumn(); colCtr++) {
		if( fieldName == this.column.getItem(colCtr)) {
			break;
		}
	}
	for(rowCtr = 0; rowCtr < this.getTotalRow(); rowCtr++) {
		var icol = this.row.getItem(rowCtr);
		if(icol.getItem(colCtr) == fieldValue){
			this.row.deleteItem(rowCtr);
		}
	}
	
}

ObjectTable.prototype.deleteAllColumns = function () {
	this.column.emptyCollection();
	this.deleteAllRows();
}
ObjectTable.prototype.deleteAllRows = function () {
	this.row.emptyCollection();
}


ObjectTable.prototype.sqlInsert = function ( fieldString , fieldValue ) {
	var ctr = 0;
	var colCtr = 0;
	var fields = new Array();
	var values = new Array(); 
	fields = fieldString.split(",");
	values = fieldValue.split(",");
	var icol = new ItemCollection();
	for(ctr = 0; ctr < fields.length; ctr++) {
		for(colCtr = 0; colCtr < this.column.getCollectionLength(); colCtr++) {
			if(fields[ctr] == this.column.getItem(colCtr)){
				icol.addNewItemAt(values[ctr], colCtr);
				break;
			}
		}
	}
	this.row.addNewItem(icol);
}
ObjectTable.prototype.addColumn = function (colName){
	this.column.addNewItem(colName);
}

ObjectTable.prototype.addColumnAt = function (colName , index){
	//trace("OBJECT TABLE: ADDCOLAT: " + colName);
	this.column.addNewItemAt(colName, index);
}

ObjectTable.prototype.deleteRow = function ( rowNum) {
	this.row.deleteItem(rowNum - 1);
}

ObjectTable.prototype.getRow = function ( rowNum ){
	return this.row.getItem(rowNum - 1);
}

ObjectTable.prototype.getColumnAt = function ( index ) {
	return this.column.getItem(index);
}

ObjectTable.prototype.deleteColumnAt = function (index) {
	this.column.deleteItem(index);
	var mtab = this;
	this.row.onLoop = function (target, ctr) {
		target.deleteItem(index);
	}
	this.row.loopIn();
}

ObjectTable.prototype.deleteColumn = function ( colName ) {
	var ctr = 0;
	for( ctr = 0; ctr < this.column.getCollectionLength(); ctr++) {
		if(this.column.getItem(ctr) == colName) {
			this.column.deleteItem(ctr);
			this.row.onLoop = function (target, index) {
				target.deleteItem(ctr);
			}
			this.row.loopIn();
			break;
		}
	}
	
}

ObjectTable.prototype.editCellAtColumn = function( newitem, colNum , rowNum){
	var icol = this.row.getItem(rowNum - 1);
	
	icol.addNewItemAt(newitem, colNum)
}
ObjectTable.prototype.editCell = function( newitem, colName , rowNum){
	var colCtr = 0;
	for(colCtr = 0; colCtr < this.column.getCollectionLength(); colCtr++){
		if(colName == this.column.getItem(colCtr)) {
			var icol = this.row.getItem(rowNum - 1);
			icol.addNewItemAt(newitem, colCtr);
		}
	}
}

ObjectTable.prototype.addRow = function ( rowItem ) {
	var ctr = 0;
	var rowval = new Array(); 
	rowval = rowItem.split(",");
	
	var icol = new ItemCollection();
	for(ctr = 0; ctr < rowval.length; ctr++){
		icol.addNewItemAt(rowval[ctr],ctr);
	}
	this.row.addNewItem(icol);
}


ObjectTable.prototype.editRow = function ( rowItem, rowNum) {
	var icol = this.row.getItem( rowNum - 1);
	var rowItems = new Array();
	rowItems = rowItem.split(",");

	for( ctr = 0; ctr < rowItems.length ; ctr++) {
		icol.addNewItemAt(rowItems[ctr], ctr);
	}
}

ObjectTable.prototype.getItemAtColumn = function ( colNum , rowNum) {
	var icol = this.row.getItem(rowNum - 1);
	return icol.getItem(colNum);
}

ObjectTable.prototype.getItem  = function ( colName, rowNum ) {
	var ctr = 0;
	for( ctr = 0; ctr < this.column.getCollectionLength(); ctr++) {
		if(this.column.getItem(ctr) == colName) {
			var icol = this.row.getItem(rowNum - 1);
			return icol.getItem(ctr);
		}
	}
}

ObjectTable.prototype.viewTable = function () {
	var rowCtr = 0;
	var colCtr = 0;
	trace("\n[Table]");
	for(rowCtr = 0;  rowCtr < this.row.getCollectionLength(); rowCtr++) {
		var icol = this.row.getItem(rowCtr);
		//trace("Row [" + (rowCtr + 1) + "]");
		var strRow = "Row [" + (rowCtr + 1) + "]";
		for(colCtr = 0; colCtr < this.column.getCollectionLength(); colCtr++) {
			//trace("\t[" + this.column.getItem(colCtr) + ":" + icol.getItem(colCtr) + "]");
			strRow += "[" + this.column.getItem(colCtr) + ":" + icol.getItem(colCtr) + "]";
		}
		trace(strRow);
	}
}

ObjectTable.prototype.getTotalRow = function () {
	return this.row.getCollectionLength();
}

ObjectTable.prototype.getTotalColumn = function () {
	this.column.onLoop = function (target, index) {
		//trace(index + ":" + target);
	}
	this.column.loopIn();
	return this.column.getCollectionLength();
}


ObjectTable.prototype.loopColumn = function() {
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalColumn(); ctr++) {
		this.broadcastMessage("onColumnLoop", this.column.getItem(ctr) , ctr);
	}
}

ObjectTable.prototype.loopRow = function () {
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalRow() ; ctr++){
		var rowItems = new Array();
		var colCtr = 0;
		var rowObj = this.row.getItem(ctr);
		for( colCtr = 0; colCtr < rowObj.getCollectionLength(); colCtr++) {
			rowItems [ colCtr ] = rowObj.getItem(colCtr);
		}
		this.broadcastMessage("onRowLoop", rowObj, rowItems , ctr + 1);
	}
}