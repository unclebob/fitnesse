"use strict";
/*jslint devel: true, undef: true, browser: true, continue: true, sloppy: true, stupid: true, vars: true, plusplus: true, regexp: true, maxerr: 50, indent: 4 */
/****
 vim:sw=4:et:ai

 Wysiwyg editor for FitNesse, based on the Trac Wysiwyg editor written by
 OpenGroove and Ciklone, BSD licensed.

 General rules are:
  - text is rendered to a Fragment node
  - div elements separate nested blocks e.g. collapsible sections

 ****/

var Wysiwyg = function (textarea, options) {

	CodeMirror.commands.autocomplete = function(cm) {
        cm.showHint({hint: CodeMirror.hint.fitnesse_anyword});
    };

	CodeMirror.commands.save = function(cm) {
    	$(document.f).submit();
    	return false;
    };

    this.codeMirrorEditor = CodeMirror.fromTextArea(textarea, {
        mode: "fitnesse",
        lineNumbers: true,
        foldGutter: true,
        showCursorWhenSelecting: true,
        viewportMargin: Infinity,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
		extraKeys: {
			"Ctrl-Space": "autocomplete"
		}
    });

    var self = this;
    var editorMode = Wysiwyg.getEditorMode();

    this.textarea = textarea;
    this.options = options || {};

    this.frame = this.createEditable(document, textarea);

    this.contentWindow = window;
    this.contentDocument = this.contentWindow.document;

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
    this.codeMirrorEditor.getWrapperElement().style.display = this.frame.style.display = "none";

    textarea.parentNode.insertBefore(this.toggleEditorButtons, textarea);
    textarea.parentNode.insertBefore(this.textareaToolbar, textarea);
    textarea.parentNode.insertBefore(this.wysiwygToolbar, textarea);

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
        self.codeMirrorEditor.getWrapperElement().style.display = self.textareaToolbar.style.display = "";
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
            return (match[1] === "true");
        }
    }

    return result;
};

Wysiwyg.getWrapOn = function () {
    return Wysiwyg.getBooleanFromCookie('textwrapon', false);
};

Wysiwyg.getAutoformat = function () {
    return Wysiwyg.getBooleanFromCookie('textautoformat', false);
};

