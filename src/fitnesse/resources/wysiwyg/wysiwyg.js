"use strict";
/*jslint devel: true, undef: true, browser: true, continue: true, sloppy: true, stupid: true, vars: true, plusplus: true, regexp: true, maxerr: 50, indent: 4 */
/****
 vim:sw=4:et:ai

 Wysiwyg editor for FitNesse, based on the Trac Wysiwyg editor written by
 OpenGroove and Ciklone, BSD licensed.

 ****/

var Wysiwyg = function (textarea, options) {
    var self = this;
    var editorMode = Wysiwyg.getEditorMode();

    this.textarea = textarea;
    this.options = options || {};

    this.frame = this.createEditable(document, textarea);

    this.contentWindow = window;
    this.contentDocument = this.contentWindow.document;

    //this.initializeEditor(this.contentDocument);
    this.wysiwygToolbar = this.createWysiwygToolbar(document);
    this.textareaToolbar = this.createTextareaToolbar(document);
    this.styleMenu = this.createStyleMenu(document);
    this.menus = [ this.styleMenu ];
    this.toolbarButtons = this.setupWysiwygMenuEvents();
    this.setupTextareaMenuEvents();
    
    this.toggleEditorButtons = null;
    this.savedWysiwygHTML = null;

    this.setupToggleEditorButtons();

    // Hide both editors, so the current one gets properly shown:
    textarea.style.display = this.frame.style.display = "none";

    this.textarea.parentNode.insertBefore(this.toggleEditorButtons, this.textarea);
    this.textarea.parentNode.insertBefore(this.textareaToolbar, this.textarea);
    this.textarea.parentNode.insertBefore(this.wysiwygToolbar, this.textarea);

    document.getElementById("wt-style").parentNode.appendChild(this.menus[0]);


    this.listenerToggleEditor(editorMode)({ initializing: true });

    // disable firefox table resizing
    try { this.contentDocument.execCommand("enableObjectResizing", false, false); } catch (e) {}
    try { this.contentDocument.execCommand("enableInlineTableEditing", false, false); } catch (e) {}

    var exception;
    try { self.execCommand("useCSS", false); } catch (e1) { }
    try { self.execCommand("styleWithCSS", false); } catch (e2) { }
    if (editorMode === "wysiwyg") {
        try { self.loadWysiwygDocument(); } catch (e3) { exception = e3; }
    }
    self.setupEditorEvents();
    self.setupFormEvent();
    if (exception) {
        self.textarea.style.display = self.textareaToolbar.style.display = "";
        self.frame.style.display = self.wysiwygToolbar.style.display = "none";
        alert("Failed to activate the wysiwyg editor.");
        throw exception;
    }
};

Wysiwyg.getBooleanFromCookie = function(fieldName, defaultValue) {
    var result = defaultValue;

    var cookies = (document.cookie || "").split(";");
    var length = cookies.length;
    var i;
    for (i = 0; i < length; i++) {
        var regex = new RegExp("^\\s*" + fieldName + "=(\\S*)");
        var match = regex.exec(cookies[i]);
        if (match) {
            switch (match[1]) {
            case "true":
                result = true;
                break;
            default:
                result = false;
                break;
            }
            break;
        }
    }

    return result;
};

Wysiwyg.getWrapOn = function () {
    return Wysiwyg.getBooleanFromCookie('textwrapon', false)
};

Wysiwyg.getAutoformat = function () {
    return Wysiwyg.getBooleanFromCookie('textautoformat', false)
};

Wysiwyg.prototype.listenerToggleEditor = function (type) {
    var self = this;
    var setEditorMode = function (mode) {
        switch (mode) {
        case "wysiwyg":
            break;
        default:
            mode = "textarea";
            break;
        }
        Wysiwyg.editorMode = mode;
        Wysiwyg.setCookie("wysiwyg", mode);
    };


    switch (type) {
    case "textarea":
        return function (event) {
            var textarea = self.textarea;
            if (textarea.style.display === "none") {
                self.hideAllMenus();
                if (event && !event.initializing) { self.loadWikiText(); }
                textarea.style.display = "";
                textarea.setAttribute("tabIndex", "");
                self.syncTextAreaHeight();
                self.frame.style.display = self.wysiwygToolbar.style.display = "none";
                self.frame.setAttribute("tabIndex", "-1");
                self.textareaToolbar.style.display = "";
                setEditorMode(type);
            }
            self.focusTextarea();
        };
    case "wysiwyg":
        return function (event) {
            var frame = self.frame;
            if (frame.style.display === "none") {
                try {
                    self.loadWysiwygDocument();
                } catch (e) {
                    Wysiwyg.stopEvent(event || window.event);
                    alert("Failed to activate the wysiwyg editor.");
                    throw e;
                }
                self.textarea.style.display = "none";
                self.textarea.setAttribute("tabIndex", "-1");
                frame.style.display = self.wysiwygToolbar.style.display = "";
                frame.setAttribute("tabIndex", "");
                self.textareaToolbar.style.display = "none";
                setEditorMode(type);
            }
            self.focusWysiwyg();
        };
    }
};

Wysiwyg.prototype.activeEditor = function () {
    return this.textarea.style.display === "none" ? "wysiwyg" : "textarea";
};

Wysiwyg.prototype.isModified = function () {
    return this.savedWysiwygHTML !== null && this.frame.innerHTML !== this.savedWysiwygHTML;
};

Wysiwyg.prototype.setupFormEvent = function () {
    var self = this;

    $(this.textarea.form).submit(function (event) {
        try {
            if (self.activeEditor() === "wysiwyg") {
                var body = self.frame;
                if (self.isModified()) {
                    self.textarea.value = self.domToWikitext(body, self.options);
                }
            }
            if (Wysiwyg.getAutoformat()) {
                var formatter = new WikiFormatter();
                self.textarea.value = formatter.format(self.textarea.value);
            }
        } catch (e) {
            Wysiwyg.stopEvent(event);
        }
    });
};

Wysiwyg.prototype.createEditable = function (d, textarea) {
    var frame = d.createElement("div");
    frame.setAttribute("class", "wysiwyg");
    frame.setAttribute("contenteditable", "true");

    textarea.parentNode.insertBefore(frame, textarea.nextSibling);
    return frame;
};

Wysiwyg.prototype.createWysiwygToolbar = function (d) {
    var html = [
        '<ul id="wm-style">',
        '<li class="wysiwyg-menu-style" title="Style">',
        '<a id="wt-style" href="#">',
        '<span class="wysiwyg-menu-style">Style</span>',
        '<span class="wysiwyg-menu-paragraph">Normal</span>',
        '<span class="wysiwyg-menu-heading1">Header 1</span>',
        '<span class="wysiwyg-menu-heading2">Header 2</span>',
        '<span class="wysiwyg-menu-heading3">Header 3</span>',
        '<span class="wysiwyg-menu-heading4">Header 4</span>',
        '<span class="wysiwyg-menu-heading5">Header 5</span>',
        '<span class="wysiwyg-menu-heading6">Header 6</span>',
        '<span class="wysiwyg-menu-code">Code block</span>',
        '</a></li>',
        '</ul>',
        '<ul>',
        '<li title="Bold (Ctrl+B)"><a id="wt-strong" href="#"></a></li>',
        '<li title="Italic (Ctrl+I)"><a id="wt-em" href="#"></a></li>',
        '<li title="Strike through"><a id="wt-strike" href="#"></a></li>',
        '<li title="Escape"><a id="wt-escape" href="#"></a></li>',
        '<li title="Remove format"><a id="wt-remove" href="#"></a></li>',
        '</ul>',
        '<ul>',
        '<li title="Link"><a id="wt-link" href="#"></a></li>',
        '<li title="Unlink"><a id="wt-unlink" href="#"></a></li>',
        '</ul>',
        '<ul class="non-table">',
        '<li title="List"><a id="wt-ul" href="#"></a></li>',
        '<li title="Indent"><a id="wt-indent" href="#"></a></li>',
        '<li title="Outdent"><a id="wt-outdent" href="#"></a></li>',
        '<li title="Horizontal rule"><a id="wt-hr" href="#"></a></li>',
        '<li title="Table"><a id="wt-table" href="#"></a></li>',
        '</ul>',
        '<ul class="non-table">',
        '<li title="Collapsible section (default closed)"><a id="wt-collapsible-closed" href="#"></a></li>',
        '<li title="Collapsible section (default open)"><a id="wt-collapsible-open" href="#"></a></li>',
        '<li title="Collapsible section (hidden)"><a id="wt-collapsible-hidden" href="#"></a></li>',
        '<li title="Remove collapsible section"><a id="wt-remove-collapsible" href="#"></a></li>',
        '</ul>',
        '<ul class="in-table">',
        '<li title="Insert cell before"><a id="wt-insert-cell-before" href="#"></a></li>',
        '<li title="Insert cell after (|)"><a id="wt-insert-cell-after" href="#"></a></li>',
        '<li title="Insert row above"><a id="wt-insert-row-before" href="#"></a></li>',
        '<li title="Insert row below"><a id="wt-insert-row-after" href="#"></a></li>',
        '<li title="Insert column before"><a id="wt-insert-col-before" href="#"></a></li>',
        '<li title="Insert column after"><a id="wt-insert-col-after" href="#"></a></li>',
        '<li title="Delete cell (Ctrl-|)"><a id="wt-delete-cell" href="#"></a></li>',
        '<li title="Delete row"><a id="wt-delete-row" href="#"></a></li>',
        '<li title="Delete column"><a id="wt-delete-col" href="#"></a></li>',
        '<li title="Delete table"><a id="wt-remove-table" href="#"></a></li>',
        '</ul>' ];
    var div = d.createElement("div");
    div.className = "wysiwyg-toolbar";
    div.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return div;
};

