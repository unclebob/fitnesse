$(function () {

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

});

