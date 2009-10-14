#include "MppScrollBar.as"
#include "MppTextLabel.as"
#include "MppText.as"
#include "MppCanvas.as"
#include "functions/vo/MppTable.as"
#include "functions/vo/ObjectTable.as"

#include "MppToolTip.as"

#include "functions/DrawFunction.as"

function MppDataGrid(container, newDepth){
	AsBroadcaster.initialize(this);
	this.addListener(this);
	
	this.container_mc = container;
	this.depth = newDepth;
	this.aligner = new MppAligner();
	this.colWidthCollection = new ItemCollection();
	this.vgridCollection = new ItemCollection();
	this.data_table = new MppTable();
	this.display_table = new ObjectTable();
	this.createClips();
	this.paint();
	this.addParentEvent();
}
//------------------------------PROPERTIES
MppDataGrid.prototype.aligner;

MppDataGrid.prototype.container_mc;
MppDataGrid.prototype.grid_mc;
MppDataGrid.prototype.depth

MppDataGrid.prototype.data_mc;
MppDataGrid.prototype.headdata_mc;

MppDataGrid.prototype.border_mc;
MppDataGrid.prototype.header_mc;
MppDataGrid.prototype.contentCanvas;
MppDataGrid.prototype.scrollpane_mc;

MppDataGrid.prototype.roll_mc;
MppDataGrid.prototype.selected_mc;

MppDataGrid.prototype.vgrid_mc;

MppDataGrid.prototype.grid_tooltip;

//
MppDataGrid.prototype.grid_width = 400;
MppDataGrid.prototype.grid_height = 300;
MppDataGrid.prototype.last_column_width;

MppDataGrid.prototype.header_height = 20;
MppDataGrid.prototype.scrollpane_width = 15;
MppDataGrid.prototype.default_cell_width = 100;

//---COLORS 
MppDataGrid.prototype.color_border = 0xB7B7B7
MppDataGrid.prototype.color_header_bg = 0xF7F7F7;
MppDataGrid.prototype.color_scroll_bg = 0xF7F7F7;
MppDataGrid.prototype.color_content = 0xFFFFFF;
MppDataGrid.prototype.color_cell_bg = 0xC8C8C8;

MppDataGrid.prototype.color_roll = 0xDCFFC4;
MppDataGrid.prototype.color_selected = 0xC5FE89;

//---DATA
MppDataGrid.prototype.data_table;
MppDataGrid.prototype.display_table;
MppDataGrid.prototype.colWidthCollection;
MppDataGrid.prototype.vgridCollection;
MppDataGrid.prototype.spliter = ",";

MppDataGrid.prototype.lbl_alpha_border = 0;
MppDataGrid.prototype.lbl_alpha_bg = 0;

MppDataGrid.prototype.lbl_selectable = false;
MppDataGrid.prototype.lbl_editable = false;

MppDataGrid.prototype.bool_tooltip = true;

//--SELECTION

MppDataGrid.prototype.selected_row;
MppDataGrid.prototype.selected_column;
MppDataGrid.prototype.selected_item;

MppDataGrid.prototype.current_column;
MppDataGrid.prototype.current_row;

MppDataGrid.prototype.currentCell_lbl;

//TEXT FORMAT --



MppDataGrid.prototype.format_size_header = 12;
MppDataGrid.prototype.format_font_header = "Arial";
MppDataGrid.prototype.format_color_header = 0x000000;
MppDataGrid.prototype.format_align_header = "left";

MppDataGrid.prototype.format_bold_header =  false;
MppDataGrid.prototype.format_underline_header = false;
MppDataGrid.prototype.format_italic_header = false;

MppDataGrid.prototype.format_size_data = 12;
MppDataGrid.prototype.format_font_data = "Arial";
MppDataGrid.prototype.format_color_data = 0x000000;
MppDataGrid.prototype.format_align_data = "left";

MppDataGrid.prototype.format_bold_data =  false;
MppDataGrid.prototype.format_underline_data = false;
MppDataGrid.prototype.format_italic_data = false;


