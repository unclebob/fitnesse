CodeMirror.registerHelper("fold", "fitnesse", function(cm, start) {
  var tablePos = findTable(cm, start);
  if (tablePos) {
    return tablePos;
  }
  var blockPos = findBlock(cm, start);
  if (blockPos) {
    return blockPos;
  }
  var headerPos = findHeader(cm, start);
  if (headerPos) {
    return headerPos;
  }
});

function findBlock(cm, start) {
  var maxDepth = 100;
  var firstLine = cm.getLine(start.line);
  if (!cm.getLine(start.line).match(/!\*.*/)) {
    return;
  }
  var counter = 1;

  var lastLineNo = cm.lastLine();
  var end = start.line, nextLine = cm.getLine(end + 1), nextNextLine = cm.getLine(end + 2);
  while (end < lastLineNo && counter > 0) {
    if (nextLine.match(/!\*.*/))
      counter++;
    if (nextLine.match(/\*+!/))
      counter--;
    ++end;
    nextLine = nextNextLine;
    nextNextLine = cm.getLine(end + 2);
  }

  if (counter > 0) {
    return;
  }

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length - 1)
  };
}

function findTable(cm, start) {
  var firstLine = cm.getLine(start.line);
  var previousLineIndex = start.line - 1;
  var tableRowRegexp = /\|.*/;
  if (!cm.getLine(start.line).match(tableRowRegexp) ||
      (cm.getLine(previousLineIndex) && cm.getLine(previousLineIndex).match(tableRowRegexp))) {
    return;
  }
  var lastLineNo = cm.lastLine();
  var end = start.line, nextLine = cm.getLine(end + 1);
  while (end < lastLineNo) {
    if (!nextLine.match(tableRowRegexp))
      break;
    ++end;
    nextLine = cm.getLine(end + 1);
  }

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length - 1)
  };
}

function findHeader(cm, start) {
  var firstLine = cm.getLine(start.line);
  if (!cm.getLine(start.line).match(/!\d .+/)) {
    return;
  }
  var header = cm.getLine(start.line).substring(0, 2);
  var headerNumber = parseInt(header.substring(1, 2));
  var lastLineNo = cm.lastLine();
  var end = start.line, nextLine = cm.getLine(end + 1);
  while (end < lastLineNo ) {
    if (nextLine.match(/!\d .+/) && (parseInt(nextLine.substring(1, 2)) <= headerNumber)) {
      break;
    }
    ++end;
    nextLine = cm.getLine(end + 1);
  }

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length)
  };
}

CodeMirror.defineSimpleMode("fitnesse", {
  // Here you can find all markup that Fitnesse uses
  start: [
    //Bold
    {regex: /'''(?:[^\\]|\\.)*?'''/, token: "variable-2"},
    //Italics
    {regex: /''(?:[^\\]|\\.)*?''/, token: "variable"},
    //Strike
    {regex: /--(?:[^\\]|\\.)*?--/, token: "variable-3"},
    //Style
    {regex: /!style_/, token: "keyword"},
    //Cross Reference
    {regex: /!see [\.\w]+/, token: "link"},
    //Headers
    {regex: /!\d .+/, token: "header"},
    //Centering
    {regex: /!c .+/, token: "header"},
    //Note
    {regex: /!note .+/, token: "quote"},
    //Image
    {regex: /!img https?:\/\/[\dA-Za-z\.\/\?#-]+/, token: "quote"},
    //External links
    {regex: /https?:\/\/[\dA-Za-z\.\/\?#-]+/, token: "link"},
    //Lists
    {regex: /^\s+[*\d]+ .*/, token: "string"},
    //Variable
    {regex: /!define/, token: "quote"},
    //Classpath
    {regex: /!path .+/, token: "quote"},
    //Table
    {regex: /\|.*/, token: "variable-3"},
    //Hash-Table
    {regex: /!{.*}/, token: "link"},
    //Collapsable Sections
    {regex: /!\*+ .*/, token: "header"},
    {regex: /\*+!/, token: "header"},
    //Table of contents
    {regex: /!contents.*/, token: "keyword"},
    //Include
    {regex: /!include .+/, token: "keyword"},
    //Help
    {regex: /!help (-editable)?/, token: "keyword"},
    //Last modified
    {regex: /!lastmodified/, token: "keyword"},
    //Today
    {regex: /!today/, token: "keyword"},
    {regex: /0x[a-f\d]+|[-+]?(?:\.\d+|\d+\.?\d*)(?:e[-+]?\d+)?/i,
     token: "number"},
     //comment
    {regex: /#.*/, token: "comment"}
  ]
});