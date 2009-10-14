/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.event.*;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;
 
class com.smartequip.integrationApplication.legacy.LegacySecurityController extends EventDispatcher{
	
	private static var AUTHENTICATE_RETRIES:Number = 3;
	private static var AUTHORIZE_RETRIES:Number = 3;
	
	private var authenticateRetriesCount:Number = 0;
	private var authorizeRetriesCount:Number = 0;
	
	public function LegacySecurityController(){
	}
	
	public function init(){
		trace("LegacySecurityController.init()")
		/*
		System.security.allowDomain( getDomain(_level0.baseURL) );
		System.security.allowDomain( "qa1.smartequip.net" );
		System.security.allowDomain( "qa3.smartequip.net" );
		System.security.allowDomain( "www.smartequip.net" );
		System.security.allowDomain( "demo.smartequip.net" );
		System.security.allowDomain( "dev.smartequip.net" );
		System.security.allowDomain( "mcb-olews01-d.jlg.com" );
		System.security.allowDomain( "mcb-olews01-p.jlg.com" );
		System.security.allowDomain( "mcb-http01-p.jlg.com" );
		*/
		//System.security.allowDomain( _level0.javascriptDomain );
		
		//trace("LegacySecurityController.init()   _level0.javascriptDomain="+_level0.javascriptDomain+" is invalid");
		trace("LegacySecurityController.init()   _level0.manufacturerURL="+_level0.manufacturerURL);
		trace("LegacySecurityController.init()   _level0.customerURL="+_level0.customerURL);
		
		//trace("LegacySecurityController.init()   getDomain(_level0.manufacturerURL)="+getDomain(_level0.manufacturerURL));
		//trace("LegacySecurityController.init()   getDomain(_level0.customerURL)="+getDomain(_level0.customerURL));
		
		//System.security.allowDomain( getDomain(_level0.manufacturerURL) );
		//System.security.allowDomain( getDomain(_level0.customerURL) );
	}
	
	public function authenticate(){
		if(_level0.legacyComponentType == LegacyComponentData.TREE) {
			trace("LegacySecurityController.authenticate()")
			_level0.navStack = [];
			_level0.nameStack = [];
			_level0.sysURL = _level0.SEServer;
		
			if ( _level0.storeidO != "" ) _level0.storeid = _level0.storeidO ;
			if (_level0.storeid == undefined ) _level0.storeid =_level0.storeID;
		
			_level0.sysXML = new XML();
			_level0.sysElement = _level0.sysXML.createElement("SYSSIGNON");
			_level0.sysElement.attributes.storeid = _level0.storeid;
			_level0.sysElement.attributes.secustomer = _level0.curCustName;
			_level0.sysXML.appendChild(_level0.sysElement);
		
			_level0.sysReplyXML = new XML();
			_level0.sysReplyXML.onLoad  = Delegate.create(this, authenticate_response);
		
			if ( _level0.newServlet ) {
				_level0.sysXML.sendAndLoad(_level0.sysURL + "servlet/RequestDirector?helper=Authenticate", _level0.sysReplyXML);
			} else {
				_level0.sysXML.sendAndLoad(_level0.sysURL + "servlet/Authenticate", _level0.sysReplyXML);
			}
		} else {
			var event:AuthenticateCompleteEvent = new AuthenticateCompleteEvent(AuthenticateCompleteEvent.COMPLETE);
			dispatchEvent(event)
		}
	}
	