MppDataGrid.prototype.interactive = true;

//--Public Methods

//SKin And DIsplay

MppDataGrid.prototype.setInteractivity = function ( bool ) {
	this.interactive = bool;
}

MppDataGrid.prototype.getDisplay = function () {
	return this.grid_mc;
}

MppDataGrid.prototype.setSkinColors = function (contentColor , scrollBgColor , borderColor) {
	this.color_border = borderColor;
	this.color_scroll_bg = scrollBgColor;
	this.color_content  =  contentColor;
	this.paint();
	this.setHeaderProperties();
	this.setLineColors();
}

MppDataGrid.prototype.setHeaderColor = function ( headerColor ) {
	this.color_header_bg = headerColor;
	this.paint();
}

MppDataGrid.prototype.setGlowSkin = function ( hoverColor , selectedColor ) {
	this.color_roll = hoverColor;
	this.color_selected = selectedColor;
	this.paintGlow();
}

MppDataGrid.prototype.setSize = function  ( newWidth , newHeight) {
	this.grid_width = newWidth;
	this.grid_height = newHeight;
	this.paint();
}

MppDataGrid.prototype.setDimension = function ( newX  , newY ) {
	this.grid_mc._x = newX;
	this.grid_mc._y = newY;
}

MppDataGrid.prototype.setCellBgColor = function ( cellBgColor ) {
	this.color_cell_bg = cellBgColor;
	this.setCellProperties();
}

//////TABLE AND DATA


MppDataGrid.prototype.setSpliter = function ( newSpliter) {
	this.spliter = newSpliter;
	this.data_table.setSpliter(this.spliter);
}



MppDataGrid.prototype.addColumn = function ( newColumn  , colWidth) {
	this.data_table.addColumn(newColumn);
	this.colWidthCollection.addNewItem(colWidth);
	this.showTable();
}

MppDataGrid.prototype.insertTable = function ( newTable, colsWidthString ) {
	this.colWidthCollection.emptyCollection();
	this.deleteAllColumns();
	var ctr = 0;
	var colsWidth = new Array();
	//trace("ColsWIdht: " + colsWidthString);
	colsWidth = colsWidthString.split(",");
	//trace("ARR: " + colsWidth);
	for(ctr = 0 ; ctr < colsWidth.length ; ctr++){
		//trace( "Current WIDHT: " + colsWidth[ctr]);
		this.colWidthCollection.addNewItem(int(colsWidth[ctr]));
	}
	this.data_table = null;
	this.data_table = newTable;
	this.showTable();
	this.generateGrid();
}

MppDataGrid.prototype.showTable = function  () {
	this.generateHeader();
	this.arrangeHeaderDisplay();
}

MppDataGrid.prototype.sqlInsert = function ( columns, columnValues ) {
	//trace("Columns: " +  columns); ;
	this.data_table.sqlInsert( columns, columnValues);
}

MppDataGrid.prototype.refreshGrid = function () {
	//this.data_table.viewTable();
	this.generateGrid();
	this.broadcastMessage("onRefresh");
}
MppDataGrid.prototype.sqlDelete = function ( columnName , columnValue ) {
	//this.data_table.viewTable();
	this.data_table.sqlDelete( columnName, columnValue );
	//this.data_table.viewTable();
	this.generateGrid();
}


MppDataGrid.prototype.editCellData = function ( newData, columnName, rowNum ) {
	this.data_table.editCell(newData, columnName, rowNum);
	this.generateGrid();
}

MppDataGrid.prototype.editCellDataAt = function ( newData, columnNum, rowNum ) {
	this.data_table.editCellAtColumn( newData, columnNum , rowNum);
	this.generateGrid();
}

MppDataGrid.prototype.deleteAllColumns = function (){
	this.data_table.deleteAllColumns();
	this.colWidthCollection.emptyCollection();
	this.showTable();
	this.generateGrid();
}

MppDataGrid.prototype.deleteAllRows = function () {
	this.data_table.deleteAllRows();
	this.generateGrid();
}

