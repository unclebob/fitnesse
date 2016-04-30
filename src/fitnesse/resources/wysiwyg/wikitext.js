"use strict";
/*jslint devel: true, undef: true, browser: true, continue: true, sloppy: true, stupid: true, vars: true, plusplus: true, regexp: true, maxerr: 50, indent: 4 */

(function () {
    // cf. WikiSystem.XML_NAME, http://www.w3.org/TR/REC-xml/#id
    var _xmlName = "[:_A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD](?:[-:_.A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]*[-_A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD])?";
    var _wikiPageName = "(?:\\B[\\.<>]|\\b)[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+(?:\\.[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+)*";
    var _wikiTextLink = "\\[\\[(?:.*?)\\]\\[(?:.*?)\\]\\]";
    var wikiInlineRules = [];
    wikiInlineRules.push("'''''");                  // 1. bolditalic -> badly supported by FitNesse parser
    wikiInlineRules.push("'''");                    // 2. bold
    wikiInlineRules.push("''");                     // 3. italic
    wikiInlineRules.push("--");                     // 4. strike
    wikiInlineRules.push("\\{\\{\\{");              // 5. code block (open)
    wikiInlineRules.push("\\}\\}\\}");              // 6. code block (close)
    wikiInlineRules.push("!\\(");                   // 7. nested (open)
    wikiInlineRules.push("\\)!");                   // 8. nested (close)
    wikiInlineRules.push("![-<(\\[]");              // 9. escaped (open)
    wikiInlineRules.push("[->)\\]]!");              // 10. escaped (close)
    wikiInlineRules.push(_wikiTextLink);			// 11. Wiki link
    wikiInlineRules.push(_wikiPageName);            // 12. WikiPage name
    wikiInlineRules.push("\\${.+?}");               // 13. Variable
    wikiInlineRules.push("!{");                     // 14. Hash table open
    wikiInlineRules.push(":");                      // 15. Hash table key-value separator
    wikiInlineRules.push(",");                      // 16. Hash table entry separator
    wikiInlineRules.push("}");                      // 17. Hash table close

    var wikiRules = [];
    // -1. header
    wikiRules.push("^[ \\t\\r\\f\\v]*![1-6][ \\t\\r\\f\\v]+.*?(?:#" + _xmlName + ")?[ \\t\\r\\f\\v]*$");
    // -2. list
    wikiRules.push("^[ \\t\\r\\f\\v]*[*1-9-][ \\t\\r\\f\\v]");
    // -3. images, e.g. !img -b 10 -w 200 -m 10 http://files/blah.png
    wikiRules.push("!img\\s(?:-[bmw]\\s+\\d+\\s+)*\\S+\\s*");
    // -4. definition and comment
    wikiRules.push("^(?:![a-z]|#)");
    // -5. closing table row
    wikiRules.push("\\|[ \\t\\r\\f\\v]*$");
    // -6. cell
    wikiRules.push("^-?!?\\||\\|");
    // -7: open collapsible section
    wikiRules.push("^!\\*+[<>]?(?:[ \\t\\r\\f\\v]*|[ \\t\\r\\f\\v]+.*)$");
    // -8: close collapsible section
    wikiRules.push("^\\*+!$");

    wikiRules = wikiRules.concat(wikiInlineRules);

    var wikiDetectLinkRules = [ _wikiPageName ];

    var wikiRulesPattern = new RegExp("(?:(" + wikiRules.join(")|(") + "))", "g");
    var wikiDetectLinkPattern = new RegExp("(?:" + wikiDetectLinkRules.join("|") + ")", "g");

    Wysiwyg.prototype._wikiPageName = _wikiPageName;
    Wysiwyg.prototype.wikiInlineRules = wikiInlineRules;
    Wysiwyg.prototype.wikiRules = wikiRules;
    Wysiwyg.prototype.xmlNamePattern = new RegExp("^" + _xmlName + "$");
    Wysiwyg.prototype.wikiRulesPattern = wikiRulesPattern;
    Wysiwyg.prototype.wikiDetectLinkPattern = wikiDetectLinkPattern;
}());

