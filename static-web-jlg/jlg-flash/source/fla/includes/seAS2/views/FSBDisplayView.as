import mx.core.UIComponent;

import mx.controls.TextArea;
import mx.controls.Button;
import mx.containers.Window;
import mx.controls.DataGrid;
import mx.controls.gridclasses.DataGridColumn;

import includes.seAS2.sys.SEVars;

import includes.seAS2.net.FSBDataLoader;
import includes.seAS2.net.FsbPdfLoader;
class includes.seAS2.views.FSBDisplayView  extends UIComponent {
	
	public static var VIEW:FSBDisplayView; 
	public var pubNum_txt:TextField;
	public var pubDesc_txt:TextField;
	
	public var distributionDate_txt:TextField;
	public var warranty_txt:TextField;
	
	public var partsreq_ta:TextArea;
	public var note_ta:TextArea;
	
	public var files_dg:DataGrid;
	public var models_dg:DataGrid;
	
	public var back_btn:Button;
	public var cancel_btn:Button;
	
	private var loader:FSBDataLoader;
	private var pdfloader:FsbPdfLoader;
	private var window:MovieClip;
	
	 
	public function FSBDisplayView () {
		SEVars.setVars();
		
		this.window = this._parent;
		FSBDisplayView.VIEW = this;
		
	}
	
	public function init ():Void {
		super.init();
	}
	
	public function createChildren() : Void {
		super.createChildren();
		this.invalidate();
	}
	
	public function draw():Void {
		
	}
	
	public function onLoad():Void {
		SEVars.setVars();
		this.cancel_btn.addEventListener("click", this.clickHandler );
		
		files_dg.setStyle("themeColor", 0xFF9900); 
		
		files_dg.setStyle("useRollOver", true);
		files_dg.setStyle("rollOverColor", 0xFF9900);
	
		
		
		var pdfIconCol:DataGridColumn = new DataGridColumn ( "viewfilelabel");
		pdfIconCol.headerText = "";
		pdfIconCol.width = 30;
		pdfIconCol.sortable = false;
		pdfIconCol.cellRenderer = "PDFIconRenderer";
		 
		files_dg.addColumn( pdfIconCol);
		 
		
		var fileNameCol:DataGridColumn = new DataGridColumn ( "filename");
		fileNameCol.headerText = "File Name" ;
		fileNameCol.width = 375;
		//fileNameCol.useHandCursor = true;
		//fileNameCol.sortable = false;
		//fileNameCol.resizable = false;
		
		files_dg.addColumn( fileNameCol );
		
		
		files_dg.addEventListener("change",  this.gridListener);
		files_dg.addEventListener("cellPress",  this.gridListener);
		
		var modelNameCol:DataGridColumn = new DataGridColumn("modelname");
		modelNameCol.headerText = "Model Name";
		modelNameCol.width = 110;
		modelNameCol.sortable = false;
		modelNameCol.resizable = false;
		models_dg.addColumn( modelNameCol );
		
		
		this.loader = new FSBDataLoader();
		this.loader.addEventListener( "complete", this.dataLoaded);
		var _param:Object = new Object();
		_param.bulletinNumber = _level0.machineInfoLevel.FSBData.bulletinNumber;
		_param.dataSourceID = SEVars.dataSourceID
		_param.mfrID = SEVars.mfrID;
		 
		 
		 
		this.pdfloader = new FsbPdfLoader();
		this.pdfloader.addEventListener("parseComplete" , loadPdfFile );

		var servletURL:String = SEVars.requestDirector + "MachineInfoFSBDetails";
		 _level300.mouseWait();
		this.loader.load( servletURL , _param );
		//LOCAL TESTING 
		//this.loader.load("datatest/MachineInformation/response_miFsbDDisplay.txt", _param );	
		//this.loader.load("datatest/MachineInformation/response_FSBNoResult.txt", _param );	
	}
	 
	public function renderData ( provider:Object ) : Void {
		
		trace("FSBDisplayView.renderData()   provider.pubNum="+provider.pubNum)
		trace("FSBDisplayView.renderData()   provider.date="+provider.date)
		
		
		if ( provider.noResult == true ) {
			 
			this.window.title = "No data found!!!";
		} else {
			this.pubNum_txt.text = provider.pubNum;
			this.pubDesc_txt.text = provider.pubDesc;
			this.warranty_txt.text = provider.warranty;
			if(provider.date!=undefined) this.distributionDate_txt.text = provider.date;
			if(provider.notes!=undefined) this.note_ta.text = provider.notes;
			partsreq_ta.text = provider.partsReq;
		 	
			for ( var i:Number = 0 ; i < provider.files.length ; i++ ) {
				provider.files[i].icon = true;
			}
			this.files_dg.dataProvider = provider.files;
		 
			this.models_dg.dataProvider = provider.models
		}
		
	}
	
	public function loadPdfFile ( _event:Object ) : Void {
		FSBDisplayView.VIEW.eventsHandler ( _event );
	}
	
	public function dataLoaded( _event:Object ) : Void {
		 _level300.mouseNormal();
		FSBDisplayView.VIEW.eventsHandler ( _event );
	}
	
	public function clickHandler ( _event:Object ) : Void {
		 FSBDisplayView.VIEW.eventsHandler ( _event ) ;
	}
	
	public function gridListener ( _event:Object ) : Void {
		 
		FSBDisplayView.VIEW.eventsHandler ( _event );
	}
	
	public function eventsHandler ( _event:Object ) : Void {
		switch ( _event.type ) {
			case "click":
				switch ( _event.target ) {
						case this.back_btn:
							 
						break;
						
						case this.cancel_btn:
							unloadMovieNum(1129);
							this.window.deletePopUp();
							
						break;
				}
			break;
			
			case "complete":
				 
				this.renderData( _event.data );
			break;
			
			case "change":
			
			break;
			
			case "cellPress":
				
				var i:Number = 0;
				var item:Object =  this.files_dg.dataProvider[ _event.itemIndex ];
				 
				//if( _event.columnIndex == 1 ) {
					var _param:Object = new Object();
					_param.bulletinNumber = _level0.machineInfoLevel.FSBData.bulletinNumber;
					 
					_param.filename = item.filename;
					_param.dataSourceID = SEVars.dataSourceID
					_param.mfrID = SEVars.mfrID;
					
					var servletURL:String = SEVars.requestDirector + "MachineInfoFSBPDF";
					
					//servletURL = "http://localhost/testcase/response_fsbpdf.php";
					this.pdfloader.loadPDFPath (servletURL,  _param );
					//var targetPDF:String = SEVars.domainURL + item.url;
					//trace("targetPDF: " + targetPDF ) ;
					//getURL( item.url );
					//getURL( targetPDF , "_blank" );
				//}
			break;
			
			case "parseComplete":
				var filepath:String = SEVars.domainURL + _event.filepath;
				trace("Parse FilePath: " + filepath );
				getURL ( filepath, "_blank" );
			break;
		}
	}
}