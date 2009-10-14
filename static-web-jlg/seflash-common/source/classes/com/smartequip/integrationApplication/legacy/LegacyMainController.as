/**
 *	The LegacyMainController class provides api to and maintaines all legacy controllers and bubbles up their events to the Initializer class.
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
//import com.smartequip.integrationApplication.legacy.citrine.controller.*;
//import com.smartequip.integrationApplication.legacy.citrine.event.*;
import com.smartequip.integrationApplication.event.*;
import com.smartequip.integrationApplication.legacy.*;


class com.smartequip.integrationApplication.legacy.LegacyMainController extends EventDispatcher{
	
	private var checkLoadLegacyComponentIntervalID;
	
	private var configurationController:LegacyConfigurationController;
	private var securityController:LegacySecurityController;
	private var treeController:LegacyTreeController;
	private var tabsController:LegacyTabsController;
	private var webSearchController:LegacyWebSearchController;
	private var introController:LegacyIntroController;
	private var loadingController:LegacyLoadingController;
	
	/**
	 * Constructor
	 */
	public function LegacyMainController(){
		initDefaultGlobalVariables();
		initLegacyControllers();
		//initSecurity();
	}
	/**
	 * Method for initializing all legacy controllers and their event listeners.
	 */
	private function initLegacyControllers(){
		trace("LegacyMainController.initLegacyControllers()")
		
		configurationController = new LegacyConfigurationController();
		configurationController.addEventListener(ConfigurationCompleteEvent.COMPLETE, Delegate.create(this, bubbleUpEvent))
		
		securityController = new LegacySecurityController();
		securityController.addEventListener(AuthenticateCompleteEvent.COMPLETE, Delegate.create(this,bubbleUpEvent))
		securityController.addEventListener(AuthorizeCompleteEvent.COMPLETE, Delegate.create(this,bubbleUpEvent))
		
		treeController = new LegacyTreeController();
		treeController.addEventListener(LegacyTabsEvent.LOAD, Delegate.create(this, bubbleUpEvent))
		treeController.addEventListener(LegacyTabsEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this, bubbleUpEvent))
		
		tabsController = new LegacyTabsController();
		tabsController.addEventListener(LegacyTreeEvent.MODIFY, Delegate.create(this, bubbleUpEvent))
		
		
		webSearchController = new LegacyWebSearchController();
		webSearchController.addEventListener(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD, Delegate.create(this, bubbleUpEvent))
		webSearchController.addEventListener(LegacyWebSearchEvent.GLOBAL_SEARCH_LOAD, Delegate.create(this, bubbleUpEvent))
		
		introController = new LegacyIntroController();
		
		loadingController = new LegacyLoadingController();
		loadingController.addEventListener(LegacyLoadingOnEvent.ON, Delegate.create(this, bubbleUpEvent))
		loadingController.addEventListener(LegacyLoadingOffEvent.OFF, Delegate.create(this, bubbleUpEvent))
		loadingController.addEventListener(ErrorShowEvent.SHOW, Delegate.create(this, bubbleUpEvent))
		loadingController.addEventListener(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD, Delegate.create(this, bubbleUpEvent))
		loadingController.addEventListener(LegacyTreeEvent.ADD_TO_SELECTION, Delegate.create(this, bubbleUpEvent))
		//loadingController.addEventListener(LegacyWebSearchEvent.SEARCH_RESULTS_LOAD, Delegate.create(this, bubbleUpEvent))
		//loadingController.preparePositionAndScale();
		//if(legacyComponentType == LegacyComponentData.TABS) loadingController.init();
		loadingController.init();
	}
	/**
	 * Method for initializing default global variables.
	 */
	private function initDefaultGlobalVariables(){
		trace("LegacyMainController.initDefaultGlobalVariables()")
		
		if(_level0.staticWebURL==undefined) _level0.staticWebURL = ""
		
		loadMovieNum (_level0.staticWebURL + "empty.swf", 100);
		loadMovieNum (_level0.staticWebURL + "empty.swf", 110);
		loadMovieNum (_level0.staticWebURL + "empty.swf", 213);
		loadMovieNum (_level0.staticWebURL + "empty.swf", 209);
		loadMovieNum (_level0.staticWebURL + "empty.swf", 210);
		
		_level0.User_ID = "";	
		_level0.secLevel = "";
		
		_level0.themeBGColor = 1;
		_level0.thisScreen = "";
		_level0.treeUsedBy = "partsdiagram";
		_level0.searchPattern = "";
		
		if ( _level0.partsList == null ) _level0.partsList = new Array();
		if ( _level0.pdispList == null ) _level0.pdispList= new Array();
		if ( _level0.pmfrID == null ) _level0.pmfrID = new Array();
		if ( _level0.pdsrc == null ) _level0.pdsrc = new Array();
		if ( _level0.ppID == null ) _level0.ppID = new Array();
		if ( _level0.pDesc == null ) _level0.pDesc = new Array();
		if ( _level0.pQty == null ) _level0.pQty = new Array();
		if ( _level0.pMC == null ) _level0.pMC = new Array();
		if ( _level0.pgDesc == null ) _level0.pgDesc = new Array();
		if ( _level0.pComponent == null ) _level0.pComponent = new Array();
		if ( _level0.pVendNo == null ) _level0.pVendNo = new Array();
	}
	/**
	 * Method for delegating security initialization to the security controller.
	 */
	public function initSecurity(){
		securityController.init();
	}
	/**
	 * Method for delegating configuration initialization to the configuration controller.
	 */
	public function initConfiguration(){
		configurationController.init();
	}
	/**
	 * Method for delegating authentication to the security controller.
	 */
	public function authenticate(){
		securityController.authenticate();
	}
	/**
	 * Method for delegating authorizion to the security controller.
	 */
	public function authorize(){
		securityController.authorize();
	}
	/**
	 * Method for loading
	 */
	public function loadLegacyComponent(){
		trace("LegacyMainController.loadLegacyComponent()     legacyComponentType="+legacyComponentType)
		
		//preparePositionAndScale();
		
		switch(legacyComponentType){
			case LegacyComponentData.TREE:
				treeController.preparePositionAndScale();
				treeController.init();
				break;
			case LegacyComponentData.TABS:
				//tabsController.init(curMfrName, curSpecLevel, curSchematic, partsdiagramAppear, curComponent);
				introController.preparePositionAndScale();
				introController.init();
				//tabsController.init();
				break;
			case LegacyComponentData.WEB_SEARCH:
				webSearchController.preparePositionAndScale();
				webSearchController.init("")
				checkLoadLegacyComponentIntervalID = setInterval(this, "checkLegacyComponentLoad", 200, legacyComponentType);
				break;
			default:
				trace("error. wrong legacyComponentType value")
		}
		
		loadingController.legacyView.mouseWait = Delegate.create(loadingController, loadingController.delegateMouseWait)
		loadingController.legacyView.mouseNormal = Delegate.create(loadingController, loadingController.delegateMouseNormal)
		//loadingController.stop();
		//checkLoadLegacyComponentIntervalID = setInterval(this, "checkLegacyComponentLoad", 200, _legacyManager.legacyComponentType);
	}
	
	public function loadLegacyTabs(event:LegacyTabsEvent){
		tabsController.preparePositionAndScale();
		tabsController.init(event.curMCFlag, 
							event.curMfrName, 
							event.curSpecLevel, 
							event.curSchematic, 
							event.partsdiagramAppear, 
							event.curComponent,
							//event.partsListArray,
							event.partsListURL,
							event.partsListRequestXML,
							event.curModel,
							event.curModelNumbers,
							event.curComponentID,
							event.useMasterDesc,
							//event.topPartsListArray,
							event.goToPartsListTab,
							event.customTabs,
							event.curSerialNum,
							event.curDataSourceID,
							event.colorCH,
							event.sysURL,
							event.curModelDisplay
							);
							
		//checkLoadLegacyComponentIntervalID = setInterval(this, "checkLegacyComponentLoad", 200, legacyComponentType);
		//tabsController.init(event.curMfrName, event.curSpecLevel, event.curSchematic, event.partsdiagramAppear, event.curComponent);
	}
	
		
	public function modifyLegacyTree(event:LegacyTreeEvent)  { 
		treeController.modifyLegacyTree(event.mfrgrp, 
										event.mfr, 
										event.prodGroupID, 
										event.modelGroupID, 
										event.model, 
										event.equipno, 
										event.serialno, 
										event.componentID, 
										event.componentGroupID, 
										event.manualTitle, 
										event.manualtype,
										event.goToPartsListTab) ;
	}
	
	public function loadLegacySearchResults2 (event:LegacyWebSearchEvent) {
		tabsController.loadLegacySearchResults2(event.searchResponseXML, event.searchParams)
	}
	
	public function loadLegacyGlobalSearchToTree() {
		treeController.loadLegacyGlobalSearch();
	}
	
	public function loadLegacyGlobalSearch (event:LegacyTabsEvent) {
		tabsController.loadLegacyGlobalSearch(event.curDataSourceID, event.curMfrID);
	}
	
	public function startLoading () {
		loadingController.start()
	}
	
	public function stopLoading () {
		loadingController.stop()
	}
	
	public function showError (event) {
		trace("LegacyMainController.showError()   event.message="+event.message)
		loadingController.showError(event.message)
	}
	
	private function bubbleUpEvent(event){
		trace("LegacyMainController.bubbleUpEvent()   event.type="+event.type)
		dispatchEvent(event);
	}
	
	private function checkLegacyComponentLoad(legacyComponentType:Array) {
		trace("LegacyMainController.checkLegacyComponentLoad()  legacyComponentType="+legacyComponentType)
		var clearLegacyComponentLoadInterval:Boolean = false;
		
		switch(legacyComponentType){
			case LegacyComponentData.TREE:
				if(treeController.legacyView!=undefined){
					clearLegacyComponentLoadInterval = true;
				}
				break;
			case LegacyComponentData.TABS:
				if(tabsController.legacyView!=undefined){
					clearLegacyComponentLoadInterval = true;
				}
				break;
			case LegacyComponentData.WEB_SEARCH:
				if( webSearchController.legacyView!=undefined){
					clearLegacyComponentLoadInterval = true;
					
					trace("LegacyMainController.checkLegacyComponentLoad()   set webSearchController.delegateSearch")
					//webSearchController.legacyView.search = Delegate.create(webSearchController, webSearchController.delegateSearch)
				}
				break;		
		}
		
		if(clearLegacyComponentLoadInterval==true) {
			clearInterval(checkLoadLegacyComponentIntervalID);
		}
	}
	
	public function get legacyComponentType():String{
		return configurationController.legacyComponentType;
	}
	
	public function get uniqueId():String{
		return configurationController.uniqueId;
	}
	
	public function globalSearchAddToSelection(){
		trace("LegacyMainController.globalSearchAddToSelection()")
		
		treeController.delegateLoadTabs();
	}
}
	