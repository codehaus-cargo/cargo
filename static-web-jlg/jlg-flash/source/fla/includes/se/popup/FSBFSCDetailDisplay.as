#include "includes/components/MppComponents.as"
#include "includes/components/functions/vo/StringCleaner.as"
#include "includes/se/sys/SEVars.as"
#include "SEGSWindowCoreUIUtil.as"


//SERVICES

#include  "includes/se/service/SEFsbDisplayService.as"

var FsbFscEvents = new Object();
var FHANDLER;

function FSBFSCDetailDisplay ( container , depth ) {
	AsBroadcaster.initialize(this);
	this.addListener(this);
	this.parent = container;
	this.createChildren();
	this.configureListener();
	FHANDLER = this;
	this.loadData();
}



FSBFSCDetailDisplay.prototype.configureListener = function () {
	this.dBulNum_lbl.setWeight( false , false , true );
	this.dBulNum_lbl.setHandCursor( true );
	this.dBulNum_lbl.setFormat( "Arial" ,12 , 0x6A88FB );
	this.dBulNum_lbl.onRelease = this.clicked;
	this.dBulNum_lbl.onDragOut = this.bulUp;
	this.dBulNum_lbl.onRollOut = this.bulUp;
	this.dBulNum_lbl.onRollOver = this.bulOver;
	
	this.window.onCloseWindow = this.eventsHandler;
}

FSBFSCDetailDisplay.prototype.loadData = function () {
	this.renderUpperData();
	this.service = new FSBService();
	this.service.onLoadDetail = this.dataLoaded;
	this.service.loadDetailedInfo( "datatest/MachineInformation/response_MIFsbFscDisplay.txt");
}

FSBFSCDetailDisplay.prototype.dataLoaded = function ( _event ) {
	 FHANDLER.renderLowerData ( _event.data );
}

FSBFSCDetailDisplay.prototype.service;
FSBFSCDetailDisplay.prototype.bulletinLink;
FSBFSCDetailDisplay.prototype.renderLowerData = function (  _dataProvider ) {
	this.dFsbBy_lbl.setLabel(  _dataProvider.fsbCompletedBy);
	this.dFsbDate_lbl.setLabel( _dataProvider.fsbCompletedDate);
	this.dWarrantyBy_lbl.setLabel( _dataProvider.warrantyCompletedBy );
	this.dWarrantyDate_lbl.setLabel( _dataProvider.warrantyCompletedDate);
	this.dOrderBy_lbl.setLabel(_dataProvider.orderBy );
	this.dOrderDate_lbl.setLabel( _dataProvider.orderDate );
	this.bulletinLink =  _dataProvider.bulletinLink;
	this.dBulNum_lbl.setLabel( _dataProvider.bulletinNumber);
	this.dBulType_lbl.setLabel( _dataProvider.bulletinType );
	this.dLowerDesc_lbl.setLabel( _dataProvider. bulletinDescription );
	this.dShortDesc_lbl.setLabel( _dataProvider.shortDescription);
	this.dKitNum_lbl.setLabel( _dataProvider.kitNumber);
}

FSBFSCDetailDisplay.prototype.renderUpperData = function ( _dataProvider ) {
	for ( var prop in _level0.machineInfoLevel.FSBData) {
		trace(prop +  " = " +   _level0.machineInfoLevel.FSBData[prop] );
	}
	this.dSerialNum_lbl.setLabel( _level0.machineInfoLevel.FSBData.serialNumber);
	this.dModelNum_lbl.setLabel( _level0.machineInfoLevel.FSBData.modelNumber);
	this.dCurOwner_lbl.setLabel( _level0.machineInfoLevel.FSBData.owner);
	this.dEngineSNum_lbl.setLabel( _level0.machineInfoLevel.FSBData.engineNumber);
	this.dUpperDesc_lbl.setLabel( _level0.machineInfoLevel.FSBData.description);
	this.dPartNum_lbl.setLabel( _level0.machineInfoLevel.FSBData.partNumber);
	
}

