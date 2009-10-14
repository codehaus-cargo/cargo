#include "NumericConverter.as"

// DRAWER

//------------------ CLASS SILENT POINT

//////---------------------------------------------------///
function drawUpArrow(target_mc,color,w,h){
	var X,Y;
	var reachedlimit = false;
	target_mc.moveTo(0,h);
	target_mc.beginFill(color);
	Y = h;
	for(X = w; X >= 0; X--){
		target_mc.lineTo(X,Y);
		if(Y <= 0){
			reachedlimit = true;
		}
		if(reachedlimit == false){
			Y--;
		} else {
			Y++;
		}
		target_mc.lineTo(X,Y);
	}
	target_mc.endFill();
}

function drawDownArrow(target_mc, color, Width, Height){
	var X,Y;
	var reachedlimit = false;
	target_mc.moveTo(0,0);
	target_mc.beginFill(color);
	Y = 0;
	for(X = Width; X >= 0; X--){
		target_mc.lineTo(X,Y);
		if(Y >= Height){
			reachedlimit = true;
		}
		if(reachedlimit == false){
			Y++;
		} else {
			Y--;
		}
		target_mc.lineTo(X,Y);
	}
	target_mc.endFill();
}

function drawFrame(target_mc, color, w, h, thick){
	target_mc.lineStyle(thick,color);
	target_mc.moveTo(0,0);
	target_mc.lineTo(w,0);
	target_mc.lineTo(w,h);
	target_mc.lineTo(0,h);
	target_mc.lineTo(0,0);
}

function drawBox(target_mc, color, w, h , alpha){
	target_mc.moveTo(0,0);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(w,0);
	target_mc.lineTo(w,h);
	target_mc.lineTo(0,h);
	target_mc.lineTo(0,0);
	target_mc.endFill();
}


function drawPolyLine (target_mc, color, startPoint, endPoint, alpha) {
	target_mc.moveTo(startPoint.getX(),startPoint.getY());
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(endPoint.getX() ,endPoint.getY());
	target_mc.lineTo(endPoint.getX(),endPoint.getY() + 1);
	target_mc.lineTo(startPoint.getX() + 1 ,startPoint.getY() + 1);
	target_mc.lineTo(startPoint.getX(),startPoint.getY());
	target_mc.endFill();
}

function drawPixelHLine(target_mc,color, startx, starty, linelen, alpha){
	target_mc.moveTo(startx,starty);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(startx + linelen ,starty);
	target_mc.lineTo(startx + linelen,starty + 1);
	target_mc.lineTo(startx,starty + 1);
	target_mc.lineTo(startx,starty);
	target_mc.endFill();
}

function drawPixelVLine(target_mc, color, startx, starty, linelen, alpha){
	target_mc.moveTo(startx,starty);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(startx ,starty + linelen);
	target_mc.lineTo(startx + 1,starty + linelen);
	target_mc.lineTo(startx + 1,starty);
	target_mc.lineTo(startx,starty);
	target_mc.endFill();
}

function drawPixelFrame(target_mc, color, w, h , alpha){
	trace("target: " + target_mc);
	target_mc.moveTo(0,0);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(w,0);
	target_mc.lineTo(w,h);
	target_mc.lineTo(w -1 ,h);
	target_mc.lineTo(w - 1,1);
	target_mc.lineTo(0,1);
	target_mc.lineTo(0,h);
	target_mc.lineTo(w - 1,h);
	target_mc.lineTo(w - 1,h - 1);
	target_mc.lineTo(1,h - 1);
	target_mc.lineTo(1,1);
	target_mc.endFill();
}

function drawVectorLine(target_mc, color, X, Y, thick, alpha) {
	target_mc.lineStyle(thick,color,alpha);
	target_mc.moveTo(0,0);
	target_mc.lineTo(X,Y);
}

function centerMC(parent_mc, child_mc) {
	child_mc._x = (parent_mc._x + parent_mc._width / 2) - (child_mc._width / 2);
	child_mc._y =  (parent_mc._y + parent_mc._height / 2) - (child_mc._height / 2);
}


function drawPixelPoint(target_mc, color, X, Y, alpha){
	target_mc.moveTo(X,Y);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(X + 1, Y);
	target_mc.lineTo(X + 1, Y + 1);
	target_mc.lineTo(X ,Y + 1);
	target_mc.lineTo(X,Y);
	target_mc.endFill();
}

function unDraw(target_mc){
	target_mc.clear();
}

function drawBoxy(target_mc, startx, starty, w , h, color, alpha) {
	//trace("X: " + startx + " Y:" + starty + " WIDTH:" + w + " HEIGHT:" + h + " COLOR:" + color + " ALPHA:" + alpha);
	target_mc.moveTo(startx,starty);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(startx + w,starty);
	target_mc.lineTo(startx + w, starty + h);
	target_mc.lineTo(startx,starty + h);
	target_mc.lineTo(startx,starty);
	target_mc.endFill();
}

