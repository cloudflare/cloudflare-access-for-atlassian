function checkForPluginUpdate(){
	AJS.$.ajax({
		  url: AJS.contextPath() + "/rest/cloudflare/1.0/pluginUpdateAvailability/check.json",
		  type: "GET",
		  dataType: "json",
		  success: function(response){
		    console.log(response);
		    var updateStatus = response;
		    if(updateStatus.newVersionAvailable){
			    AJS.flag({
			        type: 'warning',
			        title: 'Cloudflare SSO Plugin',
			        body: 'You are no longer using the most current release of the Cloudflare SSO Plugin. New updates may include important security or performance features. Please update from the open source repository on GitHub.'
			    });
		    }
		  }
	});	
}

AJS.$(window).load(checkForPluginUpdate);
