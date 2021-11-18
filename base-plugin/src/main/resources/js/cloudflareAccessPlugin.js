window.CloudflareAccessPlugin = (function(){
	return {
		checkForPluginUpdate: function(){
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
		},

		removeAudienceInput: function(el){
			let inputDiv = el.parentNode;
			inputDiv.parentNode.removeChild(inputDiv)
		},

		addAudienceInput: function(referenceElement){
			const inputEl = document.createElement("input");
			inputEl.type = "text";
			inputEl.className = "text long-field"
			inputEl.name = "tokenAudience"

			const removeBtn = document.createElement("button");
			removeBtn.className = "aui-button aui-button-link";
			removeBtn.onclick = function(){
				window.CloudflareAccessPlugin.removeAudienceInput(removeBtn);
			}
			removeBtn.appendChild(document.createTextNode("Remove"));

			const inputDiv = document.createElement("div");
			inputDiv.style.marginTop = "8px";
			[inputEl, document.createTextNode(" "), removeBtn].forEach(function(e){
				inputDiv.appendChild(e);
			});

			referenceElement.insertAdjacentElement('beforebegin', inputDiv);
		}
	}
})();

AJS.$(window).load(window.CloudflareAccessPlugin.checkForPluginUpdate);