Wysiwyg.prototype.createStyleMenu = function (d) {
    var html = [
        '<p><a id="wt-paragraph" href="#">Normal</a></p>',
        '<h1><a id="wt-heading1" href="#">Header 1</a></h1>',
        '<h2><a id="wt-heading2" href="#">Header 2</a></h2>',
        '<h3><a id="wt-heading3" href="#">Header 3</a></h3>',
        '<h4><a id="wt-heading4" href="#">Header 4</a></h4>',
        '<h5><a id="wt-heading5" href="#">Header 5</a></h5>',
        '<h6><a id="wt-heading6" href="#">Header 6</a></h6>',
        '<pre class="wiki"><a id="wt-code" href="#">Code block</a></pre>' ];
    var menu = d.createElement("div");
    menu.className = "wysiwyg-menu";
    Wysiwyg.setStyle(menu, { display: "none" });
    menu.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return menu;
};

Wysiwyg.prototype.setupWysiwygMenuEvents = function () {
    function addToolbarEvent(element, self, args) {
        var method = args.shift();
        $(element).click(function (event) {
            Wysiwyg.stopEvent(event);
            var keepMenus = false, exception;
            try { keepMenus = method.apply(self, args); } catch (e) { exception = e; }
            if (!keepMenus) {
                self.hideAllMenus();
            }
            element.blur();
            self.frame.focus();
            if (exception) {
                throw exception;
            }
        });
    }

    function argsByType(self, name, element) {
		switch (name) {
		case "style":
			return [ self.toggleMenu, self.styleMenu, element ];
		case "strong":
			return [ self.execDecorate, "bold" ];
		case "em":
			return [ self.execDecorate, "italic" ];
		case "underline":
			return [ self.execDecorate, "underline" ];
		case "strike":
			return [ self.execDecorate, "strikethrough" ];
		case "sub":
			return [ self.execDecorate, "subscript" ];
		case "sup":
			return [ self.execDecorate, "superscript" ];
		case "escape":
			return [ self.execDecorate, "escape" ];
		case "remove":
			return [ self.execCommand, "removeformat" ];
		case "paragraph":
			return [ self.formatParagraph ];
		case "heading1":
			return [ self.formatHeaderBlock, "h1" ];
		case "heading2":
			return [ self.formatHeaderBlock, "h2" ];
		case "heading3":
			return [ self.formatHeaderBlock, "h3" ];
		case "heading4":
			return [ self.formatHeaderBlock, "h4" ];
		case "heading5":
			return [ self.formatHeaderBlock, "h5" ];
		case "heading6":
			return [ self.formatHeaderBlock, "h6" ];
		case "link":
			return [ self.createLink ];
		case "unlink":
			return [ self.execCommand, "unlink" ];
        case "ol":
            return [ self.insertOrderedList ];
		case "ul":
			return [ self.insertUnorderedList ];
		case "outdent":
			return [ self.outdent ];
		case "indent":
			return [ self.indent ];
		case "table":
			return [ self.insertTable ];
		case "insert-cell-before":
			return [ self.ui_insertTableCell, false ];
		case "insert-cell-after":
			return [ self.ui_insertTableCell, true ];
		case "insert-row-before":
			return [ self.insertTableRow, false ];
		case "insert-row-after":
			return [ self.insertTableRow, true ];
		case "insert-col-before":
			return [ self.insertTableColumn, false ];
		case "insert-col-after":
			return [ self.insertTableColumn, true ];
		case "delete-cell":
			return [ self.deleteTableCell ];
		case "delete-row":
			return [ self.deleteTableRow ];
		case "delete-col":
			return [ self.deleteTableColumn ];
		case "remove-table":
			return [ self.deleteTable ];
		case "code":
			return [ self.formatCodeBlock ];
		case "hr":
			return [ self.insertHorizontalRule ];
		case "br":
			return [ self.insertLineBreak ];
		case "collapsible-closed":
			return [ self.insertCollapsibleSection, "closed" ];
		case "collapsible-open":
			return [ self.insertCollapsibleSection ];
		case "collapsible-hidden":
			return [ self.insertCollapsibleSection, "hidden" ];
		case "remove-collapsible":
			return [ self.deleteCollapsibleSection ];
		}
		return null;
	}

    function setup(container) {
        var elements = container.getElementsByTagName("a");
        var length = elements.length;
        var i;
        for (i = 0; i < length; i++) {
            var element = elements[i];
            var name = element.id.replace(/^wt-/, "");
            var args = argsByType(this, name, element);
            if (args) {
                addToolbarEvent(element, this, args);
                buttons[name] = element;
            }
        }
    }

    var buttons = {}, i;
    setup.call(this, this.wysiwygToolbar);
    for (i = 0; i < this.menus.length; i++) {
        setup.call(this, this.menus[i]);
    }
    return buttons;
};

Wysiwyg.prototype.createTextareaToolbar = function (d) {
    var html = [
        '<input id="tt-spreadsheet-to-wiki" type="button" value="Spreadsheet to FitNesse" title="This function will convert the text from spreadsheet format to FitNesse format." />',
        '<input id="tt-wiki-to-spreadsheet" type="button" value="FitNesse to Spreadsheet" title="This function will convert the text from FitNesse format to spreadsheet." />',
        '<input id="tt-format-wiki" type="button" accesskey="f" value="Format" title="Formats the wiki text" />',
        '<select id="tt-template-map">' + $('#templateMap').html() + '</select>',
        '<input id="tt-insert-template" type="button" value="Insert Template" title="Inserts the selected template" />',
        '<label title="Turns on/off wrapping"><input type="checkbox" id="tt-wrap-text" />wrap</label>',
        '<label title="Automatically format wiki text on save"><input type="checkbox" id="tt-autoformat" />autoformat</label>'];
    var div = d.createElement("div");
    div.className = "textarea-toolbar";
    div.innerHTML = html.join(" ");
    return div;
};

Wysiwyg.prototype.setupTextareaMenuEvents = function () {
    var textarea = this.textarea;
    var container = this.textareaToolbar;
    
    $('#tt-spreadsheet-to-wiki', container).click(function () {
        var translator = new SpreadsheetTranslator();
        translator.parseExcelTable(textarea.value);
        textarea.value = translator.getFitNesseTables();
        textarea.focus();
    });
    $('#tt-wiki-to-spreadsheet', container).click(function () {
        var selection = textarea.value;
        selection = selection.replace(/\r\n/g, '\n');
        selection = selection.replace(/\r/g, '\n');
         // remove the last | at the end of the line
        selection = selection.replace(/\|\n/g, '\n');
         // replace all remaining | with \t
        selection = selection.replace(/\|/g, '\t');
        textarea.value = selection;
        textarea.focus();
    });

    $('#tt-format-wiki', container).click(function () {    
        var formatter = new WikiFormatter();
        textarea.value = formatter.format(textarea.value);
        textarea.focus();
    });
    
    $('#tt-insert-template', container).click(function () {
        var selectedValue = $('#tt-template-map').val();
        var inserter = new TemplateInserter();
        inserter.insertInto(selectedValue, textarea);
        textarea.focus();
    });
    
    function setWrap(wrap) {
        if (textarea.wrap) {
            textarea.wrap = wrap ? 'soft' : 'off';
        } else { // wrap attribute not supported - try Mozilla workaround
            textarea.setAttribute('wrap', wrap ? 'soft' : 'off');
        }
        if (wrap) {
            $(textarea).removeClass('no_wrap');
            Wysiwyg.setCookie("textwrapon", "true");
        } else {
            $(textarea).addClass('no_wrap');
            Wysiwyg.setCookie("textwrapon", "false");
        }
    }

    function setAutoformat(autoformat) {
        if (autoformat) {
            Wysiwyg.setCookie("textautoformat", "true");
        } else {
            Wysiwyg.setCookie("textautoformat", "false");
        }
    }

    $('#tt-wrap-text', container)
        .change(function () {
            setWrap($(this).is(':checked'));
        })
        .prop('checked', Wysiwyg.getWrapOn())
        .change();
    $('#tt-autoformat', container)
        .change(function () {
            setAutoformat($(this).is(':checked'));
        })
        .prop('checked', Wysiwyg.getAutoformat())
        .change();
};

Wysiwyg.prototype.toggleMenu = function (menu) {
    if (menu.style.display === "none") {
        this.hideAllMenus(menu);
        Wysiwyg.setStyle(menu, { display: "" });
    } else {
        this.hideAllMenus();
    }
    return true;
};

Wysiwyg.prototype.hideAllMenus = function (except) {
    var menus = this.menus;
    var length = menus.length;
    var i;
    for (i = 0; i < length; i++) {
        if (menus[i] !== except) {
            Wysiwyg.setStyle(menus[i], { display: "none" });
        }
    }
};

Wysiwyg.prototype.execDecorate = function (name) {
    if (this.selectionContainsTagName("pre")) {
        return;
    }
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var position = this.getSelectionPosition();
    var ancestor = {};
    ancestor.start = getSelfOrAncestor(position.start, /^(?:a|tt)$/);
    ancestor.end = getSelfOrAncestor(position.end, /^(?:a|tt)$/);
    this.expandSelectionToElement(ancestor);

    if (name !== "escape") {
        this.execCommand(name);
    } else {
        this.execDecorateMonospace();
    }
    this.selectionChanged();
};

Wysiwyg.prototype.execDecorateMonospace = function () {
    var html = this.getSelectionHTML();
    if (/^<tt.*?>/i.test(html) && /<\/tt>$/i.test(html)) {
        html = html.replace(/<tt.*?>|<\/tt>/gi, "");
    } else {
        html = '<tt class="escape">' + html.replace(/<[a-z]+.*?>|<\/[a-z]+>/gi, "") + "</tt>";
    }
    this.insertHTML(html);
    var node = this.contentDocument.getElementById(id);
    if (node) {
        this.selectNode(node);
    }
};

Wysiwyg.prototype.execCommand = function (name, arg) {
    return this.contentDocument.execCommand(name, false, arg);
};

