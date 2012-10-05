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
    var body = document.body;
    var i;

    this.wrapTextarea = Wysiwyg.getWrapOn();
    this.textarea = textarea;
    this.options = options = options || {};

    this.createEditable(document, textarea);
    var frame = this.frame;

    this.contentWindow = frame.contentWindow;
    this.contentDocument = this.contentWindow.document;

    this.initializeEditor(this.contentDocument);
    this.wysiwygToolbar = this.createWysiwygToolbar(document);
    this.styleMenu = this.createStyleMenu(document);
    this.menus = [ this.styleMenu ];
    this.toolbarButtons = this.setupMenuEvents();
    this.toggleEditorButtons = null;
    this.wrapTextareaButton = null;
    this.savedWysiwygHTML = null;

    this.setupToggleEditorButtons();
    this.setupSyncTextAreaHeight();

    var styleStatic = { position: "static", left: "-9999px", top: "-9999px" };
    var styleAbsolute = { position: "absolute", left: "-9999px", top: "-9999px" };
    switch (editorMode) {
    case "textarea":
        Wysiwyg.setStyle(textarea, styleStatic);
        Wysiwyg.setStyle(frame, { position: "absolute",
            left: "-9999px", top: Wysiwyg.elementPosition(textarea).top + "px" });
        Wysiwyg.setStyle(this.wysiwygToolbar, styleAbsolute);
        Wysiwyg.setStyle(this.wrapTextareaButton.parentNode, { display: "" });
        textarea.setAttribute("tabIndex", "");
        frame.setAttribute("tabIndex", "-1");
        break;
    case "wysiwyg":
        Wysiwyg.setStyle(textarea, { position: "absolute",
            left: "-9999px", top: Wysiwyg.elementPosition(textarea).top + "px" });
        Wysiwyg.setStyle(frame, styleStatic);
        Wysiwyg.setStyle(this.wysiwygToolbar, styleStatic);
        Wysiwyg.setStyle(this.wrapTextareaButton.parentNode, { display: "none" });
        textarea.setAttribute("tabIndex", "-1");
        frame.setAttribute("tabIndex", "");
        break;
    }

    for (i = 0; i < this.menus.length; i++) {
        body.insertBefore(this.menus[i], body.firstChild);
    }
    this.textarea.parentNode.insertBefore(this.toggleEditorButtons, this.textarea);
    this.textarea.parentNode.insertBefore(this.wysiwygToolbar, this.textarea);

    function lazySetup() {
        if (self.contentDocument.body) {
            var exception;
            try { self.execCommand("useCSS", false); } catch (e1) { }
            try { self.execCommand("styleWithCSS", false); } catch (e2) { }
            if (editorMode === "wysiwyg") {
                try { self.loadWysiwygDocument(); } catch (e3) { exception = e3; }
            }
            self.setupEditorEvents();
            self.setupFormEvent();
            if (exception) {
                self.textarea.style.position = "static";
                self.frame.style.position = self.wysiwygToolbar.style.position = "absolute";
                self.wrapTextareaButton.parentNode.style.display = "none";
                alert("Failed to activate the wysiwyg editor.");
                throw exception;
            }
        } else {
            setTimeout(lazySetup, 100);
        }
    }
    lazySetup();
};

