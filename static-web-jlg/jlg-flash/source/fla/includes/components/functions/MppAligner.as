#include "NumericConverter.as"

function MppAligner(){}
MppAligner.prototype.alignLeft = function (base_mc , target_mc){
	target_mc._x = base_mc._x;
}
MppAligner.prototype.alignRight = function (base_mc , target_mc){
	target_mc._x = (base_mc._x + base_mc._width) - target_mc._width;
}

MppAligner.prototype.alignTop = function (base_mc , target_mc){
	target_mc._y = base_mc._y;
}

MppAligner.prototype.alignBottom = function (base_mc , target_mc){
	target_mc._y = ( base_mc._y + base_mc._height) - target_mc._height ;
}

MppAligner.prototype.leftOf = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " Target:" + target_mc + target_mc._width);
	target_mc._x = base_mc._x - target_mc._width;
}

MppAligner.prototype.rightOf = function (base_mc , target_mc){
	target_mc._x = base_mc._x + base_mc._width;
}

MppAligner.prototype.topOf = function (base_mc , target_mc){
	target_mc._y = base_mc._y - target_mc._height;
}

MppAligner.prototype.bottomOf = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " Target:" + target_mc + " Height: " + base_mc._height);
	target_mc._y = base_mc._y + base_mc._height;
}

MppAligner.prototype.verticalCenter = function (base_mc , target_mc){
	target_mc._y =  ((base_mc._height / 2)) - (target_mc._height / 2);
}

MppAligner.prototype.horizontalCenter = function (base_mc , target_mc){
	//trace("MppAligner.prototype.horizontalCenter()  before  target_mc="+target_mc+"    target_mc._x="+target_mc._x)
	
	target_mc._x =  ( (base_mc._width / 2)) - (target_mc._width / 2);
	
	//trace("MppAligner.prototype.horizontalCenter()  after   target_mc="+target_mc+"    target_mc._x="+target_mc._x)
}

MppAligner.prototype.center = function (base_mc , target_mc){
	//trace("Based:" + base_mc + " Target:" + target_mc + " Width: " + target_mc._width);
	
	//trace("MppAligner.prototype.center()   base_mc="+base_mc)
	//trace("MppAligner.prototype.center()   target_mc="+target_mc)
	
	this.verticalCenter(base_mc, target_mc);
	this.horizontalCenter(base_mc, target_mc);
}

//-------------------MppResizer-------------------------\\

function MppResizer () {
	this.converter = new NumericConverter();
}
MppResizer.prototype.converter;
MppResizer.prototype.percentOfBaseWidth = function( base_mc, target_mc , percentage ) {
	target_mc._width = this.converter.percentOf(base_mc._width, percentage);
	//trace("WCOnverter: " +  this.converter.percentOf(base_mc._width, percentage));
}

MppResizer.prototype.percentOfBaseHeight = function( base_mc, target_mc, percentage ) {
	target_mc._height = this.converter.percentOf(base_mc._height, percentage);
	//trace("HCOnverter: " +  this.converter.percentOf(base_mc._height, percentage));
}

MppResizer.prototype.percentOfBase = function (base_mc, target_mc, percentage) {
	//trace("Resizing");
	this.percentOfBaseWidth(base_mc, target_mc, percentage);
	this.percentOfBaseHeight(base_mc, target_mc, percentage);
}


MppResizer.prototype.resizeWidthByPercent = function ( target_mc, percentage) {
	 target_mc._width = this.converter.percentOf(target_mc._width, percentage);
}
MppResizer.prototype.resizeHeightByPercent = function ( target_mc, percentage) {
	target_mc._height = this.converter.percentOf(target_mc._height, percentage);
}

MppResizer.prototype.resizeByPercent = function ( target_mc, percentage) {
	this.resizeWidthByPercent(target_mc, percentage);
	this.resizeHeightByPercent(target_mc, percentage);
}

MppResizer.prototype.sizeToWidthHundredPercent = function ( target_mc, percentage) {
	target_mc._width = this.converter.hundredPercentOf(target_mc._width, percentage);
}

MppResizer.prototype.sizeToHeightHundredPercent = function ( target_mc, percentage) {
	target_mc._height = this.converter.hundredPercentOf(target_mc._height, percentage);
}

MppResizer.prototype.sizeToHundredPercent = function ( target_mc, percentage) {
	this.sizeToWidthHundredPercent(target_mc, percentage);
	this.sizeToHeightHundredPercent(target_mc, percentage);
}