FSBFSCDetailDisplay.prototype.eventsHandler = function  ( _event ) {
	trace("type: " + _event.type );
}

//DISPLAY -----------------------------------------------
//DISPLAY -----------------------------------------------
//DISPLAY -----------------------------------------------
FSBFSCDetailDisplay.prototype.parent;

FSBFSCDetailDisplay.prototype.window;
FSBFSCDetailDisplay.prototype.aligner;

FSBFSCDetailDisplay.prototype.mainCanvas;
FSBFSCDetailDisplay.prototype.upperCanvas;
FSBFSCDetailDisplay.prototype.lowerCanvas;
FSBFSCDetailDisplay.prototype.barCanvas;

FSBFSCDetailDisplay.prototype.upperLine1;
FSBFSCDetailDisplay.prototype.upperLine2;

FSBFSCDetailDisplay.prototype.lowerLine1;
FSBFSCDetailDisplay.prototype.lowerLine2;
FSBFSCDetailDisplay.prototype.lowerLine3;
FSBFSCDetailDisplay.prototype.lowerLine4;
FSBFSCDetailDisplay.prototype.lowerLine5;

FSBFSCDetailDisplay.prototype.sSerialNum_lbl;
FSBFSCDetailDisplay.prototype.dSerialNum_lbl;

FSBFSCDetailDisplay.prototype.sModelNum_lbl;
FSBFSCDetailDisplay.prototype.dModelNum_lbl;
FSBFSCDetailDisplay.prototype.sCurOwner_lbl;
FSBFSCDetailDisplay.prototype.dCurOwner_lbl;


 
FSBFSCDetailDisplay.prototype.sEngineSNum_lbl;
FSBFSCDetailDisplay.prototype.dEngineSNum_lbl;
FSBFSCDetailDisplay.prototype.sUpperDesc_lbl;
FSBFSCDetailDisplay.prototype.dUpperDesc_lbl;
FSBFSCDetailDisplay.prototype.sPartNum_lbl;
FSBFSCDetailDisplay.prototype.dPartNum_lbl;

FSBFSCDetailDisplay.prototype.createChildren = function () {
		this.aligner = new MppAligner();
	 	 this.window = new MppWindow( this.parent, this.depth );
		 
		 this.window.setSize(620,250);
		 this.window.setDimension(180, 100);
		 this.window.maximizeOn(false);
		 this.window.minimizeOn(false);
		 this.window.setTitle("Field Service Bulletin / Field Safety Campaign");
		 this.window.setWindowColor(0xD6D3CE, 0xFECB81);
		 this.window.setTitlebarColor(0xFF9A00, 0xBA7001, 0xFECB81);
		 this.window.openWindow();
		 
		 this.mainCanvas = new MppCanvas ( this.window.getContentContainer(), 1 )
		 this.mainCanvas.setCanvasColor(0x000000, 0xFF00FF);
	     
		 this.mainCanvas.setSize( 620 , 400);
		 ///UPPER 
		 //LINE 1
		this.upperCanvas = new MppCanvas( this.mainCanvas.getContainer(), 1 );
		this.upperCanvas.setSize( 620, 60);
		this.upperCanvas.setDimension(20 , 20);
		
		this.lowerCanvas = new MppCanvas ( this.mainCanvas.getContainer(), 2 );
		this.lowerCanvas.setSize( 620, 300);
		this.lowerCanvas.setDimension(  20, 90);
		
		this.barCanvas = new MppCanvas ( this.mainCanvas.getContainer(),3 );
		this.barCanvas.setSize( 620, 10);
		this.barCanvas.setDimension( 20, 75);
		this.barCanvas.getContainer().attachMovie( "horizontalBar", "bar", 1);
		
		
		this.upperLine1 = new MppCanvas( this.upperCanvas.getContainer(), 1 );
		this.upperLine1.setSize(620, 25);
		this.upperLine2 = new MppCanvas( this.upperCanvas.getContainer(), 2 )
		this.upperLine2.setSize(620, 25);
		this.upperLine2.setDimension(0,25);
		
		
		this.lowerLine1 = new MppCanvas( this.lowerCanvas.getContainer(), 1 );
		this.lowerLine1.setSize(620, 25);
		
		this.lowerLine2 = new MppCanvas( this.lowerCanvas.getContainer(), 2 );
		this.lowerLine2.setSize(620, 25);
		this.lowerLine2.setDimension( 0, 25);
		
		this.lowerLine3 = new MppCanvas( this.lowerCanvas.getContainer(), 3 );
		this.lowerLine3.setSize(620, 25);
		this.lowerLine3.setDimension( 0, 50);
		
		this.lowerLine4 = new MppCanvas( this.lowerCanvas.getContainer(), 4 );
		this.lowerLine4.setSize(620, 25);
		this.lowerLine4.setDimension( 0, 75);
		
		this.lowerLine5 = new MppCanvas( this.lowerCanvas.getContainer(), 5  );
		this.lowerLine5.setSize(620, 25);
		this.lowerLine5.setDimension( 0, 100);
		
		this.createLine1();
		this.createLine2();
		this.createLowerLine1();
		this.createLowerLine2();
		this.createLowerLine3();
		this.createLowerLine4();
		this.createLowerLine5();
		 
}

