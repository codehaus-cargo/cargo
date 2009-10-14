//INCLUDE COMPONENTS

#include "includes/components/MppComponents.as"
#include "includes/components/functions/vo/StringCleaner.as"
#include "includes/se/sys/SEVars.as"
#include "SEGSWindowCoreUIUtil.as" 

//INCLUDE VIEWS
#include "SESearchView.as"
#include "SEMachineInfoView.as"
#include "SEStructuredInquiryView.as"
//INCLUDE SERVICES
#include "includes/se/SESpecificServices.as"



function GlobalSearchWindow ( container , depth) {
	 
	 		this.window = new MppWindow( container , depth);
			this.aligner = new MppAligner();
			this.cleaner = new StringCleaner();
			this.grid_spliter = "&jopiruizen&"; //important in data processing
			this.maxPerPage = 10;
			this.pageCtr = 0;
			/* old size and position
			this.window.setSize(600,450);
			this.window.setDimension(180, 80);
			*/
			this.window.setSize( 600, 475 );
			this.window.setDimension( 180, 80 );
			
			this.window.maximizeOn(false);
			this.window.minimizeOn(false);
			this.window.setTitle("");
			this.window.setWindowColor(0xD6D3CE, 0xFECB81);
			this.window.setTitlebarColor(0xFF9A00, 0xBA7001, 0xFECB81);
			this.view_tab = new MppTab( this.window.getContentContainer(), 1);
			

			this.view_tab.addTab("Search", 0);
			this.view_tab.addTab("Machine Information", 1);
			this.view_tab.addTab("Structured Inquiry", 2 );
			this.view_tab.setTabButtonSize(1, 140, 20);
			this.view_tab.setTabButtonSize(2, 140, 20);
			
			this.view_tab.setLabelColor(0x000000, 0xFED192, 0x000000, 0xFF9A00);

			this.view_tab.setDimension(10,10);
			/* old size and position
			this.view_tab.setSize(580, 390);
			*/
			this.view_tab.setSize( 580, 415 );
			
			this.view_tab.setTabColor(0xECEAE8, 0xFFFFFF, 0xECEAE8, 0xFF9900);
			SEGSWindowCoreUIUtil.tabButtonConfig( this.view_tab ); 
			 
			this.searchCanvas = this.view_tab.getCanvas(0);
			this.machineInfoCanvas =  this.view_tab.getCanvas(1);
			this.structInCanvas =  this.view_tab.getCanvas(2);
			
			this.hasSearch = false;
			this.hasMachineInfo = false;
			this.hasStructuredQuery = false;
			
			this.view_tab.change = this.tabsChange;
			SEGSWindowCoreUIUtil.windowHandler = this;
}

GlobalSearchWindow.prototype.window;
GlobalSearchWindow.prototype.aligner;
GlobalSearchWindow.prototype.cleaner;
GlobalSearchWindow.prototype.grid_spliter;
GlobalSearchWindow.prototype.view_tab;

GlobalSearchWindow.prototype.searchCanvas;
GlobalSearchWindow.prototype.machineInfoCanvas;
GlobalSearchWindow.prototype.structInCanvas;

GlobalSearchWindow.prototype.hasSearch;
GlobalSearchWindow.prototype.hasMachineInfo;
GlobalSearchWindow.prototype.hasStructuredQuery;

GlobalSearchWindow.prototype.searchView;
GlobalSearchWindow.prototype.machineIView;
GlobalSearchWindow.prototype.structureQView;


GlobalSearchWindow.prototype.tabsChange = function  ( _event ) {
	//trace("GlobalSearchWindow.prototype.loadSearchResult()   _event.text="+_event.text)
	switch (_event.text ) {
		case "Search":
			SEGSWindowCoreUIUtil.windowHandler.showSearch();
		break;
		case "Machine Information":
			 
			SEGSWindowCoreUIUtil.windowHandler.showMachineInfo();
		break;
		
		case "Structured Inquiry":
			SEGSWindowCoreUIUtil.windowHandler.showSQuery();
		break;
	}
}