MppDataGrid.prototype.deleteColumn = function ( columnName ) {
	var colIndex = this.data_table.getColumnIndex(columnName);
	//trace("colIndex: " + colIndex + "NAME: " + columnName);
	this.data_table.deleteColumn(columnName);
	this.colWidthCollection.deleteItem(colIndex);
	this.showTable();
	if(this.data_table.getTotalRow != 0 ) {
		this.generateGrid();
	}
}

MppDataGrid.prototype.deleteColumnAt = function ( columnIndex ) {
	this.data_table.deleteColumnAt( columnIndex ) ;
	this.colWidthCollection.deleteItem( columnIndex );
	this.showTable();
	if(this.data_table.getTotalRow != 0 ) {
		this.generateGrid();
	}
}



// METHOD PROPERTIES

MppDataGrid.prototype.setVisibility = function ( bool ) {
	this.grid_mc._visible = bool;
}

MppDataGrid.prototype.setCellEditable = function ( bool ) {
	this.lbl_editable = bool;
	this.setCellProperties();
}

MppDataGrid.prototype.setCellSelectable = function ( bool ) {
	this.lbl_selectable = bool;
	this.setCellProperties();
}

MppDataGrid.prototype.showHorizontalGrid = function  ( bool) {
	if( bool == true ) {
		this.lbl_alpha_border = 100;
	} else {
		this.lbl_alpha_border = 0;
	}
	this.setCellProperties();
}

//---

MppDataGrid.prototype.tooltipOn = function ( bool ) {
	this.bool_tooltip =bool;
}

MppDataGrid.prototype.showRoll = function ( newX,  newY ) {
	this.roll_mc._y = newY;
	this.roll_mc._visible = true;
}

MppDataGrid.prototype.hideRoll = function () {
	this.roll_mc._visible = false;
}

MppDataGrid.prototype.showSelected = function (newX, newY) {
	this.selected_mc._y = newY ;
	this.selected_mc._visible = true;
}

MppDataGrid.prototype.hideSelected = function () {
	this.selected_mc._visible = false;
}

MppDataGrid.prototype.hideHighLights = function () {
	this.hideRoll();
	this.hideSelected();
}

//GET COMPONENTS

MppDataGrid.prototype.getScrollBar = function () {
	return this.contentCanvas.getScrollBar();
}

MppDataGrid.prototype.getToolTip = function () {
	return this.grid_tooltip;
}


MppDataGrid.prototype.getCellAsLabel = function ( colNum, rowNum ) {
	return this.display_table.getItemAtColumn( colNum, rowNum);
}

//---------TEXT FORMAT

MppDataGrid.prototype.setHeaderTextWeight = function ( newBold , newItalic, newUnderline ) {
	this.format_bold_header = newBold;
	this.format_italic_header = newItalic;
	this.format_underline_header = newUnderline;
	this.setHeaderProperties();
}

MppDataGrid.prototype.setHeaderTextFormat = function ( newFont, newSize, newColor, newAlign) {
	this.format_font_header = newFont;
	this.format_size_header = newSize;
	this.format_color_header = newColor;
	this.format_align_header = newAlign;
	this.setHeaderProperties();
}


MppDataGrid.prototype.setDataTextWeight = function ( newBold , newItalic, newUnderline ) {
	this.format_bold_data = newBold;
	this.format_italic_data = newItalic;
	this.format_underline_data = newUnderline;
	this.setCellProperties();
}

MppDataGrid.prototype.setDataTextFormat = function ( newFont, newSize, newColor, newAlign) {
	this.format_font_data = newFont;
	this.format_size_data = newSize;
	this.format_color_data = newColor;
	this.format_align_data = newAlign;
	this.setCellProperties();
}

//--Private Methods


MppDataGrid.prototype.setCellProperties = function () {
	var mgrid = this;
	this.display_table.onRowLoop = function ( target, rowItems, rowNum){
		target.onLoop = function ( target_lbl , columnNum){
			mgrid.setLabelProperties( target_lbl);
		}
		target.loopIn();
	}
	this.display_table.loopRow();
}