FSBFSCDetailDisplay.prototype.createLine1 = function () {
		this.sSerialNum_lbl = new MppTextLabel( this.upperLine1.getContainer() , 1);
		this.sSerialNum_lbl.setSize( 100, 20) ;
		this.sSerialNum_lbl.setLabel( "Serial Number:");
		this.configureStaticLabels( this.sSerialNum_lbl );
		
		this.dSerialNum_lbl = new MppTextLabel( this.upperLine1.getContainer() , 2);
		this.dSerialNum_lbl.setSize( 100 , 20) ;
		this.dSerialNum_lbl.setDimension( 100, 0);
		this.dSerialNum_lbl.setLabel( "Lorem Ipsum");
		
		this.sModelNum_lbl = new MppTextLabel( this.upperLine1.getContainer() , 3);
		this.sModelNum_lbl.setSize( 100 , 20) ;
		this.sModelNum_lbl.setDimension( 200, 0);
		this.sModelNum_lbl.setLabel( "Model Number: ");
		this.configureStaticLabels( this.sModelNum_lbl );
		
		this.dModelNum_lbl = new MppTextLabel( this.upperLine1.getContainer() , 4);
		this.dModelNum_lbl.setSize( 100 , 20) ;
		this.dModelNum_lbl.setDimension( 300, 0);
		this.dModelNum_lbl.setLabel( "Lorem Ipsum: ");
		
		this.sCurOwner_lbl = new MppTextLabel( this.upperLine1.getContainer() , 5);
		this.sCurOwner_lbl.setSize( 100 , 20) ;
		this.sCurOwner_lbl.setDimension( 400, 0);
		this.sCurOwner_lbl.setLabel( "Current Owner: ");
		this.configureStaticLabels( this.sCurOwner_lbl );
		
		this.dCurOwner_lbl = new MppTextLabel( this.upperLine1.getContainer() , 6);
		this.dCurOwner_lbl.setSize( 100 , 20) ;
		this.dCurOwner_lbl.setDimension( 500, 0);
		this.dCurOwner_lbl.setLabel( "Lorem Ipsum");
}