	private function authenticate_response(success:Boolean){
		trace("LegacySecurityController.authenticate_response()   success="+success)
		
		var e = _level0.sysReplyXML.firstChild;
		
		_level0.checkForError(_level0.sysReplyXML);
		
		if (e.nodeName == "SIGNONREPLY") {
			if ( _level0.baseURL == "") {
				_level0.baseURL = e.attributes.baseURL;
			}
			if ( _level0.manufacturerURL == "") {
				_level0.manufacturerURL = e.attributes.seCustMfrDir;
			}
			if ( _level0.customerURL == "") {
				_level0.customerURL = e.attributes.seCustDataDir;
			}
			System.security.allowDomain( getDomain(_level0.manufacturerURL) );
			System.security.allowDomain( getDomain(_level0.customerURL) );
			_level0.dfltMfrURL = _level0.manufacturerURL;
			_level0.customerURL = _level0.customerURL; // + _level0.curCustName ;
			_level0.helpURL = _level0.baseURL + "helpmovies/";
			_level0.seCustId = e.attributes.seCustId;
			_level0.customPO = e.attributes.customPO;
			_level0.region = e.attributes.reg;
			_level0.checkAvl = e.attributes.checkavl;
			_level0.mfrgroups = e.attributes.mfrgrps;
			_level0.introfile = _level0.customerURL + "/intro.swf"
			delete _level0.sysReplyXML;
			delete _level0.sysElement;
			delete _level0.sysXML;
			_level0.mfrXMLArray = new XML ();
			_level0.mfrXMLArray = (e.firstChild.childNodes);
			_level0.curDataSourceID = _level0.mfrXMLArray[0].firstChild.firstChild.nodeValue;
			_level0.curMfrID = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.firstChild.nodeValue;
			
			trace("LegacySecurityController.authenticate_response()     _level0.curDataSourceID="+_level0.curDataSourceID)
			
			if ( _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.firstChild.nodeValue == 1) {
				_level0.customTabs = true;
			} else { 
				_level0.customTabs = false;
			}
			_level0.mfrArrayLength = _level0.mfrXMLArray.length;
			_level0.colorCH = _level0.mfrXMLArray[0].firstChild.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.firstChild.nodeValue;
			
			_level0.userName=_level0.ssoUser;
            _level0.password=_level0.ssoPass;
            
            trace("LegacySecurityController.authenticate_response()     _level0.userName="+_level0.userName)
			trace("LegacySecurityController.authenticate_response()     _level0.password="+_level0.password)
			
			if ((_level0.userName != undefined && _level0.userName != "" && _level0.password != undefined && _level0.password != "" ) ||
				(_level0.SSOToken != ""  && _level0.SSOToken != undefined )) {
				//authorize();
				//dispatchEvent({type:"authenticate_response"})
				var event:AuthenticateCompleteEvent = new AuthenticateCompleteEvent(AuthenticateCompleteEvent.COMPLETE);
				dispatchEvent(event)
			}else {
				//loadMovieNum ( "login.swf", 100);
				//loadMovieNum ( "whitebkg.swf", 10);
				//_level0.messageBox = "Please enter your username and password";
			}
		} else {
			// init failed!  wait for retry.
			_level0.messageBox = "system unavailable, please try later.";
			
			trace("LegacySecurityController.authenticate_response()   "+_level0.messageBox)
			
			if(authenticateRetriesCount<AUTHENTICATE_RETRIES){
				authenticateRetriesCount++;
				authenticate();
			}else{
				_level0.showErrorMessage(_level0.messageBox);
			}
		}
		
		
		
	}
	
	public function authorize(){
		trace("type="+_level0.legacyComponentType+" data="+LegacyComponentData.TREE);
		if(_level0.legacyComponentType == LegacyComponentData.TREE) {
			_level0._userName = _level0.userName;
			_level0._password = _level0.password;
			var uname = _level0.userName.split("/");
			//_level300.mouseWait();
			_level0.loginXML = new XML();
			_level0.loginElement = _level0.loginXML.createElement("LOGIN");
			if ( _level0.uname[1].length > 0 ) {
				_level0.loginElement.attributes.username = _level0.uname[0];
				_level0.loginElement.attributes.loc = _level0.uname[1];
			} else {
				_level0.loginElement.attributes.username = _level0.userName;
				_level0.loginElement.attributes.loc = _level0.storeID;
			}
			_level0.loginElement.attributes.password = _level0.password;
			_level0.loginElement.attributes.erpuser = _level0.User_ID;
			_level0.loginElement.attributes.secustomerid = _level0.seCustId;
			_level0.loginElement.attributes.mfrid = _level0.curMfrID;
			_level0.loginElement.attributes.seclevel=_level0.secLevel;
			if (_level0.SSOToken == undefined) {
				_level0.SSOToken = "";
			}
			_level0.loginElement.attributes.SSOToken = _level0.SSOToken;
			_level0.loginXML.appendChild(_level0.loginElement);
			_level0.loginReplyXML = new XML();
			_level0.loginReplyXML.onLoad = Delegate.create(this, authorize_response);
			_level0.loginXML.sendAndLoad(_level0.baseURL + "servlet/RequestDirector?helper=Authorize", _level0.loginReplyXML);
			//_level0.loginXML.sendAndLoad(_level0.SEServer + "servlet/RequestDirector?helper=Authorize", _level0.loginReplyXML);
		} else {
			var event:AuthorizeCompleteEvent = new AuthorizeCompleteEvent(AuthorizeCompleteEvent.COMPLETE);
			dispatchEvent(event)			
		}
	}
	