MppDataGrid.prototype.setLabelProperties = function  ( mpplabel ) {
	mpplabel.setBgColor ( this.color_cell_bg);
	mpplabel.setBorderColor(this.color_border);
	mpplabel.setBorderAlpha( this.lbl_alpha_border);
	mpplabel.setBgAlpha( this.lbl_alpha_bg);
	mpplabel.setSelectable( this.lbl_selectable);
	mpplabel.setEditable( this.lbl_editable);
	mpplabel.setWeight( this.format_bold_data,  this.format_italic_data, this.format_underline_data);
	mpplabel.setFormat ( this.format_font_data, this.format_size_data, this.format_color_data, this.format_align_data);
}




MppDataGrid.prototype.setHeaderProperties = function  () {
		var mgrid = this;
		this.display_table.onColumnLoop = function  ( target, index ) {
			mgrid.setHeaderLabelProperties ( target ); 
		}
		this.display_table.loopColumn();
}

MppDataGrid.prototype.setHeaderLabelProperties = function (header_lbl) {
	//trace(" HEADER: " + header_lbl.getDisplay());
	header_lbl.setBorderColor( this.color_border );
	header_lbl.setWeight( this.format_bold_header,  this.format_italic_header, this.format_underline_header);
	header_lbl.setFormat ( this.format_font_header, this.format_size_header, this.format_color_header, this.format_align_header);
}

MppDataGrid.prototype.setLineColors = function () {
	var mgrid = this;	
	this.vgridCollection.onLoop = function (target, index ) {
		var mcolor = new Color ( target ) ;
		mcolor.setRGB(mgrid.color_border);
	}
	this.vgridCollection.loopIn();
}


//GENERATOR
MppDataGrid.prototype.columns;
MppDataGrid.prototype.generateHeader = function () {
	//trace("MppDataGrid.prototype.generateHeader()")
	this.cleanObjectColumn();
	var mpptable = this.data_table;
	var mgrid = this;
	this.headdata_mc.removeMovieClip();
	this.headdata_mc = this.header_mc.createEmptyMovieClip("data_mc", 1);
	this.columns  = new Array();
	mpptable.onColumnLoop = function ( target, index ) {
		var header_lbl = new MppTextLabel ( mgrid.headdata_mc, index);
		//trace("Header" + index + ":" + target + " Width: " +  mgrid.colWidthCollection.getItem(index) + " index: " + index + " Header LBL:" + header_lbl.getDisplay());
		var column = new Object();
		column.cells = new Array();
		column.name = target;
		column.index = index;
		
		mgrid.columns[  index ] =  column; 
		
		mgrid.display_table.addColumnAt(header_lbl, index);
		header_lbl.setSize( mgrid.colWidthCollection.getItem(index), mgrid.header_height)
		header_lbl.setLabel( target);
		
		header_lbl.setBorderAlpha(100);
		mgrid.aligner.verticalCenter(mgrid.header_mc, header_lbl.getDisplay());
	}
	
	mpptable.loopColumn();
}

MppDataGrid.prototype.cells;
 