FSBFSCDetailDisplay.prototype.createLine2 = function () {
		this.sEngineSNum_lbl = new MppTextLabel( this.upperLine2.getContainer() , 1);
		this.sEngineSNum_lbl.setSize( 100, 20) ;
		this.sEngineSNum_lbl.setLabel( "Engine Serial #:");
		this.configureStaticLabels( this.sEngineSNum_lbl );
		
		this.dEngineSNum_lbl = new MppTextLabel( this.upperLine2.getContainer() , 2);
		this.dEngineSNum_lbl.setSize( 100, 20) ;
		this.dEngineSNum_lbl.setDimension( 100, 0);
		this.dEngineSNum_lbl.setLabel( "LOrem Ipsum");
		
		
		this.sUpperDesc_lbl = new MppTextLabel( this.upperLine2.getContainer() , 3);
		this.sUpperDesc_lbl.setSize( 100, 20) ;
		this.sUpperDesc_lbl.setDimension( 200,0);
		this.sUpperDesc_lbl.setLabel( "Description:");
		this.configureStaticLabels( this.sUpperDesc_lbl );
		
		this.dUpperDesc_lbl = new MppTextLabel( this.upperLine2.getContainer() , 4);
		this.dUpperDesc_lbl.setSize( 100, 20) ;
		this.dUpperDesc_lbl.setDimension( 300, 0);
		this.dUpperDesc_lbl.setLabel( "Lorem Ipsum");
		
		
		this.sPartNum_lbl = new MppTextLabel( this.upperLine2.getContainer() , 5);
		this.sPartNum_lbl.setSize( 100, 20) ;
		this.sPartNum_lbl.setDimension( 400,0);
		this.sPartNum_lbl.setLabel( "Part Number:");
		this.configureStaticLabels( this.sPartNum_lbl );
		
		this.dPartNum_lbl = new MppTextLabel( this.upperLine2.getContainer() , 6);
		this.dPartNum_lbl.setSize( 100, 20) ;
		this.dPartNum_lbl.setDimension( 500, 0);
		this.dPartNum_lbl.setLabel( "Lorem Ipsum");
}

FSBFSCDetailDisplay.prototype.sBulNum_lbl;
FSBFSCDetailDisplay.prototype.dBulNum_lbl;
FSBFSCDetailDisplay.prototype.sBulType_lbl;
FSBFSCDetailDisplay.prototype.dBulType_lbl;

FSBFSCDetailDisplay.prototype.createLowerLine1 = function () {
	 
		this.sBulNum_lbl = new MppTextLabel( this.lowerLine1.getContainer() , 1);
		this.sBulNum_lbl.setSize( 100, 20) ;
		this.sBulNum_lbl.setLabel( "Bulletin Number:");
		this.configureStaticLabels( this.sBulNum_lbl );
		
		this.dBulNum_lbl = new MppTextLabel( this.lowerLine1.getContainer() , 2);
		this.dBulNum_lbl.setSize( 100, 20) ;
		this.dBulNum_lbl.setDimension( 100, 0);
		this.dBulNum_lbl.setLabel( "LOrem Ipsum");
		
		this.sBulType_lbl = new MppTextLabel( this.lowerLine1.getContainer() , 3);
		this.sBulType_lbl.setSize( 100, 20) ;
		this.sBulType_lbl.setDimension( 200, 0);
		this.sBulType_lbl.setLabel( "Bulletin Type:");
		this.configureStaticLabels( this.sBulType_lbl );
		
		this.dBulType_lbl = new MppTextLabel( this.lowerLine1.getContainer() , 4);
		this.dBulType_lbl.setSize( 100, 20) ;
		this.dBulType_lbl.setDimension( 300, 0);
		this.dBulType_lbl.setLabel( "Lorem Ipsum");
}

FSBFSCDetailDisplay.prototype.sLowerDesc_lbl
FSBFSCDetailDisplay.prototype.dLowerDesc_lbl
FSBFSCDetailDisplay.prototype.sShortDesc_lbl
FSBFSCDetailDisplay.prototype.dShortDesc_lbl
FSBFSCDetailDisplay.prototype.sKitNum_lbl
FSBFSCDetailDisplay.prototype.sKitNum_lbl

