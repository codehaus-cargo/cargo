import mx.core.UIComponent;
import mx.controls.Button;
class includes.seAS2.views.OkView  extends UIComponent {
	private var message_txt:TextField;
	private var ok_btn:Button;
	public static var VIEW:OkView; 
	public static var MESSAGE:String;
	private var window:MovieClip;
	
	private var _message:String;
	public function OkView () {
		OkView.VIEW  = this;
		this.window = this._parent;
	}
	
	public function onLoad () : Void {
		this.message_txt.text = OkView.MESSAGE ;
		this.ok_btn.addEventListener( "click", this.clickHandler );
	}
	
	private function clickHandler( _event:Object): Void {
		OkView.VIEW.eventHandler ( _event ) ;
	}
	
	private function eventHandler( _event:Object  ) : Void {
		switch( _event.type ) {
			case "click":
			
				this.window.deletePopUp();
				var _event:Object = new Object();
				_event.type = "okClick";
				this.dispatchEvent( _event );
			break;
		}
	}
}