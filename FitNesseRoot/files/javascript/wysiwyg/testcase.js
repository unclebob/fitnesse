$(function() {
    TracWysiwyg.tracPaths = { base: ".", stylesheets: [] };
    var options = TracWysiwyg.getOptions();
    var instance = new TracWysiwyg(document.getElementById("textarea"), options);
    var contentDocument = instance.contentDocument;

    var d = document;
    var wysiwygHtml = d.getElementById("wysiwyg-html");
    var showWysiwygHtml = d.getElementById("show-wysiwyg-html");
    setTimeout(function() {
        if (showWysiwygHtml.checked) {
            var body = contentDocument.body;
            var browserIE = body.attachEvent ? true : false;
            var elements = body.getElementsByTagName("br");
            var count = 0;
            var html = body.innerHTML.replace(/<[Bb][Rr] *[^>\/]*/g, function(value) {
                var element = elements[count++];
                var attributes = element.attributes;
                var length = attributes.length;
                if (length == 0)
                    return value;
                var texts = [ value ];
                for (var i = 0; i < length; i++) {
                    var attr = attributes[i];
                    if (!browserIE || !!element[attr.name]) {
                        texts.push(' ', attr.name, '="', attr.value, '"');
                    }
                }
                return texts.join("");
            });
            if (wysiwygHtml.value != html) {
                wysiwygHtml.value = html;
            }
        }
        setTimeout(arguments.callee, 500);
    }, 500);

    function generate(dom, wikitext, options, withoutDomToWikitext, withoutWikitextToFragment) {
        dom = dom.cloneNode(true);
        var anonymous = dom.ownerDocument.createElement("div");
        anonymous.appendChild(dom);

        if (!withoutWikitextToFragment) {
            var fragment = instance.wikitextToFragment(wikitext, contentDocument, options);
            var generated = contentDocument.createElement("div");
            generated.appendChild(fragment);
            var generatedHtml = generated.innerHTML;
            if (!generated.addEventListener || window.opera) {
                generatedHtml = generatedHtml.replace(/\n\r/g, "\uffff").replace(/\uffff\n?/g, "\n");
            }
            this.assertEqual(anonymous.innerHTML, generatedHtml, "wikitextToFragment");
        }
        if (!withoutDomToWikitext) {
            this.assertEqual(wikitext, instance.domToWikitext(anonymous, options), "domToWikitext");
        }
    }

    function generateFragment(dom, wikitext, options) {
        generate.call(this, dom, wikitext, options, true, false);
    }

    function generateWikitext(dom, wikitext, options) {
        generate.call(this, dom, wikitext, options, false, true);
    }

    function run() {
        var unit = new TracWysiwyg.TestUnit();
        var fragment = unit.fragment;
        var element = unit.element;
        var br = function() { return element("br") };
        var a = function(link, label) {
            var attrs = {
                href: link,
                title: link,
                'data-wysiwyg-link': link,
                onclick: "return false;" };
            return element("a", attrs, label || link);
        };

        unit.add("treeWalk", function() {
            var list;
            function iterator(node) {
                var value;
                if (node) {
                    switch (node.nodeType) {
                    case 1: value = node.tagName.toLowerCase(); break;
                    case 3: value = "#text"; break;
                    }
                }
                else {
                    value = "(null)";
                }
                list.push(value);
            }

            function doTreeWalk(expected, dom) {
                list = [];
                instance.treeWalk(dom, iterator);
                this.assertEqual(expected, list.join(" "));

                list = [];
                instance._treeWalkEmulation(dom, iterator);
                this.assertEqual(expected, list.join(" "));
            }

            doTreeWalk.call(this, "p #text (null)", element("div", element("p", "paragraph")));
            doTreeWalk.call(this, "#text (null)", element("div", element("p", "paragraph")).firstChild);
            doTreeWalk.call(this, "(null)", element("div", element("p")).firstChild);

            var dom = element("div");
            dom.innerHTML = [
                '<h2 id="Tables">Tables</h2>',
                '<p>',
                'Simple tables can be created like this:',
                '</p>',
                '<pre class="wiki">||Cell 1||Cell 2||Cell 3||',
                '||Cell 4||Cell 5||Cell 6||',
                '</pre><p>',
                'Display:',
                '</p>',
                '<table class="wiki">',
                '<tbody><tr><td>Cell 1</td><td>Cell 2</td><td>Cell 3',
                '</td></tr><tr><td>Cell 4</td><td>Cell 5</td><td>Cell 6',
                '</td></tr></tbody></table>',
                '<p>',
                'Note that more complex tables can be created using',
                '<a class="wiki" href="/practice/wiki/WikiRestructuredText#BiggerReSTExample">reStructuredText</a>.',
                '</p>' ].join("");
            var expected = [
                'h2', '#text', 'p', '#text', 'pre', '#text', 'p', '#text',
                'table', 'tbody',
                'tr', 'td', '#text', 'td', '#text', 'td', '#text',
                'tr', 'td', '#text', 'td', '#text', 'td', '#text',
                'p', '#text', 'a', '#text', '#text',
                '(null)'].join(" ");
            doTreeWalk.call(this, expected, dom);
        });

        unit.add("isLastChildInBlockNode", function() {
            var dom = fragment(
                element("p", element("br")),
                element("p", "foobar", element("br"), "foobar"),
                element("p", element("b", "foobar", element("br"))),
                element("p", element("b", "foobar"), element("br")),
                element("br"));
            var count = 0;
            function assert(expected, node) {
                this.assertEqual(expected, instance.isLastChildInBlockNode(node), "#" + (count++));
            }
            assert.call(this, true,  dom.childNodes[0].childNodes[0]);
            assert.call(this, false, dom.childNodes[1].childNodes[0]);
            assert.call(this, false, dom.childNodes[1].childNodes[1]);
            assert.call(this, true,  dom.childNodes[1].childNodes[2]);
            assert.call(this, false, dom.childNodes[2].childNodes[0].childNodes[0]);
            assert.call(this, true,  dom.childNodes[2].childNodes[0].childNodes[1]);
            assert.call(this, false, dom.childNodes[3].childNodes[0].childNodes[0]);
            assert.call(this, true,  dom.childNodes[3].childNodes[1]);
            assert.call(this, true,  dom.childNodes[4]);
        });

        unit.add("code block", function() {
            var dom = fragment(
                element("p", "`abc`"),
                element("pre", { "class": "wiki" }, "{{{code-block"));
            var wikitext = [
                "`abc`",
                "",
                "",
                "{{{",
                "{{{code-block",
                "}}}" ].join("\n");
            generate.call(this, dom, wikitext);
        });
        unit.add("code block nest", function() {
            var dom = fragment(
                element("pre", { "class": "wiki" }, "#!python\n= level 1\n{{{\n= level 2\n}}}\n= level 1"));
            var wikitext = [
                "{{{",
                "#!python",
                "= level 1",
                "{{{",
                "= level 2",
                "}}}",
                "= level 1",
                "}}}" ].join("\n");
            generate.call(this, dom, wikitext);
        });

        unit.add("paragraph", function() {
            var dom = fragment(
                element("p", "Paragraph continued..."),
                element("p", "Second paragraph continued..."));
            generateFragment.call(this, dom, [
                "Paragraph",
                "continued...",
                "",
                "Second paragraph",
                "continued...",
                "" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph continued...",
                "",
                "Second paragraph continued..." ].join("\n"));
        });

        unit.add("wiki macros", function() {
            var dom = fragment(
                element("table", element("tbody",
                    element("tr", element("td", "!c !2 Contents")),
                    element("tr", element("td", "!contents -g")))),
                element("p", "!path fitnesse.jar", br(), "!path classes"));
            generateFragment.call(this, dom, [
                "|!c !2 Contents|",
                "|!contents -g|",
                "",
                "!path fitnesse.jar",
                "!path classes" ].join("\n"));
        });

        unit.add("hr", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("hr"),
                element("ul",
                    element("li", "item 1"),
                    element("ol", element("li", "item 1.1"))),
                element("hr"));
            generateFragment.call(this, dom, [
                "Paragraph",
                "----",
                " * item 1",
                "   1. item 1.1",
                "----" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                "----",
                " * item 1",
                "   1. item 1.1",
                "",
                "----" ].join("\n"));
        });

        unit.add("escape !- .. -!", function() {
            var dom = element("p", "foo ", element("tt", "bar"), " baz");
            var wikitext = "foo !-bar-! baz";
            generate.call(this, dom, wikitext);
        });

        unit.add("bold italic", function() {
            var dom = element("p", element("b", element("i", "bold italic")));
            var wikitext = "'''''bold italic'''''";
            generate.call(this, dom, wikitext);
        });

        unit.add("bold", function() {
            var wikitext = [
                "The quick '''brown''' fox.",
                "",
                "The quick '''brown''' fox." ].join("\n");
            generateWikitext.call(this,
                fragment(
                    element("p", "The quick ", element("b", "brown"), " fox."),
                    element("p", "The quick ", element("strong", "brown"), " fox.")),
                wikitext);
            generateFragment.call(this,
                fragment(
                    element("p", "The quick ", element("b", "brown"), " fox."),
                    element("p", "The quick ", element("b", "brown"), " fox.")),
                wikitext);
        });
        unit.add("italic", function() {
            var wikitext = [
                "The quick ''brown'' fox.",
                "",
                "The quick ''brown'' fox." ].join("\n");
            generateWikitext.call(this,
                fragment(
                    element("p", "The quick ", element("i", "brown"), " fox."),
                    element("p", "The quick ", element("em", "brown"), " fox.")),
                wikitext);
            generateFragment.call(this,
                fragment(
                    element("p", "The quick ", element("i", "brown"), " fox."),
                    element("p", "The quick ", element("i", "brown"), " fox.")),
                wikitext);
        });
        unit.add("strike-through", function() {
            var dom = element("p", element("del", "strike-through"));
            var wikitext = "--strike-through--";
            generate.call(this, dom, wikitext);
        });
        unit.add("monospace", function() {
            var dom = element("p", "`monospace`",
                ", ", element("tt", "mono`s`pace"),
                ", ", "`mono", element("tt", "s"), "pace`");
            var wikitext = "`monospace`, {{{mono`s`pace}}}, `mono{{{s}}}pace`";
            generateFragment.call(this, dom, "`monospace`, {{{mono`s`pace}}}, `mono{{{s}}}pace`");
            generateWikitext.call(this, dom, "`monospace`, !-mono`s`pace-!, `mono!-s-!pace`");
        });
        unit.add("italic -> bold", function() {
            var dom = element("p",
                "normal",
                element("i", "italic"),
                element("b", "bold"),
                "normal");
            var wikitext = "normal''italic'''''bold'''normal";
            generate.call(this, dom, wikitext);
        });
        unit.add("bold -> italic", function() {
            var dom = element("p",
                "normal",
                element("b", "bold"),
                element("i", "italic"),
                "normal");
            var wikitext = "normal'''bold'''''italic''normal";
            generate.call(this, dom, wikitext);
        });
        unit.add("[ italic [ xyz ] bold ]", function() {
            var dom = element("p",
                "normal",
                element("i", "italic", element("b", "xyz")),
                element("b", "bold"),
                "normal");
            var wikitext = "normal''italic'''xyz''bold'''normal";
            generate.call(this, dom, wikitext);
        });
        unit.add("overlapped markups", function() {
            var dom = element("p",
                "normal",
                element("b", "bold",
                    element("i", "italic",
                        element("del", "strike-through")),
                    "sub"),
                ".");
            var wikitext = "normal'''bold''italic--strike-through--''sub'''.";
            generateFragment.call(this, dom, wikitext);
        });
        unit.add("repeated markups", function() {
            generateWikitext.call(this,
                element("p", "ab", element("b", "cd"), element("b", "ef"), "gh"),
                "ab'''cdef'''gh");
            generateWikitext.call(this,
                element("p", "ab", element("i", "cd"), element("i", "ef"), "gh"),
                "ab''cdef''gh");
            generateWikitext.call(this,
                element("p", "ab",
                             element("i", element("b", "cd")),
                             element("b", element("i", "ef")),
                             "gh"),
                "ab'''''cdef'''''gh");
        });
        unit.add("markups without text", function() {
            generateWikitext.call(this,
                element("p", "abc", element("b", ""), "def"),
                "abcdef");
            generateWikitext.call(this,
                element("p", "abc", element("i", ""), "def"),
                "abcdef");
            generateWikitext.call(this,
                element("p", "abc", element("tt", ""), "def"),
                "abcdef");
            generateWikitext.call(this,
                element("p", "abc", element("b", element("i", "")), "def"),
                "abcdef");
            generateWikitext.call(this,
                element("p", "abc", element("i", element("b", "")), "def"),
                "abcdef");
        });

        unit.add("! bold italic", function() {
            var dom = element("p", element("b", element("i", "bold",
                              element("tt", "'''''"), " italic")), ".");
            var wikitext = "'''''bold!-'''''-! italic'''''.";
            generate.call(this, dom, wikitext);
        });
        unit.add("! bold", function() {
            var dom = element("p", element("b", "bold", element("tt", "'''"), " bold"), ".");
            var wikitext = "'''bold!-'''-! bold'''.";
            generate.call(this, dom, wikitext);
        });
        unit.add("! italic", function() {
            var dom = element("p", element("i", "italic", element("tt", "''"), " italic"), ".");
            var wikitext = "''italic!-''-! italic''.";
            generate.call(this, dom, wikitext);
        });
        unit.add("! strike-through", function() {
            var dom = element("p", element("del", "strike", element("tt", "--"), "through"), ".");
            var wikitext = "--strike!----!through--.";
            generate.call(this, dom, wikitext);
        });
        unit.add("! monospace", function() {
            var dom = element("p", element("tt", "{{{monospace}}}"), " or ",
                    element("tt", "`monospace`"));
            var wikitext = "!-{{{monospace}}}-! or !-`monospace`-!";
            generate.call(this, dom, wikitext);
        });

        unit.add("WikiPageName", function() {
            var dom = fragment(
                element("p",
                    a("CamelCase", "CamelCase"),
                    " ", element("tt", "CamelCase"), " ",
                    a("FooBarA", "FooBarA"), " FOo ",
                    a("FoobarA", "FoobarA"), " ",
                    a("<ParentLink", "<ParentLink"), " ",
                    a(">ChildLink", ">ChildLink"), " ",
                    a(".AbsoluteLink", ".AbsoluteLink"), " ",
                    a(".AbsoluteLink.WikiPage", ".AbsoluteLink.WikiPage"),
                    " OneÅngström Oneångström setTextColor"));
            generateFragment.call(this, dom, 
                "CamelCase !-CamelCase-! FooBarA FOo FoobarA <ParentLink >ChildLink .AbsoluteLink .AbsoluteLink.WikiPage OneÅngström Oneångström setTextColor");
        });
        
        unit.add("links2wiki", function() {
        	var dom = fragment(
        		element("p",
        			a("TestPage", "label"),
        			a("TestPage", "läbel"),
        			a("TestPage", "TestPage"),
        			a("FrontPage?edit", "Edit"),
        			" button and add a ",
        			a("FitNesse.UserGuide.WikiWord", "!-WikiWord-!"),
        			a("http://external.link/bladieblah", "bla")
        		));
        		
        	var wikitext = "[[label][TestPage]]"
        		+ "[[läbel][TestPage]]"
        		+ "[[TestPage][TestPage]]"
        		+ "[[Edit][FrontPage?edit]] button and add a [[!-WikiWord-!][FitNesse.UserGuide.WikiWord]]"
        		+ "[[bla][http://external.link/bladieblah]]";
        	
        	// should go both ways (wiki2html and html2wiki)
        	generateWikitext.call(this, dom, wikitext);
        	generateFragment.call(this, dom, wikitext);
        });

        unit.add("citation", function() {
            var dom = fragment(
                element("blockquote", { "class": "citation" },
                    element("p", "This is the quoted text continued"),
                    element("blockquote", { "class": "citation" },
                        element("p", "a nested quote"),
                        element("blockquote", { "class": "citation" },
                            element("p", "a nested-nested quote")))),
                element("p", "A comment on the above"),
                element("blockquote", { "class": "citation" },
                    element("blockquote", { "class": "citation" },
                        element("p", "start 2nd level")),
                    element("p", "first level")));
            generateFragment.call(this, dom, [
                "> This is the quoted text",
                "> continued",
                "> > a nested quote",
                "> > > a nested-nested quote",
                "A comment on the above",
                "> > start 2nd level",
                ">first level" ].join("\n"));
            generate.call(this, dom, [
                "> This is the quoted text continued",
                "> > a nested quote",
                "> > > a nested-nested quote",
                "",
                "A comment on the above",
                "",
                "> > start 2nd level",
                "> first level" ].join("\n"));
        });

        unit.add("header", function() {
            var dom = fragment(
                element("h1", "Heading 1"),
                element("h2", { id: "anchor-2" }, "Heading 2"),
                element("h3", element("b", "Heading"), " ", element("i", "3")),
                element("h4", { id: "アンカー-4" },
                    "Heading 4 with ",
                    a("WikiStart", "WikiStart")),
                element("h5", "Heading 5"),
                element("h6", { id: "anchor-6" }, "Heading 6"));
            generate.call(this, dom, [
                "!1 Heading 1",
                "!2 Heading 2 #anchor-2",
                "!3 '''Heading''' ''3''",
                "!4 Heading 4 with [[WikiStart][WikiStart]] #アンカー-4",
                "!5 Heading 5",
                "!6 Heading 6 #anchor-6" ].join("\n"));
        });

        unit.add("header 2", function() {
            var dom = fragment(
                element("h1", "Heading 1"),
                element("h2", { id: "anchor-2" }, "Heading 2"),
                element("h3", element("b", "Heading"), " ", element("i", "3")),
                element("h4", { id: "アンカー-4" },
                    "Heading 4 with ",
                    a("WikiStart", "WikiStart")),
                element("h5", "Heading 5"),
                element("h6", { id: "anchor-6" }, "Heading 6"));
            generateFragment.call(this, dom, [
                "!1 Heading 1  ",
                "!2 Heading 2    #anchor-2",
                "!3 '''Heading''' ''3''    ",
                "!4 Heading 4 with WikiStart      #アンカー-4",
                "!5 Heading 5      ",
                "!6 Heading 6        #anchor-6" ].join("\n"));
        });

        unit.add("list", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("ul",
                    element("li", "foo bar boo baz"),
                    element("ul", element("li", "Subitem Subitem line 2")),
                    element("li", "item 2 item 2 line 2")),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "Paragraph",
                " * foo bar",
                "   boo baz",
                "   * Subitem",
                "     Subitem line 2",
                " * item 2",
                "   item 2 line 2",
                "Paragraph" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                " * foo bar boo baz",
                "   * Subitem Subitem line 2",
                " * item 2 item 2 line 2",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("list 2", function() {
            var dom = fragment(
                element("ul",
                    element("li", "foo bar boo baz"),
                    element("ul",
                        element("li", "Subitem 1"),
                        element("ul",
                            element("li", "nested item 1"),
                            element("li", "nested item 2 nested item 2 continued")),
                        element("li", "Subitem 2 subitem 2 continued"),
                        element("li", "Subitem 3 subitem 3 continued")),
                    element("li", "item 2 item 2 line 2")),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "    * foo bar",
                "      boo baz",
                "           * Subitem 1",
                "             - nested item 1",
                "             - nested item 2",
                "             nested item 2 continued",
                "            * Subitem 2",
                "             subitem 2 continued",
                "            * Subitem 3",
                "            subitem 3 continued",
                "    * item 2",
                "     item 2 line 2",
                "Paragraph" ].join("\n"));
            generate.call(this, dom, [
                " * foo bar boo baz",
                "   * Subitem 1",
                "     * nested item 1",
                "     * nested item 2 nested item 2 continued",
                "   * Subitem 2 subitem 2 continued",
                "   * Subitem 3 subitem 3 continued",
                " * item 2 item 2 line 2",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("ordered list", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("ol",
                    element("li", "item 1"),
                    element("ol", { "class": "arabiczero" },
                        element("li", "item 1.1"),
                        element("li", "item 1.2"),
                        element("ol", { "class": "loweralpha" },
                            element("li", "item 1.2.a"),
                            element("li", "item 1.2.b")),
                        element("li", "item 1.3"),
                        element("ol", { "class": "loweralpha" },
                            element("li", "item 1.3.a"),
                            element("li", "item 1.3.b")),
                        element("li", "item 1.4"),
                        element("ol", { "class": "upperalpha" },
                            element("li", "item 1.4.A"),
                            element("li", "item 1.4.B"),
                            element("li", "item 1.4.C")),
                        element("li", "item 1.5"),
                        element("ol", { "class": "upperalpha" },
                            element("li", "item 1.5.A")),
                        element("li", "item 1.6"),
                        element("ol", { "class": "lowerroman" },
                            element("li", "item 1.6.i"),
                            element("li", "item 1.6.ii")),
                        element("li", "item 1.7"),
                        element("ol", { "class": "upperroman" },
                            element("li", "item 1.7.I"),
                            element("li", "item 1.7.II")))),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "Paragraph",
                " 1. item 1",
                "   0. item 1.1",
                "   2. item 1.2",
                "     a. item 1.2.a",
                "     z. item 1.2.b",
                "   a. item 1.3",
                "     b. item 1.3.a",
                "     y. item 1.3.b",
                "   Z. item 1.4",
                "     A. item 1.4.A",
                "     z. item 1.4.B",
                "     z. item 1.4.C",
                "   ii. item 1.5",
                "     C. item 1.5.A",
                "   XVI. item 1.6",
                "     i. item 1.6.i",
                "     x. item 1.6.ii",
                "   0. item 1.7",
                "     I. item 1.7.I",
                "     III. item 1.7.II",
                "Paragraph" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                " 1. item 1",
                "   0. item 1.1",
                "   0. item 1.2",
                "     a. item 1.2.a",
                "     a. item 1.2.b",
                "   0. item 1.3",
                "     a. item 1.3.a",
                "     a. item 1.3.b",
                "   0. item 1.4",
                "     A. item 1.4.A",
                "     A. item 1.4.B",
                "     A. item 1.4.C",
                "   0. item 1.5",
                "     A. item 1.5.A",
                "   0. item 1.6",
                "     i. item 1.6.i",
                "     i. item 1.6.ii",
                "   0. item 1.7",
                "     I. item 1.7.I",
                "     I. item 1.7.II",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("list + ordered list", function() {
            var dom = fragment(
                element("ul",
                    element("li", "Item 1"),
                    element("ul", element("li", "Item 1.1")),
                    element("li", "Item 2")),
                element("ol",
                    element("li", "Item 1"),
                    element("ol", { "class": "loweralpha" },
                        element("li", "Item 1.a"),
                        element("li", "Item 1.b"),
                        element("ol", { "class": "lowerroman" },
                            element("li", "Item 1.b.i"),
                            element("li", "Item 1.b.ii"))),
                    element("li", "Item 2")),
                element("p", "And numbered lists can also be given an explicit number:"),
                element("ol", { start: 3 }, element("li", "Item 3")));
            generateFragment.call(this, dom, [
                " * Item 1",
                "   * Item 1.1",
                " * Item 2",
                " 1. Item 1",
                "   a. Item 1.a",
                "   a. Item 1.b",
                "      i. Item 1.b.i",
                "      i. Item 1.b.ii",
                " 1. Item 2",
                "And numbered lists can also be given an explicit number:",
                " 3. Item 3" ].join("\n"));
            generate.call(this, dom, [
                " * Item 1",
                "   * Item 1.1",
                " * Item 2",
                "",
                " 1. Item 1",
                "   a. Item 1.a",
                "   a. Item 1.b",
                "     i. Item 1.b.i",
                "     i. Item 1.b.ii",
                " 1. Item 2",
                "",
                "And numbered lists can also be given an explicit number:",
                "",
                " 3. Item 3" ].join("\n"));
        });

        unit.add("list at beginning of line", function() {
            var dom = fragment(
                element("ul",
                    element("li", "item 1"),
                    element("li", "item 2"),
                    element("ul", element("li", "sub 2.1"), element("li", "sub 2.2"))),
                element("ol", { "class": "loweralpha" },
                    element("li", "item A"), element("li", "item B")),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "- item 1",
                "- item 2",
                "  - sub 2.1",
                "  - sub 2.2",
                "a. item A",
                "b. item B",
                "Paragraph" ].join("\n"));
            generate.call(this, dom, [
                " * item 1",
                " * item 2",
                "   * sub 2.1",
                "   * sub 2.2",
                "",
                " a. item A",
                " a. item B",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("list + code block", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("ul",
                    element("li",
                        "item 1",
                        element("pre", { "class": "wiki" }, "code")),
                    element("ul",
                        element("li",
                            "item 1.1",
                            element("pre", { "class": "wiki" }, "code"),
                            "cont.",
                            element("pre", { "class": "wiki" }, "code"))),
                    element("li",
                        "item 2",
                        element("pre", { "class": "wiki" }, "code"))),
                element("ol", element("li", "item 1")));
            generateFragment.call(this, dom, [
                "Paragraph",
                " * item 1",
                "{{{",
                "code",
                "}}}",
                "   * item 1.1",
                "{{{",
                "code",
                "}}}",
                "     cont.",
                "{{{",
                "code",
                "}}}",
                " * item 2",
                "{{{",
                "code",
                "}}}",
                " 1. item 1" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                " * item 1",
                "{{{",
                "code",
                "}}}",
                "   * item 1.1",
                "{{{",
                "code",
                "}}}",
                "     cont.",
                "{{{",
                "code",
                "}}}",
                " * item 2",
                "{{{",
                "code",
                "}}}",
                "",
                " 1. item 1" ].join("\n"));
        });

        unit.add("list + citation", function() {
            var dom = fragment(
                element('ol', element('li', 'item 1')),
                element('blockquote', { 'class': 'citation' }, element("p", "citation 1")),
                element('ul',
                    element('li', 'item 2'),
                    element("ol",
                        element('li', 'item 2.1'),
                        element('li', 'item 2.2'))),
                element('blockquote', { 'class': 'citation' }, element("p", "citation 2 citation 3")),
                element('ol', element('li', 'item 3')));
            generateFragment.call(this, dom, [
                ' 1. item 1',
                '> citation 1',
                ' * item 2',
                '   1. item 2.1',
                '   1. item 2.2',
                '> citation 2',
                '> citation 3',
                ' 1. item 3' ].join("\n"));
            generateWikitext.call(this, dom, [
                ' 1. item 1',
                '',
                '> citation 1',
                '',
                ' * item 2',
                '   1. item 2.1',
                '   1. item 2.2',
                '',
                '> citation 2 citation 3',
                '',
                ' 1. item 3' ].join("\n"));
        });

        unit.add("blockquote", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("blockquote",
                    element("p", "blockquote 1 cont. 1"),
                    element("blockquote",
                        element("p", "blockquote 1.1"),
                        element("blockquote", element("p", "blockquote 1.1.1 cont. 1.1.1")),
                        element("p", "blockquote 1.2")),
                    element("p", "blockquote 2")),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "Paragraph",
                "  blockquote 1",
                "  cont. 1",
                "    blockquote 1.1",
                "      blockquote 1.1.1",
                "      cont. 1.1.1",
                "     blockquote 1.2",
                "    blockquote 2",
                "Paragraph" ].join("\n"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                "  blockquote 1 cont. 1",
                "    blockquote 1.1",
                "      blockquote 1.1.1 cont. 1.1.1",
                "    blockquote 1.2",
                "  blockquote 2",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("table", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("table",
                    element("tbody",
                        element("tr", element("td", "1.1"), element("td", { colspan: "2" }, "1.2")),
                        element("tr", element("td", { colspan: "3" }, "2.1")),
                        element("tr",
                            element("td", "3.1"),
                            element("td", element("i", "3.2")),
                            element("td", element("tt", "3"), " ", element("tt", "-"))))),
                element("p", "Paragraph"));
            generateFragment.call(this, dom, [
                "Paragraph",
                "|1.1|1.2|",
                "|2.1",
                "|3.1|''3.2''|!-3-! !---!",
                "Paragraph" ].join("\n"));
        });

        unit.add("escaped table", function() {
            var dom = fragment(
                element("table", { "class": "escaped" },
                    element("tbody",
                        element("tr", element("td", " table "), element("td", " ", element("tt", "escaped"), " ")),
                        element("tr", element("td", " ''not italic'' "), element("td", " '''not bold''' ")))));
            generate.call(this, dom, [
                "!| table | !-escaped-! |",
                "| ''not italic'' | '''not bold''' |" ].join("\n"));
        });

        unit.add("table 2", function() {
             var dom = fragment(
                element("p", "Paragraph"),
                element("table",
                    element("tbody",
                        element("tr", element("td", " 1.1 "), element("td", { colspan: "2" }, " 1.2 ")),
                        element("tr", element("td", { colspan: "3" }, " 2.1 ")),
                        element("tr",
                            element("td", " 3.1 "),
                            element("td", " ", element("i", "3.2"), " "),
                            element("td", " ", element("tt", "3"), " ", element("tt", "-"), " ")))),
                element("p", "Paragraph"));
            generate.call(this, dom, [
                "Paragraph",
                "",
                "| 1.1 | 1.2 |",
                "| 2.1 |",
                "| 3.1 | ''3.2'' | !-3-! !---! |",
                "",
                "Paragraph" ].join("\n"));
        });

        unit.add("table + rule", function() {
            var dom = fragment(
                element("table",
                    element("tbody", element("tr", element("td", " 1st ")))),
                element("p", element("b", "bold")),
                element("table",
                    element("tbody", element("tr", element("td", " 2nd ")))),
                element("p", element("tt", "'''normal")));
            generate.call(this, dom, [
                "| 1st |",
                "",
                "'''bold'''",
                "",
                "| 2nd |",
                "",
                "!-'''normal-!" ].join("\n"));
        });

        unit.add("table [ paragraph, ul ]", function() {
            var dom = fragment(
                element("table",
                    element("tbody",
                        element("tr",
                            element("td", element("p", "1.1")),
                            element("td",
                                element("ul",
                                    element("li", "item 1"),
                                    element("li", "item 2")))),
                        element("tr",
                            element("td",
                                element("p", "2.1* item 3 * item 4")
                                    )))));
            generateWikitext.call(this, dom, [
                "| 1.1 | * item 1\n * item 2 |",
                "| 2.1* item 3 * item 4 |" ].join("\n"));
        });

        unit.add("table with incomplete markups", function() {
            var dom = fragment(
                element("table",
                    element("tbody",
                        element("tr",
                            element("td", " ", element("b", element("i", "' "))),
                            element("td", " ", element("b", "bold"), " "))
                    )
                )
            );
            generateFragment.call(this, dom, "| '''''' | '''bold''' |");
        });

        unit.add("table from word", function() {
            var dom = element("div");
            dom.innerHTML = [
                '',
                '<table class="MsoTableGrid" style="border: medium none ; border-collapse: collapse;" border="1" cellpadding="0" cellspacing="0">',
                ' <tbody><tr style="">',
                '  <td style="border: 1pt solid windowtext; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
                '  <p class="MsoNormal"><span lang="EN-US">a<o:p></o:p></span></p>',
                '  <p class="MsoNormal"><span lang="EN-US">b<o:p></o:p></span></p>',
                '  </td>',
                '  <td style="border-style: solid solid solid none; border-color: windowtext windowtext windowtext -moz-use-text-color; border-width: 1pt 1pt 1pt medium; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
                '',
                '  <p class="MsoNormal"><span lang="EN-US">b<o:p></o:p></span></p>',
                '  </td>',
                ' </tr>',
                ' <tr style="">',
                '  <td style="border-style: none solid solid; border-color: -moz-use-text-color windowtext windowtext; border-width: medium 1pt 1pt; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
                '  <p class="MsoNormal"><span lang="EN-US">c<o:p></o:p></span></p>',
                '  </td>',
                '  <td style="border-style: none solid solid none; border-color: -moz-use-text-color windowtext windowtext -moz-use-text-color; border-width: medium 1pt 1pt medium; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
                '',
                '  <p class="MsoNormal"><span lang="EN-US">d<o:p></o:p></span></p>',
                '  </td>',
                ' </tr>',
                '</tbody></table>',
                '' ].join("\n");
            generateWikitext.call(this, dom, [
                "| a\n\nb | b |",
                "| c | d |" ].join("\n"));
        });

        unit.add("domToWikitext for code block", function() {
            var br = function() { return element("br") };
            var dom = fragment(
                element("h1", "Heading", br(), "1"),
                element("h2", "Heading", br(), "2"),
                element("h3", "Heading", br(), "3"),
                element("h4", "Heading", br(), "4"),
                element("h5", "Heading", br(), "5"),
                element("h6", "Heading", br(), "6"),
                element("p",
                    "var TracWysiwyg = function(textarea) {", " ... ", "}"),
                element("blockquote", { "class": "citation" }, element("p", "citation continued")),
                element("blockquote", element("p", "quote", br(), "continued")),
                element("ul",
                    element("li", "item 1", br(), "continued"),
                    element("ol", element("li", "item", br(), "1.1"))),
                element("dl",
                    element("dt", "def to_s(", br(), ")"),
                    element("dd", "dt", br(), "dd")),
                element("table",
                    element("tbody",
                        element("tr",
                            element("td", "cell", br(), "1"),
                            element("th", "cell", br(), "2")))));
            var wikitext = instance.domToWikitext(dom, { formatCodeBlock: true });
            this.assertEqual([
                "!1 Heading 1",
                "!2 Heading 2",
                "!3 Heading 3",
                "!4 Heading 4",
                "!5 Heading 5",
                "!6 Heading 6",
                "var TracWysiwyg = function(textarea) { ... }",
                "",
                "> citation continued",
                "",
                "  quote continued",
                "",
                " * item 1 continued",
                "   1. item 1.1",
                "",
                " def to_s( ):: dt dd",
                "",
                "| cell 1 | cell 2 |" ].join("\n"), wikitext);
        });

        unit.add("selectRange", function() {
            var d = instance.contentDocument;
            function _element() {
                var args = [ d ];
                args.push.apply(args, arguments);
                return element.apply(this, args);
            }
            function _text() {
                var args = [ d ];
                args.push.apply(args, arguments);
                return text.apply(this, args);
            }
            function assertRangeText(expected, start, startOffset, end, endOffset) {
                instance.selectRange(start, startOffset, end, endOffset);
                if (expected instanceof RegExp) {
                    unit.assertMatch(expected, instance.getSelectionText());
                }
                else {
                    unit.assertEqual(expected, instance.getSelectionText());
                }
            }
            var body = d.body;
            while (body.childNodes.length > 0) {
                body.removeChild(body.lastChild);
            }
            body.appendChild(fragment(d,
                _element("p",
                    "The", " quick", " brown",
                    _element("b", " fox", " jumps", " over"),
                    " the", " lazy", " dog."),
                _element("p", "Brick ", "quiz ", "whangs ", "jumpy ", "veldt ", "fox.")));

            var paragraph1 = body.childNodes[0];
            var paragraph2 = body.childNodes[1];
            var bold = paragraph1.childNodes[3];
            assertRangeText("The", paragraph1.childNodes[0], 0, paragraph1.childNodes[0], 3);
            assertRangeText("he", paragraph1.childNodes[0], 1, paragraph1.childNodes[0], 3);
            assertRangeText("e quick brow", paragraph1.childNodes[0], 2, paragraph1.childNodes[2], 5);
            assertRangeText("ick brown", paragraph1.childNodes[1], 3, paragraph1.childNodes[2], 6);
            assertRangeText("ick brown fox j", paragraph1.childNodes[1], 3, bold.childNodes[1], 2);
            assertRangeText("ver the laz", bold.childNodes[2], 2, paragraph1.childNodes[5], 4);
            assertRangeText(" the lazy", paragraph1.childNodes[4], 0, paragraph1.childNodes[5], 5);
            assertRangeText("lazy dog.", paragraph1.childNodes[5], 1, paragraph1.childNodes[6], 5);
            assertRangeText(/^fox jumps over the lazy dog\.[\r\n]*Brick quiz whangs$/,
                bold.childNodes[0], 1, paragraph2.childNodes[2], 6);
            assertRangeText(" fox jumps over", paragraph1, 3, paragraph1, 4);
            assertRangeText(" dog.", paragraph1, 6, paragraph1, 7);
            assertRangeText("", paragraph1, 7, paragraph1, 7);
            assertRangeText("quick brown fox jumps over", paragraph1.childNodes[1], 1, paragraph1, 4);
            assertRangeText(" fox jumps over t", paragraph1, 3, paragraph1.childNodes[4], 2);
        });

        unit.add("Collapsible area", function() {
            var dom = fragment(
                element("fieldset",
                    element("legend", "EXPANDED"),
                    element("p", " Expanded content")),
                element("fieldset", { "class": "collapsed" },
                    element("legend", "COLLAPSED"),
                    element("p", " Collapsed content")),
                element("fieldset", { "class": "hidden" },
                    element("legend", "HIDDEN"),
                    element("p", " Hidden content")));
            generate.call(this, dom, [
                "!*** EXPANDED",
                "Expanded content",
                "",
                "*!",
                "!***> COLLAPSED",
                "Collapsed content",
                "",
                "*!",
                "!***< HIDDEN",
                "Hidden content",
                "",
                "*!"].join("\n"));
        });

        unit.add("Nested collapsible area", function() {
            var dom = fragment(
                element("p", "Paragraph"),
                element("fieldset",
                    element("legend", "outer"),
                    element("p", " Text"),
                    element("fieldset",
                        element("legend", "inner"),
                        element("p", " More text")
                    )));
            generateFragment.call(this, dom, [
                "Paragraph",
                "!*** outer",
                "Text",
                "!*** inner",
                "More text",
                "*!",
                "*!"].join("\n"));
            generateWikitext.call(this, dom, [
                "Paragraph",
                "",
                "!*** outer",
                "Text",
                "",
                "!*** inner",
                "More text",
                "",
                "*!",
                "*!"].join("\n"));
        });

        unit.run();
    }

    var button = document.createElement("button");
    button.innerHTML = "run &#187;";
    button.style.textDecoration = "underline";
    document.body.appendChild(button);
    $(button).click(run);
    button.focus();
});
// vim:et:ai:sw=4