Wysiwyg.prototype.initializeEditor = function (d) {
    var l = window.location;
    var i;
    var html = [
        '<!DOCTYPE html PUBLIC',
        ' "-//W3C//DTD XHTML 1.0 Transitional//EN"',
        ' "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n',
        '<html xmlns="http://www.w3.org/1999/xhtml">',
        '<head>',
        '<base href="',
        l.protocol,
        '//',
        l.host,
        '/" />',
        '<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />'
    ];
    var stylesheets = Wysiwyg.paths.stylesheets;
    if (!stylesheets) {
        // Work around wysiwyg stops with Agilo
        var base = Wysiwyg.paths.base.replace(/\/*$/, "/");
        stylesheets = [ "editor.css" ];
    }
    var length = stylesheets.length;
    for (i = 0; i < length; i++) {
        html.push('<link rel="stylesheet" href="' + stylesheets[i] + '" type="text/css" />');
    }

    html.push('<title></title>', '</head>', '<body></body>', '</html>');

    var first = !window.opera && d.addEventListener ? true : false;
    if (first) {
        d.designMode = "On";
    }
    d.open();
    d.write(html.join(""));
    d.close();
    if (!first) {
        d.designMode = "On";
        if (d !== this.contentWindow.document) {
            this.contentDocument = this.contentWindow.document;
        }
    }
    // disable firefox table resizing
    try { d.execCommand("enableObjectResizing", false, false); } catch (e) {}
    try { d.execCommand("enableInlineTableEditing", false, false); } catch (e) {}
};

Wysiwyg.getWrapOn = function () {
    var mode = false;
    var cookies = (document.cookie || "").split(";");
    var length = cookies.length;
    var i;
    for (i = 0; i < length; i++) {
        var match = /^\s*textwrapon=(\S*)/.exec(cookies[i]);
        if (match) {
            switch (match[1]) {
            case "true":
                mode = true;
                break;
            default:
                mode = false;
                break;
            }
            break;
        }
    }

    return mode;
};

Wysiwyg.prototype.listenerToggleWrapTextarea = function (input) {
    var self = this;

    function setWrap(wrap) {
        if (self.textarea.wrap) {
            self.textarea.wrap = wrap ? 'soft' : 'off';
        } else { // wrap attribute not supported - try Mozilla workaround
            self.textarea.setAttribute('wrap', wrap ? 'soft' : 'off');
        }
        if (wrap) {
            $(self.textarea).removeClass('no_wrap');
            Wysiwyg.setCookie("textwrapon", "true");
        } else {
            $(self.textarea).addClass('no_wrap');
            Wysiwyg.setCookie("textwrapon", "false");
        }
    }

    return function () {
        self.wrapTextarea = input.checked;
        setWrap(input.checked);
        
    };
};

Wysiwyg.prototype.listenerToggleEditor = function (type) {
    var self = this;

    switch (type) {
    case "textarea":
        return function () {
            var textarea = self.textarea;
            if (textarea.style.position === "absolute") {
                self.hideAllMenus();
                self.loadWikiText();
                textarea.style.position = "static";
                self.textarea.setAttribute("tabIndex", "");
                self.syncTextAreaHeight();
                self.frame.style.position = self.wysiwygToolbar.style.position = "absolute";
                self.frame.setAttribute("tabIndex", "-1");
                self.wrapTextareaButton.parentNode.style.display = "";
                Wysiwyg.setEditorMode(type);
            }
            self.focusTextarea();
        };
    case "wysiwyg":
        return function (event) {
            var frame = self.frame;
            if (frame.style.position === "absolute") {
                try {
                    self.loadWysiwygDocument();
                } catch (e) {
                    Wysiwyg.stopEvent(event || window.event);
                    alert("Failed to activate the wysiwyg editor.");
                    throw e;
                }
                self.textarea.style.position = "absolute";
                self.textarea.setAttribute("tabIndex", "-1");
                frame.style.position = self.wysiwygToolbar.style.position = "static";
                self.frame.setAttribute("tabIndex", "");
                self.wrapTextareaButton.parentNode.style.display = "none";
                Wysiwyg.setEditorMode(type);
            }
            self.focusWysiwyg();
        };
    }
};

Wysiwyg.prototype.activeEditor = function () {
    return this.textarea.style.position === "absolute" ? "wysiwyg" : "textarea";
};

Wysiwyg.prototype.isModified = function () {
    return this.savedWysiwygHTML !== null && this.contentDocument.body.innerHTML !== this.savedWysiwygHTML;
}

Wysiwyg.prototype.setupFormEvent = function () {
    var self = this;

    $(this.textarea.form).submit(function (event) {
        var textarea = self.textarea;
        try {
            if (self.activeEditor() === "wysiwyg") {
                var body = self.contentDocument.body;
                if (self.isModified()) {
                    self.textarea.value = self.domToWikitext(body, self.options);
                }
            }
        } catch (e) {
            Wysiwyg.stopEvent(event || window.event);
        }
    });
};

Wysiwyg.prototype.createEditable = function (d, textarea) {
    var self = this;
    var getStyle = Wysiwyg.getStyle;
    var dimension = getDimension(textarea);
    if (!dimension.width || !dimension.height) {
        setTimeout(lazy, 100);
    }
    if (!dimension.width) {
        dimension.width = parseInt(getStyle(textarea, "fontSize"), 10) * (textarea.cols || 10) * 0.5;
    }
    if (!dimension.height) {
        dimension.height = parseInt(getStyle(textarea, "lineHeight"), 10) * (textarea.rows || 3);
    }
    var wrapper = d.createElement("div");
    wrapper.innerHTML = '<iframe class="wysiwyg" '
        + 'src="javascript:\'\'" '
        + 'width="' + dimension.width + '" height="' + dimension.height + '" '
        + 'frameborder="0" marginwidth="0" marginheight="0">'
        + '</iframe>';
    var frame = this.frame = wrapper.firstChild;

    textarea.parentNode.insertBefore(frame, textarea.nextSibling);

    function getDimension(textarea) {
        var width = textarea.offsetWidth;
        if (width) {
            var parentWidth = textarea.parentNode.offsetWidth
                            + parseInt(getStyle(textarea, 'borderLeftWidth'), 10)
                            + parseInt(getStyle(textarea, 'borderRightWidth'), 10);
            if (width === parentWidth) {
                width = "100%";
            }
        }
        return { width: width, height: textarea.offsetHeight };
    }

    function lazy() {
        var dimension = getDimension(textarea);
        if (dimension.width && dimension.height) {
            self.frame.width = dimension.width;
            self.frame.height = dimension.height;
            return;
        }
        setTimeout(lazy, 100);
    }
};

Wysiwyg.prototype.createWysiwygToolbar = function (d) {
    var divider = '<li class="divider"></li>';
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
        '<li title="Collapsable section (default closed)"><a id="wt-collapsable-closed" href="#"></a></li>',
        '<li title="Collapsable section (default open)"><a id="wt-collapsable-open" href="#"></a></li>',
        '<li title="Collapsable section (hidden)"><a id="wt-collapsable-hidden" href="#"></a></li>',
        '<li title="Remove collapsable section"><a id="wt-remove-collapsable" href="#"></a></li>',
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
    Wysiwyg.setStyle(menu, { position: "absolute", left: "-1000px", top: "-1000px", zIndex: 1000 });
    menu.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return menu;
};

Wysiwyg.prototype.setupMenuEvents = function () {
    function addToolbarEvent(element, self, args) {
        var method = args.shift();
        $(element).click(function (event) {
            var w = self.contentWindow;
            Wysiwyg.stopEvent(event || w.event);
            var keepMenus = false, exception;
            try { keepMenus = method.apply(self, args); } catch (e) { exception = e; }
            if (!keepMenus) {
                self.hideAllMenus();
            }
            element.blur();
            w.focus();
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
		case "collapsable-closed":
			return [ self.insertCollapsableSection, "collapsed" ];
		case "collapsable-open":
			return [ self.insertCollapsableSection ];
		case "collapsable-hidden":
			return [ self.insertCollapsableSection, "hidden" ];
		case "remove-collapsable":
			return [ self.deleteCollapsableSection ];
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

Wysiwyg.prototype.toggleMenu = function (menu, element) {
    if (parseInt(menu.style.left, 10) < 0) {
        this.hideAllMenus(menu);
        var position = Wysiwyg.elementPosition(element);
        Wysiwyg.setStyle(menu, { left: position[0] + "px", top: (position[1] + 18) + "px" });
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
            Wysiwyg.setStyle(menus[i], { left: "-1000px", top: "-1000px" });
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
    var d = this.contentDocument;
    var ime = false;
    var inPasteAction = false;

    $(d).keydown(function (event) {
        var method = null;
        var args = null;
        event = event || self.contentWindow.event;
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
        case 0x4000004c:  // C-l
            method = self.toggleAutolink;
            args = [];
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

    $(d).keypress(function (event) {
        event = event || self.contentWindow.event;
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

    $(d).keyup(function (event) {
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

    $(d).mouseup(function () {
        self.selectionChanged();
    });

    $(d).click(function () {
        self.hideAllMenus();
        self.selectionChanged();
    });

    $(d).on('click', 'div.collapsable', function (event) {
        var x = event.pageX - this.offsetLeft;
        var y = event.pageY - this.offsetTop;
        if (x < 15 && y < 15) {
            var e = $(this);
            if (e.hasClass('collapsed')) {
                e.removeClass('collapsed').addClass('hidden');
            } else if (e.hasClass('hidden')) {
                e.removeClass('hidden');
            } else {
                e.addClass('collapsed');
            }
            event.stopPropagation();
        }
    });

    $(d).on('click', 'a', function () {
        return false;
    });

    /* Pasting data is forced in a specific div: pasteddata. In there, the
     * raw data is collected and fed to the wikiToDom parser.
     */
    $(d).on('paste', 'body', function () {
        // Clone state is change in Firefox, need to extract the fields
        var position = self.getSelectionRange();
        position = {
            sameContainer: position.startContainer === position.endContainer,
            atStart: position.startOffset === 0,
            atEnd: position.endOffset === position.endContainer.length
        };
        var html = "<div id='pasteddata'><br id='pasteddata-remove-me'/></div>";
        self.insertHTML(html);
        var pastedDataBlock = d.getElementById('pasteddata');
        self.selectNode(pastedDataBlock.firstChild);
        inPasteAction = true;

        // Post processing:
        setTimeout(function () {
            var lines, next, i, c;
            inPasteAction = false;

        	console.log('Pasted data', $(pastedDataBlock).html());

            // convert nested .pasteddata divs to br's (safari/chrome)
            $('div', pastedDataBlock).each(function (i, elem) {
                if (!/^\s*$/.test($(this).text())) {
                    $(this).before(this.childNodes);
                    $(this).before('<br/>');
                }
                $(this).remove();
            });
            
            // How to determine if it's sensible html or plain text markup:
            // markup only contains text nodes and br
            var isPlainTextData = true
            $(pastedDataBlock).children().each(function (j, elem) {
            	if (elem.tagName !== 'BR') {
            		isPlainTextData = false;
            	}
            });
            
            if (isPlainTextData) {
            	console.log('plain text', $(pastedDataBlock).html());
            	lines = $(pastedDataBlock).html().split(/<br\/?>/);
            } else {
            	console.log('DOM2WIKI', $(pastedDataBlock).html());
                lines = self.domToWikitext(pastedDataBlock, self.options).split('\n');
            }

            if (position.sameContainer && lines.length === 1 && /^\<.*\>$/.test(lines[0])) {
                // paste data without markup.
                if (!position.atStart) {
                    var prev = $(pastedDataBlock).prev();
                    $(prev).append($(pastedDataBlock).text());
                    $(pastedDataBlock).remove();
                    if (!position.atEnd) {
                        next = $(prev).next();
                        c = $(next).contents();
                        for (i = 0; i < c.length; i++) {
                            $(c[i]).appendTo(prev);
                        }
                        $(next).remove();
                    }
                } else if (!position.atEnd) {
                    next = $(pastedDataBlock).next();
                    $(next).prepend($(pastedDataBlock).text());
                    $(pastedDataBlock).remove();
                }
            } else {
            	console.log('wiki text:', lines);
                var fragment = self.wikitextToFragment(lines.join("\n"), d, self.options);
                var parentTr = getSelfOrAncestor(pastedDataBlock, 'tr');
                var parentTable = getSelfOrAncestor(pastedDataBlock, 'table');
                c = $(fragment).children();
                for (i = 0; i < c.length; i++) {
                    if (parentTr && c[i].tagName === 'TABLE') {
                        $(c[i]).find('tr').each(function(j, elem) {
                        	console.log(parentTr, elem);
                            $(parentTr).after(elem);
                            parentTr = $(parentTr).next();
                        });
                    } else {
                        $(pastedDataBlock).before(c[i]);
                    }
                }
                $(pastedDataBlock).remove();
                if (parentTable) {
                    self.spanTableColumns(parentTable);
                }
            }
        }, 20);
    });
};

Wysiwyg.prototype.loadWysiwygDocument = function () {
    var d = this.contentDocument;
    var container = d.body;
    var tmp = container.lastChild;

    while (tmp) {
        container.removeChild(tmp);
        tmp = container.lastChild;
    }
    var fragment = this.wikitextToFragment(this.textarea.value, d, this.options);
    container.appendChild(fragment);
    this.savedWysiwygHTML = container.innerHTML;
};

Wysiwyg.prototype.focusWysiwyg = function () {
    var self = this;
    var w = this.contentWindow;
    function lazy() {
        w.focus();
        try { self.execCommand("useCSS", false); } catch (e1) { }
        try { self.execCommand("styleWithCSS", false); } catch (e2) { }
        self.selectionChanged();
    }
    setTimeout(lazy, 10);
};

Wysiwyg.prototype.loadWikiText = function () {
    this.textarea.value = this.domToWikitext(this.contentDocument.body, this.options);
    this.savedWysiwygHTML = null;
};

Wysiwyg.prototype.focusTextarea = function () {
    this.textarea.focus();
};

Wysiwyg.prototype.setupToggleEditorButtons = function () {
    var div = document.createElement("div");
    var mode = Wysiwyg.editorMode;
    var html = '<label for="editor-wrap-@" title="Turns on/off wrapping">'
        + '<input type="checkbox" id="editor-wrap-@" />'
        + 'wrap </label>'
        + '<label for="editor-wysiwyg-@">'
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
    div.innerHTML = html.replace(/@/g, ++Wysiwyg.count);
    this.toggleEditorButtons = div;

    buttons = div.getElementsByTagName("input");
    for (i = 0; i < buttons.length; i++) {
        var button = buttons[i];
        var token = button.id.replace(/[0-9]+$/, "@");
        switch (token) {
        case "editor-wrap-@":
            var listener = this.listenerToggleWrapTextarea(button);
            $(button).click(listener);
            $(button).keypress(listener);
            this.wrapTextareaButton = button;
            button.checked = this.wrapTextarea ? "checked" : "";
            listener();
            break;
        case "editor-wysiwyg-@":
        case "editor-textarea-@":
            $(button).click(this.listenerToggleEditor(button.value));
            break;
        }
    }
};

Wysiwyg.prototype.setupSyncTextAreaHeight = function () {
    var self = this;
    var d = document;
    var timer = null;

    var editrows = document.getElementById("editrows");
    if (editrows) {
        $(editrows).change(changeHeight);
    }

    function changeHeight() {
        if (timer !== null) {
            clearTimeout(timer);
        }
        setTimeout(sync, 10);
    }

    function sync() {
        timer = null;
        self.syncTextAreaHeight();
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
    text = text.join("");
    if (!text) {
        return;
    }

    var pattern = this.wikiDetectLinkPattern;
    pattern.lastIndex = /[^ \t\r\n\f\v]*$/.exec(text).index;
    var match, tmp;
    for (tmp = pattern.exec(text); tmp; tmp = pattern.exec(text)) {
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

    offset = text.length - match.index - label.length;
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
        var d = this.contentDocument;
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
        var d = this.contentDocument;
        var rows = focus.table.rows;
        var length = rows.length;
        var cellIndex = focus.cell.cellIndex + (after ? 1 : 0);
        var i;
        for (i = 0; i < length; i++) {
            var row = rows[i];
            this.insertTableCell(row, Math.min(cellIndex, row.cells.length));
        }
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
        for (i = 0; i < length; i++) {
            var row = rows[i];
            if (cellIndex < row.cells.length) {
                row.deleteCell(cellIndex);
            }
        }
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
    var text = this.getSelectionText();
    if (!text) {
        var node = this.getFocusNode();
        while (node.nodeType === 3) {
            node = node.parentNode;
        }
        text = Wysiwyg.getTextContent(node);
        this.selectNode(node);
    }

    var fragment = this.getSelectionFragment();
    text = this.domToWikitext(fragment, { formatCodeBlock: true }).replace(/\s+$/, "");

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

Wysiwyg.prototype.insertCollapsableSection = function (mode) {
    var self = this;
    var range = this.getSelectionRange();
    var html = this.getSelectionHTML();

    function tagsToFragment(node) {
        var close = '', open = '';
        while (node.parentNode && node !== self.contentDocument.body &&
                (node.nodeType !== 1 || node.tagName !== "DIV")) {
            if (node.nodeType === 1) {
                var tagName = node.tagName.toLowerCase();
                close += "</" + tagName + ">";
                open = "<" + tagName + ">" + open;
            }
            node = node.parentNode;
        }
        return [ close, open ];
    }

    var start = tagsToFragment(range.startContainer);
    var end = tagsToFragment(range.endContainer);

    var id = this.generateDomId();
    var classes = "";
    if (mode) { classes = " " + mode; }
    this.insertHTML(start[0] + "<div class='collapsable" + classes + "' id='" + id + "'>" + start[1] + 
    		"<p>section title</p>" + (html ? html : "") + end[0] + "</div>" + end[1]);
    var node = this.contentDocument.getElementById(id);
    if (node) {
        this.selectNode(node.firstChild.nextSibling || node.firstChild);
    }
};

Wysiwyg.prototype.deleteCollapsableSection = function () {
    var pos = this.getSelectionPosition();
    var startCol = $(pos.start).parents("div.collapsable")[0];
    var endCol = $(pos.end).parents("div.collapsable")[0];
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
        var value = attrs[name];
        anchor.setAttribute(name, value);
    }
    anchor.href = link;
    anchor.title = link;
    anchor.setAttribute("data-wysiwyg-link", link);
    if (label) {
        anchor.appendChild(d.createTextNode(label));
    }
    return anchor;
};

Wysiwyg.prototype.createCollapsableSection = function () {
    var collapsible = this.contentDocument.createElement("div");

    $(collapsible).addClass("collapsable");
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
        var button = toolbarButtons[name];
        if (button) {
            var parent = button.parentNode;
            parent.className = (parent.className || "").replace(/ *\bselected\b|$/, status[name] ? " selected" : "");
        }
    }

    if (status["table"]) {
    	$(".wysiwyg-toolbar .non-table").hide();
    	$(".wysiwyg-toolbar .in-table").show();
    } else {
    	$(".wysiwyg-toolbar .in-table").hide();
    	$(".wysiwyg-toolbar .non-table").show();
    }
    
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
    var _linkScheme = "[a-zA-Z][a-zA-Z0-9+-.]*";
    // cf. WikiSystem.XML_NAME, http://www.w3.org/TR/REC-xml/#id
    var _xmlName = "[:_A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD](?:[-:_.A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]*[-_A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD])?";
    var _quotedString = "'[^']+'|" + '"[^"]+"';
    var _wikiPageName = "(?:\\B[\\.<>]|\\b)[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+(?:\\.[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+)*";
    var _wikiTextLink = "\\[\\[(?:.*?)\\]\\[(?:.*?)\\]\\]";
    var wikiInlineRules = [];
    wikiInlineRules.push("'''''");                  // 1. bolditalic -> badly supported by FitNesse parser
    wikiInlineRules.push("'''");                    // 2. bold
    wikiInlineRules.push("''");                     // 3. italic
    wikiInlineRules.push("--");                     // 4. strike
    wikiInlineRules.push("\\{\\{\\{.*?\\}\\}\\}");  // 5. code block -> keep for simplicity
    wikiInlineRules.push("![-<{(\\[]");                // 6. escaped (open)
    wikiInlineRules.push("[->})\\]]!");                // 7. escaped (close)
    wikiInlineRules.push(_wikiTextLink);			// 8. Wiki link
    wikiInlineRules.push(_wikiPageName);            // 9. WikiPageName

    var wikiRules = wikiInlineRules.slice(0);
    // -1. citation
    //wikiRules.push("^(?: *>)+[ \\t\\r\\f\\v]*");
    // -1. header
    wikiRules.push("^[ \\t\\r\\f\\v]*![1-6][ \\t\\r\\f\\v]+.*?(?:#" + _xmlName + ")?[ \\t\\r\\f\\v]*$");
    // -2. list
    wikiRules.push("^[ \\t\\r\\f\\v]*[*-][ \\t\\r\\f\\v]");
    // -3. definition and comment
    wikiRules.push("^(?:![a-z]|#)");
    // -5. leading space
    //wikiRules.push("^[ \\t\\r\\f\\v]+(?=[^ \\t\\r\\f\\v])");
    // -4. closing table row
    wikiRules.push("(?:\\|)[ \\t\\r\\f\\v]*$");
    // -5. cell
    wikiRules.push("!?(?:\\|)");
    // -6: open collapsible section
    wikiRules.push("^!\\*+[<>]?(?:[ \\t\\r\\f\\v]*|[ \\t\\r\\f\\v]+.*)$");
    // -7: close collapsible section
    wikiRules.push("^\\*+!$");

    // TODO could be removed?
    var wikiDetectLinkRules = [ _wikiPageName ];


    var domToWikiInlinePattern = new RegExp("(?:" + wikiInlineRules.join("|") + ")", "g");
    var wikiRulesPattern = new RegExp("(?:(" + wikiRules.join(")|(") + "))", "g");
    var wikiDetectLinkPattern = new RegExp("(?:" + wikiDetectLinkRules.join("|") + ")", "g");

    Wysiwyg.prototype._linkScheme = _linkScheme;
    Wysiwyg.prototype._quotedString = _quotedString;
    Wysiwyg.prototype._wikiPageName = _wikiPageName;
    Wysiwyg.prototype.wikiInlineRules = wikiInlineRules;
    Wysiwyg.prototype.xmlNamePattern = new RegExp("^" + _xmlName + "$");
    Wysiwyg.prototype.domToWikiInlinePattern = domToWikiInlinePattern;
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
    Wysiwyg.prototype.isFirstChildInBlockNode = generator("previousSibling", blocks);
}());

Wysiwyg.prototype.wikitextToFragment = function (wikitext, contentDocument, options) {
    options = options || {};

    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var _linkScheme = this._linkScheme;
    var _quotedString = this._quotedString;
    var wikiInlineRulesCount = this.wikiInlineRules.length;

    var self = this;
    var fragment = contentDocument.createDocumentFragment();
    var holder = fragment;
    var lines = wikitext.split("\n");
    var codeText = null;
    var currentHeader = null;
    var listDepth = [];
    var decorationStatus;
    var decorationStack;
    var indexLines;
    var inCodeBlock, inCollapsibleBlock;
    inCodeBlock = inCollapsibleBlock = false;

    function inParagraph() { return getSelfOrAncestor(holder, "p"); }
    function inDefinition() { return $(holder).parents().andSelf().filter("p.meta, p.comment").get(0); }
    function inTable() { return getSelfOrAncestor(holder, "table"); }
    function inEscapedTable() { return $(holder).parents().andSelf().filter("table.escaped").get(0); }
    function inTableRow() { return getSelfOrAncestor(holder, "tr"); }
    function inAnchor() { return getSelfOrAncestor(holder, "a"); }
    function inEscapedText() { return getSelfOrAncestor(holder, "tt"); }

    function handleCodeBlock(line) {
        if (/^ *\{\{\{ *$/.test(line)) {
            inCodeBlock++;
            if (inCodeBlock === 1) {
                closeParagraph();
                codeText = [];
            } else {
                codeText.push(line);
            }
        } else if (/^ *\}\}\} *$/.test(line)) {
            inCodeBlock--;
            if (inCodeBlock === 0) {
                var pre = contentDocument.createElement("pre");
                pre.className = "wiki";
                pre.appendChild(contentDocument.createTextNode(codeText.join(
                    pre.addEventListener && !window.opera ? "\n" : "\n\r"
                )));
                holder.appendChild(pre);
                codeText = [];
            } else {
                codeText.push(line);
            }
        } else {
            codeText.push(line);
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
        fragment.appendChild(element);
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
    }

    function handleCollapsibleBlock(value) {
        inCollapsibleBlock++;
        closeParagraph();
        var collapsible = self.createCollapsableSection();

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
                $(collapsible).addClass("collapsed");
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
            if (decorationStatus.italic) {
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

    function handleInlineCode(value, length) {
        var d = contentDocument;
        var element = d.createElement("tt");
        element.setAttribute("class", "inlinecode");
        value = value.slice(length, -length);
        if (value.length > 0) {
            element.appendChild(d.createTextNode(value));
            holder.appendChild(element);
        }
    }

    function createAnchor(link, label, autolink) {
        var anchor = self.createAnchor(link, label);
        if (autolink) {
        	anchor.setAttribute("data-wysiwyg-autolink", true);
        }
        holder.appendChild(anchor);
        return anchor;
    }

    function handleLinks(value) {
        var match = handleLinks.pattern.exec(value);

        if (match) {
            var link = match[2];

            var anchor = createAnchor(link);
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

    function handleWikiLink(value) {
        createAnchor(value, value);
    }

    function handleBracketLinks(value) {
        var d = contentDocument;
        var link = value.slice(1, -1);
        var anchor = self.createAnchor(link, link);
        var _holder = holder;
        _holder.appendChild(d.createTextNode("<"));
        _holder.appendChild(anchor);
        _holder.appendChild(d.createTextNode(">"));
    }

    function handleWikiPageName(name, label) {
        if (!inAnchor()) {
            createAnchor(name, label || name, true);
        } else {
            holder.appendChild(contentDocument.createTextNode(label || name));
        }
    }

    function handleWikiAnchor(text) {
        var match = /^\[=#([^ \t\r\f\v\]]+)(?:[ \t\r\f\v]+([^\]]*))?\]$/.exec(text);
        var d = contentDocument;
        var element = d.createElement("span");
        element.className = "wikianchor";
        element.id = match[1];
        if (match[2]) {
            element.appendChild(self.wikitextToOnelinerFragment(match[2], d, self.options));
        }
        holder.appendChild(element);
    }

    function handleList(value) {
        var match = /^(\s*)[*-]\s/.exec(value);
        var className, depth, start;
        if (!match) {
            holder.appendChild(contentDocument.createTextNode(value));
            return;
        }

        depth = match[1].length;

        var last = listDepth.length - 1;
        if (depth > (last >= 0 ? listDepth[last] : -1)) {
            closeToFragment("li");
            openList("ul", className, start, depth);
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
            holder = target.parentNode;
        } else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }

    function handleTableCell(action, escaped) {
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
        // Note: we're not exceeding collapsable section boundries
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
                if (i <= wikiInlineRulesCount) {
                    return i;
                }
                return wikiInlineRulesCount - i;
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
                    && !inParagraph() && !inAnchor() && !currentHeader) {
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
            case 5:     // code block
                if (inEscapedTable() || inEscapedText()) { break; }
                handleInlineCode(matchText, 3);
                continue;
            case 6:     // open escaped
                if (inEscapedText()) { break; }
                openEscapedText(matchText);
                continue;
            case 7:     // close escaped
                closeEscapedText(matchText);
                continue;
            case 8:		// Wiki link
                if (inEscapedTable() || inEscapedText()) { break; }
                handleLinks(matchText);
                continue;
            case 9:		// WikiPageName
                if (inEscapedTable() || inEscapedText()) { break; }
                handleWikiPageName(matchText);
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
                handleList(matchText);
                continue;
            case -3:    // definition (leading "!")
                handleDefinition(matchText);
                break;
            case -4:    // closing table row
                if (inEscapedText()) { break; }
                if (inTable()) {
                    handleTableCell(-1);
                    continue;
                }
                break;
            case -5:    // cell
                if (inEscapedText()) { break; }
                if (!inTable() && match.index === 0) {
                    closeToFragment();
                }
                wikiRulesPattern.lastIndex = prevIndex;
                handleTableCell(inTableRow() ? 0 : 1, /^!/.test(matchText));
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

        if (inEscapedText()) {
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
        if (inCodeBlock || /^ *\{\{\{ *$/.test(line)) {
            handleCodeBlock(line);
            continue;
        }
        if (/^----/.test(line)) {
            closeToFragment();
            fragment.appendChild(contentDocument.createElement("hr"));
            continue;
        }
        if (line.length === 0) {
            closeToFragment();
            continue;
        }
        line = line.replace(/\t/g, "        ");
        line = line.replace(/\u00a0/g, " ");

        decorationStatus = {};
        decorationStack = [];

        handleLine(line);

        if (currentHeader) {
            closeHeader();
        }
        if (inTable()) {
            handleTableCell(-1);
        }
    }
    closeToFragment();

    return fragment;
};

Wysiwyg.prototype.wikitextToOnelinerFragment = function (wikitext, contentDocument, options) {
    var source = this.wikitextToFragment(wikitext, contentDocument, options);
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
    "br": true,
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
    var formatCodeBlock = !!options.formatCodeBlock;

    var self = this;
    var getTextContent = Wysiwyg.getTextContent;
    var getSelfOrAncestor = Wysiwyg.getSelfOrAncestor;
    var wikiOpenTokens = this.wikiOpenTokens;
    var wikiCloseTokens = this.wikiCloseTokens;
    var wikiInlineTags = this.wikiInlineTags;
    var wikiBlockTags = this.wikiBlockTags;
    var xmlNamePattern = this.xmlNamePattern;
    var domToWikiInlinePattern = this.domToWikiInlinePattern;
    var wikiPageNamePattern = new RegExp("^" + this._wikiPageName + "$");
    var decorationTokenPattern = /^(?:'''|'')$/;

    var texts = [];
    var stack = [];
    var last = root;
    var listDepth = 0;
    var quoteCitation = false;
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
            decorations.push(token);
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
        if (!/\"/.test(label)) {
            return "[[" + label + ']["' + link + '"]]';
        }
        if (!/\'/.test(label)) {
            return "[[" + label + "]['" + link + "']]";
        }
        return "[[" + label.replace(/"+/g, "") + ' ]["' + link + '"]]';
    }

    function pushAnchor(node, bracket) {
        var link = node.getAttribute("data-wysiwyg-link");
        var autolink = node.getAttribute("data-wysiwyg-autolink");

        link = (link || node.href).replace(/^\s+|\s+$/g, "");
        // Obtain label as text content. Allow to render special attributes
        //var label = getTextContent(node).replace(/^\s+|\s+$/g, "");
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
        pushTextWithDecorations(text, node);
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
            if (name === "table" && $(node).hasClass("escaped")) {
                _texts.push("!");
            }
        } else {
            var value, text;
            switch (name) {
            case "#text":
                value = node.nodeValue;
                if (value) {
                    if (!inCodeBlock) {
                        if (value && !self.isInlineNode(node.previousSibling || node.parentNode)) {
                            value = value.replace(/^[ \t\r\n\f\v]+/g, "");
                        }
                        if (value && !self.isInlineNode(node.nextSibling || node.parentNode)) {
                            value = value.replace(/[ \t\r\n\f\v]+$/g, "");
                        }
                        value = value.replace(/\r?\n/g, " ");
                    }
                    if (value) {
                        var length = _texts.length;
                        var prev = length > 0 ? _texts[length - 1] : null;
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
                    var start = container.getAttribute("start") || "";
                    if (start !== "1" && /^(?:[0-9]+|[a-zA-Z]|[ivxIVX]{1,5})$/.test(start)) {
                        _texts.push(start, ". ");
                    } else {
                        switch (container.className) {
                        case "arabiczero":
                            _texts.push("0. ");
                            break;
                        case "lowerroman":
                            _texts.push("i. ");
                            break;
                        case "upperroman":
                            _texts.push("I. ");
                            break;
                        case "loweralpha":
                            _texts.push("a. ");
                            break;
                        case "upperalpha":
                            _texts.push("A. ");
                            break;
                        default:
                            _texts.push("1. ");
                            break;
                        }
                    }
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
                if (!self.isBogusLineBreak(node)) {
                    value = null;
                    if (inCodeBlock) {
                        value = "\n";
                    } else {
                        value = " ";
                    }
                    _texts.push(value);
                }
                break;
            case "pre":
                _texts.push("\n{{{\n");
                inCodeBlock = true;
                break;
            case "th":
            case "td":
                skipNode = node;
                _texts.push("|");
                text = self.domToWikitext(node, self.options).replace(/^ +| +$/g, "");
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
                        'hashtable': [ "!{", "}!" ],
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
                if ($(node).hasClass("collapsable")) {
                    _texts.push("!***");
                    if ($(node).hasClass("collapsed")) {
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
                if ($(node).hasClass('meta')) {
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
                        text = "\n}}}\n";
                    } else if (nextSibling.tagName.toLowerCase() === "pre") {
                        text = "\n}}}";
                    } else {
                        text = "\n}}}\n";
                    }
                    if (text.slice(-1) === "\n") {
                        text += listDepth > 0 ? " " + string("  ", listDepth) : "    ";
                    }
                } else {
                    text = "\n}}}\n";
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
                if ($(node).hasClass("collapsable")) {
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
    return texts.join("").replace(/^(?: *\n)+|(?: *\n)+$/g, "");
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
            var d = this.contentDocument;
            var event = d.createEvent("KeyboardEvent");
            event.initKeyEvent("keypress", true, true, null, false, false, true, false, 0x000d, 0);
            d.body.dispatchEvent(event);
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
                tmp.setStart(d.body, 0);
                tmp.setEnd(d.body, 0);
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
    Wysiwyg.prototype.isBogusLineBreak = function (node) { return false; };
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
        this.contentWindow.focus();
        var d = this.contentDocument;
        var range = d.selection.createRange();
        var node = range.item ? range.item(0) : range.parentElement();
        return node.ownerDocument === d ? node : null;
    };
    Wysiwyg.prototype.selectNode = function (node) {
        var d = this.contentDocument;
        var body = d.body;
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
        var range = d.body.createTextRange();
        range.moveToElementText(node);
        range.select();
    };
    Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) {
        var d = this.contentDocument;
        var body = d.body;
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
        var body = this.contentDocument.body;
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
        this.contentWindow.focus();
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
        this.contentWindow.focus();
        var d = this.contentDocument;
        var body = d.body;
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
        this.contentWindow.focus();
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
        this.contentWindow.focus();
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
    Wysiwyg.prototype.insertTableCell = function (row, index) { return null; };
    Wysiwyg.prototype.getFocusNode = function () { return null; };
    Wysiwyg.prototype.selectNode = function (node) { };
    Wysiwyg.prototype.selectNodeContents = function (node) { return null; };
    Wysiwyg.prototype.selectRange = function (start, startOffset, end, endOffset) { };
    Wysiwyg.prototype.getSelectionRange = function () { return null; };
    Wysiwyg.prototype.getNativeSelectionRange = function () { return null; };
    Wysiwyg.prototype.getSelectionText = function () { return null; };
    Wysiwyg.prototype.getSelectionHTML = function () { return null; };
    Wysiwyg.prototype.getSelectionFragment = function () { return null; };
    Wysiwyg.prototype.getSelectionPosition = function () { return null; };
    Wysiwyg.prototype.expandSelectionToElement = function (arg) { };
    Wysiwyg.prototype.selectionContainsTagName = function (name) { return false; };
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

Wysiwyg.getStylePaths = function () {
    var stylesheets = [];
    var paths = { stylesheets: stylesheets, base: '/' };

    var d = document;
    var head = d.getElementsByTagName("head")[0];
    var links = head.getElementsByTagName("link");
    var length = links.length;
    var i;
    for (i = 0; i < length; i++) {
        var link = links[i];
        var href = link.getAttribute("href") || "";
        var type = link.getAttribute("type") || "";
        switch ((link.getAttribute("rel") || "").toLowerCase()) {
        case "wysiwyg.base":
            paths.base = href;
            break;
        case "wysiwyg.stylesheet":
            stylesheets.push(href);
            break;
        }
    }
    if (paths.base && stylesheets.length > 0) {
        return paths;
    }
    return null;
};

Wysiwyg.getOptions = function () {
    var options = {};
    if (window._wysiwyg) {
        options = _wysiwyg;
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

Wysiwyg.setEditorMode = function (mode) {
    switch (mode) {
    case "wysiwyg":
        break;
    default:    // "textarea"
        mode = "textarea";
        break;
    }
    Wysiwyg.editorMode = mode;
    Wysiwyg.setCookie("wysiwyg", mode);
};

Wysiwyg.setCookie = function (key, val) {
	var expires, pieces;
    var now = new Date();
    if (!/\/$/.test(Wysiwyg.paths.base)) {
        expires = new Date(now.getTime() - 86400000);
        pieces = [ key +"=",
            "path=" + Wysiwyg.paths.base + "/",
            "expires=" + expires.toUTCString() ];
        document.cookie = pieces.join("; ");
    }
    expires = new Date(now.getTime() + 365 * 86400 * 1000);
    pieces = [ key + "=" + val,
        "path=" + Wysiwyg.paths.base,
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
        style[name] = object[name];
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

Wysiwyg.elementPosition = function (element) {
    function vector(left, top) {
        var value = [ left, top ];
        value.left = left;
        value.top = top;
        return value;
    }
    var position = Wysiwyg.getStyle(element, "position");
    var left = 0, top = 0;
    var node;
    for (node = element; node; node = node.offsetParent) {
        left += node.offsetLeft || 0;
        top += node.offsetTop || 0;
    }
    if (position !== "absolute") {
        return vector(left, top);
    }
    var offset = Wysiwyg.elementPosition(element.offsetParent);
    return vector(left - offset.left, top - offset.top);
};

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
        return function (element) { return null; };
    }
})();

Wysiwyg.initialize = function () {
    if ("replace".replace(/[a-e]/g, function (m) { return "*"; }) !== "r*pl***") {
        return;
    }
    if (typeof document.designMode === undefined) {
        return;
    }
    Wysiwyg.paths = Wysiwyg.getStylePaths();
    if (!Wysiwyg.paths) {
        return;
    }
    var options = Wysiwyg.getOptions();
    var textareas = document.getElementsByTagName("textarea");
    var editors = [];
    var i;
    for (i = 0; i < textareas.length; i++) {
        var textarea = textareas[i];
        if (/\bwikitext\b/.test(textarea.className || "")) {
            editors.push(Wysiwyg.newInstance(textarea, options));
        }
    }
    return editors;
};

// vim:et:ai:ts=4
