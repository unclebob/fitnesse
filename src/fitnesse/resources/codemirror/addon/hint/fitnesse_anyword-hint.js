// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE
// Modified to adjust to FitNesse.org basic needs

(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../codemirror"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../codemirror"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  var WORD = /([@>!$\w]\w*)([^|]*\|)?/, RANGE = 500;
  var autonames;
  var autocompletes;
  
    var pageDataUrl = window.location.pathname + "?names";
      $.ajax({
        url: pageDataUrl,
		async: true,
        cache: true,
        timeout: 2000,
        success: function(result) {
		    autonames = result.split(/\r?\n/);
        },
        error: function() {
          alert("Error Accessing Child Page Names");
        }
      });
	
	
    var pageDataUrl = "WikiAutoComplete" + "?pageData";
      $.ajax({
        url: pageDataUrl,
		async: true,
        cache: true,
        timeout: 2000,
        success: function(result) {
		    autocompletes = result.split(/\r?\n/);
        },
        error: function() {
          alert("Error Accessing Page Content from 'WikiAutoComplete'");
        }
      });
  
  CodeMirror.registerHelper("hint", "fitnesse_anyword", function(editor, options) {
    var word = options && options.word || WORD;
    var range = options && options.range || RANGE;
    var cur = editor.getCursor(), curLine = editor.getLine(cur.line);
    var end = cur.ch, start = end;
    while (start && word.test(curLine.charAt(start - 1))) --start;
    var curWord = start != end && curLine.slice(start, end).toLocaleLowerCase();
	

    var list = options && options.list || [], seen = {};
	function addIfMatch(newWord){
          if ((!curWord || newWord.toLocaleLowerCase().lastIndexOf(curWord, 0) == 0) && !seen.hasOwnProperty(newWord)) {
            seen[newWord] = true;
            list.push(newWord);
          }
	}

	  
    var re = new RegExp(word.source, "g");
    for (var dir = -1; dir <= 1; dir += 2) {
      var line = cur.line, endLine = Math.min(Math.max(line + dir * range, editor.firstLine()), editor.lastLine()) + dir;
      for (; line != endLine; line += dir) {
        var text = editor.getLine(line), m;
        while (m = re.exec(text)) {
			//Don't match myself
			if (line == cur.line && m.index == start) continue;
			addIfMatch(m[0]);
        }
      }
    }

	autonames.forEach(function (item, index, array) {
		addIfMatch(">" + item);
	});
	autocompletes.forEach(function (item, index, array) {
		addIfMatch(item);
	});

    return {list: list, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end)};
  });
});
