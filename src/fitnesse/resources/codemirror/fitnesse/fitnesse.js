CodeMirror.registerHelper("fold", "fitnesse", function(cm, start) {
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

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length)
  };
});

CodeMirror.defineSimpleMode("fitnesse", {
  // The start state contains the rules that are intially used
  start: [
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