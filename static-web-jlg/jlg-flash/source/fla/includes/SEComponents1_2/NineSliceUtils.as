
function NSUtil () {
	
}

NSUtil.prototype.setSize = function ( target_mc , newWidth , newHeight ) {
	this.setWidth ( target_mc, newWidth , 1 );
	this.setWidth ( target_mc, newWidth , 4 );
	this.setWidth ( target_mc, newWidth , 7 );
	this.setHeight ( target_mc, newHeight, 1 );
	this.setHeight ( target_mc, newHeight, 2 );
	this.setHeight ( target_mc, newHeight, 3 );
}

NSUtil.prototype.setWidth = function ( target_mc , newWidth , startIndex ) {
	var first_mc = target_mc[ "slice" + startIndex];
	var second_mc = target_mc["slice" + ( startIndex + 1)];
	var third_mc = target_mc["slice" + ( startIndex + 2) ];
	first_mc._x = 0;
	second_mc._width = newWidth - (first_mc._width + third_mc._width );
	second_mc._x = first_mc._x + first_mc._width;
	third_mc._x = second_mc._x + second_mc._width;
}

NSUtil.prototype.setHeight = function ( target_mc, newHeight, startIndex ) {
	var first_mc = target_mc["slice" + startIndex ];
	var second_mc = target_mc["slice" + (startIndex + 3)];
	var third_mc = target_mc["slice" + (startIndex + 6)];
	first_mc._y = 0;
	second_mc._height = newHeight - ( first_mc._height + third_mc._height );
	second_mc._y = first_mc._y + first_mc._height;
	third_mc._y = second_mc._y + second_mc._height;
}



//Create an Instance 
_global.NineSliceUtil = new NSUtil();


//  //COLOR MANAGER

function SEColorManager () {
	
}

SEColorManager.prototype.changeColor = function( target_mc , _color ) {
	var mccolor = new Color( target_mc) ;
	mccolor.setRGB( _color );
}

SEColorManager.prototype.changeTextColor = function ( target_txt, _color) {
	var format = new TextFormat();
	format.color = _color;
	target_txt.setTextFormat( format );
	target_txt.setNewTextFormat( format );
}

_global.ColorManager = new SEColorManager();




// ALIGNER

function SEAligner(){}
SEAligner.prototype.alignLeft = function (base_mc , target_mc){
	target_mc._x = base_mc._x;
}
SEAligner.prototype.alignRight = function (base_mc , target_mc){
	target_mc._x = (base_mc._x + base_mc._width) - target_mc._width;
}

SEAligner.prototype.alignTop = function (base_mc , target_mc){
	target_mc._y = base_mc._y;
}

SEAligner.prototype.alignBottom = function (base_mc , target_mc){
	target_mc._y = ( base_mc._y + base_mc._height) - target_mc._height ;
}

SEAligner.prototype.leftOf = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " Target:" + target_mc + target_mc._width);
	target_mc._x = base_mc._x - target_mc._width;
}

SEAligner.prototype.rightOf = function (base_mc , target_mc){
	target_mc._x = base_mc._x + base_mc._width;
}

SEAligner.prototype.topOf = function (base_mc , target_mc){
	target_mc._y = base_mc._y - target_mc._height;
}

SEAligner.prototype.bottomOf = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " Target:" + target_mc + " Height: " + base_mc._height);
	target_mc._y = base_mc._y + base_mc._height;
}

SEAligner.prototype.centerY = function (base_mc , target_mc){
	target_mc._y =  ((base_mc._height / 2)) - (target_mc._height / 2);
}

SEAligner.prototype.centerX = function (base_mc , target_mc){
	target_mc._x =  ( (base_mc._width / 2)) - (target_mc._width / 2);
}

SEAligner.prototype.center = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " WID: "  +  base_mc._width  +  " Target:" + target_mc + " Width: " + target_mc._width);
	 
	this.centerY(base_mc, target_mc);
	this.centerX(base_mc, target_mc);
}

_global.AlignManager = new SEAligner();

function NumericConverter() {}
NumericConverter.prototype.percentToDecimal = function (value){
	value = value * .01;
	return value;
}
NumericConverter.prototype.percentage = function ( part, whole){
	return Math.round((part / whole) * 100);
}
NumericConverter.prototype.percentOf = function( hundrePercentValue , percentage){
	return hundrePercentValue *  this.percentToDecimal(percentage);
}
NumericConverter.prototype.hundredPercentOf = function ( base, percentage){
	return  (base * 100) / percentage;
}
NumericConverter.prototype.decimalToPercent = function(value){
	return value * 100;
}

_global.NumberManager = new NumericConverter();
