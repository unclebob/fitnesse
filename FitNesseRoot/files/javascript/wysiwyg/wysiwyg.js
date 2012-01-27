/****

 TODO:
 - Menu button for creating a Collapsable area (containing selected text?)
 - Menu for removing Collapsible area. Text is added in parent node.
 - Allow for toggle default (open/closed/hidden)
 - Edit icons
 - When adding/removing cell, make sure colspan of the other rows is correct
 - Do inline escaping in links
 - Test copy/paste in rich text editor
 - How to make editing tables simpler in the editor? It's the core of the system basically (tables).
 - Figure out a way (menu button?) to handle escaping text (!-..-!, !<..>!)

 ****/
 
var TracWysiwyg = function(textarea, options) {
    var self = this;
    var editorMode = TracWysiwyg.getEditorMode();

    this.autolink = true;
    this.wrapTextarea = false;
    this.textarea = textarea;
    this.options = options = options || {};
    var wikitextToolbar = null;
    var textareaResizable = null;
    if (wikitextToolbar && (wikitextToolbar.nodeType != 1 || wikitextToolbar.className != "wikitoolbar")) {
        wikitextToolbar = null;
    }
    this.textareaResizable = textareaResizable;
    this.wikitextToolbar = wikitextToolbar;

    this.createEditable(document, textarea, textareaResizable);
    var frame = this.frame;
    var resizable = this.resizable;

    this.contentWindow = frame.contentWindow;
    this.contentDocument = this.contentWindow.document;

    this.initializeEditor(this.contentDocument);
    this.wysiwygToolbar = this.createWysiwygToolbar(document);
    this.styleMenu = this.createStyleMenu(document);
    this.dialogWindow = this.createDialogWindow(document);
    this.decorationMenu = this.createDecorationMenu(document);
    this.tableMenu = this.createTableMenu(document);
    this.menus = [ this.styleMenu, this.decorationMenu, this.tableMenu ];
    this.toolbarButtons = this.setupMenuEvents();
    this.toggleEditorButtons = null;
    this.autolinkButton = null;
    this.wrapTextareaButton = null;
    this.savedWysiwygHTML = null;

    this.setupToggleEditorButtons();
    this.setupSyncTextAreaHeight();

    var styleStatic = { position: "static", left: "-9999px", top: "-9999px" };
    var styleAbsolute = { position: "absolute", left: "-9999px", top: "-9999px" };
    switch (editorMode) {
    case "textarea":
        TracWysiwyg.setStyle(textareaResizable || textarea, styleStatic);
        if (wikitextToolbar) {
            TracWysiwyg.setStyle(wikitextToolbar, styleStatic);
        }
        TracWysiwyg.setStyle(resizable || frame, { position: "absolute",
            left: "-9999px", top: TracWysiwyg.elementPosition(textareaResizable || textarea).top + "px" });
        TracWysiwyg.setStyle(this.wysiwygToolbar, styleAbsolute);
        TracWysiwyg.setStyle(this.autolinkButton.parentNode, { display: "none" });
        TracWysiwyg.setStyle(this.wrapTextareaButton.parentNode, { display: "" });
        textarea.setAttribute("tabIndex", "");
        frame.setAttribute("tabIndex", "-1");
        break;
    case "wysiwyg":
        TracWysiwyg.setStyle(textareaResizable || textarea, { position: "absolute",
            left: "-9999px", top: TracWysiwyg.elementPosition(textareaResizable || textarea).top + "px" });
        if (wikitextToolbar) {
            TracWysiwyg.setStyle(wikitextToolbar, styleAbsolute);
        }
        TracWysiwyg.setStyle(resizable || frame, styleStatic);
        TracWysiwyg.setStyle(this.wysiwygToolbar, styleStatic);
        TracWysiwyg.setStyle(this.autolinkButton.parentNode, { display: "" });
        TracWysiwyg.setStyle(this.wrapTextareaButton.parentNode, { display: "none" });
        textarea.setAttribute("tabIndex", "-1");
        frame.setAttribute("tabIndex", "");
        break;
    }

    var body = document.body;
    for (var i = 0; i < this.menus.length; i++) {
        body.insertBefore(this.menus[i], body.firstChild);
    }
    var element = wikitextToolbar || textareaResizable || textarea;
    element.parentNode.insertBefore(this.toggleEditorButtons, element);
    element.parentNode.insertBefore(this.wysiwygToolbar, element);
    element.parentNode.insertBefore(this.dialogWindow, element);

    function lazySetup() {
        if (self.contentDocument.body) {
            var exception;
            try { self.execCommand("useCSS", false); } catch (e) { }
            try { self.execCommand("styleWithCSS", false); } catch (e) { }
            if (editorMode == "wysiwyg") {
                try { self.loadWysiwygDocument() } catch (e) { exception = e }
            }
            self.setupEditorEvents();
            self.setupFormEvent();
            if (exception) {
                (self.textareaResizable || self.textarea).style.position = "static";
                if (self.wikitextToolbar) {
                    self.wikitextToolbar.style.position = "static";
                }
                (self.resizable || self.frame).style.position = self.wysiwygToolbar.style.position = "absolute";
                self.autolinkButton.parentNode.style.display = "none";
                self.wrapTextareaButton.parentNode.style.display = "none";
                alert("Failed to activate the wysiwyg editor.");
                throw exception;
            }
        }
        else {
            setTimeout(lazySetup, 100);
        }
    }
    lazySetup();
};