GlobalSearchWindow.prototype.eventsHandler = function ( _event ) {
	//trace("GlobalSearchWindow.prototype.eventsHandler()   _event.type="+_event.type)
	switch ( _event.type ) {
		case GlobalSearchCoreEvent.SEARCH_VIEW_CANCEL:
			SEGSWindowCoreUIUtil.windowHandler.closeWindow();
		break;
		case GlobalSearchCoreEvent.SEARCH_VIEW_DATA_LOADED:
			 SEGSWindowCoreUIUtil.windowHandler.loadSearchResult ( _event.resultXML );
		break;
		
		case GlobalSearchCoreEvent.CLOSE_WINDOW : 
			SEGSWindowCoreUIUtil.windowHandler.closeWindow();
		break;
		
		case MachineInfoViewEvent.FSB_GRID_PRESS:
			 
			// if( _level0.fsbfscDisplayLevel != undefined ) {
				// _level0.fsbfscDisplayLevel.fsbDisplay.window.openWindow();
				// _level0.fsbfscDisplayLevel.fsbDisplay.loadData();
			// } else { 
				_level0.machineInfoLevel.FSBData = new Object();
				_level0.machineInfoLevel.FSBData.bulletinNumber = _event.bulletinNumber;
				 
				loadMovieNum( _level0.staticWebURL+"FSBDetailedDisplayWindow.swf" ,1129 );
			 	//loadMovieNum ( "FSBFSCDetailDisplay.swf" ,1128); 
			// }
		break;
		
		case "bulletinNumberClick":
		
		break;
	}
}

GlobalSearchWindow.prototype.loadSearchResult   = function ( resultXML ) {
		trace("GlobalSearchWindow.prototype.loadSearchResult()")
	    this.closeWindow();
	  	_level0.checkForError(  resultXML );
		if (   resultXML.firstChild.firstChild.attributes.error.length > 1 ) {
			_level300.popup (  resultXML.firstChild.firstChild.attributes.error, "Search Warning")
			_level0.showErrorMessage(resultXML.firstChild.firstChild.attributes.error);
		}  else {
			_level0.searchResponseXML = resultXML ;
			_level0.WebSearchQuery.flag = false;
		 	 //loadMovieNum("searchResults2.swf",292)
		 	 _level0.searchResultLoader = 291;
		 	 
		 	 _level0.loadGlobalSearchSearchResults();
		 	 
	    }
}

GlobalSearchWindow.prototype.closeWindow  = function () {
	this.window.closeWindow();
}



GlobalSearchWindow.prototype.showSearch = function () {
	if( this.hasSearch == true ){
		
	} else {
		_level300.mouseWait();
		this.hasSearch = true;
		this.searchView = new SESearchView( this.searchCanvas );
		this.searchView.onCancelSearchView = this.eventsHandler;
		this.searchView.onSearchViewDataLoaded = this.eventsHandler;
		
		/* Date: Aug 24, 2007
		 * Websearch criteria restoration support 
		 */
		if ( _level0.WebSearchQuery.flag  == true ) {
			this.searchView.restoreWebSearch();
		}
		_levell300.mouseNormal();
	}
	
	if ( _level0.WebSearchQuery.flag  == true ) {
		this.searchView.restoreWebSearch();
	} else if( _level0.refineFlag == true ) {
		_level0.refineFlag = false;
		
		this.searchView.restoreOldSearch();
	} else {
		this.searchView.clearTextField();
	}
	_level300.mouseNormal();
}




GlobalSearchWindow.prototype.showMachineInfo = function () {
	if( this.hasMachineInfo == true ){
		//JUST SHOW THE CANVAS ELSE CREATE IT
	} else {
		_level300.mouseWait();
		this.hasMachineInfo = true;
		this.machineIView = new SEMachineInfoView( this.machineInfoCanvas );
		this.machineIView.onClickFsbGrid = this.eventsHandler;
		this.machineIView.onCloseWindow = this.eventsHandler;
		_level300.mouseNormal();
	}
}

GlobalSearchWindow.prototype.showSQuery = function (){
	if( this.hasStructuredQuery == true ) {
	
	} else {
		_level300.mouseWait();
		this.hasStructuredQuery = true;
		this.structureQView = new SESQueryView( this.structInCanvas );
		this.structureQView.onCloseWindow = this.eventsHandler;
		_level300.mouseNormal();
	}
}

GlobalSearchWindow.prototype.showWindow = function () {
	this.window.openWindow();
	this.selectTab();
}

GlobalSearchWindow.prototype.selectTab = function () {
	if( _level0.globalSearchTab == 0 ) { //global search 
		this.view_tab.selectTab(0);
		this.showSearch();
	} else if( _level0.globalSearchTab == 1 ) {//machine information
		this.view_tab.selectTab(1);
		this.showMachineInfo();
	} else if( _level0.globalSearchTab == 2 ) { // Structured query
		this.view_tab.selectTab(2);
		this.showSQuery();
	}
}


var  globalSearch; 
function main( container , depth) {
	globalSearch = new GlobalSearchWindow( container , depth );
	if( _level0.globalSearchTab == undefined ) {
		_level0.globalSearchTab = 0;
	}
	globalSearch.showWindow();
	_level300.mouseNormal();
}




/*
Sleep well weep well go to the deep well a often as possible bring back 
the water, jostling and gleaming God did not plan on consciouness developing so well
Well you can tell him our pail is full and we can go to hell
-Anne Rice -
*/

 //_level300.mouseWait();
 //_level300.mouseNormal();