function drawEllipseBorder (target_mc, startx, starty, width , height , color, alpha){
	
	drawPixelHLine(target_mc, color, startx + 2, starty,  width - 4, alpha);
	drawPixelHLine(target_mc, color, startx + 2, starty + height - 1 ,width - 4, alpha);
	drawPixelVLine(target_mc, color, startx, starty + 2, height - 4, alpha);
	drawPixelVLine(target_mc, color, startx + width - 1, starty + 2 ,  height - 4, alpha);
	
	//-- Draw The Four Points -- \\
	drawPixelPoint(target_mc, color, startx + 1,starty + 1, alpha);
	drawPixelPoint(target_mc, color, startx +  width - 2, starty + 1, alpha);
	
	drawPixelPoint(target_mc, color, startx + 1, starty + height - 2, alpha);
	drawPixelPoint(target_mc, color, startx + width - 2, starty + height - 2, alpha);
}

function drawEllipseFill(target_mc, startx, starty, width , height , color, alpha){
	drawBoxy(target_mc, startx + 2, starty, width - 4, height , color, alpha);
	drawBoxy(target_mc, startx + 1 , starty + 1, width - 2, height -  2, color, alpha);
	drawBoxy(target_mc, startx, starty + 2, width, height - 4, color, alpha);
}

function drawEllipseFade(target_mc, startx, starty, width , height , color, alpha, thickness,  type, fade){
	var ctr = 1;
	if(type == "IN"){
		for( ctr = 1; ctr <= thickness; ctr++){
			alpha -= fade;
			drawEllipseBorder(target_mc, startx + ctr , starty + ctr, width - (ctr*2) , height - (ctr*2) , color, alpha);
		}
	} else if(type == "OUT") {
		for( ctr = 0; ctr <= thickness; ctr++){
			alpha -= fade;
			drawEllipseBorder(target_mc, startx - ctr , starty - ctr, width +(ctr*2) , height + (ctr*2) , color, alpha);
		}
	}
}

function drawFrameInPos(target_mc, startx, starty, width , height , color, alpha){
	drawPixelHLine(target_mc, color, startx + 1, starty,  width  - 1, alpha);
	drawPixelHLine(target_mc, color, startx, starty + height , width + 1, alpha);
	
	drawPixelVLine(target_mc, color, startx, starty, height, alpha);
	drawPixelVLine(target_mc, color, startx + width, starty,  height, alpha);
}

function drawBoxInPos(target_mc, startx, starty, w, h , color, alpha){
	target_mc.moveTo(startx,starty);
	target_mc.beginFill(color,alpha )
	target_mc.lineTo(startx + w, starty);
	target_mc.lineTo(startx + w, starty + h);
	target_mc.lineTo(startx,starty + h);
	target_mc.lineTo(startx,starty);
	target_mc.endFill();
}


function drawFrameFade(target_mc, startx, starty, width , height , color, alpha, thickness,  type, fade){
	var ctr = 1;
	if(type == "IN"){
		for( ctr = 1; ctr <= thickness; ctr++){
			alpha -= fade;
			drawFrameInPos(target_mc, startx + ctr , starty + ctr, width - (ctr*2) , height - (ctr*2) , color, alpha);
		}
	} else if(type == "OUT") {
		for( ctr = 0; ctr <= thickness; ctr++){
			alpha -= fade;
			drawFrameInPos(target_mc, startx - ctr , starty - ctr, width +(ctr*2) , height + (ctr*2) , color, alpha);
		}
	}
}


function drawVFade(target_mc, startx, starty, thickness , color, alpha,  length,  type, fade){
	if(type == "UP"){
		for( ctr = 1; ctr <=length; ctr++){
			alpha -= fade;
			drawPixelHLine(target_mc, color, startx, starty - 1, thickness , alpha);
		}
	} else if(type == "DOWN") {
		for( ctr = 0; ctr <= length; ctr++){
			alpha -= fade;
			drawPixelHLine(target_mc, color, startx, starty + ctr, thickness , alpha);
		}
	}
}

function drawHFade(target_mc, startx, starty, thickness , color, alpha, length,  type, fade){
	if(type == "LEFT"){
		for( ctr = 1; ctr <= length; ctr++){
			alpha -= fade;
			drawPixelVLine(target_mc, color, startx - ctr , starty, thickness , alpha);
		}
	} else if(type == "RIGHT") {
		for( ctr = 0; ctr <= length; ctr++){
			alpha -= fade;
			drawPixelVLine(target_mc, color, startx + ctr , starty, thickness , alpha);
		}
	}
}