MppDataGrid.prototype.generateGrid = function () {
	//trace("MppDataGrid.prototype.generateGrid()")
	var mgrid = this;
	var pre_lbl = null;
	var depthCtr = 0;
	this.cleanGridObjects();
	this.cells = new Array();
	 
	this.data_mc = mgrid.contentCanvas.getContainer().createEmptyMovieClip("data_mc", 1);
	mgrid.data_table.onRowLoop = function ( target, rowItems, rowNum ) {
		
		var itemArr = new Array();
		itemArr = rowItems.split(this.spliter);
		
		pre_lbl = null;
		var yspacing = (rowNum - 1) * mgrid.header_height; 
		for(var ctr = 0; ctr < itemArr.length; ctr++ ) {
			depthCtr++;
			//trace("rloop ROWNUM: " + rowNum + " " + rowItems + " ROWTOTAL: " +  mgrid.display_table.getTotalRow() );
			
			var data_lbl = new MppTextLabel( mgrid.data_mc , depthCtr   );//rowNum  + "00" + ctr
			mgrid.cells.push( data_lbl);
			data_lbl.setLabel(itemArr[ctr]);
			data_lbl.columnIndex = ctr;
			data_lbl.rowIndex = rowNum;
			data_lbl.rowItems = itemArr;
			
			mgrid.columns[ctr].cells.push ( data_lbl ); 
			//trace("Headers: " +  mgrid.columns[ctr].name + " item: " + itemArr[ctr] );
			if( ctr != itemArr.length - 1) {
				data_lbl.setSize(mgrid.colWidthCollection.getItem(ctr), mgrid.header_height );
			} else {
				//trace("LAST WIDTH: " + mgrid.last_column_width + " LastItem: " +  itemArr[ctr] );
				data_lbl.setSize(mgrid.last_column_width , mgrid.header_height );
			}
			data_lbl.setDimension(0,yspacing);
			//var tre_lbl = mgrid.display_table.getItemAtColumn( ctr - 1 , rowNum ); 
			//trace("PREVIOUS: " + pre_lbl.getDisplay() + " CURRENT: " +  data_lbl.getDisplay() + " TRE: " + tre_lbl.getDisplay());
			if( pre_lbl != null) {
				mgrid.aligner.rightOf( pre_lbl.getDisplay(), data_lbl.getDisplay());
			}
			mgrid.display_table.insertAt(ctr, rowNum - 1 , data_lbl);
			pre_lbl = data_lbl;
		}
		
	}

	this.data_table.loopRow();
	this.broadcastMessage("onProcessComplete");
	
	var parent_mc = this.contentCanvas.getContainer();
	this.roll_mc.removeMovieClip();
	this.selected_mc.removeMovieClip();
	depthCtr++;
	this.roll_mc = parent_mc.createEmptyMovieClip("roll_mc", depthCtr);
	depthCtr++;
	this.selected_mc = parent_mc.createEmptyMovieClip("selected_mc", depthCtr);
	this.paintGlow();
	this.setCellProperties();
	this.addEvents();
	this.contentCanvas.refreshCanvas();
}

//CLEANER 
MppDataGrid.prototype.cleanGridObjects = function () {
	var mgrid = this;
	this.display_table.onRowLoop = function ( target, rowItems, rowNum ) {
		target.onLoop = function (target_lbl, index) {
			//trace("In target Label: " +  target_lbl.getLabel() );
			target_lbl.deleteMe();
		}
		target.loopIn();
	}
	this.display_table.loopRow();
	this.data_mc.removeMovieClip();
}

MppDataGrid.prototype.cleanObjectColumn = function () {
	for( var ctr = 0; ctr < this.display_table.getTotalColumn() ; ctr++){
		var mpp_lbl = this.display_table.getColumnAt(ctr);
		mpp_lbl.deleteMe();
	}
	this.display_table.deleteAllColumns();
}

MppDataGrid.prototype.cleanLines = function () {
	var mgrid = this;
	this.vgridCollection.onLoop = function ( target_mc , index) {
		target_mc.removeMovieClip();
	}
	this.vgridCollection.loopIn();
	this.vgridCollection.emptyCollection();
}

//ARRANGER