	private function authorize_response (success:Boolean) {
		trace("LegacySecurityController.authorize_response()    success="+success)
		
		_level0.enterClicked = false;
		var e = _level0.loginReplyXML.firstChild;
		
		_level0.checkForError(_level0.loginReplyXML);
		
		if (e.nodeName == "LOGINREPLY" && e.attributes.Status == "OK") {
			// Save the session ID for future communications with server
			_level0.sessionID = e.attributes.session;
			_level0.userID = e.attributes.user;
			_level0.startingScene = e.attributes.StartingSceneURI;
			_level0.emailAddress = e.attributes.Email;
			_level0.phone = e.attributes.phone;
			_level0.custAcctNo = e.attributes.AcctNo;
			_level0.custID = e.attributes.custid;
			_level0.pltype = e.attributes.pltype;
			_level0.pldesc = e.attributes.pldesc;
			_level0.roleID = e.attributes.roleid;
			// Parse out multiple locations
			_level0.stores = e.attributes.loc.split(",");
			// Init storeID to first store
			_level0.storeID = _level0.stores[0];
			_level0.region = e.attributes.reg;
			_level0.poFlag = e.attributes.po_ok;
			_level0.buyLimit = e.attributes.buying_limit;
			_level0.buyLower = e.attributes.buying_lower;
			_level0.buyCombined = e.attributes.buying_comb;
			_level0.validName = _level0.username;
			_level0.reorderFlag = e.attributes.reorder_ok;
			_level0.username = "";
			_level0.password = "";
			_level0.messageBox = "";
			_level0.curProductGroup = 0;
			_level0.curProdID = 0;
			_level0.curModel = "";
			_level0.prodList = "";
			_level0.modelList = "";
			delete _level0.loginXML;
			delete _level0.loginElement;
			delete _level0.loginReplyXML;
			_level0.userName = "";
			_level0.password = "";
			_level0.curMfr = 0;
			
			//dispatchEvent({type:"authorize_response"})
			var event:AuthorizeCompleteEvent = new AuthorizeCompleteEvent(AuthorizeCompleteEvent.COMPLETE);
			dispatchEvent(event)			
		} else {
			// Login failure
			//loadMovieNum ( "login.swf", 100);
			//loadMovieNum ( "whitebkg.swf", 10);
			_level0.messageBox = "Login failed";
			//_level0.showErrorMessage(_level0.messageBox);
			
			trace("LegacySecurityController.authorize_response()   "+_level0.messageBox)
			
			if(authorizeRetriesCount<AUTHORIZE_RETRIES){
				authorizeRetriesCount++;
				authorize();
			}else{
				_level0.showErrorMessage(_level0.messageBox);
			}
		}
	}
	
	public static function getDomain(url) {
		
		var domain;
		
		_level0.startIndex = url.indexOf("//", 0) + 2;
		_level0.endIndex = url.indexOf("/", _level0.startIndex);
		
		//domain = url.substring(_level0.startIndex, _level0.endIndex);
		domain = url.substring(0, _level0.endIndex);
		
		return domain;
	}
}
	