Wysiwyg.prototype.setupEditorEvents = function () {
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var self = this;
    var frame = this.frame;
    var ime = false;
    var inPasteAction = false;

    $(frame).keydown(function (event) {
        var method = null;
        var args = null;
        var keyCode = event.keyCode;
        switch (keyCode) {
        case 0x09:  // TAB
            var range = self.getSelectionRange();
            var stop = false;
            var element = getSelfOrAncestor(range.startContainer, /^(?:li|pre|table)$/);
            if (element) {
                switch (element.tagName.toLowerCase()) {
                case "li":
                    self.execCommand(event.shiftKey ? "outdent" : "indent");
                    self.selectionChanged();
                    stop = true;
                    break;
                case "pre":
                    self.insertHTML("\t");
                    stop = true;
                    break;
                case "table":
                    if (getSelfOrAncestor(range.endContainer, "table") === element) {
                        self.moveFocusInTable(!event.shiftKey);
                        self.selectionChanged();
                        stop = true;
                    }
                    break;
                }
            }
            if (stop) {
                Wysiwyg.stopEvent(event);
            }
            return;
        case 0xe5:
            ime = true;
            break;
        }
        switch ((keyCode & 0x00fffff) | (event.ctrlKey ? 0x40000000 : 0)
            | (event.shiftKey ? 0x20000000 : 0) | (event.altKey ? 0x10000000 : 0)) {
        case 0x40000042:  // C-b
            method = self.execDecorate;
            args = [ "bold" ];
            break;
        case 0x40000049:  // C-i
            method = self.execDecorate;
            args = [ "italic" ];
            break;
        case 0x40000055:  // C-u
            method = self.execDecorate;
            args = [ "underline" ];
            break;
        case 0x40000059:  // C-y
            method = self.execCommand;
            args = [ "redo" ];
            break;
        case 0x4000005a:  // C-z
            method = self.execCommand;
            args = [ "undo" ];
            break;
        }
        if (method !== null) {
            Wysiwyg.stopEvent(event);
            method.apply(self, args);
            self.selectionChanged();
        } else if (keyCode && !inPasteAction) {
            var focus = self.getFocusNode();
            if (!getSelfOrAncestor(focus, /^(?:p|li|h[1-6]|t[dh]|d[td]|pre|blockquote)$/)) {
                self.execCommand("formatblock", "<p>");
            }
        }
    });

    //noinspection JSUnresolvedFunction
    $(frame).keypress(function (event) {
        var modifier = (event.ctrlKey ? 0x40000000 : 0)
            | (event.shiftKey ? 0x20000000 : 0) | (event.altKey ? 0x10000000 : 0);
        switch (event.charCode || event.keyCode) {
        case 0x20:  // SPACE
            self.detectLink(event);
            return;
        case 0x3e:  // ">"
            self.detectLink(event);
            return;
        case 0x0d:  // ENTER
            self.detectLink(event);
            switch (modifier) {
            case 0:
                var focus = self._getFocusForTable();
                if (focus.table && focus.cell) {
                    self.insertTableRow(true);
                    Wysiwyg.stopEvent(event);
                } else if (self.insertParagraphOnEnter) {
                    self.insertParagraphOnEnter(event);
                }
                break;
            case 0x20000000:    // Shift
                if (self.insertLineBreakOnShiftEnter) {
                    self.insertLineBreakOnShiftEnter(event);
                }
                break;
            }
            return;
        case 0x7c: // "|"
        case 28: // ctrl-"|"
            var range = self.getSelectionRange();
            var element = getSelfOrAncestor(range.startContainer, "table");
            if (element &&
                    getSelfOrAncestor(range.endContainer, "table") === element &&
                    !getSelfOrAncestor(range.endContainer, /^(?:tt)/)) {
                if (event.ctrlKey) {
                    self.deleteTableCell();
                } else {
                    self.ui_insertTableCell(true);
                }
                Wysiwyg.stopEvent(event);
            }
            return;
        }
    });

    //noinspection JSUnresolvedFunction
    $(frame).keyup(function (event) {
        var keyCode = event.keyCode;
        if (ime) {
            switch (keyCode) {
            case 0x20:  // SPACE
                self.detectLink(event);
                break;
            }
            ime = false;
        }
        if (self.getSelectionRange()) {
            self.updateElementClassName(self.getSelectionRange().startContainer);
        }
        self.selectionChanged();
    });

    //noinspection JSUnresolvedFunction
    $(frame).mouseup(function () {
        self.selectionChanged();
    });

    $(frame).click(function () {
        self.hideAllMenus();
        self.selectionChanged();
    });

    $(frame).on('touchstart click', 'div.collapsible > p.title', function (event) {
        var x = event.pageX - $(this).offset().left;
        if (x < parseInt($(this).css('padding-left'))) {
            var e = $(this.parentNode);
            if (e.hasClass('closed')) {
                e.removeClass('closed').addClass('hidden');
            } else if (e.hasClass('hidden')) {
                e.removeClass('hidden');
            } else {
                e.addClass('closed');
            }
        }
        // Avoid default open/close handling from being called
        event.stopPropagation();
    });

    $(frame).on('click', 'a', function () {
        return false;
    });

    /* Pasting data is forced in a specific div: pasteddata. In there, the
     * raw data is collected and fed to the wikiToDom parser.
     */
    $(frame).on('paste', 'body', function () {
        function tagNode(node) { while (node.nodeType === 3 /* TextNode */) { node = node.parentNode; } return node; }

        // Clone state is change in Firefox, need to extract the fields
        var range = self.getSelectionRange();

        var position = {
            startContainer: range.startContainer,
            endContainer: range.startContainer === range.endContainer ? range.startContainer.nextSibling : range.endContainer,
            sameContainer: range.startContainer === range.endContainer,
            atStart: range.startOffset === 0,
            atEnd: range.endOffset === range.endContainer.length
        };
        inPasteAction = true;

        // Move tables up to the table they're pasted in
        function flattenTable(td) {
            var nestedRows = $('tr', td);
            if (nestedRows.length) {
                var parentTr = getSelfOrAncestor(td, 'tr');
                nestedRows.each(function(j, elem) {
                    $(parentTr).after(elem);
                    parentTr = $(parentTr).next();
                });
                self.spanTableColumns(getSelfOrAncestor(parentTr, 'table'));
            }

        }
        // Post processing:
        setTimeout(function () {
            inPasteAction = false;

            var parentTd = getSelfOrAncestor(position.startContainer, 'td');
            var wikiText, fragment;

            if (parentTd) {
                flattenTable(parentTd);

                // Make a one-liner for the content pasted in the table cell
                wikiText = self.domToWikitext(parentTd, self.options);
                fragment = self.wikitextToOnelinerFragment(wikiText.replace('\n', ' '), self.contentDocument, self.options);
                while (parentTd.firstChild) { parentTd.removeChild(parentTd.firstChild); }
                parentTd.appendChild(fragment);

                flattenTable(parentTd);

            } else if (position.sameContainer) {
                var c = tagNode(position.startContainer);
                wikiText = self.domToWikitext(c, { retainNewLines: true });
                fragment = self.wikitextToFragment(wikiText, self.contentDocument);
                c.parentNode.insertBefore(fragment, c);
                c.parentNode.removeChild(c);
                // NOTE: At this point the position object is invalid/not useful.
            }
        }, 20);
    });
};

Wysiwyg.prototype.loadWysiwygDocument = function () {
    var container = this.frame;
    if (!container) { return; }
    var tmp = container.lastChild;

    while (tmp) {
        container.removeChild(tmp);
        tmp = container.lastChild;
    }
    var fragment = this.wikitextToFragment(this.textarea.value, this.contentDocument, this.options);
    container.appendChild(fragment);
    this.savedWysiwygHTML = container.innerHTML;
};

Wysiwyg.prototype.focusWysiwyg = function () {
    var self = this;
    function lazy() {
        self.frame.focus();
        try { self.execCommand("useCSS", false); } catch (e1) { }
        try { self.execCommand("styleWithCSS", false); } catch (e2) { }
        self.selectionChanged();
        $(window).resize();
    }
    setTimeout(lazy, 10);
};

Wysiwyg.prototype.loadWikiText = function () {
    this.textarea.value = this.domToWikitext(this.frame, this.options);
    this.savedWysiwygHTML = null;
};

Wysiwyg.prototype.focusTextarea = function () {
    this.textarea.focus();
    $(window).resize();
};

Wysiwyg.prototype.setupToggleEditorButtons = function () {
    var div = document.createElement("div");
    var mode = Wysiwyg.editorMode;
    var html = '<label for="editor-wysiwyg-@">'
        + '<input type="radio" name="__EDITOR__@" value="wysiwyg" id="editor-wysiwyg-@" '
        + (mode === "wysiwyg" ? 'checked="checked"' : '') + ' />'
        + 'rich text</label> '
        + '<label for="editor-textarea-@">'
        + '<input type="radio" name="__EDITOR__@" value="textarea" id="editor-textarea-@" '
        + (mode === "textarea" ? 'checked="checked"' : '') + ' />'
        + 'plain text</label> '
        + '&nbsp; ';
    var buttons;
    var i;

    div.className = "editor-toggle";
    //noinspection JSCheckFunctionSignatures,JSCheckFunctionSignatures
    div.innerHTML = html.replace(/@/g, ++Wysiwyg.count);
    this.toggleEditorButtons = div;

    buttons = div.getElementsByTagName("input");
    for (i = 0; i < buttons.length; i++) {
        var button = buttons[i];
        var token = button.id.replace(/[0-9]+$/, "@");
        switch (token) {
        case "editor-wysiwyg-@":
        case "editor-textarea-@":
            $(button).click(this.listenerToggleEditor(button.value));
            break;
        }
    }
};

Wysiwyg.prototype.syncTextAreaHeight = function () {
    var height = this.textarea.offsetHeight;
    var frame = this.frame;
    if (height > 0 && frame.height !== height) {
        frame.height = height;
    }
};

