function checkForPluginUpdate(){
	AJS.$.ajax({
		  url: "/plugins/servlet/cloudflareaccess/checkForUpdate",
		  type: "GET",
		  dataType: "json",
		  success: function(response){
		    console.log(response);
		    var updateStatus = response;
		    if(updateStatus.hasNewVersion){
			    AJS.flag({
			        type: 'warning',
			        title: 'Cloudflare Access Plugin',
			        body: 'A new version for Cloudflare Access Plugin is available, please update.'
			    });		    	
		    }
		  }
	});	
}

AJS.$(window).load(checkForPluginUpdate);
