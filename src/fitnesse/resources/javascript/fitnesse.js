
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
 *  Scenario's and Exceptions (after test execution)
 */
$(document)
    .on("click", "article tr.scenario td, article tr.exception td", function () {
        $(this).parent().toggleClass('closed').nextUntil(":not(.exception-detail, .scenario-detail)").toggleClass("closed-detail");
    });

/**
 * Collapsible section
 */
$(document)
	.on("touchstart click", "article .collapsible > p.title", function () {
		$(this).parent().toggleClass('closed');
	})
	.on("click", "article .collapsible > p.title a", function (event) {
		// Do not open section when clicking on a link in the title, just follow the link.
		event.stopPropagation();
		return true;
	})
	.on('click', 'article .collapsible .expandall', function () {
		var section = $(this).closest('.collapsible');
		section.find('.collapsible').andSelf().removeClass('closed');
		section.find('.scenario').removeClass('closed').next().show();
		return false;
	})
	.on('click', 'article .collapsible .collapseall', function () {
		var section = $(this).closest('.collapsible');
		section.find('.collapsible, .scenario').andSelf().addClass('closed');
		section.find('.scenario').addClass('closed').next().hide();
		return false;
	});

/**
 * Hide/show passed tests
 */
$(document)
    .on('change', '.pageHistory #hidePassedTests', function () {
        var elems = $('td.date_field.pass').parent();
        if (this.checked) {
            elems.hide();
        } else {
            elems.show();
        }
    })
    .on('change', '.testHistory #hidePassedTests', function () {
        // 3rd column shows failed tests.
        var elems = $('tr > td:nth-child(5).pass').parent();
        if (this.checked) {
            elems.hide();
        } else {
            elems.show();
        }
    })
    .on('change', '.suiteExecutionReport #hidePassedTests', function () {
        var elems = $('tr.pass');
        if (this.checked) {
            elems.hide();
        } else {
            elems.show();
        }
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


});

function initErrorMetadata() {
	var errors = $("table span.fail, table span.error, table td.fail, table tr.exception, table td.error")
	                    .not(".scenario, .scenario .fail, .scenario .error, .exception .error");

	$("#error-nav-max").text(errors.length);

	function validIndex(index) {
		return index > 0 && index <= errors.length;
	}

	function getCurrentErrorNavIndex() {
		return parseInt($("#error-nav-text").val());
	}

	function setCurrentErrorNavIndex(index) {
		$("#error-nav-text").val(index);
	}

	function incrementErrorNavIndex(offset) {
		var currentErrorNavIndex = getCurrentErrorNavIndex();
		currentErrorNavIndex += offset;
		if (isNaN(currentErrorNavIndex) || currentErrorNavIndex > errors.length) {
			currentErrorNavIndex = 1;
	    } else if (currentErrorNavIndex < 1) {
	        currentErrorNavIndex = errors.length;
		}
		setCurrentErrorNavIndex(currentErrorNavIndex);
		navigateToCurrentError();
	}

    function unfoldErrors(element) {
        element.parents('.scenario-detail').removeClass('closed-detail').prev().removeClass('closed');
        element.parents('.collapsible').removeClass('closed invisible');
        element.parents('tr.hidden').removeClass('hidden');
    }

    var highlight;
	function navigateToCurrentError() {
		var currentErrorNavIndex = getCurrentErrorNavIndex();

        if (highlight) {
             highlight.removeClass("selected-error");
        }

		highlight = $(errors[currentErrorNavIndex - 1]);
        unfoldErrors(highlight);
        highlight.addClass("selected-error");
        $('html, body').animate({
            scrollTop: highlight.offset().top - 200
        }, 500);
        // Add Left-Scroll here
        $('html, body').animate({
            scrollLeft: highlight.offset().left - 200
        }, 500);
	}

	$("#error-nav-prev").click(function () {
		incrementErrorNavIndex(-1);
	});

	$("#error-nav-next").click(function () {
		incrementErrorNavIndex(1);
	});

	$("#error-nav-text").change(function (){
	    incrementErrorNavIndex(0);
	});

    /**
     * Open scenario's and collapsed sections which contain failed or errorous tests
     */
    unfoldErrors($('.fail,.error'));
}

/** Backwards compatibility */
function toggleCollapsable(id) { $('#' + id).toggle().parent('.collapse_rim').toggleClass('open'); }
function expandAll() { $('.collapse_rim').each(function(i, e) { if (!$(e).hasClass('open')) { toggleCollapsable($(e).children().last().attr('id')) } }); }
function collapseAll() { $('.collapse_rim').each(function(i, e) { if ($(e).hasClass('open')) { toggleCollapsable($(e).children().last().attr('id')) } }); }