// public
Wysiwyg.prototype.wikitextToFragment = function (wikitext, contentDocument) {
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var wikiInlineRulesCount = this.wikiInlineRules.length;
    var wikiRulesCount = this.wikiRules.length - wikiInlineRulesCount;

    var self = this;
    var fragment = contentDocument.createDocumentFragment();
    var holder = fragment;
    var lines = wikitext.split("\n");
    var currentHeader = null;
    var listDepth = [];
    var decorationStatus;
    var decorationStack;
    var indexLines;
    var inCollapsibleBlock;
    inCollapsibleBlock = false;

    function inParagraph() { return getSelfOrAncestor(holder, "p"); }
    function inDefinition() { return $(holder).parents().andSelf().filter("p.meta, p.comment").get(0); }
    function inTable() { return getSelfOrAncestor(holder, "table"); }
    function inEscapedTable() { return $(holder).parents().andSelf().filter("table.escaped").get(0); }
    function inHashTable() { return $(holder).parents().andSelf().filter("table.hashtable").get(0); }
    function inTableRow() { return getSelfOrAncestor(holder, "tr", "tbody"); }
    function inAnchor() { return getSelfOrAncestor(holder, "a"); }
    function inNestedText() { return getSelfOrAncestor(holder, "div"); }
    function inEscapedText() { return getSelfOrAncestor(holder, "tt"); }
    function inCodeBlock() { return getSelfOrAncestor(holder, "pre"); }

    function handleCodeBlock(value) {
        if (!inCodeBlock()) {
            var element = contentDocument.createElement("pre");
            holder.appendChild(element);
            holder = element;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    function closeCodeBlock(value) {
        if (inCodeBlock()) {
            var target = getSelfOrAncestor(holder, "pre");
            holder = target.parentNode;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    function handleHeader(line) {
        var match = /^\s*!([1-6])[ \t\r\f\v]+/.exec(line);
        if (!match) {
            return null;
        }

        closeToFragment();
        var tag = "h" + match[1];
        var element = contentDocument.createElement(tag);
        holder.appendChild(element);
        holder = element;
        return tag;
    }

    function closeHeader() {
        if (currentHeader) {
            var target = getSelfOrAncestor(holder, currentHeader);
            holder = target.parentNode;
            currentHeader = null;
        }
    }

    function handleDefinition(line) {
        closeToFragment();
        openParagraph();
        holder.appendChild(contentDocument.createTextNode(line));
        self.updateElementClassName(holder);
    }

    function handleCollapsibleBlock(value) {
        inCollapsibleBlock++;
        closeParagraph();
        var collapsible = self.createCollapsibleSection();

        holder.appendChild(collapsible);
        holder = collapsible;

        var m = /^!\*+([<>])?\s+(.*)$/.exec(value);
        if (m) {
            if (m[2]) {
                handleLine(m[2] || "");
                closeParagraph();
            }
            switch (m[1]) {
                case "<": // Hidden
                    $(collapsible).addClass("hidden");
                    break;
                case ">": // Collapsed
                    $(collapsible).addClass("closed");
                    break;
            }
        }
        openParagraph();
    }

    function closeCollapsibleBlock() {
        if (inCollapsibleBlock) {
            inCollapsibleBlock--;
            closeToFragment("div");
            if (holder !== fragment) {
                holder = holder.parentNode;
            }
            // Ensure the user can always edit below the block
            openParagraph();
        }
    }

    function handleOpenInlineCode(name) {
        var d = contentDocument;
        var tagName, element;
        switch (name) {
            case "bold":
                tagName = "b";
                break;
            case "italic":
                tagName = "i";
                break;
            case "strike":
                tagName = "strike";
                break;
        }

        if (holder === fragment) {
            openParagraph();
        }
        element = d.createElement(tagName);
        holder.appendChild(element);
        holder = element;
        decorationStatus[name] = true;
        decorationStack.push(name);
    }

    function handleCloseInlineCode(name) {
        var d = contentDocument;
        var tagNames = [];
        var index;
        for (index = decorationStack.length - 1; index >= 0; index--) {
            var tagName = holder.tagName;
            holder = holder.parentNode;
            if (decorationStack[index] === name) {
                break;
            }
            tagNames.push(tagName);
        }
        decorationStack.splice(index, 1);
        decorationStatus[name] = false;
        while (tagNames.length > 0) {
            var element = d.createElement(tagNames.pop());
            holder.appendChild(element);
            holder = element;
        }
    }

    function handleInline(name) {
        if (name === "bolditalic") {
            if (decorationStatus["italic"]) {
                handleInline("italic");
                handleInline("bold");
            } else {
                handleInline("bold");
                handleInline("italic");
            }
            return;
        }

        if (decorationStatus[name]) {
            handleCloseInlineCode(name);
            return;
        }
        handleOpenInlineCode(name);
    }

    function createAnchor(link, label, autolink) {
        var anchor = self.createAnchor(link, label);
        if (autolink) {
            anchor.setAttribute("data-wysiwyg-autolink", "true");
        }
        holder.appendChild(anchor);
        return anchor;
    }

    function handleLinks(value) {
        var match = handleLinks.pattern.exec(value);

        if (match) {
            var link = match[2];

            var anchor = createAnchor(link);
            //noinspection JSUnusedAssignment
            holder = anchor;
            handleLine(match[1]);
            holder = anchor.parentNode;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    handleLinks.pattern = new RegExp("\\["
        + "\\[(.*)\\]"
        + "\\[(.*)\\]"
        + "\\]");

    function handleWikiPageName(name, label) {
        if (!inAnchor()) {
            createAnchor(name, label || name, true);
        } else {
            holder.appendChild(contentDocument.createTextNode(label || name));
        }
    }

    function handleVariable(value) {
        holder.appendChild(contentDocument.createTextNode(value));
    }

    function handleImage(value) {
        openParagraph();
        var img = contentDocument.createElement("img");
        var args = value.replace(/\s+/, ' ').replace(/ +$/, '').split(' ');
        for (var i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-b":
                    img.style.border = args[++i] + "px";
                    break;
                case "-m":
                    img.style.margin = args[++i] + "px";
                    break;
                case "-w":
                    img.style.width = args[++i] + "px";
                    break;
            }
        }
        var src = args[args.length - 1];
        if (/^http:\/\/files\//.test(src)) {
            src = src.replace(/^http:\//, ".");
        }
        img.setAttribute("src", src);
        holder.appendChild(img)
    }

    function handleList(value) {
        var match = /^(\s*)([*1-9-])\s/.exec(value);
        var className = null, start = null, depth;
        if (!match) {
            holder.appendChild(contentDocument.createTextNode(value));
            return;
        }

        depth = match[1].length;

        var last = listDepth.length - 1;
        if (depth > (last >= 0 ? listDepth[last] : -1)) {
            closeToFragment("li");
            openList(/[1-9]/.test(match[2]) ? "ol" : "ul", className, start, depth);
        } else {
            var container, list, tmp;
            if (listDepth.length > 1 && depth < listDepth[last]) {
                do {
                    if (depth >= listDepth[last]) {
                        break;
                    }
                    closeList();
                    last = listDepth.length - 1;
                } while (listDepth.length > 1);
                container = holder;
            } else {
                list = getSelfOrAncestor(holder, "li");
                self.appendBogusLineBreak(list);
                container = list.parentNode;
            }
            tmp = contentDocument.createElement("li");
            container.appendChild(tmp);
            holder = tmp;
            listDepth[last] = depth;
        }
    }

    function openList(tag, className, start, depth) {
        var d = contentDocument;
        var h = holder;
        var container = d.createElement(tag);

        if (className) {
            container.className = className;
        }
        if (start) {
            container.setAttribute("start", start);
        }
        var list = d.createElement("li");
        container.appendChild(list);

        var target;
        if (h === fragment) {
            target = fragment;
        } else {
            target = getSelfOrAncestor(h, "li");
            target = target ? target.parentNode : h;
        }
        target.appendChild(container);
        holder = list;
        listDepth.push(depth);
    }

    function closeList() {
        var h = holder;
        var target = getSelfOrAncestor(h, "li");
        if (target) {
            self.appendBogusLineBreak(target);
            holder = target.parentNode.parentNode;
        } else {
            holder = h.parentNode;
        }
        listDepth.pop();
    }

    function openParagraph() {
        if (!inParagraph()) {
            var element = contentDocument.createElement("p");
            holder.appendChild(element);
            holder = element;
        }
    }

    function closeParagraph() {
        if (inParagraph()) {
            var target = holder;
            if (target !== fragment) {
                target = getSelfOrAncestor(target, "p");
                self.appendBogusLineBreak(target);
                self.updateElementClassName(target);
                holder = target.parentNode;
            }
        }
    }

    function openNestedText(value) {
        if (!inEscapedText()) {
            var element = contentDocument.createElement("div");
            element.setAttribute('class', 'nested');
            holder.appendChild(element);
            holder = element;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    function closeNestedText(value) {
        if (inNestedText()) {
            // Clean up last generated empty td node
            if (holder.tagName.toLowerCase() === "td" && !Wysiwyg.getTextContent(holder)) {
                var tdNode = holder;
                holder = holder.parentNode;
                holder.removeChild(tdNode);
            }
            var target = holder;
            target = getSelfOrAncestor(target, "div");
            if (target.getAttribute('class') === 'nested') {
                holder = target.parentNode;
                return;
            }
        }
        holder.appendChild(contentDocument.createTextNode(value));
    }

    function openEscapedText(value) {
        if (!inEscapedText()) {
            var element = contentDocument.createElement("tt");
            element.setAttribute('class', { '!-': 'escape', '!<': 'htmlescape', '![': 'plaintexttable' }[value]);
            holder.appendChild(element);
            holder = element;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    function closeEscapedText(value) {
        if (inEscapedText()) {
            var target = holder;
            target = getSelfOrAncestor(target, "tt");
            if (target.getAttribute('class') === { '-!': 'escape', '>!': 'htmlescape', ']!': 'plaintexttable' }[value]) {
                holder = target.parentNode;
                return;
            }
        }
        holder.appendChild(contentDocument.createTextNode(value));
    }

    function handleTableCell(action, tableClassName, rowClassName) {
        // action: 1 = create, 2 = add cell to current row, -1 = close row
        var d = contentDocument;
        var h, table, tbody, cell;

        if (!inTable() || tableClassName === "hashtable") {
            h = holder;

            // Just ensure you can type between to tables
            if (h.lastChild && h.lastChild.tagName === 'TABLE') {
                h.appendChild(d.createElement('p'));
            }
            table = d.createElement("table");
            if (tableClassName) {
                table.className = tableClassName;
            }
            tbody = d.createElement("tbody");
            table.appendChild(tbody);
            h.appendChild(table);
        } else {
            h = holder;
            tbody = getSelfOrAncestor(h, "tbody");
        }

        if (inTableRow()) {
            cell = getSelfOrAncestor(h, "td");
            if (cell) {
                self.appendBogusLineBreak(cell);
            }
        }

        var row;
        switch (action) {
            case 2:
                return;
            case 1:
                row = d.createElement("tr");
                tbody.appendChild(row);
                if (rowClassName) {
                    row.className = rowClassName;
                }
                break;
            case 0:
                row = getSelfOrAncestor(h, "tr");
                break;
            case -1:
                if (inTableRow()) {
                    var target = getSelfOrAncestor(h, "tr");
                    holder = target.parentNode;
                }
                return;
        }

        cell = d.createElement("td");
        row.appendChild(cell);
        holder = cell;
        decorationStatus = {};
    }

    function closeTable() {
        if (inTable() || inHashTable()) {
            var target = getSelfOrAncestor(holder, "table");
            holder = target.parentNode;
        }
    }

    function closeToFragment(stopTag) {
        // Note: we're not exceeding collapsible section boundries
        var element = holder;
        var _fragment = fragment;
        stopTag = stopTag ? stopTag.toLowerCase() : "div";

        while (element !== _fragment) {
            var tag = element.tagName.toLowerCase();
            var method = null;
            switch (tag) {
                case stopTag:
                case "div":
                    holder = element;
                    return;
                case "p":
                    method = closeParagraph;
                    break;
                case "li":
                case "ul":
                case "ol":
                    method = closeList;
                    break;
                case "td":
                case "tr":
                case "tbody":
                case "table":
                    method = closeTable;
                    break;
                case "pre":
                    method = closeCodeBlock;
                    break;
                default:
                    break;
            }
            if (method) {
                method();
                element = holder;
            } else {
                element = element.parentNode;
            }
        }

        holder = _fragment;
    }

    function getMatchNumber(match) {
        var length = match.length;
        var i;
        for (i = 1; i < length; i++) {
            if (match[i]) {
                if (i <= wikiRulesCount) {
                    return -i;
                }
                return i - wikiRulesCount;
            }
        }
        return null;
    }

    function handleLine(line) {
        var wikiRulesPattern = new RegExp(self.wikiRulesPattern.source, "g");
        var prevIndex = wikiRulesPattern.lastIndex;
        wikiRulesPattern.lastIndex = 0;
        while (true) {
            var match = wikiRulesPattern.exec(line);
            var matchNumber = null;
            var text = null;

            // Deal with unmatched text
            if (match) {
                matchNumber = getMatchNumber(match);
                if (prevIndex < match.index) {
                    text = line.substring(prevIndex, match.index);
                }
            } else {
                text = line.substring(prevIndex);
            }

            if (((prevIndex === 0 && text) || (match && match.index === 0 && matchNumber > 0))
                && !inParagraph() && !inAnchor() && !inHashTable() && !inEscapedText() && !inCodeBlock() && !currentHeader) {
                closeToFragment();
            }


            if (text || (match && matchNumber > 0)) {
                if (inParagraph() && (prevIndex === 0)) {
                    if (text && holder.hasChildNodes() && holder.lastChild.tagName !== 'BR') {
                        holder.appendChild(contentDocument.createElement("br"));
                    }
                }
                if ((listDepth.length === 0 && !inTable() && !currentHeader) || holder === fragment) {
                    openParagraph();
                }
                if (text) {
                    holder.appendChild(contentDocument.createTextNode(text));
                }
            }

            if (!match) {
                break;
            }
            prevIndex = wikiRulesPattern.lastIndex;
            var matchText = match[0];

            switch (matchNumber) {
                case 1:     // bolditalic
                    if (inEscapedTable() || inEscapedText()) { break; }
                    handleInline("bolditalic");
                    continue;
                case 2:     // bold
                    if (inEscapedTable() || inEscapedText()) { break; }
                    handleInline("bold");
                    continue;
                case 3:     // italic
                    if (inEscapedTable() || inEscapedText()) { break; }
                    handleInline("italic");
                    continue;
                case 4:     // strike
                    if (inEscapedTable() || inEscapedText()) { break; }
                    handleInline("strike");
                    continue;
                case 5:     // open code block
                    if (inEscapedTable() || inEscapedText()) { break; }
                    handleCodeBlock(matchText);
                    continue;
                case 6:     // close code block
                    if (inEscapedTable() || inEscapedText()) { break; }
                    closeCodeBlock(matchText);
                    continue;
                case 7:     // open nested
                    if (inEscapedText()) { break; }
                    openNestedText(matchText);
                    continue;
                case 8:     // close nested
                    closeNestedText(matchText);
                    continue;
                case 9:     // open escaped
                    if (inEscapedText()) { break; }
                    openEscapedText(matchText);
                    continue;
                case 10:     // close escaped
                    closeEscapedText(matchText);
                    continue;
                case 11:    // Wiki link
                    if (inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    handleLinks(matchText);
                    continue;
                case 12:	// WikiPage name
                    if (inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    handleWikiPageName(matchText);
                    continue;
                case 13:    // Variable
                    handleVariable(matchText);
                    continue;
                case 14:    // Hash table open
                    if (inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    handleTableCell(1, "hashtable");
                    continue;
                case 15:    // Hash table key-value separator ':'
                    if (!inHashTable() || inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    handleTableCell(0);
                    continue;
                case 16:    // Hash table entry separator ','
                    if (!inHashTable() || inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    handleTableCell(1);
                    continue;
                case 17:    // Hash table close
                    if (!inHashTable() || inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                    closeTable();
                    continue;
                case -1:    // header
                    currentHeader = handleHeader(matchText);
                    if (currentHeader) {
                        var m = /^\s*!([1-6])[ \t\r\f\v]+/.exec(line);
                        wikiRulesPattern.lastIndex = prevIndex = m[0].length;
                        continue;
                    }
                    break;
                case -2:    // list
                    if (inEscapedText() || inCodeBlock()) { break; }
                    handleList(matchText);
                    continue;
                case -3: // images
                    if (inEscapedText() || inCodeBlock()) { break; }
                    handleImage(matchText);
                    continue;
                case -4:    // definition (leading "!") and comments (leading "#")
                    if (inEscapedText() || inCodeBlock()) { break; }
                    handleDefinition(matchText);
                    continue;
                case -5:    // closing table row
                    if (inEscapedText() || inCodeBlock()) { break; }
                    if (inTable()) {
                        handleTableCell(-1);
                        continue;
                    }
                    break;
                case -6:    // cell
                    if (inDefinition()) { break; }
                    if (inEscapedText() || inCodeBlock()) {
                        if (/^-!/.test(matchText)) {
                            closeEscapedText(matchText.substring(0, 2));
                            matchText = matchText.substring(2);
                            if (inTable()) {
                                handleTableCell(-1);
                                continue;
                            }
                        }
                        break;
                    }
                    if (!inTable() && match.index === 0) {
                        closeToFragment();
                    }
                    wikiRulesPattern.lastIndex = prevIndex;
                    handleTableCell(inTableRow() ? 0 : 1, /^-?!/.test(matchText) ? "escaped" : null, /^-/.test(matchText) ? "hidden" : null);
                    continue;
                case -7:    // collapsible section
                    if (inEscapedText()) { break; }
                    handleCollapsibleBlock(matchText);
                    continue;
                case -8:    // close collapsible section
                    if (inEscapedText()) { break; }
                    closeCollapsibleBlock();
                    continue;
            }

            if (matchText) {
                if (listDepth.length === 0 && !currentHeader && !inTable() && !inAnchor()) {
                    openParagraph();
                }
                holder.appendChild(contentDocument.createTextNode(matchText));
            }
        }

        if (inHashTable() || inEscapedText() || inCodeBlock()) {
            var element = contentDocument.createElement("br");
            holder.appendChild(element);
        } else if (inParagraph()) {
            self.updateElementClassName(holder);
            if (inDefinition()) {
                closeParagraph();
            }
        }
    }

    for (indexLines = 0; indexLines < lines.length; indexLines++) {
        var line = lines[indexLines].replace(/\r$/, "");
        if (/^----/.test(line)) {
            closeToFragment();
            fragment.appendChild(contentDocument.createElement("hr"));
            continue;
        }
        if (line.length === 0 && !inCodeBlock()) {
            closeToFragment();
            continue;
        }
        line = line.replace(/\t/g, "        ");
        line = line.replace(/\u00a0/g, " ");

        decorationStatus = {};
        decorationStack = [];

        handleLine(line);

        // Close headers here, since they should not interfere with other line types parsed.
        if (currentHeader) {
            closeHeader();
        }
        if (inTable() && !inEscapedText() && !inHashTable() && !inCodeBlock()) {
            handleTableCell(-1);
        }

    }
    closeToFragment();

    $("table", fragment).each(function(i, t) {
        self.spanTableColumns(t);
    });
    return fragment;
};

Wysiwyg.prototype.wikiOpenTokens = {
    "h1": "!1 ",
    "h2": "!2 ",
    "h3": "!3 ",
    "h4": "!4 ",
    "h5": "!5 ",
    "h6": "!6 ",
    "b": "'''",
    "strong": "'''",
    "i": "''",
    "em": "''",
    "del": "--",
    "strike": "--",
    "hr": "----\n",
    "table": true,
    "tbody": true
};

Wysiwyg.prototype.wikiCloseTokens = {
    "#text": true,
    "a": true,
    "tt": true,
    "b": "'''",
    "strong": "'''",
    "i": "''",
    "em": "''",
    "del": "--",
    "strike": "--",
    "hr": true,
    "tbody": true,
    "tr": "|\n",
    "td": true,
    "th": true
};

Wysiwyg.prototype.wikiBlockTags = {
    "h1": true,
    "h2": true,
    "h3": true,
    "h4": true,
    "h5": true,
    "h6": true,
    "table": true,
    "hr": true
};

Wysiwyg.prototype.wikiInlineTags = {
    "a": true,
    "tt": true,
    "b": true,
    "strong": true,
    "i": true,
    "em": true,
    "u": true,
    "del": true,
    "strike": true,
    "sub": true,
    "sup": true,
    "br": true,
    "span": true,
    "img": true
};

// public
Wysiwyg.prototype.domToWikitext = function (root, options) {
    options = options || {};
    var retainNewLines = !!options.retainNewLines;
    var escapeNewLines = !!options.escapeNewLines;

    var self = this;
    var getTextContent = Wysiwyg.getTextContent;
    var wikiOpenTokens = this.wikiOpenTokens;
    var wikiCloseTokens = this.wikiCloseTokens;
    var wikiBlockTags = this.wikiBlockTags;
    var xmlNamePattern = this.xmlNamePattern;
    var wikiPageNamePattern = new RegExp("^" + this._wikiPageName + "$");
    var decorationTokenPattern = /^(?:'''|'')$/;

    var texts = [];
    var stack = [];
    var last = root;
    var listDepth = 0;
    var inCodeBlock = false;
    var tableType;
    var firstHashTableEntry = true;
    var skipNode = null;

    function tokenFromSpan(node) {
        var style = node.style;
        if (style.fontWeight === "bold") {
            return wikiOpenTokens.b;
        }
        if (style.fontStyle === "italic") {
            return wikiOpenTokens.i;
        }
        if (style.textDecoration === "line-through") {
            return wikiOpenTokens.del;
        }
        return undefined;
    }

    function isInlineNode (node) {
        if (node) {
            switch (node.nodeType) {
                case 1:
                    return self.wikiInlineTags.hasOwnProperty(node.tagName.toLowerCase());
                case 3:
                    return true;
            }
        }
        return false;
    }

    function nodeDecorations(node) {
        var _wikiOpenTokens = wikiOpenTokens;
        var _decorationTokenPattern = decorationTokenPattern;
        var hash = {};

        while (true) {
            var childNodes = node.childNodes;
            if (!childNodes || childNodes.length !== 1) {
                break;
            }
            var child = childNodes[0];
            if (child.nodeType !== 1) {
                break;
            }
            var token = _wikiOpenTokens[child.tagName.toLowerCase()];
            if (_decorationTokenPattern.test(token)) {
                hash[token] = true;
            }
            node = child;
        }

        return hash;
    }

    function pushTextWithDecorations(text, node) {
        var _texts = texts;
        var _decorationTokenPattern = decorationTokenPattern;
        var decorationsHash = nodeDecorations(node);
        var decorations = [];
        var cancelDecorations = [];
        var token;

        while (_texts.length > 0) {
            token = _texts[_texts.length - 1];
            if (_decorationTokenPattern.test(token)) {
                if (decorationsHash[token]) {
                    delete decorationsHash[token];
                    cancelDecorations.push(_texts.pop());
                    continue;
                }
                if ((token === "'''" || token === "''") && _texts.length > 1) {
                    var moreToken = _texts[_texts.length - 2];
                    if (_decorationTokenPattern.test(moreToken)
                        && token + moreToken === "'''''"
                        && decorationsHash[moreToken]) {
                        delete decorationsHash[moreToken];
                        cancelDecorations.push(moreToken);
                        _texts[_texts.length - 2] = _texts[_texts.length - 1];
                        _texts.pop();
                    }
                }
            }
            break;
        }

        for (token in decorationsHash) {
            if (decorationsHash.hasOwnProperty(token)) {
                decorations.push(token);
            }
        }
        decorations.sort();

        if (decorations.length > 0) {
            _texts.push.apply(_texts, decorations);
        }
        _texts.push(text);

        if (decorations.length > 0) {
            decorations.reverse();
            _texts.push.apply(_texts, decorations);
        }
        if (cancelDecorations.length > 0) {
            cancelDecorations.reverse();
            _texts.push.apply(_texts, cancelDecorations);
        }
    }

    function pushToken(token) {
        var _texts = texts;
        var _decorationTokenPattern = decorationTokenPattern;
        var length = _texts.length;
        if (length === 0 || !_decorationTokenPattern.test(token)) {
            _texts.push(token);
            return;
        }
        var last = _texts[length - 1];
        if (!_decorationTokenPattern.test(last)) {
            _texts.push(token);
            return;
        }
        if (last === token) {
            _texts.pop();
            return;
        }
        if (length < 2 || last + token !== "'''''") {
            _texts.push(token);
            return;
        }
        if (_texts[length - 2] === token) {
            _texts[length - 2] = _texts[length - 1];
            _texts.pop();
        } else {
            _texts.push(token);
        }
    }

    function linkText(link, label) {
        if (link === label) {
            return link;
        }
        if (!/]/.test(label) && !/^["']/.test(label)) {
            return "[[" + label + "][" + link + "]]";
        }
        return "[[" + label.replace(/"+/g, "") + "][" + link + "]]";
    }

    function pushAnchor(node) {
        var _texts = texts;
        var link = node.getAttribute("data-wysiwyg-link");
        var autolink = node.getAttribute("data-wysiwyg-autolink");

        link = (link || node.href).replace(/^\s+|\s+$/g, "");
        var label = self.domToWikitext(node, options);
        if (!label) {
            return;
        }
        var text = null;
        if (autolink === "true") {
            if (wikiPageNamePattern.test(label)) {
                text = label;
                link = label;
            }
        }
        if (text === null) {
            text = linkText(link, label);
        }
        _texts.push(text);
    }

    function string(source, times) {
        var value = (1 << times) - 1;
        return (value <= 0) ? "" : value.toString(2).replace(/1/g, source);
    }

    function open(name, node) {
        if (skipNode !== null) {
            return;
        }
        var _texts = texts;
        var token = wikiOpenTokens[name];
        if (token !== undefined) {
            if (wikiBlockTags[name] && isInlineNode(node.previousSibling) && !self.isHashTable(node)) {
                _texts.push("\n");
            }
            if (token !== true) {
                pushToken(token);
            }
            // TODO: can move to switch statement?
            if (name === "table") {
                if ($(node).hasClass("hashtable")) {
                    tableType = "hashtable";
                    _texts.push("!{");
                    firstHashTableEntry = true;
                } else {
                    tableType = "table";
                    if ($('tr', node).first().hasClass('hidden')) {
                        _texts.push("-");
                    }
                    if ($(node).hasClass("escaped")) {
                        _texts.push("!");
                    }
                }
            }
        } else {
            var value, text;
            switch (name) {
                case "#text":
                    value = node.nodeValue;
                    if (value) {
                        if (!(inCodeBlock || retainNewLines)) {
                            if (value && !isInlineNode(node.previousSibling || node.parentNode)) {
                                value = value.replace(/^[ \t\r\n\f\v]+/g, "");
                            }
                            if (value && !isInlineNode(node.nextSibling || node.parentNode)) {
                                value = value.replace(/[ \t\r\n\f\v]+$/g, "");
                            }
                            value = value.replace(/\r?\n/g, " ");
                        }
                        if (value) {
                            _texts.push(value);
                        }
                    }
                    break;
                case "p":
                    if (!/[^ \t\r\n\f\v]/.test(getTextContent(node)) && $(node).find('img').length === 0) {
                        skipNode = node;
                    }
                    break;
                case "a":
                    skipNode = node;
                    pushAnchor(node);
                    break;
                case "li":
                    _texts.push(" " + string("  ", listDepth - 1));
                    var container = node.parentNode;
                    if ((container.tagName || "").toLowerCase() === "ol") {
                        _texts.push("1 ");
                    } else {
                        _texts.push("* ");
                    }
                    break;
                case "ul":
                case "ol":
                    if (listDepth === 0) {
                        if (isInlineNode(node.previousSibling)) {
                            _texts.push("\n");
                        }
                    } else if (listDepth > 0) {
                        if (node.parentNode.tagName.toLowerCase() === "li") {
                            _texts.push("\n");
                        }
                    }
                    listDepth++;
                    break;
                case "br":
                    if (inCodeBlock || retainNewLines) {
                        _texts.push("\n");
                    } else if (escapeNewLines && node.nextSibling) {
                        _texts.push("!-\n-!");
                    } else if (!self.isBogusLineBreak(node)) {
                        _texts.push("\n");
                    }
                    break;
                case "pre":
                    _texts.push("{{{");
                    inCodeBlock = true;
                    break;
                case "th":
                case "td":
                    skipNode = node;
                    _texts.push("|");
                    text = self.domToWikitext(node, $.extend(options, { escapeNewLines: true})).replace(/^ +| +$/g, "").replace(/\n$/, "");
                    if (text) {
                        _texts.push(" ", text, " ");
                        break;
                    } else {
                        _texts.push(" ");
                    }
                    break;
                case "tr":
                    if (tableType === "hashtable") {
                        var cells = $(node).find('td');
                        skipNode = node;

                        if (cells.length >= 2) {
                            var hashKey = self.domToWikitext(cells[0], $.extend(options, { escapeNewLines: true})).replace(/^ +| +$/g, "").replace(/\n$/, "");
                            var hashValue = self.domToWikitext(cells[1], $.extend(options, { escapeNewLines: true})).replace(/^ +| +$/g, "").replace(/\n$/, "");
                            if (hashKey && hashValue) {
                                if (!firstHashTableEntry) {
                                    _texts.push(",");
                                }
                                _texts.push(hashKey);
                                _texts.push(":");
                                _texts.push(hashValue);
                                firstHashTableEntry = false;
                            }
                        }
                    }
                    break;
                case "tt":
                    skipNode = node;
                    value = node.innerHTML.replace(/<br>/g, '\n').replace(/&gt;/g, ">").replace(/&lt;/g, "<");
                    if (value) {
                        var tags = {
                            'escape': [ "!-", "-!" ],
                            'htmlescape': [ "!<", ">!" ],
                            'nested': [ "!(", ")!" ],
                            'plaintexttable': [ "![", "]!" ],
                            'inlinecode': [ "{{{", "}}}" ]
                        }[node.className || "escape"];
                        if (!tags) {
                            console.log('No tags', node.innerHTML, node.className);
                        }
                        text = tags[0] + value + tags[1];
                        pushTextWithDecorations(text, node);
                    }
                    break;
                case "span":
                    if (node.className === "wikianchor" && xmlNamePattern.test(node.id || "")) {
                        skipNode = node;
                        text = self.domToWikitext(node, options).replace(/^ +| +$|\]/g, "");
                        _texts.push("[=#", node.id, text ? " " + text + "]" : "]");
                    } else {
                        token = tokenFromSpan(node);
                        if (token !== undefined) {
                            pushToken(token);
                        }
                    }
                    break;
                case "div":
                    if ($(node).hasClass("nested")) {
                        _texts.push("!(");
                    } else if ($(node).hasClass("collapsible")) {
                        _texts.push("!***");
                        if ($(node).hasClass("closed")) {
                            _texts.push("> ");
                        } else if ($(node).hasClass("hidden")) {
                            _texts.push("< ");
                        } else {
                            _texts.push(" ");
                        }
                    }
                    break;
                case "img":
                    var border = node.style.border,
                        margin = node.style.margin,
                        width = node.style.width,
                        src = node.getAttribute("src");
                    _texts.push("!img ");
                    if (border) _texts.push("-b " + border.replace(/px$/, "") + " ");
                    if (margin) _texts.push("-m " + margin.replace(/px$/, "") + " ");
                    if (width) _texts.push("-w " + width.replace(/px$/, "") + " ");
                    if (/^\.\//.test(src)) src = src.replace(/^\.\//, "http://");
                    _texts.push(src);
                    _texts.push(" ");
                    break;
                case "script":
                case "style":
                    skipNode = node;
                    break;
            }
        }
    }

    function close(name, node) {
        if (skipNode !== null) {
            if (skipNode === node) {
                skipNode = null;
            }
            return;
        }
        var _texts = texts;
        var token = wikiCloseTokens[name];
        if (token === true) {
            // nothing to do
        } else if (token !== undefined) {
            pushToken(token);
        } else {
            switch (name) {
                case "p":
                    if ($(node).hasClass('meta') || retainNewLines) {
                        _texts.push("\n");
                    } else {
                        _texts.push("\n\n");
                    }
                    break;
                case "li":
                    if (node.getElementsByTagName("li").length === 0) {
                        _texts.push("\n");
                    }
                    break;
                case "ul":
                case "ol":
                    listDepth--;
                    if (listDepth === 0) {
                        _texts.push("\n");
                    }
                    break;
                case "pre":
                    var text;
                    var parentNode = node.parentNode;
                    if (parentNode && /^(?:li|dd)$/i.test(parentNode.tagName)) {
                        var nextSibling = node.nextSibling;
                        if (!nextSibling) {
                            text = "\n}}}";
                        } else if (nextSibling.nodeType !== 1) {
                            text = "\n}}}";
                        } else if (nextSibling.tagName.toLowerCase() === "pre") {
                            text = "}}}";
                        } else {
                            text = "\n}}}";
                        }
                        if (text.slice(-1) === "\n") {
                            text += listDepth > 0 ? " " + string("  ", listDepth) : "    ";
                        }
                    } else {
                        text = "}}}";
                    }
                    _texts.push(text);
                    inCodeBlock = false;
                    break;
                case "span":
                    token = tokenFromSpan(node);
                    if (token !== undefined) {
                        _texts.push(token);
                    }
                    break;
                case "div":
                    if ($(node).hasClass("nested")) {
                        _texts.push(")!");
                    } else if ($(node).hasClass("collapsible")) {
                        _texts.push("*!\n");
                    }
                    break;
                case "table":
                    if ($(node).hasClass("hashtable")) {
                        _texts.push("}");
                    } else {
                        _texts.push("\n");
                    }
                    break;
            }
        }
        if (/^h[1-6]$/.test(name)) {
            if (xmlNamePattern.test(node.id || "")) {
                _texts.push(" #", node.id);
            }
            _texts.push("\n");
        }
    }

    function iterator(node) {
        var name = null;
        switch (node && node.nodeType) {
            case 1: // element
                name = node.tagName.toLowerCase();
                break;
            case 3: // text
                name = "#text";
                break;
        }

        if (node && last === node.parentNode) {
            // down
            // nothing to do
        } else if (node && last === node.previousSibling) {
            // forward
            close(stack.pop(), last);
        } else {
            // up, forward
            var tmp = last;
            var nodeParent = node ? node.parentNode : root;
            while (true) {
                var parent = tmp.parentNode;
                if (parent === node) {
                    break;
                }
                close(stack.pop(), tmp);
                if (parent === nodeParent || !parent) {
                    if (!node) {
                        return;
                    }
                    break;
                }
                tmp = parent;
            }
        }
        open(name, node);
        stack.push(name);
        last = node;
    }

    this.treeWalk(root, iterator);
    var wikiText = texts.join("").replace(/^(?: *\n)+/, "").replace(/(?: *\n)+$/, "\n").replace(/\n+\)!/, ")!");
    if (window.WikiFormatter && Wysiwyg.getAutoformat()) {
        return new window.WikiFormatter().format(wikiText);
    } else {
        return wikiText;
    }
};

// public
Wysiwyg.prototype.updateElementClassName = function (element) {
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var p = getSelfOrAncestor(element, "p");
    if (p) {
        if (/^![a-z]/.test(p.innerHTML)) {
            if (p.className !== 'meta') {
                p.className = 'meta';
            }
        } else if (/^#/.test(p.innerHTML)) {
            if (p.className !== 'comment') {
                p.className = 'comment';
            }
        } else {
            $(p).removeClass();
        }
        if ($(p.parentNode).hasClass('collapsible')) {
            var collapsible = p.parentNode;
            $(collapsible.childNodes).removeClass('title');
            $(collapsible.firstChild).addClass('title');
        }
    }
};

Wysiwyg.prototype.appendBogusLineBreak = function (element) {
    if (window.getSelection) {
        var wikiInlineTags = this.wikiInlineTags;
        var last = element.lastChild;
        var br = this.contentDocument.createElement("br");
        while (true) {
            if (!last) {
                break;
            }
            if (last.nodeType !== 1) {
                return;
            }
            var name = last.tagName.toLowerCase();
            if (name === "br") {
                break;
            }
            if (!wikiInlineTags[name]) {
                return;
            }
            last = last.lastChild || last.previousSibling;
        }
        element.appendChild(br);
    }
};

Wysiwyg.prototype.isBogusLineBreak =
    (function () {
        var blocks = {
            p: true,
            blockquote: true,
            div: true,
            li: true,
            ul: true,
            ol: true,
            dl: true,
            dt: true,
            dd: true,
            h1: true,
            h2: true,
            h3: true,
            h4: true,
            h5: true,
            h6: true,
            table: true,
            thead: true,
            tbody: true,
            tr: true,
            td: true,
            th: true
        };

        return function (node) {
            if (!node) {
                return false;
            }
            while (!node["nextSibling"]) {
                node = node.parentNode;
                if (!node) {
                    return true;
                }
                if (node.nodeType === 1 && blocks[node.tagName.toLowerCase()]) {
                    return true;
                }
            }
            return false;
        };
    }());