FSBFSCDetailDisplay.prototype.createLowerLine2 = function () {
	 
		this.sLowerDesc_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 1);
		this.sLowerDesc_lbl.setSize( 100, 20) ;
		this.sLowerDesc_lbl.setLabel( "Description:");
		this.configureStaticLabels( this.sLowerDesc_lbl);
		
		this.dLowerDesc_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 2);
		this.dLowerDesc_lbl.setSize( 100, 20) ;
		this.dLowerDesc_lbl.setDimension( 100, 0);
		this.dLowerDesc_lbl.setLabel( "Lorem Ipsum");
		
		this.sShortDesc_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 3);
		this.sShortDesc_lbl.setSize( 110, 20) ;
		this.sShortDesc_lbl.setDimension( 200, 0);
		this.sShortDesc_lbl.setLabel( "Short Description:");
		this.configureStaticLabels( this.sShortDesc_lbl);
		
		this.dShortDesc_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 4);
		this.dShortDesc_lbl.setSize( 90, 20) ;
		this.dShortDesc_lbl.setDimension( 310, 0);
		this.dShortDesc_lbl.setLabel( "Lorem Ipsum");
		
		 
		this.sKitNum_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 5);
		this.sKitNum_lbl.setSize( 100, 20) ;
		this.sKitNum_lbl.setDimension( 400, 0);
		this.sKitNum_lbl.setLabel( "Kit Number:");
		this.configureStaticLabels( this.sKitNum_lbl);
		
		this.dKitNum_lbl = new MppTextLabel( this.lowerLine2.getContainer() , 6);
		this.dKitNum_lbl.setSize( 100, 20) ;
		this.dKitNum_lbl.setDimension( 500, 0);
		this.dKitNum_lbl.setLabel( "Lorem Ipsum");
		
}

 
FSBFSCDetailDisplay.prototype.sOrderDate_lbl;
FSBFSCDetailDisplay.prototype.dOrderDate_lbl;
FSBFSCDetailDisplay.prototype.sOrderBy_lbl;
FSBFSCDetailDisplay.prototype.dOrderBy_lbl;


FSBFSCDetailDisplay.prototype.createLowerLine3 = function () {
	
	    this.sOrderBy_lbl = new MppTextLabel( this.lowerLine3.getContainer() , 1);
		this.sOrderBy_lbl.setSize( 150, 20) ;
		this.sOrderBy_lbl.setLabel(  "Order by Customer:");
		this.configureStaticLabels( this.sOrderBy_lbl);
		
		this.dOrderBy_lbl = new MppTextLabel( this.lowerLine3.getContainer() , 2);
		this.dOrderBy_lbl.setSize( 100, 20) ;
		this.dOrderBy_lbl.setDimension( 150, 0);
		this.dOrderBy_lbl.setLabel( "Lorem Ipsum");
	    
		this.sOrderDate_lbl = new MppTextLabel( this.lowerLine3.getContainer() , 3);
		this.sOrderDate_lbl.setSize( 150, 20) ;
		this.sOrderDate_lbl.setDimension( 250, 0);
		this.sOrderDate_lbl.setLabel("Order Date:");
		this.configureStaticLabels( this.sOrderDate_lbl);
		
		this.dOrderDate_lbl = new MppTextLabel( this.lowerLine3.getContainer() , 4);
		this.dOrderDate_lbl.setSize( 100, 20) ;
		this.dOrderDate_lbl.setDimension( 350, 0);
		this.dOrderDate_lbl.setLabel( "Lorem Ipsum");
		
		
		
		
}




FSBFSCDetailDisplay.prototype.sWarrantyBy_lbl;
FSBFSCDetailDisplay.prototype.dWarrantyBy_lbl;
FSBFSCDetailDisplay.prototype.sWarrantyDate_lbl;
FSBFSCDetailDisplay.prototype.dWarrantyDate_lbl;