Wysiwyg.prototype.detectLink = function (event) {
    var range = this.getSelectionRange();
    var node = range.startContainer;
    if (!node || !range.collapsed) {
        return;
    }
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    if (getSelfOrAncestor(node, /^(?:a|tt|pre)$/)) {
        return;
    }

    var offset = range.startOffset;
    if (node.nodeType !== 3) {
        node = node.childNodes[offset];
        while (node && node.nodeType !== 3) {
            node = node.lastChild;
        }
        if (!node) {
            return;
        }
        offset = node.nodeValue.length;
    } else if (offset === 0) {
        node = node.previousSibling;
        if (!node || node.nodeType === 1) {
            return;
        }
        offset = node.nodeValue.length;
    }
    //noinspection JSUnusedAssignment
    var startContainer = node;
    var endContainer = node;
    var text = [ node.nodeValue.substring(0, offset) ];
    while (true) {
        if (/[ \t\r\n\f\v]/.test(text[text.length - 1])) {
            break;
        }
        node = node.previousSibling;
        if (!node || node.nodeType === 1) {
            break;
        }
        text.push(node.nodeValue);
        startContainer = node;
    }
    text.reverse();
    var linkText = text.join("");
    if (!linkText) {
        return;
    }

    var pattern = this.wikiDetectLinkPattern;
    pattern.lastIndex = /[^ \t\r\n\f\v]*$/.exec(linkText).index;
    var match, tmp;
    for (tmp = pattern.exec(linkText); tmp; tmp = pattern.exec(linkText)) {
        match = tmp;
    }
    if (!match) {
        return;
    }

    var label = match[0];
    var link = this.normalizeLink(label);
    var id = this.generateDomId();
    var anchor = this.createAnchor(link, label, { id: id, "data-wysiwyg-autolink": "true" });
    var anonymous = this.contentDocument.createElement("div");
    anonymous.appendChild(anchor);
    var html = anonymous.innerHTML;

    node = endContainer;
    var startOffset = match.index;
    while (startContainer !== node && startOffset >= startContainer.nodeValue.length) {
        startOffset -= startContainer.nodeValue.length;
        startContainer = startContainer.nextSibling;
    }
    var endOffset = startOffset + label.length;
    endContainer = startContainer;
    while (endContainer !== node && endOffset >= endContainer.nodeValue.length) {
        endOffset -= endContainer.nodeValue.length;
        endContainer = endContainer.nextSibling;
    }
    this.selectRange(startContainer, startOffset, endContainer, endOffset);

    offset = linkText.length - match.index - label.length;
    if (offset === 0) {
        switch (event.keyCode) {
        case 0x20:  // SPACE
            this.insertHTML(html + "\u00a0");
            Wysiwyg.stopEvent(event);
            return;
        case 0x0d:  // ENTER
            if (event.shiftKey) {
                if (window.opera || !anonymous.addEventListener) {
                    this.insertHTML(html + "<br/>");
                    if (window.opera) {
                        anchor = this.contentDocument.getElementById(id);
                        node = anchor.parentNode;
                        offset = node.childNodes.length;
                        this.selectRange(node, offset, node, offset);
                    }
                    Wysiwyg.stopEvent(event);
                    return;
                }
            }
            this.insertHTML(html);
            anchor = this.contentDocument.getElementById(id);
            node = event.shiftKey ? anchor.parentNode : anchor;
            offset = node.childNodes.length;
            this.selectRange(node, offset, node, offset);
            return;
        }
    }
    this.insertHTML(html);
    anchor = this.contentDocument.getElementById(id);
    node = anchor.nextSibling;
    if (!node) {
        node = anchor.parentNode;
        offset = node.childNodes.length;
    }
    this.selectRange(node, offset, node, offset);
};

Wysiwyg.prototype.outdent = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("outdent");
};

Wysiwyg.prototype.indent = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("indent");
};

Wysiwyg.prototype.formatParagraph = function () {
    if (this.selectionContainsTagName("table")) {
        return;
    }
    this.execCommand("formatblock", "<p>");
    this.selectionChanged();
};

Wysiwyg.prototype.formatHeaderBlock = function (name) {
    if (this.selectionContainsTagName("table")) {
        return;
    }
    this.execCommand("formatblock", "<" + name + ">");
    this.selectionChanged();
};

Wysiwyg.prototype.insertOrderedList = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("insertorderedlist");
    this.selectionChanged();
};

Wysiwyg.prototype.insertUnorderedList = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("insertunorderedlist");
    this.selectionChanged();
};

Wysiwyg.prototype.insertTable = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    var id = this.generateDomId();
    this.insertHTML(this.tableHTML(id, 2, 2));
    var element = this.contentDocument.getElementById(id);
    if (element) {
        this.selectNodeContents(element);
    }
    this.selectionChanged();
};

Wysiwyg.prototype._tableHTML = function (row, col) {
    var tr = "<tr>" + ((1 << col) - 1).toString(2).replace(/1/g, "<td></td>") + "</tr>";
    var html = [
        '<table class="wiki">', '<tbody>',
        ((1 << row) - 1).toString(2).replace(/1/g, tr),
        '</tbody>', '</table>' ];
    return html.join("");
};

Wysiwyg.prototype._getFocusForTable = function () {
    var hash = { node: null, cell: null, row: null, table: null };
    hash.node = this.getFocusNode();
    hash.cell = hash.node ? Wysiwyg.getSelfOrAncestor(hash.node, /^t[dh]$/) : null;
    hash.row = hash.cell ? Wysiwyg.getSelfOrAncestor(hash.cell, "tr") : null;
    hash.table = hash.row ? Wysiwyg.getSelfOrAncestor(hash.row, "table") : null;

    return hash;
};

Wysiwyg.prototype.ui_insertTableCell = function (after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var row = focus.table.rows[focus.row.rowIndex];
        var cellIndex = focus.cell.cellIndex + (after ? 1 : 0);
        $(focus.cell).removeAttr('colspan');
        var cell = this.insertTableCell(row, Math.min(cellIndex, row.cells.length));
        this.spanTableColumns(focus.table);
        this.selectNodeContents(cell);
        this.selectionChanged();
        cell.focus();
    }
};

Wysiwyg.prototype.insertTableRow = function (after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.row) {
        var cells = focus.row.getElementsByTagName("td");
        var row = focus.table.insertRow(focus.row.rowIndex + (after ? 1 : 0));
        var cell;
        var j;
        for (j = 0; j < cells.length; j++) {
            cell = this.insertTableCell(row, 0);
        }
        this.spanTableColumns(focus.table);
        this.selectNodeContents(cell);
        this.selectionChanged();
        cell.focus();
        return row;
    }
};

Wysiwyg.prototype.insertTableColumn = function (after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var rows = focus.table.rows;
        var length = rows.length;
        var cellIndex = focus.cell.cellIndex + (after ? 1 : 0);
        var i;
        for (i = 1; i < length; i++) {
            var row = rows[i];
            this.insertTableCell(row, Math.min(cellIndex, row.cells.length));
        }
        this.spanTableColumns(focus.table);
    }
};

Wysiwyg.prototype.deleteTableCell = function () {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var row = focus.table.rows[focus.row.rowIndex];
        var cellIndex = focus.cell.cellIndex;
        if (cellIndex < row.cells.length) {
            row.deleteCell(cellIndex);
        }
        this.spanTableColumns(focus.table);
        //this.selectNode(row.cells[cellIndex < row.cells.length ? cellIndex : row.cells.length - 1].firstChild);
        this.moveFocusInTable(false);
    }
};

Wysiwyg.prototype.deleteTableRow = function () {
    var focus = this._getFocusForTable();
    if (focus.table && focus.row) {
        focus.table.deleteRow(focus.row.rowIndex);
        this.spanTableColumns(focus.table);
    }
};

Wysiwyg.prototype.deleteTableColumn = function () {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var rows = focus.table.rows;
        var length = rows.length;
        var cellIndex = focus.cell.cellIndex;
        var i;
        for (i = 1; i < length; i++) {
            var row = rows[i];
            if (cellIndex < row.cells.length) {
                row.deleteCell(cellIndex);
            }
        }
        this.spanTableColumns(focus.table);
    }
};

Wysiwyg.prototype.deleteTable = function () {
    var focus = this._getFocusForTable();
    if (focus.table) {
    	$(focus.table).remove();
    }
};

Wysiwyg.prototype.spanTableColumns = function (table) {
    // Spanning columns fitnesse style.
    var maxCells = Math.max.apply(Math, $.map($('tr', table), function (e) {
        var tds = $('td', e);
        tds.removeAttr('colspan');
        return tds.size();
    }));
    $('tr', table).each(function () {
        var s = $('td', this).size();
        if (s < maxCells) {
            $('td:last', this).attr('colspan', maxCells - s + 1);
        }
    });
};

Wysiwyg.prototype.moveFocusInTable = function (forward) {
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var focus = this.getFocusNode();
    var element = getSelfOrAncestor(focus, /^(?:t[dhr]|table)$/);
    var target, table, rows, cells;
    switch (element.tagName.toLowerCase()) {
    case "td":
    case "th":
        focus = element;
        var row = getSelfOrAncestor(element, "tr");
        cells = row.cells;
        if (forward) {
            if (focus.cellIndex + 1 < cells.length) {
                target = cells[focus.cellIndex + 1];
            } else {
                table = getSelfOrAncestor(row, /^(?:tbody|table)$/);
                rows = table.rows;
                target = row.rowIndex + 1 < rows.length ? rows[row.rowIndex + 1].cells[0] : null;
            }
        } else {
            if (focus.cellIndex > 0) {
                target = cells[focus.cellIndex - 1];
            } else {
                table = getSelfOrAncestor(row, /^(?:tbody|table)$/);
                rows = table.rows;
                if (row.rowIndex > 0) {
                    cells = rows[row.rowIndex - 1].cells;
                    target = cells[cells.length - 1];
                } else {
                    target = null;
                }
            }
        }
        break;
    case "tr":
        cells = element.cells;
        target = cells[forward ? 0 : cells.length - 1];
        break;
    case "tbody":
    case "table":
        rows = element.rows;
        cells = rows[forward ? 0 : rows.length - 1].cells;
        target = cells[forward ? 0 : cells.length - 1];
        break;
    }
    if (target) {
        this.selectNodeContents(target);
    } else if (table) {
        table = getSelfOrAncestor(table, "table");
        var parent = table.parentNode;
        var elements = parent.childNodes;
        var length = elements.length;
        var offset;
        for (offset = 0; offset < length; offset++) {
            if (table === elements[offset]) {
                if (forward) {
                    offset++;
                }
                this.selectRange(parent, offset, parent, offset);
            }
        }
    }
};

