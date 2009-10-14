import mx.core.UIComponent;

class includes.seAS2.renderer.PDFIconRenderer extends UIComponent {
	private var icon:MovieClip;
	public function PDFIconRenderer () {
		
	}
	
	
	public function size( Void )  {
		icon._x = 2;
		icon._y = -2;
	}
	
	public function getPreferredHeight(Void):Number {
		return 20;
	}
	
	public function getPreferredWidth( Void ) :Number {
		return 20;
	}
	
	public function setValue ( textvalue:String , item:Object , selected:Boolean ) : Void {
		 
		if( item.icon != true ) {
			icon._visible = false;
		} else    {
			icon._visible = true;
		}
	}
}