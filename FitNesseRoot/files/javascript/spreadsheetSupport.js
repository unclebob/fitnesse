// Copyright (C) 2004 by Alain Bienvenue. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

function SelectionSpreadsheetToWiki(textArea)
{
	var translator = new SpreadsheetTranslator();
	translator.parseExcelTable(textArea.value);
	textArea.value = translator.getFitNesseTables();
	textArea.focus();
}

function SelectionWikiToSpreadsheet(textArea)
{
	var selection = textArea.value;
	selection = selection.replace(/\r\n/g, '\n');
	selection = selection.replace(/\r/g, '\n');
	selection = selection.replace(/\|\n/g,'\n'); // remove the last | at the end of the line
	selection = selection.replace(/\|/g,'\t'); // replace all remaining | with \t
	textArea.value = selection;
	textArea.focus();
}


title = "This function will convert the text from spreadsheet format to FitNesse format.";
		
document.write("<input type='button' value='Spreadsheet to FitNesse' onClick='SelectionSpreadsheetToWiki(document.f.pageContent)' title='" + title + "'>");
document.write("&nbsp;");

title = "This function will convert the text from FitNesse format to spreadsheet.";
document.write("<input type='button' value='FitNesse to Spreadsheet' onClick='SelectionWikiToSpreadsheet(document.f.pageContent)'>");