Wysiwyg.prototype.formatCodeBlock = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    if (!this.getSelectionText()) {
        var node = this.getFocusNode();
        while (node.nodeType === 3) {
            node = node.parentNode;
        }
        this.selectNode(node);
    }

    var fragment = this.getSelectionFragment();
    var text = this.domToWikitext(fragment).replace(/\s+$/, "");

    var d = this.contentDocument;
    var anonymous = d.createElement("div");
    var pre = d.createElement("pre");
    pre.className = "wiki";
    anonymous.appendChild(pre);
    if (text) {
        pre.appendChild(d.createTextNode(text));
    }

    this.insertHTML(anonymous.innerHTML);
    this.selectionChanged();
};

Wysiwyg.prototype.insertHorizontalRule = function () {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    if (!this.execCommand("inserthorizontalrule")) {
        this.insertHTML("<hr />");
    }
    this.selectionChanged();
};

Wysiwyg.prototype.insertCollapsibleSection = function (mode) {
    var self = this;
    var range = this.getSelectionRange();
    var d = this.contentDocument;
    
    function topNode(node) {
        while (node.parentNode && node.parentNode !== self.frame) {
            node = node.parentNode;
        }
        return node;
    }

    var nodes, node;
    var start = topNode(range.startContainer);
    if (range.startContainer === range.endContainer) {
        nodes =  range.startOffset === range.endOffset ? [] : [ topNode(range.startContainer) ];
    } else {
        var end = topNode(range.endContainer);
        nodes = [];
        for (node = start; node !== end; node = node.nextSibling) {
            nodes.push(node);
        }
        nodes.push(end);
    }
    
    var classes = (mode) ? " " + mode : "";
    var collapsible = d.createElement("div");
    collapsible.setAttribute("class", "collapsible" + classes);
    start.parentNode.insertBefore(collapsible, start);
    var sectionName = d.createElement("p");
    sectionName.appendChild(d.createTextNode("section title"));
    collapsible.appendChild(sectionName);
    for (node in nodes) {
        //noinspection JSUnfilteredForInLoop
        collapsible.appendChild(nodes[node]);
    }
    
    this.selectNode(sectionName);
};

Wysiwyg.prototype.deleteCollapsibleSection = function () {
    var pos = this.getSelectionPosition();
    var startCol = $(pos.start).parents("div.collapsible")[0];
    var endCol = $(pos.end).parents("div.collapsible")[0];
    if (startCol === endCol) {
        $(startCol).before($(startCol).children());
        $(startCol).remove();
    }
};

Wysiwyg.prototype.createLink = function () {
    if (this.selectionContainsTagName("pre")) {
        return;
    }

    var focus = this.getFocusNode();
    var anchor = Wysiwyg.getSelfOrAncestor(focus, "a");
    var expand = anchor || Wysiwyg.getSelfOrAncestor(focus, "tt");
    var currLink;
    if (anchor) {
        var autolink = anchor.getAttribute("data-wysiwyg-autolink");

        if (autolink === "true") {
            var pattern = this.wikiDetectLinkPattern;
            pattern.lastIndex = 0;
            var label = Wysiwyg.getTextContent(anchor);
            var match = pattern.exec(label);
            if (match && match.index === 0 && match[0].length === label.length) {
                currLink = this.normalizeLink(label);
            }
        }
        if (!currLink) {
            currLink = anchor.getAttribute("data-wysiwyg-link") || anchor.href;
        }
    } else {
        currLink = "";
    }
    if (expand) {
        this.selectNodeContents(expand);
    }
    var text = this.getSelectionText() || "";
    var newLink = (prompt(text ? "Enter link:" : "Insert link:", currLink) || "").replace(/^\s+|\s+$/g, "");
    if (newLink && newLink !== currLink) {
        text = text || newLink;
        newLink = this.normalizeLink(newLink);
        var id = this.generateDomId();
        var d = this.contentDocument;
        var anonymous = d.createElement("div");
        anchor = this.createAnchor(newLink, text, { id: id });
        anonymous.appendChild(anchor);
        this.insertHTML(anonymous.innerHTML);
        anchor = d.getElementById(id);
        if (anchor) {
            this.selectNodeContents(anchor);
        }
    }
    this.selectionChanged();
};

Wysiwyg.prototype.createAnchor = function (link, label, attrs) {
    var d = this.contentDocument;
    var anchor = d.createElement("a");
    var name;
    for (name in attrs) {
        if (attrs.hasOwnProperty(name)) {
            var value = attrs[name];
            anchor.setAttribute(name, value);
        }
    }
    anchor.href = link;
    anchor.title = link;
    anchor.setAttribute("data-wysiwyg-link", link);
    if (label) {
        anchor.appendChild(d.createTextNode(label));
    }
    return anchor;
};

Wysiwyg.prototype.createCollapsibleSection = function () {
    var collapsible = this.contentDocument.createElement("div");

    $(collapsible).addClass("collapsible");
    return collapsible;
};

Wysiwyg.prototype.collectChildNodes = function (dest, source) {
    var childNodes = source.childNodes;
    var i;
    for (i = childNodes.length - 1; i >= 0; i--) {
        dest.insertBefore(childNodes[i], dest.firstChild);
    }
};

Wysiwyg.prototype.generateDomId = function () {
    var d = this.contentDocument;
    while (true) {
        var id = "tmp-" + (new Date().valueOf().toString(36));
        if (!d.getElementById(id)) {
            return id;
        }
    }
};

Wysiwyg.prototype.selectionChanged = function () {
    var status = {
        strong: false,
        em: false,
        underline: false,
        strike: false,
        sub: false,
        sup: false,
        escape: false,
        paragraph: false,
        heading1: false,
        heading2: false,
        heading3: false,
        heading4: false,
        heading5: false,
        heading6: false,
        link: false,
        ol: false,
        ul: false,
        outdent: false,
        indent: false,
        table: false,
        code: false,
        quote: false,
        hr: false,
        br: false
    };
    var tagNameToKey = {
        b: "strong",
        i: "em",
        u: "underline",
        del: "strike",
        tt: "escape",
        p: "paragraph",
        h1: "heading1",
        h2: "heading2",
        h3: "heading3",
        h4: "heading4",
        h5: "heading5",
        h6: "heading6",
        a: "link",
        pre: "code"
    };
    var position = this.getSelectionPosition();
    var node, toolbarButtons, name;

    if (position.start) {
        node = position.start === position.end ? position.start.firstChild : position.start.nextSibling;
        node = node || position.start;
    } else {
        node = null;
    }
    while (node) {
        if (node.nodeType === 1) {
            name = node.tagName.toLowerCase();
            if (tagNameToKey.hasOwnProperty(name)) {
                name = tagNameToKey[name];
            }
            status[name] = true;
        }
        node = node.parentNode;
    }

    toolbarButtons = this.toolbarButtons;
    for (name in status) {
        if (status.hasOwnProperty(name)) {
            var button = toolbarButtons[name];
            if (button) {
                var parent = button.parentNode;
                parent.className = (parent.className || "").replace(/ *\bselected\b|$/, status[name] ? " selected" : "");
            }
        }
    }

    if (status["table"]) {
    	$(".wysiwyg-toolbar .non-table").hide();
    	$(".wysiwyg-toolbar .in-table").show();
    } else {
    	$(".wysiwyg-toolbar .in-table").hide();
    	$(".wysiwyg-toolbar .non-table").show();
    }
    $(window).resize();
    
    var styles = [ "quote", "paragraph", "code", "heading1",
        "heading2", "heading3", "heading4", "heading5", "heading6" ];
    var styleButton = toolbarButtons.style;
    var styleButtonClass = "wysiwyg-menu-style";
    var i;
    for (i = 0; i < styles.length; i++) {
        name = styles[i];
        if (status[name]) {
            styleButtonClass = "wysiwyg-menu-" + name;
            break;
        }
    }
    styleButton.parentNode.className = styleButtonClass;
};

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
    wikiInlineRules.push("![-<{(\\[]");             // 7. escaped (open)
    wikiInlineRules.push("[->)\\]]!|\\}");          // 8. escaped (close)
    wikiInlineRules.push(_wikiTextLink);			// 9. Wiki link
    wikiInlineRules.push(_wikiPageName);            // 10. WikiPage name
    wikiInlineRules.push("\\${[^}]+}");             // 11. Variable

    var wikiRules = [];
    // -1. header
    wikiRules.push("^[ \\t\\r\\f\\v]*![1-6][ \\t\\r\\f\\v]+.*?(?:#" + _xmlName + ")?[ \\t\\r\\f\\v]*$");
    // -2. list
    wikiRules.push("^[ \\t\\r\\f\\v]*[*1-9-][ \\t\\r\\f\\v]");
    // -3. definition and comment
    wikiRules.push("^(?:![a-z]|#)");
    // -4. closing table row
    wikiRules.push("\\|[ \\t\\r\\f\\v]*$");
    // -5. cell
    wikiRules.push("^-?!?\\||\\|");
    // -6: open collapsible section
    wikiRules.push("^!\\*+[<>]?(?:[ \\t\\r\\f\\v]*|[ \\t\\r\\f\\v]+.*)$");
    // -7: close collapsible section
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