MppDataGrid.prototype.arrangeHeaderDisplay = function () {
	var dis_tab = this.display_table;
	var totalWidth = 0;
	var mgrid = this;
	this.cleanLines();
	dis_tab.onColumnLoop = function ( target, index ) {
		var  line_mc = mgrid.vgrid_mc.createEmptyMovieClip("line" + index + "_mc", index);
		mgrid.vgridCollection.addNewItem(line_mc);
		
		drawPixelVLine(line_mc ,mgrid.color_border,  0,0, mgrid.grid_height - mgrid.header_height + 1, 100)
		mgrid.aligner.bottomOf(mgrid.header_mc, mgrid.vgrid_mc);
		//trace("DISPLAY INDEX: " + index + " Target:" + target + " TWIDTH: " +  totalWidth + " TOTAL COL " + dis_tab.getTotalColumn());
		if(index == dis_tab.getTotalColumn() - 1) {
			var lastWidth = mgrid.grid_width - totalWidth - dis_tab.getTotalColumn() + 1 ;
			mgrid.last_column_width = lastWidth - mgrid.scrollpane_width - 2;
			target.setSize(lastWidth ,  mgrid.header_height);
		} 
		totalWidth  += mgrid.colWidthCollection.getItem(index);
		if( index != 0 ) {
			var pre_lbl = dis_tab.getColumnAt(index - 1);
			//trace( "--DISPLAY: Pre: " +  pre_lbl.getDisplay() + " Current: " + target.getDisplay());
			mgrid.aligner.rightOf( pre_lbl.getDisplay(), target.getDisplay());
		} 
		mgrid.aligner.rightOf(target.getDisplay(), line_mc);
		line_mc._x -= 1; 
	}
	//trace("DISPLAY TOTAL COL: " + dis_tab.getTotalColumn() );
	dis_tab.loopColumn();
}



MppDataGrid.prototype.createClips = function () {
	
	this.grid_mc = this.container_mc.createEmptyMovieClip("MppDataGrid" +  this.depth + "_mc" , this.detph);
	this.header_mc = this.grid_mc.createEmptyMovieClip("header_mc", 1);
	this.scrollpane_mc = this.grid_mc.createEmptyMovieClip("scrollpane_mc", 2);
	this.contentCanvas = new MppCanvas( this.grid_mc, 3);
	this.border_mc = this.grid_mc.createEmptyMovieClip("border_mc", 4);
	this.vgrid_mc = this.grid_mc.createEmptyMovieClip("vgrid_mc", 5);	
	this.grid_tooltip = new MppToolTip( this.grid_mc, 6);
	
}

//------PAINTER

MppDataGrid.prototype.paint = function () {
	this.paintHeader();
	this.paintScrollPane();
	this.paintContent();
	this.arrangeDisplay();
}


MppDataGrid.prototype.paintHeader = function () {
	this.header_mc.clear();
	drawBoxInPos( this.header_mc, 0 , 0 , this.grid_width, this.header_height, this.color_header_bg, 100);
	drawFrameInPos(this.header_mc, 0, 0, this.grid_width , this.header_height, this.color_border,100);
}

MppDataGrid.prototype.paintScrollPane = function () {
	this.scrollpane_mc.clear();
	this.border_mc.clear();
	drawBoxInPos (this.scrollpane_mc, 0, 0, this.scrollpane_width , this.grid_height - this.header_height, this.color_scroll_bg, 100);
	drawFrameInPos (this.border_mc, 0, 0, this.grid_width , this.grid_height + 1 , this.color_border , 100);
}

MppDataGrid.prototype.paintContent = function () {
	
	this.contentCanvas.setSize(this.grid_width - this.scrollpane_width, this.grid_height - this.header_height);
	this.contentCanvas.setBgAlpha(100);
	this.contentCanvas.setCanvasColor( this.color_content, this.color_border);
	
}

MppDataGrid.prototype.paintGlow = function () {
	this.roll_mc.clear();
	this.selected_mc.clear();
	drawBoxInPos(this.roll_mc, 0,0, this.grid_width - this.scrollpane_width, this.header_height, this.color_roll, 30);
	drawBoxInPos(this.selected_mc, 0,0, this.grid_width - this.scrollpane_width, this.header_height, this.color_selected, 30);
	this.hideHighLights();
}

MppDataGrid.prototype.arrangeDisplay = function () {
	this.contentCanvas.setDimension( this.header_mc._x, this.header_mc._y + this.header_height + 1);
	this.scrollpane_mc._y = this.header_height;
	this.aligner.bottomOf(this.header_mc, this.scrollpane_mc);
	this.aligner.rightOf( this.contentCanvas.getContainer(),  this.scrollpane_mc );
}

