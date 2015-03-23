CodeMirror.registerHelper("fold", "fitnesse", function(cm, start) {
  var tablePos = findTable(cm, start);
  if (tablePos)
    return tablePos;
  var blockPos = findBlock(cm, start);
  if (blockPos)
    return blockPos;
  return undefined;
});

function findBlock(cm, start) {
  var maxDepth = 100;
  var firstLine = cm.getLine(start.line);
  if (!cm.getLine(start.line).match(/!\*.*/))
    return undefined;

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

  if (counter > 0)
    return undefined;

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length - 1)
  }
}

function findTable(cm, start) {
  var firstLine = cm.getLine(start.line);
  var previousLineIndex = start.line - 1;
  var tableRowRegexp = /\|.*/;
  if (!cm.getLine(start.line).match(tableRowRegexp) ||
      (cm.getLine(previousLineIndex) && cm.getLine(previousLineIndex).match(tableRowRegexp)))
    return undefined;
  console.log(cm.getLine(previousLineIndex));
  console.log(cm.getLine(previousLineIndex).match(tableRowRegexp));

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
  }
}

CodeMirror.defineSimpleMode("fitnesse", {
  // The start state contains the rules that are intially used
  start: [
    //Bold
    {regex: /'''(?:[^\\]|\\.)*?'''/, token: "variable-2"},
    //Italics
    {regex: /''(?:[^\\]|\\.)*?''/, token: "variable"},
    //Strike
    {regex: /--(?:[^\\]|\\.)*?--/, token: "variable-3"},
    //Strike
    {regex: /!style_/, token: "keyword"},
    //Cross Reference
    {regex: /!see [\.\w]+/, token: "link"},
    // The regex matches the token, the token property contains the type
    {regex: /"(?:[^\\]|\\.)*?"/, token: "string"},
    // You can match multiple tokens at once. Note that the captured
    // groups must span the whole string in this case
    {regex: /(function)(\s+)([a-z$][\w$]*)/,
     token: ["keyword", null, "variable-2"]},
    // Rules are matched in the order in which they appear, so there is
    // no ambiguity between this one and the one above
    {regex: /0x[a-f\d]+|[-+]?(?:\.\d+|\d+\.?\d*)(?:e[-+]?\d+)?/i,
     token: "number"},
    {regex: /#.*/, token: "comment"},
    {regex: /\|.*/, token: "variable-3"},
    {regex: /[-+*\/=<>!]+/, token: "operator"},
    // indent and dedent properties guide autoindentation
    {regex: /[\{\[\(]/, indent: true},
    {regex: /[\}\]\)]/, dedent: true},
    {regex: /[a-z$][\w$]*/, token: "variable"},
    // You can embed other modes with the mode property. This rule
    // causes all code between << and >> to be highlighted with the XML
    // mode.
    {regex: /<</, token: "meta", mode: {spec: "xml", end: />>/}}
  ],
  // The meta property contains global information about the mode. It
  // can contain properties like lineComment, which are supported by
  // all modes, and also directives like dontIndentStates, which are
  // specific to simple modes.
  meta: {
    dontIndentStates: ["comment"],
    lineComment: "#"
  }
});