//----------------SilentPoint------------------------\\

function SilentPoint(valx, valy) {
	this.x = valx;
	this.y = valy;
}

SilentPoint.prototype.x;
SilentPoint.prototype.y;
SilentPoint.prototype.setDimension = function(valx, valy) {
	this.x = valx;
	this.y = valy;
}

SilentPoint.prototype.getX = function (){
	return this.x;
}
SilentPoint.prototype.getY = function (){
	return this.y;
}
//-----------------------------FRACTION----------\\

function Fraction( Numerator , Denominator){
	this.numerator = Numerator;
	this.denominator = Denominator;
}
Fraction.prototype.numerator;
Fraction.prototype.denominator;
		
Fraction.prototype.Fraction = function (Numerator , Denominator ){
	this.numerator = Numerator;
	this.denominator = Denominator;
}
		
Fraction.prototype.setNumerator = function(value){
	this.numerator = value;
}
Fraction.prototype.setDenominator = function(value){
	this.denominator = value;
}
		
Fraction.prototype.getNumerator = function (){
	return this.numerator; 
}
Fraction.prototype.getDenominator = function () {
	return this.denominator;
}
		
Fraction.prototype.getDecimalValue = function (){
	return this.numerator / this.denominator;
}
		
	


//-----------------NumericConverter
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


NumericConverter.prototype.getSlope = function (startPoint , endPoint) {
	return (endPoint.y - startPoint.x) / (endPoint.x - startPoint.x);
}
		
NumericConverter.prototype.getSlopeAsFraction = function (startPoint , endPoint){
	var myfraction = new Fraction();
	myfraction.setNumerator(endPoint.y - startPoint.y);
	myfraction.setDenominator(endPoint.x - startPoint.x);
	return fraction;
}
		
NumericConverter.prototype.fractionToPercent = function (fraction ){
	return decimalToPercent(fraction.getDecimalValue()); 
}
NumericConverter.prototype.isNegative = function (value){
	var bool = false;
	if(value < 0){
			bool = true;
		} 
		return bool;
}