TracWysiwyg.prototype.initializeEditor = function(d) {
    var l = window.location;
    var html = [];
    html.push(
        '<!DOCTYPE html PUBLIC',
        ' "-//W3C//DTD XHTML 1.0 Transitional//EN"',
        ' "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n',
        '<html xmlns="http://www.w3.org/1999/xhtml">',
        '<head>',
        '<base href="', l.protocol, '//', l.host, '/" />',
        '<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />');
    var stylesheets = TracWysiwyg.tracPaths.stylesheets;
    if (!stylesheets) {
        // Work around wysiwyg stops with Agilo
        var base = TracWysiwyg.tracPaths.base.replace(/\/*$/, "/");
        //stylesheets = [ base + "chrome/common/css/trac.css", base + "chrome/wysiwyg/editor.css" ];
        stylesheets = [ base + "chrome/common/css/trac.css", "editor.css" ];
    }
    var length = stylesheets.length;
    for (var i = 0; i < length; i++) {
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
        if (d != this.contentWindow.document) {
            this.contentDocument = this.contentWindow.document;
        }
    }
    // disable firefox table resizing
    d.execCommand("enableObjectResizing", false, "false");
    d.execCommand("enableInlineTableEditing", false, "false");
};

TracWysiwyg.prototype.toggleAutolink = function() {
    this.autolink = !this.autolink;
    this.autolinkButton.checked = this.autolink;
};

TracWysiwyg.prototype.listenerToggleAutolink = function(input) {
    var self = this;
    return function(event) {
        self.autolink = input.checked;
    };
};

TracWysiwyg.prototype.toggleWrapTextarea = function() {
    this.wrapTextarea = !this.wrapTextarea;
    this.wrapTextareaButton.checked = this.wrapTextarea;
};

TracWysiwyg.prototype.listenerToggleWrapTextarea = function(input) {
    var self = this;

    function setWrap(wrap) {
        if (self.textarea.wrap) {
            self.textarea.wrap = wrap ? 'soft' : 'off';
        } else { // wrap attribute not supported - try Mozilla workaround
            self.textarea.setAttribute('wrap', wrap ? 'soft' : 'off');
            var newarea= self.textarea.cloneNode(true);
            newarea.value= self.textarea.value;
            self.textarea.parentNode.replaceChild(newarea, self.textarea);
            self.textarea = newarea;
        }
        if (wrap) {
           $(self.textarea).removeClass('no_wrap');
        } else {
            $(self.textarea).addClass('no_wrap');
        }
    }
    
    return function(event) {
        self.wrapTextarea = input.checked;
        setWrap(input.checked);
    };
};

TracWysiwyg.prototype.listenerToggleEditor = function(type) {
    var self = this;

    switch (type) {
    case "textarea":
        return function(event) {
            var textarea = self.textareaResizable || self.textarea;
            if (textarea.style.position == "absolute") {
                self.hideAllMenus();
                self.loadTracWikiText();
                textarea.style.position = "static";
                self.textarea.setAttribute("tabIndex", "");
                if (self.wikitextToolbar) {
                    self.wikitextToolbar.style.position = "static";
                }
                self.syncTextAreaHeight();
                (self.resizable || self.frame).style.position = self.wysiwygToolbar.style.position = "absolute";
                self.frame.setAttribute("tabIndex", "-1");
                self.autolinkButton.parentNode.style.display = "none";
                self.wrapTextareaButton.parentNode.style.display = "";
                TracWysiwyg.setEditorMode(type);
            }
            self.focusTextarea();
        };
    case "wysiwyg":
        return function(event) {
            var frame = self.resizable || self.frame;
            if (frame.style.position == "absolute") {
                try {
                    self.loadWysiwygDocument();
                }
                catch (e) {
                    TracWysiwyg.stopEvent(event || window.event);
                    alert("Failed to activate the wysiwyg editor.");
                    throw e;
                }
                (self.textareaResizable || self.textarea).style.position = "absolute";
                self.textarea.setAttribute("tabIndex", "-1");
                if (self.wikitextToolbar) {
                    self.wikitextToolbar.style.position = "absolute";
                }
                frame.style.position = self.wysiwygToolbar.style.position = "static";
                self.frame.setAttribute("tabIndex", "");
                self.autolinkButton.parentNode.style.display = "";
                self.wrapTextareaButton.parentNode.style.display = "none";
                TracWysiwyg.setEditorMode(type);
            }
            self.focusWysiwyg();
        };
    }
};

TracWysiwyg.prototype.activeEditor = function() {
    return this.textarea.style.position == "absolute" ? "wysiwyg" : "textarea";
};

TracWysiwyg.prototype.setupFormEvent = function() {
    var self = this;

    function listener(event) {
        var textarea = self.textareaResizable || self.textarea;
        try {
            if (textarea.style.position == "absolute") {
                var body = self.contentDocument.body;
                if (self.savedWysiwygHTML !== null && body.innerHTML != self.savedWysiwygHTML) {
                    self.textarea.value = self.domToWikitext(body, self.options);
                }
            }
        }
        catch (e) {
            TracWysiwyg.stopEvent(event || window.event);
        }
    }
    $(this.textarea.form).submit(listener);
};

TracWysiwyg.prototype.createEditable = function(d, textarea, textareaResizable) {
    var self = this;
    var getStyle = TracWysiwyg.getStyle;
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

    if (textareaResizable) {
        var offset = null;
        var offsetFrame = null;
        var contentDocument = null;
        var grip = d.createElement("div");
        grip.className = "trac-grip";
        if (/^[0-9]+$/.exec(dimension.width)) {
            grip.style.width = dimension.width + "px";
        }
        $(grip).mousedown(beginDrag);
        wrapper.appendChild(grip);
        var resizable = d.createElement("div");
        resizable.className = "trac-resizable";
        resizable.appendChild(wrapper);
        grip.style.marginLeft = (frame.offsetLeft - grip.offsetLeft) + 'px';
        grip.style.marginRight = (grip.offsetWidth - frame.offsetWidth) +'px';
        this.resizable = resizable;
        textareaResizable.parentNode.insertBefore(resizable, textareaResizable.nextSibling);
    }
    else {
        textarea.parentNode.insertBefore(frame, textarea.nextSibling);
    }

    function beginDrag(event) {
        offset = frame.height - event.pageY;
        contentDocument = self.contentDocument;
        frame.blur();
        $(d).mousemove(dragging);
        $(d).mouseup(endDrag);
        $(contentDocument).mousemove(draggingForFrame);
        $(contentDocument).mouseup(endDrag);
    }

    var topPageY = 0, framePageY = 0;
    function dragging(event) {
        var height = Math.max(32, offset + event.pageY);
        textarea.style.height = height + "px";
        frame.height = height;
    }

    function draggingForFrame(event) {
        var height = Math.max(32, event.clientY);
        textarea.style.height = height + "px";
        frame.height = height;
    }

    function endDrag(event) {
        self.focusWysiwyg();
        TracWysiwyg.removeEvent(d, "mousemove", dragging);
        TracWysiwyg.removeEvent(d, "mouseup", endDrag);
        TracWysiwyg.removeEvent(contentDocument, "mousemove", draggingForFrame);
        TracWysiwyg.removeEvent(contentDocument, "mouseup", endDrag);
    }

    function getDimension(textarea) {
        var width = textarea.offsetWidth;
        if (width) {
            var parentWidth = textarea.parentNode.offsetWidth
                            + parseInt(getStyle(textarea, 'borderLeftWidth'), 10)
                            + parseInt(getStyle(textarea, 'borderRightWidth'), 10);
            if (width == parentWidth) {
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
            if (textareaResizable) {
                grip.style.width = /^[0-9]+$/.exec(dimension.width) ? dimension.width + "px" : dimension.width;
            }
            return;
        }
        setTimeout(lazy, 100);
    }
};

TracWysiwyg.prototype.createWysiwygToolbar = function(d) {
    var html = [
        '<ul>',
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
        '<li title="Bold (Ctrl+B)"><a id="wt-strong" href="#"></a></li>',
        '<li title="Italic (Ctrl+I)"><a id="wt-em" href="#"></a></li>',
        '<li title="Monospace"><a id="wt-monospace" href="#"></a></li>',
        '<li><a id="wt-decorationmenu" href="#"></a></li>',
        '<li title="Remove format"><a id="wt-remove" href="#"></a></li>',
        '<li title="Link"><a id="wt-link" href="#"></a></li>',
        '<li title="Unlink"><a id="wt-unlink" href="#"></a></li>',
        '<li title="Table"><a id="wt-table" href="#"></a></li>',
        '<li><a id="wt-tablemenu" href="#"></a></li>',
        '<li title="Horizontal rule"><a id="wt-hr" href="#"></a></li>',
        '<li title="Line break (Shift+Enter)"><a id="wt-br" href="#"></a></li>',
        '</ul>' ];
    var div = d.createElement("div");
    div.className = "wysiwyg-toolbar";
    div.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return div;
};

TracWysiwyg.prototype.createDialogWindow = function(d) {
	var html = [
		'<p>Autocomplete for Fitnesse commands</p>'+
		'<input id="autocomplete" type="text" />' ];
  		
  	var dialog = d.createElement("div")
  	dialog.id = "dialog";
  	dialog.style.display = "none";
  	dialog.title = 'Autocomplete dialog';
  	dialog.innerHTML = html.join("");
  	
  	return dialog;
};

TracWysiwyg.prototype.createStyleMenu = function(d) {
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
    TracWysiwyg.setStyle(menu, { position: "absolute", left: "-1000px", top: "-1000px", zIndex: 1000 });
    menu.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return menu;
};

TracWysiwyg.prototype.createDecorationMenu = function(d) {
    var html = [
        '<ul class="menu">',
        '<li><a id="wt-strike" href="#">Strike through</a></li>',
        '<li><a id="wt-sup" href="#">Superscript</a></li>',
        '<li><a id="wt-sub" href="#">Subscript</a></li>',
        '</ul>' ];
    var menu = d.createElement("div");
    menu.className = "wysiwyg-menu";
    TracWysiwyg.setStyle(menu, { position: "absolute", left: "-1000px", top: "-1000px", zIndex: 1000 });
    menu.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return menu;
};

TracWysiwyg.prototype.createTableMenu = function(d) {
    var html = [
        '<ul class="menu">',
        '<li><a id="wt-insert-cell-before" href="#">Insert cell before</a></li>',
        '<li><a id="wt-insert-cell-after" href="#">Insert cell after</a></li>',
        '<li><a id="wt-insert-row-before" href="#">Insert row before</a></li>',
        '<li><a id="wt-insert-row-after" href="#">Insert row after</a></li>',
        '<li><a id="wt-insert-col-before" href="#">Insert column before</a></li>',
        '<li><a id="wt-insert-col-after" href="#">Insert column after</a></li>',
        '<li><a id="wt-delete-cell" href="#">Delete cell</a></li>',
        '<li><a id="wt-delete-row" href="#">Delete row</a></li>',
        '<li><a id="wt-delete-col" href="#">Delete column</a></li>',
        '</ul>' ];
    var menu = d.createElement("div");
    menu.className = "wysiwyg-menu";
    TracWysiwyg.setStyle(menu, { position: "absolute", left: "-1000px", top: "-1000px", zIndex: 1000 });
    menu.innerHTML = html.join("").replace(/ href="#">/g, ' href="#" onmousedown="return false" tabindex="-1">');
    return menu;
};

TracWysiwyg.prototype.setupMenuEvents = function() {
    function addToolbarEvent(element, self, args) {
        var method = args.shift();
        $(element).click(function(event) {
            var w = self.contentWindow;
            TracWysiwyg.stopEvent(event || w.event);
            var keepMenus = false, exception;
            try { keepMenus = method.apply(self, args) } catch (e) { exception = e }
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
        case "style":       return [ self.toggleMenu, self.styleMenu, element ];
        case "strong":      return [ self.execDecorate, "bold" ];
        case "em":          return [ self.execDecorate, "italic" ];
        case "underline":   return [ self.execDecorate, "underline" ];
        case "strike":      return [ self.execDecorate, "strikethrough" ];
        case "sub":         return [ self.execDecorate, "subscript" ];
        case "sup":         return [ self.execDecorate, "superscript" ];
        case "monospace":   return [ self.execDecorate, "monospace" ];
        case "decorationmenu":  return [ self.toggleMenu, self.decorationMenu, element ];
        case "remove":      return [ self.execCommand, "removeformat" ];
        case "paragraph":   return [ self.formatParagraph ];
        case "heading1":    return [ self.formatHeaderBlock, "h1" ];
        case "heading2":    return [ self.formatHeaderBlock, "h2" ];
        case "heading3":    return [ self.formatHeaderBlock, "h3" ];
        case "heading4":    return [ self.formatHeaderBlock, "h4" ];
        case "heading5":    return [ self.formatHeaderBlock, "h5" ];
        case "heading6":    return [ self.formatHeaderBlock, "h6" ];
        case "link":        return [ self.createLink ];
        case "unlink":      return [ self.execCommand, "unlink" ];
        case "table":       return [ self.insertTable ];
        case "tablemenu":   return [ self.toggleMenu, self.tableMenu, element ];
        case "insert-cell-before":   return [ self.insertTableCell_, false ];
        case "insert-cell-after":    return [ self.insertTableCell_, true ];
        case "insert-row-before":   return [ self.insertTableRow, false ];
        case "insert-row-after":    return [ self.insertTableRow, true ];
        case "insert-col-before":   return [ self.insertTableColumn, false ];
        case "insert-col-after":    return [ self.insertTableColumn, true ];
        case "delete-cell":  return [ self.deleteTableCell ];
        case "delete-row":  return [ self.deleteTableRow ];
        case "delete-col":  return [ self.deleteTableColumn ];
        case "code":        return [ self.formatCodeBlock ];
        case "hr":          return [ self.insertHorizontalRule ];
        case "br":          return [ self.insertLineBreak ];
        }
        return null;
    }

    function setup(container) {
        var elements = container.getElementsByTagName("a");
        var length = elements.length;
        for (var i = 0; i < length; i++) {
            var element = elements[i];
            var name = element.id.replace(/^wt-/, "");
            var args = argsByType(this, name, element);
            if (args) {
                addToolbarEvent(element, this, args);
                buttons[name] = element;
            }
        }
    }

    var buttons = {};
    setup.call(this, this.wysiwygToolbar);
    for (var i = 0; i < this.menus.length; i++) {
        setup.call(this, this.menus[i]);
    }
    return buttons;
};

TracWysiwyg.prototype.toggleMenu = function(menu, element) {
    if (parseInt(menu.style.left, 10) < 0) {
        this.hideAllMenus(menu);
        var position = TracWysiwyg.elementPosition(element);
        TracWysiwyg.setStyle(menu, { left: position[0] + "px", top: (position[1] + 18) + "px" });
    }
    else {
        this.hideAllMenus();
    }
    return true;
};

TracWysiwyg.prototype.hideAllMenus = function(except) {
    var menus = this.menus;
    var length = menus.length;
    for (var i = 0; i < length; i++) {
        if (menus[i] != except) {
            TracWysiwyg.setStyle(menus[i], { left: "-1000px", top: "-1000px" });
        }
    }
};

TracWysiwyg.prototype.execDecorate = function(name) {
    if (this.selectionContainsTagName("pre")) {
        return;
    }
    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
    var position = this.getSelectionPosition();
    var ancestor = {};
    ancestor.start = getSelfOrAncestor(position.start, /^(?:a|tt)$/);
    ancestor.end = getSelfOrAncestor(position.end, /^(?:a|tt)$/);
    this.expandSelectionToElement(ancestor);

    if (name != "monospace") {
        this.execCommand(name);
    }
    else {
        this.execDecorateMonospace();
    }
    this.selectionChanged();
};

TracWysiwyg.prototype.execDecorateMonospace = function() {
    var html = this.getSelectionHTML();
    var removePattern = /<tt.*?>|<\/tt>/gi;
    if (/^<tt.*?>/i.test(html) && /<\/tt>$/i.test(html)) {
        html = html.replace(removePattern, "");
    }
    else {
        var id = this.generateDomId();
        html = '<tt id="' + id + '">' + html.replace(removePattern, "") + "</tt>";
    }
    this.insertHTML(html);
    var node = this.contentDocument.getElementById(id);
    if (node) {
        this.selectNode(node);
    }
};

TracWysiwyg.prototype.execCommand = function(name, arg) {
    return this.contentDocument.execCommand(name, false, arg);
};

TracWysiwyg.prototype.setupEditorEvents = function() {
    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
    var self = this;
    var d = this.contentDocument;
    var w = this.contentWindow;
    var ime = false;

    function listenerKeydown(event) {
        var method = null;
        var args = null;
        event = event || self.contentWindow.event;
        var keyCode = event.keyCode;
        switch (keyCode) {
        case 0x09:  // TAB
            var range = self.getSelectionRange();
            var stop = false;
            var element = getSelfOrAncestor(range.startContainer, /^(?:pre|table)$/);
            if (element) {
                switch (element.tagName.toLowerCase()) {
                case "pre":
                    self.insertHTML("\t");
                    stop = true;
                    break;
                case "table":
                    if (getSelfOrAncestor(range.endContainer, "table") == element) {
                        self.moveFocusInTable(!event.shiftKey);
                        self.selectionChanged();
                        stop = true;
                    }
                    break;
                }
            }
            if (stop) {
                TracWysiwyg.stopEvent(event);
            }
            return;
        case 0xe5:
            ime = true;
            break;
        }
        switch ((keyCode & 0x00fffff) | (event.ctrlKey ? 0x40000000 : 0)
            | (event.shiftKey ? 0x20000000 : 0) | (event.altKey ? 0x10000000 : 0))
        {
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
            TracWysiwyg.stopEvent(event);
            method.apply(self, args);
            self.selectionChanged();
        }
        else if (keyCode) {
            var focus = self.getFocusNode();
            if (!getSelfOrAncestor(focus, /^(?:p|h[1-6]|t[dh]|pre)$/)) {
                self.execCommand("formatblock", "<p>");
            }
        }
    }
    $(d).keydown(listenerKeydown);

    function listenerKeypress(event) {
        event = event || self.contentWindow.event;
        var modifier = (event.ctrlKey ? 0x40000000 : 0)
            | (event.shiftKey ? 0x20000000 : 0) | (event.altKey ? 0x10000000 : 0);
        switch (event.charCode || event.keyCode) {
        case 0x20:  // SPACE
            switch (modifier) {
            case 0:
                self.detectTracLink(event);
                break;
            case 0x20000000:    // Shift
            	self.showAutoCompleteOnShiftSpace(event);
            	// prevent space from being entered in table
            	TracWysiwyg.stopEvent(event);
                break;
            }            
            return;
        case 0x3e:  // ">"
            self.detectTracLink(event);
            return;
        case 0x0d:  // ENTER
            self.detectTracLink(event);
            switch (modifier) {
            case 0:
                if (self.insertParagraphOnEnter) {
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
        }
    }
    $(d).keypress(listenerKeypress);

    function listenerKeyup(event) {
        var keyCode = event.keyCode;
        if (ime) {
            switch (keyCode) {
            case 0x20:  // SPACE
                self.detectTracLink(event);
                break;
            }
            ime = false;
        }
        self.updateElementClassName(self.getSelectionRange().startContainer);
        self.selectionChanged();
    }
    $(d).keyup(listenerKeyup);

    function listenerMouseup(event) {
        self.selectionChanged();
    }
    $(d).mouseup(listenerMouseup);

    function listenerClick(event) {
        self.hideAllMenus();
        self.selectionChanged();
    }
    $(d).click(listenerClick);
};

TracWysiwyg.prototype.loadWysiwygDocument = function() {
    var d = this.contentDocument;
    var container = d.body;
    var tmp;

    while (tmp = container.lastChild) {
        container.removeChild(tmp);
    }
    var fragment = this.wikitextToFragment(this.textarea.value, d, this.options);
    container.appendChild(fragment);
    this.savedWysiwygHTML = container.innerHTML;
};

TracWysiwyg.prototype.focusWysiwyg = function() {
    var self = this;
    var w = this.contentWindow;
    function lazy() {
        w.focus();
        try { self.execCommand("useCSS", false); } catch (e) { }
        try { self.execCommand("styleWithCSS", false); } catch (e) { }
        self.selectionChanged();
    }
    setTimeout(lazy, 10);
};

TracWysiwyg.prototype.loadTracWikiText = function() {
    this.textarea.value = this.domToWikitext(this.contentDocument.body, this.options);
    this.savedWysiwygHTML = null;
};

TracWysiwyg.prototype.focusTextarea = function() {
    this.textarea.focus();
};

TracWysiwyg.prototype.setupToggleEditorButtons = function() {
    var div = document.createElement("div");
    var mode = TracWysiwyg.editorMode;
    var html = ''
        + '<label for="editor-autolink-@" title="Links as you type (Ctrl-L)">'
        + '<input type="checkbox" id="editor-autolink-@" checked="checked" />'
        + 'autolink </label>'
        + '<label for="editor-wrap-@" title="Turns on/off wrapping">'
        + '<input type="checkbox" id="editor-wrap-@" />'
        + 'wrap </label>'
        + '<label for="editor-wysiwyg-@">'
        + '<input type="radio" name="__EDITOR__@" value="wysiwyg" id="editor-wysiwyg-@" '
        + (mode == "wysiwyg" ? 'checked="checked"' : '') + ' />'
        + 'rich text</label> '
        + '<label for="editor-textarea-@">'
        + '<input type="radio" name="__EDITOR__@" value="textarea" id="editor-textarea-@" '
        + (mode == "textarea" ? 'checked="checked"' : '') + ' />'
        + 'plain text</label> '
        + '&nbsp; ';
    div.className = "editor-toggle";
    div.innerHTML = html.replace(/@/g, ++TracWysiwyg.count);
    this.toggleEditorButtons = div;

    var buttons = div.getElementsByTagName("input");
    for (var i = 0; i < buttons.length; i++) {
        var button = buttons[i];
        var token = button.id.replace(/[0-9]+$/, "@");
        switch (token) {
        case 'editor-autolink-@':
            var listener = this.listenerToggleAutolink(button);
            $(button).click(listener);
            $(button).keypress(listener);
            this.autolinkButton = button;
            break;
        case "editor-wrap-@":
            var listener = this.listenerToggleWrapTextarea(button);
            $(button).click(listener);
            $(button).keypress(listener);
            this.wrapTextareaButton = button;
            break;
        case "editor-wysiwyg-@":
        case "editor-textarea-@":
            $(button).click(this.listenerToggleEditor(button.value));
            break;
        }
    }
};

TracWysiwyg.prototype.setupSyncTextAreaHeight = function() {
    var self = this;
    var d = document;
    var timer = null;

    var editrows = document.getElementById("editrows");
    if (editrows) {
        $(editrows).change(changeHeight);
    }
    if (this.textareaResizable) {
        $(this.textarea.nextSibling).mousedown(beginDrag);
    }

    function changeHeight() {
        if (timer !== null) {
            clearTimeout(timer);
        }
        setTimeout(sync, 10);
    }

    function beginDrag(event) {
        $(d).mousemove(changeHeight);
        $(d).mouseup(endDrag);
    }

    function endDrag(event) {
        TracWysiwyg.removeEvent(d, "mousemove", changeHeight);
        TracWysiwyg.removeEvent(d, "mouseup", endDrag);
    }

    function sync() {
        timer = null;
        self.syncTextAreaHeight();
    }
};

TracWysiwyg.prototype.syncTextAreaHeight = function() {
    var height = this.textarea.offsetHeight;
    var frame = this.frame;
    if (height > 0 && frame.height != height) {
        frame.height = height;
    }
};
TracWysiwyg.prototype.detectTracLink = function(event) {
    if (!this.autolink) {
        return;
    }
    var range = this.getSelectionRange();
    var node = range.startContainer;
    if (!node || !range.collapsed) {
        return;
    }
    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
    if (getSelfOrAncestor(node, /^(?:a|tt|pre)$/)) {
        return;
    }

    var offset = range.startOffset;
    if (node.nodeType != 3) {
        node = node.childNodes[offset];
        while (node && node.nodeType != 3) {
            node = node.lastChild;
        }
        if (!node) {
            return;
        }
        offset = node.nodeValue.length;
    }
    else if (offset == 0) {
        node = node.previousSibling;
        if (!node || node.nodeType == 1) {
            return;
        }
        offset = node.nodeValue.length;
    }
    var startContainer = node;
    var endContainer = node;
    var text = [ node.nodeValue.substring(0, offset) ];
    for ( ; ; ) {
        if (/[ \t\r\n\f\v]/.test(text[text.length - 1])) {
            break;
        }
        node = node.previousSibling;
        if (!node || node.nodeType == 1) {
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

    var pattern = this.wikiDetectTracLinkPattern;
    pattern.lastIndex = /[^ \t\r\n\f\v]*$/.exec(text).index;
    var match, tmp;
    for (tmp = pattern.exec(text); tmp; tmp = pattern.exec(text)) {
        match = tmp;
    }
    if (!match) {
        return;
    }

    var label = match[0];
    var link = this.normalizeTracLink(label);
    var id = this.generateDomId();
    var anchor = this.createAnchor(link, label, { id: id, "data-wysiwyg-autolink": "true" });
    var anonymous = this.contentDocument.createElement("div");
    anonymous.appendChild(anchor);
    var html = anonymous.innerHTML;

    node = endContainer;
    var startOffset = match.index;
    while (startContainer != node && startOffset >= startContainer.nodeValue.length) {
        startOffset -= startContainer.nodeValue.length;
        startContainer = startContainer.nextSibling;
    }
    var endOffset = startOffset + label.length;
    endContainer = startContainer;
    while (endContainer != node && endOffset >= endContainer.nodeValue.length) {
        endOffset -= endContainer.nodeValue.length;
        endContainer = endContainer.nextSibling;
    }
    this.selectRange(startContainer, startOffset, endContainer, endOffset);

    offset = text.length - match.index - label.length;
    if (offset == 0) {
        switch (event.keyCode) {
        case 0x20:  // SPACE
            this.insertHTML(html + "\u00a0");
            TracWysiwyg.stopEvent(event);
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
                    TracWysiwyg.stopEvent(event);
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

TracWysiwyg.prototype.formatParagraph = function() {
    if (this.selectionContainsTagName("table")) {
        return;
    }
    this.execCommand("formatblock", "<p>");
    this.selectionChanged();
};

TracWysiwyg.prototype.formatHeaderBlock = function(name) {
    if (this.selectionContainsTagName("table")) {
        return;
    }
    this.execCommand("formatblock", "<" + name + ">");
    this.selectionChanged();
};

TracWysiwyg.prototype.insertOrderedList = function() {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("insertorderedlist");
    this.selectionChanged();
};

TracWysiwyg.prototype.insertUnorderedList = function() {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    this.execCommand("insertunorderedlist");
    this.selectionChanged();
};

TracWysiwyg.prototype.insertTable = function() {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    var id = this.generateDomId();
    this.insertHTML(this.tableHTML(id, 2, 3));
    var element = this.contentDocument.getElementById(id)
    if (element) {
        this.selectNodeContents(element);
    }
    this.selectionChanged();
};

TracWysiwyg.prototype._tableHTML = function(row, col) {
    var tr = "<tr>" + ((1 << col) - 1).toString(2).replace(/1/g, "<td></td>") + "</tr>";
    var html = [
        '<table class="wiki">', '<tbody>',
        ((1 << row) - 1).toString(2).replace(/1/g, tr),
        '</tbody>', '</table>' ];
    return html.join("");
};

TracWysiwyg.prototype._getFocusForTable = function() {
    var hash = { node: null, cell: null, row: null, table: null };
    hash.node = this.getFocusNode();
    hash.cell = hash.node ? TracWysiwyg.getSelfOrAncestor(hash.node, /^t[dh]$/) : null;
    hash.row = hash.cell ? TracWysiwyg.getSelfOrAncestor(hash.cell, "tr") : null;
    hash.table = hash.row ? TracWysiwyg.getSelfOrAncestor(hash.row, "table") : null;
    return hash;
};

TracWysiwyg.prototype.insertTableCell_ = function(after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var row = focus.table.rows[focus.row.rowIndex];
        var cellIndex = focus.cell.cellIndex + (after ? 1 : 0);
        var colspan = $(focus.cell).attr('colspan');
        if (colspan > 1) {
            $(focus.cell).attr('colspan', colspan - 1);
        }
        this.insertTableCell(row, Math.min(cellIndex, row.cells.length));
    }
};

TracWysiwyg.prototype.insertTableRow = function(after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.row) {
        var d = this.contentDocument;
        var cells = focus.row.getElementsByTagName("td");
        var row = focus.table.insertRow(focus.row.rowIndex + (after ? 1 : 0));
        for (var j = 0; j < cells.length; j++) {
            this.insertTableCell(row, 0);
        }
    }
};

TracWysiwyg.prototype.insertTableColumn = function(after) {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var d = this.contentDocument;
        var rows = focus.table.rows;
        var length = rows.length;
        var cellIndex = focus.cell.cellIndex + (after ? 1 : 0);
        for (var i = 0; i < length; i++) {
            var row = rows[i];
            this.insertTableCell(row, Math.min(cellIndex, row.cells.length));
        }
    }
};

TracWysiwyg.prototype.deleteTableCell = function() {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var row = focus.table.rows[focus.row.rowIndex];
        var cellIndex = focus.cell.cellIndex;
        if (cellIndex < row.cells.length) {
            row.deleteCell(cellIndex);
        }
    }
};

TracWysiwyg.prototype.deleteTableRow = function() {
    var focus = this._getFocusForTable();
    if (focus.table && focus.row) {
        focus.table.deleteRow(focus.row.rowIndex);
    }
};

TracWysiwyg.prototype.deleteTableColumn = function() {
    var focus = this._getFocusForTable();
    if (focus.table && focus.cell) {
        var rows = focus.table.rows;
        var length = rows.length;
        var cellIndex = focus.cell.cellIndex;
        for (var i = 0; i < length; i++) {
            var row = rows[i];
            if (cellIndex < row.cells.length) {
                row.deleteCell(cellIndex);
            }
        }
    }
};

TracWysiwyg.prototype.moveFocusInTable = function(forward) {
    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
    var focus = this.getFocusNode();
    var element = getSelfOrAncestor(focus, /^(?:t[dhr]|table)$/);
    var target, table, rows, cells;
    switch (element.tagName.toLowerCase()) {
    case "td": case "th":
        focus = element;
        var row = getSelfOrAncestor(element, "tr");
        cells = row.cells;
        if (forward) {
            if (focus.cellIndex + 1 < cells.length) {
                target = cells[focus.cellIndex + 1];
            }
            else {
                table = getSelfOrAncestor(row, /^(?:tbody|table)$/);
                rows = table.rows;
                target = row.rowIndex + 1 < rows.length ? rows[row.rowIndex + 1].cells[0] : null;
            }
        }
        else {
            if (focus.cellIndex > 0) {
                target = cells[focus.cellIndex - 1];
            }
            else {
                table = getSelfOrAncestor(row, /^(?:tbody|table)$/);
                rows = table.rows;
                if (row.rowIndex > 0) {
                    cells = rows[row.rowIndex - 1].cells;
                    target = cells[cells.length - 1];
                }
                else {
                    target = null;
                }
            }
        }
        break;
    case "tr":
        cells = element.cells;
        target = cells[forward ? 0 : cells.length - 1];
        break;
    case "tbody": case "table":
        rows = element.rows;
        cells = rows[forward ? 0 : rows.length - 1].cells;
        target = cells[forward ? 0 : cells.length - 1];
        break;
    }
    if (target) {
        this.selectNodeContents(target);
    }
    else if (table) {
        table = getSelfOrAncestor(table, "table");
        var parent = table.parentNode;
        var elements = parent.childNodes;
        var length = elements.length;
        for (var offset = 0; offset < length; offset++) {
            if (table == elements[offset]) {
                if (forward) {
                    offset++;
                }
                this.selectRange(parent, offset, parent, offset);
            }
        }
    }
};

TracWysiwyg.prototype.formatCodeBlock = function() {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    var text = this.getSelectionText();
    if (!text) {
        var node = this.getFocusNode();
        while (node.nodeType == 3) {
            node = node.parentNode;
        }
        text = TracWysiwyg.getTextContent(node);
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

TracWysiwyg.prototype.insertHorizontalRule = function() {
    if (this.selectionContainsTagName("table") || this.selectionContainsTagName("pre")) {
        return;
    }
    if (!this.execCommand("inserthorizontalrule")) {
        this.insertHTML("<hr />");
    }
    this.selectionChanged();
};

TracWysiwyg.prototype.createLink = function() {
    if (this.selectionContainsTagName("pre")) {
        return;
    }

    var focus = this.getFocusNode();
    var anchor = TracWysiwyg.getSelfOrAncestor(focus, "a");
    var expand = anchor || TracWysiwyg.getSelfOrAncestor(focus, "tt");
    var currLink;
    if (anchor) {
        var autolink = anchor.getAttribute("data-wysiwyg-autolink");

        if (autolink == "true") {
            var pattern = this.wikiDetectTracLinkPattern;
            pattern.lastIndex = 0;
            var label = TracWysiwyg.getTextContent(anchor);
            var match = pattern.exec(label);
            if (match && match.index == 0 && match[0].length == label.length) {
                currLink = this.normalizeTracLink(label);
            }
        }
        if (!currLink) {
            currLink = anchor.getAttribute("data-wysiwyg-link") || anchor.href;
        }
    }
    else {
        currLink = "";
    }
    if (expand) {
        this.selectNodeContents(expand);
    }
    var text = this.getSelectionText() || "";
    var newLink = (prompt(text ? "Enter link:" : "Insert link:", currLink) || "").replace(/^\s+|\s+$/g, "");
    if (newLink && newLink != currLink) {
        text = text || newLink;
        newLink = this.normalizeTracLink(newLink);
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

TracWysiwyg.prototype.createAnchor = function(link, label, attrs) {
    var d = this.contentDocument;
    var anchor = d.createElement("a");
    for (var name in attrs) {
        var value = attrs[name];
        anchor.setAttribute(name, value);
    }
    anchor.href = link;
    anchor.title = link;
    anchor.setAttribute("data-wysiwyg-link", link);
    anchor.setAttribute("onclick", "return false;");
    anchor.appendChild(d.createTextNode(label));
    return anchor;
};
TracWysiwyg.prototype.collectChildNodes = function(dest, source) {
    var childNodes = source.childNodes;
    for (var i = childNodes.length - 1; i >= 0; i--) {
        dest.insertBefore(childNodes[i], dest.firstChild);
    }
};

TracWysiwyg.prototype.generateDomId = function() {
    var d = this.contentDocument;
    for ( ; ; ) {
        var id = "tmp-" + (new Date().valueOf().toString(36));
        if (!d.getElementById(id)) {
            return id;
        }
    }
};

TracWysiwyg.prototype.selectionChanged = function() {
    var status = {
        strong: false, em: false, underline: false, strike: false, sub: false,
        sup: false, monospace: false, paragraph: false, heading1: false,
        heading2: false, heading3: false, heading4: false, heading5: false,
        heading6: false, link: false, outdent: false,
        indent: false, table: false, code: false, quote: false, hr: false,
        br: false };
    var tagNameToKey = {
        b: "strong", i: "em", u: "underline", del: "strike", tt: "monospace",
        p: "paragraph", h1: "heading1", h2: "heading2", h3: "heading3",
        h4: "heading4", h5: "heading5", h6: "heading6", a: "link", pre: "code" };
    var position = this.getSelectionPosition();

    var node;
    if (position.start) {
        node = position.start == position.end ? position.start.firstChild : position.start.nextSibling;
        node = node || position.start;
    }
    else {
        node = null;
    }
    while (node) {
        if (node.nodeType == 1) {
            var name = node.tagName.toLowerCase();
            if (name in tagNameToKey) {
                name = tagNameToKey[name];
            }
            status[name] = true;
        }
        node = node.parentNode;
    }

    var toolbarButtons = this.toolbarButtons;
    for (var name in status) {
        var button = toolbarButtons[name];
        if (button) {
            var parent = button.parentNode;
            parent.className = (parent.className || "").replace(/ *\bselected\b|$/, status[name] ? " selected" : "");
        }
    }

    var styles = [ "quote", "paragraph", "code", "heading1",
        "heading2", "heading3", "heading4", "heading5", "heading6" ];
    var styleButton = toolbarButtons["style"];
    var styleButtonClass = "wysiwyg-menu-style";
    for (var i = 0; i < styles.length; i++) {
        var name = styles[i];
        if (status[name]) {
            styleButtonClass = "wysiwyg-menu-" + name;
            break;
        }
    }
    styleButton.parentNode.className = styleButtonClass;
};

(function() {
    var _linkScheme = "[a-zA-Z][a-zA-Z0-9+-.]*";
    // cf. WikiSystem.XML_NAME, http://www.w3.org/TR/REC-xml/#id
    var _xmlName = "[:_A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD](?:[-:_.A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]*[-_A-Za-z0-9\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD])?"
    var _quotedString = "'[^']+'|" + '"[^"]+"';
    var _changesetPath = "/[^\\]]*";
    var _wikiPageName = "(?:\\B[\\.<>]|\\b)[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+(?:\\.[A-Z][a-z]+(?:[A-Z][a-z0-9]*)+)*";
    var _wikiTextLink = "\\[\\[(?:.*?)\\]\\[(?:.*?)\\]\\]";
    var wikiInlineRules = [];
    wikiInlineRules.push("'''''");                  // 1. bolditalic -> badly supported by FitNesse parser
    wikiInlineRules.push("'''");                    // 2. bold
    wikiInlineRules.push("''");                     // 3. italic
    wikiInlineRules.push("--");                     // 4. strike
    wikiInlineRules.push("\\{\\{\\{.*?\\}\\}\\}");  // 5. code block -> keep for simplicity
    wikiInlineRules.push("!-.*?-!");                // 6. escaped
    wikiInlineRules.push(_wikiTextLink)				// 7. Wiki link
    wikiInlineRules.push(_wikiPageName);            // 8. WikiPageName

    var wikiRules = wikiInlineRules.slice(0);
    // -1. header
    wikiRules.push("^[ \\t\\r\\f\\v]*![1-6][ \\t\\r\\f\\v]+.*?(?:#" + _xmlName + ")?[ \\t\\r\\f\\v]*$");
    // -2. definition and comment
    wikiRules.push("^(?:![a-z]|#).*$");
    // -3. closing table row
    wikiRules.push("(?:\\|)[ \\t\\r\\f\\v]*$");
    // -4. cell
    wikiRules.push("!?(?:\\|)");
    // -5: open collapsible section
    wikiRules.push("^!\\*+[<>]?(?:[ \\t\\r\\f\\v]*|[ \\t\\r\\f\\v]+.*)$");
    // -6: close collapsible section
    wikiRules.push("^\\*+!$");

    // TODO could be removed?
    var wikiDetectTracLinkRules = [];
    wikiDetectTracLinkRules.push(_wikiPageName);


    var domToWikiInlinePattern = new RegExp("(?:" + wikiInlineRules.join("|") + ")", "g");
    var wikiRulesPattern = new RegExp("(?:(" + wikiRules.join(")|(") + "))", "g");
    var wikiDetectTracLinkPattern = new RegExp("(?:" + wikiDetectTracLinkRules.join("|") + ")", "g");

    TracWysiwyg.prototype._linkScheme = _linkScheme;
    TracWysiwyg.prototype._quotedString = _quotedString;
    TracWysiwyg.prototype._wikiPageName = _wikiPageName;
    TracWysiwyg.prototype.wikiInlineRules = wikiInlineRules;
    TracWysiwyg.prototype.xmlNamePattern = new RegExp("^" + _xmlName + "$");
    TracWysiwyg.prototype.domToWikiInlinePattern = domToWikiInlinePattern;
    TracWysiwyg.prototype.wikiRulesPattern = wikiRulesPattern;
    TracWysiwyg.prototype.wikiDetectTracLinkPattern = wikiDetectTracLinkPattern;
})();

TracWysiwyg.prototype.normalizeTracLink = function(link) {
    if (/^[\/.#]/.test(link)) {
        link = encodeURIComponent(link);
    }
    if (!/^[\w.+-]+:/.test(link)) {
        link = link;
    }
    if (/^[^\"\']/.test(link) && /\s/.test(link)) {
        if (link.indexOf('"') === -1) {
            link = '"' + link + '"';
        }
        else if (link.indexOf("'") === -1) {
            link = "'" + link + "'";
        }
        else {
            link = '"' + link.replace(/"/g, "%22") + '"';
        }
    }
    return link;
};

TracWysiwyg.prototype.isInlineNode = function(node) {
    if (node) {
        switch (node.nodeType) {
        case 1:
            return (node.tagName.toLowerCase() in this.wikiInlineTags);
        case 3:
            return true;
        }
    }
    return false;
};

(function() {
    var blocks = {
        p: true, div: true,
        h1: true, h2: true, h3: true, h4: true, h5: true, h6: true,
        table: true, thead: true, tbody: true, tr: true, td: true, th: true };

    function generator(prop, blocks) {
        return function (node) {
            if (!node) {
                return false;
            }
            for ( ; ; ) {
                if (node[prop]) {
                    return false;
                }
                node = node.parentNode;
                if (!node) {
                    return true;
                }
                if (node.nodeType == 1 && node.tagName.toLowerCase() in blocks) {
                    return true;
                }
            }
            return false;
        };
    }

    TracWysiwyg.prototype.isLastChildInBlockNode = generator("nextSibling", blocks);
    TracWysiwyg.prototype.isFirstChildInBlockNode = generator("previousSibling", blocks);
})();

TracWysiwyg.prototype.wikitextToFragment = function(wikitext, contentDocument, options) {
    options = options || {};
    var escapeNewlines = !!options.escapeNewlines;

    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
    var _linkScheme = this._linkScheme;
    var _quotedString = this._quotedString;
    var wikiInlineRulesCount = this.wikiInlineRules.length;
    var wikiRulesPattern = new RegExp(this.wikiRulesPattern.source, "g");
    
    var self = this;
    var fragment = contentDocument.createDocumentFragment();
    var holder = fragment;
    var lines = wikitext.split("\n");
    var codeText = null;
    var currentHeader = null;
    var decorationStatus;
    var decorationStack;
    var inCodeBlock, inCollapsibleBlock, inParagraph, inDefinition, inTable, inEscapedTable, inTableRow;
    inCodeBlock = inCollapsibleBlock = inParagraph = inDefinition = inTable = inEscapedTable = inTableRow = false;

    function handleCodeBlock(line) {
        if (/^ *\{\{\{ *$/.test(line)) {
            inCodeBlock++;
            if (inCodeBlock == 1) {
                closeParagraph();
                codeText = [];
            }
            else {
                codeText.push(line);
            }
        }
        else if (/^ *\}\}\} *$/.test(line)) {
            inCodeBlock--;
            if (inCodeBlock == 0) {
                var pre = contentDocument.createElement("pre");
                pre.className = "wiki";
                pre.appendChild(contentDocument.createTextNode(codeText.join(
                    pre.addEventListener && !window.opera ? "\n" : "\n\r")));
                holder.appendChild(pre);
                codeText = [];
            }
            else {
                codeText.push(line);
            }
        }
        else {
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
        inDefinition = true;
    }

    function closeDefinition() {
        closeParagraph();
    }

    function handleCollapsibleBlock(value) {
        inCollapsibleBlock++;
        var m = /^!\*+([<>])?\s+(.*)$/.exec(value);
        var collapsible = contentDocument.createElement("div");
        var title = contentDocument.createElement("p");
        $(collapsible).addClass("collapsable");
        collapsible.appendChild(title);
        title.appendChild(contentDocument.createTextNode(m[2] || unescape("title")));
        switch (m[1]) {
        case "<": // Hidden
            $(collapsible).addClass("hidden");
            break;
        case ">": // Collapsed
            $(collapsible).addClass("collapsed");
            break;
        }
        holder.appendChild(collapsible);
        holder = collapsible;
        openParagraph();
    }

    function closeCollapsibleBlock() {
        if (inCollapsibleBlock) {
            inCollapsibleBlock--;
            closeToFragment("div");
            holder = holder.parentNode;
        }
    }

    function handleInline(name) {
        if (name == "bolditalic") {
            if (decorationStatus.italic) {
                handleInline("italic");
                handleInline("bold");
            }
            else {
                handleInline("bold");
                handleInline("italic");
            }
            return;
        }

        var d = contentDocument;
        if (decorationStatus[name]) {
            var tagNames = [];
            for (var index = decorationStack.length - 1; index >= 0; index--) {
                var tagName = holder.tagName;
                holder = holder.parentNode;
                if (decorationStack[index] == name) {
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
            return;
        }

        var tagName;
        switch (name) {
        case "bold":        tagName = "b";      break;
        case "italic":      tagName = "i";      break;
        case "strike":      tagName = "del";    break;
        }

        if (holder == fragment) {
            openParagraph();
        }
        element = d.createElement(tagName);
        holder.appendChild(element);
        holder = element;
        decorationStatus[name] = true;
        decorationStack.push(name);
    }

    function handleInlineCode(value, length) {
        var d = contentDocument;
        var element = d.createElement("tt");
        value = value.slice(length, -length);
        if (value.length > 0) {
            element.appendChild(d.createTextNode(value));
            holder.appendChild(element);
        }
    }

    function createAnchor(link, label) {
        var anchor = self.createAnchor(link, label);
        holder.appendChild(anchor);
    }

    function handleTracLinks(value) {
        var match = handleTracLinks.pattern.exec(value);
        
        if (match) {
            var link = match[2];
            
            // some unknown sanitizing going on...
            var text = (match[1] || match[2].replace(/^[\w.+-]+:/, "")).replace(/^(["'])(.*)\1$/g, "$2");
            
            createAnchor(link, text);
        }
        else {
            holder.appendChild(contentDocument.createTextNode(value));
        }
    }
    /* old regexp for the 'real' trac links
    handleTracLinks.pattern = new RegExp("\\["
        + "((?:" + _linkScheme + ":)?(?:" + _quotedString + "|[^\\]\\s]+))"
        + "(?:\\s+(.*))?\\]");*/
        
    //TODO I'm sure this RegExp can be more 1337 (use _quotedString?)
    // compared to wikiTextLink it needs extra ()'s around the groups, why??
    handleTracLinks.pattern = new RegExp("\\["
    	+ "\\[((?:.*?))\\]"
    	+ "\\[((?:.*?))\\]"
    	+ "\\]");

    function handleTracWikiLink(value) {
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
        createAnchor(name, label || name);
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

    function openParagraph() {
        if (!inParagraph) {
            var element = contentDocument.createElement("p");
            holder.appendChild(element);
            holder = element;
            inParagraph = true;
        }
    }

    function closeParagraph() {
        if (inParagraph) {
            var target = holder;
            if (target != fragment) {
                target = getSelfOrAncestor(target, "p");
                self.appendBogusLineBreak(target);
                self.updateElementClassName(target);
            }
            holder = target.parentNode;
            inParagraph = false;
            inDefinition = false;
        }
    }

    function handleTableCell(action, escaped) {
        var d = contentDocument;
        var h, table, tbody;

        if (!inTable) {
            h = holder;
            table = d.createElement("table");
            if (escaped) table.className = "escaped";
            tbody = d.createElement("tbody");
            table.appendChild(tbody);
            h.appendChild(table);
            inTable = true;
            inTableRow = false;
        }
        else {
            h = holder;
            tbody = getSelfOrAncestor(h, "tbody");
        }

        if (inTableRow) {
            var cell = getSelfOrAncestor(h, "td");
            if (cell) {
                self.appendBogusLineBreak(cell);
            }
        }

        var row;
        switch (action) {
        case 1:
            row = d.createElement("tr");
            tbody.appendChild(row);
            inTableRow = true;
            break;
        case 0:
            row = getSelfOrAncestor(h, "tr");
            break;
        case -1:
            if (inTableRow) {
                var target = getSelfOrAncestor(h, "tr");
                holder = target.parentNode;
                inTableRow = false;
            }
            return;
        }

        var cell = d.createElement("td");
        row.appendChild(cell);
        holder = cell;
        decorationStatus = {};
    }

    function closeTable() {
        if (inTable) {
            var target = getSelfOrAncestor(holder, "table");

            // Spanning columns fitnesse style.
            var maxCells = Math.max.apply(Math, $.map($('tr', target), function(e) {
                return $('td', e).size();
            }));
            $('tr', target).each(function() {
                var s = $('td', this).size();
                if (s < maxCells) {
                    $('td:last', this).attr('colspan', maxCells - s + 1);
                }
            });
            holder = target.parentNode;
            inTable = inEscapedTable = inTableRow = false;
        }
    }

    function closeToFragment(stopTag) {
        // Note: we're not exceeding collapsable section boundries
        var element = holder;
        var _fragment = fragment;
        stopTag = stopTag ? stopTag.toLowerCase() : "div";

        while (element != _fragment) {
            var tag = element.tagName.toLowerCase();
            if (tag == stopTag) {
                holder = element;
                return;
            }
            var method = null;
            switch (tag) {
            case "p":
                method = closeParagraph;
                break;
            case "td": case "tr": case "tbody": case "table":
                method = closeTable;
                break;
            default:
                break;
            }
            if (method) {
                method();
                element = holder;
            }
            else {
                element = element.parentNode;
            }
        }

        holder = _fragment;
    }

    function getMatchNumber(match) {
        var length = match.length;
        for (var i = 1; i < length; i++) {
            if (match[i]) {
                if (i <= wikiInlineRulesCount) {
                    return i;
                }
                return wikiInlineRulesCount - i;
            }
        }
        return null;
    }

    for (var indexLines = 0; indexLines < lines.length; indexLines++) {
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
        if (line.length == 0 && !inCollapsibleBlock) {
            closeToFragment();
            continue;
        }
        line = line.replace(/\t/g, "        ");
        line = line.replace(/\u00a0/g, " ");

        wikiRulesPattern.lastIndex = 0;
        var prevIndex = wikiRulesPattern.lastIndex;
        decorationStatus = {};
        decorationStack = [];
        for ( ; ; ) {
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

            if ((prevIndex == 0 && text || match && match.index == 0 && matchNumber > 0)
                && !inParagraph && !inCollapsibleBlock) {
                closeToFragment();
            }


            if (text || match && matchNumber > 0) {
                if (inParagraph && (prevIndex == 0)) {
                    text = text ? (" " + text) : "";
                }
                if (!inTable && !currentHeader || holder == fragment) {
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
                if (inEscapedTable) { break; }
                handleInline("bolditalic");
                continue;
            case 2:     // bold
                if (inEscapedTable) { break; }
                handleInline("bold");
                continue;
            case 3:     // italic
                if (inEscapedTable) { break; }
                handleInline("italic");
                continue;
            case 4:     // strike
                if (inEscapedTable) { break; }
                handleInline("strike");
                continue;
            case 5:     // code block
                if (inEscapedTable) { break; }
                handleInlineCode(matchText, 3);
                continue;
            case 6:     // escaped
                handleInlineCode(matchText, 2);
                continue;
        	case 7:		// Wiki link
                if (inEscapedTable) { break; }
        		handleTracLinks(matchText);
        		continue;
            case 8:		// WikiPageName
                if (inEscapedTable) { break; }
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
            case -2:    // definition (leading "!")
                handleDefinition(matchText);
                break;
            case -3:    // closing table row
                if (inTable) {
                    handleTableCell(-1);
                    continue;
                }
                break;
            case -4:    // cell
                if (!inTable && match.index == 0) {
                    closeToFragment();
                }
                wikiRulesPattern.lastIndex = prevIndex;
                if (!inTable) {
                    inEscapedTable = /^!/.test(matchText);
                }

                handleTableCell(inTableRow ? 0 : 1, inEscapedTable);
                continue;
            case -5: // collapsible section
                handleCollapsibleBlock(matchText);
                continue;
            case -6: // close collapsible section
                closeCollapsibleBlock();
                continue;
            }

            if (matchText) {
                if (!currentHeader && !inTable) {
                    openParagraph();
                }
                holder.appendChild(contentDocument.createTextNode(matchText));
            }
        }

        // End of line actions:

        if (currentHeader) {
            closeHeader();
        }
        if (inDefinition) {
            closeDefinition();
        }
        if (inTable) {
            handleTableCell(-1);
        }
    }
    closeToFragment();

    return fragment;
};

TracWysiwyg.prototype.wikitextToOnelinerFragment = function(wikitext, contentDocument, options) {
    var source = this.wikitextToFragment(wikitext, contentDocument, options);
    var fragment = contentDocument.createDocumentFragment();
    this.collectChildNodes(fragment, source.firstChild);
    return fragment;
};

TracWysiwyg.prototype.wikiOpenTokens = {
    "h1": "!1 ", "h2": "!2 ", "h3": "!3 ", "h4": "!4 ", "h5": "!5 ", "h6": "!6 ",
    "b": "'''", "strong": "'''",
    "i": "''", "em": "''",
    "del": "--", "strike": "--",
    "hr": "----\n",
    "table": true,
    "tbody": true,
    "div": "!***" };

TracWysiwyg.prototype.wikiCloseTokens = {
    "#text": true,
    "a": true,
    "tt": true,
    "b": "'''", "strong": "'''",
    "i": "''", "em": "''",
    "del": "--", "strike": "--",
    "br": true,
    "hr": true,
    "tbody": true,
    "tr": "|\n",
    "td": true, "th": true,
    "div": "*!\n" };

TracWysiwyg.prototype.wikiBlockTags = {
    "h1": true, "h2": true, "h3": true, "h4": true, "h5": true, "h6": true,
    "table": true, "hr": true };

TracWysiwyg.prototype.wikiInlineTags = {
    "a": true, "tt": true, "b": true, "strong": true, "i": true, "em": true,
    "u": true, "del": true, "strike": true, "sub": true, "sup": true,
    "br": true, "span": true };

TracWysiwyg.prototype.domToWikitext = function(root, options) {
    options = options || {};
    var formatCodeBlock = !!options.formatCodeBlock;
    var escapeNewlines = !!options.escapeNewlines;

    var self = this;
    var getTextContent = TracWysiwyg.getTextContent;
    var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
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
    var inCodeBlock = false;
    var skipNode = null;
    var openBracket = false;

    function escapeText(s) {
        return "!-" + s + "-!";
    }

    function tokenFromSpan(node) {
        var style = node.style;
        if (style.fontWeight == "bold") {
            return wikiOpenTokens["b"];
        }
        if (style.fontStyle == "italic") {
            return wikiOpenTokens["i"];
        }
        switch (style.textDecoration) {
        case "line-through":
            return wikiOpenTokens["del"];
        }
        return undefined;
    }

    function nodeDecorations(node) {
        var _wikiOpenTokens = wikiOpenTokens;
        var _decorationTokenPattern = decorationTokenPattern;
        var hash = {};

        for ( ; ; ) {
            var childNodes = node.childNodes;
            if (!childNodes || childNodes.length != 1) {
                break;
            }
            var child = childNodes[0];
            if (child.nodeType != 1) {
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

        while (_texts.length > 0) {
            var token = _texts[_texts.length - 1];
            if (_decorationTokenPattern.test(token)) {
                if (decorationsHash[token]) {
                    delete decorationsHash[token];
                    cancelDecorations.push(_texts.pop());
                    continue;
                }
                if ((token == "'''" || token == "''") && _texts.length > 1) {
                    var moreToken = _texts[_texts.length - 2];
                    if (_decorationTokenPattern.test(moreToken)
                        && token + moreToken == "'''''"
                        && decorationsHash[moreToken])
                    {
                        delete decorationsHash[moreToken];
                        cancelDecorations.push(moreToken);
                        _texts[_texts.length - 2] = _texts[_texts.length - 1];
                        _texts.pop();
                    }
                }
            }
            break;
        }

        for (var token in decorationsHash) {
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
        if (length == 0 || !_decorationTokenPattern.test(token)) {
            _texts.push(token);
            return;
        }
        var last = _texts[length - 1];
        if (!_decorationTokenPattern.test(last)) {
            _texts.push(token);
            return;
        }
        if (last == token) {
            _texts.pop();
            return;
        }
        if (length < 2 || last + token != "'''''") {
            _texts.push(token);
            return;
        }
        if (_texts[length - 2] == token) {
            _texts[length - 2] = _texts[length - 1];
            _texts.pop();
        }
        else {
            _texts.push(token);
        }
    }

    function tracLinkText(link, label) {
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
        var label = getTextContent(node).replace(/^\s+|\s+$/g, "");
        if (!label) {
            return;
        }
        var text = null;
        if (autolink == "true") {
            if (wikiPageNamePattern.test(label)) {
                text = label;
                link = label;
            }
        }
        else {
            if (link == label) {
                if (bracket) {
                    text = label;
                }
            }
        }
        if (!text) {
            // TODO this chould be simplified to just the else now I think (Vincent)
            var match = /^([\w.+-]+):(@?(.*))$/.exec(link);
            if (match) {
                if (label == match[2]) {
                    if (match[1] == "wiki" && wikiPageNamePattern.test(match[2])) {
                        text = match[2];
                    }
                    else {
                        text = "[" + link + "]";
                    }
                }
            }
        }
        if (text === null) {
            text = tracLinkText(link, label);
        }
        pushTextWithDecorations(text, node);
    }

    function string(source, times) {
        var value = (1 << times) - 1;
        if (value <= 0) {
            return "";
        }
        else {
            return value.toString(2).replace(/1/g, source);
        }
    }

    function open(name, node) {
        if (skipNode !== null) {
            return;
        }
        var _texts = texts;
        var token = wikiOpenTokens[name];
        if (token !== undefined) {
            if (name in wikiBlockTags && self.isInlineNode(node.previousSibling)) {
                _texts.push("\n");
            }
            if (token !== true) {
                pushToken(token);
            }
            if (name == "table" && $(node).hasClass("escaped")) {
                _texts.push("!");
            }
            if (name == "div" && $(node).hasClass("collapsable")) {
                if ($(node).hasClass("collapsed")) {
                    _texts.push("> ");
                } else if ($(node).hasClass("hidden")) {
                    _texts.push("< ");
                } else {
                    _texts.push(" ");
                }
            }
            openBracket = false;
        }
        else {
            switch (name) {
            case "#text":
                var value = node.nodeValue;
                if (value) {
                    if (!inCodeBlock) {
                        if (value && !self.isInlineNode(node.previousSibling || node.parentNode)) {
                            value = value.replace(/^[ \t\r\n\f\v]+/g, "");
                        }
                        if (value && !self.isInlineNode(node.nextSibling || node.parentNode)) {
                            value = value.replace(/[ \t\r\n\f\v]+$/g, "");
                        }
                        value = value.replace(/\r?\n/g, " ");
                        //if (!formatCodeBlock && !inEscapedTable) {
                        //    value = value.replace(domToWikiInlinePattern, escapeText);
                        //}
                        openBracket = /<$/.test(value);
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
                var bracket = false;
                if (openBracket) {
                    var nextSibling = node.nextSibling;
                    bracket = nextSibling && nextSibling.nodeType == 3 && /^>/.test(nextSibling.nodeValue);
                    openBracket = false;
                }
                pushAnchor(node, bracket);
                break;
            case "br":
                if (!self.isBogusLineBreak(node)) {
                    var value = null;
                    if (inCodeBlock) {
                        value = "\n";
                    }
                    else {
                        value = " ";
                    }
                    _texts.push(value);
                }
                break;
            case "pre":
                _texts.push("\n{{{\n");
                inCodeBlock = true;
                break;
            case "table":
                if (node.className == 'escaped') {
                    _texts.push("!");
                }
            case "th":
            case "td":
                skipNode = node;
                _texts.push("|");
                var text = self.domToWikitext(node, self.options).replace(/^ +| +$/g, "");
                if (text) {
                    _texts.push(" ", text, " ");    break;
                }
                else {
                    _texts.push(" ");
                }
                break;
            case "tr":
                break;
            case "tt":
                skipNode = node;
                var value = getTextContent(node);
                var text;
                if (value) {
	            text = "!-" + value + "-!";
                    pushTextWithDecorations(text, node);
                }
                break;
            case "span":
                if (node.className == "wikianchor" && xmlNamePattern.test(node.id || "")) {
                    skipNode = node;
                    var text = self.domToWikitext(node, self.options).replace(/^ +| +$|\]/g, "");
                    _texts.push("[=#", node.id, text ? " " + text + "]" : "]");
                }
                else {
                    var token = tokenFromSpan(node);
                    if (token !== undefined) {
                        pushToken(token);
                    }
                }
                break;
            case "script":
            case "style":
                skipNode = node;
                break;
            }
            if (name != "#text") {
                openBracket = false;
            }
        }
    }

    function close(name, node) {
        if (skipNode !== null) {
            if (skipNode == node) {
                skipNode = null;
            }
            return;
        }
        var _texts = texts;
        var token = wikiCloseTokens[name];
        if (token === true) {
            // nothing to do
        }
        else if (token !== undefined) {
            pushToken(token);
        }
        else {
            switch (name) {
            case "p":
                if ($(node).hasClass('meta')) {
                    _texts.push("\n");
                } else {
                    _texts.push("\n\n");
                }
                break;
            case "pre":
                _texts.push("\n}}}\n");
                inCodeBlock = false;
                break;
            case "span":
                var token = tokenFromSpan(node);
                if (token !== undefined) {
                    _texts.push(token);
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

        if (node && last == node.parentNode) {  // down
            // nothing to do
        }
        else if (node && last == node.previousSibling) {    // forward
            close(stack.pop(), last);
        }
        else {  // up, forward
            var tmp = last;
            var nodeParent = node ? node.parentNode : root;
            for ( ; ; ) {
                var parent = tmp.parentNode;
                if (parent == node) {
                    break;
                }
                close(stack.pop(), tmp);
                if (parent == nodeParent || !parent) {
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

if (window.getSelection) {
    TracWysiwyg.prototype.appendBogusLineBreak = function(element) {
        var wikiInlineTags = this.wikiInlineTags;
        var last = element.lastChild;
        for ( ; ; ) {
            if (!last) {
                break;
            }
            if (last.nodeType != 1) {
                return;
            }
            var name = last.tagName.toLowerCase();
            if (name == "br") {
                break;
            }
            if (!(name in wikiInlineTags)) {
                return;
            }
            last = last.lastChild || last.previousSibling;
        }
        var br = this.contentDocument.createElement("br");
        element.appendChild(br);
    };
    TracWysiwyg.prototype.isBogusLineBreak = TracWysiwyg.prototype.isLastChildInBlockNode;
    TracWysiwyg.prototype.insertParagraphOnEnter = function(event) {
        var range = this.getSelectionRange();
        var node = range.endContainer;
        var header = null;
        if (node && node.nodeType == 3 && range.endOffset == node.nodeValue.length) {
            var nextSibling = node.nextSibling;
            if (!nextSibling || nextSibling.tagName.toLowerCase() == "br") {
                while (node) {
                    if (node.nodeType == 1 && /^h[1-6]$/i.exec(node.tagName)) {
                        header = node;
                        break;
                    }
                    node = node.parentNode;
                }
                if (header) {
                    var parent = header.parentNode;
                    var childNodes = parent.childNodes;
                    var length = childNodes.length;
                    for (var offset = 0; offset < length; offset++) {
                        if (childNodes[offset] == header) {
                            offset++;
                            break;
                        }
                    }
                    this.selectRange(parent, offset, parent, offset);
                    this.insertHTML('<p><br/></p>');
                    TracWysiwyg.stopEvent(event);
                }
            }
        }
    };
    TracWysiwyg.prototype.tableHTML = function(id, row, col) {
        var html = this._tableHTML(row, col);
        return html.replace(/<td><\/td>/g, '<td><br/></td>').replace(/<td>/, '<td id="' + id + '">');
    };
    TracWysiwyg.prototype.insertTableCell = function(row, index) {
        var cell = row.insertCell(index);
        this.appendBogusLineBreak(cell);
        return cell;
    };
    TracWysiwyg.prototype.getFocusNode = function() {
        return this.contentWindow.getSelection().focusNode;
    };
    TracWysiwyg.prototype.updateElementClassName = function(element) {
        var getSelfOrAncestor = TracWysiwyg.getSelfOrAncestor;
        var p = getSelfOrAncestor(element, "p");
        if (p) {
            if (/^![a-z]/.test(p.innerHTML)) {
                if (p.className != 'meta') {
                    p.className = 'meta';
                }
            } else if (/^#/.test(p.innerHTML)) {
                if (p.className != 'comment') {
                    p.className = 'comment';
                }
            } else {
                $(p).removeClass();
            }
        }
    };
    TracWysiwyg.prototype.showAutoCompleteOnShiftSpace = function(event) {
		var tdElement = TracWysiwyg.getSelfOrAncestor(this.getSelectionRange().startContainer, "td");
		var self = this;
		
		// autocomplete only applicable on td
		if (tdElement) {
			var seleniumCommands = ["waitForTextPresent", "clickAndWait"];
			
			var elementOffset = $(tdElement).offset();
			var elementWidth = $(tdElement).width();
			var elementHeight = $(tdElement).height();
			
			var dialogX = elementOffset.left + (elementWidth / 2);
			var dialogY = elementOffset.top + (elementHeight / 2);
			
			
			$("#dialog").dialog({ position: [dialogX, dialogY] });
			//attach autocomplete
			$("#autocomplete").autocomplete({
	
				//define callback to format results
				source: function (request, response) {
					response(seleniumCommands);	
				},
	
				//define select handler
				select: function(event, ui) {
					self.insertHTML(ui.item.value);
					$("#dialog").dialog("close");
				},
	
				//define select handler
				change: function() {
					//prevent 'to' field being updated and correct position
					$("#autocomplete").val("").css("top", 2);
				}
			});		
		}
	};     
    if (window.opera) {
        TracWysiwyg.prototype.insertLineBreak = function() {
            this.execCommand("inserthtml", "<br/>");
        };
        TracWysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    }
    else if (window.getSelection().setBaseAndExtent) {  // Safari 2+
        TracWysiwyg.prototype.insertLineBreak = function() {
            this.execCommand("insertlinebreak");
        };
        TracWysiwyg.prototype.insertLineBreakOnShiftEnter = function(event) {
            this.insertLineBreak();
            TracWysiwyg.stopEvent(event);
        };
    }
    else {  // Firefox 2+
        TracWysiwyg.prototype.insertLineBreak = function() {
            var d = this.contentDocument;
            var event = d.createEvent("KeyboardEvent");
            event.initKeyEvent("keypress", true, true, null, false, false, true, false, 0x000d, 0);
            d.body.dispatchEvent(event);
        };
        TracWysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    }
    if (window.getSelection().removeAllRanges) {
        TracWysiwyg.prototype.selectNode = function(node) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.selectNode(node);
            selection.addRange(range);
        };
        TracWysiwyg.prototype.selectNodeContents = function(node) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.selectNodeContents(node);
            selection.addRange(range);
        };
        TracWysiwyg.prototype.selectRange = function(start, startOffset, end, endOffset) {
            var selection = this.contentWindow.getSelection();
            selection.removeAllRanges();
            var range = this.contentDocument.createRange();
            range.setStart(start, startOffset);
            range.setEnd(end, endOffset);
            selection.addRange(range);
        };
        TracWysiwyg.prototype.getNativeSelectionRange = function() {
            var selection = this.contentWindow.getSelection();
            return selection.rangeCount > 0 ? selection.getRangeAt(0) : null;
        };
        TracWysiwyg.prototype.expandSelectionToElement = function(arg) {
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
        TracWysiwyg.prototype.insertHTML = function(html) {
            this.execCommand("inserthtml", html);
        };
    }
    else {      // Safari 2
        TracWysiwyg.prototype.selectNode = function(node) {
            var selection = this.contentWindow.getSelection();
            var range = this.contentDocument.createRange();
            range.selectNode(node);
            selection.setBaseAndExtent(range.startContainer, range.startOffset, range.endContainer, range.endOffset);
            range.detach();
        };
        TracWysiwyg.prototype.selectNodeContents = function(node) {
            this.selectRange(node, 0, node, node.childNodes.length);
        };
        TracWysiwyg.prototype.selectRange = function(start, startOffset, end, endOffset) {
            var selection = this.contentWindow.getSelection();
            selection.setBaseAndExtent(start, startOffset, end, endOffset);
        };
        TracWysiwyg.prototype.getNativeSelectionRange = function() {
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
        TracWysiwyg.prototype.expandSelectionToElement = function(arg) {
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
        TracWysiwyg.prototype.insertHTML = function(html) {
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
    TracWysiwyg.prototype.getSelectionRange = TracWysiwyg.prototype.getNativeSelectionRange;
    TracWysiwyg.prototype.getSelectionText = function() {
        var range = this.getNativeSelectionRange();
        return range ? range.toString() : null;
    };
    TracWysiwyg.prototype.getSelectionHTML = function() {
        var fragment = this.getSelectionFragment();
        var anonymous = this.contentDocument.createElement("div");
        anonymous.appendChild(fragment);
        return anonymous.innerHTML;
    };
    TracWysiwyg.prototype.getSelectionFragment = function() {
        var range = this.getNativeSelectionRange();
        return range ? range.cloneContents() : this.contentDocument.createDocumentFragment();
    };
    TracWysiwyg.prototype.getSelectionPosition = function() {
        var range = this.getNativeSelectionRange();
        var position = { start: null, end: null };
        if (range) {
            position.start = range.startContainer;
            position.end = range.endContainer;
        }
        return position;
    };
    TracWysiwyg.prototype.selectionContainsTagName = function(name) {
        var selection = this.contentWindow.getSelection();
        var range = this.getNativeSelectionRange();
        if (!range) {
            return false;
        }
        var ancestor = range.commonAncestorContainer;
        if (!ancestor) {
            return false;
        }
        if (TracWysiwyg.getSelfOrAncestor(ancestor, name)) {
            return true;
        }
        if (ancestor.nodeType != 1) {
            return false;
        }
        var elements = ancestor.getElementsByTagName(name);
        var length = elements.length;
        for (var i = 0; i < length; i++) {
            if (selection.containsNode(elements[i], true)) {
                return true;
            }
        }
        return false;
    };
}
else if (document.selection) {
    TracWysiwyg.prototype.appendBogusLineBreak = function(element) { };
    TracWysiwyg.prototype.isBogusLineBreak = function(node) { return false };
    TracWysiwyg.prototype.insertParagraphOnEnter = null;
    TracWysiwyg.prototype.insertLineBreak = function() {
        this.insertHTML("<br/>");
    };
    TracWysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    TracWysiwyg.prototype.tableHTML = function(id, row, col) {
        var html = this._tableHTML(row, col);
        return html.replace(/<td>/, '<td id="' + id + '">');
    };
    TracWysiwyg.prototype.insertTableCell = function(row, index) {
        return row.insertCell(index);
    };
    TracWysiwyg.prototype.getFocusNode = function() {
        this.contentWindow.focus();
        var d = this.contentDocument;
        var range = d.selection.createRange();
        var node = range.item ? range.item(0) : range.parentElement();
        return node.ownerDocument == d ? node : null;
    };
    TracWysiwyg.prototype.selectNode = function(node) {
        var d = this.contentDocument;
        var body = d.body;
        var range;
        d.selection.empty();
        try {
            range = body.createControlRange();
            range.addElement(node);
        }
        catch (e) {
            range = body.createTextRange();
            range.moveToElementText(node);
        }
        range.select();
    };
    TracWysiwyg.prototype.selectNodeContents = function(node) {
        var d = this.contentDocument;
        d.selection.empty();
        var range = d.body.createTextRange();
        range.moveToElementText(node);
        range.select();
    };
    TracWysiwyg.prototype.selectRange = function(start, startOffset, end, endOffset) {
        var d = this.contentDocument;
        var body = d.body;
        d.selection.empty();
        var range = endPoint(start, startOffset);
        if (start != end || startOffset != endOffset) {
            range.setEndPoint("EndToEnd", endPoint(end, endOffset));
        }
        range.select();

        function endPoint(node, offset) {
            var range;
            if (node.nodeType == 1) {
                var childNodes = node.childNodes;
                if (offset >= childNodes.length) {
                    range = body.createTextRange();
                    range.moveToElementText(node);
                    range.collapse(false);
                    return range;
                }
                node = childNodes[offset];
                if (node.nodeType == 1) {
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
            if (node.nodeType != 3) {
                throw "selectRange: nodeType != @".replace(/@/, node.nodeType);
            }

            range = body.createTextRange();
            var element = node.previousSibling;
            while (element) {
                var nodeType = element.nodeType;
                if (nodeType == 1) {
                    range.moveToElementText(element);
                    range.collapse(false);
                    break;
                }
                if (nodeType == 3) {
                    offset += element.nodeValue.length;
                }
                element = element.previousSibling;
            }
            if (!element) {
                range.moveToElementText(node.parentNode);
                range.collapse(true);
            }
            if (offset != 0) {
                range.move("character", offset);
            }
            return range;
        }
    };
    TracWysiwyg.prototype.getSelectionRange = function() {
        var body = this.contentDocument.body;
        var pseudo = {};
        var start = this.getNativeSelectionRange();
        if (start.item) {
            var element = start.item(0);
            var parent = element.parentNode;
            var childNodes = parent.childNodes;
            var length = childNodes.length;
            for (var i = 0; i < length; i++) {
                if (childNodes[i] == element) {
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
        pseudo.collapsed = start.compareEndPoints("StartToEnd", end) == 0;
        start.collapse(true);
        end.collapse(false);

        function nextElement(range) {
            var parent = range.parentElement();
            var childNodes = parent.childNodes;
            var length = childNodes.length;
            for (var i = 0; i < length; i++) {
                var node = childNodes[i];
                if (node.nodeType == 1) {
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
            tmp.moveToElementText(element || parent);
            tmp.collapse(!!element);
            tmp.move("character", -index);
            if (!element) {
                length++;
            }
            for ( ; length >= 0; length--) {
                if (tmp.compareEndPoints("EndToStart", range) == 0) {
                    return length;
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
            while (node && node.nodeType == 3) {
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
                pseudo[offsetKey] = containerKey == "startContainer" ? 0 : length - 1;
                return;
            }
            element = parent;
            parent = element.parentNode;
            childNodes = parent.childNodes;
            length = childNodes.length;
            for (offset = 0; offset < length; offset++) {
                if (element == childNodes[offset]) {
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
    TracWysiwyg.prototype.getNativeSelectionRange = function() {
        this.contentWindow.focus();
        return this.contentDocument.selection.createRange();
    };
    TracWysiwyg.prototype.getSelectionText = function() {
        var range = this.getNativeSelectionRange();
        if (range) {
            return range.item ? range.item(0).innerText : range.text;
        }
        return null;
    };
    TracWysiwyg.prototype.getSelectionHTML = function() {
        var range = this.getNativeSelectionRange();
        if (range) {
            return range.item ? range.item(0).innerHTML : range.htmlText;
        }
        return null;
    };
    TracWysiwyg.prototype.getSelectionFragment = function() {
        var d = this.contentDocument;
        var fragment = d.createDocumentFragment();
        var anonymous = d.createElement("div");
        anonymous.innerHTML = this.getSelectionHTML();
        this.collectChildNodes(fragment, anonymous);
        return fragment;
    };
    TracWysiwyg.prototype.getSelectionPosition = function() {
        this.contentWindow.focus();
        var d = this.contentDocument;
        var range = d.selection.createRange();
        var startNode = null;
        var endNode = null;
        if (range.item) {
            if (range.item(0).ownerDocument == d) {
                startNode = range.item(0);
                endNode = range.item(range.length - 1);
            }
        }
        else {
            if (range.parentElement().ownerDocument == d) {
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
    TracWysiwyg.prototype.expandSelectionToElement = function(arg) {
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
    TracWysiwyg.prototype.selectionContainsTagName = function(name) {
        this.contentWindow.focus();
        var d = this.contentDocument;
        var selection = d.selection;
        var range = selection.createRange();
        var parent = range.item ? range.item(0) : range.parentElement();
        if (!parent) {
            return false;
        }
        if (TracWysiwyg.getSelfOrAncestor(parent, name)) {
            return true;
        }
        var elements = parent.getElementsByTagName(name);
        var length = elements.length;
        for (var i = 0; i < length; i++) {
            var testRange = selection.createRange();
            testRange.moveToElementText(elements[i]);
            if (range.compareEndPoints("StartToEnd", testRange) <= 0
                && range.compareEndPoints("EndToStart", testRange) >= 0)
            {
                return true;
            }
        }
        return false;
    };
    TracWysiwyg.prototype.insertHTML = function(html) {
        this.contentWindow.focus();
        var selection = this.contentDocument.selection;
        var range = selection.createRange();
        range.pasteHTML(html.replace(/\t/g, "&#9;"));
        range.collapse(false);
        range.select();
        range = this.contentDocument.selection.createRange();
    };
}
else {
    TracWysiwyg.prototype.appendBogusLineBreak = function(element) { };
    TracWysiwyg.prototype.insertParagraphOnEnter = null;
    TracWysiwyg.prototype.insertLineBreak = function() { };
    TracWysiwyg.prototype.insertTableCell = function(row, index) { return null };
    TracWysiwyg.prototype.getFocusNode = function() { return null };
    TracWysiwyg.prototype.selectNode = function(node) { };
    TracWysiwyg.prototype.selectNodeContents = function(node) { return null };
    TracWysiwyg.prototype.selectRange = function(start, startOffset, end, endOffset) { };
    TracWysiwyg.prototype.getSelectionRange = function() { return null };
    TracWysiwyg.prototype.getNativeSelectionRange = function() { return null };
    TracWysiwyg.prototype.getSelectionText = function() { return null };
    TracWysiwyg.prototype.getSelectionHTML = function() { return null };
    TracWysiwyg.prototype.getSelectionFragment = function() { return null };
    TracWysiwyg.prototype.getSelectionPosition = function() { return null };
    TracWysiwyg.prototype.expandSelectionToElement = function(arg) { };
    TracWysiwyg.prototype.selectionContainsTagName = function(name) { return false };
    TracWysiwyg.prototype.insertHTML = function(html) { };
}

TracWysiwyg.prototype._treeWalkEmulation = function(root, iterator) {
    if (!root.firstChild) {
        iterator(null);
        return;
    }
    var element = root;
    var tmp;
    while (element) {
        if (tmp = element.firstChild) {
            element = tmp;
        }
        else if (tmp = element.nextSibling) {
            element = tmp;
        }
        else {
            for ( ; ; ) {
                element = element.parentNode;
                if (element == root || !element) {
                    iterator(null);
                    return;
                }
                if (tmp = element.nextSibling) {
                    element = tmp;
                    break;
                }
            }
        }
        iterator(element);
    }
};

if (document.createTreeWalker) {
    TracWysiwyg.prototype.treeWalk = function(root, iterator) {
        var walker = root.ownerDocument.createTreeWalker(
            root, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null, true);
        while (walker.nextNode()) {
            iterator(walker.currentNode);
        }
        iterator(null);
    };
}
else {
    TracWysiwyg.prototype.treeWalk = TracWysiwyg.prototype._treeWalkEmulation;
}

TracWysiwyg.instances = [];
TracWysiwyg.count = 0;
TracWysiwyg.tracPaths = null;

TracWysiwyg.newInstance = function(textarea, options) {
    var instance = new TracWysiwyg(textarea, options);
    TracWysiwyg.instances.push(instance);
    return instance;
};

TracWysiwyg.findInstance = function(textarea) {
    var instances = TracWysiwyg.instances;
    var length = instances.length;
    for (var i = 0; i < length; i++) {
        var instance = instances[i];
        if (instance.textarea == textarea) {
            return instance;
        }
    }
    return null;
};

TracWysiwyg.getTracPaths = function() {
    var stylesheets = [];
    var paths = { stylesheets: stylesheets, base: '/' };

    var d = document;
    var head = d.getElementsByTagName("head")[0];
    var links = head.getElementsByTagName("link");
    var length = links.length;
    for (var i = 0; i < length; i++) {
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

TracWysiwyg.getOptions = function() {
    var options = {};
    if (typeof window._wysiwyg != "undefined") {
        options = _wysiwyg;
    }
    return options;
};

TracWysiwyg.getEditorMode = function() {
    if (TracWysiwyg.editorMode) {
        return TracWysiwyg.editorMode;
    }

    var mode = null;
    var cookies = (document.cookie || "").split(";");
    var length = cookies.length;
    for (var i = 0; i < length; i++) {
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
        // TODO: do same for wrap option.
    }

    TracWysiwyg.editorMode = mode || "textarea";
    return TracWysiwyg.editorMode;
};

TracWysiwyg.setEditorMode = function(mode) {
    switch (mode) {
    case "wysiwyg":
        break;
    default:    // "textarea"
        mode = "textarea";
        break;
    }
    TracWysiwyg.editorMode = mode;

    var now = new Date();
    if (!/\/$/.test(TracWysiwyg.tracPaths.base)) {
        expires = new Date(now.getTime() - 86400000);
        pieces = [ "wysiwyg=",
            "path=" + TracWysiwyg.tracPaths.base + "/",
            "expires=" + expires.toUTCString() ];
        document.cookie = pieces.join("; ");
    }
    var expires = new Date(now.getTime() + 365 * 86400 * 1000);
    var pieces = [ "wysiwyg=" + mode,
        "path=" + TracWysiwyg.tracPaths.base,
        "expires=" + expires.toUTCString() ];
    document.cookie = pieces.join("; ");
};

TracWysiwyg.removeEvent = function(element, type, func) {
    jQuery(element).unbind(type, func);
};

TracWysiwyg.stopEvent = function(event) {
    if (event.preventDefault) {
        event.preventDefault();
        event.stopPropagation();
    }
    else {
        event.returnValue = false;
        event.cancelBubble = true;
    }
};

TracWysiwyg.setStyle = function(element, object) {
    var style = element.style;
    for (var name in object) {
        style[name] = object[name];
    }
};

if (document.defaultView) {
    TracWysiwyg.getStyle = function(element, name) {
        var value = element.style[name];
        if (!value) {
            var style = element.ownerDocument.defaultView.getComputedStyle(element, null)
            value = style ? style[name] : null;
        }
        return value;
    };
}
else {
    TracWysiwyg.getStyle = function(element, name) {
        return element.style[name] || element.currentStyle[name];
    };
}

TracWysiwyg.elementPosition = function(element) {
    function vector(left, top) {
        var value = [ left, top ];
        value.left = left;
        value.top = top;
        return value;
    }
    var position = TracWysiwyg.getStyle(element, "position");
    var left = 0, top = 0;
    for (var node = element; node; node = node.offsetParent) {
        left += node.offsetLeft || 0;
        top += node.offsetTop || 0;
    }
    if (position != "absolute") {
        return vector(left, top);
    }
    var offset = TracWysiwyg.elementPosition(element.offsetParent);
    return vector(left - offset.left, top - offset.top);
};

TracWysiwyg.getSelfOrAncestor = function(element, name) {
    var target = element;
    var d = element.ownerDocument;
    if (name instanceof RegExp) {
        while (target && target != d) {
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
    }
    else {
        name = name.toLowerCase();
        while (target && target != d) {
            switch (target.nodeType) {
            case 1: // element
                if (target.tagName.toLowerCase() == name) {
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

/*TracWysiwyg.unserializeFromHref = function(href, name) {
    var attrs = {};
    if (href.indexOf("#") !== -1) {
        var pieces = href.replace(/^[^#]*#/, '').split(/&/g);
        var length = pieces.length;
        for (var i = 0; i < length; i++) {
            var pair = pieces[i].split(/=/g, 2);
            attrs[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        }
    }
    return name ? attrs[name] : attrs;
};*/

TracWysiwyg.getTextContent = (function() {
    var anonymous = document.createElement("div");
    if (typeof anonymous.textContent != "undefined") {
        return function(element) { return element.textContent };
    }
    else if (typeof anonymous.innerText != "undefined") {
        return function(element) { return element.innerText };
    }
    else {
        return function(element) { return null };
    }
})();

TracWysiwyg.initialize = function() {
    if ("replace".replace(/[a-e]/g, function(m) { return "*" }) != "r*pl***") {
        return;
    }
    if (typeof document.designMode == "undefined") {
        return;
    }
    TracWysiwyg.tracPaths = TracWysiwyg.getTracPaths();
    if (!TracWysiwyg.tracPaths) {
        return;
    }
    var options = TracWysiwyg.getOptions();
    var textareas = document.getElementsByTagName("textarea");
    var editors = [];
    for (var i = 0; i < textareas.length; i++) {
        var textarea = textareas[i];
        if (/\bwikitext\b/.test(textarea.className || "")) {
            editors.push(TracWysiwyg.newInstance(textarea, options));
        }
    }
    return editors;
};

// vim:et:ai:ts=4
