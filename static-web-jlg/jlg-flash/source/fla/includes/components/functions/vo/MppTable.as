#include "MppMenuItems.as"
#include "StringCleaner.as"
function MppTable () {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.column = new ItemCollection();
	this.row = new ItemCollection();
	this.item = new ItemCollection()
	this.cleaner = new StringCleaner();
}
MppTable.prototype.spliter = ",";
MppTable.prototype.column;
MppTable.prototype.row;
MppTable.prototype.item;
MppTable.prototype.cleaner;


MppTable.prototype.setSpliter = function ( newSpliter) {
	this.spliter = newSpliter;
	//trace("SETTING spliter: " + this.spliter);
}

MppTable.prototype.sqlCreateTable = function ( tableColumns ) {
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


MppTable.prototype.sqlEdit = function ( fieldName , fieldValue, fieldString, newItems) {
	fieldName = this.cleaner.trim(fieldName);
	fieldValue = this.cleaner.trim(fieldValue);
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
			values = newItems.split(this.spliter);
			fields =  this.trimValue(fields);
			values = this.trimValue(values);
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

MppTable.prototype.sqlDelete = function (fieldName , fieldValue) {
	fieldName = this.cleaner.trim(fieldName);
	fieldValue = this.cleaner.trim(fieldValue);
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
MppTable.prototype.sqlInsert = function ( fieldString:String , fieldValue:String ) {
	//trace("MppTable.prototype.sqlInsert()    fieldString="+fieldString)
	//trace("MppTable.prototype.sqlInsert()    fieldValue="+fieldValue)
	
	//trace("MppTable.prototype.sqlInsert()    this.spliter="+this.spliter)
	
	var ctr = 0;
	var colCtr = 0;
	//var fields:Array = new Array();
	//var values:Array = new Array(); 
	var fields:Array
	var values:Array
	
	fields = fieldString.split(",");
	values = fieldValue.split(this.spliter);
	
	fields =  this.trimValue(fields);
	values = this.trimValue(values);
	
	//trace("MppTable.prototype.sqlInsert()    fields="+fields)
	//trace("MppTable.prototype.sqlInsert()    values="+values)
	
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
MppTable.prototype.addColumn = function (colName){
	colName = this.cleaner.trim(colName);
	this.column.addNewItem(colName);
}

MppTable.prototype.addColumnAt = function (colName , index){
	colName = this.cleaner.trim(colName);
	this.column.addNewItemAt(colName, index);
}



MppTable.prototype.deleteRow = function ( rowNum) {
	this.row.deleteItem(rowNum - 1);
}

MppTable.prototype.getColumnAt = function ( index ) {
	return this.column.getItem(index);
}

MppTable.prototype.deleteColumnAt = function (index) {
	this.column.deleteItem(index);
	var mtab = this;
	this.row.onLoop = function (target, ctr) {
		target.deleteItem(index);
	}
	this.row.loopIn();
}

MppTable.prototype.deleteColumn = function ( colName ) {
	var ctr = 0;
	colName = this.cleaner.trim(colName);
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

MppTable.prototype.deleteAllColumns = function () {
	this.column.emptyCollection();
	this.deleteAllRows();
}
MppTable.prototype.deleteAllRows = function () {
	this.row.emptyCollection();
}

MppTable.prototype.editCellAtColumn = function( newitem, colNum , rowNum){
	var icol = this.row.getItem(rowNum - 1);
	newitem = this.cleaner.trim(newitem);
	icol.addNewItemAt(newitem, colNum)
}
MppTable.prototype.editCell = function( newitem, colName , rowNum){
	newitem = this.cleaner.trim(newitem);
	colName = this.cleaner.trim(colName);
	var colCtr = 0;
	for(colCtr = 0; colCtr < this.column.getCollectionLength(); colCtr++){
		if(colName == this.column.getItem(colCtr)) {
			var icol = this.row.getItem(rowNum - 1);
			icol.addNewItemAt(newitem, colCtr);
		}
	}
}

MppTable.prototype.addRow = function ( rowItem ) {
	var ctr = 0;
	var rowval = new Array(); 
	rowval = rowItem.split(this.spliter);
	rowval = this.trimValue(rowval);
	var icol = new ItemCollection();
	for(ctr = 0; ctr < rowval.length; ctr++){
		icol.addNewItemAt(rowval[ctr],ctr);
	}
	this.row.addNewItem(icol);
}


MppTable.prototype.editRow = function ( rowItem, rowNum) {
	var icol = this.row.getItem( rowNum - 1);
	var rowItems = new Array();
	rowItems = rowItem.split(this.spliter);
	rowItems = this.trimValue(rowItems);
	for( ctr = 0; ctr < rowItems.length ; ctr++) {
		icol.addNewItemAt(rowItems[ctr], ctr);
	}
}

MppTable.prototype.getColumnIndex = function ( colName ) {
	var colIndex = null;
	colName = this.cleaner.trim(colName);
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalColumn(); ctr++) {
		 if(this.column.getItem(ctr) == colName ) {
			colIndex = ctr;
			break;
		 }
	}
	return colIndex;
}

MppTable.prototype.hasColumn = function ( colName ) {
	var boolReturn = false;
	colName = this.cleaner.trim(colName);
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalColumn(); ctr++) {
		 if(this.column.getItem(ctr) == colName ) {
			boolReturn = true;
			break;
		 }
	}
	return boolReturn;
}

MppTable.prototype.getItemAtColoumn = function ( colNum , rowNum) {
	var icol = this.row.getItem(rowNum - 1);
	return icol.getItem(colNum);
}

MppTable.prototype.getRow = function ( index , returnType ) {
	
	var icol = this.row.getItem( index - 1);
	var rowArr = new Array();
	var returnObj;
	icol.onLoop = function ( target, iIndex ){
		rowArr[iIndex] = target;
	}
	icol.loopIn();
	if(returnType == "collection"){
		returnObj =  icol;
	} else if( returnType == "array")  {
		returnObj =  rowArr;
	} else {
		returnObj = rowArr;
	}
	return returnObj;
}

MppTable.prototype.getItem  = function ( colName, rowNum ) {
	var ctr = 0;
	for( ctr = 0; ctr < this.column.getCollectionLength(); ctr++) {
		if(this.column.getItem(ctr) == colName) {
			var icol = this.row.getItem(rowNum - 1);
			return icol.getItem(ctr);
		}
	}
}

MppTable.prototype.viewTable = function () {
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

MppTable.prototype.getTotalRow = function () {
	return this.row.getCollectionLength();
}

MppTable.prototype.getTotalColumn = function () {
	this.column.onLoop = function (target, index) {
		//trace(index + ":" + target);
	}
	this.column.loopIn();
	return this.column.getCollectionLength();
}


MppTable.prototype.loopColumn = function() {
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalColumn(); ctr++) {
		this.broadcastMessage("onColumnLoop", this.column.getItem(ctr) , ctr);
	}
}

MppTable.prototype.loopRow = function () {
	var ctr = 0;
	for( ctr = 0; ctr < this.getTotalRow() ; ctr++){
		var rowStr = "";
		var colCtr = 0;
		var rowObj = this.row.getItem(ctr);
		for( colCtr = 0; colCtr < rowObj.getCollectionLength(); colCtr++) {
			if(colCtr + 1 != rowObj.getCollectionLength() ) {
				rowStr += rowObj.getItem(colCtr) + this.spliter;
			} else {
				rowStr += rowObj.getItem(colCtr);
			}
		}
		this.broadcastMessage("onRowLoop", rowObj, rowStr , ctr + 1);
	}
}

MppTable.prototype.trimValue = function ( strArray ) {
	var ctr = 0;
	for(ctr = 0; ctr < strArray.length; ctr++ ){
		strArray[ctr] = this.cleaner.trim( strArray[ctr]);
	}
	return strArray;
}