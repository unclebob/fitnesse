
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
$(document).on("click", "article tr.scenario td", function() {
	$(this).parent().toggleClass("open").next().toggle();
});

/**
 * Collapsible section
 */
$(document).on("click", "article .collapsible > p.title", function() {
	$(this).parent().toggleClass('closed');
});

$(document).on("click", "article .collapsible > p.title a", function(event) {
	// Do not open when clicking on a link in the title.
	event.stopPropagation();
	return true;
});

$(document).on('click', 'article .collapsible .expandall', function (event) {
	$(this).closest('.collapsible').find('.collapsible').andSelf().removeClass('closed');
	event.stopPropagation();
});

$(document).on('click', 'article .collapsible .collapseall', function (event) {
	$(this).closest('.collapsible').find('.collapsible').andSelf().addClass('closed');
	event.stopPropagation();
});

/**
 * Field validations
 */
$(document).ready(function() {
	
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
	
	$('input.wikiword').keyup(function() {
		validateField.apply(this, 
				[/^[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+$/,
		         "<p class='validationerror'>The page name should be a valid <em>WikiWord</em>!</p>"]);
	});
	
	$('input.wikipath').keyup(function() {
		validateField.apply(this, 
				[/^(?:[<>^.])?(?:[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+[.]?)+$/,
				 "<p class='validationerror'>The page path should be a valid <em>WikiPath.WikiWord</em>!</p>"]);
	});
	
});



window.onbeforeunload=stopNavIfTestRunning;

function stopNavIfTestRunning(){
  var stoptestDiv = document.querySelector("div#stop-test a img");

  if (stoptestDiv){
    return "There is a test or suite currently running.  Are you sure you want to navigate away from this page?";
  }
}