function SeUtil()
{
}

SeUtil.deciAdjust = function(s)
{		
	var n = s.indexOf(".");
	if (n != -1)
	{
		s = s.substring(0, n + 3);
	}
	return s;
}

SeUtil.formatAsDollars = function(amt, prec) 
{
	if ( (prec == "") or (prec == null) ) 
		prec = 2;
	var factor = Math.pow(10,prec);
	var num = String(Math.round(amt*factor));
	while ( num.length < prec ) num = "0"+num;
  	if ( isNaN(amt) or isNaN(prec) ) 
	{
		return("");
	} else 
	{
		return(num.substr(0,num.length-prec) + "." + num.substr(-prec));
	}
} 

SeUtil.stripNum = function(s)
{
	var c;
	var stripped = "";
	for (var i = 0; i < s.length ; i++) 
	{
		c = s.charAt(i);
		if ((!isNaN(c)) || (c == "."))
		{
			stripped += c;
		}
	}
	return(stripped);
}

SeUtil.stripChar = function(s, sc)
{
	var c;
	var stripped = "";
	for (var i = 0; i < s.length ; i++) 
	{
		c = s.charAt(i);
		if (c != sc)
		{
			stripped += c;
		}
	}
	return(stripped);
}

SeUtil.stripBrackets = function(s)
{
	var c;
	var stripped = "";
	for (var i = 0; i < s.length ; i++) 
	{
		c = s.charAt(i);
		if ((c != "[") && (c != "]"))
		{
			stripped += c;
		}
	}
	return(stripped);
}