FSBFSCDetailDisplay.prototype.createLowerLine4 = function () {
		this.sWarrantyBy_lbl = new MppTextLabel( this.lowerLine4.getContainer() , 1);
		this.sWarrantyBy_lbl.setSize( 150, 20) ;
		this.sWarrantyBy_lbl.setLabel(  "Warranty Completed by:");
		this.configureStaticLabels( this.sWarrantyBy_lbl);
		
		this.dWarrantyBy_lbl = new MppTextLabel( this.lowerLine4.getContainer() , 2);
		this.dWarrantyBy_lbl.setSize( 100, 20) ;
		this.dWarrantyBy_lbl.setDimension( 150, 0);
		this.dWarrantyBy_lbl.setLabel( "Lorem Ipsum");
	    
		this.sWarrantyDate_lbl = new MppTextLabel( this.lowerLine4.getContainer() , 3);
		this.sWarrantyDate_lbl.setSize( 150, 20) ;
		this.sWarrantyDate_lbl.setDimension( 250, 0);
		this.sWarrantyDate_lbl.setLabel("Completed Date:");
		this.configureStaticLabels( this.sWarrantyDate_lbl);
		
		this.dWarrantyDate_lbl = new MppTextLabel( this.lowerLine4.getContainer() , 4);
		this.dWarrantyDate_lbl.setSize( 100, 20) ;
		this.dWarrantyDate_lbl.setDimension( 350, 0);
		this.dWarrantyDate_lbl.setLabel( "Lorem Ipsum");
}


FSBFSCDetailDisplay.prototype.sFsbBy_lbl;
FSBFSCDetailDisplay.prototype.dFsbBy_lbl;
FSBFSCDetailDisplay.prototype.sFsbDate_lbl;
FSBFSCDetailDisplay.prototype.dFsbDate_lbl;

FSBFSCDetailDisplay.prototype.createLowerLine5 = function () {
		this.sFsbBy_lbl = new MppTextLabel( this.lowerLine5.getContainer() , 1);
		this.sFsbBy_lbl.setSize( 150, 20) ;
		this.sFsbBy_lbl.setLabel(  "FSB Completed by:");
		this.configureStaticLabels( this.sFsbBy_lbl);
		
		this.dFsbBy_lbl = new MppTextLabel( this.lowerLine5.getContainer() , 2);
		this.dFsbBy_lbl.setSize( 100, 20) ;
		this.dFsbBy_lbl.setDimension( 150, 0);
		this.dFsbBy_lbl.setLabel( "Lorem Ipsum");
	    
		this.sFsbDate_lbl = new MppTextLabel( this.lowerLine5.getContainer() , 3);
		this.sFsbDate_lbl.setSize( 150, 20) ;
		this.sFsbDate_lbl.setDimension( 250, 0);
		this.sFsbDate_lbl.setLabel("Completed Date:");
		this.configureStaticLabels( this.sFsbDate_lbl);
		
		this.dFsbDate_lbl = new MppTextLabel( this.lowerLine5.getContainer() , 4);
		this.dFsbDate_lbl.setSize( 100, 20) ;
		this.dFsbDate_lbl.setDimension( 350, 0);
		this.dFsbDate_lbl.setLabel( "Lorem Ipsum");
}



FSBFSCDetailDisplay.prototype.configureStaticLabels = function ( target_lbl ) {
	target_lbl.setWeight ( true , false , false);
}

FSBFSCDetailDisplay.prototype.clicked = function ( _event )  {
	FHANDLER.dBulNum_lbl.setFormat( "Arial" ,12 , 0x6A88FB );
	
	//getURL( FHANDLER.bulletinLink ,"_self");
	var params = new Object();
	params.type = "bulletinNumberClick";
	params.data = new Object();
	params.data.bulletinLink = FHANDLER.bulletinLink;
	FHANDLER.broadcastMessage("bulletinNumberClick" , params.data  );
}

FSBFSCDetailDisplay.prototype.bulOver = function ( _event ) {
	FHANDLER.dBulNum_lbl.setFormat( "Arial" ,12 , 0x6A88FB );
}
 
FSBFSCDetailDisplay.prototype.bulUp = function ( _event ) {
	FHANDLER.dBulNum_lbl.setFormat ( "Arial" ,12 , 0x052BBE );
}


var fsbDisplay;  
function main   ( parent, depth ) {
	 	 fsbDisplay = new FSBFSCDetailDisplay(  parent, depth);
		 fsbDisplay.bulletinNumberClick = listener; 
}