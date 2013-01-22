
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

	function getMaxErrorNavIndex(){
		return parseInt($("#error-nav-max").text());
	}

	function getCurrentErrorNavIndex(){
		return parseInt($("#error-nav-text").val());
	}

	function setCurrentErrorNavIndex(index){
		$("#error-nav-text").val(index);
	}

	function incrementErrorNavIndex(){
		var currentErrorNavIndex = getCurrentErrorNavIndex();
		if( isNaN(currentErrorNavIndex) || currentErrorNavIndex === getMaxErrorNavIndex()){
			currentErrorNavIndex = 1;
		} else {
			currentErrorNavIndex += 1;
		}
		setCurrentErrorNavIndex(currentErrorNavIndex);
	}

	function decrementErrorNavIndex(){
		var currentErrorNavIndex = getCurrentErrorNavIndex();
		if( isNaN(currentErrorNavIndex) || currentErrorNavIndex === 1){
			currentErrorNavIndex = getMaxErrorNavIndex();
		} else {
			currentErrorNavIndex -= 1;
		}
		setCurrentErrorNavIndex(currentErrorNavIndex);
	}

    function unfoldErrors(element) {
        element.parents('.scenario-detail').show().prev().removeClass('closed');
        element.parents('.collapsible').removeClass('closed invisible');
        element.parents('tr.hidden').removeClass('hidden');
    }
    
	function navigateToCurrentError(){
		var currentErrorNavIndex = getCurrentErrorNavIndex();
		$("span.fail, span.error, td.fail, td.error")
            .removeClass("selected-error")
		    .filter(function() {
                return $(this).data("error-num") == currentErrorNavIndex
            }).each(function(){
				unfoldErrors($(this)); 
				$(this).addClass("selected-error");
				$('html, body').animate({
     				scrollTop: $(this).offset().top - 200
 				}, 500);
			});
	}

	$("#error-nav-prev").click(function(){
		decrementErrorNavIndex();
		navigateToCurrentError();
	});

	$("#error-nav-next").click(function(){
		incrementErrorNavIndex();
		navigateToCurrentError();
	});

	$("#error-nav-text").change(function(){
		if(getCurrentErrorNavIndex() > 0 && getCurrentErrorNavIndex() <= getMaxErrorNavIndex()){
			navigateToCurrentError();
		}
	});


    /**
     * Open scenario's and collapsed sections which contain failed or errorous tests
     */
    unfoldErrors($('.fail,.error'));
});

function initErrorMetadata(){
	var i = 1;
	$("table span.fail, table span.error, table td.fail, table td.error").each(function(){ $(this).data("error-num", i); i++});
	$("#error-nav-max").text(i - 1);
}


/** Backwards compatibility */
function toggleCollapsable(id) { $('#' + id).toggle().parent('.collapse_rim').toggleClass('open'); }
function expandAll() { $('.collapse_rim').each(function(i, e) { if (!$(e).hasClass('open')) { toggleCollapsable($(e).children().last().attr('id')) } }); }
function collapseAll() { $('.collapse_rim').each(function(i, e) { if ($(e).hasClass('open')) { toggleCollapsable($(e).children().last().attr('id')) } }); }