Wysiwyg.prototype.normalizeLink = function (link) {
    if (/^[\/.#]/.test(link)) {
        link = encodeURIComponent(link);
    }
//    if (!/^[\w.+\-]+:/.test(link)) {
//        link = link;
//    }
    if (/^[^\"\']/.test(link) && /\s/.test(link)) {
        if (link.indexOf('"') === -1) {
            link = '"' + link + '"';
        } else if (link.indexOf("'") === -1) {
            link = "'" + link + "'";
        } else {
            link = '"' + link.replace(/"/g, "%22") + '"';
        }
    }
    return link;
};

Wysiwyg.prototype.isInlineNode = function (node) {
    if (node) {
        switch (node.nodeType) {
        case 1:
            return this.wikiInlineTags.hasOwnProperty(node.tagName.toLowerCase());
        case 3:
            return true;
        }
    }
    return false;
};

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

    function generator(prop, blocks) {
        return function (node) {
            if (!node) {
                return false;
            }
            while (!node[prop]) {
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
    }

    Wysiwyg.prototype.isLastChildInBlockNode = generator("nextSibling", blocks);
}());

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
    function inTableRow() { return getSelfOrAncestor(holder, "tr"); }
    function inAnchor() { return getSelfOrAncestor(holder, "a"); }
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

    function openEscapedText(value) {
        if (!inEscapedText()) {
            var element = contentDocument.createElement("tt");
            element.setAttribute('class', { '!-': 'escape', '!<': 'htmlescape', '!{': 'hashtable', '!(': 'nested', '![': 'plaintexttable' }[value]);
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
            if (target.getAttribute('class') === { '-!': 'escape', '>!': 'htmlescape', '}': 'hashtable', ')!': 'nested', ']!': 'plaintexttable' }[value]) {
                holder = target.parentNode;
                return;
            }
        }
        holder.appendChild(contentDocument.createTextNode(value));
    }

    function handleTableCell(action, escaped, hidden) {
        var d = contentDocument;
        var h, table, tbody, cell;

        if (!inTable()) {
            h = holder;
            
            // Just ensure you can type between to tables
            if (h.lastChild && h.lastChild.tagName === 'TABLE') {
            	h.appendChild(d.createElement('p'));
            }
            table = d.createElement("table");
            if (escaped) {
                table.className = "escaped";
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
        case 1:
            row = d.createElement("tr");
            tbody.appendChild(row);
            if (hidden) {
                row.className = "hidden";
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
        if (inTable()) {
            var target = getSelfOrAncestor(holder, "table");

            self.spanTableColumns(target);
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
                    && !inParagraph() && !inAnchor() && !inEscapedText() && !inCodeBlock() && !currentHeader) {
                closeToFragment();
            }


            if (text || (match && matchNumber > 0)) {
                if (inParagraph() && (prevIndex === 0)) {
                    text = text ? ((holder.hasChildNodes() && holder.lastChild.tagName !== 'BR' ? " " : "") + text) : "";
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
            case 7:     // open escaped
                if (inEscapedText()) { break; }
                openEscapedText(matchText);
                continue;
            case 8:     // close escaped
                closeEscapedText(matchText);
                continue;
            case 9:		// Wiki link
                if (inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                handleLinks(matchText);
                continue;
            case 10:		// WikiPage name
                if (inEscapedTable() || inEscapedText() || inCodeBlock()) { break; }
                handleWikiPageName(matchText);
                continue;
            case 11:    // Variable
                handleVariable(matchText);
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
            case -3:    // definition (leading "!") and comments (leading "#")
                if (inEscapedText() || inCodeBlock()) { break; }
                handleDefinition(matchText);
                continue;
            case -4:    // closing table row
                if (inEscapedText() || inCodeBlock()) { break; }
                if (inTable()) {
                    handleTableCell(-1);
                    continue;
                }
                break;
            case -5:    // cell
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
                handleTableCell(inTableRow() ? 0 : 1, /^-?!/.test(matchText), /^-/.test(matchText));
                continue;
            case -6: // collapsible section
                if (inEscapedText()) { break; }
                handleCollapsibleBlock(matchText);
                continue;
            case -7: // close collapsible section
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

        if (inEscapedText() || inCodeBlock()) {
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
        if (inTable() && !inEscapedText()) {
            handleTableCell(-1);
        }
        
    }
    closeToFragment();

    return fragment;
};

Wysiwyg.prototype.wikitextToOnelinerFragment = function (wikitext, contentDocument) {
    var source = this.wikitextToFragment(wikitext, contentDocument);
    var fragment = contentDocument.createDocumentFragment();
    this.collectChildNodes(fragment, source.firstChild);
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
    "span": true
};

Wysiwyg.prototype.domToWikitext = function (root, options) {
    options = options || {};
    var retainNewLines = !!options.retainNewLines;

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
    var skipNode = null;

    function tokenFromSpan(node) {
        var style = node.style;
        if (style.fontWeight === "bold") {
            return wikiOpenTokens.b;
        }
        if (style.fontStyle === "italic") {
            return wikiOpenTokens.i;
        }
        switch (style.textDecoration) {
        case "line-through":
            return wikiOpenTokens.del;
        }
        return undefined;
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
        if (!/\]/.test(label) && !/^[\"\']/.test(label)) {
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
            if (wikiBlockTags[name] && self.isInlineNode(node.previousSibling)) {
                _texts.push("\n");
            }
            if (token !== true) {
                pushToken(token);
            }
            if (name === "table") {
                if ($('tr', node).first().hasClass('hidden')) {
                    _texts.push("-");
                }
                if ($(node).hasClass("escaped")) {
                    _texts.push("!");
                }
            }
        } else {
            var value, text;
            switch (name) {
            case "#text":
                value = node.nodeValue;
                if (value) {
                    if (!(inCodeBlock || retainNewLines)) {
                        if (value && !self.isInlineNode(node.previousSibling || node.parentNode)) {
                            value = value.replace(/^[ \t\r\n\f\v]+/g, "");
                        }
                        if (value && !self.isInlineNode(node.nextSibling || node.parentNode)) {
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
                if (!/[^ \t\r\n\f\v]/.test(getTextContent(node))) {
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
                    if (self.isInlineNode(node.previousSibling)) {
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
                } else if (!self.isBogusLineBreak(node)) {
                    _texts.push(" ");
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
                text = self.domToWikitext(node, self.options).replace(/^ +| +$/g, "").replace(/\n$/, "");
                if (text) {
                    _texts.push(" ", text, " ");
                    break;
                } else {
                    _texts.push(" ");
                }
                break;
            case "tr":
                break;
            case "tt":
                skipNode = node;
                value = node.innerHTML.replace(/<br>/g, '\n').replace(/&gt;/g, ">").replace(/&lt;/g, "<");
                if (value) {
                    var tags = {
                        'escape': [ "!-", "-!" ],
                        'htmlescape': [ "!<", ">!" ],
                        'hashtable': [ "!{", "}" ],
                        'nested': [ "!(", ")!" ],
                        'plaintexttable': [ "![", "]!" ],
                        'inlinecode': [ "{{{", "}}}" ]
                    }[node.getAttribute('class') || "escape"];
                    text = tags[0] + value + tags[1];
                    pushTextWithDecorations(text, node);
                }
                break;
            case "span":
                if (node.className === "wikianchor" && xmlNamePattern.test(node.id || "")) {
                    skipNode = node;
                    text = self.domToWikitext(node, self.options).replace(/^ +| +$|\]/g, "");
                    _texts.push("[=#", node.id, text ? " " + text + "]" : "]");
                } else {
                    token = tokenFromSpan(node);
                    if (token !== undefined) {
                        pushToken(token);
                    }
                }
                break;
            case "div":
                if ($(node).hasClass("collapsible")) {
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
                if ($(node).hasClass("collapsible")) {
                    _texts.push("*!\n");
                }
                break;
            case "table":
                _texts.push("\n");
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
    return texts.join("").replace(/^(?: *\n)+/, "").replace(/(?: *\n)+$/, "\n");
};

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

if (window.getSelection) {
    Wysiwyg.prototype.appendBogusLineBreak = function (element) {
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
    };
    Wysiwyg.prototype.isBogusLineBreak = Wysiwyg.prototype.isLastChildInBlockNode;
    Wysiwyg.prototype.insertParagraphOnEnter = function (event) {
        var range = this.getSelectionRange();
        var node = range.endContainer;
        var header = null;
        if (node && node.nodeType === 3 && range.endOffset === node.nodeValue.length) {
            var nextSibling = node.nextSibling;
            if (!nextSibling || nextSibling.tagName.toLowerCase() === "br") {
                while (node) {
                    if (node.nodeType === 1 && /^h[1-6]$/i.exec(node.tagName)) {
                        header = node;
                        break;
                    }
                    node = node.parentNode;
                }
                if (header) {
                    var parent = header.parentNode;
                    var childNodes = parent.childNodes;
                    var length = childNodes.length;
                    var offset;
                    for (offset = 0; offset < length; offset++) {
                        if (childNodes[offset] === header) {
                            offset++;
                            break;
                        }
                    }
                    this.selectRange(parent, offset, parent, offset);
                    this.insertHTML('<p><br/></p>');
                    Wysiwyg.stopEvent(event);
                }
            }
        }
    };
    Wysiwyg.prototype.tableHTML = function (id, row, col) {
        var html = this._tableHTML(row, col);
        return html.replace(/<td><\/td>/g, '<td><br/></td>').replace(/<td>/, '<td id="' + id + '">');
    };
    Wysiwyg.prototype.insertTableCell = function (row, index) {
        var cell = row.insertCell(index);
        this.appendBogusLineBreak(cell);
        return cell;
    };
    Wysiwyg.prototype.getFocusNode = function () {
        return this.contentWindow.getSelection().focusNode;
    };
    if (window.opera) {
        Wysiwyg.prototype.insertLineBreak = function () {
            this.execCommand("inserthtml", "<br/>");
        };
        Wysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    } else if (window.getSelection().setBaseAndExtent) {  // Safari 2+
        Wysiwyg.prototype.insertLineBreak = function () {
            this.execCommand("insertlinebreak");
        };
        Wysiwyg.prototype.insertLineBreakOnShiftEnter = function (event) {
            this.insertLineBreak();
            Wysiwyg.stopEvent(event);
        };
    } else {  // Firefox 2+
        Wysiwyg.prototype.insertLineBreak = function () {
            var event = this.contentDocument.createEvent("KeyboardEvent");
            event.initKeyEvent("keypress", true, true, null, false, false, true, false, 0x000d, 0);
            this.frame.dispatchEvent(event);
        };
        Wysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    }
    if (window.getSelection().removeAllRanges) {
        Wysiwyg.prototype.selectNode = function (node) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.selectNode(node);
            selection.addRange(range);
        };
        Wysiwyg.prototype.selectNodeContents = function (node) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.selectNodeContents(node);
            selection.addRange(range);
        };
        Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.setStart(start, startOffset);
            range.setEnd(end, endOffset);
            selection.addRange(range);
        };
        Wysiwyg.prototype.getNativeSelectionRange = function () {
            var selection = this.contentWindow.getSelection();
            return selection.rangeCount > 0 ? selection.getRangeAt(0) : null;
        };
        Wysiwyg.prototype.expandSelectionToElement = function (arg) {
            if (arg.start || arg.end) {
                var selection = this.contentWindow.getSelection();
                var range = this.getNativeSelectionRange() || this.contentDocument.createRange();
                selection.removeAllRanges();
                if (arg.start) {
                    range.setStartBefore(arg.start);
                }
                if (arg.end) {
                    range.setEndAfter(arg.end);
                }
                selection.addRange(range);
            }
        };
        Wysiwyg.prototype.insertHTML = function (html) {
            this.execCommand("inserthtml", html);
        };
    } else {      // Safari 2
        Wysiwyg.prototype.selectNode = function (node) {
            var selection = this.contentWindow.getSelection();
            var range = this.contentDocument.createRange();
            range.selectNode(node);
            selection.setBaseAndExtent(range.startContainer, range.startOffset, range.endContainer, range.endOffset);
            range.detach();
        };
        Wysiwyg.prototype.selectNodeContents = function (node) {
            this.selectRange(node, 0, node, node.childNodes.length);
        };
        Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) {
            var selection = this.contentWindow.getSelection();
            selection.setBaseAndExtent(start, startOffset, end, endOffset);
        };
        Wysiwyg.prototype.getNativeSelectionRange = function () {
            var selection = this.contentWindow.getSelection();
            if (selection.anchorNode) {
                var range = this.contentDocument.createRange();
                range.setStart(selection.baseNode, selection.baseOffset);
                range.setEnd(selection.extentNode, selection.extentOffset);
                if (range.collapsed && !selection.isCollapsed) {
                    range.setStart(selection.extentNode, selection.extentOffset);
                    range.setEnd(selection.baseNode, selection.baseOffset);
                }
                return range;
            }
            return null;
        };
        Wysiwyg.prototype.expandSelectionToElement = function (arg) {
            if (arg.start || arg.end) {
                var selection = this.contentWindow.getSelection();
                var range = this.getNativeSelectionRange();
                if (arg.start) {
                    range.setStartBefore(arg.start);
                }
                if (arg.end) {
                    range.setEndAfter(arg.end);
                }
                selection.setBaseAndExtent(range.startContainer, range.startOffset, range.endContainer, range.endOffset);
                range.detach();
            }
        };
        Wysiwyg.prototype.insertHTML = function (html) {
            var range = this.getNativeSelectionRange();
            if (range) {
                var d = this.contentDocument;
                var tmp = d.createRange();
                tmp.setStart(this.frame, 0);
                tmp.setEnd(this.frame, 0);
                var fragment = tmp.createContextualFragment(html);
                range.deleteContents();
                range.insertNode(fragment);
                range.detach();
                tmp.detach();
            }
        };
    }
    Wysiwyg.prototype.getSelectionRange = Wysiwyg.prototype.getNativeSelectionRange;
    Wysiwyg.prototype.getSelectionText = function () {
        var range = this.getNativeSelectionRange();
        return range ? range.toString() : null;
    };
    Wysiwyg.prototype.getSelectionHTML = function () {
        var fragment = this.getSelectionFragment();
        var anonymous = this.contentDocument.createElement("div");
        anonymous.appendChild(fragment);
        return anonymous.innerHTML;
    };
    Wysiwyg.prototype.getSelectionFragment = function () {
        var range = this.getNativeSelectionRange();
        return range ? range.cloneContents() : this.contentDocument.createDocumentFragment();
    };
    Wysiwyg.prototype.getSelectionPosition = function () {
        var range = this.getNativeSelectionRange();
        var position = { start: null, end: null };
        if (range) {
            position.start = range.startContainer;
            position.end = range.endContainer;
        }
        return position;
    };
    Wysiwyg.prototype.selectionContainsTagName = function (name) {
        var selection = this.contentWindow.getSelection();
        var range = this.getNativeSelectionRange();
        if (!range) {
            return false;
        }
        var ancestor = range.commonAncestorContainer;
        if (!ancestor) {
            return false;
        }
        if (Wysiwyg.getSelfOrAncestor(ancestor, name)) {
            return true;
        }
        if (ancestor.nodeType !== 1) {
            return false;
        }
        var elements = ancestor.getElementsByTagName(name);
        var length = elements.length;
        var i;
        for (i = 0; i < length; i++) {
            if (selection.containsNode(elements[i], true)) {
                return true;
            }
        }
        return false;
    };

} else if (document.selection) {
    Wysiwyg.prototype.appendBogusLineBreak = function (element) { };
    Wysiwyg.prototype.isBogusLineBreak = function () { return false; };
    Wysiwyg.prototype.insertParagraphOnEnter = null;
    Wysiwyg.prototype.insertLineBreak = function () {
        this.insertHTML("<br/>");
    };
    Wysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    Wysiwyg.prototype.tableHTML = function (id, row, col) {
        var html = this._tableHTML(row, col);
        return html.replace(/<td>/, '<td id="' + id + '">');
    };
    Wysiwyg.prototype.insertTableCell = function (row, index) {
        return row.insertCell(index);
    };
    Wysiwyg.prototype.getFocusNode = function () {
        this.frame.focus();
        var d = this.contentDocument;
        var range = d.selection.createRange();
        var node = range.item ? range.item(0) : range.parentElement();
        return node.ownerDocument === d ? node : null;
    };
    Wysiwyg.prototype.selectNode = function (node) {
        var d = this.contentDocument;
        var body = this.frame;
        var range;
        d.selection.empty();
        try {
            range = body.createControlRange();
            range.addElement(node);
        } catch (e) {
            range = body.createTextRange();
            range.moveToElementText(node);
        }
        range.select();
    };
    Wysiwyg.prototype.selectNodeContents = function (node) {
        var d = this.contentDocument;
        d.selection.empty();
        var range = this.frame.createTextRange();
        range.moveToElementText(node);
        range.select();
    };
    Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) {
        var d = this.contentDocument;
        var body = this.frame;
        d.selection.empty();
        var range = endPoint(start, startOffset);
        if (start !== end || startOffset !== endOffset) {
            range.setEndPoint("EndToEnd", endPoint(end, endOffset));
        }
        range.select();

        function endPoint(node, offset) {
            var range;
            if (node.nodeType === 1) {
                var childNodes = node.childNodes;
                if (offset >= childNodes.length) {
                    range = body.createTextRange();
                    range.moveToElementText(node);
                    range.collapse(false);
                    return range;
                }
                node = childNodes[offset];
                if (node.nodeType === 1) {
                    range = body.createTextRange();
                    range.moveToElementText(node);
                    range.collapse(true);
                    switch (node.tagName.toLowerCase()) {
                    case "table":
                        range.move("character", -1);
                        break;
                    }
                    return range;
                }
                return endPoint(node, 0);
            }
            if (node.nodeType !== 3) {
                throw "selectRange: nodeType != @".replace(/@/, node.nodeType);
            }

            range = body.createTextRange();
            var element = node.previousSibling;
            while (element) {
                var nodeType = element.nodeType;
                if (nodeType === 1) {
                    range.moveToElementText(element);
                    range.collapse(false);
                    break;
                }
                if (nodeType === 3) {
                    offset += element.nodeValue.length;
                }
                element = element.previousSibling;
            }
            if (!element) {
                range.moveToElementText(node.parentNode);
                range.collapse(true);
            }
            if (offset !== 0) {
                range.move("character", offset);
            }
            return range;
        }
    };
    Wysiwyg.prototype.getSelectionRange = function () {
        var body = this.frame;
        var pseudo = {};
        var start = this.getNativeSelectionRange();
        if (start.item) {
            var element = start.item(0);
            var parent = element.parentNode;
            var childNodes = parent.childNodes;
            var length = childNodes.length;
            var i;

            for (i = 0; i < length; i++) {
                if (childNodes[i] === element) {
                    pseudo.startOffset = i;
                    pseudo.endOffset = i + 1;
                    break;
                }
            }
            pseudo.collapsed = false;
            pseudo.startContainer = pseudo.endContainer = parent;
            return pseudo;
        }
        var end = start.duplicate();
        pseudo.collapsed = start.compareEndPoints("StartToEnd", end) === 0;
        start.collapse(true);
        end.collapse(false);

        function nextElement(range) {
            var parent = range.parentElement();
            var childNodes = parent.childNodes;
            var length = childNodes.length;
            var i;
            for (i = 0; i < length; i++) {
                var node = childNodes[i];
                if (node.nodeType === 1) {
                    var tmp = body.createTextRange();
                    tmp.moveToElementText(node);
                    if (range.compareEndPoints("EndToStart", tmp) <= 0) {
                        return node;
                    }
                }
            }
            return null;
        }

        function nodeOffset(range, parent, element, index, length) {
            var tmp = body.createTextRange();
            var len;
            tmp.moveToElementText(element || parent);
            tmp.collapse(!!element);
            tmp.move("character", -index);
            if (!element) {
                length++;
            }
            for (len = length; len >= 0; len--) {
                if (tmp.compareEndPoints("EndToStart", range) === 0) {
                    return len;
                }
                tmp.move("character", -1);
            }
            return null;
        }

        function setContainerOffset(range, containerKey, offsetKey) {
            var parent = range.parentElement();
            var element = nextElement(range);
            var index = 0;
            var node = element ? element.previousSibling : parent.lastChild;
            var offset, length;
            while (node && node.nodeType === 3) {
                length = node.nodeValue.length;
                offset = nodeOffset(range, parent, element, index, length);
                if (offset !== null) {
                    pseudo[containerKey] = node;
                    pseudo[offsetKey] = offset;
                    return;
                }
                index += length;
                node = node.previousSibling;
            }
            var childNodes = parent.childNodes;
            length = childNodes.length;
            if (length > 0) {
                pseudo[containerKey] = parent;
                pseudo[offsetKey] = containerKey === "startContainer" ? 0 : length - 1;
                return;
            }
            element = parent;
            parent = element.parentNode;
            childNodes = parent.childNodes;
            length = childNodes.length;
            for (offset = 0; offset < length; offset++) {
                if (element === childNodes[offset]) {
                    pseudo[containerKey] = parent;
                    pseudo[offsetKey] = offset;
                    return;
                }
            }
        }

        setContainerOffset(start, "startContainer", "startOffset");
        setContainerOffset(end, "endContainer", "endOffset");
        return pseudo;
    };
    Wysiwyg.prototype.getNativeSelectionRange = function () {
        this.contentWindow.focus();
        return this.contentDocument.selection.createRange();
    };
    Wysiwyg.prototype.getSelectionText = function () {
        var range = this.getNativeSelectionRange();
        if (range) {
            return range.item ? range.item(0).innerText : range.text;
        }
        return null;
    };
    Wysiwyg.prototype.getSelectionHTML = function () {
        var range = this.getNativeSelectionRange();
        if (range) {
            return range.item ? range.item(0).innerHTML : range.htmlText;
        }
        return null;
    };
    Wysiwyg.prototype.getSelectionFragment = function () {
        var d = this.contentDocument;
        var fragment = d.createDocumentFragment();
        var anonymous = d.createElement("div");
        anonymous.innerHTML = this.getSelectionHTML();
        this.collectChildNodes(fragment, anonymous);
        return fragment;
    };
    Wysiwyg.prototype.getSelectionPosition = function () {
        this.frame.focus();
        var d = this.contentDocument;
        var range = d.selection.createRange();
        var startNode = null;
        var endNode = null;
        if (range.item) {
            if (range.item(0).ownerDocument === d) {
                startNode = range.item(0);
                endNode = range.item(range.length - 1);
            }
        } else {
            if (range.parentElement().ownerDocument === d) {
                var startRange = range.duplicate();
                startRange.collapse(true);
                startNode = startRange.parentElement();
                var endRange = range.duplicate();
                endRange.collapse(false);
                endNode = endRange.parentElement();
            }
        }
        return { start: startNode, end: endNode };
    };
    Wysiwyg.prototype.expandSelectionToElement = function (arg) {
        this.frame.focus();
        var d = this.contentDocument;
        var body = this.frame;
        var range = d.selection.createRange();
        var tmp;
        if (arg.start) {
            tmp = body.createTextRange();
            tmp.moveToElementText(arg.start);
            range.setEndPoint("StartToStart", tmp);
        }
        if (arg.end) {
            tmp = body.createTextRange();
            tmp.moveToElementText(arg.end);
            range.setEndPoint("EndToEnd", tmp);
        }
        if (tmp) {
            range.select();
        }
    };
    Wysiwyg.prototype.selectionContainsTagName = function (name) {
        this.frame.focus();
        var d = this.contentDocument;
        var selection = d.selection;
        var range = selection.createRange();
        var parent = range.item ? range.item(0) : range.parentElement();
        if (!parent) {
            return false;
        }
        if (Wysiwyg.getSelfOrAncestor(parent, name)) {
            return true;
        }
        var elements = parent.getElementsByTagName(name);
        var length = elements.length;
        var i;
        for (i = 0; i < length; i++) {
            var testRange = selection.createRange();
            testRange.moveToElementText(elements[i]);
            if (range.compareEndPoints("StartToEnd", testRange) <= 0
                    && range.compareEndPoints("EndToStart", testRange) >= 0) {
                return true;
            }
        }
        return false;
    };
    Wysiwyg.prototype.insertHTML = function (html) {
        this.frame.focus();
        var selection = this.contentDocument.selection;
        var range = selection.createRange();
        range.pasteHTML(html.replace(/\t/g, "&#9;"));
        range.collapse(false);
        range.select();
        range = this.contentDocument.selection.createRange();
    };

} else {
    Wysiwyg.prototype.appendBogusLineBreak = function (element) { };
    Wysiwyg.prototype.insertParagraphOnEnter = null;
    Wysiwyg.prototype.insertLineBreak = function () { };
    Wysiwyg.prototype.insertTableCell = function () { return null; };
    Wysiwyg.prototype.getFocusNode = function () { return null; };
    Wysiwyg.prototype.selectNode = function (node) { };
    Wysiwyg.prototype.selectNodeContents = function () { return null; };
    Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) { };
    Wysiwyg.prototype.getSelectionRange = function () { return null; };
    Wysiwyg.prototype.getNativeSelectionRange = function () { return null; };
    Wysiwyg.prototype.getSelectionText = function () { return null; };
    Wysiwyg.prototype.getSelectionHTML = function () { return null; };
    Wysiwyg.prototype.getSelectionFragment = function () { return null; };
    Wysiwyg.prototype.getSelectionPosition = function () { return null; };
    Wysiwyg.prototype.expandSelectionToElement = function () { };
    Wysiwyg.prototype.selectionContainsTagName = function () { return false; };
    Wysiwyg.prototype.insertHTML = function (html) { };
}

Wysiwyg.prototype._treeWalkEmulation = function (root, iterator) {
    if (!root.firstChild) {
        iterator(null);
        return;
    }
    var element = root;
    while (element) {
        if (element.firstChild) {
            element = element.firstChild;
        } else if (element.nextSibling) {
            element = element.nextSibling;
        } else {
            while (true) {
                element = element.parentNode;
                if (element === root || !element) {
                    iterator(null);
                    return;
                }
                if (element.nextSibling) {
                    element = element.nextSibling;
                    break;
                }
            }
        }
        iterator(element);
    }
};

if (document.createTreeWalker) {
    Wysiwyg.prototype.treeWalk = function (root, iterator) {
        var walker = root.ownerDocument.createTreeWalker(
            root,
                NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT,
            null,
            true
        );
        while (walker.nextNode()) {
            iterator(walker.currentNode);
        }
        iterator(null);
    };
} else {
    Wysiwyg.prototype.treeWalk = Wysiwyg.prototype._treeWalkEmulation;
}

Wysiwyg.instances = [];
Wysiwyg.count = 0;
Wysiwyg.paths = null;

Wysiwyg.newInstance = function (textarea, options) {
    var instance = new Wysiwyg(textarea, options);
    Wysiwyg.instances.push(instance);
    return instance;
};

Wysiwyg.findInstance = function (textarea) {
    var instances = Wysiwyg.instances;
    var length = instances.length;
    var i;
    for (i = 0; i < length; i++) {
        var instance = instances[i];
        if (instance.textarea === textarea) {
            return instance;
        }
    }
    return null;
};

Wysiwyg.getOptions = function () {
    var options = {};
    //noinspection JSUnresolvedVariable
    if (window._wysiwyg) {
        options = window._wysiwyg;
    }
    return options;
};

Wysiwyg.getEditorMode = function () {
    if (Wysiwyg.editorMode) {
        return Wysiwyg.editorMode;
    }

    var mode = null;
    var cookies = (document.cookie || "").split(";");
    var length = cookies.length;
    var i;
    for (i = 0; i < length; i++) {
        var match = /^\s*wysiwyg=(\S*)/.exec(cookies[i]);
        if (match) {
            switch (match[1]) {
            case "wysiwyg":
                mode = match[1];
                break;
            default:    // "textarea"
                mode = null;
                break;
            }
            break;
        }
    }

    Wysiwyg.editorMode = mode || "textarea";
    return Wysiwyg.editorMode;
};

Wysiwyg.setCookie = function (key, val) {
    var now = new Date();
    var expires = new Date(now.getTime() + 365 * 86400 * 1000);
    var pieces = [ key + "=" + val,
        "path=" + window.location.origin,
        "expires=" + expires.toUTCString() ];
    document.cookie = pieces.join("; ");
};

Wysiwyg.removeEvent = function (element, type, func) {
    jQuery(element).unbind(type, func);
};

Wysiwyg.stopEvent = function (event) {
    if (event.preventDefault) {
        event.preventDefault();
        event.stopPropagation();
    } else {
        event.returnValue = false;
        event.cancelBubble = true;
    }
};

Wysiwyg.setStyle = function (element, object) {
    var style = element.style;
    var name;
    for (name in object) {
        if (object.hasOwnProperty(name)) {
            style[name] = object[name];
        }
    }
};

if (document.defaultView) {
    Wysiwyg.getStyle = function (element, name) {
        var value = element.style[name];
        if (!value) {
            var style = element.ownerDocument.defaultView.getComputedStyle(element, null);
            value = style ? style[name] : null;
        }
        return value;
    };
} else {
    Wysiwyg.getStyle = function (element, name) {
        return element.style[name] || element.currentStyle[name];
    };
}

Wysiwyg.getSelfOrAncestor = function (element, name) {
    var target = element;
    var d = element.ownerDocument;
    if (name instanceof RegExp) {
        while (target && target !== d) {
            switch (target.nodeType) {
            case 1: // element
                if (name.test(target.tagName.toLowerCase())) {
                    return target;
                }
                break;
            case 11: // fragment
                return null;
            }
            target = target.parentNode;
        }
    } else {
        name = name.toLowerCase();
        while (target && target !== d) {
            switch (target.nodeType) {
            case 1: // element
                if (target.tagName.toLowerCase() === name) {
                    return target;
                }
                break;
            case 11: // fragment
                return null;
            }
            target = target.parentNode;
        }
    }
    return null;
};

Wysiwyg.getTextContent = (function () {
    var anonymous = document.createElement("div");
    if (typeof anonymous.textContent !== undefined) {
        return function (element) { return element.textContent; };
    } else if (typeof anonymous.innerText !== undefined) {
        return function (element) { return element.innerText; };
    } else {
        return function () { return null; };
    }
})();

Wysiwyg.initialize = function () {
    if ("replace".replace(/[a-e]/g, function () { return "*"; }) !== "r*pl***") {
        return;
    }
    if (typeof document.designMode === undefined) {
        return;
    }
    var options = Wysiwyg.getOptions();
    var textAreas = document.getElementsByTagName("textarea");
    var editors = [];
    var i;
    for (i = 0; i < textAreas.length; i++) {
        var textArea = textAreas[i];
        if (/\bwikitext\b/.test(textArea.className || "")) {
            editors.push(Wysiwyg.newInstance(textArea, options));
        }
    }
    return editors;
};

// vim:et:ai:ts=4
