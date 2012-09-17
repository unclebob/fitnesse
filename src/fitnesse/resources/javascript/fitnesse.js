
function symbolicLinkRename(linkName, resource)
{
  var newName = document.symbolics[linkName].value.replace(/ +/g, '');

  if (newName.length > 0)
    window.location = resource + '?responder=symlink&rename=' + linkName + '&newname=' + newName;
  else
    alert('Enter a new name first.');
}

function doSilentRequest(url)
{
  $.get(url);
  return false;
}

/**
 *  Scenario's (after test execution)
 */
$(document).on("click", "article tr.scenario td", function () {
	$(this).parent().toggleClass('closed').next().toggle();
});

/**
 * Collapsible section
 */
$(document)
	.on("click", "article .collapsible > p.title", function () {
		$(this).parent().toggleClass('closed');
	})
	.on("click", "article .collapsible > p.title a", function (event) {
		// Do not open section when clicking on a link in the title, just follow the link.
		event.stopPropagation();
		return true;
	})
	.on('click', 'article .collapsible .expandall', function (event) {
		var section = $(this).closest('.collapsible');
		section.find('.collapsible').andSelf().removeClass('closed');
		section.find('.scenario').removeClass('closed').next().show();
		return false;
	})
	.on('click', 'article .collapsible .collapseall', function (event) {
		section = $(this).closest('.collapsible');
		section.find('.collapsible, .scenario').andSelf().addClass('closed');
		section.find('.scenario').addClass('closed').next().hide();
		return false;
	});

/**
 * Notify user when changing page while test execution is in progress.
 */
window.onbeforeunload = function () {
	if (document.querySelector("li#test-action .stop")){
		return "There is a test or suite currently running.\nAre you sure you want to navigate away from this page?";
	}
};

$(document).ready(function() {

	/**
	 * Change a.button to a real button (so it looks the same), retaining link behaviour
	 */
	$('a.button').replaceWith(function () {
		var self = $(this);
		var button = $('<button/>');
		button.text(self.text());
		button.click(function () {
			window.location = self.attr('href');
			return false;
		});
		return button;
	});
	
	/**
	 * Field validations
	 */
	function validateField(re, msg) {
		var pageNameError = $(this).data("error");
		if (!re.test($(this).val())) {
			if (!pageNameError) {
				pageNameError = $(msg);
				$(this).after(pageNameError);
				$(this).data("error", pageNameError);
			}
			pageNameError.show();
		} else {
			if (pageNameError) {
				pageNameError.hide();
			}
		}
	}
	
	$('input.wikiword').keyup(function () {
		validateField.apply(this, 
				[/^[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+$/,
		         "<p class='validationerror'>The page name should be a valid <em>WikiWord</em>!</p>"]);
	});
	
	$('input.wikipath').keyup(function () {
		validateField.apply(this, 
				[/^(?:[<>^.])?(?:[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+[.]?)+$/,
				 "<p class='validationerror'>The page path should be a valid <em>WikiPath.WikiWord</em>!</p>"]);
	});
	
});

