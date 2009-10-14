/**
 *
 */
/**
 *
 */
import mx.utils.Delegate;
import mx.events.EventDispatcher;
import com.smartequip.integrationApplication.legacy.LegacyComponentData;

class com.smartequip.integrationApplication.legacy.LegacyIntroController extends EventDispatcher{
	
	
	public function LegacyIntroController(){
	}
	
	
	
	
	
	public function init(){
		trace("LegacyIntroController.init()")
		
		var introFile;
		
		if(_level0.startingScene!=""){
			var base = new Array();
			base = _level0.startingScene.split("/");
			//thisScreen = base[0];
			if ( base[2] != "" ) {
				introFile =  eval( "_level0." + base[1] ) ;
				introFile = introFile + "/" + base[2];
			} else {
				introFile = base[0];
			}
		}else{
			introFile = "intro.swf";	
		}
		
		trace("LegacyTabsController.delegateOnReleaseFunction()   introFile="+introFile)
		introFile = _level0.staticWebURL + "intropartsservices.swf"
		loadMovieNum(introFile,200);
	}
	
	
	public function preparePositionAndScale(){
		/*
		_level0.legacyComponent_x = - 430;
		_level0.legacyComponent_y = - 185;
		_level0.legacyComponent_xscale = 150;
		_level0.legacyComponent_yscale = 150;
		*/
	}
	
	public function get legacyView():MovieClip{
		return _level200;
	}
	
}
	