Wysiwyg.prototype.listenerToggleEditor = function (type) {
    var self = this;
    var setEditorMode = function (mode) {
        if (mode !== "wysiwyg") {
            mode = "textarea";
        }
        Wysiwyg.editorMode = mode;
        Wysiwyg.setCookie("wysiwyg", mode);
    };


    switch (type) {
    case "textarea":
        return function (event) {
            var wrappedElement = self.codeMirrorEditor.getWrapperElement();
            if (wrappedElement.style.display === "none") {
                self.hideAllMenus();
                if (event && !event.initializing) { self.loadWikiText(); }
                wrappedElement.style.display = "";
                wrappedElement.setAttribute("tabIndex", "");
                self.syncTextAreaHeight();
                self.frame.style.display = self.wysiwygToolbar.style.display = "none";
                self.frame.setAttribute("tabIndex", "-1");
                self.textareaToolbar.style.display = "";
                self.codeMirrorEditor.refresh();
                setEditorMode(type);
            }
            self.focusTextarea();
        };
    case "wysiwyg":
        return function (event) {
            var frame = self.frame;
            var wrappedElement = self.codeMirrorEditor.getWrapperElement();
            if (frame.style.display === "none") {
                try {
                    self.loadWysiwygDocument();
                } catch (e) {
                    Wysiwyg.stopEvent(event || window.event);
                    alert("Failed to activate the wysiwyg editor.");
                    throw e;
                }
                wrappedElement.style.display = "none";
                wrappedElement.setAttribute("tabIndex", "-1");
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
    return this.codeMirrorEditor.getWrapperElement().style.display === "none" ? "wysiwyg" : "textarea";
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
                    self.codeMirrorEditor.setValue(self.domToWikitext(body, self.options));
                    self.codeMirrorEditor.save();
                }
            }
            if (Wysiwyg.getAutoformat()) {
                var formatter = new WikiFormatter();
                self.codeMirrorEditor.setValue(formatter.format(self.codeMirrorEditor.getValue()));
                self.codeMirrorEditor.save();
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
        '<li title="Bold (Ctrl+B)"><a id="wt-strong" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M11.63 7.82C12.46 7.24 13 6.38 13 5.5 13 3.57 11.43 2 9.5 2H4v12h6.25c1.79 0 3.25-1.46 3.25-3.25 0-1.3-.77-2.41-1.87-2.93zM6.5 4h2.75c.83 0 1.5.67 1.5 1.5S10.08 7 9.25 7H6.5V4zm3.25 8H6.5V9h3.25c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z"/></svg></a></li>',
        '<li title="Italic (Ctrl+I)"><a id="wt-em" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M7 2v2h2.58l-3.66 8H3v2h8v-2H8.42l3.66-8H15V2z"/></svg></a></li>',
        '<li title="Strike through"><a id="wt-strike" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M8 15h2v-4H8v4zM4 2v2h4v3h2V4h4V2H4zm-1 8h12V8H3v2z"/></svg></a></li>',
        '<li title="Escape"><a id="wt-escape" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <path d="m 11,15.5 a 1.5,1.5 0 1 1 -3,0 1.5,1.5 0 1 1 3,0 z" transform="translate(-3.5,-1)" id="path4665" style="fill:#000000;fill-opacity:1;stroke:none" /> <path d="M 6,12 5,3 7,3 z" id="path4667" style="fill:#000000;fill-opacity:1;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" /> <path d="m 10,8 c 4,0 4,0 4,0 l 0,0" id="path4669" style="fill:none;stroke:#000000;stroke-width:1.89999998;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '<li title="Remove format"><a id="wt-remove" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M2.27 4.55L7.43 9.7 5 15h2.5l1.64-3.58L13.73 16 15 14.73 3.55 3.27 2.27 4.55zM5.82 3l2 2h1.76l-.55 1.21 1.71 1.71L12.08 5H16V3H5.82z"/></svg></a></li>',
        '<li title="Image"><a id="wt-image" href="#insert-image"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <path d="M 16.086957,14.444444 V 3.5555556 C 16.086957,2.7 15.382609,2 14.521739,2 H 3.5652174 C 2.7043478,2 2,2.7 2,3.5555556 V 14.444444 C 2,15.3 2.7043478,16 3.5652174,16 H 14.521739 c 0.86087,0 1.565218,-0.7 1.565218,-1.555556 z M 6.3043478,10.166667 8.2608696,12.507778 11,9 14.521739,13.666667 H 3.5652174 l 2.7391304,-3.5 z" /> </svg></a></li>',
        '<li title="Hash table"><a id="wt-hash-table" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" id="svg2985" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" d="m 10,15 c 6,0 1,-6 6,-6 M 10,3 c 6,0 1,6 6,6" /> <path d="M 8,15 C 2,15 7,9 2,9 M 8,3 C 2,3 7,9 2,9" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg></a></li>',
        '</ul>',
        '<ul>',
        '<li title="Link"><a id="wt-link" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M 11,1.75 C 9.8,1.75 8.675,2.2544118 7.85,2.975 8.45,3.1191176 8.975,3.3352941 9.425,3.6235294 9.875,3.3352941 10.4,3.1911765 11,3.1911765 c 1.65,0 3,1.2970588 3,2.8823529 v 3.6029412 c 0,1.5852944 -1.35,2.8823534 -3,2.8823534 -1.65,0 -3,-1.297059 -3,-2.8823534 V 7.1544118 C 7.625,6.7220588 7.1,6.4338235 6.5,6.4338235 V 9.6764706 C 6.5,12.054412 8.525,14 11,14 c 2.475,0 4.5,-1.945588 4.5,-4.3235294 V 6.0735294 C 15.5,3.6955882 13.475,1.75 11,1.75 z"/> <path d="m 6.5,16.161765 c 1.2,0 2.325,-0.504412 3.15,-1.225 -0.6,-0.144118 -1.125,-0.360294 -1.575,-0.64853 -0.45,0.288236 -0.975,0.432353 -1.575,0.432353 -1.65,0 -3,-1.297059 -3,-2.882353 V 8.2352941 C 3.5,6.65 4.85,5.3529412 6.5,5.3529412 c 1.65,0 3,1.2970588 3,2.8823529 v 2.5220589 c 0.375,0.432353 0.9,0.720588 1.5,0.720588 V 8.2352941 C 11,5.8573529 8.975,3.9117647 6.5,3.9117647 4.025,3.9117647 2,5.8573529 2,8.2352941 v 3.6029409 c 0,2.377941 2.025,4.32353 4.5,4.32353 z"/> </svg></a></li>',
        '<li title="Unlink"><a id="wt-unlink" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="m 10.936089,1.8927186 0.132989,1.4243861 c 1.462877,-0.1424386 2.726271,0.9970702 2.925754,2.5638949 l 0.332472,3.5609651 c 0.132989,1.5668243 -0.930921,2.9199913 -2.393798,3.1336493 -1.462877,0.142439 -2.7262711,-0.99707 -2.9257543,-2.563895 L 7.6778636,10.154158 C 7.8773468,12.504395 9.8056846,14.213658 12,14 14.194315,13.786342 15.790181,11.720982 15.590698,9.3707454 L 15.258226,5.8097803 C 15.058743,3.388324 13.130405,1.6790607 10.936089,1.8927186 z"/> <path d="M 6.9464251,16.136579 6.8134363,14.712193 C 5.3505594,14.854632 4.0871656,13.715123 3.9541768,12.148298 L 3.6881992,8.587333 C 3.488716,7.0205084 4.6191209,5.6673417 6.0819978,5.5249031 7.5448748,5.3824645 8.8082685,6.5219733 8.9412573,8.0887979 L 10.271145,7.9463593 C 10.071662,5.5961224 8.1433244,3.8868591 5.949009,4.100517 3.7546936,4.3141749 2.0923334,6.3795347 2.2918167,8.7297716 l 0.2659776,3.5609654 c 0.2659776,2.350237 2.1943154,4.0595 4.3886308,3.845842 z"/> </svg></a></li>',
        '</ul>',
        '<ul class="non-table">',
        '<li title="List"><a id="wt-ul" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M7 10h9V8H7v2zm0-7v2h9V3H7zm0 12h9v-2H7v2zm-4-5h2V8H3v2zm0-7v2h2V3H3zm0 12h2v-2H3v2z"/></svg></a></li>',
        '<li title="Indent"><a id="wt-indent" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M8 12h8v-2H8v2zM5.5 9L2 5.5v7L5.5 9zM2 16h14v-2H2v2zM2 2v2h14V2H2zm6 6h8V6H8v2z"/></svg></a></li>',
        '<li title="Outdent"><a id="wt-outdent" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M10 10H2v2h8v-2zm0-4H2v2h8V6zM2 16h14v-2H2v2zm14-3.5v-7L12.5 9l3.5 3.5zM2 2v2h14V2H2z"/></svg></a></li>',
        '<li title="Horizontal rule"><a id="wt-hr" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <path d="m 2,9 14,0 0,0" id="path3180" style="fill:none;stroke:#000000;stroke-width:2.16024685;stroke-linecap:butt;stroke-opacity:1" /> </svg></a></li>',
        '<li title="Table"><a id="wt-table" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"><path style="stroke-width:2;stroke-opacity:1;marker-end:none;fill:none;stroke:#000000" d="M 3,3 3,15 15,15 15,3 z"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,7 10,0 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,11 10,0"/></svg></a></li>',
        '</ul>',
        '<ul class="non-table">',
        '<li title="Collapsible section (default closed)"><a id="wt-collapsible-closed" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;fill-rule:nonzero" d="M 16,5.5 12.5,9 16,12.5 z"/> <path style="fill:#000000;fill-opacity:1;stroke:none" d="M 4.5,11.5 8,8 4.5,4.5 z" /> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none" d="m 15.5,2.5 c -14,0 -13,0 -13,0 l 0,13" /> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" d="m 10,10.5 c 10,0 5.5,0 5.5,0" /> <path d="m 6.5,14.5 c 10,0 9,0 9,0" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '<li title="Collapsible section (default open)"><a id="wt-collapsible-open" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18"> <path d="M 16,5.5 12.5,9 16,12.5 z" style="fill:none;stroke:none" /> <path d="M 4.5,4.5 8,8 11.5,4.5 z" style="fill:#000000;fill-opacity:1;stroke:none" /> <path d="m 15.5,2.5 c -14,0 -13,0 -13,0 l 0,13" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="m 6.5,10.5 c 10,0 9,0 9,0" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="m 6.5,14.5 c 10,0 9,0 9,0" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '<li title="Collapsible section (hidden)"><a id="wt-collapsible-hidden" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;fill-rule:nonzero" d="M 16,5.5 12.5,9 16,12.5 z" /> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-opacity:0.50196081;stroke-miterlimit:4;stroke-dasharray:none" d="m 15.5,2.5 c -14,0 -13,0 -13,0 l 0,13" /> <path d="m 6.5,14.5 c 10,0 9,0 9,0" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:0.50196081;stroke-dasharray:none"/> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:0.50196078;stroke-dasharray:none" d="m 9,10 c 10,0 6.5,0 6.5,0"/> <path style="fill:#000000;fill-opacity:1;stroke:#ffffff;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" d="M 9 3 C 7 3 5 5 5 7 C 5 8.1331878 5.4944498 9.0940909 6.09375 9.90625 C 4.2247342 11.736073 6.3209481 11.750998 7.625 11.625 C 9.4559533 13.372886 13.5 15.5 13.5 15.5 C 13.5 15.5 13.134898 12.287173 13.03125 10.71875 C 14.233828 10.363035 15.739596 9.685859 13.21875 8.75 C 13.349978 8.2958135 13.5 7.7895642 13.5 7 C 13.5 5 11 3 9 3 z " /> <path style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:#ffffff;stroke-width:0.5;stroke-linecap:square;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" d="m 8.5,6.75 a 0.5,0.25 0 1 1 -1,0 0.5,0.25 0 1 1 1,0 z" transform="translate(-0.5,-0.75)" /> <path d="m 8.5,6.75 a 0.5,0.25 0 1 1 -1,0 0.5,0.25 0 1 1 1,0 z" style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:#ffffff;stroke-width:0.5;stroke-linecap:square;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" transform="translate(2.5,-0.5)" /> <path style="fill:#ffffff;fill-opacity:1;fill-rule:nonzero;stroke:#ffffff;stroke-width:0.5;stroke-linecap:square;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" d="M 9,9 A 0.25,0.5 0 0 1 8.9308389,9.3452379 L 8.75,9 z" transform="matrix(3,0,0,1,-17,0)" /> </svg> </a></li>',
        '<li title="Remove collapsible section"><a id="wt-remove-collapsible" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <path d="M 16,5.5 12.5,9 16,12.5 z" style="fill:none;stroke:none" /> <path d="M 14,3 C 0,3 3,3 3,3 l 0,12" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="M 6.5394551,10.5 14,10.543804" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="m 6.5,14.5 c 10,0 9,0 9,0" style="fill:none;stroke:#000000;stroke-width:2;stroke-linecap:square;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="M 1,3 16,16" style="fill:none;stroke:#000000;stroke-width:1.72800004;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> <path d="M 1.4743476,1 17,15" style="fill:none;stroke:#ffffff;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '</ul>',
        '<ul class="in-table">',
        '<li title="Insert cell before"><a id="wt-insert-cell-before" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7.5,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,7 10,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3.0000002" height="3" x="4" y="7.5"/> </svg> </a></li>',
        '<li title="Insert cell after (|)"><a id="wt-insert-cell-after" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" id="svg3053" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 10.5,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="11" y="7.5"/> </svg> </a></li>',
        '<li title="Insert row above"><a id="wt-insert-row-before" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7.5 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="4"/> <rect y="4" x="4" height="3" width="2.5" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="4"/> </svg> </a></li>',
        '<li title="Insert row below"><a id="wt-insert-row-after" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,10.5 10,0"/> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="11" /> <rect y="11" x="4" height="3" width="2.5" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="11"/> </svg></a></li>',
        '<li title="Insert column before"><a id="wt-insert-col-before" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7.5,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0"/> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="2.5" x="4" y="4" /> <rect y="11.5" x="4" height="2.5" width="3" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="4" y="7.5"/> </svg> </a></li>',
        '<li title="Insert column after"><a id="wt-insert-col-after" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" id="svg3307" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 10.5,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,11 10,0"/> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="2.5" x="11" y="4" /> <rect y="11.5" x="11" height="2.5" width="3" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="11" y="7.5" /> </svg> </a></li>',
        '<li title="Delete cell (Ctrl-|)"><a id="wt-delete-cell" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="7.5" /> </svg> </a></li>',
        '<li title="Delete row"><a id="wt-delete-row" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="7.5"/> <rect y="7.5" x="4" height="3" width="2.5" style="fill:#ff0000;fill-opacity:1;stroke:none" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="7.5" /> </svg></a></li>',
        '<li title="Delete column"><a id="wt-delete-col" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="3" height="2.5" x="7.5" y="4" /> <rect y="11.5" x="7.5" height="2.5" width="3" style="fill:#ff0000;fill-opacity:1;stroke:none" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="7.5" /> </svg> </a></li>',
        '<li title="Table"><a id="wt-table-in-table" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"><path style="stroke-width:2;stroke-opacity:1;marker-end:none;fill:none;stroke:#000000" d="M 3,3 3,15 15,15 15,3 z"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,7 10,0 0,0"/><path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 4,11 10,0"/></svg></a></li>',
        '<li title="Delete table"><a id="wt-remove-table" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <g transform="matrix(0.91666667,0,0,0.91666667,1.25,0.25)"> <path d="M 3,3 3,15 15,15 15,3 z" style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" /> <path d="m 7,4 0,10 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 11,4 0,10 0,0 0,0 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 4,7 10,0 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 4,11 10,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> </g> <path d="m 2,4 13,12 0,0" style="fill:none;stroke:#000000;stroke-width:1.19087446px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" /> <path d="m 2,2 14,13 0,0" style="fill:none;stroke:#ffffff;stroke-width:1.72819757;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '</ul>',
        '<ul class="in-hash-table">',
        '<li title="Insert row above"><a id="wt-insert-row-before" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7.5 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="4"/> <rect y="4" x="4" height="3" width="2.5" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="4"/> </svg> </a></li>',
        '<li title="Insert row below"><a id="wt-insert-row-after" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" version="1.1"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z"/> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,10.5 10,0"/> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="11" /> <rect y="11" x="4" height="3" width="2.5" style="fill:#00ff00;fill-opacity:1;stroke:none" /> <rect style="fill:#00ff00;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="11"/> </svg></a></li>',
        '<li title="Delete row"><a id="wt-delete-row" href="#"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"> <path style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" d="M 3,3 3,15 15,15 15,3 z" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 7,4 0,10 0,0" /> <path style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" d="m 11,4 0,10 0,0 0,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,7 10,0 0,0"/> <path style="fill:none;stroke:#000000;stroke-width:1px;stroke-opacity:1" d="m 4,11 10,0" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="2.5" height="3" x="11.5" y="7.5"/> <rect y="7.5" x="4" height="3" width="2.5" style="fill:#ff0000;fill-opacity:1;stroke:none" /> <rect style="fill:#ff0000;fill-opacity:1;stroke:none" width="3" height="3" x="7.5" y="7.5" /> </svg></a></li>',
        '<li title="Delete table"><a id="wt-remove-table" href="#"><svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="18" height="18" viewBox="0 0 18 18"> <g transform="matrix(0.91666667,0,0,0.91666667,1.25,0.25)"> <path d="M 3,3 3,15 15,15 15,3 z" style="fill:none;stroke:#000000;stroke-width:2;stroke-opacity:1;marker-end:none" /> <path d="m 7,4 0,10 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 11,4 0,10 0,0 0,0 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 4,7 10,0 0,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> <path d="m 4,11 10,0" style="fill:none;stroke:#000000;stroke-width:1;stroke-opacity:1" /> </g> <path d="m 2,4 13,12 0,0" style="fill:none;stroke:#000000;stroke-width:1.19087446px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" /> <path d="m 2,2 14,13 0,0" style="fill:none;stroke:#ffffff;stroke-width:1.72819757;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none" /> </svg> </a></li>',
        '</ul>'];
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
        case "image":
            return [ self.insertImage ];
		case "table":
		case "table-in-table":
			return [ self.insertTable ];
        case "hash-table":
            return [ self.insertHashTable ];
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
    var codeMirror = this.codeMirrorEditor;
    var container = this.textareaToolbar;

    $('#tt-spreadsheet-to-wiki', container).click(function () {
        var translator = new SpreadsheetTranslator();
        translator.parseExcelTable(codeMirror.getValue());
        codeMirror.setValue(translator.getFitNesseTables());
        codeMirror.focus();
    });
    $('#tt-wiki-to-spreadsheet', container).click(function () {
        var selection = codeMirror.getValue();
        selection = selection.replace(/\r\n/g, '\n');
        selection = selection.replace(/\r/g, '\n');
         // remove the last | at the end of the line
        selection = selection.replace(/\|\n/g, '\n');
         // replace all remaining | with \t
        selection = selection.replace(/\|/g, '\t');
        codeMirror.setValue(selection);
        codeMirror.focus();
    });

    $('#tt-format-wiki', container).click(function () {
        var formatter = new WikiFormatter();
        var scrollInfo = codeMirror.getScrollInfo();
        var cursorInfo = codeMirror.getDoc().getCursor();
        codeMirror.setValue(formatter.format(codeMirror.getValue()));
        codeMirror.scrollTo(scrollInfo.left, scrollInfo.top);
        codeMirror.getDoc().setCursor(cursorInfo.line, cursorInfo.ch);
        codeMirror.focus();
    });

    $('#tt-insert-template', container).click(function () {
        var selectedValue = $('#tt-template-map').val();
        var inserter = new TemplateInserter();
        inserter.insertInto(selectedValue, codeMirror);
        codeMirror.focus();
    });

    function setWrap(wrap) {
        if (wrap) {
            codeMirror.setOption("lineWrapping", true);
            Wysiwyg.setCookie("textwrapon", "true");
        } else {
            codeMirror.setOption("lineWrapping", false);
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

Wysiwyg.prototype.execCommand = function (name, arg, selectionRange) {
    this.frame.focus();
    if (selectionRange) {
        this.selectRange(selectionRange.startContainer, selectionRange.startOffset, selectionRange.endContainer, selectionRange.endOffset);
        this.selectionChanged();
    }
    return this.contentDocument.execCommand(name, false, arg);
};

Wysiwyg.prototype.isHashTable = function (node) {
    return node.tagName === "TABLE" && /hashtable/.test(node.className);
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
            if (element && !self.isHashTable(element) &&
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

        function wikitextToOnelinerFragment (wikitext, contentDocument) {
            var source = this.wikitextToFragment(wikitext, contentDocument);
            var fragment = contentDocument.createDocumentFragment();
            this.collectChildNodes(fragment, source.firstChild);
            return fragment;
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
                fragment = wikitextToOnelinerFragment(wikiText.replace('\n', ' '), self.contentDocument, self.options);
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
    var fragment = this.wikitextToFragment(this.codeMirrorEditor.getValue(), this.contentDocument, this.options);
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
    this.codeMirrorEditor.setValue(this.domToWikitext(this.frame, this.options));
    this.codeMirrorEditor.save();
    this.savedWysiwygHTML = null;
};

Wysiwyg.prototype.focusTextarea = function () {
    this.codeMirrorEditor.focus();
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


Wysiwyg.prototype.normalizeLink = function (link) {
    if (/^[\/.#]/.test(link)) {
        link = encodeURIComponent(link);
    }
    if (/^[^"']/.test(link) && /\s/.test(link)) {
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

Wysiwyg.prototype.insertImage = function () {
    var self = this;
    var insertImage = 'insert-image';
    var path = ['files'];
    var filesList = $('#' + insertImage + ' ul');
    var pathName = $('#' + insertImage + ' pre');
    var selectionRange = self.getSelectionRange();

    loadFiles();

    var insertImageBox = $('#' + insertImage + " > div");
    insertImageBox
        .on('click', 'span', function () {
            path = Array.prototype.slice.call($(this).data());
            loadFiles();
        })
        .on('click', 'li.directory', function () {
            path.push($(this).data().name);
            loadFiles();
        })
        .on('click', 'li.file', function () {
            window.location.hash = "";
            insertImageBox.off();
            self.execCommand("insertimage", pathStr() + $(this).data().name, selectionRange);
        })
        .on('click', 'button', function () {
            var url = $('#' + insertImage + ' input').val();
            window.location.hash = "";
            insertImageBox.off();
            self.execCommand("insertimage", url, selectionRange);
        });

    window.location.hash = insertImage;

    function loadFiles() {

        $.getJSON(pathStr(), {responder: 'files', format: 'json'}, function (data) {
            breadcrumbHtml();
            filesList.empty();
            $(data).each(function (i, e) {
                var h = $("<li>" + e.name + (e.directory ? '/' : '') + "</li>").attr('class', e.directory ? 'directory' : 'file').data(e);
                filesList.append(h);
            });
        });
    }

    function pathStr() {
        var s = "";
        for (var i in path) {
            s = s + path[i] + '/';
        }
        return s;
    }

    function breadcrumbHtml() {
        var s = [];
        pathName.empty();
        for (var i in path) {
            s.push(path[i]);
            var d = $.extend({}, s);
            d.length = s.length;
            pathName.append(" / ");
            pathName.append($("<span rel='" + s + "'>" + path[i] + "</span>").data(d));
        }
    }

};

Wysiwyg.prototype.insertTable = function () {
    if (this.selectionContainsTagName("pre")) {
        return;
    }
    var self = this;
    function nest(html) {
        if (self.selectionContainsTagName("table")) {
            return '<div class="nested">' + html + "</div>";
        }
        return html;
    }
    var id = this.generateDomId();
    this.insertHTML(nest(this.tableHTML(id, 2, 2, "wiki")));
    var element = this.contentDocument.getElementById(id);
    if (element) {
        this.selectNodeContents(element);
    }
    this.selectionChanged();
};

Wysiwyg.prototype.insertHashTable = function () {
    if (this.selectionContainsTagName("pre")) {
        return;
    }
    var id = this.generateDomId();
    this.insertHTML(this.tableHTML(id, 2, 2, "hashtable"));
    var element = this.contentDocument.getElementById(id);
    if (element) {
        element.setAttribute('class', 'hashtable');
        this.selectNodeContents(element);
    }
    this.selectionChanged();
};

Wysiwyg.prototype._tableHTML = function (row, col, className) {
    var tr = "<tr>" + ((1 << col) - 1).toString(2).replace(/1/g, "<td></td>") + "</tr>";
    var html = [
        '<table class="', className, '">', '<tbody>',
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
    var rows = $('> tbody > tr', table);
    var maxCells = Math.max.apply(Math, $.map(rows, function (e) {
        var tds = $('> td', e);
        tds.removeAttr('colspan');
        return tds.length;
    }));
    rows.each(function () {
        var s = $('> td', this).length;
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
    var text = this.domToWikitext(fragment, this.options).replace(/\s+$/, "");

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
    var hashTable = false;

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
            hashTable |= this.isHashTable(node);
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

    if (hashTable) {
        $(".wysiwyg-toolbar .non-table").hide();
        $(".wysiwyg-toolbar .in-table").hide();
        $(".wysiwyg-toolbar .in-hash-table").show();
    } else if (status["table"]) {
        $(".wysiwyg-toolbar .non-table").hide();
        $(".wysiwyg-toolbar .in-table").show();
        $(".wysiwyg-toolbar .in-hash-table").hide();
    } else {
        $(".wysiwyg-toolbar .non-table").show();
        $(".wysiwyg-toolbar .in-table").hide();
        $(".wysiwyg-toolbar .in-hash-table").hide();
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


if (window.getSelection) {
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
    Wysiwyg.prototype.tableHTML = function (id, row, col, className) {
        var html = this._tableHTML(row, col, className);
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
    // For IE <= 8
    Wysiwyg.prototype.insertParagraphOnEnter = null;
    Wysiwyg.prototype.insertLineBreak = function () {
        this.insertHTML("<br/>");
    };
    Wysiwyg.prototype.insertLineBreakOnShiftEnter = null;
    Wysiwyg.prototype.tableHTML = function (id, row, col, className) {
        var html = this._tableHTML(row, col, className);
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

Wysiwyg.count = 0;

Wysiwyg.newInstance = function (textarea, options) {
    return new Wysiwyg(textarea, options);
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
        "expires=" + expires.toUTCString() ];
    document.cookie = pieces.join(";");
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

// Find parent element of type 'name', stop if an element of type 'notName' is found.
Wysiwyg.getSelfOrAncestor = function (element, name, notName) {
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
                } else if (target.tagName.toLowerCase() === notName || target.tagName.toLowerCase() === "div") {
                    return null;
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

Wysiwyg.initialize = function (textArea) {
    if ("replace".replace(/[a-e]/g, function () { return "*"; }) !== "r*pl***") {
        return;
    }
    if (typeof document.designMode === undefined) {
        return;
    }
    var options = Wysiwyg.getOptions();
    return Wysiwyg.newInstance(textArea, options);
};

// vim:et:ai:ts=4
