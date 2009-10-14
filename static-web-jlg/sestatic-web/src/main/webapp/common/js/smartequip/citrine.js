/*
 * Depends on Jquery on cookie
 * 
 */
function sessionTimeOut(info) {

	var jsessionId = $.cookie("JSESSIONID");
	
	info = info +"|"+jsessionId;
	
	$.ajax( {
		type :"GET",
		url :"/CitrineSeWeb/frontendlogger/log?logmsg="+info
	});	

	
	

	parent.redirectHome(info);
};	


/*
 * Depends on Jquery for ajax call
 * 
 */
function keepAliveSEBackEnd(hostName) {
	
	$.ajax( {
		type :"GET",
		cache: false,
		url :hostName+"/CitrineSeWeb/keepalive/alive",
		success: function(retValue) {
			var ret = eval("(" + retValue + ")");
			if( "-100" == ret.rcode) {
				//time out session
				sessionTimeOut("keep alive backend timed out. ret code="+ret.rcode);
			}
		}
	});

	$.ajax( {
		type :"GET",
		cache: false,
		url :hostName+"/SELegacyRequestDirectorWeb/keepalive/alive",
		success: function(retValue) {
			var ret = eval("(" + retValue + ")");
			if( "-100" == ret.rcode) {
				//time out session
				sessionTimeOut("keep alive backend timed out. ret code="+ret.rcode);
			}
		}
	});
	
};