MppDataGrid.prototype.addEvents = function () {
	var mgrid = this;
	this.display_table.onRowLoop = function ( target, rowItems, rowNum ) {
		target.onLoop = function (target_lbl, index) {
			
			mgrid.addCellEvents(target_lbl);
		}
		target.loopIn();
	}
	this.display_table.loopRow();
	
}




MppDataGrid.prototype.addParentEvent = function () {
	var mgrid = this;
	this.grid_mc.useHandCursor = false;
	/*
	this.grid_mc.onMouseMove = function () {
		if( _xmouse < mgrid.grid_mc._x ||  _xmouse > ( mgrid.grid_mc._x + mgrid.grid_mc._width )) {
			mgrid.hideRoll();
		}  else if(_ymouse < mgrid.grid_mc._y  ||  _ymouse > (mgrid.grid_mc._y + mgrid.grid_mc._height)){
			mgrid.hideRoll();
		}
	}*/
}




MppDataGrid.prototype.addCellEvents = function (cell_lbl) {
	var mgrid = this;
	
	cell_lbl.onRollOver = function () {
		if(mgrid.interactive == true) {
			var new_mc = cell_lbl.getDisplay()
			mgrid.showRoll(new_mc._x  , new_mc._y);
			mgrid.grid_tooltip.setText( cell_lbl.getLabel());
			if(mgrid.bool_tooltip == true){
				mgrid.grid_tooltip.showTip();
			}
		}
	}
	cell_lbl.onRollOut = function () {
		if(mgrid.interactive == true) {
			mgrid.hideRoll();
			mgrid.grid_tooltip.hideTip();
		}
	}
	cell_lbl.onRelease = function () {
		
		if(mgrid.interactive == true) {
			mgrid.currentCell_lbl.setBgAlpha( mgrid.lbl_alpha_bg );
			var new_mc = cell_lbl.getDisplay()
			mgrid.showSelected(new_mc._x  , new_mc._y);
			cell_lbl.setBgAlpha(100);
			mgrid.currentCell_lbl = cell_lbl;
		}
	}
	cell_lbl.onPress = function () {
		if(mgrid.interactive == true) {
			mgrid.findData(cell_lbl);
			var eventObject = Object();
			eventObject.column = mgrid.current_column;
			eventObject.rowIndex = mgrid.selected_row;
			eventObject.columnIndex = mgrid.selected_column;
			eventObject.item = mgrid.selected_item;
			eventObject.row = mgrid.current_row;
			mgrid.broadcastMessage("onCellPress" , eventObject);
		}
	}	
}

MppDataGrid.prototype.findData = function (mpplbl){
	var rowCtr = 1;
	var colCtr = 0;
	for(rowCtr = 1; rowCtr <= this.display_table.getTotalRow(); rowCtr++){
		var broken = false;
		for(colCtr = 0; colCtr < this.display_table.getTotalColumn();colCtr++){
			var lbl = this.display_table.getItemAtColumn( colCtr, rowCtr);
			//trace("Current Search: " + mpplbl.getDisplay()  + " CURRENT ROW : " + rowCtr + " Col:" + colCtr + " data: " + lbl.getLabel() + " DISPLAY: " +  lbl.getDisplay() );
			if( mpplbl.getDisplay() == lbl.getDisplay() ) {
				this.selected_column = colCtr;
				this.selected_row = rowCtr;
				//trace("Selected Row: " + this.selected_row + " Selected Column: " + this.selected_column);
				broken = true;
				break;
			}
		}
		if( broken == true ){
			break;
		}
	}
	
	this.current_column = this.data_table.getColumnAt( this.selected_column)
	this.selected_item = this.data_table.getItem(this.current_column, this.selected_row);
	this.current_row = this.data_table.getRow( this.selected_row, "array");
	//trace("SELECTED COL: " + this.current_column + " SELECTED ITEM: " + this.selected_item  + " ROW: " + this.current_row );
}